-- ============================================================
-- V38 — Funciones para Evaluación de Postulantes por Coordinador
-- ============================================================

-- ============================================================
-- A. FUNCIÓN DE LISTADO: fn_listar_postulaciones_coordinador
-- Retorna todas las postulaciones de la carrera del coordinador
-- ============================================================
CREATE OR REPLACE FUNCTION postulacion.fn_listar_postulaciones_coordinador(
    p_id_usuario INTEGER
)
RETURNS TABLE (
    id_postulacion INTEGER,
    id_convocatoria INTEGER,
    id_estudiante INTEGER,
    nombre_estudiante VARCHAR,
    matricula VARCHAR,
    semestre INTEGER,
    nombre_asignatura VARCHAR,
    nombre_carrera VARCHAR,
    fecha_postulacion DATE,
    estado_codigo VARCHAR,
    estado_nombre VARCHAR,
    requiere_atencion BOOLEAN,
    total_documentos BIGINT,
    documentos_pendientes BIGINT,
    documentos_aprobados BIGINT,
    documentos_observados BIGINT,
    observaciones VARCHAR
) AS $$
DECLARE
    v_id_coordinador INTEGER;
    v_id_carrera INTEGER;
BEGIN
    -- Obtener el coordinador y su carrera
    SELECT c.id_coordinador, c.id_carrera
    INTO v_id_coordinador, v_id_carrera
    FROM academico.coordinador c
    WHERE c.id_usuario = p_id_usuario
      AND c.activo = TRUE;

    IF v_id_coordinador IS NULL THEN
        RAISE EXCEPTION 'AVISO: El usuario no tiene rol de coordinador activo asignado';
    END IF;

    RETURN QUERY
    SELECT
        p.id_postulacion,
        p.id_convocatoria,
        p.id_estudiante,
        (u.nombres || ' ' || u.apellidos)::VARCHAR AS nombre_estudiante,
        e.matricula::VARCHAR,
        e.semestre,
        a.nombre_asignatura::VARCHAR AS nombre_asignatura,
        car.nombre_carrera::VARCHAR AS nombre_carrera,
        p.fecha_postulacion,
        tep.codigo::VARCHAR AS estado_codigo,
        tep.nombre::VARCHAR AS estado_nombre,
        -- Requiere atención si está PENDIENTE o CORREGIDA
        (tep.codigo IN ('PENDIENTE', 'CORREGIDA'))::BOOLEAN AS requiere_atencion,
        -- Conteo de documentos
        COALESCE((
            SELECT COUNT(*)
            FROM postulacion.requisito_adjunto ra
            WHERE ra.id_postulacion = p.id_postulacion
        ), 0)::BIGINT AS total_documentos,
        COALESCE((
            SELECT COUNT(*)
            FROM postulacion.requisito_adjunto ra
            JOIN convocatoria.tipo_estado_requisito ter ON ra.id_tipo_estado_requisito = ter.id_tipo_estado_requisito
            WHERE ra.id_postulacion = p.id_postulacion
              AND UPPER(ter.nombre_estado) = 'PENDIENTE'
        ), 0)::BIGINT AS documentos_pendientes,
        COALESCE((
            SELECT COUNT(*)
            FROM postulacion.requisito_adjunto ra
            JOIN convocatoria.tipo_estado_requisito ter ON ra.id_tipo_estado_requisito = ter.id_tipo_estado_requisito
            WHERE ra.id_postulacion = p.id_postulacion
              AND UPPER(ter.nombre_estado) IN ('APROBADO', 'VALIDADO')
        ), 0)::BIGINT AS documentos_aprobados,
        COALESCE((
            SELECT COUNT(*)
            FROM postulacion.requisito_adjunto ra
            JOIN convocatoria.tipo_estado_requisito ter ON ra.id_tipo_estado_requisito = ter.id_tipo_estado_requisito
            WHERE ra.id_postulacion = p.id_postulacion
              AND UPPER(ter.nombre_estado) = 'OBSERVADO'
        ), 0)::BIGINT AS documentos_observados,
        p.observaciones::VARCHAR
    FROM postulacion.postulacion p
    JOIN academico.estudiante e ON p.id_estudiante = e.id_estudiante
    JOIN seguridad.usuario u ON e.id_usuario = u.id_usuario
    JOIN convocatoria.convocatoria cv ON p.id_convocatoria = cv.id_convocatoria
    JOIN academico.asignatura a ON cv.id_asignatura = a.id_asignatura
    JOIN academico.carrera car ON a.id_carrera = car.id_carrera
    LEFT JOIN postulacion.tipo_estado_postulacion tep ON p.id_tipo_estado_postulacion = tep.id_tipo_estado_postulacion
    WHERE car.id_carrera = v_id_carrera
      AND p.activo = TRUE
    ORDER BY
        CASE WHEN tep.codigo IN ('PENDIENTE', 'CORREGIDA') THEN 0 ELSE 1 END,
        p.fecha_postulacion DESC;

EXCEPTION WHEN OTHERS THEN
    RAISE EXCEPTION 'ERROR SISTEMA [%]: %', SQLSTATE, SQLERRM;
END;
$$ LANGUAGE plpgsql;

-- ============================================================
-- B. FUNCIÓN DETALLE: fn_obtener_detalle_postulacion_coordinador
-- Retorna información detallada de una postulación específica
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
    -- Validar coordinador
    SELECT c.id_coordinador, c.id_carrera
    INTO v_id_coordinador, v_id_carrera
    FROM academico.coordinador c
    WHERE c.id_usuario = p_id_usuario
      AND c.activo = TRUE;

    IF v_id_coordinador IS NULL THEN
        RAISE EXCEPTION 'AVISO: El usuario no tiene rol de coordinador activo asignado';
    END IF;

    -- Validar que la postulación pertenece a la carrera del coordinador
    SELECT car.id_carrera INTO v_postulacion_carrera
    FROM postulacion.postulacion p
    JOIN convocatoria.convocatoria cv ON p.id_convocatoria = cv.id_convocatoria
    JOIN academico.asignatura a ON cv.id_asignatura = a.id_asignatura
    JOIN academico.carrera car ON a.id_carrera = car.id_carrera
    WHERE p.id_postulacion = p_id_postulacion;

    IF v_postulacion_carrera IS NULL THEN
        RAISE EXCEPTION 'AVISO: La postulación no existe';
    END IF;

    IF v_postulacion_carrera != v_id_carrera THEN
        RAISE EXCEPTION 'AVISO: No tiene permisos para ver esta postulación';
    END IF;

    -- Construir respuesta JSON
    SELECT jsonb_build_object(
        'postulacion', jsonb_build_object(
            'id_postulacion', p.id_postulacion,
            'fecha_postulacion', p.fecha_postulacion,
            'estado_codigo', tep.codigo,
            'estado_nombre', tep.nombre,
            'observaciones', p.observaciones
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
            'fecha_publicacion', cv.fecha_publicacion,
            'fecha_cierre', cv.fecha_cierre,
            'cupos_disponibles', cv.cupos_disponibles
        ),
        'documentos', (
            SELECT COALESCE(jsonb_agg(
                jsonb_build_object(
                    'id_requisito_adjunto', ra.id_requisito_adjunto,
                    'tipo_requisito', trp.nombre_requisito,
                    'descripcion_requisito', trp.descripcion,
                    'nombre_archivo', ra.nombre_archivo,
                    'fecha_subida', ra.fecha_subida,
                    'estado_codigo', ter.nombre_estado,
                    'id_tipo_estado_requisito', ter.id_tipo_estado_requisito,
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
            'total', (SELECT COUNT(*) FROM postulacion.requisito_adjunto WHERE id_postulacion = p.id_postulacion),
            'pendientes', (SELECT COUNT(*) FROM postulacion.requisito_adjunto ra
                           JOIN convocatoria.tipo_estado_requisito ter ON ra.id_tipo_estado_requisito = ter.id_tipo_estado_requisito
                           WHERE ra.id_postulacion = p.id_postulacion AND UPPER(ter.nombre_estado) = 'PENDIENTE'),
            'aprobados', (SELECT COUNT(*) FROM postulacion.requisito_adjunto ra
                          JOIN convocatoria.tipo_estado_requisito ter ON ra.id_tipo_estado_requisito = ter.id_tipo_estado_requisito
                          WHERE ra.id_postulacion = p.id_postulacion AND UPPER(ter.nombre_estado) IN ('APROBADO', 'VALIDADO')),
            'observados', (SELECT COUNT(*) FROM postulacion.requisito_adjunto ra
                           JOIN convocatoria.tipo_estado_requisito ter ON ra.id_tipo_estado_requisito = ter.id_tipo_estado_requisito
                           WHERE ra.id_postulacion = p.id_postulacion AND UPPER(ter.nombre_estado) = 'OBSERVADO'),
            'rechazados', (SELECT COUNT(*) FROM postulacion.requisito_adjunto ra
                           JOIN convocatoria.tipo_estado_requisito ter ON ra.id_tipo_estado_requisito = ter.id_tipo_estado_requisito
                           WHERE ra.id_postulacion = p.id_postulacion AND UPPER(ter.nombre_estado) = 'RECHAZADO')
        ),
        'puede_aprobar', (
            SELECT NOT EXISTS (
                SELECT 1 FROM postulacion.requisito_adjunto ra
                JOIN convocatoria.tipo_estado_requisito ter ON ra.id_tipo_estado_requisito = ter.id_tipo_estado_requisito
                WHERE ra.id_postulacion = p.id_postulacion
                  AND UPPER(ter.nombre_estado) NOT IN ('APROBADO', 'VALIDADO')
            )
        )
    ) INTO v_resultado
    FROM postulacion.postulacion p
    JOIN academico.estudiante e ON p.id_estudiante = e.id_estudiante
    JOIN seguridad.usuario u ON e.id_usuario = u.id_usuario
    JOIN convocatoria.convocatoria cv ON p.id_convocatoria = cv.id_convocatoria
    JOIN academico.asignatura a ON cv.id_asignatura = a.id_asignatura
    JOIN academico.docente d ON cv.id_docente = d.id_docente
    JOIN seguridad.usuario ud ON d.id_usuario = ud.id_usuario
    LEFT JOIN postulacion.tipo_estado_postulacion tep ON p.id_tipo_estado_postulacion = tep.id_tipo_estado_postulacion
    WHERE p.id_postulacion = p_id_postulacion;

    RETURN v_resultado;

EXCEPTION WHEN OTHERS THEN
    RAISE EXCEPTION 'ERROR SISTEMA [%]: %', SQLSTATE, SQLERRM;
END;
$$ LANGUAGE plpgsql;




-- ============================================================
-- C. FUNCIÓN DE ACCIÓN: fn_evaluar_documento_individual
-- Evalúa un documento individual (VALIDAR, OBSERVAR, RECHAZAR)
-- ============================================================
CREATE OR REPLACE FUNCTION postulacion.fn_evaluar_documento_individual(
    p_id_usuario INTEGER,
    p_id_requisito_adjunto INTEGER,
    p_accion VARCHAR(20),  -- 'VALIDAR', 'OBSERVAR', 'RECHAZAR'
    p_observacion TEXT DEFAULT NULL
)
    RETURNS JSONB AS $$
DECLARE
    v_id_coordinador INTEGER;
    v_id_carrera INTEGER;
    v_id_postulacion INTEGER;
    v_postulacion_carrera INTEGER;
    v_id_nuevo_estado INTEGER;
    v_nombre_estado VARCHAR;
    v_tiene_observados BOOLEAN;
    v_todos_validados BOOLEAN;
    v_id_estudiante INTEGER;
    v_id_usuario_estudiante INTEGER;
    v_id_estado_postulacion_observada INTEGER;
    v_id_estado_postulacion_revision INTEGER;
BEGIN
    -- Validar coordinador
    SELECT c.id_coordinador, c.id_carrera
    INTO v_id_coordinador, v_id_carrera
    FROM academico.coordinador c
    WHERE c.id_usuario = p_id_usuario
      AND c.activo = TRUE;

    IF v_id_coordinador IS NULL THEN
        RETURN jsonb_build_object(
                'exito', FALSE,
                'codigo', 'ERROR_AUTORIZACION',
                'mensaje', 'AVISO: El usuario no tiene rol de coordinador activo asignado'
               );
    END IF;

    -- Obtener información del documento y validar permisos
    SELECT ra.id_postulacion, car.id_carrera, p.id_estudiante
    INTO v_id_postulacion, v_postulacion_carrera, v_id_estudiante
    FROM postulacion.requisito_adjunto ra
             JOIN postulacion.postulacion p ON ra.id_postulacion = p.id_postulacion
             JOIN convocatoria.convocatoria cv ON p.id_convocatoria = cv.id_convocatoria
             JOIN academico.asignatura a ON cv.id_asignatura = a.id_asignatura
             JOIN academico.carrera car ON a.id_carrera = car.id_carrera
    WHERE ra.id_requisito_adjunto = p_id_requisito_adjunto;

    IF v_id_postulacion IS NULL THEN
        RETURN jsonb_build_object(
                'exito', FALSE,
                'codigo', 'ERROR_NO_ENCONTRADO',
                'mensaje', 'AVISO: El documento no existe'
               );
    END IF;

    IF v_postulacion_carrera != v_id_carrera THEN
        RETURN jsonb_build_object(
                'exito', FALSE,
                'codigo', 'ERROR_PERMISOS',
                'mensaje', 'AVISO: No tiene permisos para evaluar este documento'
               );
    END IF;

    -- Obtener el id_usuario del estudiante
    SELECT e.id_usuario INTO v_id_usuario_estudiante
    FROM academico.estudiante e
    WHERE e.id_estudiante = v_id_estudiante;

    -- Determinar el nuevo estado según la acción
    CASE UPPER(p_accion)
        WHEN 'VALIDAR' THEN
            SELECT id_tipo_estado_requisito, nombre_estado
            INTO v_id_nuevo_estado, v_nombre_estado
            FROM convocatoria.tipo_estado_requisito
            WHERE UPPER(nombre_estado) IN ('APROBADO', 'VALIDADO') AND activo = TRUE
            LIMIT 1;

            IF v_id_nuevo_estado IS NULL THEN
                -- Crear el estado si no existe
                INSERT INTO convocatoria.tipo_estado_requisito (nombre_estado, descripcion, activo)
                VALUES ('APROBADO', 'Documento validado por el coordinador', TRUE)
                RETURNING id_tipo_estado_requisito, nombre_estado INTO v_id_nuevo_estado, v_nombre_estado;
            END IF;

        WHEN 'OBSERVAR' THEN
            IF p_observacion IS NULL OR TRIM(p_observacion) = '' THEN
                RETURN jsonb_build_object(
                        'exito', FALSE,
                        'codigo', 'ERROR_VALIDACION',
                        'mensaje', 'AVISO: Debe proporcionar una observación para el documento'
                       );
            END IF;

            SELECT id_tipo_estado_requisito, nombre_estado
            INTO v_id_nuevo_estado, v_nombre_estado
            FROM convocatoria.tipo_estado_requisito
            WHERE UPPER(nombre_estado) = 'OBSERVADO' AND activo = TRUE
            LIMIT 1;

        WHEN 'RECHAZAR' THEN
            SELECT id_tipo_estado_requisito, nombre_estado
            INTO v_id_nuevo_estado, v_nombre_estado
            FROM convocatoria.tipo_estado_requisito
            WHERE UPPER(nombre_estado) = 'RECHAZADO' AND activo = TRUE
            LIMIT 1;

        ELSE
            RETURN jsonb_build_object(
                    'exito', FALSE,
                    'codigo', 'ERROR_ACCION',
                    'mensaje', 'AVISO: Acción no válida. Use VALIDAR, OBSERVAR o RECHAZAR'
                   );
        END CASE;

    IF v_id_nuevo_estado IS NULL THEN
        RETURN jsonb_build_object(
                'exito', FALSE,
                'codigo', 'ERROR_CONFIGURACION',
                'mensaje', 'AVISO: No se encontró el estado de requisito configurado en el sistema'
               );
    END IF;

    -- Actualizar el documento
    UPDATE postulacion.requisito_adjunto
    SET id_tipo_estado_requisito = v_id_nuevo_estado,
        observacion = CASE WHEN UPPER(p_accion) = 'OBSERVAR' THEN p_observacion ELSE observacion END
    WHERE id_requisito_adjunto = p_id_requisito_adjunto;

    -- Si se observó un documento, actualizar estado de la postulación a OBSERVADA
    IF UPPER(p_accion) = 'OBSERVAR' THEN
        SELECT id_tipo_estado_postulacion INTO v_id_estado_postulacion_observada
        FROM postulacion.tipo_estado_postulacion
        WHERE codigo = 'OBSERVADA';

        IF v_id_estado_postulacion_observada IS NOT NULL THEN
            UPDATE postulacion.postulacion
            SET id_tipo_estado_postulacion = v_id_estado_postulacion_observada,
                estado_postulacion = 'OBSERVADA'
            WHERE id_postulacion = v_id_postulacion;
        END IF;

        -- Notificar al estudiante (usando notificacion_ws)
        IF v_id_usuario_estudiante IS NOT NULL THEN
            INSERT INTO notificacion.notificacion_ws (
                id_usuario,
                titulo,
                mensaje,
                tipo,
                leido,
                fecha_creacion,
                id_referencia
            )
            VALUES (
                       v_id_usuario_estudiante,
                       'Documento Observado',
                       'Tu documento ha sido observado. Por favor, revisa las observaciones y realiza las correcciones necesarias.',
                       'OBSERVACION',
                       FALSE,
                       NOW(),
                       v_id_postulacion
                   );
        END IF;
    END IF;

    -- Verificar estados de documentos
    SELECT EXISTS (
        SELECT 1 FROM postulacion.requisito_adjunto ra
                          JOIN convocatoria.tipo_estado_requisito ter ON ra.id_tipo_estado_requisito = ter.id_tipo_estado_requisito
        WHERE ra.id_postulacion = v_id_postulacion
          AND UPPER(ter.nombre_estado) = 'OBSERVADO'
    ) INTO v_tiene_observados;

    SELECT NOT EXISTS (
        SELECT 1 FROM postulacion.requisito_adjunto ra
                          JOIN convocatoria.tipo_estado_requisito ter ON ra.id_tipo_estado_requisito = ter.id_tipo_estado_requisito
        WHERE ra.id_postulacion = v_id_postulacion
          AND UPPER(ter.nombre_estado) NOT IN ('APROBADO', 'VALIDADO')
    ) INTO v_todos_validados;

    RETURN jsonb_build_object(
            'exito', TRUE,
            'codigo', 'OK',
            'mensaje', 'Documento evaluado correctamente',
            'id_requisito_adjunto', p_id_requisito_adjunto,
            'nuevo_estado', v_nombre_estado,
            'tiene_observados', v_tiene_observados,
            'todos_validados', v_todos_validados,
            'puede_aprobar_postulacion', v_todos_validados
           );

EXCEPTION WHEN OTHERS THEN
    RETURN jsonb_build_object(
            'exito', FALSE,
            'codigo', 'ERROR_SISTEMA',
            'mensaje', 'ERROR SISTEMA [' || SQLSTATE || ']: ' || SQLERRM
           );
END;
$$ LANGUAGE plpgsql;

-- ============================================================
-- D. FUNCIÓN DE ACCIÓN FINAL: fn_dictaminar_postulacion
-- Aprobar o Rechazar la postulación completa
-- ============================================================
CREATE OR REPLACE FUNCTION postulacion.fn_dictaminar_postulacion(
    p_id_usuario INTEGER,
    p_id_postulacion INTEGER,
    p_accion VARCHAR(20),  -- 'APROBAR', 'RECHAZAR'
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
BEGIN
    -- Validar coordinador
    SELECT c.id_coordinador, c.id_carrera
    INTO v_id_coordinador, v_id_carrera
    FROM academico.coordinador c
    WHERE c.id_usuario = p_id_usuario
      AND c.activo = TRUE;

    IF v_id_coordinador IS NULL THEN
        RETURN jsonb_build_object(
                'exito', FALSE,
                'codigo', 'ERROR_AUTORIZACION',
                'mensaje', 'AVISO: El usuario no tiene rol de coordinador activo asignado'
               );
    END IF;

    -- Obtener información de la postulación y validar permisos
    SELECT car.id_carrera, p.id_estudiante
    INTO v_postulacion_carrera, v_id_estudiante
    FROM postulacion.postulacion p
             JOIN convocatoria.convocatoria cv ON p.id_convocatoria = cv.id_convocatoria
             JOIN academico.asignatura a ON cv.id_asignatura = a.id_asignatura
             JOIN academico.carrera car ON a.id_carrera = car.id_carrera
    WHERE p.id_postulacion = p_id_postulacion;

    IF v_postulacion_carrera IS NULL THEN
        RETURN jsonb_build_object(
                'exito', FALSE,
                'codigo', 'ERROR_NO_ENCONTRADO',
                'mensaje', 'AVISO: La postulación no existe'
               );
    END IF;

    IF v_postulacion_carrera != v_id_carrera THEN
        RETURN jsonb_build_object(
                'exito', FALSE,
                'codigo', 'ERROR_PERMISOS',
                'mensaje', 'AVISO: No tiene permisos para dictaminar esta postulación'
               );
    END IF;

    -- Obtener el id_usuario del estudiante
    SELECT e.id_usuario INTO v_id_usuario_estudiante
    FROM academico.estudiante e
    WHERE e.id_estudiante = v_id_estudiante;

    -- Validar acción
    CASE UPPER(p_accion)
        WHEN 'APROBAR' THEN
            -- Verificar que todos los documentos estén validados
            SELECT NOT EXISTS (
                SELECT 1 FROM postulacion.requisito_adjunto ra
                                  JOIN convocatoria.tipo_estado_requisito ter ON ra.id_tipo_estado_requisito = ter.id_tipo_estado_requisito
                WHERE ra.id_postulacion = p_id_postulacion
                  AND UPPER(ter.nombre_estado) NOT IN ('APROBADO', 'VALIDADO')
            ) INTO v_todos_validados;

            IF NOT v_todos_validados THEN
                RETURN jsonb_build_object(
                        'exito', FALSE,
                        'codigo', 'ERROR_DOCUMENTOS_PENDIENTES',
                        'mensaje', 'AVISO: No se puede aprobar la postulación. Todos los documentos deben estar validados.'
                       );
            END IF;

            v_estado_codigo := 'APROBADA';

        WHEN 'RECHAZAR' THEN
            IF p_observacion IS NULL OR TRIM(p_observacion) = '' THEN
                RETURN jsonb_build_object(
                        'exito', FALSE,
                        'codigo', 'ERROR_VALIDACION',
                        'mensaje', 'AVISO: Debe proporcionar un motivo para rechazar la postulación'
                       );
            END IF;

            v_estado_codigo := 'RECHAZADA';

        ELSE
            RETURN jsonb_build_object(
                    'exito', FALSE,
                    'codigo', 'ERROR_ACCION',
                    'mensaje', 'AVISO: Acción no válida. Use APROBAR o RECHAZAR'
                   );
        END CASE;

    -- Obtener ID del estado
    SELECT id_tipo_estado_postulacion INTO v_id_nuevo_estado
    FROM postulacion.tipo_estado_postulacion
    WHERE codigo = v_estado_codigo;

    IF v_id_nuevo_estado IS NULL THEN
        RETURN jsonb_build_object(
                'exito', FALSE,
                'codigo', 'ERROR_CONFIGURACION',
                'mensaje', 'AVISO: No se encontró el estado de postulación configurado en el sistema'
               );
    END IF;

    -- Actualizar la postulación
    UPDATE postulacion.postulacion
    SET id_tipo_estado_postulacion = v_id_nuevo_estado,
        estado_postulacion = v_estado_codigo,
        observaciones = CASE WHEN p_observacion IS NOT NULL THEN p_observacion ELSE observaciones END
    WHERE id_postulacion = p_id_postulacion;

    -- Notificar al estudiante (usando notificacion_ws)
    IF v_id_usuario_estudiante IS NOT NULL THEN
        INSERT INTO notificacion.notificacion_ws (
            id_usuario,
            titulo,
            mensaje,
            tipo,
            leido,
            fecha_creacion,
            id_referencia
        )
        VALUES (
                   v_id_usuario_estudiante,
                   CASE
                       WHEN v_estado_codigo = 'APROBADA' THEN 'Postulación Aprobada'
                       ELSE 'Postulación Rechazada'
                       END,
                   CASE
                       WHEN v_estado_codigo = 'APROBADA' THEN
                           '¡Felicitaciones! Tu postulación ha sido APROBADA. Pronto recibirás más información sobre los siguientes pasos.'
                       ELSE
                           'Tu postulación ha sido RECHAZADA. Motivo: ' || COALESCE(p_observacion, 'No especificado')
                       END,
                   CASE WHEN v_estado_codigo = 'APROBADA' THEN 'APROBACION' ELSE 'RECHAZO' END,
                   FALSE,
                   NOW(),
                   p_id_postulacion
               );
    END IF;

    RETURN jsonb_build_object(
            'exito', TRUE,
            'codigo', 'OK',
            'mensaje', CASE
                           WHEN v_estado_codigo = 'APROBADA' THEN 'Postulación aprobada correctamente'
                           ELSE 'Postulación rechazada correctamente'
                END,
            'id_postulacion', p_id_postulacion,
            'nuevo_estado', v_estado_codigo
           );

EXCEPTION WHEN OTHERS THEN
    RETURN jsonb_build_object(
            'exito', FALSE,
            'codigo', 'ERROR_SISTEMA',
            'mensaje', 'ERROR SISTEMA [' || SQLSTATE || ']: ' || SQLERRM
           );
END;
$$ LANGUAGE plpgsql;


-- ============================================================
-- E. FUNCIÓN AUXILIAR: fn_cambiar_estado_postulacion_revision
-- Cambia automáticamente a EN_REVISION cuando el coordinador abre
-- ============================================================
CREATE OR REPLACE FUNCTION postulacion.fn_cambiar_estado_postulacion_revision(
    p_id_usuario INTEGER,
    p_id_postulacion INTEGER
)
RETURNS JSONB AS $$
DECLARE
    v_id_coordinador INTEGER;
    v_id_carrera INTEGER;
    v_postulacion_carrera INTEGER;
    v_estado_actual VARCHAR;
    v_id_estado_revision INTEGER;
BEGIN
    -- Validar coordinador
    SELECT c.id_coordinador, c.id_carrera
    INTO v_id_coordinador, v_id_carrera
    FROM academico.coordinador c
    WHERE c.id_usuario = p_id_usuario
      AND c.activo = TRUE;

    IF v_id_coordinador IS NULL THEN
        RETURN jsonb_build_object(
            'exito', FALSE,
            'mensaje', 'AVISO: El usuario no tiene rol de coordinador activo asignado'
        );
    END IF;

    -- Obtener información de la postulación
    SELECT car.id_carrera, tep.codigo
    INTO v_postulacion_carrera, v_estado_actual
    FROM postulacion.postulacion p
    JOIN convocatoria.convocatoria cv ON p.id_convocatoria = cv.id_convocatoria
    JOIN academico.asignatura a ON cv.id_asignatura = a.id_asignatura
    JOIN academico.carrera car ON a.id_carrera = car.id_carrera
    LEFT JOIN postulacion.tipo_estado_postulacion tep ON p.id_tipo_estado_postulacion = tep.id_tipo_estado_postulacion
    WHERE p.id_postulacion = p_id_postulacion;

    IF v_postulacion_carrera IS NULL THEN
        RETURN jsonb_build_object(
            'exito', FALSE,
            'mensaje', 'AVISO: La postulación no existe'
        );
    END IF;

    IF v_postulacion_carrera != v_id_carrera THEN
        RETURN jsonb_build_object(
            'exito', FALSE,
            'mensaje', 'AVISO: No tiene permisos para esta postulación'
        );
    END IF;

    -- Solo cambiar si está en PENDIENTE o CORREGIDA
    IF v_estado_actual NOT IN ('PENDIENTE', 'CORREGIDA') THEN
        RETURN jsonb_build_object(
            'exito', TRUE,
            'mensaje', 'La postulación ya se encuentra en estado: ' || v_estado_actual,
            'estado_actual', v_estado_actual,
            'cambio_realizado', FALSE
        );
    END IF;

    -- Obtener estado EN_REVISION
    SELECT id_tipo_estado_postulacion INTO v_id_estado_revision
    FROM postulacion.tipo_estado_postulacion
    WHERE codigo = 'EN_REVISION';

    IF v_id_estado_revision IS NULL THEN
        RETURN jsonb_build_object(
            'exito', FALSE,
            'mensaje', 'AVISO: No se encontró el estado EN_REVISION configurado'
        );
    END IF;

    -- Actualizar
    UPDATE postulacion.postulacion
    SET id_tipo_estado_postulacion = v_id_estado_revision,
        estado_postulacion = 'EN_REVISION'
    WHERE id_postulacion = p_id_postulacion;

    RETURN jsonb_build_object(
        'exito', TRUE,
        'mensaje', 'Postulación cambiada a EN_REVISION',
        'estado_anterior', v_estado_actual,
        'estado_actual', 'EN_REVISION',
        'cambio_realizado', TRUE
    );

EXCEPTION WHEN OTHERS THEN
    RETURN jsonb_build_object(
        'exito', FALSE,
        'mensaje', 'ERROR SISTEMA [' || SQLSTATE || ']: ' || SQLERRM
    );
END;
$$ LANGUAGE plpgsql;


-- ============================================================
-- También corregir el trigger de notificación de postulación
-- para usar notificacion_ws
-- ============================================================
CREATE OR REPLACE FUNCTION public.fn_notif_cambio_estado_postulacion()
    RETURNS TRIGGER AS $$
DECLARE
    v_id_usuario_destino INTEGER;
    v_titulo VARCHAR(150);
    v_mensaje TEXT;
BEGIN
    -- Disparar si cambió el estado o las observaciones
    IF (NEW.estado_postulacion IS DISTINCT FROM OLD.estado_postulacion)
        OR (NEW.observaciones IS DISTINCT FROM OLD.observaciones) THEN

        -- Obtener el id_usuario del estudiante
        SELECT e.id_usuario INTO v_id_usuario_destino
        FROM academico.estudiante e
        WHERE e.id_estudiante = NEW.id_estudiante;

        IF v_id_usuario_destino IS NOT NULL THEN
            -- Determinar título y mensaje según el estado
            v_titulo := 'Actualización de Postulación';
            v_mensaje := 'Tu postulación fue actualizada. Revisa el estado y observaciones en la plataforma.';

            INSERT INTO notificacion.notificacion_ws (
                id_usuario,
                titulo,
                mensaje,
                tipo,
                leido,
                fecha_creacion,
                id_referencia
            )
            VALUES (
                       v_id_usuario_destino,
                       v_titulo,
                       v_mensaje,
                       'ACTUALIZACION',
                       FALSE,
                       NOW(),
                       NEW.id_postulacion
                   );
        END IF;
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;




-- ============================================================
-- PERMISOS
-- ============================================================
GRANT EXECUTE ON FUNCTION postulacion.fn_listar_postulaciones_coordinador(INTEGER) TO role_coordinador;
GRANT EXECUTE ON FUNCTION postulacion.fn_obtener_detalle_postulacion_coordinador(INTEGER, INTEGER) TO role_coordinador;
GRANT EXECUTE ON FUNCTION postulacion.fn_evaluar_documento_individual(INTEGER, INTEGER, VARCHAR, TEXT) TO role_coordinador;
GRANT EXECUTE ON FUNCTION postulacion.fn_dictaminar_postulacion(INTEGER, INTEGER, VARCHAR, TEXT) TO role_coordinador;
GRANT EXECUTE ON FUNCTION postulacion.fn_cambiar_estado_postulacion_revision(INTEGER, INTEGER) TO role_coordinador;

-- Permisos de lectura/escritura en tablas necesarias
GRANT SELECT, UPDATE ON postulacion.postulacion TO role_coordinador;
GRANT SELECT, UPDATE ON postulacion.requisito_adjunto TO role_coordinador;
GRANT SELECT ON postulacion.tipo_estado_postulacion TO role_coordinador;
GRANT SELECT ON convocatoria.tipo_estado_requisito TO role_coordinador;
GRANT INSERT ON notificacion.notificacion TO role_coordinador;

