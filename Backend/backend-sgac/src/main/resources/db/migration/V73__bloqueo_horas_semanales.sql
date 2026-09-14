-- =================================================================================
-- V73: Bloqueo real del limite de horas semanales (Bloque 1D - F1.13)
-- Ningun ayudante puede registrar mas de 20 horas en una misma semana.
-- =================================================================================

CREATE OR REPLACE FUNCTION ayudantia.fn_validar_limite_horas_semanales()
RETURNS TRIGGER AS $$
DECLARE
    v_horas_semana NUMERIC(5,2);
    v_semana_anio  INTEGER;
    v_anio         INTEGER;
    v_horas_nuevas NUMERIC(5,2);
BEGIN
    -- Determinar la semana y año de la sesion
    v_semana_anio := EXTRACT(WEEK FROM NEW.fecha_actividad);
    v_anio        := EXTRACT(YEAR FROM NEW.fecha_actividad);

    -- Calcular la duracion en horas de la sesion actual
    v_horas_nuevas := NEW.horas_realizadas;

    -- Sumar las horas ya registradas para ese ayudante en la misma semana y año
    SELECT COALESCE(SUM(horas_realizadas), 0)
    INTO v_horas_semana
    FROM ayudantia.registro_actividad
    WHERE id_ayudantia = NEW.id_ayudantia
      AND EXTRACT(WEEK FROM fecha_actividad) = v_semana_anio
      AND EXTRACT(YEAR FROM fecha_actividad) = v_anio
      AND id_registro_actividad != COALESCE(NEW.id_registro_actividad, -1); -- Excluir la sesion actual si es un UPDATE

    -- Validar que la suma total no exceda 20 horas
    IF (v_horas_semana + v_horas_nuevas) > 20.0 THEN
        RAISE EXCEPTION 'El límite de horas semanales (20 horas) ha sido excedido. Horas previas esta semana: %, Horas a registrar: %', ROUND(v_horas_semana, 2), ROUND(v_horas_nuevas, 2);
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_validar_horas_semanales ON ayudantia.registro_actividad;

CREATE TRIGGER trg_validar_horas_semanales
BEFORE INSERT OR UPDATE ON ayudantia.registro_actividad
FOR EACH ROW
EXECUTE FUNCTION ayudantia.fn_validar_limite_horas_semanales();
