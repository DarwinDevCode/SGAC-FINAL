CREATE OR REPLACE FUNCTION planificacion.fn_obtener_cronograma_activo()
    RETURNS JSONB
    LANGUAGE plpgsql
    SECURITY DEFINER
    STABLE
AS $function$
DECLARE
    v_periodo    RECORD;
    v_fases      JSONB;
    v_dias_total INTEGER;
    v_dias_trans INTEGER;
    v_pct        NUMERIC(5,2);
BEGIN
    SELECT
        id_periodo_academico,
        nombre_periodo,
        fecha_inicio,
        fecha_fin
    INTO v_periodo
    FROM academico.periodo_academico
    WHERE estado  = 'EN PROCESO'
      AND activo  = TRUE
    ORDER BY fecha_inicio DESC
    LIMIT 1;

    IF NOT FOUND THEN
        RETURN jsonb_build_object(
                'exito',   false,
                'mensaje', 'No existe un período académico activo en este momento. '
                    || 'Consulte con el administrador del sistema.'
               );
    END IF;

    v_dias_total := (v_periodo.fecha_fin - v_periodo.fecha_inicio) + 1;
    v_dias_trans := GREATEST(0,
                             LEAST(
                                     (CURRENT_DATE - v_periodo.fecha_inicio) + 1,
                                     v_dias_total
                             )
                    );
    v_pct := ROUND((v_dias_trans::NUMERIC / v_dias_total::NUMERIC) * 100, 2);

    SELECT jsonb_agg(
                   jsonb_build_object(
                           'idPeriodoFase', pf.id_periodo_fase,
                           'idTipoFase',    pf.id_tipo_fase,
                           'orden',         tf.orden,
                           'codigo',        tf.codigo,
                           'nombre',        tf.nombre,
                           'descripcion',   COALESCE(tf.descripcion, ''),
                           'fechaInicio',   to_char(pf.fecha_inicio, 'YYYY-MM-DD'),
                           'fechaFin',      to_char(pf.fecha_fin,    'YYYY-MM-DD'),
                           'duracionDias',  (pf.fecha_fin - pf.fecha_inicio) + 1,
                           'esActual',      (CURRENT_DATE BETWEEN pf.fecha_inicio AND pf.fecha_fin)
                   )
                   ORDER BY tf.orden ASC
           )
    INTO v_fases
    FROM planificacion.periodo_fase pf
             JOIN planificacion.tipo_fase    tf ON tf.id_tipo_fase = pf.id_tipo_fase
    WHERE pf.id_periodo_academico = v_periodo.id_periodo_academico;

    IF v_fases IS NULL THEN
        v_fases := '[]'::jsonb;
    END IF;

    RETURN jsonb_build_object(
            'exito',   true,
            'periodo', jsonb_build_object(
                    'id',                v_periodo.id_periodo_academico,
                    'nombre',            v_periodo.nombre_periodo,
                    'fechaInicio',       to_char(v_periodo.fecha_inicio, 'YYYY-MM-DD'),
                    'fechaFin',          to_char(v_periodo.fecha_fin,    'YYYY-MM-DD'),
                    'diasTranscurridos', v_dias_trans,
                    'diasTotales',       v_dias_total,
                    'porcentajeAvance',  v_pct
                       ),
            'fases',   v_fases
           );

EXCEPTION
    WHEN OTHERS THEN
        RETURN jsonb_build_object(
                'exito',   false,
                'mensaje', 'Error al obtener el cronograma activo: ' || SQLERRM
               );
END;
$function$;