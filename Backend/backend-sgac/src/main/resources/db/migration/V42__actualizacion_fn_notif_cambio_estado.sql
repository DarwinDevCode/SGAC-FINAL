create or replace function seguridad.fn_notif_cambio_estado_postulacion() returns trigger
    language plpgsql
as
$$
DECLARE
    v_id_usuario_destino INTEGER;
    v_titulo VARCHAR(150);
    v_mensaje TEXT;
BEGIN
    IF (NEW.id_tipo_estado_postulacion IS DISTINCT FROM OLD.id_tipo_estado_postulacion)
        OR (NEW.observaciones IS DISTINCT FROM OLD.observaciones) THEN

        -- Obtener el id_usuario del estudiante
        SELECT e.id_usuario INTO v_id_usuario_destino
        FROM academico.estudiante e
        WHERE e.id_estudiante = NEW.id_estudiante;

        IF v_id_usuario_destino IS NOT NULL THEN
            -- Determinar título y mensaje según el estado
            v_titulo := 'Actualización de Postulación';
            v_mensaje := 'Tu postulación fue actualizada a estado: ' || COALESCE(NEW.id_tipo_estado_postulacion, 'N/A') || '. Revisa el estado y observaciones en la plataforma.';

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
$$;
