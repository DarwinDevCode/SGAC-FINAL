-- V63__fix_timer_sync_validacion.sql
-- ─────────────────────────────────────────────────────────────────────
-- ESCENARIO 1: Sincronización del timer desde el servidor
--   Se reescribe fn_cambiar_estado_evaluacion para que el mensaje de
--   respuesta incluya el timestamp exacto del servidor (en UTC) en que
--   se registró hora_inicio_real.  El frontend calcula:
--     Tiempo_Restante = BLOQUE_TOTAL - (now_server - inicio_server)
--   usando ese valor, eliminando cualquier deriva de reloj del cliente.
--
-- ESCENARIO 3: Validación de rangos con RAISE EXCEPTION
--   Se reescribe fn_registrar_puntaje_jurado para que los rangos
--   inválidos lancen una excepción PL/pgSQL en lugar de hacer RETURN.
--   Spring los captura como DataIntegrityViolationException → 422.
-- ─────────────────────────────────────────────────────────────────────


-- ════════════════════════════════════════════════════════════════════
-- FIX 1: fn_cambiar_estado_evaluacion  — devuelve serverTimestamp
-- ════════════════════════════════════════════════════════════════════
CREATE OR REPLACE FUNCTION postulacion.fn_cambiar_estado_evaluacion(
    p_id_evaluacion_oposicion INTEGER,
    p_accion                  TEXT
)
    RETURNS JSONB
    LANGUAGE plpgsql
    SECURITY DEFINER
AS $$
DECLARE
    v_codigo_actual   TEXT;
    v_id_estado_nuevo INTEGER;
    v_puntaje_total   NUMERIC(5,2);
    v_id_comision     INTEGER;
    v_hora_inicio_now TIME;
    v_ts_inicio       TEXT;   -- ← nuevo: ISO-8601 UTC del servidor
BEGIN
    p_accion := UPPER(TRIM(COALESCE(p_accion, '')));

    SELECT tee.codigo
    INTO   v_codigo_actual
    FROM   postulacion.evaluacion_oposicion   eo
               JOIN   postulacion.tipo_estado_evaluacion tee
                      ON tee.id_tipo_estado_evaluacion = eo.id_tipo_estado_evaluacion
    WHERE  eo.id_evaluacion_oposicion = p_id_evaluacion_oposicion;

    IF NOT FOUND THEN
        RETURN jsonb_build_object('exito', false, 'mensaje', 'Evaluación no encontrada.');
    END IF;

    -- ── INICIAR ───────────────────────────────────────────────────────
    IF p_accion = 'INICIAR' THEN
        IF v_codigo_actual != 'PROGRAMADA' THEN
            RETURN jsonb_build_object(
                    'exito',   false,
                    'mensaje', 'Solo se puede iniciar una evaluación PROGRAMADA. Estado actual: ' || v_codigo_actual
                   );
        END IF;

        SELECT id_tipo_estado_evaluacion INTO v_id_estado_nuevo
        FROM   postulacion.tipo_estado_evaluacion WHERE codigo = 'EN_CURSO';

        -- Capturar hora exacta UNA SOLA VEZ para coherencia
        v_hora_inicio_now := LOCALTIME;
        -- ISO-8601 UTC: el frontend lo parsea con new Date(ts) — independiente del TZ del cliente
        v_ts_inicio := to_char(NOW() AT TIME ZONE 'UTC', 'YYYY-MM-DD"T"HH24:MI:SS"Z"');

        UPDATE postulacion.evaluacion_oposicion
        SET    id_tipo_estado_evaluacion = v_id_estado_nuevo,
               hora_inicio_real          = v_hora_inicio_now
        WHERE  id_evaluacion_oposicion   = p_id_evaluacion_oposicion;

        RETURN jsonb_build_object(
                'exito',           true,
                'mensaje',         'Evaluación iniciada. El tribunal puede comenzar a calificar.',
                'horaReal',        to_char(v_hora_inicio_now, 'HH24:MI:SS'),
                'serverTimestamp', v_ts_inicio   -- ← campo nuevo para el timer del frontend
               );
    END IF;

    -- ── NO_PRESENTO ────────────────────────────────────────────────────
    IF p_accion = 'NO_PRESENTO' THEN
        IF v_codigo_actual != 'PROGRAMADA' THEN
            RETURN jsonb_build_object(
                    'exito',   false,
                    'mensaje', 'Solo se puede marcar como No Presentó desde estado PROGRAMADA.'
                   );
        END IF;

        SELECT id_tipo_estado_evaluacion INTO v_id_estado_nuevo
        FROM   postulacion.tipo_estado_evaluacion WHERE codigo = 'NO_PRESENTO';

        UPDATE postulacion.evaluacion_oposicion
        SET    id_tipo_estado_evaluacion = v_id_estado_nuevo
        WHERE  id_evaluacion_oposicion   = p_id_evaluacion_oposicion;

        RETURN jsonb_build_object('exito', true, 'mensaje', 'Postulante marcado como No Presentó.');
    END IF;

    -- ── FINALIZAR ──────────────────────────────────────────────────────
    IF p_accion = 'FINALIZAR' THEN
        IF v_codigo_actual != 'EN_CURSO' THEN
            RETURN jsonb_build_object(
                    'exito',   false,
                    'mensaje', 'Solo se puede finalizar una evaluación EN CURSO. Estado actual: ' || v_codigo_actual
                   );
        END IF;

        SELECT cs.id_comision_seleccion INTO v_id_comision
        FROM   postulacion.evaluacion_oposicion eo
                   JOIN   postulacion.postulacion          p  ON p.id_postulacion   = eo.id_postulacion
                   JOIN   postulacion.comision_seleccion   cs ON cs.id_convocatoria = p.id_convocatoria
        WHERE  eo.id_evaluacion_oposicion = p_id_evaluacion_oposicion
          AND  cs.activo = TRUE
        LIMIT 1;

        SELECT ROUND(
                       COALESCE(SUM(
                                        COALESCE(uc.puntaje_material,   0) +
                                        COALESCE(uc.puntaje_exposicion, 0) +
                                        COALESCE(uc.puntaje_respuestas, 0)
                                ), 0) / 3.0
                   , 2)
        INTO   v_puntaje_total
        FROM   seguridad.usuario_comision uc
        WHERE  uc.id_comision_seleccion    = v_id_comision
          AND  uc.id_evaluacion_oposicion  = p_id_evaluacion_oposicion
          AND  uc.activo                   = TRUE;

        UPDATE seguridad.usuario_comision
        SET    finalizo_calificacion = TRUE
        WHERE  id_comision_seleccion   = v_id_comision
          AND  id_evaluacion_oposicion = p_id_evaluacion_oposicion
          AND  activo = TRUE;

        SELECT id_tipo_estado_evaluacion INTO v_id_estado_nuevo
        FROM   postulacion.tipo_estado_evaluacion WHERE codigo = 'FINALIZADA';

        UPDATE postulacion.evaluacion_oposicion
        SET    puntaje_total_oposicion   = COALESCE(v_puntaje_total, 0),
               hora_fin_real             = LOCALTIME,
               id_tipo_estado_evaluacion = v_id_estado_nuevo
        WHERE  id_evaluacion_oposicion   = p_id_evaluacion_oposicion;

        RETURN jsonb_build_object(
                'exito',        true,
                'mensaje',      'Evaluación finalizada y bloqueada.',
                'puntajeFinal', COALESCE(v_puntaje_total, 0),
                'horaFin',      to_char(LOCALTIME, 'HH24:MI:SS')
               );
    END IF;

    RETURN jsonb_build_object('exito', false, 'mensaje', 'Acción no reconocida: ' || p_accion);

EXCEPTION WHEN OTHERS THEN
    RETURN jsonb_build_object('exito', false, 'mensaje', '[ERROR] ' || SQLERRM);
END;
$$;


-- ════════════════════════════════════════════════════════════════════
-- FIX 2: fn_consultar_cronograma_oposicion — incluye serverTimestamp
--   Para el Efecto F5: al recargar, el frontend recibe el timestamp
--   de inicio del servidor directamente en la respuesta del cronograma.
--   Si el turno está EN_CURSO, calcula cuánto tiempo ya transcurrió.
-- ════════════════════════════════════════════════════════════════════
CREATE OR REPLACE FUNCTION postulacion.fn_consultar_cronograma_oposicion(
    p_id_convocatoria INTEGER
)
    RETURNS JSONB
    LANGUAGE plpgsql
    SECURITY DEFINER
    STABLE
AS $$
BEGIN
    RETURN jsonb_build_object(
            'exito', true,
            'cronograma', COALESCE(
                    (SELECT jsonb_agg(
                                    jsonb_build_object(
                                            'idEvaluacionOposicion', eo.id_evaluacion_oposicion,
                                            'orden',                 eo.orden_exposicion,
                                            'nombres',               u.nombres,
                                            'apellidos',             u.apellidos,
                                            'correo',                u.correo,
                                            'tema',                  eo.tema_exposicion,
                                            'fecha',                 to_char(eo.fecha_evaluacion, 'YYYY-MM-DD'),
                                            'horaInicio',            to_char(eo.hora_inicio,      'HH24:MI'),
                                            'horaFin',               to_char(eo.hora_fin,         'HH24:MI'),
                                            'horaInicioReal',        to_char(eo.hora_inicio_real, 'HH24:MI:SS'),
                                            'horaFinReal',           to_char(eo.hora_fin_real,    'HH24:MI'),
                                            'lugar',                 eo.lugar,
                                            'estado',                tee.codigo,
                                            'nombreEstado',          tee.nombre,
                                            'puntajeFinal',          eo.puntaje_total_oposicion,
                                        -- ← nuevo: ISO-8601 UTC del momento en que empezó este turno
                                            'serverTimestamp',       CASE WHEN tee.codigo = 'EN_CURSO'
                                                                              THEN to_char(
                                                (eo.fecha_evaluacion + eo.hora_inicio_real) AT TIME ZONE 'America/Guayaquil' AT TIME ZONE 'UTC',
                                                'YYYY-MM-DD"T"HH24:MI:SS"Z"'
                                                                                   )
                                                                          ELSE NULL END,
                                            'jurados', (
                                                SELECT COALESCE(jsonb_agg(jsonb_build_object(
                                                                                  'idUsuario',           uj.id_usuario,
                                                                                  'nombres',             uj2.nombres,
                                                                                  'apellidos',           uj2.apellidos,
                                                                                  'rol',                 uj.rol_integrante,
                                                                                  'puntajeMaterial',     uj.puntaje_material,
                                                                                  'puntajeExposicion',   uj.puntaje_exposicion,
                                                                                  'puntajeRespuestas',   uj.puntaje_respuestas,
                                                                                  'subtotal',            COALESCE(uj.puntaje_material, 0)
                                                                                      + COALESCE(uj.puntaje_exposicion, 0)
                                                                                      + COALESCE(uj.puntaje_respuestas, 0),
                                                                                  'finalizo',            uj.finalizo_calificacion
                                                                          ) ORDER BY uj.rol_integrante), '[]'::jsonb)
                                                FROM   seguridad.usuario_comision uj
                                                           JOIN   seguridad.usuario          uj2 ON uj2.id_usuario = uj.id_usuario
                                                WHERE  uj.id_evaluacion_oposicion = eo.id_evaluacion_oposicion
                                                   OR (uj.id_evaluacion_oposicion IS NULL
                                                    AND uj.id_comision_seleccion = (
                                                        SELECT cs.id_comision_seleccion
                                                        FROM   postulacion.comision_seleccion cs
                                                        WHERE  cs.id_convocatoria = p_id_convocatoria
                                                          AND  cs.activo = TRUE
                                                        LIMIT 1
                                                    ))
                                            )
                                    ) ORDER BY eo.orden_exposicion
                            )
                     FROM  postulacion.evaluacion_oposicion   eo
                               JOIN  postulacion.postulacion              p   ON p.id_postulacion   = eo.id_postulacion
                               JOIN  academico.estudiante                 est ON est.id_estudiante  = p.id_estudiante
                               JOIN  seguridad.usuario                    u   ON u.id_usuario       = est.id_usuario
                               JOIN  postulacion.tipo_estado_evaluacion   tee ON tee.id_tipo_estado_evaluacion = eo.id_tipo_estado_evaluacion
                     WHERE p.id_convocatoria = p_id_convocatoria
                    ),
                    '[]'::jsonb
                          )
           );

EXCEPTION WHEN OTHERS THEN
    RETURN jsonb_build_object('exito', false, 'mensaje', '[ERROR] ' || SQLERRM);
END;
$$;


-- ════════════════════════════════════════════════════════════════════
-- FIX 3: fn_registrar_puntaje_jurado — RAISE en lugar de RETURN
--   Los rangos inválidos ahora lanzan una excepción que el
--   GlobalExceptionHandler captura y convierte en HTTP 400.
-- ════════════════════════════════════════════════════════════════════
CREATE OR REPLACE FUNCTION postulacion.fn_registrar_puntaje_jurado(
    p_id_evaluacion_oposicion INTEGER,
    p_id_usuario              INTEGER,
    p_puntaje_material        NUMERIC(5,2),
    p_puntaje_exposicion      NUMERIC(5,2),
    p_puntaje_respuestas      NUMERIC(5,2),
    p_finalizar               BOOLEAN DEFAULT FALSE
)
    RETURNS JSONB
    LANGUAGE plpgsql
    SECURITY DEFINER
AS $$
DECLARE
    v_codigo_estado     TEXT;
    v_finalizo          BOOLEAN;
    v_id_comision       INTEGER;
    v_todos_finalizaron BOOLEAN;
    v_puntaje_total     NUMERIC(5,2);
BEGIN
    -- ── Validación de rangos con RAISE (causa HTTP 400 vía GlobalExceptionHandler) ──
    IF p_puntaje_material IS NULL OR p_puntaje_exposicion IS NULL OR p_puntaje_respuestas IS NULL THEN
        RAISE EXCEPTION 'VALIDACION: Los tres criterios de puntaje son obligatorios.';
    END IF;
    IF p_puntaje_material < 0 OR p_puntaje_material > 10 THEN
        RAISE EXCEPTION 'VALIDACION: El puntaje de material debe estar entre 0.00 y 10.00. Recibido: %', p_puntaje_material;
    END IF;
    IF p_puntaje_exposicion < 0 OR p_puntaje_exposicion > 4 THEN
        RAISE EXCEPTION 'VALIDACION: El puntaje de exposición debe estar entre 0.00 y 4.00. Recibido: %', p_puntaje_exposicion;
    END IF;
    IF p_puntaje_respuestas < 0 OR p_puntaje_respuestas > 6 THEN
        RAISE EXCEPTION 'VALIDACION: El puntaje de respuestas debe estar entre 0.00 y 6.00. Recibido: %', p_puntaje_respuestas;
    END IF;

    -- ── Estado de la evaluación ────────────────────────────────────────
    SELECT tee.codigo
    INTO   v_codigo_estado
    FROM   postulacion.evaluacion_oposicion   eo
               JOIN   postulacion.tipo_estado_evaluacion tee
                      ON tee.id_tipo_estado_evaluacion = eo.id_tipo_estado_evaluacion
    WHERE  eo.id_evaluacion_oposicion = p_id_evaluacion_oposicion;

    IF NOT FOUND THEN
        RAISE EXCEPTION 'VALIDACION: Evaluación con id % no encontrada.', p_id_evaluacion_oposicion;
    END IF;

    IF v_codigo_estado != 'EN_CURSO' THEN
        RAISE EXCEPTION 'VALIDACION: Solo se puede calificar cuando la evaluación está EN CURSO. Estado actual: %', v_codigo_estado;
    END IF;

    -- ── Verificar miembro de comisión ──────────────────────────────────
    SELECT uc.id_comision_seleccion, uc.finalizo_calificacion
    INTO   v_id_comision, v_finalizo
    FROM   seguridad.usuario_comision      uc
               JOIN   postulacion.comision_seleccion  cs ON cs.id_comision_seleccion = uc.id_comision_seleccion
               JOIN   postulacion.postulacion          p  ON p.id_convocatoria       = cs.id_convocatoria
               JOIN   postulacion.evaluacion_oposicion eo ON eo.id_postulacion       = p.id_postulacion
    WHERE  eo.id_evaluacion_oposicion = p_id_evaluacion_oposicion
      AND  uc.id_usuario              = p_id_usuario
      AND  uc.activo                  = TRUE
      AND  cs.activo                  = TRUE
    LIMIT 1;

    IF NOT FOUND THEN
        RAISE EXCEPTION 'ACCESO: El usuario % no pertenece al tribunal de esta convocatoria.', p_id_usuario;
    END IF;

    IF v_finalizo THEN
        RAISE EXCEPTION 'VALIDACION: El jurado ya finalizó su calificación y no puede modificarla.';
    END IF;

    -- ── Guardar notas ──────────────────────────────────────────────────
    UPDATE seguridad.usuario_comision
    SET    puntaje_material        = p_puntaje_material,
           puntaje_exposicion      = p_puntaje_exposicion,
           puntaje_respuestas      = p_puntaje_respuestas,
           id_evaluacion_oposicion = p_id_evaluacion_oposicion,
           fecha_evaluacion        = CURRENT_DATE,
           finalizo_calificacion   = p_finalizar
    WHERE  id_usuario            = p_id_usuario
      AND  id_comision_seleccion = v_id_comision
      AND  activo                = TRUE;

    IF p_finalizar THEN
        SELECT BOOL_AND(uc.finalizo_calificacion) INTO v_todos_finalizaron
        FROM   seguridad.usuario_comision uc
        WHERE  uc.id_comision_seleccion = v_id_comision
          AND  uc.activo = TRUE;

        IF v_todos_finalizaron THEN
            SELECT ROUND(
                           COALESCE(SUM(
                                            COALESCE(uc.puntaje_material,   0) +
                                            COALESCE(uc.puntaje_exposicion, 0) +
                                            COALESCE(uc.puntaje_respuestas, 0)
                                    ), 0) / 3.0
                       , 2)
            INTO   v_puntaje_total
            FROM   seguridad.usuario_comision uc
            WHERE  uc.id_comision_seleccion = v_id_comision
              AND  uc.activo = TRUE;

            UPDATE postulacion.evaluacion_oposicion
            SET    puntaje_total_oposicion   = v_puntaje_total,
                   id_tipo_estado_evaluacion = (
                       SELECT id_tipo_estado_evaluacion
                       FROM   postulacion.tipo_estado_evaluacion
                       WHERE  codigo = 'FINALIZADA'
                   )
            WHERE  id_evaluacion_oposicion = p_id_evaluacion_oposicion;

            RETURN jsonb_build_object(
                    'exito',            true,
                    'mensaje',          'Todos los jurados finalizaron. Nota final calculada.',
                    'todosFinalizaron', true,
                    'puntajeFinal',     v_puntaje_total
                   );
        END IF;
    END IF;

    RETURN jsonb_build_object(
            'exito',            true,
            'mensaje',          CASE WHEN p_finalizar THEN 'Calificación finalizada y bloqueada.'
                                     ELSE 'Puntaje guardado correctamente.' END,
            'todosFinalizaron', false,
            'subtotal',         p_puntaje_material + p_puntaje_exposicion + p_puntaje_respuestas
           );

-- No EXCEPTION genérico aquí: queremos que el RAISE llegue intacto a Spring
END;
$$;