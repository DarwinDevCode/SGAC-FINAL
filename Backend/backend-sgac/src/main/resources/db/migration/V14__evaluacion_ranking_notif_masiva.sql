-- ============================================================
-- V14 — SP para evaluación final y ranking (P13 - Ítem 15)
-- Tablas reales: postulacion.postulacion, postulacion.evaluacion_meritos,
--                postulacion.evaluacion_oposicion, academico.estudiante,
--                seguridad.usuario
-- ============================================================

-- SP para calcular el resultado final por postulación
CREATE OR REPLACE FUNCTION public.sp_calcular_resultado_final(p_id_postulacion INTEGER)
RETURNS TABLE(
    id_postulacion      INTEGER,
    nombre_estudiante   TEXT,
    puntaje_meritos     NUMERIC,
    puntaje_oposicion   NUMERIC,
    puntaje_total       NUMERIC,
    posicion            BIGINT
) AS $$
BEGIN
    RETURN QUERY
    SELECT
        p.id_postulacion,
        (u.nombres || ' ' || u.apellidos)::TEXT AS nombre_estudiante,
        COALESCE(em.puntaje_total, 0)::NUMERIC  AS puntaje_meritos,
        COALESCE(eo.puntaje_total, 0)::NUMERIC  AS puntaje_oposicion,
        (COALESCE(em.puntaje_total, 0) + COALESCE(eo.puntaje_total, 0))::NUMERIC AS puntaje_total,
        RANK() OVER (
            PARTITION BY p.id_convocatoria
            ORDER BY (COALESCE(em.puntaje_total, 0) + COALESCE(eo.puntaje_total, 0)) DESC
        ) AS posicion
    FROM postulacion.postulacion p
    JOIN academico.estudiante e    ON e.id_estudiante = p.id_estudiante
    JOIN seguridad.usuario u       ON u.id_usuario    = e.id_usuario
    LEFT JOIN postulacion.evaluacion_meritos em   ON em.id_postulacion = p.id_postulacion
    LEFT JOIN postulacion.evaluacion_oposicion eo ON eo.id_postulacion = p.id_postulacion
    WHERE p.id_postulacion = p_id_postulacion;
END;
$$ LANGUAGE plpgsql;

-- SP ranking completo por convocatoria
CREATE OR REPLACE FUNCTION public.sp_ranking_convocatoria(p_id_convocatoria INTEGER)
RETURNS TABLE(
    id_postulacion      INTEGER,
    nombre_estudiante   TEXT,
    matricula           TEXT,
    puntaje_meritos     NUMERIC,
    puntaje_oposicion   NUMERIC,
    puntaje_total       NUMERIC,
    posicion            BIGINT
) AS $$
BEGIN
    RETURN QUERY
    SELECT
        p.id_postulacion,
        (u.nombres || ' ' || u.apellidos)::TEXT AS nombre_estudiante,
        est.matricula::TEXT,
        COALESCE(em.puntaje_total, 0)::NUMERIC  AS puntaje_meritos,
        COALESCE(eo.puntaje_total, 0)::NUMERIC  AS puntaje_oposicion,
        (COALESCE(em.puntaje_total, 0) + COALESCE(eo.puntaje_total, 0))::NUMERIC AS puntaje_total,
        RANK() OVER (
            ORDER BY (COALESCE(em.puntaje_total, 0) + COALESCE(eo.puntaje_total, 0)) DESC
        ) AS posicion
    FROM postulacion.postulacion p
    JOIN academico.estudiante est  ON est.id_estudiante = p.id_estudiante
    JOIN seguridad.usuario u       ON u.id_usuario      = est.id_usuario
    LEFT JOIN postulacion.evaluacion_meritos em   ON em.id_postulacion = p.id_postulacion
    LEFT JOIN postulacion.evaluacion_oposicion eo ON eo.id_postulacion = p.id_postulacion
    WHERE p.id_convocatoria = p_id_convocatoria
    ORDER BY puntaje_total DESC;
END;
$$ LANGUAGE plpgsql;

-- SP para notificaciones masivas por rol (P10 - Ítem 11/12/13)
CREATE OR REPLACE FUNCTION public.sp_enviar_notificacion_masiva(
    p_mensaje           TEXT,
    p_tipo              VARCHAR(50),
    p_tipo_notificacion VARCHAR(30),   -- MASIVA_ROL | MASIVA_TODOS
    p_id_rol            INTEGER DEFAULT NULL,
    p_id_convocatoria   INTEGER DEFAULT NULL
)
RETURNS INTEGER AS $$
DECLARE
    v_count INTEGER := 0;
    v_uid   INTEGER;
BEGIN
    FOR v_uid IN
        SELECT DISTINCT u.id_usuario
        FROM seguridad.usuario u
        JOIN seguridad.usuario_tipo_rol utr ON utr.id_usuario = u.id_usuario
        WHERE u.activo = TRUE
          AND (p_id_rol IS NULL OR utr.id_tipo_rol = p_id_rol)
    LOOP
        INSERT INTO notificacion.notificacion
            (id_usuario_destino, mensaje, fecha_envio, leido, tipo, tipo_notificacion, id_convocatoria)
        VALUES
            (v_uid, p_mensaje, NOW(), false, p_tipo, p_tipo_notificacion, p_id_convocatoria);
        v_count := v_count + 1;
    END LOOP;
    RETURN v_count;
END;
$$ LANGUAGE plpgsql;
