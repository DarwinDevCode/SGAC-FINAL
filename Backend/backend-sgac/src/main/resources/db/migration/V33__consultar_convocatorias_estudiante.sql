DROP FUNCTION IF EXISTS seguridad.fn_validar_contexto_estudiante(INTEGER);
CREATE OR REPLACE FUNCTION seguridad.fn_validar_contexto_estudiante(
    p_id_usuario INTEGER,
    OUT p_id_estudiante INTEGER,
    OUT p_es_valido BOOLEAN,
    OUT p_mensaje TEXT
)
    LANGUAGE plpgsql
AS $$
DECLARE
    v_tiene_rol BOOLEAN := FALSE;
BEGIN
    p_es_valido := FALSE;
    p_id_estudiante := NULL;

    SELECT EXISTS(
        SELECT 1
        FROM seguridad.usuario_tipo_rol utr
                 INNER JOIN seguridad.tipo_rol tr ON tr.id_tipo_rol = utr.id_tipo_rol
        WHERE utr.id_usuario = p_id_usuario
          AND UPPER(tr.nombre_tipo_rol) = 'ESTUDIANTE'
          AND tr.activo = TRUE
    ) INTO v_tiene_rol;

    IF NOT v_tiene_rol THEN
        p_mensaje := 'Aviso: El usuario no tiene rol de estudiante asignado';
        RETURN;
    END IF;

    SELECT e.id_estudiante
    INTO p_id_estudiante
    FROM academico.estudiante e
    WHERE e.id_usuario = p_id_usuario;

    IF p_id_estudiante IS NULL THEN
        p_mensaje := 'Aviso: No existe registro de estudiante para este usuario';
        RETURN;
    END IF;

    p_es_valido := TRUE;
    p_mensaje := 'Validación exitosa';

EXCEPTION WHEN OTHERS THEN
    p_es_valido := FALSE;
    p_id_estudiante := NULL;
    p_mensaje := 'ERROR SISTEMA [' || SQLSTATE || ']: ' || SQLERRM;
END;
$$;

-- ==================================================================================

DROP FUNCTION IF EXISTS academico.fn_verificar_elegibilidad_academica(integer);
CREATE OR REPLACE FUNCTION academico.fn_verificar_elegibilidad_academica(
    p_id_estudiante integer,
    OUT p_es_elegible boolean,
    OUT p_mensaje text
) RETURNS record LANGUAGE plpgsql AS $$
DECLARE
    v_semestre_actual INTEGER;
    v_semestre_minimo INTEGER := 6;
BEGIN
    p_es_elegible := FALSE;

    SELECT e.semestre INTO v_semestre_actual
    FROM academico.estudiante e
    WHERE e.id_estudiante = p_id_estudiante;

    IF v_semestre_actual IS NULL THEN
        p_mensaje := 'Aviso: No se encontró información académica del estudiante o el estudiante no existe.';
        RETURN;
    END IF;

    -- Validar semestre mínimo
    IF v_semestre_actual < v_semestre_minimo THEN
        p_mensaje := 'Requisito no cumplido: Debes estar en 6to semestre o superior (Nivel actual: ' || v_semestre_actual || ')';
        RETURN;
    END IF;

    p_es_elegible := TRUE;
    p_mensaje := 'Estudiante elegible para postulación';

EXCEPTION WHEN OTHERS THEN
    p_es_elegible := FALSE;
    p_mensaje := 'ERROR SISTEMA en validación académica: ' || SQLERRM;
END;
$$;



DROP FUNCTION IF EXISTS convocatoria.fn_listar_convocatorias_estudiante(INTEGER);
CREATE OR REPLACE FUNCTION convocatoria.fn_listar_convocatorias_estudiante(
    p_id_usuario INTEGER
)
    RETURNS TABLE(
                     id_convocatoria INTEGER,
                     nombre_asignatura VARCHAR,
                     semestre_asignatura INTEGER,
                     nombre_carrera VARCHAR,
                     nombre_docente VARCHAR,
                     cupos_disponibles INTEGER,
                     fecha_inicio_postulacion DATE,
                     fecha_fin_postulacion DATE,
                     estado_convocatoria VARCHAR,
                     puede_postular BOOLEAN
                 )
    LANGUAGE plpgsql
    STABLE
AS $$
DECLARE
    v_id_estudiante INTEGER;
    v_id_carrera INTEGER;
    v_semestre_estudiante INTEGER;
    v_semestre_minimo CONSTANT INTEGER := 6;
BEGIN
    SELECT e.id_estudiante, e.id_carrera, e.semestre
    INTO v_id_estudiante, v_id_carrera, v_semestre_estudiante
    FROM seguridad.fn_identidad_usuario(p_id_usuario) u
             JOIN academico.estudiante e ON e.id_estudiante = u.id_rol_especifico
    WHERE u.nombre_rol = 'ESTUDIANTE'
    LIMIT 1;

    IF v_id_estudiante IS NULL THEN
        RAISE EXCEPTION 'Acceso denegado: El usuario no es un estudiante activo.';
    END IF;

    IF v_semestre_estudiante < v_semestre_minimo THEN
        RAISE EXCEPTION 'Requisito no cumplido: Debes estar en 6to semestre o superior.';
    END IF;

    RETURN QUERY
        WITH ventana_postulacion AS (
            SELECT
                pa.id_periodo_academico,
                MAX(CASE WHEN tf.codigo = 'POSTULACION' THEN pf.fecha_inicio END) as inicio,
                MAX(CASE WHEN tf.codigo = 'POSTULACION' THEN pf.fecha_fin END) as fin
            FROM academico.periodo_academico pa
                     JOIN planificacion.periodo_fase pf ON pf.id_periodo_academico = pa.id_periodo_academico
                     JOIN planificacion.tipo_fase tf ON tf.id_tipo_fase = pf.id_tipo_fase
            WHERE pa.activo = TRUE
              AND pa.estado = 'EN PROCESO'
            GROUP BY pa.id_periodo_academico
        )
        SELECT
            c.id_convocatoria,
            a.nombre_asignatura::VARCHAR,
            a.semestre::INTEGER,
            ca.nombre_carrera::VARCHAR,
            (u.nombres || ' ' || u.apellidos)::VARCHAR AS nombre_docente,
            c.cupos_disponibles,
            vp.inicio::DATE      AS fecha_inicio_postulacion,
            vp.fin::DATE         AS fecha_fin_postulacion,

            CASE
                WHEN CURRENT_DATE < vp.inicio THEN 'SE HABILITARÁ PROXIMAMENTE'
                WHEN CURRENT_DATE BETWEEN vp.inicio AND vp.fin THEN 'ABIERTA'
                ELSE 'FINALIZADA'
                END::VARCHAR AS estado_convocatoria,

            COALESCE((CURRENT_DATE BETWEEN vp.inicio AND vp.fin), FALSE)::BOOLEAN AS puede_postular

        FROM convocatoria.convocatoria c
                 INNER JOIN ventana_postulacion vp       ON vp.id_periodo_academico = c.id_periodo_academico
                 INNER JOIN academico.asignatura a       ON a.id_asignatura = c.id_asignatura
                 INNER JOIN academico.carrera ca         ON ca.id_carrera = a.id_carrera
                 INNER JOIN academico.docente d          ON d.id_docente = c.id_docente
                 INNER JOIN seguridad.usuario u          ON u.id_usuario = d.id_usuario

        WHERE c.activo = TRUE
          AND (c.estado = 'PUBLICADA' OR c.estado = 'ABIERTA')
          AND ca.id_carrera = v_id_carrera
          AND a.semestre < v_semestre_estudiante

        ORDER BY a.semestre ASC, a.nombre_asignatura ASC;

EXCEPTION WHEN OTHERS THEN
    RAISE EXCEPTION 'Error al listar convocatorias: %', SQLERRM;
END;
$$;
