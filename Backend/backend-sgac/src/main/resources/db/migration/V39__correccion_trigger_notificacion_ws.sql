-- ============================================================
-- V39 — Corrección del trigger de notificaciones
-- Usa la tabla notificacion_ws en lugar de notificacion
-- ============================================================

-- ============================================================
-- Corregir el trigger fn_notif_cambio_estado_postulacion
-- para usar notificacion_ws con las columnas correctas
-- ============================================================
CREATE OR REPLACE FUNCTION public.fn_notif_cambio_estado_postulacion()
RETURNS TRIGGER AS $$
DECLARE
    v_id_usuario_destino INTEGER;
    v_titulo VARCHAR(150);
    v_mensaje TEXT;
BEGIN
    -- Disparar si cambió el estado o las observaciones
    IF (NEW.estado_postulacion IS DISTINCT FROM OLD.estado_postulacion)
       OR (NEW.observaciones IS DISTINCT FROM OLD.observaciones) THEN

        -- Obtener el id_usuario del estudiante
        SELECT e.id_usuario INTO v_id_usuario_destino
        FROM academico.estudiante e
        WHERE e.id_estudiante = NEW.id_estudiante;

        IF v_id_usuario_destino IS NOT NULL THEN
            -- Determinar título y mensaje según el estado
            v_titulo := 'Actualización de Postulación';
            v_mensaje := 'Tu postulación fue actualizada a estado: ' || COALESCE(NEW.estado_postulacion, 'N/A') || '. Revisa el estado y observaciones en la plataforma.';

            -- Usar notificacion_ws con columnas correctas
            INSERT INTO notificacion.notificacion_ws (
                id_usuario,
                titulo,
                mensaje,
                tipo,
                leido,
                fecha_creacion,
                id_referencia
            )
            VALUES (
                v_id_usuario_destino,
                v_titulo,
                v_mensaje,
                'ACTUALIZACION',
                FALSE,
                NOW(),
                NEW.id_postulacion
            );
        END IF;
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- ============================================================
-- Recrear las funciones de evaluación con notificacion_ws
-- ============================================================

-- C. FUNCIÓN DE ACCIÓN: fn_evaluar_documento_individual
CREATE OR REPLACE FUNCTION postulacion.fn_evaluar_documento_individual(
    p_id_usuario INTEGER,
    p_id_requisito_adjunto INTEGER,
    p_accion VARCHAR(20),
    p_observacion TEXT DEFAULT NULL
)
RETURNS JSONB AS $$
DECLARE
    v_id_coordinador INTEGER;
    v_id_carrera INTEGER;
    v_id_postulacion INTEGER;
    v_postulacion_carrera INTEGER;
    v_id_nuevo_estado INTEGER;
    v_nombre_estado VARCHAR;
    v_tiene_observados BOOLEAN;
    v_todos_validados BOOLEAN;
    v_id_estudiante INTEGER;
    v_id_usuario_estudiante INTEGER;
    v_id_estado_postulacion_observada INTEGER;
BEGIN
    -- Validar coordinador
    SELECT c.id_coordinador, c.id_carrera
    INTO v_id_coordinador, v_id_carrera
    FROM academico.coordinador c
    WHERE c.id_usuario = p_id_usuario
      AND c.activo = TRUE;

    IF v_id_coordinador IS NULL THEN
        RETURN jsonb_build_object(
            'exito', FALSE,
            'codigo', 'ERROR_AUTORIZACION',
            'mensaje', 'AVISO: El usuario no tiene rol de coordinador activo asignado'
        );
    END IF;

    -- Obtener información del documento y validar permisos
    SELECT ra.id_postulacion, car.id_carrera, p.id_estudiante
    INTO v_id_postulacion, v_postulacion_carrera, v_id_estudiante
    FROM postulacion.requisito_adjunto ra
    JOIN postulacion.postulacion p ON ra.id_postulacion = p.id_postulacion
    JOIN convocatoria.convocatoria cv ON p.id_convocatoria = cv.id_convocatoria
    JOIN academico.asignatura a ON cv.id_asignatura = a.id_asignatura
    JOIN academico.carrera car ON a.id_carrera = car.id_carrera
    WHERE ra.id_requisito_adjunto = p_id_requisito_adjunto;

    IF v_id_postulacion IS NULL THEN
        RETURN jsonb_build_object(
            'exito', FALSE,
            'codigo', 'ERROR_NO_ENCONTRADO',
            'mensaje', 'AVISO: El documento no existe'
        );
    END IF;

    IF v_postulacion_carrera != v_id_carrera THEN
        RETURN jsonb_build_object(
            'exito', FALSE,
            'codigo', 'ERROR_PERMISOS',
            'mensaje', 'AVISO: No tiene permisos para evaluar este documento'
        );
    END IF;

    -- Obtener el id_usuario del estudiante
    SELECT e.id_usuario INTO v_id_usuario_estudiante
    FROM academico.estudiante e
    WHERE e.id_estudiante = v_id_estudiante;

    -- Determinar el nuevo estado según la acción
    CASE UPPER(p_accion)
        WHEN 'VALIDAR' THEN
            SELECT id_tipo_estado_requisito, nombre_estado
            INTO v_id_nuevo_estado, v_nombre_estado
            FROM convocatoria.tipo_estado_requisito
            WHERE UPPER(nombre_estado) IN ('APROBADO', 'VALIDADO') AND activo = TRUE
            LIMIT 1;

            IF v_id_nuevo_estado IS NULL THEN
                INSERT INTO convocatoria.tipo_estado_requisito (nombre_estado, descripcion, activo)
                VALUES ('APROBADO', 'Documento validado por el coordinador', TRUE)
                RETURNING id_tipo_estado_requisito, nombre_estado INTO v_id_nuevo_estado, v_nombre_estado;
            END IF;

        WHEN 'OBSERVAR' THEN
            IF p_observacion IS NULL OR TRIM(p_observacion) = '' THEN
                RETURN jsonb_build_object(
                    'exito', FALSE,
                    'codigo', 'ERROR_VALIDACION',
                    'mensaje', 'AVISO: Debe proporcionar una observación para el documento'
                );
            END IF;

            SELECT id_tipo_estado_requisito, nombre_estado
            INTO v_id_nuevo_estado, v_nombre_estado
            FROM convocatoria.tipo_estado_requisito
            WHERE UPPER(nombre_estado) = 'OBSERVADO' AND activo = TRUE
            LIMIT 1;

        WHEN 'RECHAZAR' THEN
            SELECT id_tipo_estado_requisito, nombre_estado
            INTO v_id_nuevo_estado, v_nombre_estado
            FROM convocatoria.tipo_estado_requisito
            WHERE UPPER(nombre_estado) = 'RECHAZADO' AND activo = TRUE
            LIMIT 1;

        ELSE
            RETURN jsonb_build_object(
                'exito', FALSE,
                'codigo', 'ERROR_ACCION',
                'mensaje', 'AVISO: Acción no válida. Use VALIDAR, OBSERVAR o RECHAZAR'
            );
    END CASE;

    IF v_id_nuevo_estado IS NULL THEN
        RETURN jsonb_build_object(
            'exito', FALSE,
            'codigo', 'ERROR_CONFIGURACION',
            'mensaje', 'AVISO: No se encontró el estado de requisito configurado en el sistema'
        );
    END IF;

    -- Actualizar el documento
    UPDATE postulacion.requisito_adjunto
    SET id_tipo_estado_requisito = v_id_nuevo_estado,
        observacion = CASE WHEN UPPER(p_accion) = 'OBSERVAR' THEN p_observacion ELSE observacion END
    WHERE id_requisito_adjunto = p_id_requisito_adjunto;

    -- Si se observó un documento, actualizar estado de la postulación a OBSERVADA
    IF UPPER(p_accion) = 'OBSERVAR' THEN
        SELECT id_tipo_estado_postulacion INTO v_id_estado_postulacion_observada
        FROM postulacion.tipo_estado_postulacion
        WHERE codigo = 'OBSERVADA';

        IF v_id_estado_postulacion_observada IS NOT NULL THEN
            UPDATE postulacion.postulacion
            SET id_tipo_estado_postulacion = v_id_estado_postulacion_observada,
                estado_postulacion = 'OBSERVADA'
            WHERE id_postulacion = v_id_postulacion;
        END IF;

        -- Notificar al estudiante usando notificacion_ws
        IF v_id_usuario_estudiante IS NOT NULL THEN
            INSERT INTO notificacion.notificacion_ws (
                id_usuario,
                titulo,
                mensaje,
                tipo,
                leido,
                fecha_creacion,
                id_referencia
            )
            VALUES (
                v_id_usuario_estudiante,
                'Documento Observado',
                'Tu documento ha sido observado. Por favor, revisa las observaciones y realiza las correcciones necesarias.',
                'OBSERVACION',
                FALSE,
                NOW(),
                v_id_postulacion
            );
        END IF;
    END IF;

    -- Verificar estados de documentos
    SELECT EXISTS (
        SELECT 1 FROM postulacion.requisito_adjunto ra
        JOIN convocatoria.tipo_estado_requisito ter ON ra.id_tipo_estado_requisito = ter.id_tipo_estado_requisito
        WHERE ra.id_postulacion = v_id_postulacion
          AND UPPER(ter.nombre_estado) = 'OBSERVADO'
    ) INTO v_tiene_observados;

    SELECT NOT EXISTS (
        SELECT 1 FROM postulacion.requisito_adjunto ra
        JOIN convocatoria.tipo_estado_requisito ter ON ra.id_tipo_estado_requisito = ter.id_tipo_estado_requisito
        WHERE ra.id_postulacion = v_id_postulacion
          AND UPPER(ter.nombre_estado) NOT IN ('APROBADO', 'VALIDADO')
    ) INTO v_todos_validados;

    RETURN jsonb_build_object(
        'exito', TRUE,
        'codigo', 'OK',
        'mensaje', 'Documento evaluado correctamente',
        'id_requisito_adjunto', p_id_requisito_adjunto,
        'nuevo_estado', v_nombre_estado,
        'tiene_observados', v_tiene_observados,
        'todos_validados', v_todos_validados,
        'puede_aprobar_postulacion', v_todos_validados
    );

EXCEPTION WHEN OTHERS THEN
    RETURN jsonb_build_object(
        'exito', FALSE,
        'codigo', 'ERROR_SISTEMA',
        'mensaje', 'ERROR SISTEMA [' || SQLSTATE || ']: ' || SQLERRM
    );
END;
$$ LANGUAGE plpgsql;

-- D. FUNCIÓN DE ACCIÓN FINAL: fn_dictaminar_postulacion
CREATE OR REPLACE FUNCTION postulacion.fn_dictaminar_postulacion(
    p_id_usuario INTEGER,
    p_id_postulacion INTEGER,
    p_accion VARCHAR(20),
    p_observacion TEXT DEFAULT NULL
)
RETURNS JSONB AS $$
DECLARE
    v_id_coordinador INTEGER;
    v_id_carrera INTEGER;
    v_postulacion_carrera INTEGER;
    v_id_estudiante INTEGER;
    v_id_usuario_estudiante INTEGER;
    v_todos_validados BOOLEAN;
    v_id_nuevo_estado INTEGER;
    v_estado_codigo VARCHAR;
BEGIN
    -- Validar coordinador
    SELECT c.id_coordinador, c.id_carrera
    INTO v_id_coordinador, v_id_carrera
    FROM academico.coordinador c
    WHERE c.id_usuario = p_id_usuario
      AND c.activo = TRUE;

    IF v_id_coordinador IS NULL THEN
        RETURN jsonb_build_object(
            'exito', FALSE,
            'codigo', 'ERROR_AUTORIZACION',
            'mensaje', 'AVISO: El usuario no tiene rol de coordinador activo asignado'
        );
    END IF;

    -- Obtener información de la postulación y validar permisos
    SELECT car.id_carrera, p.id_estudiante
    INTO v_postulacion_carrera, v_id_estudiante
    FROM postulacion.postulacion p
    JOIN convocatoria.convocatoria cv ON p.id_convocatoria = cv.id_convocatoria
    JOIN academico.asignatura a ON cv.id_asignatura = a.id_asignatura
    JOIN academico.carrera car ON a.id_carrera = car.id_carrera
    WHERE p.id_postulacion = p_id_postulacion;

    IF v_postulacion_carrera IS NULL THEN
        RETURN jsonb_build_object(
            'exito', FALSE,
            'codigo', 'ERROR_NO_ENCONTRADO',
            'mensaje', 'AVISO: La postulación no existe'
        );
    END IF;

    IF v_postulacion_carrera != v_id_carrera THEN
        RETURN jsonb_build_object(
            'exito', FALSE,
            'codigo', 'ERROR_PERMISOS',
            'mensaje', 'AVISO: No tiene permisos para dictaminar esta postulación'
        );
    END IF;

    -- Obtener el id_usuario del estudiante
    SELECT e.id_usuario INTO v_id_usuario_estudiante
    FROM academico.estudiante e
    WHERE e.id_estudiante = v_id_estudiante;

    -- Validar acción
    CASE UPPER(p_accion)
        WHEN 'APROBAR' THEN
            SELECT NOT EXISTS (
                SELECT 1 FROM postulacion.requisito_adjunto ra
                JOIN convocatoria.tipo_estado_requisito ter ON ra.id_tipo_estado_requisito = ter.id_tipo_estado_requisito
                WHERE ra.id_postulacion = p_id_postulacion
                  AND UPPER(ter.nombre_estado) NOT IN ('APROBADO', 'VALIDADO')
            ) INTO v_todos_validados;

            IF NOT v_todos_validados THEN
                RETURN jsonb_build_object(
                    'exito', FALSE,
                    'codigo', 'ERROR_DOCUMENTOS_PENDIENTES',
                    'mensaje', 'AVISO: No se puede aprobar la postulación. Todos los documentos deben estar validados.'
                );
            END IF;

            v_estado_codigo := 'APROBADA';

        WHEN 'RECHAZAR' THEN
            IF p_observacion IS NULL OR TRIM(p_observacion) = '' THEN
                RETURN jsonb_build_object(
                    'exito', FALSE,
                    'codigo', 'ERROR_VALIDACION',
                    'mensaje', 'AVISO: Debe proporcionar un motivo para rechazar la postulación'
                );
            END IF;

            v_estado_codigo := 'RECHAZADA';

        ELSE
            RETURN jsonb_build_object(
                'exito', FALSE,
                'codigo', 'ERROR_ACCION',
                'mensaje', 'AVISO: Acción no válida. Use APROBAR o RECHAZAR'
            );
    END CASE;

    -- Obtener ID del estado
    SELECT id_tipo_estado_postulacion INTO v_id_nuevo_estado
    FROM postulacion.tipo_estado_postulacion
    WHERE codigo = v_estado_codigo;

    IF v_id_nuevo_estado IS NULL THEN
        RETURN jsonb_build_object(
            'exito', FALSE,
            'codigo', 'ERROR_CONFIGURACION',
            'mensaje', 'AVISO: No se encontró el estado de postulación configurado en el sistema'
        );
    END IF;

    -- Actualizar la postulación
    UPDATE postulacion.postulacion
    SET id_tipo_estado_postulacion = v_id_nuevo_estado,
        estado_postulacion = v_estado_codigo,
        observaciones = CASE WHEN p_observacion IS NOT NULL THEN p_observacion ELSE observaciones END
    WHERE id_postulacion = p_id_postulacion;

    -- Notificar al estudiante usando notificacion_ws
    IF v_id_usuario_estudiante IS NOT NULL THEN
        INSERT INTO notificacion.notificacion_ws (
            id_usuario,
            titulo,
            mensaje,
            tipo,
            leido,
            fecha_creacion,
            id_referencia
        )
        VALUES (
            v_id_usuario_estudiante,
            CASE
                WHEN v_estado_codigo = 'APROBADA' THEN 'Postulación Aprobada'
                ELSE 'Postulación Rechazada'
            END,
            CASE
                WHEN v_estado_codigo = 'APROBADA' THEN
                    '¡Felicitaciones! Tu postulación ha sido APROBADA. Pronto recibirás más información sobre los siguientes pasos.'
                ELSE
                    'Tu postulación ha sido RECHAZADA. Motivo: ' || COALESCE(p_observacion, 'No especificado')
            END,
            CASE WHEN v_estado_codigo = 'APROBADA' THEN 'APROBACION' ELSE 'RECHAZO' END,
            FALSE,
            NOW(),
            p_id_postulacion
        );
    END IF;

    RETURN jsonb_build_object(
        'exito', TRUE,
        'codigo', 'OK',
        'mensaje', CASE
            WHEN v_estado_codigo = 'APROBADA' THEN 'Postulación aprobada correctamente'
            ELSE 'Postulación rechazada correctamente'
        END,
        'id_postulacion', p_id_postulacion,
        'nuevo_estado', v_estado_codigo
    );

EXCEPTION WHEN OTHERS THEN
    RETURN jsonb_build_object(
        'exito', FALSE,
        'codigo', 'ERROR_SISTEMA',
        'mensaje', 'ERROR SISTEMA [' || SQLSTATE || ']: ' || SQLERRM
    );
END;
$$ LANGUAGE plpgsql;

-- ============================================================
-- Permisos actualizados para notificacion_ws
-- ============================================================
GRANT INSERT ON notificacion.notificacion_ws TO role_coordinador;
GRANT USAGE ON SEQUENCE notificacion.notificacion_ws_id_notificacion_seq TO role_coordinador;

