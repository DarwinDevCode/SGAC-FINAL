-- V29: Creación de tablas de lookup faltantes en el schema ayudantia
-- Requeridas por las stored procedures del módulo Docente (V30)

-- ─────────────────────────────────────────────
-- 1. tipo_estado_registro
--    Lookup de estados para registro_actividad
--    (PENDIENTE, ACEPTADO, RECHAZADO, OBSERVADO)
-- ─────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS ayudantia.tipo_estado_registro (
    id_tipo_estado_registro SERIAL PRIMARY KEY,
    nombre_estado           VARCHAR(50)  NOT NULL,
    descripcion             TEXT,
    activo                  BOOLEAN      DEFAULT TRUE
);

INSERT INTO ayudantia.tipo_estado_registro (nombre_estado, descripcion, activo) VALUES
    ('PENDIENTE',  'Actividad pendiente de revisión',   TRUE),
    ('ACEPTADO',   'Actividad aceptada por el docente', TRUE),
    ('RECHAZADO',  'Actividad rechazada por el docente',TRUE),
    ('OBSERVADO',  'Actividad con observaciones',        TRUE)
ON CONFLICT DO NOTHING;

-- ─────────────────────────────────────────────
-- 2. tipo_estado_evidencia
--    Lookup de estados para evidencia_registro_actividad
-- ─────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS ayudantia.tipo_estado_evidencia (
    id_tipo_estado_evidencia SERIAL PRIMARY KEY,
    nombre_estado            VARCHAR(50) NOT NULL,
    descripcion              TEXT,
    activo                   BOOLEAN     DEFAULT TRUE
);

INSERT INTO ayudantia.tipo_estado_evidencia (nombre_estado, descripcion, activo) VALUES
    ('PENDIENTE',  'Evidencia pendiente de revisión',    TRUE),
    ('ACEPTADO',   'Evidencia aceptada',                 TRUE),
    ('RECHAZADO',  'Evidencia rechazada',                TRUE),
    ('OBSERVADO',  'Evidencia con observaciones',        TRUE)
ON CONFLICT DO NOTHING;

-- ─────────────────────────────────────────────
-- 3. tipo_evidencia
--    Lookup de tipos de archivo de evidencia
-- ─────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS ayudantia.tipo_evidencia (
    id_tipo_evidencia   SERIAL PRIMARY KEY,
    nombre              VARCHAR(50) NOT NULL,
    extension_permitida VARCHAR(10),
    activo              BOOLEAN     DEFAULT TRUE
);

INSERT INTO ayudantia.tipo_evidencia (nombre, extension_permitida, activo) VALUES
    ('PDF',   'pdf',  TRUE),
    ('Imagen','png',  TRUE),
    ('Word',  'docx', TRUE),
    ('Excel', 'xlsx', TRUE)
ON CONFLICT DO NOTHING;

-- ─────────────────────────────────────────────
-- 4. Agregar FK a registro_actividad si no existe
-- ─────────────────────────────────────────────
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'ayudantia'
          AND table_name   = 'registro_actividad'
          AND column_name  = 'id_tipo_estado_registro'
    ) THEN
        ALTER TABLE ayudantia.registro_actividad
            ADD COLUMN id_tipo_estado_registro INTEGER NOT NULL DEFAULT 1
                REFERENCES ayudantia.tipo_estado_registro(id_tipo_estado_registro);
    END IF;
END $$;

-- ─────────────────────────────────────────────
-- 5. Agregar FKs a evidencia_registro_actividad si no existen
-- ─────────────────────────────────────────────
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'ayudantia'
          AND table_name   = 'evidencia_registro_actividad'
          AND column_name  = 'id_tipo_estado_evidencia'
    ) THEN
        ALTER TABLE ayudantia.evidencia_registro_actividad
            ADD COLUMN id_tipo_estado_evidencia INTEGER NOT NULL DEFAULT 1
                REFERENCES ayudantia.tipo_estado_evidencia(id_tipo_estado_evidencia);
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'ayudantia'
          AND table_name   = 'evidencia_registro_actividad'
          AND column_name  = 'id_tipo_evidencia'
    ) THEN
        ALTER TABLE ayudantia.evidencia_registro_actividad
            ADD COLUMN id_tipo_evidencia INTEGER NOT NULL DEFAULT 1
                REFERENCES ayudantia.tipo_evidencia(id_tipo_evidencia);
    END IF;
END $$;

-- ─────────────────────────────────────────────
-- 6. Permisos
-- ─────────────────────────────────────────────
GRANT SELECT ON ayudantia.tipo_estado_registro    TO app_user_default;
GRANT SELECT ON ayudantia.tipo_estado_evidencia   TO app_user_default;
GRANT SELECT ON ayudantia.tipo_evidencia           TO app_user_default;

GRANT SELECT ON ayudantia.tipo_estado_registro    TO administrador_consultas;
GRANT SELECT ON ayudantia.tipo_estado_evidencia   TO administrador_consultas;
GRANT SELECT ON ayudantia.tipo_evidencia           TO administrador_consultas;
