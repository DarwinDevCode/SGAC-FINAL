-- Corrige 42804: "structure of query does not match function result type"
-- (tipos OUT deben coincidir exactamente con lo que devuelve el SELECT).

DROP FUNCTION IF EXISTS ayudantia.fn_listar_sesiones(integer, date, date, character varying) CASCADE;
DROP FUNCTION IF EXISTS ayudantia.fn_listar_sesiones(integer, date, date, character varying, integer) CASCADE;

-- 5 argumentos: 1º = id_usuario (SesionServiceImpl / informes IA)

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
    codigo_estado TEXT,
    nombre_estado TEXT,
    observaciones TEXT
)
LANGUAGE plpgsql
STABLE
AS $$
BEGIN
    RETURN QUERY
    SELECT
        ra.id_registro_actividad::integer,
        ra.descripcion_actividad::text,
        ra.tema_tratado::text,
        ra.fecha::date,
        COALESCE(
            (SELECT (COUNT(*))::integer
             FROM ayudantia.detalle_asistencia_actividad daa
             WHERE daa.id_registro_actividad = ra.id_registro_actividad),
            0
        )::integer,
        ra.horas_dedicadas::numeric,
        ra.id_tipo_estado_registro::integer,
        ter.codigo::text,
        ter.nombre_estado::text,
        ra.observaciones::text
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
    'Lista sesiones por id_usuario (filtros opcionales).';

-- 4 argumentos: 1º = id_ayudantia (SesionRepository JdbcTemplate)

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
    lugar TEXT,
    descripcion_actividad TEXT,
    observaciones TEXT,
    fecha_observacion DATE,
    codigo_estado TEXT,
    nombre_estado TEXT
)
LANGUAGE plpgsql
STABLE
AS $$
BEGIN
    RETURN QUERY
    SELECT
        ra.id_registro_actividad::integer,
        ra.fecha::date,
        ra.hora_inicio::time without time zone,
        ra.hora_fin::time without time zone,
        ra.horas_dedicadas::numeric,
        ra.tema_tratado::text,
        ra.lugar::text,
        ra.descripcion_actividad::text,
        ra.observaciones::text,
        ra.fecha_observacion::date,
        ter.codigo::text,
        ter.nombre_estado::text
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
    'Lista sesiones por id_ayudantia.';

GRANT EXECUTE ON FUNCTION ayudantia.fn_listar_sesiones(INTEGER, DATE, DATE, CHARACTER VARYING, INTEGER) TO role_ayudante_catedra;
GRANT EXECUTE ON FUNCTION ayudantia.fn_listar_sesiones(INTEGER, DATE, DATE, CHARACTER VARYING, INTEGER) TO app_user_default;
GRANT EXECUTE ON FUNCTION ayudantia.fn_listar_sesiones(INTEGER, DATE, DATE, CHARACTER VARYING) TO role_ayudante_catedra;
GRANT EXECUTE ON FUNCTION ayudantia.fn_listar_sesiones(INTEGER, DATE, DATE, CHARACTER VARYING) TO app_user_default;
