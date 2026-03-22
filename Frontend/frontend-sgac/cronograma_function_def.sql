CREATE OR REPLACE FUNCTION postulacion.fn_consultar_cronograma_oposicion(p_id_convocatoria integer)
 RETURNS jsonb
 LANGUAGE plpgsql
 STABLE SECURITY DEFINER
AS $function$
BEGIN
    RETURN jsonb_build_object(
            'exito', true,
            'cronograma', COALESCE(
                    (SELECT jsonb_agg(
                                    jsonb_build_object(
                                            'idEvaluacionOposicion', eo.id_evaluacion_oposicion,
                                            'orden',                 eo.orden_exposicion,
                                            'nombres',               u.nombres,
                                            'apellidos',             u.apellidos,
                                            'correo',                u.correo,
                                            'tema',                  eo.tema_exposicion,
                                            'fecha',                 to_char(eo.fecha_evaluacion, 'YYYY-MM-DD'),
                                            'horaInicio',            to_char(eo.hora_inicio,      'HH24:MI'),
                                            'horaFin',               to_char(eo.hora_fin,         'HH24:MI'),
                                            'horaInicioReal',        to_char(eo.hora_inicio_real, 'HH24:MI:SS'),
                                            'horaFinReal',           to_char(eo.hora_fin_real,    'HH24:MI'),
                                            'lugar',                 eo.lugar,
                                            'estado',                tee.codigo,
                                            'nombreEstado',          tee.nombre,
                                            'puntajeFinal',          eo.puntaje_total_oposicion,
                                        -- ← nuevo: ISO-8601 UTC del momento en que empezó este turno
                                            'serverTimestamp',       CASE WHEN tee.codigo = 'EN_CURSO'
                                                                              THEN to_char(
                                                (eo.fecha_evaluacion + eo.hora_inicio_real) AT TIME ZONE 'America/Guayaquil' AT TIME ZONE 'UTC',
                                                'YYYY-MM-DD"T"HH24:MI:SS"Z"'
                                                                                   )
                                                                          ELSE NULL END,
                                            'jurados', (
                                                SELECT COALESCE(jsonb_agg(jsonb_build_object(
                                                                                  'idUsuario',           uj.id_usuario,
                                                                                  'nombres',             uj2.nombres,
                                                                                  'apellidos',           uj2.apellidos,
                                                                                  'rol',                 uj.rol_integrante,
                                                                                  'puntajeMaterial',     uj.puntaje_material,
                                                                                  'puntajeExposicion',   uj.puntaje_exposicion,
                                                                                  'puntajeRespuestas',   uj.puntaje_respuestas,
                                                                                  'subtotal',            COALESCE(uj.puntaje_material, 0)
                                                                                      + COALESCE(uj.puntaje_exposicion, 0)
                                                                                      + COALESCE(uj.puntaje_respuestas, 0),
                                                                                  'finalizo',            uj.finalizo_calificacion
                                                                          ) ORDER BY uj.rol_integrante), '[]'::jsonb)
                                                FROM   seguridad.usuario_comision uj
                                                           JOIN   seguridad.usuario          uj2 ON uj2.id_usuario = uj.id_usuario
                                                WHERE  uj.id_evaluacion_oposicion = eo.id_evaluacion_oposicion
                                                   OR (uj.id_evaluacion_oposicion IS NULL
                                                    AND uj.id_comision_seleccion = (
                                                        SELECT cs.id_comision_seleccion
                                                        FROM   postulacion.comision_seleccion cs
                                                        WHERE  cs.id_convocatoria = p_id_convocatoria
                                                          AND  cs.activo = TRUE
                                                        LIMIT 1
                                                    ))
                                            )
                                    ) ORDER BY eo.orden_exposicion
                            )
                     FROM  postulacion.evaluacion_oposicion   eo
                               JOIN  postulacion.postulacion              p   ON p.id_postulacion   = eo.id_postulacion
                               JOIN  academico.estudiante                 est ON est.id_estudiante  = p.id_estudiante
                               JOIN  seguridad.usuario                    u   ON u.id_usuario       = est.id_usuario
                               JOIN  postulacion.tipo_estado_evaluacion   tee ON tee.id_tipo_estado_evaluacion = eo.id_tipo_estado_evaluacion
                     WHERE p.id_convocatoria = p_id_convocatoria
                    ),
                    '[]'::jsonb
                          )
           );

EXCEPTION WHEN OTHERS THEN
    RETURN jsonb_build_object('exito', false, 'mensaje', '[ERROR] ' || SQLERRM);
END;
$function$
