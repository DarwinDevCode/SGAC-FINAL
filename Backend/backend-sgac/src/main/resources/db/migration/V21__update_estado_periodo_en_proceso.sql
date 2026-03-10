-- =============================================================
-- V21: Actualizar funciones y triggers para usar el estado 'EN PROCESO'
-- =============================================================

-- Actualizar todos los periodos que estaban como 'ACTIVO' a 'EN PROCESO' para no romper la consistencia
UPDATE academico.periodo_academico
SET estado = 'EN PROCESO'
WHERE estado = 'ACTIVO';

-- ---------------------------------------------------------------
-- Función disparadora: cambia estado a INACTIVO si fecha_fin < hoy
-- ---------------------------------------------------------------
CREATE OR REPLACE FUNCTION academico.fn_verificar_estado_periodo()
RETURNS TRIGGER AS $$
BEGIN
    IF NEW.fecha_fin < CURRENT_DATE AND NEW.estado = 'EN PROCESO' THEN
        NEW.estado  := 'INACTIVO';
        NEW.activo  := FALSE;
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- ---------------------------------------------------------------
-- Función pública: inactivar períodos vencidos (llamada por el scheduler de Spring)
-- ---------------------------------------------------------------
CREATE OR REPLACE FUNCTION public.fn_inactivar_periodos_vencidos()
RETURNS INTEGER AS $$
DECLARE
    v_filas_afectadas INTEGER;
BEGIN
    UPDATE academico.periodo_academico
    SET estado = 'INACTIVO',
        activo = FALSE
    WHERE fecha_fin < CURRENT_DATE
      AND estado = 'EN PROCESO';

    GET DIAGNOSTICS v_filas_afectadas = ROW_COUNT;
    RETURN v_filas_afectadas;
END;
$$ LANGUAGE plpgsql;

-- ---------------------------------------------------------------
-- Función pública: activar un período manualmente por ID
-- ---------------------------------------------------------------
CREATE OR REPLACE FUNCTION public.fn_activar_periodo_academico(p_id INTEGER)
RETURNS INTEGER AS $$
DECLARE
    v_existe INTEGER;
BEGIN
    SELECT COUNT(*) INTO v_existe
    FROM academico.periodo_academico
    WHERE id_periodo_academico = p_id;

    IF v_existe = 0 THEN
        RETURN -1; -- No existe
    END IF;

    UPDATE academico.periodo_academico
    SET estado = 'EN PROCESO',
        activo = TRUE
    WHERE id_periodo_academico = p_id;

    RETURN p_id;
END;
$$ LANGUAGE plpgsql;
