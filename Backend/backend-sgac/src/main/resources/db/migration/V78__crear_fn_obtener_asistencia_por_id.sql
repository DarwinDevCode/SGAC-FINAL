-- Bug: GET /api/ayudantias/sesiones/{idRegistro}/asistencia llama a
-- ayudantia.fn_obtener_asistencia_por_id, que nunca fue creada (el repositorio
-- Java la referencia pero la funcion no existe en la base). El endpoint
-- siempre fallaba con "bad SQL grammar".
--
-- Se crea siguiendo el mismo patron que fn_obtener_asistencia_sesion_actual
-- (V?? - AsistenciaSesionService), pero buscando la sesion por
-- id_registro_actividad exacto en lugar de "la proxima planificada", y
-- validando que esa sesion pertenezca a una ayudantia del usuario que
-- consulta (evita que un ayudante vea la asistencia de la sesion de otro
-- con solo adivinar el id).

CREATE OR REPLACE FUNCTION ayudantia.fn_obtener_asistencia_por_id(p_id_usuario integer, p_id_registro integer)
 RETURNS seguridad.res_operacion
 LANGUAGE plpgsql
AS $function$
DECLARE
    v_id_ayudantia INTEGER;
    v_sesion RECORD;
    v_puede_editar BOOLEAN := FALSE;
    v_estudiantes_json JSON;
BEGIN
    SELECT a.id_ayudantia INTO v_id_ayudantia
    FROM ayudantia.ayudantia a
             JOIN postulacion.postulacion p ON a.id_postulacion = p.id_postulacion
             JOIN academico.estudiante e ON p.id_estudiante = e.id_estudiante
    WHERE e.id_usuario = p_id_usuario
      AND a.id_tipo_estado_ayudantia = (SELECT id_tipo_estado_ayudantia FROM ayudantia.tipo_estado_ayudantia WHERE codigo = 'ACTIVO')
    LIMIT 1;

    IF v_id_ayudantia IS NULL THEN
        RETURN (FALSE, 'No posee una ayudantía activa.', NULL)::seguridad.res_operacion;
    END IF;

    SELECT * INTO v_sesion
    FROM ayudantia.registro_actividad
    WHERE id_registro_actividad = p_id_registro
      AND id_ayudantia = v_id_ayudantia;

    IF v_sesion.id_registro_actividad IS NULL THEN
        RETURN (FALSE, 'La sesión no existe o no pertenece a su ayudantía.', NULL)::seguridad.res_operacion;
    END IF;

    IF (v_sesion.fecha = (CURRENT_TIMESTAMP AT TIME ZONE 'America/Guayaquil')::DATE
        AND (CURRENT_TIMESTAMP AT TIME ZONE 'America/Guayaquil')::TIME
            BETWEEN v_sesion.hora_inicio AND v_sesion.hora_fin) THEN
        v_puede_editar := TRUE;
    END IF;

    SELECT json_agg(t) INTO v_estudiantes_json
    FROM (
             SELECT
                 da.id_detalle_asistencia_actividad as id_detalle,
                 p.nombre_completo,
                 p.curso,
                 p.paralelo,
                 da.asistio
             FROM ayudantia.detalle_asistencia_actividad da
                      JOIN ayudantia.participante_ayudantia p ON da.id_participante_ayudantia = p.id_participante_ayudantia
             WHERE da.id_registro_actividad = v_sesion.id_registro_actividad
             ORDER BY p.nombre_completo ASC
         ) t;

    RETURN (
            TRUE,
            CASE WHEN v_puede_editar THEN 'Sesión activa: Puede marcar asistencia.' ELSE 'Sesión fuera de horario: Modo lectura.' END,
            json_build_object(
                    'sesion', json_build_object(
                    'id_registro', v_sesion.id_registro_actividad,
                    'tema', v_sesion.tema_tratado,
                    'fecha', v_sesion.fecha,
                    'horario', v_sesion.hora_inicio || ' - ' || v_sesion.hora_fin,
                    'lugar', v_sesion.lugar,
                    'puede_editar', v_puede_editar
                              ),
                    'estudiantes', COALESCE(v_estudiantes_json, '[]'::json)
            )
        )::seguridad.res_operacion;

END;
$function$;
