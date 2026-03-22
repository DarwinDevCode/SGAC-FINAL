CREATE OR REPLACE FUNCTION ayudantia.fn_listar_sesiones(p_id_ayudantia integer, p_fecha_desde date, p_fecha_hasta date, p_estado_codigo character varying)
 RETURNS TABLE(id_registro_actividad integer, fecha date, hora_inicio time without time zone, hora_fin time without time zone, horas_dedicadas numeric, tema_tratado text, descripcion_actividad text, lugar character varying, codigo_estado character varying, nombre_estado character varying, observaciones character varying, fecha_observacion date)
 LANGUAGE plpgsql
AS $function$
BEGIN
    RETURN QUERY
        SELECT
            ra.id_registro_actividad,
            ra.fecha,
            ra.hora_inicio,
            ra.hora_fin,
            ra.horas_dedicadas,
            ra.tema_tratado,
            ra.descripcion_actividad,
            ra.lugar,
            ter.codigo AS codigo_estado,
            ter.nombre_estado,
            ra.observaciones, -- <--- SELECCIONADO SIN ALIAS
            ra.fecha_observacion
        FROM ayudantia.registro_actividad ra
                 JOIN ayudantia.tipo_estado_registro ter ON ra.id_tipo_estado_registro = ter.id_tipo_estado_registro
        WHERE ra.id_ayudantia = p_id_ayudantia
          AND (p_fecha_desde IS NULL OR ra.fecha >= p_fecha_desde)
          AND (p_fecha_hasta IS NULL OR ra.fecha <= p_fecha_hasta)
          AND (p_estado_codigo IS NULL OR p_estado_codigo = '' OR ter.codigo = p_estado_codigo)
        ORDER BY ra.fecha DESC, ra.hora_inicio DESC;
END;
$function$
