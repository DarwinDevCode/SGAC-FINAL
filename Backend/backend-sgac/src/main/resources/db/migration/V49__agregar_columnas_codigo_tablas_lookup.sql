-- ═══════════════════════════════════════════════════════════════════════════════════════════════════
-- V49_1__agregar_columnas_codigo_tablas_lookup.sql
-- Descripción: Agrega las columnas 'codigo' faltantes en las tablas de catálogos maestros
--              para mantener consistencia con el esquema definido.
-- Fecha: 2026-03-10
-- ═══════════════════════════════════════════════════════════════════════════════════════════════════

-- ─────────────────────────────────────────────
-- 1. Agregar columna 'codigo' a ayudantia.tipo_estado_registro
-- ─────────────────────────────────────────────
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'ayudantia'
          AND table_name = 'tipo_estado_registro'
          AND column_name = 'codigo'
    ) THEN
        ALTER TABLE ayudantia.tipo_estado_registro
            ADD COLUMN codigo VARCHAR(25) UNIQUE;

        -- Actualizar registros existentes con códigos basados en nombre_estado
        UPDATE ayudantia.tipo_estado_registro
        SET codigo = UPPER(REPLACE(nombre_estado, ' ', '_'))
        WHERE codigo IS NULL;

        -- Hacer la columna NOT NULL después de asignar valores
        ALTER TABLE ayudantia.tipo_estado_registro
            ALTER COLUMN codigo SET NOT NULL;
    END IF;
END $$;

-- ─────────────────────────────────────────────
-- 2. Agregar columna 'codigo' a ayudantia.tipo_evidencia
-- ─────────────────────────────────────────────
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'ayudantia'
          AND table_name = 'tipo_evidencia'
          AND column_name = 'codigo'
    ) THEN
        ALTER TABLE ayudantia.tipo_evidencia
            ADD COLUMN codigo VARCHAR(25) UNIQUE;

        -- Actualizar registros existentes con códigos basados en nombre
        UPDATE ayudantia.tipo_evidencia
        SET codigo = UPPER(REPLACE(nombre, ' ', '_'))
        WHERE codigo IS NULL;

        -- Hacer la columna NOT NULL después de asignar valores
        ALTER TABLE ayudantia.tipo_evidencia
            ALTER COLUMN codigo SET NOT NULL;
    END IF;
END $$;

-- ─────────────────────────────────────────────
-- 3. Verificar/Agregar restricción UNIQUE a nombre_estado en tipo_estado_registro
-- ─────────────────────────────────────────────
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint c
        JOIN pg_namespace n ON n.oid = c.connamespace
        WHERE n.nspname = 'ayudantia'
          AND c.conname = 'tipo_estado_registro_nombre_estado_key'
    ) THEN
        -- Solo agregar si no existe
        BEGIN
            ALTER TABLE ayudantia.tipo_estado_registro
                ADD CONSTRAINT tipo_estado_registro_nombre_estado_key UNIQUE (nombre_estado);
        EXCEPTION
            WHEN duplicate_object THEN NULL;
        END;
    END IF;
END $$;

-- ─────────────────────────────────────────────
-- 4. Verificar/Agregar restricción UNIQUE a nombre en tipo_evidencia
-- ─────────────────────────────────────────────
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint c
        JOIN pg_namespace n ON n.oid = c.connamespace
        WHERE n.nspname = 'ayudantia'
          AND c.conname = 'tipo_evidencia_nombre_key'
    ) THEN
        -- Solo agregar si no existe
        BEGIN
            ALTER TABLE ayudantia.tipo_evidencia
                ADD CONSTRAINT tipo_evidencia_nombre_key UNIQUE (nombre);
        EXCEPTION
            WHEN duplicate_object THEN NULL;
        END;
    END IF;
END $$;

-- ─────────────────────────────────────────────
-- Comentarios de documentación
-- ─────────────────────────────────────────────
COMMENT ON COLUMN ayudantia.tipo_estado_registro.codigo IS 'Código único identificador del estado de registro';
COMMENT ON COLUMN ayudantia.tipo_evidencia.codigo IS 'Código único identificador del tipo de evidencia';

