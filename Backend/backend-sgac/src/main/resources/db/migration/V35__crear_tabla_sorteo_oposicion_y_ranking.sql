-- V35__crear_tabla_sorteo_oposicion_y_ranking.sql
-- Creación de tablas faltantes para el módulo de Evaluación y Selección
-- e implementación de la función de ranking necesaria para el frontend.

-- ==========================================
-- 1. Tabla: postulacion.sorteo_oposicion
-- ==========================================
CREATE TABLE IF NOT EXISTS postulacion.sorteo_oposicion (
    id_sorteo SERIAL PRIMARY KEY,
    id_postulacion INTEGER NOT NULL,
    tema_sorteado VARCHAR(500) NOT NULL,
    semilla_sorteo BIGINT,
    fecha_sorteo TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    notificado BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT fk_sorteo_postulacion FOREIGN KEY (id_postulacion) REFERENCES postulacion.postulacion (id_postulacion)
);

-- ==========================================
-- 2. Tabla: postulacion.resumen_evaluacion
-- ==========================================
CREATE TABLE IF NOT EXISTS postulacion.resumen_evaluacion (
    id_resumen SERIAL PRIMARY KEY,
    id_postulacion INTEGER NOT NULL UNIQUE,
    total_meritos NUMERIC(5,2),
    promedio_oposicion NUMERIC(5,2),
    total_final NUMERIC(5,2),
    estado VARCHAR(20),
    posicion INTEGER,
    fecha_calculo TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    CONSTRAINT fk_resumen_postulacion FOREIGN KEY (id_postulacion) REFERENCES postulacion.postulacion (id_postulacion)
);

-- ==========================================
-- 3. Función: sp_calcular_ranking_evaluacion
-- ==========================================
-- Devuelve 10 columnas que espera el DTO del backend en ResumenEvaluacionRepository:
-- idPostulacion, nombreEstudiante, matricula, totalMeritos, promedioOposicion,
-- totalFinal, matSum, pertinenciaSum, estado, posicion
CREATE OR REPLACE FUNCTION public.sp_calcular_ranking_evaluacion(p_id_convocatoria INTEGER)
RETURNS TABLE (
    id_postulacion INTEGER,
    nombre_estudiante TEXT,
    matricula TEXT,
    total_meritos NUMERIC,
    promedio_oposicion NUMERIC,
    total_final NUMERIC,
    mat_sum NUMERIC,
    pertinencia_sum NUMERIC,
    estado TEXT,
    posicion BIGINT
) AS $$
BEGIN
    RETURN QUERY
    SELECT 
        r.id_postulacion,
        (u.nombres || ' ' || u.apellidos)::TEXT AS nombre_estudiante,
        est.matricula::TEXT,
        r.total_meritos::NUMERIC,
        r.promedio_oposicion::NUMERIC,
        r.total_final::NUMERIC,
        COALESCE(SUM(coi.criterio_material), 0)::NUMERIC AS mat_sum,
        COALESCE(SUM(coi.criterio_pertinencia), 0)::NUMERIC AS pertinencia_sum,
        COALESCE(r.estado, 'PENDIENTE')::TEXT AS estado,
        r.posicion::BIGINT
    FROM postulacion.resumen_evaluacion r
    JOIN postulacion.postulacion p ON p.id_postulacion = r.id_postulacion
    JOIN academico.estudiante est ON est.id_estudiante = p.id_estudiante
    JOIN seguridad.usuario u ON u.id_usuario = est.id_usuario
    LEFT JOIN postulacion.calificacion_oposicion_individual coi ON coi.id_postulacion = r.id_postulacion
    WHERE p.id_convocatoria = p_id_convocatoria
    GROUP BY r.id_postulacion, u.nombres, u.apellidos, est.matricula, 
             r.total_meritos, r.promedio_oposicion, r.total_final, r.estado, r.posicion
    ORDER BY r.posicion ASC;
END;
$$ LANGUAGE plpgsql;
