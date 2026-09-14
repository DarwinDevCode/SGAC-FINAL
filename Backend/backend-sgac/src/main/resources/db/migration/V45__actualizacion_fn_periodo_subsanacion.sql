CREATE OR REPLACE FUNCTION convocatoria.fn_es_periodo_subsanacion(
    p_id_convocatoria INTEGER
)
    RETURNS BOOLEAN
    LANGUAGE plpgsql
    SECURITY DEFINER
AS $$
DECLARE
    v_id_periodo INTEGER;
    v_fecha_inicio_postulacion DATE;
    v_fecha_fin_evaluacion DATE;
BEGIN
    SELECT id_periodo_academico INTO v_id_periodo
    FROM convocatoria.convocatoria
    WHERE id_convocatoria = p_id_convocatoria;

    IF v_id_periodo IS NULL THEN
        RETURN FALSE;
    END IF;

    SELECT
        MIN(CASE WHEN tf.codigo = 'POSTULACION' THEN pf.fecha_inicio END),
        MAX(CASE WHEN tf.codigo = 'EVALUACION_REQUISITOS' THEN pf.fecha_fin END)
    INTO
        v_fecha_inicio_postulacion,
        v_fecha_fin_evaluacion
    FROM planificacion.periodo_fase pf
             JOIN planificacion.tipo_fase tf ON pf.id_tipo_fase = tf.id_tipo_fase
    WHERE pf.id_periodo_academico = v_id_periodo
      AND tf.codigo IN ('POSTULACION', 'EVALUACION_REQUISITOS');

    RETURN (CURRENT_DATE BETWEEN v_fecha_inicio_postulacion AND v_fecha_fin_evaluacion);

EXCEPTION WHEN OTHERS THEN
    RETURN FALSE;
END;
$$;