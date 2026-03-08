CREATE OR REPLACE FUNCTION seguridad.fn_validar_contexto_estudiante(
    _id_usuario INTEGER,
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
        WHERE utr.id_usuario = _id_usuario
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
    WHERE e.id_usuario = _id_usuario;

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

CREATE OR REPLACE FUNCTION academico.fn_verificar_elegibilidad_academica(
    _id_estudiante INTEGER,
    OUT p_es_elegible BOOLEAN,
    OUT p_mensaje TEXT
)
    LANGUAGE plpgsql
AS $$
DECLARE
    v_semestre_actual INTEGER;
    v_semestre_minimo CONSTANT INTEGER := 6;
BEGIN
    p_es_elegible := FALSE;

    -- Obtener semestre actual del estudiante
    SELECT e.semestre
    INTO v_semestre_actual
    FROM academico.estudiante e
    WHERE e.id_estudiante = _id_estudiante;

    IF v_semestre_actual IS NULL THEN
        p_mensaje := 'Aviso: No se encontró información académica del estudiante';
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

-- ==================================================================================

CREATE OR REPLACE FUNCTION convocatoria.fn_listar_convocatorias_estudiante(
    _id_usuario INTEGER
)
    RETURNS TABLE(
                     id_convocatoria INTEGER,
                     nombre_asignatura VARCHAR,
                     semestre_asignatura INTEGER,
                     nombre_carrera VARCHAR,
                     nombre_docente VARCHAR,
                     cupos_disponibles INTEGER,
                     fecha_publicacion DATE,
                     fecha_cierre DATE,
                     estado VARCHAR
                 )
    LANGUAGE plpgsql
AS $$
DECLARE
    v_id_estudiante INTEGER;
    v_es_valido BOOLEAN;
    v_mensaje TEXT;
    v_es_elegible BOOLEAN;
    v_id_carrera INTEGER;
    v_semestre_estudiante INTEGER;
BEGIN
    -- Paso 1: Validación de identidad
    SELECT * INTO v_id_estudiante, v_es_valido, v_mensaje
    FROM seguridad.fn_validar_contexto_estudiante(_id_usuario);

    IF NOT v_es_valido THEN
        RAISE EXCEPTION '%', v_mensaje;
    END IF;

    -- Paso 2: Validación académica
    SELECT * INTO v_es_elegible, v_mensaje
    FROM academico.fn_verificar_elegibilidad_academica(v_id_estudiante);

    IF NOT v_es_elegible THEN
        RAISE EXCEPTION '%', v_mensaje;
    END IF;

    -- Obtener datos del estudiante para filtros
    SELECT e.id_carrera, e.semestre
    INTO v_id_carrera, v_semestre_estudiante
    FROM academico.estudiante e
    WHERE e.id_estudiante = v_id_estudiante;

    -- Paso 3: Consulta de convocatorias filtradas
    RETURN QUERY
        SELECT
            c.id_convocatoria,
            a.nombre_asignatura,
            a.semestre AS semestre_asignatura,
            ca.nombre_carrera,
            CONCAT(u.nombres, ' ', u.apellidos)::VARCHAR AS nombre_docente,
            c.cupos_disponibles,
            c.fecha_publicacion,
            c.fecha_cierre,
            c.estado
        FROM convocatoria.convocatoria c
                 INNER JOIN academico.asignatura a ON a.id_asignatura = c.id_asignatura
                 INNER JOIN academico.carrera ca ON ca.id_carrera = a.id_carrera
                 INNER JOIN academico.docente d ON d.id_docente = c.id_docente
                 INNER JOIN seguridad.usuario u ON u.id_usuario = d.id_usuario
        WHERE c.activo = TRUE
          AND c.estado = 'ABIERTA'
          AND a.id_carrera = v_id_carrera
          AND a.semestre < v_semestre_estudiante
          AND c.fecha_cierre >= CURRENT_DATE
        ORDER BY c.fecha_cierre ASC;

EXCEPTION WHEN OTHERS THEN
    RAISE EXCEPTION 'ERROR SISTEMA [%]: %', SQLSTATE, SQLERRM;
END;
$$;

