
CREATE TABLE IF NOT EXISTS postulacion.tipo_estado_postulacion (
    id_tipo_estado_postulacion  SERIAL PRIMARY KEY,
    codigo                      VARCHAR(30) NOT NULL UNIQUE,
    nombre                      VARCHAR(100) NOT NULL,
    descripcion                 VARCHAR(255),
    activo                      BOOLEAN DEFAULT TRUE,
    fecha_creacion              TIMESTAMP DEFAULT NOW()
);

-- 2. Insertar los estados posibles de una postulación
INSERT INTO postulacion.tipo_estado_postulacion (codigo, nombre, descripcion) VALUES
    ('PENDIENTE', 'Pendiente', 'La postulación ha sido recibida y está pendiente de revisión'),
    ('EN_REVISION', 'En Revisión', 'La postulación está siendo revisada por el coordinador'),
    ('OBSERVADA', 'Observada', 'La postulación tiene documentos observados que requieren corrección'),
    ('CORREGIDA', 'Corregida', 'El estudiante ha subsanado las observaciones'),
    ('APROBADA', 'Aprobada', 'La postulación ha sido aprobada'),
    ('RECHAZADA', 'Rechazada', 'La postulación ha sido rechazada'),
    ('SELECCIONADO', 'Seleccionado', 'El estudiante ha sido seleccionado como ayudante'),
    ('NO_SELECCIONADO', 'No Seleccionado', 'El estudiante no fue seleccionado en esta convocatoria'),
    ('CANCELADA', 'Cancelada', 'La postulación fue cancelada por el estudiante')
ON CONFLICT (codigo) DO NOTHING;

-- 3. Agregar columna FK a la tabla postulacion.postulacion
ALTER TABLE postulacion.postulacion
ADD COLUMN IF NOT EXISTS id_tipo_estado_postulacion INTEGER;

-- 4. Migrar datos existentes: convertir estado_postulacion (VARCHAR) a id_tipo_estado_postulacion (FK)
UPDATE postulacion.postulacion p
SET id_tipo_estado_postulacion = tep.id_tipo_estado_postulacion
FROM postulacion.tipo_estado_postulacion tep
WHERE UPPER(TRIM(p.estado_postulacion)) = UPPER(tep.codigo)
  AND p.id_tipo_estado_postulacion IS NULL;

-- 5. Para registros sin match, asignar estado PENDIENTE por defecto
UPDATE postulacion.postulacion p
SET id_tipo_estado_postulacion = (
    SELECT id_tipo_estado_postulacion
    FROM postulacion.tipo_estado_postulacion
    WHERE codigo = 'PENDIENTE'
)
WHERE p.id_tipo_estado_postulacion IS NULL;

-- 6. Agregar la restricción de clave foránea
ALTER TABLE postulacion.postulacion
ADD CONSTRAINT fk_postulacion_tipo_estado
FOREIGN KEY (id_tipo_estado_postulacion)
REFERENCES postulacion.tipo_estado_postulacion(id_tipo_estado_postulacion);

-- 7. Crear índice para mejorar rendimiento en consultas por estado
CREATE INDEX IF NOT EXISTS idx_postulacion_tipo_estado
ON postulacion.postulacion(id_tipo_estado_postulacion);

-- 9. (Opcional) Crear vista para facilitar consultas con el estado
CREATE OR REPLACE VIEW postulacion.v_postulacion_con_estado AS
SELECT
    p.id_postulacion,
    p.id_convocatoria,
    p.id_estudiante,
    p.fecha_postulacion,
    p.estado_postulacion AS estado_postulacion_legacy,
    tep.codigo AS estado_codigo,
    tep.nombre AS estado_nombre,
    p.observaciones,
    p.activo
FROM postulacion.postulacion p
LEFT JOIN postulacion.tipo_estado_postulacion tep
    ON p.id_tipo_estado_postulacion = tep.id_tipo_estado_postulacion;


-- Permisos
GRANT SELECT ON postulacion.tipo_estado_postulacion TO role_estudiante;
GRANT SELECT ON postulacion.tipo_estado_postulacion TO role_coordinador;
GRANT SELECT ON postulacion.tipo_estado_postulacion TO role_docente;
GRANT SELECT ON postulacion.tipo_estado_postulacion TO role_decano;
GRANT SELECT ON postulacion.v_postulacion_con_estado TO role_estudiante;
GRANT SELECT ON postulacion.v_postulacion_con_estado TO role_coordinador;

