-- ============================================================
-- V11 — Tipo de documento permitido + SP reemplazar adjunto
-- ============================================================

-- 1. Añadir columna tipo_documento_permitido a tipo_requisito_postulacion
--    Acepta lista separada por comas: 'PDF', 'PDF,DOCX', 'JPG,PNG', etc.
ALTER TABLE convocatoria.tipo_requisito_postulacion
    ADD COLUMN IF NOT EXISTS tipo_documento_permitido VARCHAR(100) DEFAULT 'PDF';

-- 2. Función pública para reemplazar un requisito adjunto
--    Llamada por el postulante cuando el coordinador marca un requisito como OBSERVADO.
CREATE OR REPLACE FUNCTION public.sp_reemplazar_requisito_adjunto(
    p_id_adjunto   INTEGER,
    p_archivo      BYTEA,
    p_nombre       VARCHAR,
    p_fecha_subida DATE
)
RETURNS INTEGER AS $$
DECLARE
    v_id_estado_pendiente INTEGER;
BEGIN
    -- Obtener el id del estado PENDIENTE/ENTREGADO (id = 1 por convención del proyecto)
    v_id_estado_pendiente := 1;

    UPDATE postulacion.requisito_adjunto
    SET archivo                = p_archivo,
        nombre_archivo         = p_nombre,
        fecha_subida           = p_fecha_subida,
        id_tipo_estado_requisito = v_id_estado_pendiente,
        observacion            = NULL
    WHERE id_requisito_adjunto = p_id_adjunto;

    IF NOT FOUND THEN
        RETURN -1;
    END IF;

    RETURN p_id_adjunto;
END;
$$ LANGUAGE plpgsql;
