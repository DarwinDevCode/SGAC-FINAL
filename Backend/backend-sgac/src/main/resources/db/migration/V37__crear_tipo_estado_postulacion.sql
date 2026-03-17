-- 1. Asegurar la existencia de la tabla de catálogo
CREATE TABLE IF NOT EXISTS postulacion.tipo_estado_postulacion (
                                                                   id_tipo_estado_postulacion  SERIAL PRIMARY KEY,
                                                                   codigo                      VARCHAR(30) NOT NULL UNIQUE,
                                                                   nombre                      VARCHAR(100) NOT NULL,
                                                                   descripcion                 VARCHAR(255),
                                                                   activo                      BOOLEAN DEFAULT TRUE,
                                                                   fecha_creacion              TIMESTAMP DEFAULT NOW()
);


-- 3. Asegurar que la columna FK existe en la tabla principal
-- (Ya lo hicimos antes, pero esto lo garantiza sin errores)
ALTER TABLE postulacion.postulacion
    ADD COLUMN IF NOT EXISTS id_tipo_estado_postulacion INTEGER;

-- 4. Limpieza de huérfanos: Si algún registro quedó sin estado, poner PENDIENTE
UPDATE postulacion.postulacion
SET id_tipo_estado_postulacion = (SELECT id_tipo_estado_postulacion FROM postulacion.tipo_estado_postulacion WHERE codigo = 'PENDIENTE')
WHERE id_tipo_estado_postulacion IS NULL;

-- 5. Restricción de Integridad (Solo si no existe)
DO $$
    BEGIN
        IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_postulacion_tipo_estado') THEN
            ALTER TABLE postulacion.postulacion
                ADD CONSTRAINT fk_postulacion_tipo_estado
                    FOREIGN KEY (id_tipo_estado_postulacion)
                        REFERENCES postulacion.tipo_estado_postulacion(id_tipo_estado_postulacion);
        END IF;
    END $$;

-- 6. Índice de rendimiento para el Coordinador y Dashboard
CREATE INDEX IF NOT EXISTS idx_postulacion_tipo_estado
    ON postulacion.postulacion(id_tipo_estado_postulacion);

-- 7. Vista Actualizada (Sin la columna legacy 'estado_postulacion')
DROP VIEW IF EXISTS postulacion.v_postulacion_con_estado;
CREATE OR REPLACE VIEW postulacion.v_postulacion_con_estado AS
SELECT
    p.id_postulacion,
    p.id_convocatoria,
    p.id_estudiante,
    p.fecha_postulacion,
    tep.codigo AS estado_codigo,
    tep.nombre AS estado_nombre,
    p.observaciones,
    p.activo
FROM postulacion.postulacion p
         INNER JOIN postulacion.tipo_estado_postulacion tep
                    ON p.id_tipo_estado_postulacion = tep.id_tipo_estado_postulacion;

-- 8. Permisos de seguridad
GRANT SELECT ON postulacion.tipo_estado_postulacion TO role_estudiante, role_coordinador, role_docente, role_decano;
GRANT SELECT ON postulacion.v_postulacion_con_estado TO role_estudiante, role_coordinador;