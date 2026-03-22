CREATE OR REPLACE FUNCTION ayudantia.fn_detalle_sesion(p_id_usuario integer, p_id_registro integer)
 RETURNS TABLE(id_registro integer, fecha date, tema_tratado text, descripcion text, numero_asistentes integer, horas_dedicadas numeric, estado character varying, nombre_asignatura character varying, nombre_docente text, nombre_periodo character varying)
 LANGUAGE plpgsql
AS $function$
DECLARE
    v_id_ayudantia INTEGER;
BEGIN
    v_id_ayudantia := ayudantia.fn_obtener_id_ayudantia(p_id_usuario);

    IF v_id_ayudantia IS NULL THEN
        RETURN;
    END IF;

    RETURN QUERY
    SELECT
        ra.id_registro_actividad,
        ra.fecha,
        ra.tema_tratado,
        ra.descripcion_actividad,
        ra.numero_asistentes,
        ra.horas_dedicadas,
        ter.nombre_estado,
        asi.nombre_asignatura,
        (u.nombres || ' ' || u.apellidos),
        pa.nombre_periodo
    FROM ayudantia.registro_actividad   ra
    JOIN ayudantia.tipo_estado_registro ter ON ter.id_tipo_estado_registro = ra.id_tipo_estado_registro
    JOIN ayudantia.ayudantia             a  ON a.id_ayudantia    = ra.id_ayudantia
    JOIN postulacion.postulacion         pp ON pp.id_postulacion = a.id_postulacion
    JOIN convocatoria.convocatoria       co ON co.id_convocatoria = pp.id_convocatoria
    JOIN academico.asignatura           asi ON asi.id_asignatura  = co.id_asignatura
    JOIN academico.docente               d  ON d.id_docente       = co.id_docente
    JOIN seguridad.usuario               u  ON u.id_usuario       = d.id_usuario
    JOIN academico.periodo_academico     pa ON pa.id_periodo_academico = co.id_periodo_academico
    WHERE ra.id_ayudantia     = v_id_ayudantia
      AND ra.id_registro_actividad = p_id_registro;
END;
$function$
