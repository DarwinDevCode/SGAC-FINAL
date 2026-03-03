-- Crea la tabla de auditoría usada por funciones del esquema ayudantia.
-- Evita error: relation "academico.log_auditoria" does not exist

CREATE SCHEMA IF NOT EXISTS academico;

CREATE TABLE IF NOT EXISTS academico.log_auditoria (
    id_log_auditoria      BIGSERIAL PRIMARY KEY,
    id_usuario            INTEGER,
    id_tipo_rol           INTEGER,
    accion                VARCHAR(20)  NOT NULL,
    tabla_afectada        VARCHAR(100) NOT NULL,
    registro_afectado     INTEGER,
    fecha_hora            TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    valor_anterior        TEXT,
    valor_nuevo           TEXT
);

-- Índices opcionales para consultas por usuario/fecha
CREATE INDEX IF NOT EXISTS idx_log_auditoria_usuario_fecha
    ON academico.log_auditoria (id_usuario, fecha_hora DESC);

