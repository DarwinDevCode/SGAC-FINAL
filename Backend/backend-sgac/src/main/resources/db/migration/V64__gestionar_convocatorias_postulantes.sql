CREATE OR REPLACE FUNCTION postulacion.fn_resolver_sala_usuario(
    p_id_usuario INTEGER
)
    RETURNS JSONB
    LANGUAGE plpgsql
    SECURITY DEFINER
    STABLE
AS $$
DECLARE
    v_id_conv     INTEGER;
    v_nombre_asig TEXT;
    v_nombre_carr TEXT;
    v_rol         TEXT;
BEGIN
    -- ── Prioridad 1: evaluación EN_CURSO en su comisión ───────────────
    SELECT
        c.id_convocatoria,
        a.nombre_asignatura,
        ca.nombre_carrera,
        uc.rol_integrante
    INTO v_id_conv, v_nombre_asig, v_nombre_carr, v_rol
    FROM   seguridad.usuario_comision       uc
               JOIN   postulacion.comision_seleccion   cs  ON cs.id_comision_seleccion = uc.id_comision_seleccion
               JOIN   convocatoria.convocatoria         c   ON c.id_convocatoria        = cs.id_convocatoria
               JOIN   academico.asignatura              a   ON a.id_asignatura          = c.id_asignatura
               JOIN   academico.carrera                 ca  ON ca.id_carrera            = a.id_carrera
    WHERE  uc.id_usuario = p_id_usuario
      AND  uc.activo     = TRUE
      AND  cs.activo     = TRUE
      AND  EXISTS (
        SELECT 1
        FROM   postulacion.evaluacion_oposicion  eo
                   JOIN   postulacion.postulacion         p  ON p.id_postulacion = eo.id_postulacion
                   JOIN   postulacion.tipo_estado_evaluacion tee
                          ON tee.id_tipo_estado_evaluacion = eo.id_tipo_estado_evaluacion
        WHERE  p.id_convocatoria = c.id_convocatoria
          AND  tee.codigo        = 'EN_CURSO'
    )
    ORDER BY c.id_convocatoria DESC
    LIMIT 1;

    IF FOUND THEN
        RETURN jsonb_build_object(
                'exito',            true,
                'prioridad',        1,
                'idConvocatoria',   v_id_conv,
                'nombreAsignatura', v_nombre_asig,
                'nombreCarrera',    v_nombre_carr,
                'rolIntegrante',    v_rol,
                'mensaje',          'Sala activa encontrada (evaluación EN_CURSO).'
               );
    END IF;

    -- ── Prioridad 2: evaluación PROGRAMADA en su comisión ─────────────
    SELECT
        c.id_convocatoria,
        a.nombre_asignatura,
        ca.nombre_carrera,
        uc.rol_integrante
    INTO v_id_conv, v_nombre_asig, v_nombre_carr, v_rol
    FROM   seguridad.usuario_comision       uc
               JOIN   postulacion.comision_seleccion   cs  ON cs.id_comision_seleccion = uc.id_comision_seleccion
               JOIN   convocatoria.convocatoria         c   ON c.id_convocatoria        = cs.id_convocatoria
               JOIN   academico.asignatura              a   ON a.id_asignatura          = c.id_asignatura
               JOIN   academico.carrera                 ca  ON ca.id_carrera            = a.id_carrera
    WHERE  uc.id_usuario = p_id_usuario
      AND  uc.activo     = TRUE
      AND  cs.activo     = TRUE
      AND  EXISTS (
        SELECT 1
        FROM   postulacion.evaluacion_oposicion  eo
                   JOIN   postulacion.postulacion         p  ON p.id_postulacion = eo.id_postulacion
                   JOIN   postulacion.tipo_estado_evaluacion tee
                          ON tee.id_tipo_estado_evaluacion = eo.id_tipo_estado_evaluacion
        WHERE  p.id_convocatoria = c.id_convocatoria
          AND  tee.codigo        = 'PROGRAMADA'
    )
    ORDER BY c.id_convocatoria DESC
    LIMIT 1;

    IF FOUND THEN
        RETURN jsonb_build_object(
                'exito',            true,
                'prioridad',        2,
                'idConvocatoria',   v_id_conv,
                'nombreAsignatura', v_nombre_asig,
                'nombreCarrera',    v_nombre_carr,
                'rolIntegrante',    v_rol,
                'mensaje',          'Sala encontrada (evaluaciones PROGRAMADAS).'
               );
    END IF;

    -- ── Prioridad 3: cualquier comisión activa ────────────────────────
    SELECT
        c.id_convocatoria,
        a.nombre_asignatura,
        ca.nombre_carrera,
        uc.rol_integrante
    INTO v_id_conv, v_nombre_asig, v_nombre_carr, v_rol
    FROM   seguridad.usuario_comision       uc
               JOIN   postulacion.comision_seleccion   cs  ON cs.id_comision_seleccion = uc.id_comision_seleccion
               JOIN   convocatoria.convocatoria         c   ON c.id_convocatoria        = cs.id_convocatoria
               JOIN   academico.asignatura              a   ON a.id_asignatura          = c.id_asignatura
               JOIN   academico.carrera                 ca  ON ca.id_carrera            = a.id_carrera
    WHERE  uc.id_usuario = p_id_usuario
      AND  uc.activo     = TRUE
      AND  cs.activo     = TRUE
    ORDER BY c.id_convocatoria DESC
    LIMIT 1;

    IF FOUND THEN
        RETURN jsonb_build_object(
                'exito',            true,
                'prioridad',        3,
                'idConvocatoria',   v_id_conv,
                'nombreAsignatura', v_nombre_asig,
                'nombreCarrera',    v_nombre_carr,
                'rolIntegrante',    v_rol,
                'mensaje',          'Comisión encontrada (sin evaluaciones activas aún).'
               );
    END IF;

    -- ── Sin comisión asignada ─────────────────────────────────────────
    RETURN jsonb_build_object(
            'exito',   false,
            'mensaje', 'No tienes ninguna comisión de evaluación asignada en el período activo.'
           );

EXCEPTION WHEN OTHERS THEN
    RETURN jsonb_build_object('exito', false, 'mensaje', '[ERROR] ' || SQLERRM);
END;
$$;

GRANT EXECUTE ON FUNCTION postulacion.fn_resolver_sala_usuario(INTEGER)
    TO app_user_default;


CREATE OR REPLACE FUNCTION postulacion.fn_resolver_mi_sala(p_id_usuario INTEGER)
    RETURNS JSONB
    LANGUAGE plpgsql
    SECURITY DEFINER
    STABLE
AS $$
DECLARE
    v_row RECORD;
BEGIN
    SELECT
        cs.id_convocatoria,
        a.nombre_asignatura,
        ca.nombre_carrera
    INTO v_row
    FROM   seguridad.usuario_comision     uc
               JOIN   postulacion.comision_seleccion cs ON cs.id_comision_seleccion = uc.id_comision_seleccion
               JOIN   convocatoria.convocatoria       c  ON c.id_convocatoria       = cs.id_convocatoria
               JOIN   academico.asignatura            a  ON a.id_asignatura         = c.id_asignatura
               JOIN   academico.carrera               ca ON ca.id_carrera           = a.id_carrera
    WHERE  uc.id_usuario = p_id_usuario
      AND  uc.activo     = TRUE
      AND  cs.activo     = TRUE
      AND  c.activo      = TRUE
      -- Solo convocatorias del período académico activo
      AND  EXISTS (
        SELECT 1 FROM academico.periodo_academico pa
        WHERE pa.id_periodo_academico = c.id_periodo_academico
          AND pa.activo = TRUE AND pa.estado = 'EN PROCESO'
    )
    ORDER BY cs.id_comision_seleccion DESC
    LIMIT 1;

    IF NOT FOUND THEN
        RETURN jsonb_build_object(
                'exito',   false,
                'mensaje', 'No tienes ninguna comisión asignada en el período activo.'
               );
    END IF;

    RETURN jsonb_build_object(
            'exito',            true,
            'idConvocatoria',   v_row.id_convocatoria,
            'nombreAsignatura', v_row.nombre_asignatura,
            'nombreCarrera',    v_row.nombre_carrera
           );

EXCEPTION WHEN OTHERS THEN
    RETURN jsonb_build_object('exito', false, 'mensaje', '[ERROR] ' || SQLERRM);
END;
$$;

GRANT EXECUTE ON FUNCTION postulacion.fn_resolver_mi_sala(INTEGER) TO app_user_default;