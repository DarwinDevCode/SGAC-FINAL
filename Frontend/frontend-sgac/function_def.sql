CREATE OR REPLACE FUNCTION postulacion.fn_listar_postulaciones_coordinador(p_id_usuario integer)
 RETURNS TABLE(id_postulacion integer, id_convocatoria integer, id_estudiante integer, nombre_estudiante character varying, matricula character varying, semestre integer, nombre_asignatura character varying, nombre_carrera character varying, fecha_postulacion date, estado_codigo character varying, estado_nombre character varying, requiere_atencion boolean, total_documentos bigint, documentos_pendientes bigint, documentos_aprobados bigint, documentos_observados bigint, observaciones character varying)
 LANGUAGE plpgsql
AS $function$
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
            (tep.codigo IN ('PENDIENTE', 'CORREGIDA'))::BOOLEAN AS requiere_atencion,
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
                           AND UPPER(ter.codigo) = 'PENDIENTE'
                     ), 0)::BIGINT AS documentos_pendientes,
            COALESCE((
                         SELECT COUNT(*)
                         FROM postulacion.requisito_adjunto ra
                                  JOIN convocatoria.tipo_estado_requisito ter ON ra.id_tipo_estado_requisito = ter.id_tipo_estado_requisito
                         WHERE ra.id_postulacion = p.id_postulacion
                           AND UPPER(ter.codigo) IN ('APROBADO', 'VALIDADO')
                     ), 0)::BIGINT AS documentos_aprobados,
            COALESCE((
                         SELECT COUNT(*)
                         FROM postulacion.requisito_adjunto ra
                                  JOIN convocatoria.tipo_estado_requisito ter ON ra.id_tipo_estado_requisito = ter.id_tipo_estado_requisito
                         WHERE ra.id_postulacion = p.id_postulacion
                           AND UPPER(ter.codigo) = 'OBSERVADO'
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
$function$
