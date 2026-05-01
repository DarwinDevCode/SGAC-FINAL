-- ============================================================
-- V47 — Refactorización Sistema de Evaluación con Ventana de 24h
-- ============================================================
-- Incluye:
-- 1. DDL: Añadir fecha_observacion a requisito_adjunto
-- 2. fn_es_periodo_subsanacion refactorizada (calendario académico)
-- 3. fn_evaluar_documento_individual con estados irreversibles
-- 4. fn_dictaminar_postulacion con validación de periodo activo
-- 5. fn_subsanar_documento_estudiante con lógica 24h
-- 6. Outputs actualizados con fecha_limite_subsanacion
-- ============================================================

-- ============================================================
-- TAREA 1: DDL - Añadir columna fecha_observacion
-- ============================================================

-- ============================================================
-- TAREA 2: fn_es_periodo_subsanacion (Validación de Periodo Activo)
-- ============================================================
-- Ahora valida:
-- 1. Que el periodo académico esté ACTIVO o EN PROCESO
-- 2. Que la fecha actual esté entre POSTULACION y EVALUACION_REQUISITOS
-- ============================================================
CREATE OR REPLACE FUNCTION convocatoria.fn_es_periodo_subsanacion(
    p_id_convocatoria INTEGER
)
RETURNS BOOLEAN
LANGUAGE plpgsql
SECURITY DEFINER
AS $$
DECLARE
    v_id_periodo INTEGER;
    v_estado_periodo VARCHAR;
    v_fecha_inicio_postulacion DATE;
    v_fecha_fin_evaluacion DATE;
BEGIN
    -- 1. Obtener el periodo académico y su estado
    SELECT c.id_periodo_academico, UPPER(pa.estado)
    INTO v_id_periodo, v_estado_periodo
    FROM convocatoria.convocatoria c
    JOIN academico.periodo_academico pa ON c.id_periodo_academico = pa.id_periodo_academico
    WHERE c.id_convocatoria = p_id_convocatoria;

    IF v_id_periodo IS NULL THEN
        RETURN FALSE;
    END IF;

    -- 2. Validar que el periodo esté ACTIVO o EN PROCESO
    IF v_estado_periodo NOT IN ('ACTIVO', 'EN PROCESO', 'EN_PROCESO') THEN
        RETURN FALSE;
    END IF;

    -- 3. Obtener fechas de las fases POSTULACION y EVALUACION_REQUISITOS
    SELECT
        MIN(CASE WHEN tf.codigo = 'POSTULACION' THEN pf.fecha_inicio END),
        MAX(CASE WHEN tf.codigo = 'EVALUACION_REQUISITOS' THEN pf.fecha_fin END)
    INTO
        v_fecha_inicio_postulacion,
        v_fecha_fin_evaluacion
    FROM planificacion.periodo_fase pf
    JOIN planificacion.tipo_fase tf ON pf.id_tipo_fase = tf.id_tipo_fase
    WHERE pf.id_periodo_academico = v_id_periodo
      AND tf.codigo IN ('POSTULACION', 'EVALUACION_REQUISITOS');

    -- 4. Validar si hoy está dentro del rango de subsanación
    RETURN (CURRENT_DATE BETWEEN v_fecha_inicio_postulacion AND v_fecha_fin_evaluacion);

EXCEPTION WHEN OTHERS THEN
    RETURN FALSE;
END;
$$;

COMMENT ON FUNCTION convocatoria.fn_es_periodo_subsanacion IS
    'Verifica si la fecha actual permite subsanación: periodo ACTIVO/EN PROCESO y dentro de POSTULACION hasta EVALUACION_REQUISITOS.';


-- ============================================================
-- TAREA 2: fn_evaluar_documento_individual (Estados Irreversibles + 24h)
-- ============================================================
CREATE OR REPLACE FUNCTION postulacion.fn_evaluar_documento_individual(
    p_id_usuario INTEGER,
    p_id_requisito_adjunto INTEGER,
    p_accion VARCHAR(20),
    p_observacion TEXT DEFAULT NULL
)
RETURNS JSONB AS $$
DECLARE
    v_id_coordinador INTEGER;
    v_id_carrera INTEGER;
    v_id_postulacion INTEGER;
    v_id_convocatoria INTEGER;
    v_postulacion_carrera INTEGER;
    v_id_nuevo_estado INTEGER;
    v_nombre_estado_req VARCHAR;
    v_estado_actual VARCHAR;
    v_id_usuario_estudiante INTEGER;
    v_id_estado_postulacion_observada INTEGER;
    v_tiene_observados BOOLEAN;
    v_todos_validados BOOLEAN;
    v_es_periodo_activo BOOLEAN;
BEGIN
    -- 1. Validar coordinador
    SELECT c.id_coordinador, c.id_carrera
    INTO v_id_coordinador, v_id_carrera
    FROM academico.coordinador c
    WHERE c.id_usuario = p_id_usuario AND c.activo = TRUE;

    IF v_id_coordinador IS NULL THEN
        RETURN jsonb_build_object('exito', FALSE, 'mensaje', 'El usuario no es un coordinador activo');
    END IF;

    -- 2. Obtener información del documento y estado actual
    SELECT ra.id_postulacion, car.id_carrera, e.id_usuario,
           UPPER(ter.codigo), cv.id_convocatoria
    INTO v_id_postulacion, v_postulacion_carrera, v_id_usuario_estudiante,
         v_estado_actual, v_id_convocatoria
    FROM postulacion.requisito_adjunto ra
    JOIN postulacion.postulacion p ON ra.id_postulacion = p.id_postulacion
    JOIN convocatoria.convocatoria cv ON p.id_convocatoria = cv.id_convocatoria
    JOIN academico.asignatura a ON cv.id_asignatura = a.id_asignatura
    JOIN academico.carrera car ON a.id_carrera = car.id_carrera
    JOIN academico.estudiante e ON p.id_estudiante = e.id_estudiante
    JOIN convocatoria.tipo_estado_requisito ter ON ra.id_tipo_estado_requisito = ter.id_tipo_estado_requisito
    WHERE ra.id_requisito_adjunto = p_id_requisito_adjunto;

    IF v_id_postulacion IS NULL THEN
        RETURN jsonb_build_object('exito', FALSE, 'mensaje', 'Documento no encontrado');
    END IF;

    IF v_postulacion_carrera != v_id_carrera THEN
        RETURN jsonb_build_object('exito', FALSE, 'mensaje', 'No tiene permisos sobre esta carrera');
    END IF;

    -- 3. VALIDAR PERIODO ACTIVO (Disponibilidad del servicio)
    v_es_periodo_activo := convocatoria.fn_es_periodo_subsanacion(v_id_convocatoria);
    IF NOT v_es_periodo_activo THEN
        RETURN jsonb_build_object(
            'exito', FALSE,
            'mensaje', 'El servicio de evaluación no está disponible. El periodo académico debe estar activo y en fase de evaluación de requisitos.'
        );
    END IF;

    -- 4. ESTADOS FINALES IRREVERSIBLES
    -- RECHAZADO es definitivo: no puede cambiar
    IF v_estado_actual = 'RECHAZADO' THEN
        RETURN jsonb_build_object(
            'exito', FALSE,
            'mensaje', 'Este documento ya fue RECHAZADO y su estado es definitivo. No se permite modificación.'
        );
    END IF;

    -- APROBADO/VALIDADO no puede ser observado ni rechazado posteriormente
    IF v_estado_actual IN ('APROBADO', 'VALIDADO') AND UPPER(p_accion) IN ('OBSERVAR', 'RECHAZAR') THEN
        RETURN jsonb_build_object(
            'exito', FALSE,
            'mensaje', 'Este documento ya fue APROBADO. No se permite observar o rechazar documentos aprobados.'
        );
    END IF;

    -- 5. Procesar acción
    CASE UPPER(p_accion)
        WHEN 'VALIDAR' THEN
            SELECT id_tipo_estado_requisito, nombre_estado INTO v_id_nuevo_estado, v_nombre_estado_req
            FROM convocatoria.tipo_estado_requisito
            WHERE UPPER(codigo) IN ('APROBADO', 'VALIDADO') AND activo = TRUE LIMIT 1;

        WHEN 'OBSERVAR' THEN
            -- Validar que no esté ya aprobado
            IF v_estado_actual IN ('APROBADO', 'VALIDADO') THEN
                RETURN jsonb_build_object('exito', FALSE, 'mensaje', 'No se puede observar un documento aprobado');
            END IF;

            IF p_observacion IS NULL OR TRIM(p_observacion) = '' THEN
                RETURN jsonb_build_object('exito', FALSE, 'mensaje', 'Debe indicar una observación');
            END IF;

            SELECT id_tipo_estado_requisito, nombre_estado INTO v_id_nuevo_estado, v_nombre_estado_req
            FROM convocatoria.tipo_estado_requisito
            WHERE UPPER(codigo) = 'OBSERVADO' AND activo = TRUE LIMIT 1;

        WHEN 'RECHAZAR' THEN
            SELECT id_tipo_estado_requisito, nombre_estado INTO v_id_nuevo_estado, v_nombre_estado_req
            FROM convocatoria.tipo_estado_requisito
            WHERE UPPER(codigo) = 'RECHAZADO' AND activo = TRUE LIMIT 1;

        ELSE
            RETURN jsonb_build_object('exito', FALSE, 'mensaje', 'Acción no permitida. Use: VALIDAR, OBSERVAR o RECHAZAR');
    END CASE;

    -- 6. Actualizar el Requisito Adjunto
    UPDATE postulacion.requisito_adjunto
    SET id_tipo_estado_requisito = v_id_nuevo_estado,
        observacion = CASE WHEN UPPER(p_accion) = 'OBSERVAR' THEN p_observacion ELSE observacion END,
        -- NUEVO: Registrar timestamp de observación para la ventana de 24h
        fecha_observacion = CASE WHEN UPPER(p_accion) = 'OBSERVAR' THEN CURRENT_TIMESTAMP ELSE NULL END
    WHERE id_requisito_adjunto = p_id_requisito_adjunto;

    -- 7. Si es OBSERVAR, actualizar estado de postulación y notificar
    IF UPPER(p_accion) = 'OBSERVAR' THEN
        SELECT id_tipo_estado_postulacion INTO v_id_estado_postulacion_observada
        FROM postulacion.tipo_estado_postulacion WHERE codigo = 'OBSERVADA';

        IF v_id_estado_postulacion_observada IS NOT NULL THEN
            UPDATE postulacion.postulacion
            SET id_tipo_estado_postulacion = v_id_estado_postulacion_observada
            WHERE id_postulacion = v_id_postulacion;
        END IF;

        -- Notificar al estudiante con fecha límite de 24h
        INSERT INTO notificacion.notificacion_ws (id_usuario, titulo, mensaje, tipo, id_referencia, fecha_creacion, leido)
        VALUES (
            v_id_usuario_estudiante,
            'Documento Observado - Acción Requerida',
            'Un documento de tu postulación ha sido observado. Tienes 24 horas para corregirlo y subir un nuevo archivo.',
            'OBSERVACION',
            v_id_postulacion,
            NOW(),
            FALSE
        );
    END IF;

    -- 8. Calcular estados actuales
    SELECT EXISTS (
        SELECT 1 FROM postulacion.requisito_adjunto ra
        JOIN convocatoria.tipo_estado_requisito ter ON ra.id_tipo_estado_requisito = ter.id_tipo_estado_requisito
        WHERE ra.id_postulacion = v_id_postulacion AND UPPER(ter.codigo) = 'OBSERVADO'
    ) INTO v_tiene_observados;

    SELECT NOT EXISTS (
        SELECT 1 FROM postulacion.requisito_adjunto ra
        JOIN convocatoria.tipo_estado_requisito ter ON ra.id_tipo_estado_requisito = ter.id_tipo_estado_requisito
        WHERE ra.id_postulacion = v_id_postulacion
          AND UPPER(ter.codigo) NOT IN ('APROBADO', 'VALIDADO')
    ) INTO v_todos_validados;

    RETURN jsonb_build_object(
        'exito', TRUE,
        'mensaje', 'Evaluación registrada correctamente',
        'nuevo_estado_documento', v_nombre_estado_req,
        'tiene_observados', v_tiene_observados,
        'todos_validados', v_todos_validados,
        -- NUEVO: Incluir fecha límite si fue observado
        'fecha_limite_subsanacion', CASE
            WHEN UPPER(p_accion) = 'OBSERVAR' THEN (CURRENT_TIMESTAMP + INTERVAL '24 hours')::TEXT
            ELSE NULL
        END
    );

EXCEPTION WHEN OTHERS THEN
    RETURN jsonb_build_object('exito', FALSE, 'mensaje', 'ERROR SISTEMA [' || SQLSTATE || ']: ' || SQLERRM);
END;
$$ LANGUAGE plpgsql;


-- ============================================================
-- TAREA 2: fn_dictaminar_postulacion (Validación Periodo + Cierre)
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
    v_id_convocatoria INTEGER;
    v_postulacion_carrera INTEGER;
    v_id_estudiante INTEGER;
    v_id_usuario_estudiante INTEGER;
    v_todos_validados BOOLEAN;
    v_id_nuevo_estado INTEGER;
    v_estado_codigo VARCHAR;
    v_es_periodo_activo BOOLEAN;
BEGIN
    -- 1. Validar coordinador activo
    SELECT c.id_coordinador, c.id_carrera
    INTO v_id_coordinador, v_id_carrera
    FROM academico.coordinador c
    WHERE c.id_usuario = p_id_usuario AND c.activo = TRUE;

    IF v_id_coordinador IS NULL THEN
        RETURN jsonb_build_object('exito', FALSE, 'mensaje', 'Acceso denegado: El usuario no es un coordinador activo');
    END IF;

    -- 2. Obtener información de postulación
    SELECT car.id_carrera, p.id_estudiante, e.id_usuario, cv.id_convocatoria
    INTO v_postulacion_carrera, v_id_estudiante, v_id_usuario_estudiante, v_id_convocatoria
    FROM postulacion.postulacion p
    JOIN convocatoria.convocatoria cv ON p.id_convocatoria = cv.id_convocatoria
    JOIN academico.asignatura a ON cv.id_asignatura = a.id_asignatura
    JOIN academico.carrera car ON a.id_carrera = car.id_carrera
    JOIN academico.estudiante e ON p.id_estudiante = e.id_estudiante
    WHERE p.id_postulacion = p_id_postulacion;

    IF v_postulacion_carrera IS NULL THEN
        RETURN jsonb_build_object('exito', FALSE, 'mensaje', 'La postulación no existe');
    END IF;

    IF v_postulacion_carrera != v_id_carrera THEN
        RETURN jsonb_build_object('exito', FALSE, 'mensaje', 'No tiene permisos para dictaminar esta postulación');
    END IF;

    -- 3. VALIDAR PERIODO ACTIVO
    v_es_periodo_activo := convocatoria.fn_es_periodo_subsanacion(v_id_convocatoria);
    IF NOT v_es_periodo_activo THEN
        RETURN jsonb_build_object(
            'exito', FALSE,
            'mensaje', 'No se puede dictaminar. El periodo académico debe estar activo y en fase de evaluación.'
        );
    END IF;

    -- 4. Lógica según la acción
    CASE UPPER(p_accion)
        WHEN 'APROBAR' THEN
            -- Verificar que TODOS los documentos estén APROBADOS o VALIDADOS
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
-- TAREA 2: fn_subsanar_documento_estudiante (Lógica 24 Horas)
-- ============================================================
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
    v_fecha_observacion TIMESTAMP;
    v_fecha_limite TIMESTAMP;
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

    -- 2. Obtener información del requisito adjunto incluyendo fecha_observacion
    SELECT
        ra.id_requisito_adjunto,
        ra.id_postulacion,
        ter.codigo AS estado_actual,
        trp.nombre_requisito,
        p.id_estudiante AS estudiante_postulacion,
        c.id_convocatoria,
        ra.fecha_observacion
    INTO v_requisito
    FROM postulacion.requisito_adjunto ra
    INNER JOIN convocatoria.tipo_estado_requisito ter ON ra.id_tipo_estado_requisito = ter.id_tipo_estado_requisito
    INNER JOIN convocatoria.tipo_requisito_postulacion trp ON ra.id_tipo_requisito_postulacion = trp.id_tipo_requisito_postulacion
    INNER JOIN postulacion.postulacion p ON ra.id_postulacion = p.id_postulacion
    INNER JOIN convocatoria.convocatoria c ON p.id_convocatoria = c.id_convocatoria
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
    IF UPPER(v_requisito.estado_actual) != 'OBSERVADO' THEN
        RETURN jsonb_build_object(
            'exito', FALSE,
            'codigo', 'DOC_NO_OBSERVADO',
            'mensaje', 'Solo puedes reemplazar documentos con estado OBSERVADO. Estado actual: ' || v_requisito.estado_actual
        );
    END IF;

    -- 6. NUEVA VALIDACIÓN: Ventana de 24 horas
    v_fecha_observacion := v_requisito.fecha_observacion;

    IF v_fecha_observacion IS NOT NULL THEN
        v_fecha_limite := v_fecha_observacion + INTERVAL '24 hours';

        IF CURRENT_TIMESTAMP > v_fecha_limite THEN
            -- El plazo expiró: Actualizar automáticamente a RECHAZADO
            UPDATE postulacion.requisito_adjunto
            SET id_tipo_estado_requisito = (
                SELECT id_tipo_estado_requisito
                FROM convocatoria.tipo_estado_requisito
                WHERE UPPER(codigo) = 'RECHAZADO'
                LIMIT 1
            )
            WHERE id_requisito_adjunto = p_id_requisito_adjunto;

            RETURN jsonb_build_object(
                'exito', FALSE,
                'codigo', 'PLAZO_EXPIRADO',
                'mensaje', 'El plazo de 24 horas para subsanar este documento ha expirado. El documento ha sido marcado como RECHAZADO automáticamente.',
                'fecha_observacion', v_fecha_observacion::TEXT,
                'fecha_limite', v_fecha_limite::TEXT
            );
        END IF;
    END IF;

    -- 7. Validar periodo de subsanación (fases del calendario)
    v_es_periodo_subsanacion := convocatoria.fn_es_periodo_subsanacion(v_requisito.id_convocatoria);

    IF NOT v_es_periodo_subsanacion THEN
        RETURN jsonb_build_object(
            'exito', FALSE,
            'codigo', 'FUERA_PERIODO',
            'mensaje', 'El periodo de subsanación ha finalizado según el cronograma académico. Solo puedes corregir documentos durante las fases de postulación o evaluación de requisitos.'
        );
    END IF;

    -- 8. Obtener ID del estado CORREGIDO
    SELECT id_tipo_estado_requisito INTO v_id_estado_corregido
    FROM convocatoria.tipo_estado_requisito
    WHERE UPPER(codigo) = 'CORREGIDO'
    LIMIT 1;

    IF v_id_estado_corregido IS NULL THEN
        SELECT id_tipo_estado_requisito INTO v_id_estado_corregido
        FROM convocatoria.tipo_estado_requisito
        WHERE UPPER(codigo) = 'PENDIENTE'
        LIMIT 1;
    END IF;

    -- 9. Obtener nombre del estudiante
    SELECT CONCAT(u.nombres, ' ', u.apellidos) INTO v_nombre_estudiante
    FROM seguridad.usuario u
    WHERE u.id_usuario = p_id_usuario;

    v_nombre_requisito := v_requisito.nombre_requisito;

    -- 10. Actualizar el documento (limpiar fecha_observacion)
    UPDATE postulacion.requisito_adjunto
    SET archivo = p_archivo,
        nombre_archivo = p_nombre_archivo,
        fecha_subida = CURRENT_DATE,
        id_tipo_estado_requisito = v_id_estado_corregido,
        observacion = NULL,
        fecha_observacion = NULL  -- Limpiar al corregir
    WHERE id_requisito_adjunto = p_id_requisito_adjunto;

    -- 11. Actualizar estado de postulación a CORREGIDA
    UPDATE postulacion.postulacion
    SET id_tipo_estado_postulacion = (
        SELECT id_tipo_estado_postulacion
        FROM postulacion.tipo_estado_postulacion
        WHERE codigo = 'CORREGIDA'
        LIMIT 1
    )
    WHERE id_postulacion = v_requisito.id_postulacion;

    -- 12. Notificar al coordinador
    v_id_coordinador := postulacion.sp_notificar_coordinador_subsanacion(
        v_requisito.id_postulacion,
        p_id_requisito_adjunto,
        v_nombre_estudiante,
        v_nombre_requisito
    );

    RETURN jsonb_build_object(
        'exito', TRUE,
        'codigo', 'OK',
        'mensaje', 'Documento subsanado correctamente dentro del plazo de 24 horas. El coordinador ha sido notificado.',
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


-- ============================================================
-- TAREA 3: fn_obtener_detalle_postulacion_coordinador (Output con fecha_limite)
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

    -- 3. Construir respuesta JSON con fecha_limite_subsanacion
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
                    'estado_nombre', ter.nombre_estado,
                    'observacion', ra.observacion,
                    'tiene_archivo', (ra.archivo IS NOT NULL),
                    -- NUEVO: Campos para gestión de 24h
                    'fecha_observacion', ra.fecha_observacion,
                    'fecha_limite_subsanacion', CASE
                        WHEN UPPER(ter.codigo) = 'OBSERVADO' AND ra.fecha_observacion IS NOT NULL
                        THEN (ra.fecha_observacion + INTERVAL '24 hours')::TEXT
                        ELSE NULL
                    END,
                    'plazo_expirado', CASE
                        WHEN UPPER(ter.codigo) = 'OBSERVADO' AND ra.fecha_observacion IS NOT NULL
                        THEN (CURRENT_TIMESTAMP > (ra.fecha_observacion + INTERVAL '24 hours'))
                        ELSE FALSE
                    END
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
        'puede_aprobar', COALESCE(cd.total > 0 AND cd.total = cd.aprobados, FALSE),
        -- NUEVO: Indicador de servicio activo
        'servicio_evaluacion_activo', convocatoria.fn_es_periodo_subsanacion(cv.id_convocatoria)
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
-- TAREA 3: Actualizar fn_ver_detalle_postulacion (Vista Estudiante)
-- ============================================================
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
    v_resumen JSONB;
    v_resultado JSONB;
BEGIN
    -- 1. Validar contexto del estudiante
    SELECT p_id_estudiante, p_es_valido, p_mensaje
    INTO v_id_estudiante, v_es_valido, v_mensaje
    FROM seguridad.fn_validar_contexto_estudiante(p_id_usuario);

    IF NOT v_es_valido THEN
        RETURN jsonb_build_object(
            'exito', FALSE,
            'mensaje', v_mensaje
        );
    END IF;

    -- 2. Obtener postulación activa (PENDIENTE, EN_REVISION, OBSERVADA, CORREGIDA) o RECHAZADA
    SELECT
        p.id_postulacion,
        p.fecha_postulacion,
        tep.codigo AS estado_codigo,
        tep.nombre AS estado_nombre,
        p.observaciones,
        cv.id_convocatoria,
        a.nombre_asignatura,
        a.semestre AS semestre_asignatura,
        car.nombre_carrera,
        (ud.nombres || ' ' || ud.apellidos) AS nombre_docente,
        cv.cupos_disponibles,
        cv.id_periodo_academico
    INTO v_postulacion
    FROM postulacion.postulacion p
    JOIN convocatoria.convocatoria cv ON p.id_convocatoria = cv.id_convocatoria
    JOIN academico.asignatura a ON cv.id_asignatura = a.id_asignatura
    JOIN academico.carrera car ON a.id_carrera = car.id_carrera
    JOIN academico.docente d ON cv.id_docente = d.id_docente
    JOIN seguridad.usuario ud ON d.id_usuario = ud.id_usuario
    LEFT JOIN postulacion.tipo_estado_postulacion tep ON p.id_tipo_estado_postulacion = tep.id_tipo_estado_postulacion
    WHERE p.id_estudiante = v_id_estudiante
      AND p.activo = TRUE
      AND (tep.codigo IN ('PENDIENTE', 'EN_REVISION', 'OBSERVADA', 'CORREGIDA', 'RECHAZADA')
           OR tep.codigo IS NULL)
    ORDER BY p.fecha_postulacion DESC
    LIMIT 1;

    IF v_postulacion IS NULL THEN
        RETURN jsonb_build_object(
            'exito', FALSE,
            'mensaje', 'No tienes una postulación activa en este momento.'
        );
    END IF;

    SELECT COALESCE(jsonb_agg(
        jsonb_build_object(
            'fase', tf.nombre,
            'codigo', tf.codigo,
            'inicio', pf.fecha_inicio,
            'fin', pf.fecha_fin,
            'estado', CASE
                WHEN CURRENT_DATE < pf.fecha_inicio THEN 'PENDIENTE'
                WHEN CURRENT_DATE BETWEEN pf.fecha_inicio AND pf.fecha_fin THEN 'ACTIVA'
                ELSE 'COMPLETADA'
            END
        ) ORDER BY pf.fecha_inicio
    ), '[]'::jsonb)
    INTO v_cronograma
    FROM planificacion.periodo_fase pf
    JOIN planificacion.tipo_fase tf ON pf.id_tipo_fase = tf.id_tipo_fase
    WHERE pf.id_periodo_academico = v_postulacion.id_periodo_academico;

    -- 4. Obtener documentos con fecha_limite_subsanacion
    SELECT COALESCE(jsonb_agg(
        jsonb_build_object(
            'id_requisito_adjunto', ra.id_requisito_adjunto,
            'tipo_requisito', trp.nombre_requisito,
            'nombre_archivo', ra.nombre_archivo,
            'fecha_subida', ra.fecha_subida,
            'estado_nombre', ter.codigo,
            'observacion', ra.observacion,
            'tiene_archivo', (ra.archivo IS NOT NULL),
            'es_editable', (UPPER(ter.codigo) = 'OBSERVADO'),
            -- NUEVO: Campos para gestión de 24h
            'fecha_observacion', ra.fecha_observacion,
            'fecha_limite_subsanacion', CASE
                WHEN UPPER(ter.codigo) = 'OBSERVADO' AND ra.fecha_observacion IS NOT NULL
                THEN (ra.fecha_observacion + INTERVAL '24 hours')::TEXT
                ELSE NULL
            END,
            'tiempo_restante_segundos', CASE
                WHEN UPPER(ter.codigo) = 'OBSERVADO' AND ra.fecha_observacion IS NOT NULL
                THEN GREATEST(0, EXTRACT(EPOCH FROM ((ra.fecha_observacion + INTERVAL '24 hours') - CURRENT_TIMESTAMP))::INTEGER)
                ELSE NULL
            END,
            'plazo_expirado', CASE
                WHEN UPPER(ter.codigo) = 'OBSERVADO' AND ra.fecha_observacion IS NOT NULL
                THEN (CURRENT_TIMESTAMP > (ra.fecha_observacion + INTERVAL '24 hours'))
                ELSE FALSE
            END
        )
    ), '[]'::jsonb)
    INTO v_documentos
    FROM postulacion.requisito_adjunto ra
    JOIN convocatoria.tipo_requisito_postulacion trp ON ra.id_tipo_requisito_postulacion = trp.id_tipo_requisito_postulacion
    JOIN convocatoria.tipo_estado_requisito ter ON ra.id_tipo_estado_requisito = ter.id_tipo_estado_requisito
    WHERE ra.id_postulacion = v_postulacion.id_postulacion;

    -- 5. Calcular resumen
    SELECT jsonb_build_object(
        'pendientes', COUNT(*) FILTER (WHERE UPPER(ter.codigo) = 'PENDIENTE'),
        'aprobados', COUNT(*) FILTER (WHERE UPPER(ter.codigo) IN ('APROBADO', 'VALIDADO')),
        'observados', COUNT(*) FILTER (WHERE UPPER(ter.codigo) = 'OBSERVADO'),
        'rechazados', COUNT(*) FILTER (WHERE UPPER(ter.codigo) = 'RECHAZADO'),
        'corregidos', COUNT(*) FILTER (WHERE UPPER(ter.codigo) = 'CORREGIDO')
    )
    INTO v_resumen
    FROM postulacion.requisito_adjunto ra
    JOIN convocatoria.tipo_estado_requisito ter ON ra.id_tipo_estado_requisito = ter.id_tipo_estado_requisito
    WHERE ra.id_postulacion = v_postulacion.id_postulacion;

    -- 6. Construir respuesta final
    v_resultado := jsonb_build_object(
        'exito', TRUE,
        'mensaje', 'Detalle de postulación obtenido correctamente',
        'postulacion', jsonb_build_object(
            'id_postulacion', v_postulacion.id_postulacion,
            'fecha_postulacion', v_postulacion.fecha_postulacion,
            'estado_codigo', v_postulacion.estado_codigo,
            'estado_nombre', v_postulacion.estado_nombre,
            'observaciones', COALESCE(v_postulacion.observaciones, '')
        ),
        'convocatoria', jsonb_build_object(
            'id_convocatoria', v_postulacion.id_convocatoria,
            'nombre_asignatura', v_postulacion.nombre_asignatura,
            'semestre_asignatura', v_postulacion.semestre_asignatura,
            'nombre_carrera', v_postulacion.nombre_carrera,
            'nombre_docente', v_postulacion.nombre_docente,
            'cupos_disponibles', v_postulacion.cupos_disponibles
        ),
        'cronograma', v_cronograma,
        'documentos', v_documentos,
        'resumen_documentos', v_resumen,
        -- NUEVO: Indicadores de periodo
        'es_periodo_subsanacion', convocatoria.fn_es_periodo_subsanacion(v_postulacion.id_convocatoria),
        'es_postulacion_rechazada', (v_postulacion.estado_codigo = 'RECHAZADA')
    );

    RETURN v_resultado;

EXCEPTION WHEN OTHERS THEN
    RETURN jsonb_build_object(
        'exito', FALSE,
        'mensaje', 'ERROR SISTEMA [' || SQLSTATE || ']: ' || SQLERRM
    );
END;
$$;

COMMENT ON FUNCTION postulacion.fn_ver_detalle_postulacion IS
    'Obtiene el detalle de la postulación activa del estudiante con información de cronograma, documentos y estado de subsanación (24h).';


-- ============================================================
-- Permisos
-- ============================================================
GRANT EXECUTE ON FUNCTION convocatoria.fn_es_periodo_subsanacion TO role_ayudante_catedra;
GRANT EXECUTE ON FUNCTION postulacion.fn_evaluar_documento_individual TO role_ayudante_catedra;
GRANT EXECUTE ON FUNCTION postulacion.fn_dictaminar_postulacion TO role_ayudante_catedra;
GRANT EXECUTE ON FUNCTION postulacion.fn_subsanar_documento_estudiante TO role_ayudante_catedra;
GRANT EXECUTE ON FUNCTION postulacion.fn_obtener_detalle_postulacion_coordinador TO role_ayudante_catedra;
GRANT EXECUTE ON FUNCTION postulacion.fn_ver_detalle_postulacion TO role_ayudante_catedra;

