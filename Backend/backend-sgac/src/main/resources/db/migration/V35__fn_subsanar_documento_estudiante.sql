-- ============================================
-- Migración: Funciones para Subsanación de Documentos
-- Incluye validación de periodo y notificación al coordinador
-- ============================================

-- ============================================
-- FUNCIÓN A: Validación de Periodo de Subsanación
-- ============================================
CREATE OR REPLACE FUNCTION convocatoria.fn_es_periodo_subsanacion(
    p_id_convocatoria INTEGER
)
    RETURNS BOOLEAN
    LANGUAGE plpgsql
    SECURITY DEFINER
AS $$
DECLARE
    v_fecha_publicacion DATE;
    v_fecha_cierre DATE;
    v_fecha_revision_inicio DATE;
    v_fecha_revision_fin DATE;
BEGIN
    -- Obtener fechas de la convocatoria
    SELECT fecha_publicacion, fecha_cierre INTO v_fecha_publicacion, v_fecha_cierre
    FROM convocatoria.convocatoria
    WHERE id_convocatoria = p_id_convocatoria;

    IF v_fecha_cierre IS NULL THEN
        RETURN FALSE;
    END IF;

    -- Calcular ventana de revisión (fecha_cierre + 1 hasta fecha_cierre + 7)
    v_fecha_revision_inicio := v_fecha_cierre + INTERVAL '1 day';
    v_fecha_revision_fin := v_fecha_cierre + INTERVAL '7 days';

    -- Permitir subsanación si:
    -- 1. Estamos en la etapa de Postulación (convocatoria abierta) O
    -- 2. Estamos en la etapa de Revisión
    RETURN (CURRENT_DATE BETWEEN v_fecha_publicacion AND v_fecha_cierre)
        OR (CURRENT_DATE BETWEEN v_fecha_revision_inicio AND v_fecha_revision_fin);
END;
$$;

COMMENT ON FUNCTION convocatoria.fn_es_periodo_subsanacion IS
'Verifica si la fecha actual está dentro del periodo de subsanación (revisión) de una convocatoria';


CREATE OR REPLACE FUNCTION postulacion.sp_notificar_coordinador_subsanacion(
    p_id_postulacion INTEGER,
    p_id_requisito INTEGER,
    p_nombre_estudiante TEXT,
    p_nombre_requisito TEXT
)
RETURNS INTEGER
LANGUAGE plpgsql
SECURITY DEFINER
AS $$
DECLARE
    v_id_coordinador INTEGER;
    v_id_carrera INTEGER;
    v_mensaje TEXT;
    v_id_notificacion INTEGER;
BEGIN
    SELECT co.id_coordinador, ca.id_carrera
    INTO v_id_coordinador, v_id_carrera
    FROM postulacion.postulacion p
    INNER JOIN convocatoria.convocatoria c ON p.id_convocatoria = c.id_convocatoria
    INNER JOIN academico.asignatura a ON c.id_asignatura = a.id_asignatura
    INNER JOIN academico.carrera ca ON a.id_carrera = ca.id_carrera
    INNER JOIN academico.coordinador co on co.id_carrera = ca.id_carrera
    WHERE p.id_postulacion = p_id_postulacion;

    IF v_id_coordinador IS NULL THEN
        -- Intentar obtener coordinador de la carrera
        SELECT co.id_usuario INTO v_id_coordinador
        FROM academico.coordinador co
        WHERE co.id_carrera = v_id_carrera
        AND co.activo = TRUE
        LIMIT 1;
    END IF;

    IF v_id_coordinador IS NULL THEN
        RETURN NULL; -- No hay coordinador asignado
    END IF;

    -- Crear mensaje de notificación
    v_mensaje := 'El estudiante ' || p_nombre_estudiante ||
                 ' ha corregido el documento "' || p_nombre_requisito ||
                 '". Pendiente de nueva revisión.';

    -- Insertar notificación
    INSERT INTO notificacion.notificacion_ws (
        id_usuario,
        titulo,
        mensaje,
        tipo,
        id_referencia,
        leido,
        fecha_creacion
    ) VALUES (
        v_id_coordinador,
        'Documento Subsanado',
        v_mensaje,
        'SUBSANACION_DOCUMENTO',
        p_id_postulacion,
        FALSE,
        NOW()
    ) RETURNING id_notificacion INTO v_id_notificacion;

    RETURN v_id_coordinador;
END;
$$;

COMMENT ON FUNCTION postulacion.sp_notificar_coordinador_subsanacion IS
'Crea una notificación para el coordinador cuando un estudiante subsana un documento observado';


-- ============================================
-- FUNCIÓN C: Subsanar Documento (Orquestador Principal)
-- ============================================
CREATE OR REPLACE FUNCTION postulacion.fn_subsanar_documento_estudiante(
    p_id_usuario INTEGER,
    p_id_requisito_adjunto INTEGER,
    p_archivo BYTEA,
    p_nombre_archivo VARCHAR(150)
)
RETURNS JSONB
LANGUAGE plpgsql
SECURITY DEFINER
AS $$
DECLARE
    v_id_estudiante INTEGER;
    v_es_valido BOOLEAN;
    v_mensaje TEXT;
    v_requisito RECORD;
    v_id_estado_corregido INTEGER;
    v_id_coordinador INTEGER;
    v_nombre_estudiante TEXT;
    v_nombre_requisito TEXT;
    v_es_periodo_subsanacion BOOLEAN;
BEGIN
    -- 1. Validar contexto del estudiante
    SELECT p_id_estudiante, p_es_valido, p_mensaje
    INTO v_id_estudiante, v_es_valido, v_mensaje
    FROM seguridad.fn_validar_contexto_estudiante(p_id_usuario);

    IF NOT v_es_valido THEN
        RETURN jsonb_build_object(
            'exito', FALSE,
            'codigo', 'NO_ES_ESTUDIANTE',
            'mensaje', v_mensaje
        );
    END IF;

    -- 2. Obtener información del requisito adjunto
    SELECT
        ra.id_requisito_adjunto,
        ra.id_postulacion,
        ter.nombre_estado AS estado_actual,
        trp.nombre_requisito,
        p.id_estudiante AS estudiante_postulacion,
        c.id_convocatoria
    INTO v_requisito
    FROM postulacion.requisito_adjunto ra
    INNER JOIN convocatoria.tipo_estado_requisito ter
        ON ra.id_tipo_estado_requisito = ter.id_tipo_estado_requisito
    INNER JOIN convocatoria.tipo_requisito_postulacion trp
        ON ra.id_tipo_requisito_postulacion = trp.id_tipo_requisito_postulacion
    INNER JOIN postulacion.postulacion p
        ON ra.id_postulacion = p.id_postulacion
    INNER JOIN convocatoria.convocatoria c
        ON p.id_convocatoria = c.id_convocatoria
    WHERE ra.id_requisito_adjunto = p_id_requisito_adjunto;

    -- 3. Validar que el requisito existe
    IF v_requisito IS NULL THEN
        RETURN jsonb_build_object(
            'exito', FALSE,
            'codigo', 'REQUISITO_NO_EXISTE',
            'mensaje', 'El documento no existe'
        );
    END IF;

    -- 4. Validar que el documento pertenece al estudiante
    IF v_requisito.estudiante_postulacion != v_id_estudiante THEN
        RETURN jsonb_build_object(
            'exito', FALSE,
            'codigo', 'SIN_PERMISO',
            'mensaje', 'No tienes permiso para modificar este documento'
        );
    END IF;

    -- 5. Validar que el estado sea OBSERVADO
    IF v_requisito.estado_actual != 'OBSERVADO' THEN
        RETURN jsonb_build_object(
            'exito', FALSE,
            'codigo', 'DOC_NO_OBSERVADO',
            'mensaje', 'Solo puedes reemplazar documentos con estado OBSERVADO. Estado actual: ' || v_requisito.estado_actual
        );
    END IF;

    -- 6. Validar periodo de subsanación
    v_es_periodo_subsanacion := convocatoria.fn_es_periodo_subsanacion(v_requisito.id_convocatoria);

    IF NOT v_es_periodo_subsanacion THEN
        RETURN jsonb_build_object(
            'exito', FALSE,
            'codigo', 'FUERA_PERIODO',
            'mensaje', 'El periodo de subsanación ha finalizado. Solo puedes corregir documentos durante la etapa de revisión.'
        );
    END IF;

    -- 7. Obtener ID del estado CORREGIDO
    SELECT id_tipo_estado_requisito INTO v_id_estado_corregido
    FROM convocatoria.tipo_estado_requisito
    WHERE nombre_estado = 'CORREGIDO'
    LIMIT 1;

    -- Si no existe CORREGIDO, usar PENDIENTE
    IF v_id_estado_corregido IS NULL THEN
        SELECT id_tipo_estado_requisito INTO v_id_estado_corregido
        FROM convocatoria.tipo_estado_requisito
        WHERE nombre_estado = 'PENDIENTE'
        LIMIT 1;
    END IF;

    -- 8. Obtener nombre del estudiante para la notificación
    SELECT CONCAT(u.nombres, ' ', u.apellidos) INTO v_nombre_estudiante
    FROM seguridad.usuario u
    WHERE u.id_usuario = p_id_usuario;

    v_nombre_requisito := v_requisito.nombre_requisito;

    -- 9. Actualizar el documento
    UPDATE postulacion.requisito_adjunto
    SET archivo = p_archivo,
        nombre_archivo = p_nombre_archivo,
        fecha_subida = CURRENT_DATE,
        id_tipo_estado_requisito = v_id_estado_corregido,
        observacion = NULL
    WHERE id_requisito_adjunto = p_id_requisito_adjunto;

    -- 10. Notificar al coordinador (y obtener su ID para WebSocket)
    v_id_coordinador := postulacion.sp_notificar_coordinador_subsanacion(
        v_requisito.id_postulacion,
        p_id_requisito_adjunto,
        v_nombre_estudiante,
        v_nombre_requisito
    );

    -- 11. Retornar respuesta exitosa con datos para WebSocket
    RETURN jsonb_build_object(
        'exito', TRUE,
        'codigo', 'OK',
        'mensaje', 'Documento subsanado correctamente. El coordinador ha sido notificado para una nueva revisión.',
        'id_requisito_adjunto', p_id_requisito_adjunto,
        'nuevo_estado', 'CORREGIDO',
        'notificacion_enviada', (v_id_coordinador IS NOT NULL),
        'id_coordinador', COALESCE(v_id_coordinador, 0),
        'id_postulacion', v_requisito.id_postulacion,
        'nombre_estudiante', v_nombre_estudiante,
        'nombre_requisito', v_nombre_requisito
    );

EXCEPTION WHEN OTHERS THEN
    RETURN jsonb_build_object(
        'exito', FALSE,
        'codigo', 'ERROR_SISTEMA',
        'mensaje', 'ERROR SISTEMA [' || SQLSTATE || ']: ' || SQLERRM
    );
END;
$$;

COMMENT ON FUNCTION postulacion.fn_subsanar_documento_estudiante IS
'Permite a un estudiante subsanar un documento observado durante el periodo de revisión.
Valida estado y fechas, actualiza el documento y notifica al coordinador.';


-- ============================================
-- Otorgar permisos
-- ============================================
GRANT EXECUTE ON FUNCTION convocatoria.fn_es_periodo_subsanacion TO role_ayudante_catedra;
GRANT EXECUTE ON FUNCTION postulacion.sp_notificar_coordinador_subsanacion TO role_ayudante_catedra;
GRANT EXECUTE ON FUNCTION postulacion.fn_subsanar_documento_estudiante TO role_ayudante_catedra;

