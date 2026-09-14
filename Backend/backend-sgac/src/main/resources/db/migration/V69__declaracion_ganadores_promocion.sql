create or replace function postulacion.fn_finalizar_proceso_seleccion(p_id_convocatoria integer) returns jsonb
    security definer
    language plpgsql
as
$$
DECLARE
    v_cupos            INTEGER;
    v_posicion         INTEGER := 0;
    v_id_sel           INTEGER;
    v_id_ele           INTEGER;
    v_id_no_sel        INTEGER;
    v_id_est_activa    INTEGER;
    v_horas_semanales  NUMERIC(5,2) := 20.00;   -- horas semanales por defecto
    v_horas_maximas    NUMERIC(5,2);
    v_periodo_academico    INTEGER;
    v_seleccionados    INTEGER := 0;
    v_semanas          INTEGER :=16;
    v_elegibles        INTEGER := 0;
    v_no_sel           INTEGER := 0;
    v_rec              RECORD;
BEGIN

    -- ── Validar que la convocatoria existe y sigue activa ─────────────
    SELECT cupos_disponibles, id_periodo_academico
    INTO   v_cupos, v_periodo_academico
    FROM   convocatoria.convocatoria
    WHERE  id_convocatoria = p_id_convocatoria
      AND  activo = TRUE;

    IF NOT FOUND THEN
        RAISE EXCEPTION 'VALIDACION: La convocatoria % no existe o ya fue cerrada.', p_id_convocatoria;
    END IF;

    -- ── Recuperar IDs de estados de postulación ────────────────────────
    SELECT id_tipo_estado_postulacion INTO v_id_sel
    FROM   postulacion.tipo_estado_postulacion WHERE codigo = 'SELECCIONADO' LIMIT 1;

    SELECT id_tipo_estado_postulacion INTO v_id_ele
    FROM   postulacion.tipo_estado_postulacion WHERE codigo = 'ELEGIBLE' LIMIT 1;

    SELECT id_tipo_estado_postulacion INTO v_id_no_sel
    FROM   postulacion.tipo_estado_postulacion WHERE codigo = 'NO_SELECCIONADO' LIMIT 1;

    IF v_id_sel IS NULL OR v_id_ele IS NULL OR v_id_no_sel IS NULL THEN
        RAISE EXCEPTION
            'VALIDACION: Los estados SELECCIONADO / ELEGIBLE / NO_SELECCIONADO no están configurados en tipo_estado_postulacion.';
    END IF;

    -- ── Recuperar ID del estado ACTIVA de ayudantía ────────────────────
    SELECT id_tipo_estado_ayudantia INTO v_id_est_activa
    FROM   ayudantia.tipo_estado_ayudantia WHERE codigo = 'ACTIVO' LIMIT 1;

    IF v_id_est_activa IS NULL THEN
        RAISE EXCEPTION
            'VALIDACION: El estado ACTIVA no está configurado en tipo_estado_ayudantia.';
    END IF;


    SELECT
        COALESCE((pf.fecha_fin - pf.fecha_inicio) / 7, 16)
    INTO v_semanas
    FROM planificacion.periodo_fase pf
             JOIN planificacion.tipo_fase tf ON tf.id_tipo_fase = pf.id_tipo_fase
    WHERE pf.id_periodo_academico = v_periodo_academico
      AND tf.codigo = 'EJECUCION_ACTIVIDADES';

    v_horas_maximas := v_horas_semanales * v_semanas;

    FOR v_rec IN
        SELECT
            po.id_postulacion,
            u.nombre_usuario,
            u.nombres || ' ' || u.apellidos            AS nombre_completo,
            ROUND(
                    COALESCE(em.nota_total_meritos,          0) +
                    COALESCE(eo.puntaje_total_oposicion,     0)
                , 2)::NUMERIC(5,2)                         AS puntaje_total
        FROM   postulacion.postulacion               po
                   JOIN   academico.estudiante                  est ON est.id_estudiante = po.id_estudiante
                   JOIN   seguridad.usuario                     u   ON u.id_usuario      = est.id_usuario
            -- Evaluación de méritos más reciente (cualquier estado)
                   LEFT JOIN LATERAL (
            SELECT nota_total_meritos
            FROM   postulacion.evaluacion_meritos
            WHERE  id_postulacion = po.id_postulacion
            ORDER  BY id_evaluacion_meritos DESC
            LIMIT  1
            ) em ON TRUE
                   LEFT JOIN LATERAL (
            SELECT eo2.puntaje_total_oposicion
            FROM   postulacion.evaluacion_oposicion  eo2
                       JOIN   postulacion.tipo_estado_evaluacion tee
                              ON tee.id_tipo_estado_evaluacion = eo2.id_tipo_estado_evaluacion
            WHERE  eo2.id_postulacion = po.id_postulacion
              AND  tee.codigo         = 'FINALIZADA'
            ORDER  BY eo2.id_evaluacion_oposicion DESC
            LIMIT  1
            ) eo ON TRUE
        WHERE  po.id_convocatoria = p_id_convocatoria
          AND  po.activo          = TRUE
        ORDER  BY puntaje_total DESC,
                  nombre_completo ASC
        LOOP
            v_posicion := v_posicion + 1;

            IF v_rec.puntaje_total < 25.00 THEN
                UPDATE postulacion.postulacion
                SET    id_tipo_estado_postulacion = v_id_no_sel
                WHERE  id_postulacion             = v_rec.id_postulacion;
                v_no_sel := v_no_sel + 1;

            ELSIF v_posicion <= v_cupos THEN
                UPDATE postulacion.postulacion
                SET    id_tipo_estado_postulacion = v_id_sel
                WHERE  id_postulacion             = v_rec.id_postulacion;

                CALL seguridad.sp_promover_estudiante_a_ayudante(
                        v_rec.nombre_usuario,
                        v_horas_semanales
                     );

                IF NOT EXISTS (
                    SELECT 1 FROM ayudantia.ayudantia
                    WHERE id_postulacion = v_rec.id_postulacion
                ) THEN
                    INSERT INTO ayudantia.ayudantia (
                        id_tipo_estado_ayudantia,
                        id_postulacion,
                        fecha_inicio,
                        horas_semanales_max,
                        horas_maximas
                    ) VALUES (
                                 v_id_est_activa,
                                 v_rec.id_postulacion,
                                 CURRENT_DATE,
                                 v_horas_semanales,
                                 v_horas_maximas
                             );
                END IF;

                v_seleccionados := v_seleccionados + 1;

            ELSE
                UPDATE postulacion.postulacion
                SET    id_tipo_estado_postulacion = v_id_ele
                WHERE  id_postulacion             = v_rec.id_postulacion;
                v_elegibles := v_elegibles + 1;
            END IF;

        END LOOP;

    UPDATE convocatoria.convocatoria
    SET    activo = FALSE,
           estado = 'RESUELTA'
    WHERE  id_convocatoria = p_id_convocatoria;

    RAISE NOTICE '[fn_finalizar_proceso_seleccion] conv=% → sel=%, ele=%, noSel=%',
        p_id_convocatoria, v_seleccionados, v_elegibles, v_no_sel;

    RETURN jsonb_build_object(
            'exito',           TRUE,
            'seleccionados',   v_seleccionados,
            'elegibles',       v_elegibles,
            'noSeleccionados', v_no_sel,
            'mensaje',
            format(
                    'Proceso finalizado. %s seleccionado(s), %s elegible(s), %s no seleccionado(s). Convocatoria cerrada.',
                    v_seleccionados, v_elegibles, v_no_sel
            )
           );

EXCEPTION
    WHEN OTHERS THEN
        IF SQLERRM LIKE 'VALIDACION:%' OR SQLERRM LIKE 'ACCESO:%' THEN RAISE; END IF;
        RAISE EXCEPTION 'Error en fn_finalizar_proceso_seleccion(conv=%): % (SQLSTATE: %)',
            p_id_convocatoria, SQLERRM, SQLSTATE;
END;
$$;

comment on function postulacion.fn_finalizar_proceso_seleccion(integer) is 'Cierre maestro de la fase de selección. Asigna SELECCIONADO / ELEGIBLE / NO_SELECCIONADO, promueve ganadores a AYUDANTE_CATEDRA, crea su ayudantia y marca la convocatoria como RESUELTA.';

alter function postulacion.fn_finalizar_proceso_seleccion(integer) owner to admin1;

grant execute on function postulacion.fn_finalizar_proceso_seleccion(integer) to role_administrador;

grant execute on function postulacion.fn_finalizar_proceso_seleccion(integer) to role_ayudante_catedra;

