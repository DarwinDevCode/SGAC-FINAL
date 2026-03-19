-- V71: Sincronización de Esquema con Entidades Java
-- Crea tablas y columnas faltantes detectadas en el reporte de incidencias.

DO $$
BEGIN
    ---------------------------------------------------------------------------
    -- 1. Esquema: ayudantia
    ---------------------------------------------------------------------------
    
    -- Tabla: ayudantia.tipo_documento
    IF NOT EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = 'ayudantia' AND table_name = 'tipo_documento') THEN
        CREATE TABLE ayudantia.tipo_documento (
            id_tipo_documento SERIAL PRIMARY KEY,
            nombre VARCHAR(50) NOT NULL,
            codigo VARCHAR(25) NOT NULL UNIQUE,
            activo BOOLEAN DEFAULT TRUE NOT NULL
        );
        ALTER TABLE ayudantia.tipo_documento OWNER TO admin1;
    END IF;

    -- Tabla: ayudantia.documento_academico
    IF NOT EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = 'ayudantia' AND table_name = 'documento_academico') THEN
        CREATE TABLE ayudantia.documento_academico (
            id_documento SERIAL PRIMARY KEY,
            nombre_mostrar VARCHAR(150) NOT NULL,
            ruta_archivo VARCHAR(500) NOT NULL,
            extension VARCHAR(10),
            peso_bytes INTEGER,
            fecha_subida TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
            id_tipo_documento INTEGER NOT NULL REFERENCES ayudantia.tipo_documento(id_tipo_documento),
            id_periodo INTEGER NOT NULL REFERENCES academico.periodo_academico(id_periodo_academico),
            id_convocatoria INTEGER REFERENCES convocatoria.convocatoria(id_convocatoria),
            id_usuario_sube INTEGER NOT NULL REFERENCES seguridad.usuario(id_usuario),
            activo BOOLEAN DEFAULT TRUE NOT NULL
        );
        ALTER TABLE ayudantia.documento_academico OWNER TO admin1;
    END IF;

    ---------------------------------------------------------------------------
    -- 2. Esquema: postulacion
    ---------------------------------------------------------------------------

    -- Tabla: postulacion.tipo_estado_evaluacion
    IF NOT EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = 'postulacion' AND table_name = 'tipo_estado_evaluacion') THEN
        CREATE TABLE postulacion.tipo_estado_evaluacion (
            id_tipo_estado_evaluacion SERIAL PRIMARY KEY,
            nombre VARCHAR(50) NOT NULL,
            codigo VARCHAR(30) NOT NULL UNIQUE,
            descripcion TEXT,
            activo BOOLEAN DEFAULT TRUE NOT NULL
        );
        ALTER TABLE postulacion.tipo_estado_evaluacion OWNER TO admin1;
    END IF;

    -- Tabla: postulacion.banco_temas
    IF NOT EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = 'postulacion' AND table_name = 'banco_temas') THEN
        CREATE TABLE postulacion.banco_temas (
            id_tema SERIAL PRIMARY KEY,
            id_convocatoria INTEGER NOT NULL REFERENCES convocatoria.convocatoria(id_convocatoria),
            descripcion_tema VARCHAR(255) NOT NULL,
            activo BOOLEAN DEFAULT TRUE NOT NULL
        );
        ALTER TABLE postulacion.banco_temas OWNER TO admin1;
    END IF;

    -- Columnas en postulacion.evaluacion_meritos
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema = 'postulacion' AND table_name = 'evaluacion_meritos' AND column_name = 'nota_total_meritos') THEN
        ALTER TABLE postulacion.evaluacion_meritos ADD COLUMN nota_total_meritos NUMERIC(5,2);
    END IF;

    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema = 'postulacion' AND table_name = 'evaluacion_meritos' AND column_name = 'id_tipo_estado_evaluacion') THEN
        ALTER TABLE postulacion.evaluacion_meritos ADD COLUMN id_tipo_estado_evaluacion INTEGER REFERENCES postulacion.tipo_estado_evaluacion(id_tipo_estado_evaluacion);
    END IF;

    -- Columnas en postulacion.evaluacion_oposicion
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema = 'postulacion' AND table_name = 'evaluacion_oposicion' AND column_name = 'orden_exposicion') THEN
        ALTER TABLE postulacion.evaluacion_oposicion ADD COLUMN orden_exposicion INTEGER;
    END IF;

    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema = 'postulacion' AND table_name = 'evaluacion_oposicion' AND column_name = 'hora_inicio_real') THEN
        ALTER TABLE postulacion.evaluacion_oposicion ADD COLUMN hora_inicio_real TIME;
    END IF;

    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema = 'postulacion' AND table_name = 'evaluacion_oposicion' AND column_name = 'hora_fin_real') THEN
        ALTER TABLE postulacion.evaluacion_oposicion ADD COLUMN hora_fin_real TIME;
    END IF;

    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema = 'postulacion' AND table_name = 'evaluacion_oposicion' AND column_name = 'puntaje_total_oposicion') THEN
        ALTER TABLE postulacion.evaluacion_oposicion ADD COLUMN puntaje_total_oposicion NUMERIC(5,2);
    END IF;

    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema = 'postulacion' AND table_name = 'evaluacion_oposicion' AND column_name = 'id_tipo_estado_evaluacion') THEN
        ALTER TABLE postulacion.evaluacion_oposicion ADD COLUMN id_tipo_estado_evaluacion INTEGER REFERENCES postulacion.tipo_estado_evaluacion(id_tipo_estado_evaluacion);
    END IF;

END $$;
