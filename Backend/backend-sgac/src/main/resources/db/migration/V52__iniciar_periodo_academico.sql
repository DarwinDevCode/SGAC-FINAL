CREATE OR REPLACE FUNCTION academico.fn_iniciar_periodo_academico(
    p_id_periodo INTEGER
)
    RETURNS JSONB
    LANGUAGE plpgsql
    SECURITY DEFINER
AS $function$
DECLARE
    v_periodo        RECORD;
    v_activo_nombre  VARCHAR;
    v_activo_fin     DATE;
    v_total_fases    INTEGER;
BEGIN
    SELECT
        id_periodo_academico,
        nombre_periodo,
        fecha_inicio,
        fecha_fin,
        estado,
        activo
    INTO v_periodo
    FROM academico.periodo_academico
    WHERE id_periodo_academico = p_id_periodo;

    IF NOT FOUND THEN
        RETURN jsonb_build_object(
                'exito',   false,
                'mensaje', 'No se encontró el período académico con ID ' || p_id_periodo
               );
    END IF;

    IF v_periodo.estado <> 'CONFIGURADO' THEN
        RETURN jsonb_build_object(
                'exito',   false,
                'mensaje', 'El período "' || v_periodo.nombre_periodo || '" tiene estado "'
                               || v_periodo.estado || '". Solo los períodos en estado CONFIGURADO '
                    || 'pueden iniciarse. Configure el cronograma antes de iniciar.'
               );
    END IF;

    SELECT nombre_periodo, fecha_fin
    INTO   v_activo_nombre, v_activo_fin
    FROM   academico.periodo_academico
    WHERE  activo = TRUE
      AND  id_periodo_academico <> p_id_periodo
      AND  CURRENT_DATE <= fecha_fin
    LIMIT 1;

    IF FOUND THEN
        RETURN jsonb_build_object(
                'exito',   false,
                'mensaje', 'Ya existe el período activo "' || v_activo_nombre
                               || '" que finaliza el ' || to_char(v_activo_fin, 'DD/MM/YYYY')
                    || '. No se puede iniciar un nuevo período hasta que el actual concluya.'
               );
    END IF;

    SELECT COUNT(*)
    INTO   v_total_fases
    FROM   planificacion.periodo_fase
    WHERE  id_periodo_academico = p_id_periodo;

    IF v_total_fases = 0 THEN
        RETURN jsonb_build_object(
                'exito',   false,
                'mensaje', 'El período no tiene fases en el cronograma. '
                    || 'Configure el cronograma antes de iniciar el período.'
               );
    END IF;

    UPDATE academico.periodo_academico
    SET    activo = FALSE
    WHERE  activo = TRUE
      AND  id_periodo_academico <> p_id_periodo;

    UPDATE academico.periodo_academico
    SET
        estado = 'EN PROCESO',
        activo = TRUE
    WHERE id_periodo_academico = p_id_periodo;

    RETURN jsonb_build_object(
            'exito',   true,
            'mensaje', 'El período académico "' || v_periodo.nombre_periodo
                || '" ha sido iniciado exitosamente. Estado: EN PROCESO.',
            'id',      p_id_periodo
           );

EXCEPTION
    WHEN OTHERS THEN
        RETURN jsonb_build_object(
                'exito',   false,
                'mensaje', 'Error al iniciar el período académico: ' || SQLERRM
               );
END;
$function$;