CREATE OR REPLACE FUNCTION academico.fn_abrir_periodo_academico(
    p_nombre       VARCHAR,
    p_fecha_inicio DATE,
    p_fecha_fin    DATE
)
    RETURNS JSONB
    LANGUAGE plpgsql
    SECURITY DEFINER
AS $function$
DECLARE
    v_periodo_activo_id     INTEGER;
    v_periodo_activo_fin    DATE;
    v_periodo_activo_nombre VARCHAR;
    v_id_periodo            INTEGER;
    v_fase                  RECORD;
    v_total_fases           INTEGER;
    v_dia_placeholder       DATE;
    v_idx                   INTEGER := 0;
BEGIN
    IF p_fecha_fin <= p_fecha_inicio THEN
        RETURN jsonb_build_object(
                'exito',   false,
                'mensaje', 'La fecha de fin debe ser posterior a la fecha de inicio del período'
               );
    END IF;

    IF TRIM(p_nombre) = '' OR p_nombre IS NULL THEN
        RETURN jsonb_build_object(
                'exito',   false,
                'mensaje', 'El nombre del período académico no puede estar vacío'
               );
    END IF;

    SELECT
        id_periodo_academico,
        fecha_fin,
        nombre_periodo
    INTO
        v_periodo_activo_id,
        v_periodo_activo_fin,
        v_periodo_activo_nombre
    FROM academico.periodo_academico
    WHERE activo = TRUE
    LIMIT 1;

    IF FOUND THEN
        IF CURRENT_DATE <= v_periodo_activo_fin THEN
            RETURN jsonb_build_object(
                    'exito',   false,
                    'mensaje', 'Ya existe el período académico activo "' || v_periodo_activo_nombre
                                   || '" (ID: ' || v_periodo_activo_id || ') que finaliza el '
                                   || to_char(v_periodo_activo_fin, 'DD/MM/YYYY')
                        || '. No es posible abrir un nuevo período hasta que el actual haya concluido.'
                   );
        END IF;
    END IF;

    SELECT COUNT(*)
    INTO   v_total_fases
    FROM   planificacion.tipo_fase
    WHERE  activo = TRUE;

    IF v_total_fases = 0 THEN
        RETURN jsonb_build_object(
                'exito',   false,
                'mensaje', 'No existen tipos de fase activos en planificacion.tipo_fase. '
                    || 'Registre al menos una fase antes de abrir un período académico.'
               );
    END IF;

    IF (p_fecha_fin - p_fecha_inicio) < (v_total_fases - 1) THEN
        RETURN jsonb_build_object(
                'exito',   false,
                'mensaje', 'El rango del período (' || (p_fecha_fin - p_fecha_inicio + 1)
                               || ' día(s)) es insuficiente para instanciar ' || v_total_fases
                               || ' fases con fechas placeholder no solapadas. '
                               || 'Se requieren al menos ' || v_total_fases || ' días.'
               );
    END IF;

    INSERT INTO academico.periodo_academico (
        nombre_periodo,
        fecha_inicio,
        fecha_fin,
        estado,
        activo
    )
    VALUES (
               TRIM(p_nombre),
               p_fecha_inicio,
               p_fecha_fin,
               'PLANIFICACION',
               FALSE
           )
    RETURNING id_periodo_academico INTO v_id_periodo;


    FOR v_fase IN
        SELECT id_tipo_fase, orden, nombre
        FROM   planificacion.tipo_fase
        WHERE  activo = TRUE
        ORDER BY orden ASC
        LOOP
            v_dia_placeholder := p_fecha_inicio + v_idx;

            INSERT INTO planificacion.periodo_fase (
                id_periodo_academico,
                id_tipo_fase,
                fecha_inicio,
                fecha_fin
            )
            VALUES (
                       v_id_periodo,
                       v_fase.id_tipo_fase,
                       v_dia_placeholder,
                       v_dia_placeholder
                   );

            v_idx := v_idx + 1;  -- Avanzar al siguiente día disponible
        END LOOP;

    RETURN jsonb_build_object(
            'exito',   true,
            'mensaje', 'Período académico "' || TRIM(p_nombre) || '" abierto exitosamente '
                           || 'con ' || v_total_fases || ' fase(s) instanciadas en estado PLANIFICACION. '
                || 'Use fn_ajustar_cronograma_lote() para configurar las fechas definitivas.',
            'id',      v_id_periodo
           );

EXCEPTION
    WHEN OTHERS THEN
        RETURN jsonb_build_object(
                'exito',   false,
                'mensaje', 'Error al abrir el período académico: ' || SQLERRM
               );
END;
$function$;


CREATE OR REPLACE FUNCTION planificacion.fn_ajustar_cronograma_lote(
    p_id_periodo  INTEGER,
    p_fases_json  JSONB
)
    RETURNS JSONB
    LANGUAGE plpgsql
    SECURITY DEFINER
AS $function$
DECLARE
    v_periodo               RECORD;
    v_fase                  RECORD;
    v_total_fases_json      INTEGER;
    v_total_fases_sistema   INTEGER;

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


    IF v_periodo.estado NOT IN ('PLANIFICACION', 'CONFIGURADO') THEN
        RETURN jsonb_build_object(
                'exito',   false,
                'mensaje', 'El período "' || v_periodo.nombre_periodo
                               || '" tiene estado "' || v_periodo.estado
                               || '" y no admite ajustes de cronograma. '
                    || 'Solo los períodos en estado PLANIFICACION o CONFIGURADO pueden modificarse.'
               );
    END IF;

    IF p_fases_json IS NULL OR jsonb_typeof(p_fases_json) != 'array' THEN
        RETURN jsonb_build_object(
                'exito',   false,
                'mensaje', 'El parámetro p_fases_json debe ser un arreglo JSON válido'
               );
    END IF;

    v_total_fases_json := jsonb_array_length(p_fases_json);

    IF v_total_fases_json = 0 THEN
        RETURN jsonb_build_object(
                'exito',   false,
                'mensaje', 'El arreglo de fases no puede estar vacío. '
                    || 'Debe incluir todas las fases del cronograma.'
               );
    END IF;

    SELECT COUNT(*) INTO v_total_fases_sistema
    FROM planificacion.tipo_fase
    WHERE activo = TRUE;

    IF v_total_fases_json != v_total_fases_sistema THEN
        RETURN jsonb_build_object(
                'exito',   false,
                'mensaje', 'Se enviaron ' || v_total_fases_json || ' fase(s) pero el sistema '
                               || 'tiene ' || v_total_fases_sistema || ' fase(s) activa(s). '
                    || 'El cronograma debe incluir exactamente todas las fases activas.'
               );
    END IF;

    IF EXISTS (
        SELECT 1
        FROM   jsonb_array_elements(p_fases_json) AS elem
        WHERE  NOT EXISTS (
            SELECT 1
            FROM   planificacion.tipo_fase tf
            WHERE  tf.id_tipo_fase = (elem->>'id_tipo_fase')::INTEGER
              AND  tf.activo = TRUE
        )
    ) THEN
        RETURN jsonb_build_object(
                'exito',   false,
                'mensaje', 'El arreglo contiene id_tipo_fase inválidos o pertenecientes a fases inactivas. '
                    || 'Verifique los identificadores enviados.'
               );
    END IF;

    DELETE FROM planificacion.periodo_fase
    WHERE  id_periodo_academico = p_id_periodo;

    FOR v_fase IN
        SELECT
            (elem->>'id_tipo_fase')::INTEGER  AS id_tipo_fase,
            (elem->>'fecha_inicio')::DATE     AS fecha_inicio,
            (elem->>'fecha_fin')::DATE        AS fecha_fin,
            tf.orden                           AS orden
        FROM   jsonb_array_elements(p_fases_json) AS elem
                   JOIN   planificacion.tipo_fase tf
                          ON tf.id_tipo_fase = (elem->>'id_tipo_fase')::INTEGER
        ORDER BY tf.orden ASC
        LOOP
            INSERT INTO planificacion.periodo_fase (
                id_periodo_academico,
                id_tipo_fase,
                fecha_inicio,
                fecha_fin
            )
            VALUES (
                       p_id_periodo,
                       v_fase.id_tipo_fase,
                       v_fase.fecha_inicio,
                       v_fase.fecha_fin
                   );
        END LOOP;

    UPDATE academico.periodo_academico
    SET    estado = 'CONFIGURADO'
    WHERE  id_periodo_academico = p_id_periodo;

    RETURN jsonb_build_object(
            'exito',   true,
            'mensaje', 'Cronograma del período "' || v_periodo.nombre_periodo
                           || '" ajustado exitosamente con ' || v_total_fases_json
                || ' fase(s). Estado actualizado a CONFIGURADO.',
            'id',      p_id_periodo
           );

EXCEPTION
    WHEN OTHERS THEN
        RETURN jsonb_build_object(
                'exito',   false,
                'mensaje', SQLERRM
               );
END;
$function$;