-- =================================================================================
-- V76: Firma electronica y Auditoria IA
-- =================================================================================

-- 1. Firma Electrónica (F1.21)
-- Guardar el registro de las firmas realizadas en documentos
CREATE TABLE IF NOT EXISTS ayudantia.firma_documento (
    id_firma             SERIAL PRIMARY KEY,
    id_informe           INTEGER REFERENCES ayudantia.informe(id_informe),
    id_certificado       INTEGER REFERENCES ayudantia.certificado(id_certificado),
    id_usuario_firmante  INTEGER NOT NULL REFERENCES seguridad.usuario(id_usuario),
    hash_documento       VARCHAR(256) NOT NULL,
    sello_tiempo         TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    ip_origen            VARCHAR(50),
    certificado_digital_usado TEXT,
    CONSTRAINT chk_documento_firmado CHECK (
        (id_informe IS NOT NULL AND id_certificado IS NULL) OR 
        (id_informe IS NULL AND id_certificado IS NOT NULL)
    )
);

-- 2. Auditoría IA (F1.22)
-- Guardar las consultas al Asistente IA
CREATE TABLE IF NOT EXISTS ayudantia.log_asistente_ia (
    id_log_ia            SERIAL PRIMARY KEY,
    id_usuario           INTEGER NOT NULL REFERENCES seguridad.usuario(id_usuario),
    tipo_consulta        VARCHAR(50) NOT NULL CHECK (tipo_consulta IN ('REGLAMENTO', 'REVISION_TEXTO', 'SUGERENCIA_RUBRICA')),
    prompt_enviado       TEXT NOT NULL,
    respuesta_recibida   TEXT NOT NULL,
    tokens_usados        INTEGER,
    fecha_consulta       TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
