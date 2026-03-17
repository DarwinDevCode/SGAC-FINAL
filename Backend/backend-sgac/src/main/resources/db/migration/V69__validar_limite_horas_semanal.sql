-- ============================================================
-- V69 — Validación de límite de 20 horas semanales
-- Trigger para impedir registros que excedan la carga permitida
-- ============================================================

CREATE OR REPLACE FUNCTION ayudantia.fn_trg_validar_limite_horas_semanal()
RETURNS TRIGGER AS $$
DECLARE
    v_horas_acumuladas NUMERIC(6,2);
    v_limite_semanal   CONSTANT NUMERIC := 20;
    v_semana_inicio    DATE;
    v_semana_fin       DATE;
BEGIN
    -- Determinar el rango de la semana para la fecha del registro
    v_semana_inicio := DATE_TRUNC('week', NEW.fecha)::DATE;
    v_semana_fin    := (DATE_TRUNC('week', NEW.fecha) + INTERVAL '6 days')::DATE;

    -- Calcular horas ya registradas en esa semana (excluyendo el registro actual si es un UPDATE)
    SELECT COALESCE(SUM(horas_dedicadas), 0)
    INTO   v_horas_acumuladas
    FROM   ayudantia.registro_actividad
    WHERE  id_ayudantia = NEW.id_ayudantia
      AND  fecha >= v_semana_inicio
      AND  fecha <= v_semana_fin
      AND  (TG_OP = 'INSERT' OR id_registro_actividad <> NEW.id_registro_actividad);

    -- Validar si el nuevo total excede el límite
    IF (v_horas_acumuladas + NEW.horas_dedicadas) > v_limite_semanal THEN
        RAISE EXCEPTION 'No se puede registrar la actividad. El total de horas para la semana (% - %) excedería el límite de % horas permitidas. (Horas actuales: %, Intento: %)', 
            v_semana_inicio, v_semana_fin, v_limite_semanal, v_horas_acumuladas, NEW.horas_dedicadas;
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Crear el trigger
DROP TRIGGER IF EXISTS trg_validar_horas_semanal ON ayudantia.registro_actividad;

CREATE TRIGGER trg_validar_horas_semanal
BEFORE INSERT OR UPDATE ON ayudantia.registro_actividad
FOR EACH ROW
EXECUTE FUNCTION ayudantia.fn_trg_validar_limite_horas_semanal();
