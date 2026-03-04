-- V30: Stored Procedures para módulo Docente
-- Incluye creación de tablas y columnas faltantes detectadas en ejecución

-- ─────────────────────────────────────────────
-- 0a. tipo_estado_ayudantia (tabla lookup faltante)
-- ─────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS ayudantia.tipo_estado_ayudantia (
    id_tipo_estado_ayudantia SERIAL PRIMARY KEY,
    nombre_estado            VARCHAR(50) NOT NULL,
    descripcion              TEXT,
    activo                   BOOLEAN     DEFAULT TRUE
);

INSERT INTO ayudantia.tipo_estado_ayudantia (nombre_estado, descripcion, activo) VALUES
    ('ACTIVO',    'Ayudantía activa',              TRUE),
    ('INACTIVO',  'Ayudantía inactiva',            TRUE),
    ('FINALIZADO','Ayudantía finalizada',          TRUE),
    ('PENDIENTE', 'Ayudantía pendiente de inicio', TRUE)
ON CONFLICT DO NOTHING;

GRANT SELECT ON ayudantia.tipo_estado_ayudantia TO app_user_default;
GRANT SELECT ON ayudantia.tipo_estado_ayudantia TO administrador_consultas;

-- Agregar FK en ayudantia.ayudantia si no existe
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'ayudantia'
          AND table_name   = 'ayudantia'
          AND column_name  = 'id_tipo_estado_ayudantia'
    ) THEN
        ALTER TABLE ayudantia.ayudantia
            ADD COLUMN id_tipo_estado_ayudantia INTEGER NOT NULL DEFAULT 1
                REFERENCES ayudantia.tipo_estado_ayudantia(id_tipo_estado_ayudantia);
    END IF;
END $$;

-- ─────────────────────────────────────────────
-- 0b. Columnas faltantes en registro_actividad
-- ─────────────────────────────────────────────
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'ayudantia'
          AND table_name   = 'registro_actividad'
          AND column_name  = 'observaciones'
    ) THEN
        ALTER TABLE ayudantia.registro_actividad
            ADD COLUMN observaciones VARCHAR(500);
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'ayudantia'
          AND table_name   = 'registro_actividad'
          AND column_name  = 'fecha_observacion'
    ) THEN
        ALTER TABLE ayudantia.registro_actividad
            ADD COLUMN fecha_observacion DATE;
    END IF;
END $$;

-- ─────────────────────────────────────────────
-- 0c. Columnas faltantes en evidencia_registro_actividad
-- ─────────────────────────────────────────────
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'ayudantia'
          AND table_name   = 'evidencia_registro_actividad'
          AND column_name  = 'observaciones'
    ) THEN
        ALTER TABLE ayudantia.evidencia_registro_actividad
            ADD COLUMN observaciones VARCHAR(500);
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'ayudantia'
          AND table_name   = 'evidencia_registro_actividad'
          AND column_name  = 'fecha_observacion'
    ) THEN
        ALTER TABLE ayudantia.evidencia_registro_actividad
            ADD COLUMN fecha_observacion DATE;
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'ayudantia'
          AND table_name   = 'evidencia_registro_actividad'
          AND column_name  = 'mime_type'
    ) THEN
        ALTER TABLE ayudantia.evidencia_registro_actividad
            ADD COLUMN mime_type VARCHAR(100);
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'ayudantia'
          AND table_name   = 'evidencia_registro_actividad'
          AND column_name  = 'ruta_archivo'
    ) THEN
        ALTER TABLE ayudantia.evidencia_registro_actividad
            ADD COLUMN ruta_archivo VARCHAR(500) NOT NULL DEFAULT '';
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'ayudantia'
          AND table_name   = 'evidencia_registro_actividad'
          AND column_name  = 'tamanio_bytes'
    ) THEN
        ALTER TABLE ayudantia.evidencia_registro_actividad
            ADD COLUMN tamanio_bytes INTEGER;
    END IF;
END $$;

-- ─────────────────────────────────────────────
-- 1. sp_resumen_docente  (Dashboard KPIs)
-- ─────────────────────────────────────────────
DROP FUNCTION IF EXISTS public.sp_resumen_docente(INTEGER);

CREATE OR REPLACE FUNCTION public.sp_resumen_docente(p_id_usuario_docente INTEGER)
RETURNS TABLE (
    total_ayudantes           BIGINT,
    actividades_pendientes    BIGINT,
    actividades_aceptadas     BIGINT,
    actividades_rechazadas    BIGINT,
    actividades_observadas    BIGINT,
    total_actividades         BIGINT
)
LANGUAGE sql
STABLE
AS $$
    SELECT
        COUNT(DISTINCT ay.id_ayudantia)                                        AS total_ayudantes,
        COUNT(ra.id_registro_actividad)
            FILTER (WHERE UPPER(ter.nombre_estado) = 'PENDIENTE')              AS actividades_pendientes,
        COUNT(ra.id_registro_actividad)
            FILTER (WHERE UPPER(ter.nombre_estado) = 'ACEPTADO')               AS actividades_aceptadas,
        COUNT(ra.id_registro_actividad)
            FILTER (WHERE UPPER(ter.nombre_estado) = 'RECHAZADO')              AS actividades_rechazadas,
        COUNT(ra.id_registro_actividad)
            FILTER (WHERE UPPER(ter.nombre_estado) = 'OBSERVADO')              AS actividades_observadas,
        COUNT(ra.id_registro_actividad)                                        AS total_actividades
    FROM ayudantia.ayudantia ay
    JOIN postulacion.postulacion pp      ON pp.id_postulacion  = ay.id_postulacion
    JOIN convocatoria.convocatoria cv    ON cv.id_convocatoria = pp.id_convocatoria
    JOIN academico.docente dc            ON dc.id_docente      = cv.id_docente
    JOIN seguridad.usuario u_doc         ON u_doc.id_usuario   = dc.id_usuario
    LEFT JOIN ayudantia.registro_actividad ra
                                         ON ra.id_ayudantia    = ay.id_ayudantia
    LEFT JOIN ayudantia.tipo_estado_registro ter
                                         ON ter.id_tipo_estado_registro = ra.id_tipo_estado_registro
    WHERE u_doc.id_usuario = p_id_usuario_docente;
$$;

-- ─────────────────────────────────────────────
-- 2. sp_listar_ayudantes_docente
-- ─────────────────────────────────────────────
DROP FUNCTION IF EXISTS public.sp_listar_ayudantes_docente(INTEGER);

CREATE OR REPLACE FUNCTION public.sp_listar_ayudantes_docente(p_id_usuario_docente INTEGER)
RETURNS TABLE (
    id_ayudantia              INTEGER,
    id_usuario                INTEGER,
    nombre_completo           TEXT,
    correo                    VARCHAR,
    nombre_asignatura         VARCHAR,
    estado_ayudantia          VARCHAR,
    horas_cumplidas           INTEGER,
    actividades_total         BIGINT,
    actividades_pendientes    BIGINT
)
LANGUAGE sql
STABLE
AS $$
    SELECT
        ay.id_ayudantia,
        u.id_usuario,
        CONCAT(u.nombres, ' ', u.apellidos)      AS nombre_completo,
        u.correo                                  AS correo,
        '—'::VARCHAR                              AS nombre_asignatura,
        tea.nombre_estado                         AS estado_ayudantia,
        ay.horas_cumplidas,
        COUNT(ra.id_registro_actividad)           AS actividades_total,
        COUNT(ra.id_registro_actividad)
            FILTER (WHERE UPPER(ter.nombre_estado) = 'PENDIENTE') AS actividades_pendientes
    FROM ayudantia.ayudantia ay
    JOIN postulacion.postulacion pp      ON pp.id_postulacion  = ay.id_postulacion
    JOIN convocatoria.convocatoria cv    ON cv.id_convocatoria = pp.id_convocatoria
    JOIN academico.docente dc            ON dc.id_docente      = cv.id_docente
    JOIN seguridad.usuario u_doc         ON u_doc.id_usuario   = dc.id_usuario
    JOIN academico.estudiante est        ON est.id_estudiante  = pp.id_estudiante
    JOIN seguridad.usuario u             ON u.id_usuario       = est.id_usuario
    JOIN ayudantia.tipo_estado_ayudantia tea
                                         ON tea.id_tipo_estado_ayudantia = ay.id_tipo_estado_ayudantia
    LEFT JOIN ayudantia.registro_actividad ra
                                         ON ra.id_ayudantia = ay.id_ayudantia
    LEFT JOIN ayudantia.tipo_estado_registro ter
                                         ON ter.id_tipo_estado_registro = ra.id_tipo_estado_registro
    WHERE u_doc.id_usuario = p_id_usuario_docente
    GROUP BY ay.id_ayudantia, u.id_usuario, u.nombres, u.apellidos,
             u.correo, tea.nombre_estado, ay.horas_cumplidas
    ORDER BY actividades_pendientes DESC, nombre_completo;
$$;

-- ─────────────────────────────────────────────
-- 3. sp_actividades_ayudante_docente
-- ─────────────────────────────────────────────
DROP FUNCTION IF EXISTS public.sp_actividades_ayudante_docente(INTEGER);

CREATE OR REPLACE FUNCTION public.sp_actividades_ayudante_docente(p_id_ayudantia INTEGER)
RETURNS TABLE (
    id_registro_actividad     INTEGER,
    descripcion_actividad     TEXT,
    tema_tratado              TEXT,
    fecha                     DATE,
    numero_asistentes         INTEGER,
    horas_dedicadas           NUMERIC,
    estado_revision           VARCHAR,
    observaciones             VARCHAR,
    fecha_observacion         DATE
)
LANGUAGE sql
STABLE
AS $$
    SELECT
        ra.id_registro_actividad,
        ra.descripcion_actividad,
        ra.tema_tratado,
        ra.fecha,
        ra.numero_asistentes,
        ra.horas_dedicadas,
        ter.nombre_estado     AS estado_revision,
        ra.observaciones,
        ra.fecha_observacion
    FROM ayudantia.registro_actividad ra
    JOIN ayudantia.tipo_estado_registro ter
                              ON ter.id_tipo_estado_registro = ra.id_tipo_estado_registro
    WHERE ra.id_ayudantia = p_id_ayudantia
    ORDER BY ra.fecha DESC, ra.id_registro_actividad DESC;
$$;

-- ─────────────────────────────────────────────
-- 4. sp_evidencias_actividad_docente
-- ─────────────────────────────────────────────
DROP FUNCTION IF EXISTS public.sp_evidencias_actividad_docente(INTEGER);

CREATE OR REPLACE FUNCTION public.sp_evidencias_actividad_docente(p_id_registro INTEGER)
RETURNS TABLE (
    id_evidencia_registro_actividad  INTEGER,
    tipo_evidencia                   VARCHAR,
    nombre_archivo                   VARCHAR,
    ruta_archivo                     VARCHAR,
    mime_type                        VARCHAR,
    fecha_subida                     DATE,
    estado_evidencia                 VARCHAR,
    observaciones                    VARCHAR,
    fecha_observacion                DATE
)
LANGUAGE sql
STABLE
AS $$
    SELECT
        e.id_evidencia_registro_actividad,
        te.nombre                     AS tipo_evidencia,
        e.nombre_archivo,
        e.ruta_archivo,
        e.mime_type,
        e.fecha_subida,
        tee.nombre_estado             AS estado_evidencia,
        e.observaciones,
        e.fecha_observacion
    FROM ayudantia.evidencia_registro_actividad e
    JOIN ayudantia.tipo_evidencia te
                                      ON te.id_tipo_evidencia = e.id_tipo_evidencia
    JOIN ayudantia.tipo_estado_evidencia tee
                                      ON tee.id_tipo_estado_evidencia = e.id_tipo_estado_evidencia
    WHERE e.id_registro_actividad = p_id_registro
      AND e.activo = TRUE
    ORDER BY e.fecha_subida;
$$;

-- Permisos
GRANT EXECUTE ON FUNCTION public.sp_resumen_docente(INTEGER)                TO app_user_default;
GRANT EXECUTE ON FUNCTION public.sp_listar_ayudantes_docente(INTEGER)       TO app_user_default;
GRANT EXECUTE ON FUNCTION public.sp_actividades_ayudante_docente(INTEGER)   TO app_user_default;
GRANT EXECUTE ON FUNCTION public.sp_evidencias_actividad_docente(INTEGER)   TO app_user_default;
