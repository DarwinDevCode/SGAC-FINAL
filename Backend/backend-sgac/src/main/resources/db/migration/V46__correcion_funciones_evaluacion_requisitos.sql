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
    -- 1. Validar que el usuario sea un coordinador activo
    SELECT c.id_coordinador, c.id_carrera
    INTO v_id_coordinador, v_id_carrera
    FROM academico.coordinador c
    WHERE c.id_usuario = p_id_usuario
      AND c.activo = TRUE;

    IF v_id_coordinador IS NULL THEN
        RAISE EXCEPTION 'AVISO: El usuario no tiene rol de coordinador activo asignado';
    END IF;

    -- 2. Validar que la postulación existe y pertenece a la carrera del coordinador
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

    -- 3. Construir respuesta JSON consolidada
    WITH info_calendario AS (
        -- Obtenemos fechas de visibilidad real desde el calendario centralizado
        SELECT
            pf.id_periodo_academico,
            MIN(CASE WHEN tf.codigo = 'PUBLICACION_OFERTA' THEN pf.fecha_inicio END) as f_publicacion,
            MAX(CASE WHEN tf.codigo = 'POSTULACION' THEN pf.fecha_fin END) as f_cierre
        FROM planificacion.periodo_fase pf
                 JOIN planificacion.tipo_fase tf ON pf.id_tipo_fase = tf.id_tipo_fase
        GROUP BY pf.id_periodo_academico
    ),
         conteo_docs AS (
             -- Calculamos todos los totales en una sola pasada (Mucho más rápido)
             SELECT
                 ra.id_postulacion,
                 COUNT(*) as total,
                 COUNT(*) FILTER (WHERE UPPER(ter.nombre_estado) = 'PENDIENTE') as pendientes,
                 COUNT(*) FILTER (WHERE UPPER(ter.nombre_estado) IN ('APROBADO', 'VALIDADO')) as aprobados,
                 COUNT(*) FILTER (WHERE UPPER(ter.nombre_estado) = 'OBSERVADO') as observados,
                 COUNT(*) FILTER (WHERE UPPER(ter.nombre_estado) = 'RECHAZADO') as rechazados,
                 COUNT(*) FILTER (WHERE UPPER(ter.nombre_estado) = 'CORREGIDO') as corregidos
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
                       -- Fechas obtenidas del calendario centralizado
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
               -- Lógica de aprobación: Solo si todos están aprobados y hay al menos uno
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
    v_postulacion_carrera INTEGER;
    v_id_nuevo_estado INTEGER;
    v_nombre_estado_req VARCHAR;
    v_id_usuario_estudiante INTEGER;
    v_id_estado_postulacion_observada INTEGER;
    v_tiene_observados BOOLEAN;
    v_todos_validados BOOLEAN;
BEGIN
    SELECT c.id_coordinador, c.id_carrera
    INTO v_id_coordinador, v_id_carrera
    FROM academico.coordinador c
    WHERE c.id_usuario = p_id_usuario AND c.activo = TRUE;

    IF v_id_coordinador IS NULL THEN
        RETURN jsonb_build_object('exito', FALSE, 'mensaje', 'El usuario no es un coordinador activo');
    END IF;

    SELECT ra.id_postulacion, car.id_carrera, e.id_usuario
    INTO v_id_postulacion, v_postulacion_carrera, v_id_usuario_estudiante
    FROM postulacion.requisito_adjunto ra
             JOIN postulacion.postulacion p ON ra.id_postulacion = p.id_postulacion
             JOIN convocatoria.convocatoria cv ON p.id_convocatoria = cv.id_convocatoria
             JOIN academico.asignatura a ON cv.id_asignatura = a.id_asignatura
             JOIN academico.carrera car ON a.id_carrera = car.id_carrera
             JOIN academico.estudiante e ON p.id_estudiante = e.id_estudiante
    WHERE ra.id_requisito_adjunto = p_id_requisito_adjunto;

    IF v_id_postulacion IS NULL THEN
        RETURN jsonb_build_object('exito', FALSE, 'mensaje', 'Documento no encontrado');
    END IF;

    IF v_postulacion_carrera != v_id_carrera THEN
        RETURN jsonb_build_object('exito', FALSE, 'mensaje', 'No tiene permisos sobre esta carrera');
    END IF;

    CASE UPPER(p_accion)
        WHEN 'VALIDAR' THEN
            SELECT id_tipo_estado_requisito, nombre_estado INTO v_id_nuevo_estado, v_nombre_estado_req
            FROM convocatoria.tipo_estado_requisito
            WHERE UPPER(nombre_estado) IN ('APROBADO', 'VALIDADO') AND activo = TRUE LIMIT 1;

        WHEN 'OBSERVAR' THEN
            IF p_observacion IS NULL OR TRIM(p_observacion) = '' THEN
                RETURN jsonb_build_object('exito', FALSE, 'mensaje', 'Debe indicar una observación');
            END IF;
            SELECT id_tipo_estado_requisito, nombre_estado INTO v_id_nuevo_estado, v_nombre_estado_req
            FROM convocatoria.tipo_estado_requisito
            WHERE UPPER(nombre_estado) = 'OBSERVADO' AND activo = TRUE LIMIT 1;

        WHEN 'RECHAZAR' THEN
            SELECT id_tipo_estado_requisito, nombre_estado INTO v_id_nuevo_estado, v_nombre_estado_req
            FROM convocatoria.tipo_estado_requisito
            WHERE UPPER(nombre_estado) = 'RECHAZADO' AND activo = TRUE LIMIT 1;
        ELSE
            RETURN jsonb_build_object('exito', FALSE, 'mensaje', 'Acción no permitida');
        END CASE;

    -- 4. Actualizar el Requisito Adjunto
    UPDATE postulacion.requisito_adjunto
    SET id_tipo_estado_requisito = v_id_nuevo_estado,
        observacion = CASE WHEN UPPER(p_accion) = 'OBSERVAR' THEN p_observacion ELSE NULL END
    WHERE id_requisito_adjunto = p_id_requisito_adjunto;

    IF UPPER(p_accion) = 'OBSERVAR' THEN
        SELECT id_tipo_estado_postulacion INTO v_id_estado_postulacion_observada
        FROM postulacion.tipo_estado_postulacion WHERE codigo = 'OBSERVADA';

        IF v_id_estado_postulacion_observada IS NOT NULL THEN
            UPDATE postulacion.postulacion
            SET id_tipo_estado_postulacion = v_id_estado_postulacion_observada
            WHERE id_postulacion = v_id_postulacion;
        END IF;

        INSERT INTO notificacion.notificacion_ws (id_usuario, titulo, mensaje, tipo, id_referencia)
        VALUES (v_id_usuario_estudiante, 'Documento Observado', 'Se ha observado un documento en tu postulación.', 'OBSERVACION', v_id_postulacion);
    END IF;

    SELECT EXISTS (
        SELECT 1 FROM postulacion.requisito_adjunto ra
                          JOIN convocatoria.tipo_estado_requisito ter ON ra.id_tipo_estado_requisito = ter.id_tipo_estado_requisito
        WHERE ra.id_postulacion = v_id_postulacion AND UPPER(ter.nombre_estado) = 'OBSERVADO'
    ) INTO v_tiene_observados;

    SELECT NOT EXISTS (
        SELECT 1 FROM postulacion.requisito_adjunto ra
                          JOIN convocatoria.tipo_estado_requisito ter ON ra.id_tipo_estado_requisito = ter.id_tipo_estado_requisito
        WHERE ra.id_postulacion = v_id_postulacion
          AND UPPER(ter.nombre_estado) NOT IN ('APROBADO', 'VALIDADO')
    ) INTO v_todos_validados;

    RETURN jsonb_build_object(
            'exito', TRUE,
            'mensaje', 'Evaluación registrada',
            'nuevo_estado_documento', v_nombre_estado_req,
            'tiene_observados', v_tiene_observados,
            'todos_validados', v_todos_validados
           );

EXCEPTION WHEN OTHERS THEN
    RETURN jsonb_build_object('exito', FALSE, 'mensaje', 'Error: ' || SQLERRM);
END;
$$ LANGUAGE plpgsql;


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
    -- 1. Validar que el usuario sea un coordinador activo
    SELECT c.id_coordinador, c.id_carrera
    INTO v_id_coordinador, v_id_carrera
    FROM academico.coordinador c
    WHERE c.id_usuario = p_id_usuario AND c.activo = TRUE;

    IF v_id_coordinador IS NULL THEN
        RETURN jsonb_build_object('exito', FALSE, 'mensaje', 'Acceso denegado: El usuario no es un coordinador activo');
    END IF;

    -- 2. Validar existencia de postulación y pertenencia a la carrera
    SELECT car.id_carrera, p.id_estudiante, e.id_usuario
    INTO v_postulacion_carrera, v_id_estudiante, v_id_usuario_estudiante
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

    -- 3. Lógica según la acción (APROBAR o RECHAZAR)
    CASE UPPER(p_accion)
        WHEN 'APROBAR' THEN
            -- Verificamos que no existan documentos que NO estén APROBADOS o VALIDADOS
            SELECT NOT EXISTS (
                SELECT 1 FROM postulacion.requisito_adjunto ra
                                  JOIN convocatoria.tipo_estado_requisito ter ON ra.id_tipo_estado_requisito = ter.id_tipo_estado_requisito
                WHERE ra.id_postulacion = p_id_postulacion
                  AND UPPER(ter.nombre_estado) NOT IN ('APROBADO', 'VALIDADO')
            ) INTO v_todos_validados;

            IF NOT v_todos_validados THEN
                RETURN jsonb_build_object('exito', FALSE, 'mensaje', 'No se puede aprobar: Existen documentos pendientes de validación');
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

    -- 4. Obtener ID del estado desde el catálogo (postulacion.tipo_estado_postulacion)
    SELECT id_tipo_estado_postulacion INTO v_id_nuevo_estado
    FROM postulacion.tipo_estado_postulacion
    WHERE UPPER(codigo) = v_estado_codigo;

    IF v_id_nuevo_estado IS NULL THEN
        RETURN jsonb_build_object('exito', FALSE, 'mensaje', 'Error de configuración: Estado de postulación no encontrado');
    END IF;

    -- 5. ACTUALIZAR POSTULACIÓN
    UPDATE postulacion.postulacion
    SET id_tipo_estado_postulacion = v_id_nuevo_estado,
        -- ELIMINADO: estado_postulacion (ya no existe en tabla física)
        observaciones = COALESCE(p_observacion, observaciones)
    WHERE id_postulacion = p_id_postulacion;

    -- 6. NOTIFICAR AL ESTUDIANTE
    IF v_id_usuario_estudiante IS NOT NULL THEN
        INSERT INTO notificacion.notificacion_ws (id_usuario, titulo, mensaje, tipo, id_referencia)
        VALUES (
                   v_id_usuario_estudiante,
                   CASE WHEN v_estado_codigo = 'APROBADA' THEN 'Postulación Aprobada' ELSE 'Postulación Rechazada' END,
                   CASE WHEN v_estado_codigo = 'APROBADA'
                            THEN '¡Felicitaciones! Tu postulación ha sido aprobada.'
                        ELSE 'Tu postulación ha sido rechazada. Motivo: ' || p_observacion END,
                   CASE WHEN v_estado_codigo = 'APROBADA' THEN 'APROBACION' ELSE 'RECHAZO' END,
                   p_id_postulacion
               );
    END IF;

    RETURN jsonb_build_object(
            'exito', TRUE,
            'mensaje', 'Dictamen registrado como ' || v_estado_codigo,
            'id_postulacion', p_id_postulacion,
            'nuevo_estado', v_estado_codigo
           );

EXCEPTION WHEN OTHERS THEN
    RETURN jsonb_build_object('exito', FALSE, 'mensaje', 'Error: ' || SQLERRM);
END;
$$ LANGUAGE plpgsql;


CREATE OR REPLACE FUNCTION postulacion.fn_cambiar_estado_postulacion_revision(
    p_id_usuario INTEGER,
    p_id_postulacion INTEGER
)
    RETURNS JSONB AS $$
DECLARE
    v_id_coordinador INTEGER;
    v_id_carrera INTEGER;
    v_postulacion_carrera INTEGER;
    v_estado_actual_codigo VARCHAR;
    v_id_estado_revision INTEGER;
BEGIN
    -- 1. Validar que el usuario sea un coordinador activo
    SELECT c.id_coordinador, c.id_carrera
    INTO v_id_coordinador, v_id_carrera
    FROM academico.coordinador c
    WHERE c.id_usuario = p_id_usuario
      AND c.activo = TRUE;

    IF v_id_coordinador IS NULL THEN
        RETURN jsonb_build_object(
                'exito', FALSE,
                'mensaje', 'Acceso denegado: El usuario no es un coordinador activo'
               );
    END IF;

    -- 2. Obtener información de la postulación (Carrera y Estado actual)
    SELECT a.id_carrera, tep.codigo
    INTO v_postulacion_carrera, v_estado_actual_codigo
    FROM postulacion.postulacion p
             JOIN convocatoria.convocatoria cv ON p.id_convocatoria = cv.id_convocatoria
             JOIN academico.asignatura a ON cv.id_asignatura = a.id_asignatura
             LEFT JOIN postulacion.tipo_estado_postulacion tep ON p.id_tipo_estado_postulacion = tep.id_tipo_estado_postulacion
    WHERE p.id_postulacion = p_id_postulacion;

    IF v_postulacion_carrera IS NULL THEN
        RETURN jsonb_build_object(
                'exito', FALSE,
                'mensaje', 'La postulación no existe'
               );
    END IF;

    -- 3. Validar pertenencia a la carrera
    IF v_postulacion_carrera != v_id_carrera THEN
        RETURN jsonb_build_object(
                'exito', FALSE,
                'mensaje', 'No tiene permisos sobre esta postulación'
               );
    END IF;

    -- 4. Lógica de transición: Solo cambiar si está en estados iniciales
    -- Se incluye 'CORREGIDA' porque si el estudiante subsanó, vuelve a estar lista para revisar
    IF v_estado_actual_codigo NOT IN ('PENDIENTE', 'CORREGIDO') THEN
        RETURN jsonb_build_object(
                'exito', TRUE,
                'mensaje', 'No se requiere cambio. Estado actual: ' || COALESCE(v_estado_actual_codigo, 'SIN ESTADO'),
                'cambio_realizado', FALSE
               );
    END IF;

    -- 5. Obtener el ID del estado 'EN_REVISION' del catálogo
    SELECT id_tipo_estado_postulacion INTO v_id_estado_revision
    FROM postulacion.tipo_estado_postulacion
    WHERE codigo = 'EN_REVISION';

    IF v_id_estado_revision IS NULL THEN
        RETURN jsonb_build_object(
                'exito', FALSE,
                'mensaje', 'Error de configuración: No existe el estado EN_REVISION'
               );
    END IF;

    -- 6. ACTUALIZAR POSTULACIÓN
    UPDATE postulacion.postulacion
    SET id_tipo_estado_postulacion = v_id_estado_revision
    WHERE id_postulacion = p_id_postulacion;

    RETURN jsonb_build_object(
            'exito', TRUE,
            'mensaje', 'Estado actualizado a EN_REVISION',
            'estado_anterior', v_estado_actual_codigo,
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