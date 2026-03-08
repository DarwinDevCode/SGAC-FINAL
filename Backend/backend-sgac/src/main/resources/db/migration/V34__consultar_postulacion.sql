CREATE OR REPLACE FUNCTION convocatoria.fn_calcular_cronograma_convocatoria(
    p_fecha_publicacion DATE,
    p_fecha_cierre DATE
)
    RETURNS JSONB
    LANGUAGE plpgsql
    SECURITY DEFINER
AS $$
DECLARE
    v_fecha_revision_inicio DATE;
    v_fecha_revision_fin DATE;
    v_fecha_resultados DATE;
    v_cronograma JSONB;
BEGIN
    IF p_fecha_publicacion IS NULL OR p_fecha_cierre IS NULL THEN
        RETURN jsonb_build_object(
                'exito', FALSE,
                'mensaje', 'Las fechas de publicación y cierre son requeridas'
               );
    END IF;

    v_fecha_revision_inicio := p_fecha_cierre + INTERVAL '1 day';
    v_fecha_revision_fin := p_fecha_cierre + INTERVAL '7 days';

    v_fecha_resultados := v_fecha_revision_fin + INTERVAL '1 day';

    v_cronograma := jsonb_build_object(
            'exito', TRUE,
            'etapas', jsonb_build_array(
                    jsonb_build_object(
                            'numero', 1,
                            'nombre', 'Postulación',
                            'descripcion', 'Período para enviar postulación y documentos',
                            'fecha_inicio', p_fecha_publicacion,
                            'fecha_fin', p_fecha_cierre,
                            'estado', CASE
                                          WHEN CURRENT_DATE < p_fecha_publicacion THEN 'PENDIENTE'
                                          WHEN CURRENT_DATE BETWEEN p_fecha_publicacion AND p_fecha_cierre THEN 'EN_CURSO'
                                          ELSE 'COMPLETADA'
                                END
                    ),
                    jsonb_build_object(
                            'numero', 2,
                            'nombre', 'Revisión',
                            'descripcion', 'Revisión de documentos por el coordinador',
                            'fecha_inicio', v_fecha_revision_inicio,
                            'fecha_fin', v_fecha_revision_fin,
                            'estado', CASE
                                          WHEN CURRENT_DATE < v_fecha_revision_inicio THEN 'PENDIENTE'
                                          WHEN CURRENT_DATE BETWEEN v_fecha_revision_inicio AND v_fecha_revision_fin THEN 'EN_CURSO'
                                          ELSE 'COMPLETADA'
                                END
                    ),
                    jsonb_build_object(
                            'numero', 3,
                            'nombre', 'Resultados',
                            'descripcion', 'Publicación de resultados finales',
                            'fecha_inicio', v_fecha_resultados,
                            'fecha_fin', v_fecha_resultados,
                            'estado', CASE
                                          WHEN CURRENT_DATE < v_fecha_resultados THEN 'PENDIENTE'
                                          WHEN CURRENT_DATE = v_fecha_resultados THEN 'EN_CURSO'
                                          ELSE 'COMPLETADA'
                                END
                    )
                      )
                    );

    RETURN v_cronograma;
END;
$$;

-- ==================================================================================

CREATE OR REPLACE FUNCTION postulacion.fn_obtener_documentos_postulacion(
    p_id_postulacion INTEGER
)
    RETURNS JSONB
    LANGUAGE plpgsql
    SECURITY DEFINER
AS $$
DECLARE
    v_documentos JSONB;
    v_count INTEGER;
BEGIN
    IF NOT EXISTS (SELECT 1 FROM postulacion.postulacion WHERE id_postulacion = p_id_postulacion) THEN
        RETURN jsonb_build_object(
                'exito', FALSE,
                'mensaje', 'La postulación no existe'
               );
    END IF;

    SELECT jsonb_agg(
                   jsonb_build_object(
                           'id_requisito_adjunto', ra.id_requisito_adjunto,
                           'id_tipo_requisito', trp.id_tipo_requisito_postulacion,
                           'nombre_requisito', trp.nombre_requisito,
                           'descripcion_requisito', trp.descripcion,
                           'tipo_documento_permitido', trp.tipo_documento_permitido,
                           'nombre_archivo', ra.nombre_archivo,
                           'fecha_subida', ra.fecha_subida,
                           'estado', ter.nombre_estado,
                           'id_tipo_estado_requisito', ter.id_tipo_estado_requisito,
                           'observacion', COALESCE(ra.observacion, ''),
                           'es_editable', (ter.nombre_estado = 'OBSERVADO'),
                           'tiene_archivo', (ra.archivo IS NOT NULL)
                   )
                   ORDER BY trp.id_tipo_requisito_postulacion
           )
    INTO v_documentos
    FROM postulacion.requisito_adjunto ra
             INNER JOIN convocatoria.tipo_requisito_postulacion trp
                        ON ra.id_tipo_requisito_postulacion = trp.id_tipo_requisito_postulacion
             INNER JOIN convocatoria.tipo_estado_requisito ter
                        ON ra.id_tipo_estado_requisito = ter.id_tipo_estado_requisito
    WHERE ra.id_postulacion = p_id_postulacion;

    SELECT COUNT(*) INTO v_count
    FROM postulacion.requisito_adjunto
    WHERE id_postulacion = p_id_postulacion;

    RETURN jsonb_build_object(
            'exito', TRUE,
            'total_documentos', v_count,
            'documentos', COALESCE(v_documentos, '[]'::jsonb)
           );
END;
$$;

-- ==================================================================================

CREATE OR REPLACE FUNCTION postulacion.fn_ver_detalle_postulacion(
    p_id_usuario INTEGER
)
    RETURNS JSONB
    LANGUAGE plpgsql
    SECURITY DEFINER
AS $$
DECLARE
    v_id_estudiante INTEGER;
    v_es_valido BOOLEAN;
    v_mensaje TEXT;
    v_postulacion RECORD;
    v_cronograma JSONB;
    v_documentos JSONB;
    v_resultado JSONB;
BEGIN
    SELECT * INTO v_id_estudiante, v_es_valido, v_mensaje
    FROM seguridad.fn_validar_contexto_estudiante(p_id_usuario);

    IF NOT v_es_valido THEN
        RAISE EXCEPTION '%', v_mensaje;
    END IF;

    SELECT
        p.id_postulacion,
        p.fecha_postulacion,
        p.estado_postulacion,
        p.observaciones,
        c.id_convocatoria,
        c.fecha_publicacion,
        c.fecha_cierre,
        c.cupos_disponibles,
        c.estado AS estado_convocatoria,
        a.nombre_asignatura,
        a.semestre AS semestre_asignatura,
        ca.nombre_carrera,
        CONCAT(u.nombres, ' ', u.apellidos) AS nombre_docente
    INTO v_postulacion
    FROM postulacion.postulacion p
             INNER JOIN convocatoria.convocatoria c ON p.id_convocatoria = c.id_convocatoria
             INNER JOIN academico.asignatura a ON c.id_asignatura = a.id_asignatura
             INNER JOIN academico.carrera ca ON a.id_carrera = ca.id_carrera
             INNER JOIN academico.docente d ON c.id_docente = d.id_docente
             INNER JOIN seguridad.usuario u ON d.id_usuario = u.id_usuario
    WHERE p.id_estudiante = v_id_estudiante
      AND p.activo = TRUE
      AND p.estado_postulacion NOT IN ('RECHAZADA', 'CANCELADA')
    ORDER BY p.fecha_postulacion DESC
    LIMIT 1;

    IF v_postulacion IS NULL THEN
        RETURN jsonb_build_object(
                'exito', FALSE,
                'codigo', 'SIN_POSTULACION',
                'mensaje', 'No tienes ninguna postulación activa en este momento'
               );
    END IF;

    v_cronograma := convocatoria.fn_calcular_cronograma_convocatoria(
            v_postulacion.fecha_publicacion,
            v_postulacion.fecha_cierre
                    );

    v_documentos := postulacion.fn_obtener_documentos_postulacion(v_postulacion.id_postulacion);

    v_resultado := jsonb_build_object(
            'exito', TRUE,
            'mensaje', 'Postulación encontrada exitosamente',
            'postulacion', jsonb_build_object(
                    'id_postulacion', v_postulacion.id_postulacion,
                    'fecha_postulacion', v_postulacion.fecha_postulacion,
                    'estado_postulacion', v_postulacion.estado_postulacion,
                    'observaciones', COALESCE(v_postulacion.observaciones, '')
                           ),
            'convocatoria', jsonb_build_object(
                    'id_convocatoria', v_postulacion.id_convocatoria,
                    'nombre_asignatura', v_postulacion.nombre_asignatura,
                    'semestre_asignatura', v_postulacion.semestre_asignatura,
                    'nombre_carrera', v_postulacion.nombre_carrera,
                    'nombre_docente', v_postulacion.nombre_docente,
                    'cupos_disponibles', v_postulacion.cupos_disponibles,
                    'estado_convocatoria', v_postulacion.estado_convocatoria,
                    'fecha_publicacion', v_postulacion.fecha_publicacion,
                    'fecha_cierre', v_postulacion.fecha_cierre
                            ),
            'cronograma', v_cronograma->'etapas',
            'documentos', v_documentos->'documentos',
            'total_documentos', v_documentos->'total_documentos',
            'resumen_documentos', (
                SELECT jsonb_build_object(
                               'pendientes', COUNT(*) FILTER (WHERE ter.nombre_estado = 'PENDIENTE'),
                               'aprobados', COUNT(*) FILTER (WHERE ter.nombre_estado = 'APROBADO'),
                               'observados', COUNT(*) FILTER (WHERE ter.nombre_estado = 'OBSERVADO'),
                               'rechazados', COUNT(*) FILTER (WHERE ter.nombre_estado = 'RECHAZADO')
                       )
                FROM postulacion.requisito_adjunto ra
                         INNER JOIN convocatoria.tipo_estado_requisito ter
                                    ON ra.id_tipo_estado_requisito = ter.id_tipo_estado_requisito
                WHERE ra.id_postulacion = v_postulacion.id_postulacion
            )
                   );
    RETURN v_resultado;

EXCEPTION WHEN OTHERS THEN
    RAISE EXCEPTION 'ERROR SISTEMA [%]: %', SQLSTATE, SQLERRM;
END;
$$;

