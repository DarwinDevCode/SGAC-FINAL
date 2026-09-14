-- ============================================================
-- V13 — Notificaciones masivas: campos de clasificación
-- P10 (Ítems 11, 12, 13)
-- ============================================================

-- Agrega clasificación de notificación (INDIVIDUAL, MASIVA_ROL, MASIVA_TODOS)
ALTER TABLE notificacion.notificacion
    ADD COLUMN IF NOT EXISTS tipo_notificacion VARCHAR(30) DEFAULT 'INDIVIDUAL',
    ADD COLUMN IF NOT EXISTS id_convocatoria   INTEGER     REFERENCES convocatoria.convocatoria(id_convocatoria);

-- ============================================================
-- V13 también crea el trigger para notificaciones automáticas
-- al cambiar el estado de una postulación (P11 - Ítem 14)
-- ============================================================

-- Función del trigger de notificación al cambiar estado postulación
CREATE OR REPLACE FUNCTION public.fn_notif_cambio_estado_postulacion()
RETURNS TRIGGER AS $$
BEGIN
    -- Disparar si cambió el estado o las observaciones
    IF (NEW.id_tipo_estado_postulacion IS DISTINCT FROM OLD.id_tipo_estado_postulacion)
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

-- Eliminar el trigger si ya existe y recrearlo
DROP TRIGGER IF EXISTS trg_notif_postulacion ON postulacion.postulacion;

CREATE TRIGGER trg_notif_postulacion
AFTER UPDATE ON postulacion.postulacion
FOR EACH ROW EXECUTE FUNCTION public.fn_notif_cambio_estado_postulacion();

-- ============================================================
-- SP para KPIs del dashboard del coordinador (P12 - Ítem 3)
-- ============================================================

CREATE OR REPLACE FUNCTION public.sp_dashboard_coordinador(p_id_carrera INTEGER)
RETURNS TABLE(
    total_convocatorias        BIGINT,
    convocatorias_activas      BIGINT,
    total_postulaciones        BIGINT,
    postulaciones_pendientes   BIGINT,
    postulaciones_aprobadas    BIGINT
) AS $$
BEGIN
    RETURN QUERY
    SELECT
        (SELECT COUNT(*) FROM convocatoria.convocatoria cv
         JOIN academico.asignatura a ON cv.id_asignatura = a.id_asignatura
         WHERE a.id_carrera = p_id_carrera)::BIGINT,

        (SELECT COUNT(*) FROM convocatoria.convocatoria cv
         JOIN academico.asignatura a ON cv.id_asignatura = a.id_asignatura
         JOIN convocatoria.tipo_estado_convocatoria tec ON cv.id_tipo_estado_convocatoria = tec.id_tipo_estado_convocatoria
         WHERE a.id_carrera = p_id_carrera
           AND UPPER(tec.nombre_estado_convocatoria) IN ('ABIERTA','ACTIVA','PUBLICADA'))::BIGINT,

        (SELECT COUNT(*) FROM convocatoria.postulacion p
         JOIN convocatoria.convocatoria cv ON p.id_convocatoria = cv.id_convocatoria
         JOIN academico.asignatura a ON cv.id_asignatura = a.id_asignatura
         WHERE a.id_carrera = p_id_carrera)::BIGINT,

        (SELECT COUNT(*) FROM convocatoria.postulacion p
         JOIN convocatoria.convocatoria cv ON p.id_convocatoria = cv.id_convocatoria
         JOIN academico.asignatura a ON cv.id_asignatura = a.id_asignatura
         JOIN convocatoria.tipo_estado_postulacion tep ON p.id_tipo_estado_postulacion = tep.id_tipo_estado_postulacion
         WHERE a.id_carrera = p_id_carrera
           AND UPPER(tep.nombre_estado_postulacion) = 'PENDIENTE')::BIGINT,

        (SELECT COUNT(*) FROM convocatoria.postulacion p
         JOIN convocatoria.convocatoria cv ON p.id_convocatoria = cv.id_convocatoria
         JOIN academico.asignatura a ON cv.id_asignatura = a.id_asignatura
         JOIN convocatoria.tipo_estado_postulacion tep ON p.id_tipo_estado_postulacion = tep.id_tipo_estado_postulacion
         WHERE a.id_carrera = p_id_carrera
           AND UPPER(tep.nombre_estado_postulacion) = 'APROBADO')::BIGINT;
END;
$$ LANGUAGE plpgsql;

-- SP KPIs postulante
CREATE OR REPLACE FUNCTION public.sp_dashboard_postulante(p_id_usuario INTEGER)
RETURNS TABLE(
    total_postulaciones        BIGINT,
    postulaciones_pendientes   BIGINT,
    postulaciones_aprobadas    BIGINT,
    postulaciones_rechazadas   BIGINT
) AS $$
BEGIN
    RETURN QUERY
    SELECT
        (SELECT COUNT(*) FROM convocatoria.postulacion p
         JOIN academico.estudiante e ON p.id_estudiante = e.id_estudiante
         WHERE e.id_usuario = p_id_usuario)::BIGINT,

        (SELECT COUNT(*) FROM convocatoria.postulacion p
         JOIN academico.estudiante e ON p.id_estudiante = e.id_estudiante
         JOIN convocatoria.tipo_estado_postulacion tep ON p.id_tipo_estado_postulacion = tep.id_tipo_estado_postulacion
         WHERE e.id_usuario = p_id_usuario
           AND UPPER(tep.nombre_estado_postulacion) = 'PENDIENTE')::BIGINT,

        (SELECT COUNT(*) FROM convocatoria.postulacion p
         JOIN academico.estudiante e ON p.id_estudiante = e.id_estudiante
         JOIN convocatoria.tipo_estado_postulacion tep ON p.id_tipo_estado_postulacion = tep.id_tipo_estado_postulacion
         WHERE e.id_usuario = p_id_usuario
           AND UPPER(tep.nombre_estado_postulacion) = 'APROBADO')::BIGINT,

        (SELECT COUNT(*) FROM convocatoria.postulacion p
         JOIN academico.estudiante e ON p.id_estudiante = e.id_estudiante
         JOIN convocatoria.tipo_estado_postulacion tep ON p.id_tipo_estado_postulacion = tep.id_tipo_estado_postulacion
         WHERE e.id_usuario = p_id_usuario
           AND UPPER(tep.nombre_estado_postulacion) = 'RECHAZADO')::BIGINT;
END;
$$ LANGUAGE plpgsql;
