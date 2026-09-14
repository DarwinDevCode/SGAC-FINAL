CREATE OR REPLACE FUNCTION postulacion.fn_listar_postulaciones_para_meritos(
    p_id_usuario INTEGER
)
    RETURNS JSONB
    LANGUAGE plpgsql
    SECURITY DEFINER
    STABLE
AS $$
DECLARE
    v_id_periodo  INTEGER;
    v_fase_activa BOOLEAN := FALSE;
    v_resultado   JSONB;
BEGIN
    -- Período activo
    SELECT pa.id_periodo_academico INTO v_id_periodo
    FROM   academico.periodo_academico pa
    WHERE  pa.activo = TRUE AND pa.estado = 'EN PROCESO'
    ORDER  BY pa.id_periodo_academico DESC
    LIMIT  1;

    IF v_id_periodo IS NULL THEN
        RETURN jsonb_build_object(
                'exito',   false,
                'mensaje', 'No existe un período académico activo en estado EN PROCESO.'
               );
    END IF;

    -- ¿Fase de evaluación activa?
    SELECT CURRENT_DATE BETWEEN pf.fecha_inicio AND pf.fecha_fin
    INTO   v_fase_activa
    FROM   planificacion.periodo_fase pf
               JOIN   planificacion.tipo_fase tf ON tf.id_tipo_fase = pf.id_tipo_fase
    WHERE  pf.id_periodo_academico = v_id_periodo
      AND  tf.codigo               = 'EVALUACION_MER_OPO'
    LIMIT 1;

    -- Lista de postulaciones
    SELECT COALESCE(
                   jsonb_agg(
                           jsonb_build_object(
                                   'idPostulacion',     po.id_postulacion,
                                   'nombres',           u.nombres,
                                   'apellidos',         u.apellidos,
                                   'correo',            u.correo,
                                   'matricula',         est.matricula,
                                   'semestreEstudiante',est.semestre,
                                   'nombreAsignatura',  a.nombre_asignatura,
                                   'semestreAsignatura',a.semestre,
                                   'nombreCarrera',     ca.nombre_carrera,
                                   'estadoPostulacion', tep.codigo,
                               -- Estado de la evaluación de méritos (si ya existe)
                                   'estadoEvaluacion',  tee.codigo,
                                   'nombreEstadoEval',  tee.nombre,
                                   'notaTotal',         em.nota_total_meritos,
                                   'fechaEvaluacion',   to_char(em.fecha_evaluacion, 'YYYY-MM-DD'),
                                   'idEvaluacionMeritos', em.id_evaluacion_meritos
                           ) ORDER BY ca.nombre_carrera, a.nombre_asignatura, u.apellidos
                   ),
                   '[]'::jsonb
           )
    INTO   v_resultado
    FROM   postulacion.postulacion                  po
               JOIN   convocatoria.convocatoria          c    ON c.id_convocatoria  = po.id_convocatoria
               JOIN   academico.asignatura               a    ON a.id_asignatura    = c.id_asignatura
               JOIN   academico.carrera                  ca   ON ca.id_carrera      = a.id_carrera
               JOIN   academico.estudiante               est  ON est.id_estudiante  = po.id_estudiante
               JOIN   seguridad.usuario                  u    ON u.id_usuario       = est.id_usuario
               JOIN   postulacion.tipo_estado_postulacion tep ON tep.id_tipo_estado_postulacion = po.id_tipo_estado_postulacion
        -- Evaluación de méritos más reciente (puede ser NULL)
               LEFT JOIN LATERAL (
        SELECT em2.id_evaluacion_meritos,
               em2.nota_total_meritos,
               em2.fecha_evaluacion,
               em2.id_tipo_estado_evaluacion
        FROM   postulacion.evaluacion_meritos em2
        WHERE  em2.id_postulacion = po.id_postulacion
        ORDER  BY em2.id_evaluacion_meritos DESC
        LIMIT  1
        ) lat ON TRUE
               LEFT JOIN postulacion.evaluacion_meritos     em  ON em.id_evaluacion_meritos = lat.id_evaluacion_meritos
               LEFT JOIN postulacion.tipo_estado_evaluacion tee ON tee.id_tipo_estado_evaluacion = em.id_tipo_estado_evaluacion
    WHERE  po.activo = TRUE
      AND  c.activo  = TRUE
      AND  c.id_periodo_academico = v_id_periodo
      AND  tep.codigo IN ('APROBADA', 'EN_EVALUACION')
      -- Solo postulaciones en carreras del coordinador
      AND  ca.id_carrera IN (
        SELECT co.id_carrera
        FROM   academico.coordinador co
        WHERE  co.id_usuario = p_id_usuario
          AND  co.activo     = TRUE
    );

    RETURN jsonb_build_object(
            'exito',         true,
            'faseActiva',    COALESCE(v_fase_activa, FALSE),
            'postulaciones', v_resultado
           );

EXCEPTION WHEN OTHERS THEN
    RETURN jsonb_build_object('exito', false, 'mensaje', '[ERROR] ' || SQLERRM);
END;
$$;


CREATE OR REPLACE FUNCTION postulacion.fn_obtener_evaluacion_meritos(
    p_id_postulacion INTEGER,
    p_id_usuario     INTEGER
)
    RETURNS JSONB
    LANGUAGE plpgsql
    SECURITY DEFINER
    STABLE
AS $$
DECLARE
    v_id_periodo  INTEGER;
    v_fase_activa BOOLEAN := FALSE;
    v_resultado   JSONB;
BEGIN
    SELECT pa.id_periodo_academico INTO v_id_periodo
    FROM   academico.periodo_academico pa
    WHERE  pa.activo = TRUE AND pa.estado = 'EN PROCESO'
    LIMIT  1;

    SELECT CURRENT_DATE BETWEEN pf.fecha_inicio AND pf.fecha_fin
    INTO   v_fase_activa
    FROM   planificacion.periodo_fase pf
               JOIN   planificacion.tipo_fase tf ON tf.id_tipo_fase = pf.id_tipo_fase
    WHERE  pf.id_periodo_academico = v_id_periodo
      AND  tf.codigo               = 'EVALUACION_MER_OPO'
    LIMIT  1;

    SELECT jsonb_build_object(
                   'idPostulacion',      po.id_postulacion,
                   'nombres',            u.nombres,
                   'apellidos',          u.apellidos,
                   'correo',             u.correo,
                   'matricula',          est.matricula,
                   'semestreEstudiante', est.semestre,
                   'nombreAsignatura',   a.nombre_asignatura,
                   'semestreAsignatura', a.semestre,
                   'nombreCarrera',      ca.nombre_carrera,
                   'estadoPostulacion',  tep.codigo,
                   'faseActiva',         COALESCE(v_fase_activa, FALSE),
                   'evaluacion', CASE
                                     WHEN em.id_evaluacion_meritos IS NULL THEN NULL
                                     ELSE jsonb_build_object(
                                             'idEvaluacionMeritos', em.id_evaluacion_meritos,
                                             'notaAsignatura',      em.nota_asignatura,
                                             'notaSemestres',       em.nota_semestres,
                                             'notaExperiencia',     em.nota_experiencia,
                                             'notaEventos',         em.nota_eventos,
                                             'notaTotal',           em.nota_total_meritos,
                                             'estado',              tee.codigo,
                                             'nombreEstado',        tee.nombre,
                                             'fechaEvaluacion',     to_char(em.fecha_evaluacion, 'YYYY-MM-DD')
                                          )
                       END
           )
    INTO   v_resultado
    FROM   postulacion.postulacion                  po
               JOIN   convocatoria.convocatoria          c    ON c.id_convocatoria  = po.id_convocatoria
               JOIN   academico.asignatura               a    ON a.id_asignatura    = c.id_asignatura
               JOIN   academico.carrera                  ca   ON ca.id_carrera      = a.id_carrera
               JOIN   academico.estudiante               est  ON est.id_estudiante  = po.id_estudiante
               JOIN   seguridad.usuario                  u    ON u.id_usuario       = est.id_usuario
               JOIN   postulacion.tipo_estado_postulacion tep ON tep.id_tipo_estado_postulacion = po.id_tipo_estado_postulacion
               LEFT JOIN LATERAL (
        SELECT * FROM postulacion.evaluacion_meritos em2
        WHERE  em2.id_postulacion = po.id_postulacion
        ORDER  BY em2.id_evaluacion_meritos DESC
        LIMIT  1
        ) em  ON TRUE
               LEFT JOIN postulacion.tipo_estado_evaluacion tee ON tee.id_tipo_estado_evaluacion = em.id_tipo_estado_evaluacion
    WHERE  po.id_postulacion = p_id_postulacion;

    IF v_resultado IS NULL THEN
        RETURN jsonb_build_object('exito', false, 'mensaje', 'Postulación no encontrada.');
    END IF;

    RETURN jsonb_build_object('exito', true) || v_resultado;

EXCEPTION WHEN OTHERS THEN
    RETURN jsonb_build_object('exito', false, 'mensaje', '[ERROR] ' || SQLERRM);
END;
$$;


CREATE OR REPLACE FUNCTION postulacion.fn_guardar_evaluacion_meritos(
    p_id_postulacion         INTEGER,
    p_id_usuario             INTEGER,
    p_nota_asignatura_raw    NUMERIC(5,2),  -- nota de aprobación de la asignatura (0–10)
    p_semestres_json         JSONB,          -- [8.5, 9.2, 8.0, …] notas de cada semestre
    p_nota_experiencia       NUMERIC(5,2),
    p_nota_eventos           NUMERIC(5,2),
    p_finalizar              BOOLEAN DEFAULT FALSE
)
    RETURNS JSONB
    LANGUAGE plpgsql
    SECURITY DEFINER
AS $$
DECLARE
    v_id_periodo           INTEGER;
    v_fase_activa          BOOLEAN;
    v_id_carrera           INTEGER;
    v_nota_asignatura      NUMERIC(5,2);
    v_nota_semestres       NUMERIC(5,2);
    v_nota_total           NUMERIC(5,2);
    v_id_evaluacion        INTEGER;
    v_estado_actual        TEXT;
    v_id_estado_borrador   INTEGER;
    v_id_estado_finalizada INTEGER;
    v_id_estado_en_eval    INTEGER;
BEGIN
    -- ── Validar fase ──────────────────────────────────────────────
    SELECT pa.id_periodo_academico INTO v_id_periodo
    FROM   academico.periodo_academico pa
    WHERE  pa.activo = TRUE AND pa.estado = 'EN PROCESO'
    LIMIT  1;

    IF v_id_periodo IS NULL THEN
        RAISE EXCEPTION 'VALIDACION: No existe un período académico activo.';
    END IF;

    SELECT CURRENT_DATE BETWEEN pf.fecha_inicio AND pf.fecha_fin
    INTO   v_fase_activa
    FROM   planificacion.periodo_fase pf
               JOIN   planificacion.tipo_fase tf ON tf.id_tipo_fase = pf.id_tipo_fase
    WHERE  pf.id_periodo_academico = v_id_periodo
      AND  tf.codigo               = 'EVALUACION_MER_OPO'
    LIMIT  1;

    IF NOT COALESCE(v_fase_activa, FALSE) THEN
        RAISE EXCEPTION 'VALIDACION: El período de calificación de méritos no está activo.';
    END IF;

    -- ── Validar acceso del coordinador ────────────────────────────
    SELECT a.id_carrera INTO v_id_carrera
    FROM   postulacion.postulacion    po
               JOIN   convocatoria.convocatoria c  ON c.id_convocatoria = po.id_convocatoria
               JOIN   academico.asignatura      a  ON a.id_asignatura   = c.id_asignatura
    WHERE  po.id_postulacion = p_id_postulacion AND po.activo = TRUE;

    IF NOT FOUND THEN
        RAISE EXCEPTION 'VALIDACION: La postulación no existe o está inactiva.';
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM academico.coordinador co
        WHERE  co.id_usuario = p_id_usuario
          AND  co.id_carrera  = v_id_carrera
          AND  co.activo      = TRUE
    ) THEN
        RAISE EXCEPTION 'ACCESO: No tienes permiso para evaluar esta postulación.';
    END IF;

    -- ── Validar rangos de los campos manuales ─────────────────────
    IF p_nota_experiencia IS NULL OR p_nota_experiencia < 0 OR p_nota_experiencia > 4 THEN
        RAISE EXCEPTION 'VALIDACION: La nota de experiencia debe estar entre 0.00 y 4.00.';
    END IF;
    IF p_nota_eventos IS NULL OR p_nota_eventos < 0 OR p_nota_eventos > 2 THEN
        RAISE EXCEPTION 'VALIDACION: La nota de eventos debe estar entre 0.00 y 2.00.';
    END IF;
    IF p_nota_asignatura_raw IS NULL OR p_nota_asignatura_raw < 0 OR p_nota_asignatura_raw > 10 THEN
        RAISE EXCEPTION 'VALIDACION: La nota de aprobación de la asignatura debe estar entre 0.00 y 10.00.';
    END IF;

    -- ── Calcular nota_asignatura (tabla de conversión) ────────────
    -- 9.50–10.00 → 10 pts | 9.00–9.49 → 9 pts
    -- 8.50–8.99  →  8 pts | 8.00–8.49 → 7 pts | <8.00 → 0 pts
    v_nota_asignatura := CASE
                             WHEN p_nota_asignatura_raw >= 9.50 THEN 10.00
                             WHEN p_nota_asignatura_raw >= 9.00 THEN  9.00
                             WHEN p_nota_asignatura_raw >= 8.50 THEN  8.00
                             WHEN p_nota_asignatura_raw >= 8.00 THEN  7.00
                             ELSE                                      0.00
        END;

    -- ── Calcular nota_semestres (acumulado, tope 4.00) ────────────
    -- Cada semestre aporta según su rango:
    --   [9.50, 10]  → 1.00 pts | [9.00, 9.49] → 0.70 pts
    --   [8.50, 8.99]→ 0.50 pts | [8.00, 8.49] → 0.25 pts
    SELECT LEAST(
                   COALESCE(
                           SUM(
                                   CASE
                                       WHEN n::NUMERIC >= 9.50 THEN 1.00
                                       WHEN n::NUMERIC >= 9.00 THEN 0.70
                                       WHEN n::NUMERIC >= 8.50 THEN 0.50
                                       WHEN n::NUMERIC >= 8.00 THEN 0.25
                                       ELSE                         0.00
                                       END
                           ), 0.00
                   ), 4.00
           )::NUMERIC(5,2)
    INTO   v_nota_semestres
    FROM   jsonb_array_elements_text(COALESCE(p_semestres_json, '[]'::jsonb)) n;

    -- ── Total ──────────────────────────────────────────────────────
    v_nota_total := ROUND(
            v_nota_asignatura + v_nota_semestres + p_nota_experiencia + p_nota_eventos,
            2
                    );

    -- ── IDs de estados ─────────────────────────────────────────────
    SELECT id_tipo_estado_evaluacion INTO v_id_estado_borrador
    FROM   postulacion.tipo_estado_evaluacion WHERE codigo = 'BORRADOR' LIMIT 1;

    SELECT id_tipo_estado_evaluacion INTO v_id_estado_finalizada
    FROM   postulacion.tipo_estado_evaluacion WHERE codigo = 'FINALIZADA' LIMIT 1;

    -- ── Verificar evaluación existente ────────────────────────────
    SELECT em.id_evaluacion_meritos, tee.codigo
    INTO   v_id_evaluacion, v_estado_actual
    FROM   postulacion.evaluacion_meritos         em
               JOIN   postulacion.tipo_estado_evaluacion tee ON tee.id_tipo_estado_evaluacion = em.id_tipo_estado_evaluacion
    WHERE  em.id_postulacion = p_id_postulacion
    ORDER  BY em.id_evaluacion_meritos DESC
    LIMIT  1;

    IF FOUND AND v_estado_actual = 'FINALIZADA' THEN
        RAISE EXCEPTION
            'VALIDACION: La evaluación de méritos ya fue finalizada y no puede modificarse. Use la opción "Reabrir Evaluación" para corregir.';
    END IF;

    IF v_id_evaluacion IS NULL THEN
        -- ── CREAR nuevo registro ──────────────────────────────────
        -- nota_total_meritos es GENERATED ALWAYS AS (...) STORED → PostgreSQL la calcula sola
        INSERT INTO postulacion.evaluacion_meritos (
            id_postulacion,
            nota_asignatura,
            nota_semestres,
            nota_experiencia,
            nota_eventos,
            fecha_evaluacion,
            id_tipo_estado_evaluacion
        ) VALUES (
                     p_id_postulacion,
                     v_nota_asignatura,
                     v_nota_semestres,
                     p_nota_experiencia,
                     p_nota_eventos,
                     CURRENT_DATE,
                     CASE WHEN p_finalizar THEN v_id_estado_finalizada ELSE v_id_estado_borrador END
                 )
        RETURNING id_evaluacion_meritos INTO v_id_evaluacion;

        -- Actualizar postulación → EN_EVALUACION al crear el primer borrador
        SELECT id_tipo_estado_postulacion INTO v_id_estado_en_eval
        FROM   postulacion.tipo_estado_postulacion WHERE codigo = 'EN_EVALUACION' LIMIT 1;

        IF v_id_estado_en_eval IS NOT NULL THEN
            UPDATE postulacion.postulacion
            SET    id_tipo_estado_postulacion = v_id_estado_en_eval
            WHERE  id_postulacion = p_id_postulacion;
        END IF;

    ELSE
        -- ── ACTUALIZAR borrador existente ──────────────────────────
        -- nota_total_meritos es columna generada → se recalcula automáticamente al actualizar
        UPDATE postulacion.evaluacion_meritos
        SET    nota_asignatura            = v_nota_asignatura,
               nota_semestres             = v_nota_semestres,
               nota_experiencia           = p_nota_experiencia,
               nota_eventos               = p_nota_eventos,
               fecha_evaluacion           = CURRENT_DATE,
               id_tipo_estado_evaluacion  = CASE WHEN p_finalizar THEN v_id_estado_finalizada ELSE v_id_estado_borrador END
        WHERE  id_evaluacion_meritos = v_id_evaluacion;
    END IF;

    RETURN jsonb_build_object(
            'exito',               true,
            'mensaje',             CASE WHEN p_finalizar
                                            THEN 'Evaluación de méritos finalizada exitosamente. El puntaje es ahora inmutable.'
                                        ELSE 'Borrador guardado correctamente.'
                END,
            'idEvaluacionMeritos', v_id_evaluacion,
            'notaAsignatura',      v_nota_asignatura,
            'notaSemestres',       v_nota_semestres,
            'notaExperiencia',     p_nota_experiencia,
            'notaEventos',         p_nota_eventos,
            'notaTotal',           v_nota_total,
            'finalizada',          p_finalizar
           );

EXCEPTION
    WHEN OTHERS THEN
        -- Re-lanzar excepciones con prefijo para que GlobalExceptionHandler las maneje
        IF SQLERRM LIKE 'VALIDACION:%' OR SQLERRM LIKE 'ACCESO:%' THEN
            RAISE;
        END IF;
        RETURN jsonb_build_object('exito', false, 'mensaje', '[ERROR] ' || SQLERRM);
END;
$$;


-- ══════════════════════════════════════════════════════════════════
-- 5. fn_reabrir_evaluacion_meritos(p_id_postulacion, p_id_usuario)
--    Cambia el estado de FINALIZADA → BORRADOR.
--    Solo disponible mientras la fase EVALUACION_MER_OPO esté activa.
-- ══════════════════════════════════════════════════════════════════
CREATE OR REPLACE FUNCTION postulacion.fn_reabrir_evaluacion_meritos(
    p_id_postulacion INTEGER,
    p_id_usuario     INTEGER
)
    RETURNS JSONB
    LANGUAGE plpgsql
    SECURITY DEFINER
AS $$
DECLARE
    v_id_periodo           INTEGER;
    v_fase_activa          BOOLEAN;
    v_id_evaluacion        INTEGER;
    v_estado_actual        TEXT;
    v_id_carrera           INTEGER;
    v_id_estado_borrador   INTEGER;
BEGIN
    -- Validar fase
    SELECT pa.id_periodo_academico INTO v_id_periodo
    FROM   academico.periodo_academico pa
    WHERE  pa.activo = TRUE AND pa.estado = 'EN PROCESO' LIMIT 1;

    SELECT CURRENT_DATE BETWEEN pf.fecha_inicio AND pf.fecha_fin INTO v_fase_activa
    FROM   planificacion.periodo_fase pf
               JOIN   planificacion.tipo_fase tf ON tf.id_tipo_fase = pf.id_tipo_fase
    WHERE  pf.id_periodo_academico = v_id_periodo
      AND  tf.codigo = 'EVALUACION_MER_OPO'
    LIMIT  1;

    IF NOT COALESCE(v_fase_activa, FALSE) THEN
        RAISE EXCEPTION 'VALIDACION: El período de calificación de méritos no está activo. No se puede reabrir la evaluación.';
    END IF;

    -- Validar acceso del coordinador
    SELECT a.id_carrera INTO v_id_carrera
    FROM   postulacion.postulacion    po
               JOIN   convocatoria.convocatoria c  ON c.id_convocatoria = po.id_convocatoria
               JOIN   academico.asignatura      a  ON a.id_asignatura   = c.id_asignatura
    WHERE  po.id_postulacion = p_id_postulacion;

    IF NOT EXISTS (
        SELECT 1 FROM academico.coordinador co
        WHERE  co.id_usuario = p_id_usuario AND co.id_carrera = v_id_carrera AND co.activo = TRUE
    ) THEN
        RAISE EXCEPTION 'ACCESO: No tienes permiso para reabrir esta evaluación.';
    END IF;

    -- Obtener evaluación actual
    SELECT em.id_evaluacion_meritos, tee.codigo
    INTO   v_id_evaluacion, v_estado_actual
    FROM   postulacion.evaluacion_meritos em
               JOIN   postulacion.tipo_estado_evaluacion tee ON tee.id_tipo_estado_evaluacion = em.id_tipo_estado_evaluacion
    WHERE  em.id_postulacion = p_id_postulacion
    ORDER  BY em.id_evaluacion_meritos DESC
    LIMIT  1;

    IF NOT FOUND THEN
        RAISE EXCEPTION 'VALIDACION: No existe una evaluación de méritos para esta postulación.';
    END IF;

    IF v_estado_actual != 'FINALIZADA' THEN
        RAISE EXCEPTION 'VALIDACION: Solo se puede reabrir una evaluación con estado FINALIZADA. Estado actual: %', v_estado_actual;
    END IF;

    SELECT id_tipo_estado_evaluacion INTO v_id_estado_borrador
    FROM   postulacion.tipo_estado_evaluacion WHERE codigo = 'BORRADOR' LIMIT 1;

    UPDATE postulacion.evaluacion_meritos
    SET    id_tipo_estado_evaluacion = v_id_estado_borrador
    WHERE  id_evaluacion_meritos     = v_id_evaluacion;

    RETURN jsonb_build_object(
            'exito',   true,
            'mensaje', 'Evaluación reabierta exitosamente. Puedes corregir los puntajes.'
           );

EXCEPTION
    WHEN OTHERS THEN
        IF SQLERRM LIKE 'VALIDACION:%' OR SQLERRM LIKE 'ACCESO:%' THEN RAISE; END IF;
        RETURN jsonb_build_object('exito', false, 'mensaje', '[ERROR] ' || SQLERRM);
END;
$$;


-- ── Permisos ──────────────────────────────────────────────────────
GRANT EXECUTE ON FUNCTION postulacion.fn_listar_postulaciones_para_meritos(INTEGER) TO app_user_default;
GRANT EXECUTE ON FUNCTION postulacion.fn_obtener_evaluacion_meritos(INTEGER, INTEGER)  TO app_user_default;
GRANT EXECUTE ON FUNCTION postulacion.fn_guardar_evaluacion_meritos(INTEGER, INTEGER, NUMERIC, JSONB, NUMERIC, NUMERIC, BOOLEAN) TO app_user_default;
GRANT EXECUTE ON FUNCTION postulacion.fn_reabrir_evaluacion_meritos(INTEGER, INTEGER)  TO app_user_default;