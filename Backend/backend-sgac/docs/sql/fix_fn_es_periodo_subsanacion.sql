-- =====================================================
-- SCRIPT PARA CORREGIR LA FUNCIÓN fn_es_periodo_subsanacion
-- Y LA FUNCIÓN sp_notificar_coordinador_subsanacion
-- Ejecutar este script directamente en PostgreSQL
-- =====================================================

-- CORRECCIÓN 1: Permite subsanar documentos durante:
-- 1. La etapa de Postulación (convocatoria abierta)
-- 2. La etapa de Revisión (7 días después del cierre)

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
'Verifica si la fecha actual permite subsanación de documentos: durante la etapa de Postulación (convocatoria abierta) o durante la etapa de Revisión';


-- =====================================================
-- CORRECCIÓN 2: Función de notificación al coordinador
-- El problema era que usaba id_coordinador en lugar de id_usuario
-- =====================================================

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
    v_id_usuario_coordinador INTEGER;
    v_id_carrera INTEGER;
    v_mensaje TEXT;
    v_id_notificacion INTEGER;
BEGIN
    -- Obtener el id_usuario del coordinador (NO el id_coordinador)
    SELECT co.id_usuario, ca.id_carrera
    INTO v_id_usuario_coordinador, v_id_carrera
    FROM postulacion.postulacion p
    INNER JOIN convocatoria.convocatoria c ON p.id_convocatoria = c.id_convocatoria
    INNER JOIN academico.asignatura a ON c.id_asignatura = a.id_asignatura
    INNER JOIN academico.carrera ca ON a.id_carrera = ca.id_carrera
    INNER JOIN academico.coordinador co ON co.id_carrera = ca.id_carrera AND co.activo = TRUE
    WHERE p.id_postulacion = p_id_postulacion
    LIMIT 1;

    IF v_id_usuario_coordinador IS NULL THEN
        -- Intentar obtener coordinador activo de la carrera
        SELECT co.id_usuario INTO v_id_usuario_coordinador
        FROM academico.coordinador co
        WHERE co.id_carrera = v_id_carrera
        AND co.activo = TRUE
        LIMIT 1;
    END IF;

    IF v_id_usuario_coordinador IS NULL THEN
        RETURN NULL; -- No hay coordinador asignado
    END IF;

    -- Crear mensaje de notificación
    v_mensaje := 'El estudiante ' || p_nombre_estudiante ||
                 ' ha corregido el documento "' || p_nombre_requisito ||
                 '". Pendiente de nueva revisión.';

    -- Insertar notificación (usando id_usuario, no id_coordinador)
    INSERT INTO notificacion.notificacion_ws (
        id_usuario,
        titulo,
        mensaje,
        tipo,
        id_referencia,
        leido,
        fecha_creacion
    ) VALUES (
        v_id_usuario_coordinador,
        'Documento Subsanado',
        v_mensaje,
        'SUBSANACION_DOCUMENTO',
        p_id_postulacion,
        FALSE,
        NOW()
    ) RETURNING id_notificacion INTO v_id_notificacion;

    RETURN v_id_usuario_coordinador;
END;
$$;

COMMENT ON FUNCTION postulacion.sp_notificar_coordinador_subsanacion IS
'Crea una notificación para el coordinador cuando un estudiante subsana un documento observado';

-- Verificar que las funciones fueron creadas correctamente
-- SELECT convocatoria.fn_es_periodo_subsanacion(1);
