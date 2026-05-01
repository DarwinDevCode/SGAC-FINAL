-- ============================================================
-- V12 — SP para importar requisitos de un período anterior
-- ============================================================

/**
 * Copia todos los requisitos activos del periodo de origen al periodo destino.
 * Si el requisito ya existe en el destino (misma combinación idPeriodo + idTipoReq)
 * no lo duplica.
 * Retorna la cantidad de requisitos importados.
 */
CREATE OR REPLACE FUNCTION public.sp_importar_requisitos_periodo(
    p_id_periodo_origen  INTEGER,
    p_id_periodo_destino INTEGER
)
RETURNS INTEGER AS $$
DECLARE
    v_count INTEGER := 0;
    r RECORD;
BEGIN
    FOR r IN
        SELECT id_tipo_requisito_postulacion, obligatorio, orden
        FROM convocatoria.periodo_academico_requisito_postulacion
        WHERE id_periodo_academico = p_id_periodo_origen
          AND activo = TRUE
    LOOP
        -- Solo insertar si no existe ya para este periodo destino
        IF NOT EXISTS (
            SELECT 1 FROM convocatoria.periodo_academico_requisito_postulacion
            WHERE id_periodo_academico = p_id_periodo_destino
              AND id_tipo_requisito_postulacion = r.id_tipo_requisito_postulacion
              AND activo = TRUE
        ) THEN
            PERFORM public.sp_crear_periodo_requisito(
                p_id_periodo_destino,
                r.id_tipo_requisito_postulacion,
                r.obligatorio,
                r.orden
            );
            v_count := v_count + 1;
        END IF;
    END LOOP;

    RETURN v_count;
END;
$$ LANGUAGE plpgsql;
