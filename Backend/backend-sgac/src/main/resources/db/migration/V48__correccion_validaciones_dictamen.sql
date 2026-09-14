-- ============================================================
-- V48 — Corrección de validaciones en dictamen de postulación
-- ============================================================
-- 1. Validar que no se pueda dictaminar una postulación ya finalizada
-- 2. Añadir campo estado_codigo para documentos en detalle
-- ============================================================

-- ============================================================
-- Corregir fn_dictaminar_postulacion para validar estado actual
-- ============================================================
CREATE OR REPLACE FUNCTION postulacion.fn_dictaminar_postulacion(
    p_id_usuario INTEGER,
    p_id_postulacion INTEGER,
    p_accion VARCHAR(20),
    p_observacion TEXT DEFAULT NULL
)
RETURNS JSONB AS $$
DECLARE
    v_id_coordinador INTEGER;
    v_id_carrera INTEGER;
    v_postulacion_carrera INTEGER;
    v_id_estudiante INTEGER;
    v_id_usuario_estudiante INTEGER;
    v_todos_validados BOOLEAN;
    v_id_nuevo_estado INTEGER;
    v_estado_codigo VARCHAR;
    v_estado_actual_codigo VARCHAR;
BEGIN
    -- 1. Validar coordinador activo
    SELECT c.id_coordinador, c.id_carrera
    INTO v_id_coordinador, v_id_carrera
    FROM academico.coordinador c
    WHERE c.id_usuario = p_id_usuario AND c.activo = TRUE;

    IF v_id_coordinador IS NULL THEN
        RETURN jsonb_build_object('exito', FALSE, 'mensaje', 'Acceso denegado: El usuario no es un coordinador activo');
    END IF;

    -- 2. Obtener información de postulación Y ESTADO ACTUAL
    SELECT car.id_carrera, p.id_estudiante, e.id_usuario, tep.codigo
    INTO v_postulacion_carrera, v_id_estudiante, v_id_usuario_estudiante, v_estado_actual_codigo
    FROM postulacion.postulacion p
    JOIN convocatoria.convocatoria cv ON p.id_convocatoria = cv.id_convocatoria
    JOIN academico.asignatura a ON cv.id_asignatura = a.id_asignatura
    JOIN academico.carrera car ON a.id_carrera = car.id_carrera
    JOIN academico.estudiante e ON p.id_estudiante = e.id_estudiante
    LEFT JOIN postulacion.tipo_estado_postulacion tep ON p.id_tipo_estado_postulacion = tep.id_tipo_estado_postulacion
    WHERE p.id_postulacion = p_id_postulacion;

    IF v_postulacion_carrera IS NULL THEN
        RETURN jsonb_build_object('exito', FALSE, 'mensaje', 'La postulación no existe');
    END IF;

    IF v_postulacion_carrera != v_id_carrera THEN
        RETURN jsonb_build_object('exito', FALSE, 'mensaje', 'No tiene permisos para dictaminar esta postulación');
    END IF;

    -- 3. NUEVA VALIDACIÓN: Verificar que la postulación no esté ya finalizada
    IF UPPER(COALESCE(v_estado_actual_codigo, '')) IN ('APROBADA', 'RECHAZADA') THEN
        RETURN jsonb_build_object(
            'exito', FALSE,
            'mensaje', 'Esta postulación ya fue ' || v_estado_actual_codigo || ' y no se pueden realizar más acciones.',
            'estado_actual', v_estado_actual_codigo
        );
    END IF;

    -- 4. Lógica según la acción
    CASE UPPER(p_accion)
        WHEN 'APROBAR' THEN
            SELECT NOT EXISTS (
                SELECT 1 FROM postulacion.requisito_adjunto ra
                JOIN convocatoria.tipo_estado_requisito ter ON ra.id_tipo_estado_requisito = ter.id_tipo_estado_requisito
                WHERE ra.id_postulacion = p_id_postulacion
                  AND UPPER(ter.codigo) NOT IN ('APROBADO', 'VALIDADO')
            ) INTO v_todos_validados;

            IF NOT v_todos_validados THEN
                RETURN jsonb_build_object(
                    'exito', FALSE,
                    'mensaje', 'No se puede aprobar: El 100% de los documentos deben estar APROBADOS o VALIDADOS.'
                );
            END IF;

            v_estado_codigo := 'APROBADA';

        WHEN 'RECHAZAR' THEN
            IF p_observacion IS NULL OR TRIM(p_observacion) = '' THEN
                RETURN jsonb_build_object('exito', FALSE, 'mensaje', 'Debe proporcionar un motivo para el rechazo');
            END IF;
            v_estado_codigo := 'RECHAZADA';

        ELSE
            RETURN jsonb_build_object('exito', FALSE, 'mensaje', 'Acción no válida. Use APROBAR o RECHAZAR');
    END CASE;

    -- 5. Obtener ID del estado del catálogo
    SELECT id_tipo_estado_postulacion INTO v_id_nuevo_estado
    FROM postulacion.tipo_estado_postulacion
    WHERE UPPER(codigo) = v_estado_codigo;

    IF v_id_nuevo_estado IS NULL THEN
        RETURN jsonb_build_object('exito', FALSE, 'mensaje', 'Error de configuración: Estado de postulación no encontrado');
    END IF;

    -- 6. Actualizar postulación
    UPDATE postulacion.postulacion
    SET id_tipo_estado_postulacion = v_id_nuevo_estado,
        observaciones = COALESCE(p_observacion, observaciones)
    WHERE id_postulacion = p_id_postulacion;

    -- 7. Notificar al estudiante
    IF v_id_usuario_estudiante IS NOT NULL THEN
        INSERT INTO notificacion.notificacion_ws (id_usuario, titulo, mensaje, tipo, id_referencia, fecha_creacion, leido)
        VALUES (
            v_id_usuario_estudiante,
            CASE WHEN v_estado_codigo = 'APROBADA' THEN 'Postulación Aprobada' ELSE 'Postulación Rechazada' END,
            CASE WHEN v_estado_codigo = 'APROBADA'
                THEN '¡Felicitaciones! Tu postulación ha sido aprobada. Pronto recibirás información sobre los siguientes pasos.'
                ELSE 'Tu postulación ha sido rechazada. Motivo: ' || p_observacion
            END,
            CASE WHEN v_estado_codigo = 'APROBADA' THEN 'APROBACION' ELSE 'RECHAZO' END,
            p_id_postulacion,
            NOW(),
            FALSE
        );
    END IF;

    RETURN jsonb_build_object(
        'exito', TRUE,
        'mensaje', 'Dictamen registrado como ' || v_estado_codigo,
        'id_postulacion', p_id_postulacion,
        'nuevo_estado', v_estado_codigo
    );

EXCEPTION WHEN OTHERS THEN
    RETURN jsonb_build_object('exito', FALSE, 'mensaje', 'ERROR SISTEMA [' || SQLSTATE || ']: ' || SQLERRM);
END;
$$ LANGUAGE plpgsql;


-- ============================================================
-- Corregir fn_obtener_detalle_postulacion_coordinador para incluir estado_codigo en documentos
-- ============================================================
CREATE OR REPLACE FUNCTION postulacion.fn_obtener_detalle_postulacion_coordinador(
    p_id_usuario INTEGER,
    p_id_postulacion INTEGER
)
RETURNS JSONB AS $$
DECLARE
    v_id_coordinador INTEGER;
    v_id_carrera INTEGER;
    v_resultado JSONB;
    v_postulacion_carrera INTEGER;
BEGIN
    -- 1. Validar coordinador activo
    SELECT c.id_coordinador, c.id_carrera
    INTO v_id_coordinador, v_id_carrera
    FROM academico.coordinador c
    WHERE c.id_usuario = p_id_usuario AND c.activo = TRUE;

    IF v_id_coordinador IS NULL THEN
        RAISE EXCEPTION 'AVISO: El usuario no tiene rol de coordinador activo asignado';
    END IF;

    -- 2. Validar pertenencia
    SELECT a.id_carrera INTO v_postulacion_carrera
    FROM postulacion.postulacion p
    JOIN convocatoria.convocatoria cv ON p.id_convocatoria = cv.id_convocatoria
    JOIN academico.asignatura a ON cv.id_asignatura = a.id_asignatura
    WHERE p.id_postulacion = p_id_postulacion;

    IF v_postulacion_carrera IS NULL THEN
        RAISE EXCEPTION 'AVISO: La postulación no existe';
    END IF;

    IF v_postulacion_carrera != v_id_carrera THEN
        RAISE EXCEPTION 'AVISO: No tiene permisos para ver esta postulación';
    END IF;

    -- 3. Construir respuesta JSON con estado_codigo para documentos
    WITH info_calendario AS (
        SELECT
            pf.id_periodo_academico,
            MIN(CASE WHEN tf.codigo = 'PUBLICACION_OFERTA' THEN pf.fecha_inicio END) as f_publicacion,
            MAX(CASE WHEN tf.codigo = 'POSTULACION' THEN pf.fecha_fin END) as f_cierre
        FROM planificacion.periodo_fase pf
        JOIN planificacion.tipo_fase tf ON pf.id_tipo_fase = tf.id_tipo_fase
        GROUP BY pf.id_periodo_academico
    ),
    conteo_docs AS (
        SELECT
            ra.id_postulacion,
            COUNT(*) as total,
            COUNT(*) FILTER (WHERE UPPER(ter.codigo) = 'PENDIENTE') as pendientes,
            COUNT(*) FILTER (WHERE UPPER(ter.codigo) IN ('APROBADO', 'VALIDADO')) as aprobados,
            COUNT(*) FILTER (WHERE UPPER(ter.codigo) = 'OBSERVADO') as observados,
            COUNT(*) FILTER (WHERE UPPER(ter.codigo) = 'RECHAZADO') as rechazados,
            COUNT(*) FILTER (WHERE UPPER(ter.codigo) = 'CORREGIDO') as corregidos
        FROM postulacion.requisito_adjunto ra
        JOIN convocatoria.tipo_estado_requisito ter ON ra.id_tipo_estado_requisito = ter.id_tipo_estado_requisito
        WHERE ra.id_postulacion = p_id_postulacion
        GROUP BY ra.id_postulacion
    )
    SELECT jsonb_build_object(
        'postulacion', jsonb_build_object(
            'id_postulacion', p.id_postulacion,
            'fecha_postulacion', p.fecha_postulacion,
            'estado_codigo', tep.codigo,
            'estado_nombre', tep.nombre,
            'observaciones', COALESCE(p.observaciones, '')
        ),
        'estudiante', jsonb_build_object(
            'id_estudiante', e.id_estudiante,
            'nombre_completo', u.nombres || ' ' || u.apellidos,
            'email', u.correo,
            'matricula', e.matricula,
            'semestre', e.semestre,
            'estado_academico', e.estado_academico
        ),
        'convocatoria', jsonb_build_object(
            'id_convocatoria', cv.id_convocatoria,
            'asignatura', a.nombre_asignatura,
            'docente', ud.nombres || ' ' || ud.apellidos,
            'fecha_publicacion', cal.f_publicacion,
            'fecha_cierre', cal.f_cierre,
            'cupos_disponibles', cv.cupos_disponibles
        ),
        'documentos', (
            SELECT COALESCE(jsonb_agg(
                jsonb_build_object(
                    'id_requisito_adjunto', ra.id_requisito_adjunto,
                    'tipo_requisito', trp.nombre_requisito,
                    'nombre_archivo', ra.nombre_archivo,
                    'fecha_subida', ra.fecha_subida,
                    -- CORREGIDO: Incluir ambos campos
                    'estado_codigo', UPPER(ter.codigo),
                    'estado_nombre', ter.codigo,
                    'observacion', ra.observacion,
                    'tiene_archivo', (ra.archivo IS NOT NULL)
                )
            ), '[]'::jsonb)
            FROM postulacion.requisito_adjunto ra
            JOIN convocatoria.tipo_requisito_postulacion trp ON ra.id_tipo_requisito_postulacion = trp.id_tipo_requisito_postulacion
            JOIN convocatoria.tipo_estado_requisito ter ON ra.id_tipo_estado_requisito = ter.id_tipo_estado_requisito
            WHERE ra.id_postulacion = p.id_postulacion
        ),
        'resumen_documentos', jsonb_build_object(
            'total', COALESCE(cd.total, 0),
            'pendientes', COALESCE(cd.pendientes, 0),
            'aprobados', COALESCE(cd.aprobados, 0),
            'observados', COALESCE(cd.observados, 0),
            'rechazados', COALESCE(cd.rechazados, 0),
            'corregidos', COALESCE(cd.corregidos, 0)
        ),
        'puede_aprobar', COALESCE(cd.total > 0 AND cd.total = cd.aprobados, FALSE)
    ) INTO v_resultado
    FROM postulacion.postulacion p
    JOIN academico.estudiante e ON p.id_estudiante = e.id_estudiante
    JOIN seguridad.usuario u ON e.id_usuario = u.id_usuario
    JOIN convocatoria.convocatoria cv ON p.id_convocatoria = cv.id_convocatoria
    JOIN academico.asignatura a ON cv.id_asignatura = a.id_asignatura
    JOIN academico.docente d ON cv.id_docente = d.id_docente
    JOIN seguridad.usuario ud ON d.id_usuario = ud.id_usuario
    JOIN info_calendario cal ON cal.id_periodo_academico = cv.id_periodo_academico
    LEFT JOIN postulacion.tipo_estado_postulacion tep ON p.id_tipo_estado_postulacion = tep.id_tipo_estado_postulacion
    LEFT JOIN conteo_docs cd ON cd.id_postulacion = p.id_postulacion
    WHERE p.id_postulacion = p_id_postulacion;

    RETURN v_resultado;

EXCEPTION WHEN OTHERS THEN
    RAISE EXCEPTION 'ERROR SISTEMA [%]: %', SQLSTATE, SQLERRM;
END;
$$ LANGUAGE plpgsql;


-- ============================================================
-- Permisos
-- ============================================================
GRANT EXECUTE ON FUNCTION postulacion.fn_dictaminar_postulacion TO role_ayudante_catedra;
GRANT EXECUTE ON FUNCTION postulacion.fn_obtener_detalle_postulacion_coordinador TO role_ayudante_catedra;

