-- ============================================================
-- V24 — Corrección del trigger de notificaciones automáticas 
-- al cambiar el estado de una postulación (reemplazo de V13)
-- ============================================================

-- Función del trigger de notificación al cambiar estado postulación
CREATE OR REPLACE FUNCTION public.fn_notif_cambio_estado_postulacion()
RETURNS TRIGGER AS $$
BEGIN
    -- Disparar si cambió el estado o las observaciones
    -- Se corrige la referencia a la columna: estado_postulacion en vez de id_tipo_estado_postulacion
    IF (NEW.estado_postulacion IS DISTINCT FROM OLD.estado_postulacion)
       OR (NEW.observaciones IS DISTINCT FROM OLD.observaciones) THEN
        INSERT INTO notificacion.notificacion
            (id_usuario_destino, mensaje, fecha_envio, leido, tipo, tipo_notificacion)
        SELECT
            e.id_usuario,
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

-- Al reemplazar la función con el CREATE OR REPLACE FUNCTION, 
-- el trigger trg_notif_postulacion de la tabla postulacion.postulacion 
-- usará esta versión corregida automáticamente.
