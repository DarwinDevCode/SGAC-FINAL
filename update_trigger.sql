CREATE OR REPLACE FUNCTION public.fn_notif_cambio_estado_postulacion()
RETURNS TRIGGER AS $$
BEGIN
    IF (NEW.estado_postulacion IS DISTINCT FROM OLD.estado_postulacion)
       OR (NEW.observaciones IS DISTINCT FROM OLD.observaciones) THEN
        INSERT INTO notificacion.notificacion
            (id_usuario, id_usuario_destino, titulo, mensaje, fecha_envio, leido, tipo, tipo_notificacion)
        SELECT
            e.id_usuario,
            e.id_usuario,
            'Actualización de tu postulación',
            'Tu postulación fue actualizada. Revisa el estado y observaciones en la plataforma.',
            NOW(),
            false,
            'OBSERVACION',
            'INDIVIDUAL'
        FROM academico.estudiante e
        WHERE e.id_estudiante = NEW.id_estudiante;
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;
