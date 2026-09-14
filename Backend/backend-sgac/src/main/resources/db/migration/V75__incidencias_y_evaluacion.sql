-- =================================================================================
-- V75: Incidencias y Evaluación de Desempeño (Bloque 1D - F1.18 y F1.20)
-- =================================================================================

-- 1. Tabla de Incidencias (F1.18)
CREATE TABLE IF NOT EXISTS ayudantia.incidencia (
    id_incidencia        SERIAL PRIMARY KEY,
    id_ayudantia         INTEGER NOT NULL REFERENCES ayudantia.ayudantia(id_ayudantia),
    id_usuario_reporta   INTEGER NOT NULL REFERENCES seguridad.usuario(id_usuario),
    tipo_incidencia      VARCHAR(50) NOT NULL CHECK (tipo_incidencia IN ('INASISTENCIA', 'BAJO_RENDIMIENTO', 'FALTA_DISCIPLINARIA', 'OTRO')),
    descripcion          TEXT NOT NULL,
    estado               VARCHAR(50) NOT NULL DEFAULT 'REPORTADA' CHECK (estado IN ('REPORTADA', 'EN_REVISION', 'RESUELTA', 'APLICA_REEMPLAZO')),
    resolucion           TEXT,
    fecha_reporte        TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_resolucion     TIMESTAMP
);

-- 2. Tabla de Evaluación de Desempeño (F1.20)
CREATE TABLE IF NOT EXISTS ayudantia.evaluacion_desempeno (
    id_evaluacion        SERIAL PRIMARY KEY,
    id_ayudantia         INTEGER NOT NULL REFERENCES ayudantia.ayudantia(id_ayudantia),
    id_docente           INTEGER NOT NULL REFERENCES academico.docente(id_docente),
    calificacion_general NUMERIC(5,2) NOT NULL CHECK (calificacion_general >= 0 AND calificacion_general <= 100),
    criterios_evaluados  JSONB NOT NULL, -- Ej: {"puntualidad": 10, "conocimiento": 20, "dominio_grupo": 15}
    comentarios          TEXT,
    fecha_evaluacion     TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
