-- ============================================================
-- V68 — Mejora de algoritmos de desempate en Ranking
-- Prioridad: Total > Oposición > Méritos > Alfabético
-- ============================================================

CREATE OR REPLACE FUNCTION postulacion.fn_obtener_ranking_resultados(
    p_id_usuario INTEGER,
    p_rol        TEXT
)
    RETURNS JSONB
    LANGUAGE plpgsql
    SECURITY DEFINER
    STABLE
AS $$
DECLARE
    v_rol        TEXT;
    v_id_periodo INTEGER;
    v_ventana_ok BOOLEAN := FALSE;
    v_resultados JSONB;
BEGIN
    v_rol := UPPER(TRIM(COALESCE(p_rol, '')));

    -- 1. Obtener período activo
    SELECT pa.id_periodo_academico
    INTO   v_id_periodo
    FROM   academico.periodo_academico pa
    WHERE  pa.activo = TRUE
      AND  pa.estado = 'EN PROCESO'
    ORDER  BY pa.id_periodo_academico DESC
    LIMIT  1;

    IF v_id_periodo IS NULL THEN
        RETURN jsonb_build_object(
                'exito',   false,
                'mensaje', 'No existe un período académico activo en estado EN PROCESO.'
               );
    END IF;

    -- 2. Validar ventana de visualización (Desde RESULTADOS_FINALES hasta CIERRE_ADMINISTRATIVO)
    SELECT
                CURRENT_DATE >= pf_ini.fecha_inicio
            AND CURRENT_DATE <= pf_fin.fecha_fin
    INTO v_ventana_ok
    FROM
        planificacion.periodo_fase pf_ini
            JOIN planificacion.tipo_fase tf_ini
                 ON tf_ini.id_tipo_fase = pf_ini.id_tipo_fase
                     AND tf_ini.codigo      = 'RESULTADOS_FINALES',
        planificacion.periodo_fase pf_fin
            JOIN planificacion.tipo_fase tf_fin
                 ON tf_fin.id_tipo_fase = pf_fin.id_tipo_fase
                     AND tf_fin.codigo      = 'CIERRE_ADMINISTRATIVO'
    WHERE pf_ini.id_periodo_academico = v_id_periodo
      AND pf_fin.id_periodo_academico = v_id_periodo;

    IF NOT COALESCE(v_ventana_ok, FALSE) THEN
        RETURN jsonb_build_object(
                'exito',           false,
                'faseNoPublicada', true,
                'mensaje',         'Los resultados finales aún no han sido publicados oficialmente.'
               );
    END IF;

    -- 3. Generar ranking con lógica de desempate mejorada
    WITH base AS (
        SELECT
            po.id_postulacion,
            po.id_convocatoria,
            est.id_usuario,
            u.nombres || ' ' || u.apellidos                              AS postulante,
            COALESCE(em.nota_total_meritos,          0.0)::NUMERIC(5,2)  AS meritos,
            COALESCE(eo_fin.puntaje_total_oposicion, 0.0)::NUMERIC(5,2)  AS oposicion,
            ROUND(
                    COALESCE(em.nota_total_meritos,          0.0) +
                    COALESCE(eo_fin.puntaje_total_oposicion, 0.0)
                , 2)::NUMERIC(5,2)                                            AS total,
            c.cupos_disponibles,
            a.nombre_asignatura,
            a.semestre,
            a.id_asignatura,
            ca.nombre_carrera,
            ca.id_carrera,
            fa.nombre_facultad,
            fa.id_facultad
        FROM   postulacion.postulacion          po
                   JOIN convocatoria.convocatoria      c   ON c.id_convocatoria  = po.id_convocatoria
                   JOIN academico.asignatura           a   ON a.id_asignatura    = c.id_asignatura
                   JOIN academico.carrera              ca  ON ca.id_carrera      = a.id_carrera
                   JOIN academico.facultad             fa  ON fa.id_facultad     = ca.id_facultad
                   JOIN academico.estudiante           est ON est.id_estudiante  = po.id_estudiante
                   JOIN seguridad.usuario              u   ON u.id_usuario       = est.id_usuario
                   LEFT JOIN LATERAL (
            SELECT nota_total_meritos
            FROM   postulacion.evaluacion_meritos
            WHERE  id_postulacion = po.id_postulacion
            ORDER  BY id_evaluacion_meritos DESC
            LIMIT  1
            ) em ON TRUE
                   LEFT JOIN LATERAL (
            SELECT eo.puntaje_total_oposicion
            FROM   postulacion.evaluacion_oposicion   eo
                       JOIN postulacion.tipo_estado_evaluacion tee
                            ON tee.id_tipo_estado_evaluacion = eo.id_tipo_estado_evaluacion
            WHERE  eo.id_postulacion = po.id_postulacion
              AND  tee.codigo        = 'FINALIZADA'
            ORDER  BY eo.id_evaluacion_oposicion DESC
            LIMIT  1
            ) eo_fin ON TRUE
        WHERE po.activo = TRUE
          AND c.activo  = TRUE
          AND c.id_periodo_academico = v_id_periodo
    ),

         ranked AS (
             SELECT
                 b.*,
                 RANK() OVER (
                     PARTITION BY b.id_convocatoria
                     ORDER BY 
                        b.total DESC,       -- 1. Puntaje Total
                        b.oposicion DESC,   -- 2. Desempate: Oposición
                        b.meritos DESC,     -- 3. Desempate: Méritos
                        b.postulante ASC    -- 4. Alfabético
                     )::INTEGER AS posicion
             FROM base b
         ),
         con_estado AS (
             SELECT
                 r.*,
                 CASE
                     WHEN r.total < 15.00                                THEN 'NO_SELECCIONADO' -- Umbral mínimo si aplica, sino 0
                     WHEN r.posicion <= r.cupos_disponibles              THEN 'SELECCIONADO'
                     ELSE                                                     'ELEGIBLE'
                     END AS estado
             FROM ranked r
         )

    -- 4. Filtrado según el rol del usuario que consulta
    SELECT COALESCE(jsonb_agg(
                            jsonb_build_object(
                                    'posicion',         ce.posicion,
                                    'postulante',       ce.postulante,
                                    'meritos',          ce.meritos,
                                    'oposicion',        ce.oposicion,
                                    'total',            ce.total,
                                    'estado',           ce.estado,
                                    'asignatura',       ce.nombre_asignatura,
                                    'semestre',         ce.semestre,
                                    'carrera',          ce.nombre_carrera,
                                    'facultad',         ce.nombre_facultad,
                                    'cuposDisponibles', ce.cupos_disponibles
                            ) ORDER BY ce.nombre_asignatura, ce.posicion
                     ), '[]'::jsonb)
    INTO v_resultados
    FROM con_estado ce
    WHERE CASE v_rol
              WHEN 'ESTUDIANTE' THEN
                  ce.id_usuario = p_id_usuario

              WHEN 'COORDINADOR' THEN
                  ce.id_carrera IN (
                      SELECT co.id_carrera
                      FROM   academico.coordinador co
                      WHERE  co.id_usuario = p_id_usuario AND co.activo = TRUE
                  )

              WHEN 'DECANO' THEN
                  ce.id_facultad IN (
                      SELECT de.id_facultad
                      FROM   academico.decano de
                      WHERE  de.id_usuario = p_id_usuario AND de.activo = TRUE
                  )

              WHEN 'DOCENTE' THEN
                  ce.id_convocatoria IN (
                      SELECT DISTINCT cs.id_convocatoria
                      FROM   postulacion.comision_seleccion  cs
                                 JOIN seguridad.usuario_comision    uc
                                      ON uc.id_comision_seleccion = cs.id_comision_seleccion
                      WHERE  uc.id_usuario = p_id_usuario
                        AND  cs.activo     = TRUE
                        AND  uc.activo     = TRUE
                  )

              WHEN 'ADMINISTRADOR' THEN TRUE
              ELSE FALSE
              END = TRUE;

    RETURN jsonb_build_object(
            'exito',      true,
            'resultados', v_resultados
           );

EXCEPTION WHEN OTHERS THEN
    RETURN jsonb_build_object('exito', false, 'mensaje', '[ERROR] ' || SQLERRM);
END;
$$;
