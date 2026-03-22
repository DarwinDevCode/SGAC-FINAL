-- Sustituye versiones antiguas: CREATE OR REPLACE no puede cambiar columnas OUT de una función ya existente.

DROP FUNCTION IF EXISTS ayudantia.fn_listar_sesiones(integer, date, date, character varying) CASCADE;
DROP FUNCTION IF EXISTS ayudantia.fn_listar_sesiones(integer, date, date, character varying, integer) CASCADE;

-- Firma que espera RegistroActividadConfigRepository / SesionServiceImpl (5 argumentos).
-- El 1er parámetro es id_usuario del estudiante/ayudante, no id_ayudantia.

CREATE OR REPLACE FUNCTION ayudantia.fn_listar_sesiones(
    p_id_usuario INTEGER,
    p_fecha_desde DATE,
    p_fecha_hasta DATE,
    p_estado CHARACTER VARYING,
    p_id_periodo INTEGER
)
RETURNS TABLE (
    id_registro_actividad INTEGER,
    descripcion_actividad TEXT,
    tema_tratado TEXT,
    fecha DATE,
    numero_asistentes INTEGER,
    horas_dedicadas NUMERIC,
    id_tipo_estado_registro INTEGER,
    codigo_estado VARCHAR,
    nombre_estado VARCHAR,
    observaciones TEXT
)
LANGUAGE plpgsql
STABLE
AS $$
BEGIN
    RETURN QUERY
    SELECT
        ra.id_registro_actividad,
        ra.descripcion_actividad,
        ra.tema_tratado,
        ra.fecha,
        (SELECT CAST(COUNT(*) AS INTEGER)
         FROM ayudantia.detalle_asistencia_actividad daa
         WHERE daa.id_registro_actividad = ra.id_registro_actividad) AS numero_asistentes,
        ra.horas_dedicadas,
        ra.id_tipo_estado_registro,
        ter.codigo AS codigo_estado,
        ter.nombre_estado AS nombre_estado,
        ra.observaciones
    FROM ayudantia.registro_actividad ra
    JOIN ayudantia.tipo_estado_registro ter
        ON ter.id_tipo_estado_registro = ra.id_tipo_estado_registro
    JOIN ayudantia.ayudantia a ON a.id_ayudantia = ra.id_ayudantia
    JOIN postulacion.postulacion p ON p.id_postulacion = a.id_postulacion
    JOIN convocatoria.convocatoria c ON c.id_convocatoria = p.id_convocatoria
    JOIN academico.estudiante e ON e.id_estudiante = p.id_estudiante
    WHERE e.id_usuario = p_id_usuario
      AND (p_fecha_desde IS NULL OR ra.fecha >= p_fecha_desde)
      AND (p_fecha_hasta IS NULL OR ra.fecha <= p_fecha_hasta)
      AND (p_estado IS NULL OR ter.codigo = p_estado)
      AND (p_id_periodo IS NULL OR c.id_periodo_academico = p_id_periodo)
    ORDER BY ra.fecha DESC, ra.id_registro_actividad DESC;
END;
$$;

COMMENT ON FUNCTION ayudantia.fn_listar_sesiones(INTEGER, DATE, DATE, CHARACTER VARYING, INTEGER) IS
    'Lista sesiones/actividades del ayudante por id_usuario, con filtros opcionales de fechas, estado y periodo académico.';

-- Sobrecarga usada por SesionRepository (JdbcTemplate): 1er argumento es id_ayudantia.

CREATE OR REPLACE FUNCTION ayudantia.fn_listar_sesiones(
    p_id_ayudantia INTEGER,
    p_fecha_desde DATE,
    p_fecha_hasta DATE,
    p_estado CHARACTER VARYING
)
RETURNS TABLE (
    id_registro_actividad INTEGER,
    fecha DATE,
    hora_inicio TIME,
    hora_fin TIME,
    horas_dedicadas NUMERIC,
    tema_tratado TEXT,
    lugar VARCHAR,
    descripcion_actividad TEXT,
    observaciones VARCHAR,
    fecha_observacion DATE,
    codigo_estado VARCHAR,
    nombre_estado VARCHAR
)
LANGUAGE plpgsql
STABLE
AS $$
BEGIN
    RETURN QUERY
    SELECT
        ra.id_registro_actividad,
        ra.fecha,
        ra.hora_inicio,
        ra.hora_fin,
        ra.horas_dedicadas,
        ra.tema_tratado,
        ra.lugar,
        ra.descripcion_actividad,
        ra.observaciones,
        ra.fecha_observacion,
        ter.codigo AS codigo_estado,
        ter.nombre_estado AS nombre_estado
    FROM ayudantia.registro_actividad ra
    JOIN ayudantia.tipo_estado_registro ter
        ON ter.id_tipo_estado_registro = ra.id_tipo_estado_registro
    WHERE ra.id_ayudantia = p_id_ayudantia
      AND (p_fecha_desde IS NULL OR ra.fecha >= p_fecha_desde)
      AND (p_fecha_hasta IS NULL OR ra.fecha <= p_fecha_hasta)
      AND (p_estado IS NULL OR ter.codigo = p_estado)
    ORDER BY ra.fecha DESC, ra.id_registro_actividad DESC;
END;
$$;

COMMENT ON FUNCTION ayudantia.fn_listar_sesiones(INTEGER, DATE, DATE, CHARACTER VARYING) IS
    'Lista sesiones por id_ayudantia (módulo configuración de sesiones).';

GRANT EXECUTE ON FUNCTION ayudantia.fn_listar_sesiones(INTEGER, DATE, DATE, CHARACTER VARYING, INTEGER) TO role_ayudante_catedra;
GRANT EXECUTE ON FUNCTION ayudantia.fn_listar_sesiones(INTEGER, DATE, DATE, CHARACTER VARYING, INTEGER) TO app_user_default;
GRANT EXECUTE ON FUNCTION ayudantia.fn_listar_sesiones(INTEGER, DATE, DATE, CHARACTER VARYING) TO role_ayudante_catedra;
GRANT EXECUTE ON FUNCTION ayudantia.fn_listar_sesiones(INTEGER, DATE, DATE, CHARACTER VARYING) TO app_user_default;
