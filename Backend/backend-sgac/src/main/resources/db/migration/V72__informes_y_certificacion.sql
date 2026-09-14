-- =================================================================================
-- V72: Informes, Cadena de Aprobacion, Plantillas y Certificados (Bloque 1C)
-- =================================================================================

-- 1. Tabla de Informes de Ayudantia
CREATE TABLE IF NOT EXISTS ayudantia.informe (
    id_informe           SERIAL PRIMARY KEY,
    id_ayudantia         INTEGER NOT NULL REFERENCES ayudantia.ayudantia(id_ayudantia),
    tipo                 VARCHAR(50) NOT NULL CHECK (tipo IN ('MENSUAL', 'FINAL')),
    mes                  INTEGER, -- Null para FINAL
    anio                 INTEGER,
    horas_reportadas     NUMERIC(5,2) NOT NULL DEFAULT 0.00,
    estado               VARCHAR(50) NOT NULL DEFAULT 'BORRADOR' 
                         CHECK (estado IN ('BORRADOR', 'ENVIADO', 'APROBADO_DOCENTE', 'APROBADO_COORDINADOR', 'RECHAZADO')),
    observaciones        TEXT,
    archivo_adjunto_url  VARCHAR(500),
    fecha_creacion       TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    activo               BOOLEAN DEFAULT TRUE
);

-- 2. Tabla de Historial de Aprobacion (Cadena Secuencial)
CREATE TABLE IF NOT EXISTS ayudantia.historial_aprobacion_informe (
    id_historial         SERIAL PRIMARY KEY,
    id_informe           INTEGER NOT NULL REFERENCES ayudantia.informe(id_informe),
    id_usuario_revisor   INTEGER NOT NULL REFERENCES seguridad.usuario(id_usuario),
    rol_revisor          VARCHAR(50) NOT NULL,
    estado_anterior      VARCHAR(50),
    estado_nuevo         VARCHAR(50) NOT NULL,
    comentario           TEXT,
    fecha_revision       TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 3. Tabla de Plantillas de Documentos (HTML/Text)
CREATE TABLE IF NOT EXISTS ayudantia.plantilla_documento (
    id_plantilla         SERIAL PRIMARY KEY,
    codigo               VARCHAR(50) NOT NULL UNIQUE,
    nombre               VARCHAR(150) NOT NULL,
    contenido_html       TEXT NOT NULL,
    variables_requeridas JSONB,
    activo               BOOLEAN DEFAULT TRUE
);

-- 4. Tabla de Certificados Generados
CREATE TABLE IF NOT EXISTS ayudantia.certificado (
    id_certificado       SERIAL PRIMARY KEY,
    id_ayudantia         INTEGER NOT NULL REFERENCES ayudantia.ayudantia(id_ayudantia),
    codigo_verificacion  VARCHAR(100) NOT NULL UNIQUE,
    url_pdf              VARCHAR(500),
    fecha_emision        TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    activo               BOOLEAN DEFAULT TRUE
);

-- 5. Insertar plantillas por defecto
INSERT INTO ayudantia.plantilla_documento (codigo, nombre, contenido_html, variables_requeridas)
VALUES 
('CERTIFICADO_FINAL', 'Certificado de Ayudantía', '<h1>Certificado</h1><p>El estudiante {{ESTUDIANTE}} ha completado {{HORAS}} horas...</p>', '["ESTUDIANTE", "HORAS", "PERIODO", "ASIGNATURA"]'::jsonb),
('INFORME_MENSUAL', 'Plantilla de Informe Mensual', '<h1>Informe Mensual</h1>...', '["ESTUDIANTE", "MES", "HORAS", "ACTIVIDADES"]'::jsonb)
ON CONFLICT (codigo) DO NOTHING;
