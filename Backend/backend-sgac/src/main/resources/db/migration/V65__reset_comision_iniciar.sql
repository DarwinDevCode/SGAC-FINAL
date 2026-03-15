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
    v_ts_inicio       TEXT;
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


    IF p_accion = 'INICIAR' THEN
        IF v_codigo_actual != 'PROGRAMADA' THEN
            RETURN jsonb_build_object(
                    'exito',   false,
                    'mensaje', 'Solo se puede iniciar una evaluación PROGRAMADA. Estado actual: ' || v_codigo_actual
                   );
        END IF;

        -- Resolver la comisión vinculada a esta convocatoria
        SELECT cs.id_comision_seleccion INTO v_id_comision
        FROM   postulacion.evaluacion_oposicion eo
                   JOIN   postulacion.postulacion          p  ON p.id_postulacion   = eo.id_postulacion
                   JOIN   postulacion.comision_seleccion   cs ON cs.id_convocatoria = p.id_convocatoria
        WHERE  eo.id_evaluacion_oposicion = p_id_evaluacion_oposicion
          AND  cs.activo = TRUE
        LIMIT 1;

        IF v_id_comision IS NOT NULL THEN
            UPDATE seguridad.usuario_comision
            SET    id_evaluacion_oposicion = p_id_evaluacion_oposicion,
                   finalizo_calificacion   = FALSE,
                   puntaje_material        = NULL,
                   puntaje_exposicion      = NULL,
                   puntaje_respuestas      = NULL,
                   fecha_evaluacion        = NULL
            WHERE  id_comision_seleccion = v_id_comision
              AND  activo               = TRUE;
        END IF;

        -- Cambiar estado y registrar hora real
        SELECT id_tipo_estado_evaluacion INTO v_id_estado_nuevo
        FROM   postulacion.tipo_estado_evaluacion WHERE codigo = 'EN_CURSO';

        v_hora_inicio_now := LOCALTIME;
        v_ts_inicio := to_char(NOW() AT TIME ZONE 'UTC', 'YYYY-MM-DD"T"HH24:MI:SS"Z"');

        UPDATE postulacion.evaluacion_oposicion
        SET    id_tipo_estado_evaluacion = v_id_estado_nuevo,
               hora_inicio_real          = v_hora_inicio_now
        WHERE  id_evaluacion_oposicion   = p_id_evaluacion_oposicion;

        RETURN jsonb_build_object(
                'exito',           true,
                'mensaje',         'Evaluación iniciada. El tribunal puede comenzar a calificar.',
                'horaReal',        to_char(v_hora_inicio_now, 'HH24:MI:SS'),
                'serverTimestamp', v_ts_inicio
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

GRANT EXECUTE ON FUNCTION postulacion.fn_cambiar_estado_evaluacion(INTEGER, TEXT)
    TO app_user_default;