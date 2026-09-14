--
-- PostgreSQL database dump
--

\restrict sNdjeJKsTjJgK38zqK46ewOxMYuBQi2PboonzcBDR5DVHkMnEcDFtEZCQfZtKsb

-- Dumped from database version 18.1
-- Dumped by pg_dump version 18.1

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET transaction_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

--
-- Name: academico; Type: SCHEMA; Schema: -; Owner: -
--

CREATE SCHEMA academico;


--
-- Name: ayudantia; Type: SCHEMA; Schema: -; Owner: -
--

CREATE SCHEMA ayudantia;


--
-- Name: convocatoria; Type: SCHEMA; Schema: -; Owner: -
--

CREATE SCHEMA convocatoria;


--
-- Name: notificacion; Type: SCHEMA; Schema: -; Owner: -
--

CREATE SCHEMA notificacion;


--
-- Name: planificacion; Type: SCHEMA; Schema: -; Owner: -
--

CREATE SCHEMA planificacion;


--
-- Name: postulacion; Type: SCHEMA; Schema: -; Owner: -
--

CREATE SCHEMA postulacion;


--
-- Name: public; Type: SCHEMA; Schema: -; Owner: -
--

CREATE SCHEMA public;


--
-- Name: SCHEMA public; Type: COMMENT; Schema: -; Owner: -
--

COMMENT ON SCHEMA public IS 'standard public schema';


--
-- Name: seguridad; Type: SCHEMA; Schema: -; Owner: -
--

CREATE SCHEMA seguridad;


--
-- Name: resultado_validacion; Type: TYPE; Schema: public; Owner: -
--

CREATE TYPE public.resultado_validacion AS (
	valido boolean,
	mensaje text
);


--
-- Name: res_operacion; Type: TYPE; Schema: seguridad; Owner: -
--

CREATE TYPE seguridad.res_operacion AS (
	valido boolean,
	mensaje text,
	datos json
);


--
-- Name: fn_abrir_periodo_academico(character varying, date, date); Type: FUNCTION; Schema: academico; Owner: -
--

CREATE FUNCTION academico.fn_abrir_periodo_academico(p_nombre character varying, p_fecha_inicio date, p_fecha_fin date) RETURNS jsonb
    LANGUAGE plpgsql SECURITY DEFINER
    AS $$
DECLARE
    v_periodo_activo_id     INTEGER;
    v_periodo_activo_fin    DATE;
    v_periodo_activo_nombre VARCHAR;
    v_id_periodo            INTEGER;
    v_fase                  RECORD;
    v_total_fases           INTEGER;
    v_dia_placeholder       DATE;
    v_idx                   INTEGER := 0;
BEGIN
    IF p_fecha_fin <= p_fecha_inicio THEN
        RETURN jsonb_build_object(
                'exito',   false,
                'mensaje', 'La fecha de fin debe ser posterior a la fecha de inicio del período'
               );
    END IF;

    IF TRIM(p_nombre) = '' OR p_nombre IS NULL THEN
        RETURN jsonb_build_object(
                'exito',   false,
                'mensaje', 'El nombre del período académico no puede estar vacío'
               );
    END IF;

    SELECT
        id_periodo_academico,
        fecha_fin,
        nombre_periodo
    INTO
        v_periodo_activo_id,
        v_periodo_activo_fin,
        v_periodo_activo_nombre
    FROM academico.periodo_academico
    WHERE activo = TRUE
    LIMIT 1;

    IF FOUND THEN
        IF CURRENT_DATE <= v_periodo_activo_fin THEN
            RETURN jsonb_build_object(
                    'exito',   false,
                    'mensaje', 'Ya existe el período académico activo "' || v_periodo_activo_nombre
                                   || '" (ID: ' || v_periodo_activo_id || ') que finaliza el '
                                   || to_char(v_periodo_activo_fin, 'DD/MM/YYYY')
                        || '. No es posible abrir un nuevo período hasta que el actual haya concluido.'
                   );
        END IF;
    END IF;

    SELECT COUNT(*)
    INTO   v_total_fases
    FROM   planificacion.tipo_fase
    WHERE  activo = TRUE;

    IF v_total_fases = 0 THEN
        RETURN jsonb_build_object(
                'exito',   false,
                'mensaje', 'No existen tipos de fase activos en planificacion.tipo_fase. '
                    || 'Registre al menos una fase antes de abrir un período académico.'
               );
    END IF;

    IF (p_fecha_fin - p_fecha_inicio) < (v_total_fases - 1) THEN
        RETURN jsonb_build_object(
                'exito',   false,
                'mensaje', 'El rango del período (' || (p_fecha_fin - p_fecha_inicio + 1)
                               || ' día(s)) es insuficiente para instanciar ' || v_total_fases
                               || ' fases con fechas placeholder no solapadas. '
                               || 'Se requieren al menos ' || v_total_fases || ' días.'
               );
    END IF;

    INSERT INTO academico.periodo_academico (
        nombre_periodo,
        fecha_inicio,
        fecha_fin,
        estado,
        activo
    )
    VALUES (
               TRIM(p_nombre),
               p_fecha_inicio,
               p_fecha_fin,
               'PLANIFICACION',
               FALSE
           )
    RETURNING id_periodo_academico INTO v_id_periodo;


    FOR v_fase IN
        SELECT id_tipo_fase, orden, nombre
        FROM   planificacion.tipo_fase
        WHERE  activo = TRUE
        ORDER BY orden ASC
        LOOP
            v_dia_placeholder := p_fecha_inicio + v_idx;

            INSERT INTO planificacion.periodo_fase (
                id_periodo_academico,
                id_tipo_fase,
                fecha_inicio,
                fecha_fin
            )
            VALUES (
                       v_id_periodo,
                       v_fase.id_tipo_fase,
                       v_dia_placeholder,
                       v_dia_placeholder
                   );

            v_idx := v_idx + 1;  -- Avanzar al siguiente día disponible
        END LOOP;

    RETURN jsonb_build_object(
            'exito',   true,
            'mensaje', 'Período académico "' || TRIM(p_nombre) || '" abierto exitosamente '
                           || 'con ' || v_total_fases || ' fase(s) instanciadas en estado PLANIFICACION. '
                || 'Use fn_ajustar_cronograma_lote() para configurar las fechas definitivas.',
            'id',      v_id_periodo
           );

EXCEPTION
    WHEN OTHERS THEN
        RETURN jsonb_build_object(
                'exito',   false,
                'mensaje', 'Error al abrir el período académico: ' || SQLERRM
               );
END;
$$;


--
-- Name: fn_gestionar_carga_docente(integer, integer[]); Type: FUNCTION; Schema: academico; Owner: -
--

CREATE FUNCTION academico.fn_gestionar_carga_docente(p_id_docente integer, p_asignaturas_ids integer[]) RETURNS jsonb
    LANGUAGE plpgsql SECURITY DEFINER
    AS $$
DECLARE
    v_revocadas          INTEGER := 0;
    v_asignadas          INTEGER := 0;
    v_sin_cambio         INTEGER := 0;
    v_asig_id            INTEGER;
    v_nombres_revocados  TEXT[];
    v_nombres_asignados  TEXT[];
    v_correo_docente     TEXT;
    v_nombre_docente     TEXT;
BEGIN
    -- ── Validar que el docente existe y está activo ───────────────
    IF NOT EXISTS (
        SELECT 1 FROM academico.docente
        WHERE id_docente = p_id_docente AND activo = TRUE
    ) THEN
        RAISE EXCEPTION 'El docente con ID % no existe o está inactivo.', p_id_docente;
    END IF;

    -- ── Datos del docente para el resultado ──────────────────────
    SELECT u.correo,
           u.nombres || ' ' || u.apellidos
    INTO v_correo_docente, v_nombre_docente
    FROM academico.docente d
             JOIN seguridad.usuario u ON u.id_usuario = d.id_usuario
    WHERE d.id_docente = p_id_docente;

    -- ── REVOCAR + capturar nombres en una sola operación atómica ─
    --    La CTE con RETURNING garantiza que solo capturamos exactamente
    --    las filas que este UPDATE modificó, sin ambigüedad.
    WITH revocadas_cte AS (
        UPDATE academico.docente_asignatura
            SET activo = FALSE
            WHERE id_docente   = p_id_docente
                AND activo       = TRUE
                AND id_asignatura <> ALL(COALESCE(p_asignaturas_ids, ARRAY[]::INTEGER[]))
            RETURNING id_asignatura
    )
    SELECT
        COUNT(*)::INTEGER,
        ARRAY_AGG(a.nombre_asignatura ORDER BY a.nombre_asignatura)
    INTO v_revocadas, v_nombres_revocados
    FROM revocadas_cte rc
             JOIN academico.asignatura a ON a.id_asignatura = rc.id_asignatura;

    -- ── ASIGNAR: insertar o reactivar las nuevas ──────────────────
    IF p_asignaturas_ids IS NOT NULL THEN
        FOREACH v_asig_id IN ARRAY p_asignaturas_ids
            LOOP
                IF NOT EXISTS (
                    SELECT 1 FROM academico.asignatura WHERE id_asignatura = v_asig_id
                ) THEN
                    RAISE EXCEPTION 'La asignatura con ID % no existe.', v_asig_id;
                END IF;

                -- Upsert: inserta nueva o reactiva si estaba inactiva.
                -- Si ya estaba activa (sin cambio), el DO UPDATE no dispara
                -- porque la condición WHERE es falsa → FOUND = FALSE.
                INSERT INTO academico.docente_asignatura (id_docente, id_asignatura, activo)
                VALUES (p_id_docente, v_asig_id, TRUE)
                ON CONFLICT (id_docente, id_asignatura)
                    DO UPDATE
                    SET activo = TRUE
                WHERE academico.docente_asignatura.activo = FALSE;

                IF FOUND THEN
                    v_asignadas := v_asignadas + 1;
                ELSE
                    v_sin_cambio := v_sin_cambio + 1;
                END IF;
            END LOOP;
    END IF;

    -- ── Capturar carga final activa (estado post-operación) ───────
    SELECT ARRAY_AGG(a.nombre_asignatura ORDER BY a.nombre_asignatura)
    INTO v_nombres_asignados
    FROM academico.docente_asignatura da
             JOIN academico.asignatura a ON a.id_asignatura = da.id_asignatura
    WHERE da.id_docente = p_id_docente
      AND da.activo     = TRUE;

    RAISE NOTICE '[fn_gestionar_carga_docente] Docente %: % revocadas, % asignadas, % sin cambio.',
        p_id_docente, v_revocadas, v_asignadas, v_sin_cambio;

    RETURN jsonb_build_object(
            'exito',               TRUE,
            'idDocente',           p_id_docente,
            'nombreDocente',       v_nombre_docente,
            'correoDocente',       v_correo_docente,
            'revocadas',           v_revocadas,
            'asignadas',           v_asignadas,
            'sinCambio',           v_sin_cambio,
            'asignaturasActuales', COALESCE(to_jsonb(v_nombres_asignados), '[]'::JSONB),
            'asignaturasRevocadas',COALESCE(to_jsonb(v_nombres_revocados), '[]'::JSONB)
           );

EXCEPTION
    WHEN OTHERS THEN
        RAISE EXCEPTION 'Error en fn_gestionar_carga_docente: % (SQLSTATE: %)', SQLERRM, SQLSTATE;
END;
$$;


--
-- Name: FUNCTION fn_gestionar_carga_docente(p_id_docente integer, p_asignaturas_ids integer[]); Type: COMMENT; Schema: academico; Owner: -
--

COMMENT ON FUNCTION academico.fn_gestionar_carga_docente(p_id_docente integer, p_asignaturas_ids integer[]) IS 'Sincronización atómica de carga académica. Recibe el estado FINAL deseado y aplica revocaciones + asignaciones en una sola TX.';


--
-- Name: fn_get_asignaturas_por_carrera(integer); Type: FUNCTION; Schema: academico; Owner: -
--

CREATE FUNCTION academico.fn_get_asignaturas_por_carrera(p_id_carrera integer) RETURNS seguridad.res_operacion
    LANGUAGE plpgsql
    AS $$
DECLARE
    v_datos json;
BEGIN
    SELECT json_agg(t)
    INTO v_datos
    FROM (
             SELECT id_asignatura AS id,
                    nombre_asignatura AS nombre,
                    semestre
             FROM academico.asignatura
             WHERE id_carrera = p_id_carrera
               AND activo = true
             ORDER BY semestre, nombre_asignatura ASC
         ) t;

    RETURN (true, 'Asignaturas obtenidas exitosamente', COALESCE(v_datos, '[]'::json));

EXCEPTION
    WHEN OTHERS THEN
        RETURN (false, 'Error al obtener asignaturas: ' || SQLERRM, NULL);
END;
$$;


--
-- Name: fn_get_carreras_activas(integer); Type: FUNCTION; Schema: academico; Owner: -
--

CREATE FUNCTION academico.fn_get_carreras_activas(p_id_facultad integer) RETURNS seguridad.res_operacion
    LANGUAGE plpgsql
    AS $$
DECLARE
    v_datos json;
BEGIN
    IF p_id_facultad IS NULL THEN
        RETURN (false, 'El ID de la facultad es requerido'::text, NULL)::seguridad.res_operacion;
    END IF;

    SELECT json_agg(t)
    INTO v_datos
    FROM (
             SELECT id_carrera AS id,
                    nombre_carrera AS nombre
             FROM academico.carrera
             WHERE id_facultad = p_id_facultad
               AND activo = true
             ORDER BY nombre_carrera ASC
         ) t;

    RETURN (true, 'Carreras obtenidas exitosamente'::text, COALESCE(v_datos, '[]'::json))::seguridad.res_operacion;

EXCEPTION
    WHEN OTHERS THEN
        RETURN (false, 'Error al obtener carreras: ' || SQLERRM, NULL)::seguridad.res_operacion;
END;
$$;


--
-- Name: fn_get_facultades_activas(); Type: FUNCTION; Schema: academico; Owner: -
--

CREATE FUNCTION academico.fn_get_facultades_activas() RETURNS seguridad.res_operacion
    LANGUAGE plpgsql
    AS $$
DECLARE
    v_datos json;
BEGIN
    SELECT json_agg(t)
    INTO v_datos
    FROM (
             SELECT id_facultad AS id,
                    nombre_facultad AS nombre
             FROM academico.facultad
             WHERE activo = true
             ORDER BY nombre_facultad ASC
         ) t;

    RETURN (true, 'Facultades obtenidas exitosamente'::text, COALESCE(v_datos, '[]'::json))::seguridad.res_operacion;

EXCEPTION
    WHEN OTHERS THEN
        RETURN (false, 'Error al obtener facultades: ' || SQLERRM, NULL)::seguridad.res_operacion;
END;
$$;


--
-- Name: fn_iniciar_periodo_academico(integer); Type: FUNCTION; Schema: academico; Owner: -
--

CREATE FUNCTION academico.fn_iniciar_periodo_academico(p_id_periodo integer) RETURNS jsonb
    LANGUAGE plpgsql SECURITY DEFINER
    AS $$
DECLARE
    v_periodo        RECORD;
    v_activo_nombre  VARCHAR;
    v_activo_fin     DATE;
    v_total_fases    INTEGER;
BEGIN
    SELECT
        id_periodo_academico,
        nombre_periodo,
        fecha_inicio,
        fecha_fin,
        estado,
        activo
    INTO v_periodo
    FROM academico.periodo_academico
    WHERE id_periodo_academico = p_id_periodo;

    IF NOT FOUND THEN
        RETURN jsonb_build_object(
                'exito',   false,
                'mensaje', 'No se encontró el período académico con ID ' || p_id_periodo
               );
    END IF;

    IF v_periodo.estado <> 'CONFIGURADO' THEN
        RETURN jsonb_build_object(
                'exito',   false,
                'mensaje', 'El período "' || v_periodo.nombre_periodo || '" tiene estado "'
                               || v_periodo.estado || '". Solo los períodos en estado CONFIGURADO '
                    || 'pueden iniciarse. Configure el cronograma antes de iniciar.'
               );
    END IF;

    SELECT nombre_periodo, fecha_fin
    INTO   v_activo_nombre, v_activo_fin
    FROM   academico.periodo_academico
    WHERE  activo = TRUE
      AND  id_periodo_academico <> p_id_periodo
      AND  CURRENT_DATE <= fecha_fin
    LIMIT 1;

    IF FOUND THEN
        RETURN jsonb_build_object(
                'exito',   false,
                'mensaje', 'Ya existe el período activo "' || v_activo_nombre
                               || '" que finaliza el ' || to_char(v_activo_fin, 'DD/MM/YYYY')
                    || '. No se puede iniciar un nuevo período hasta que el actual concluya.'
               );
    END IF;

    SELECT COUNT(*)
    INTO   v_total_fases
    FROM   planificacion.periodo_fase
    WHERE  id_periodo_academico = p_id_periodo;

    IF v_total_fases = 0 THEN
        RETURN jsonb_build_object(
                'exito',   false,
                'mensaje', 'El período no tiene fases en el cronograma. '
                    || 'Configure el cronograma antes de iniciar el período.'
               );
    END IF;

    UPDATE academico.periodo_academico
    SET    activo = FALSE
    WHERE  activo = TRUE
      AND  id_periodo_academico <> p_id_periodo;

    UPDATE academico.periodo_academico
    SET
        estado = 'EN PROCESO',
        activo = TRUE
    WHERE id_periodo_academico = p_id_periodo;

    RETURN jsonb_build_object(
            'exito',   true,
            'mensaje', 'El período académico "' || v_periodo.nombre_periodo
                || '" ha sido iniciado exitosamente. Estado: EN PROCESO.',
            'id',      p_id_periodo
           );

EXCEPTION
    WHEN OTHERS THEN
        RETURN jsonb_build_object(
                'exito',   false,
                'mensaje', 'Error al iniciar el período académico: ' || SQLERRM
               );
END;
$$;


--
-- Name: fn_listar_asignaturas_docente(integer); Type: FUNCTION; Schema: academico; Owner: -
--

CREATE FUNCTION academico.fn_listar_asignaturas_docente(p_id_docente integer) RETURNS jsonb
    LANGUAGE sql STABLE SECURITY DEFINER
    AS $$
SELECT COALESCE(
               jsonb_agg(
                       jsonb_build_object(
                               'idAsignatura',     a.id_asignatura,
                               'nombreAsignatura', a.nombre_asignatura,
                               'semestre',         a.semestre,
                               'idCarrera',        c.id_carrera,
                               'nombreCarrera',    c.nombre_carrera,
                               'idFacultad',       f.id_facultad,
                               'nombreFacultad',   f.nombre_facultad,
                               'etiqueta',         a.nombre_asignatura
                                                       || ' · ' || c.nombre_carrera
                                                       || ' · ' || f.nombre_facultad
                       )
                       ORDER BY f.nombre_facultad, c.nombre_carrera, a.semestre, a.nombre_asignatura
               ),
               '[]'::JSONB
       )
FROM academico.docente_asignatura da
         JOIN academico.asignatura a ON a.id_asignatura = da.id_asignatura
         JOIN academico.carrera    c ON c.id_carrera    = a.id_carrera
         JOIN academico.facultad   f ON f.id_facultad   = c.id_facultad
WHERE da.id_docente = p_id_docente
  AND da.activo     = TRUE;
$$;


--
-- Name: FUNCTION fn_listar_asignaturas_docente(p_id_docente integer); Type: COMMENT; Schema: academico; Owner: -
--

COMMENT ON FUNCTION academico.fn_listar_asignaturas_docente(p_id_docente integer) IS 'Retorna las asignaturas activas asignadas a un docente con jerarquía completa.';


--
-- Name: fn_listar_docentes_activos(); Type: FUNCTION; Schema: academico; Owner: -
--

CREATE FUNCTION academico.fn_listar_docentes_activos() RETURNS jsonb
    LANGUAGE sql STABLE SECURITY DEFINER
    AS $$
WITH docentes_con_conteo AS (
    SELECT
        d.id_docente,
        u.nombres,
        u.apellidos,
        u.cedula,
        u.correo,
        COUNT(da.id_docente_asignatura) AS total_asignaturas
    FROM academico.docente d
             JOIN seguridad.usuario u
                  ON u.id_usuario    = d.id_usuario
                      AND u.activo        = TRUE
             JOIN seguridad.usuario_tipo_rol utr
                  ON utr.id_usuario  = u.id_usuario
                      AND utr.activo      = TRUE
             JOIN seguridad.tipo_rol tr
                  ON tr.id_tipo_rol     = utr.id_tipo_rol
                      AND tr.nombre_tipo_rol = 'DOCENTE'
                      AND tr.activo          = TRUE
             LEFT JOIN academico.docente_asignatura da
                       ON da.id_docente   = d.id_docente
                           AND da.activo       = TRUE
    WHERE d.activo = TRUE
    GROUP BY d.id_docente, u.nombres, u.apellidos, u.cedula, u.correo
)
SELECT COALESCE(
               jsonb_agg(
                       jsonb_build_object(
                               'idDocente',        id_docente,
                               'nombres',          nombres,
                               'apellidos',        apellidos,
                               'cedula',           cedula,
                               'correo',           correo,
                               'totalAsignaturas', total_asignaturas
                       )
                       ORDER BY apellidos, nombres
               ),
               '[]'::JSONB
       )
FROM docentes_con_conteo;
$$;


--
-- Name: FUNCTION fn_listar_docentes_activos(); Type: COMMENT; Schema: academico; Owner: -
--

COMMENT ON FUNCTION academico.fn_listar_docentes_activos() IS 'Lista todos los docentes activos (con rol DOCENTE activo) junto con el conteo de asignaturas actualmente asignadas.';


--
-- Name: fn_listar_jerarquia_asignaturas(); Type: FUNCTION; Schema: academico; Owner: -
--

CREATE FUNCTION academico.fn_listar_jerarquia_asignaturas() RETURNS jsonb
    LANGUAGE sql STABLE SECURITY DEFINER
    AS $$
SELECT COALESCE(
               jsonb_agg(
                       jsonb_build_object(
                               'idAsignatura',     a.id_asignatura,
                               'nombreAsignatura', a.nombre_asignatura,
                               'semestre',         a.semestre,
                               'idCarrera',        c.id_carrera,
                               'nombreCarrera',    c.nombre_carrera,
                               'idFacultad',       f.id_facultad,
                               'nombreFacultad',   f.nombre_facultad,
                               'etiqueta',         a.nombre_asignatura
                                                       || ' · ' || c.nombre_carrera
                                                       || ' · ' || f.nombre_facultad
                       )
                       ORDER BY f.nombre_facultad, c.nombre_carrera, a.semestre, a.nombre_asignatura
               ),
               '[]'::JSONB
       )
FROM academico.asignatura a
         JOIN academico.carrera  c ON c.id_carrera  = a.id_carrera
         JOIN academico.facultad f ON f.id_facultad = c.id_facultad
WHERE COALESCE(a.activo, TRUE) = TRUE
  AND COALESCE(c.activo, TRUE) = TRUE
  AND COALESCE(f.activo, TRUE) = TRUE;
$$;


--
-- Name: FUNCTION fn_listar_jerarquia_asignaturas(); Type: COMMENT; Schema: academico; Owner: -
--

COMMENT ON FUNCTION academico.fn_listar_jerarquia_asignaturas() IS 'Catálogo global de asignaturas con su jerarquía completa (Asignatura · Carrera · Facultad) para el buscador global.';


--
-- Name: fn_obtener_estadisticas_coordinador(integer); Type: FUNCTION; Schema: academico; Owner: -
--

CREATE FUNCTION academico.fn_obtener_estadisticas_coordinador(p_id_usuario integer) RETURNS TABLE(total_convocatorias_propias bigint, convocatorias_activas bigint, convocatorias_inactivas bigint, total_postulantes_recibidos bigint, postulantes_aprobados bigint, postulantes_rechazados bigint, postulantes_en_evaluacion bigint, postulantes_pendientes bigint, top_convocatorias jsonb)
    LANGUAGE plpgsql SECURITY DEFINER
    AS $$
DECLARE
    v_total_convocatorias BIGINT := 0;
    v_convocatorias_activas BIGINT := 0;
    v_convocatorias_inactivas BIGINT := 0;
    v_total_postulantes BIGINT := 0;
    v_postulantes_aprobados BIGINT := 0;
    v_postulantes_rechazados BIGINT := 0;
    v_postulantes_en_evaluacion BIGINT := 0;
    v_postulantes_pendientes BIGINT := 0;
    v_top_convocatorias JSONB := '[]'::JSONB;
BEGIN
    -- 1. Calcular convocatorias del coordinador
    SELECT 
        COUNT(*),
        COUNT(*) FILTER (WHERE c.activo = TRUE),
        COUNT(*) FILTER (WHERE c.activo = FALSE)
    INTO 
        v_total_convocatorias,
        v_convocatorias_activas,
        v_convocatorias_inactivas
    FROM convocatoria.convocatoria c
    JOIN academico.asignatura a ON c.id_asignatura = a.id_asignatura
    JOIN academico.coordinador coord ON a.id_carrera = coord.id_carrera
    WHERE coord.id_usuario = p_id_usuario;

    -- 2. Calcular estadísticas de postulantes para las convocatorias del coordinador
    SELECT 
        COUNT(*),
        COUNT(*) FILTER (WHERE UPPER(tep.codigo) IN ('APROBADO', 'SELECCIONADO', 'ELEGIBLE', 'APROBADA')),
        COUNT(*) FILTER (WHERE UPPER(tep.codigo) = 'RECHAZADO'),
        COUNT(*) FILTER (WHERE UPPER(tep.codigo) IN ('EN_EVALUACION', 'EN EVALUACION')),
        COUNT(*) FILTER (WHERE UPPER(tep.codigo) = 'PENDIENTE')
    INTO
        v_total_postulantes,
        v_postulantes_aprobados,
        v_postulantes_rechazados,
        v_postulantes_en_evaluacion,
        v_postulantes_pendientes
    FROM postulacion.postulacion p
    JOIN postulacion.tipo_estado_postulacion tep ON p.id_tipo_estado_postulacion = tep.id_tipo_estado_postulacion
    JOIN convocatoria.convocatoria c ON p.id_convocatoria = c.id_convocatoria
    JOIN academico.asignatura a ON c.id_asignatura = a.id_asignatura
    JOIN academico.coordinador coord ON a.id_carrera = coord.id_carrera
    WHERE coord.id_usuario = p_id_usuario;

    -- 3. Top 5 convocatorias por postulantes
    SELECT COALESCE(jsonb_agg(
        jsonb_build_object(
            'tituloConvocatoria', conv_stats.nombre_asignatura,
            'cantidadPostulantes', conv_stats.cantidad
        )
    ), '[]'::JSONB)
    INTO v_top_convocatorias
    FROM (
        SELECT 
            a.nombre_asignatura,
            COUNT(p.id_postulacion) as cantidad
        FROM convocatoria.convocatoria c
        JOIN academico.asignatura a ON c.id_asignatura = a.id_asignatura
        JOIN academico.coordinador coord ON a.id_carrera = coord.id_carrera
        LEFT JOIN postulacion.postulacion p ON c.id_convocatoria = p.id_convocatoria
        WHERE coord.id_usuario = p_id_usuario
        GROUP BY c.id_convocatoria, a.nombre_asignatura
        ORDER BY cantidad DESC
        LIMIT 5
    ) conv_stats;

    RETURN QUERY SELECT 
        v_total_convocatorias,
        v_convocatorias_activas,
        v_convocatorias_inactivas,
        v_total_postulantes,
        v_postulantes_aprobados,
        v_postulantes_rechazados,
        v_postulantes_en_evaluacion,
        v_postulantes_pendientes,
        v_top_convocatorias;
END;
$$;


--
-- Name: fn_obtener_estadisticas_decano(integer); Type: FUNCTION; Schema: academico; Owner: -
--

CREATE FUNCTION academico.fn_obtener_estadisticas_decano(p_id_facultad integer) RETURNS TABLE(total_convocatorias bigint, convocatorias_activas bigint, convocatorias_inactivas bigint, total_postulantes bigint, postulantes_seleccionados bigint, postulantes_no_seleccionados bigint, postulantes_en_evaluacion bigint, postulantes_pendientes bigint, actividad_coordinador jsonb)
    LANGUAGE plpgsql
    AS $$
DECLARE
    v_actividad JSONB;
BEGIN
    -- Actividad por coordinador
    SELECT COALESCE(jsonb_agg(jsonb_build_object(
        'nombreCoordinador', u.nombres || ' ' || u.apellidos,
        'totalConvocatorias', COALESCE(cv_count.cnt, 0)
    )), '[]'::jsonb)
    INTO v_actividad
    FROM academico.coordinador co
    JOIN seguridad.usuario u ON co.id_usuario = u.id_usuario
    JOIN academico.carrera c ON co.id_carrera = c.id_carrera
    LEFT JOIN (
        SELECT a.id_carrera, COUNT(*) as cnt
        FROM convocatoria.convocatoria cv
        JOIN academico.asignatura a ON cv.id_asignatura = a.id_asignatura
        WHERE cv.activo = TRUE
        GROUP BY a.id_carrera
    ) cv_count ON cv_count.id_carrera = c.id_carrera
    WHERE c.id_facultad = p_id_facultad AND co.activo = TRUE;
    RETURN QUERY
    SELECT 
        -- Convocatorias
        (SELECT COUNT(*) FROM convocatoria.convocatoria cv
         JOIN academico.asignatura a ON cv.id_asignatura = a.id_asignatura
         JOIN academico.carrera c ON a.id_carrera = c.id_carrera
         WHERE c.id_facultad = p_id_facultad AND cv.activo = TRUE)::BIGINT,
         
        (SELECT COUNT(*) FROM convocatoria.convocatoria cv
         JOIN academico.asignatura a ON cv.id_asignatura = a.id_asignatura
         JOIN academico.carrera c ON a.id_carrera = c.id_carrera
         WHERE c.id_facultad = p_id_facultad AND cv.activo = TRUE 
         AND UPPER(cv.estado) IN ('ABIERTA', 'ACTIVA', 'PUBLICADA', 'PUBLICADO'))::BIGINT,
         
        (SELECT COUNT(*) FROM convocatoria.convocatoria cv
         JOIN academico.asignatura a ON cv.id_asignatura = a.id_asignatura
         JOIN academico.carrera c ON a.id_carrera = c.id_carrera
         WHERE c.id_facultad = p_id_facultad AND cv.activo = TRUE 
         AND UPPER(cv.estado) NOT IN ('ABIERTA', 'ACTIVA', 'PUBLICADA', 'PUBLICADO'))::BIGINT,
         
        -- Postulantes
        (SELECT COUNT(*) FROM postulacion.postulacion p
         JOIN convocatoria.convocatoria cv ON p.id_convocatoria = cv.id_convocatoria
         JOIN academico.asignatura a ON cv.id_asignatura = a.id_asignatura
         JOIN academico.carrera c ON a.id_carrera = c.id_carrera
         WHERE c.id_facultad = p_id_facultad AND p.activo = TRUE)::BIGINT,
         
        (SELECT COUNT(*) FROM postulacion.postulacion p
         JOIN convocatoria.convocatoria cv ON p.id_convocatoria = cv.id_convocatoria
         JOIN academico.asignatura a ON cv.id_asignatura = a.id_asignatura
         JOIN academico.carrera c ON a.id_carrera = c.id_carrera
         JOIN postulacion.tipo_estado_postulacion tep ON p.id_tipo_estado_postulacion = tep.id_tipo_estado_postulacion
         WHERE c.id_facultad = p_id_facultad AND p.activo = TRUE 
         AND tep.codigo IN ('SELECCIONADO', 'APROBADA', 'APROBADO'))::BIGINT,
         
        (SELECT COUNT(*) FROM postulacion.postulacion p
         JOIN convocatoria.convocatoria cv ON p.id_convocatoria = cv.id_convocatoria
         JOIN academico.asignatura a ON cv.id_asignatura = a.id_asignatura
         JOIN academico.carrera c ON a.id_carrera = c.id_carrera
         JOIN postulacion.tipo_estado_postulacion tep ON p.id_tipo_estado_postulacion = tep.id_tipo_estado_postulacion
         WHERE c.id_facultad = p_id_facultad AND p.activo = TRUE 
         AND tep.codigo IN ('NO_SELECCIONADO', 'RECHAZADA', 'RECHAZADO'))::BIGINT,
         
        (SELECT COUNT(*) FROM postulacion.postulacion p
         JOIN convocatoria.convocatoria cv ON p.id_convocatoria = cv.id_convocatoria
         JOIN academico.asignatura a ON cv.id_asignatura = a.id_asignatura
         JOIN academico.carrera c ON a.id_carrera = c.id_carrera
         JOIN postulacion.tipo_estado_postulacion tep ON p.id_tipo_estado_postulacion = tep.id_tipo_estado_postulacion
         WHERE c.id_facultad = p_id_facultad AND p.activo = TRUE 
         AND tep.codigo IN ('EN_REVISION', 'EN_EVALUACION', 'EN_PROCESO'))::BIGINT,
         
        (SELECT COUNT(*) FROM postulacion.postulacion p
         JOIN convocatoria.convocatoria cv ON p.id_convocatoria = cv.id_convocatoria
         JOIN academico.asignatura a ON cv.id_asignatura = a.id_asignatura
         JOIN academico.carrera c ON a.id_carrera = c.id_carrera
         JOIN postulacion.tipo_estado_postulacion tep ON p.id_tipo_estado_postulacion = tep.id_tipo_estado_postulacion
         WHERE c.id_facultad = p_id_facultad AND p.activo = TRUE 
         AND tep.codigo = 'PENDIENTE')::BIGINT,
         
        v_actividad;
END;
$$;


--
-- Name: fn_obtener_id_periodo_activo(); Type: FUNCTION; Schema: academico; Owner: -
--

CREATE FUNCTION academico.fn_obtener_id_periodo_activo() RETURNS integer
    LANGUAGE sql STABLE
    AS $$
SELECT id_periodo_academico
FROM   academico.periodo_academico
WHERE  activo = TRUE
  AND  estado IN ('EN PROCESO', 'CONFIGURADO')
ORDER  BY fecha_inicio DESC
LIMIT  1;
$$;


--
-- Name: fn_reporte_convocatorias_coordinador(integer); Type: FUNCTION; Schema: academico; Owner: -
--

CREATE FUNCTION academico.fn_reporte_convocatorias_coordinador(p_id_usuario integer) RETURNS TABLE(id_convocatoria integer, nombre_asignatura character varying, nombre_carrera character varying, nombre_periodo character varying, fecha_inicio date, fecha_fin date, cupos_aprobados integer, estado character varying, numero_postulantes bigint)
    LANGUAGE plpgsql SECURITY DEFINER
    AS $$
BEGIN
    RETURN QUERY
    SELECT 
        c.id_convocatoria,
        a.nombre_asignatura,
        car.nombre_carrera,
        pa.nombre_periodo,
        CURRENT_DATE,
        CURRENT_DATE,
        c.cupos_disponibles,
        CASE WHEN c.activo THEN 'ACTIVO'::VARCHAR ELSE 'INACTIVO'::VARCHAR END,
        (SELECT COUNT(*) FROM postulacion.postulacion p WHERE p.id_convocatoria = c.id_convocatoria)::BIGINT as num_postulantes
    FROM convocatoria.convocatoria c
    JOIN academico.asignatura a ON c.id_asignatura = a.id_asignatura
    JOIN academico.carrera car ON a.id_carrera = car.id_carrera
    JOIN academico.periodo_academico pa ON c.id_periodo_academico = pa.id_periodo_academico
    JOIN academico.coordinador coord ON car.id_carrera = coord.id_carrera
    WHERE coord.id_usuario = p_id_usuario
    ORDER BY CURRENT_DATE DESC;
END;
$$;


--
-- Name: fn_reporte_convocatorias_decano(integer); Type: FUNCTION; Schema: academico; Owner: -
--

CREATE FUNCTION academico.fn_reporte_convocatorias_decano(p_id_facultad integer) RETURNS TABLE(id_convocatoria integer, nombre_asignatura character varying, nombre_carrera character varying, nombre_coordinador text, fecha_inicio date, fecha_fin date, estado character varying, numero_postulantes bigint)
    LANGUAGE plpgsql
    AS $$
BEGIN
    RETURN QUERY
    WITH ventana_postulacion AS (
        SELECT
            pa.id_periodo_academico,
            MAX(CASE WHEN tf.codigo = 'POSTULACION' THEN pf.fecha_inicio END) as inicio,
            MAX(CASE WHEN tf.codigo = 'POSTULACION' THEN pf.fecha_fin END) as fin
        FROM academico.periodo_academico pa
        JOIN planificacion.periodo_fase pf ON pf.id_periodo_academico = pa.id_periodo_academico
        JOIN planificacion.tipo_fase tf ON tf.id_tipo_fase = pf.id_tipo_fase
        GROUP BY pa.id_periodo_academico
    )
    SELECT 
        cv.id_convocatoria,
        a.nombre_asignatura::VARCHAR,
        c.nombre_carrera::VARCHAR,
        u.nombres || ' ' || u.apellidos,
        vp.inicio::DATE,
        vp.fin::DATE,
        cv.estado::VARCHAR,
        (SELECT COUNT(*) FROM postulacion.postulacion p WHERE p.id_convocatoria = cv.id_convocatoria AND p.activo = TRUE)::BIGINT
    FROM convocatoria.convocatoria cv
    JOIN academico.asignatura a ON cv.id_asignatura = a.id_asignatura
    JOIN academico.carrera c ON a.id_carrera = c.id_carrera
    JOIN academico.coordinador co ON c.id_carrera = co.id_carrera AND co.activo = TRUE
    JOIN seguridad.usuario u ON co.id_usuario = u.id_usuario
    LEFT JOIN ventana_postulacion vp ON vp.id_periodo_academico = cv.id_periodo_academico
    WHERE c.id_facultad = p_id_facultad AND cv.activo = TRUE;
END;
$$;


--
-- Name: fn_reporte_coordinadores_decano(integer); Type: FUNCTION; Schema: academico; Owner: -
--

CREATE FUNCTION academico.fn_reporte_coordinadores_decano(p_id_facultad integer) RETURNS jsonb
    LANGUAGE sql STABLE SECURITY DEFINER
    AS $$
SELECT COALESCE(
    jsonb_agg(
        jsonb_build_object(
            'idCoordinador', c.id_coordinador,
            'nombreCoordinador', u.nombres || ' ' || u.apellidos,
            'nombreCarrera', car.nombre_carrera,
            'convocatoriasAbiertas', (SELECT COUNT(*) FROM convocatoria.convocatoria con JOIN academico.asignatura a ON a.id_asignatura = con.id_asignatura WHERE a.id_carrera = c.id_carrera AND con.activo = TRUE AND con.estado = 'ABIERTA'),
            'convocatoriasCerradas', (SELECT COUNT(*) FROM convocatoria.convocatoria con JOIN academico.asignatura a ON a.id_asignatura = con.id_asignatura WHERE a.id_carrera = c.id_carrera AND con.activo = TRUE AND con.estado = 'CERRADA'),
            'totalPostulantes', (SELECT COUNT(*) FROM postulacion.postulacion p JOIN convocatoria.convocatoria con ON con.id_convocatoria = p.id_convocatoria JOIN academico.asignatura a ON a.id_asignatura = con.id_asignatura WHERE a.id_carrera = c.id_carrera)
        )
    ),
    '[]'::JSONB
)
FROM academico.coordinador c
JOIN seguridad.usuario u ON u.id_usuario = c.id_usuario
JOIN academico.carrera car ON car.id_carrera = c.id_carrera
WHERE car.id_facultad = p_id_facultad AND c.activo = TRUE;
$$;


--
-- Name: fn_reporte_global_personal(); Type: FUNCTION; Schema: academico; Owner: -
--

CREATE FUNCTION academico.fn_reporte_global_personal() RETURNS TABLE(nombre text, cargo_contexto text, estado character varying)
    LANGUAGE plpgsql
    AS $$
BEGIN
    RETURN QUERY
    -- Decanos
    SELECT 
        u.nombres || ' ' || u.apellidos AS nombre,
        'Decano - ' || f.nombre_facultad AS cargo_contexto,
        (CASE WHEN d.activo THEN 'Activo' ELSE 'Inactivo' END)::VARCHAR AS estado
    FROM academico.decano d
    JOIN seguridad.usuario u ON d.id_usuario = u.id_usuario
    JOIN academico.facultad f ON d.id_facultad = f.id_facultad
    
    UNION ALL
    
    -- Coordinadores
    SELECT 
        u.nombres || ' ' || u.apellidos AS nombre,
        'Coordinador - ' || c.nombre_carrera AS cargo_contexto,
        (CASE WHEN co.activo THEN 'Activo' ELSE 'Inactivo' END)::VARCHAR AS estado
    FROM academico.coordinador co
    JOIN seguridad.usuario u ON co.id_usuario = u.id_usuario
    JOIN academico.carrera c ON co.id_carrera = c.id_carrera
    
    ORDER BY nombre;
END;
$$;


--
-- Name: fn_reporte_postulantes_coordinador(integer); Type: FUNCTION; Schema: academico; Owner: -
--

CREATE FUNCTION academico.fn_reporte_postulantes_coordinador(p_id_usuario integer) RETURNS TABLE(id_postulacion integer, nombre_estudiante character varying, cedula character varying, nombre_asignatura character varying, nombre_periodo character varying, fecha_postulacion date, estado_evaluacion character varying)
    LANGUAGE plpgsql SECURITY DEFINER
    AS $$
BEGIN
    RETURN QUERY
    SELECT 
        p.id_postulacion,
        (u.nombres || ' ' || u.apellidos)::VARCHAR,
        u.cedula,
        a.nombre_asignatura,
        pa.nombre_periodo,
        p.fecha_postulacion,
        tep.nombre::VARCHAR
    FROM postulacion.postulacion p
    JOIN postulacion.tipo_estado_postulacion tep ON p.id_tipo_estado_postulacion = tep.id_tipo_estado_postulacion
    JOIN academico.estudiante est ON p.id_estudiante = est.id_estudiante
    JOIN seguridad.usuario u ON est.id_usuario = u.id_usuario
    JOIN convocatoria.convocatoria c ON p.id_convocatoria = c.id_convocatoria
    JOIN academico.asignatura a ON c.id_asignatura = a.id_asignatura
    JOIN academico.periodo_academico pa ON c.id_periodo_academico = pa.id_periodo_academico
    JOIN academico.coordinador coord ON a.id_carrera = coord.id_carrera
    WHERE coord.id_usuario = p_id_usuario
    ORDER BY p.fecha_postulacion DESC;
END;
$$;


--
-- Name: fn_reporte_postulantes_decano(integer); Type: FUNCTION; Schema: academico; Owner: -
--

CREATE FUNCTION academico.fn_reporte_postulantes_decano(p_id_facultad integer) RETURNS TABLE(id_postulacion integer, nombre_estudiante text, cedula character varying, nombre_asignatura character varying, nombre_periodo character varying, fecha_postulacion date, estado_evaluacion character varying)
    LANGUAGE plpgsql
    AS $$
BEGIN
    RETURN QUERY
    SELECT 
        p.id_postulacion,
        u.nombres || ' ' || u.apellidos,
        u.cedula,
        a.nombre_asignatura::VARCHAR,
        pa.nombre_periodo::VARCHAR,
        p.fecha_postulacion::DATE,
        COALESCE(tep.nombre, 'PENDIENTE')::VARCHAR
    FROM postulacion.postulacion p
    JOIN academico.estudiante e ON p.id_estudiante = e.id_estudiante
    JOIN seguridad.usuario u ON e.id_usuario = u.id_usuario
    JOIN convocatoria.convocatoria cv ON p.id_convocatoria = cv.id_convocatoria
    JOIN academico.asignatura a ON cv.id_asignatura = a.id_asignatura
    JOIN academico.carrera c ON a.id_carrera = c.id_carrera
    JOIN academico.periodo_academico pa ON cv.id_periodo_academico = pa.id_periodo_academico
    LEFT JOIN postulacion.tipo_estado_postulacion tep ON p.id_tipo_estado_postulacion = tep.id_tipo_estado_postulacion
    WHERE c.id_facultad = p_id_facultad AND p.activo = TRUE;
END;
$$;


--
-- Name: fn_verificar_elegibilidad_academica(integer); Type: FUNCTION; Schema: academico; Owner: -
--

CREATE FUNCTION academico.fn_verificar_elegibilidad_academica(p_id_estudiante integer, OUT p_es_elegible boolean, OUT p_mensaje text) RETURNS record
    LANGUAGE plpgsql
    AS $$
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


--
-- Name: fn_verificar_estado_periodo(); Type: FUNCTION; Schema: academico; Owner: -
--

CREATE FUNCTION academico.fn_verificar_estado_periodo() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
    IF NEW.fecha_fin < CURRENT_DATE AND NEW.estado = 'EN PROCESO' THEN
        NEW.estado  := 'INACTIVO';
        NEW.activo  := FALSE;
    END IF;
    RETURN NEW;
END;
$$;


--
-- Name: fn_actualizar_documento(integer, character varying, integer, integer, integer, integer); Type: FUNCTION; Schema: ayudantia; Owner: -
--

CREATE FUNCTION ayudantia.fn_actualizar_documento(p_id_documento integer, p_nombre_mostrar character varying, p_id_tipo_doc integer, p_id_facultad integer, p_id_carrera integer, p_id_usuario integer) RETURNS seguridad.res_operacion
    LANGUAGE plpgsql
    AS $$
DECLARE
    v_id_fac_coord   INTEGER;
    v_id_car_coord   INTEGER;
    v_es_coordinador BOOLEAN := FALSE;
BEGIN
    SELECT c.id_carrera, car.id_facultad, TRUE
    INTO v_id_car_coord, v_id_fac_coord, v_es_coordinador
    FROM academico.coordinador c
             JOIN academico.carrera car ON c.id_carrera = car.id_carrera
    WHERE c.id_usuario = p_id_usuario AND c.activo = TRUE;

    IF v_es_coordinador THEN
        IF p_id_carrera IS NOT NULL THEN
            p_id_carrera := v_id_car_coord;
            p_id_facultad := v_id_fac_coord;
        ELSE
            p_id_facultad := v_id_fac_coord;
            p_id_carrera := NULL;
        END IF;
    END IF;

    UPDATE ayudantia.documento_academico
    SET nombre_mostrar = p_nombre_mostrar,
        id_tipo_documento = p_id_tipo_doc,
        id_facultad = p_id_facultad,
        id_carrera = p_id_carrera
    WHERE id_documento = p_id_documento;

    IF NOT FOUND THEN
        RETURN (FALSE, 'No se encontró el documento o no tiene permisos', NULL)::seguridad.res_operacion;
    END IF;

    RETURN (TRUE, 'Documento actualizado correctamente', NULL)::seguridad.res_operacion;

EXCEPTION
    WHEN OTHERS THEN
        RETURN (FALSE, 'Error al actualizar: ' || SQLERRM, NULL)::seguridad.res_operacion;
END;
$$;


--
-- Name: fn_actualizar_tipo_estado_ayudantia(integer, character varying, text, character varying); Type: FUNCTION; Schema: ayudantia; Owner: -
--

CREATE FUNCTION ayudantia.fn_actualizar_tipo_estado_ayudantia(p_id integer, p_nombre_estado character varying, p_descripcion text, p_codigo character varying) RETURNS jsonb
    LANGUAGE plpgsql SECURITY DEFINER
    AS $$
BEGIN
    -- Validar que el registro exista
    IF NOT EXISTS (
        SELECT 1 FROM ayudantia.tipo_estado_ayudantia
        WHERE id_tipo_estado_ayudantia = p_id
    ) THEN
        RETURN jsonb_build_object('exito', false, 'mensaje', 'No se encontró el estado de ayudantía con el ID especificado');
    END IF;

    -- Validar que el nombre no exista en otro registro (case-insensitive)
    IF EXISTS (
        SELECT 1 FROM ayudantia.tipo_estado_ayudantia
        WHERE LOWER(TRIM(nombre_estado)) = LOWER(TRIM(p_nombre_estado))
        AND id_tipo_estado_ayudantia != p_id
    ) THEN
        RETURN jsonb_build_object('exito', false, 'mensaje', 'Ya existe otro estado de ayudantía con el nombre especificado');
    END IF;

    -- Validar que el código no exista en otro registro (case-insensitive)
    IF EXISTS (
        SELECT 1 FROM ayudantia.tipo_estado_ayudantia
        WHERE LOWER(TRIM(codigo)) = LOWER(TRIM(p_codigo))
        AND id_tipo_estado_ayudantia != p_id
    ) THEN
        RETURN jsonb_build_object('exito', false, 'mensaje', 'Ya existe otro estado de ayudantía con el código especificado');
    END IF;

    -- Actualizar el registro
    UPDATE ayudantia.tipo_estado_ayudantia
    SET nombre_estado = TRIM(p_nombre_estado),
        descripcion = TRIM(p_descripcion),
        codigo = UPPER(TRIM(p_codigo))
    WHERE id_tipo_estado_ayudantia = p_id;

    RETURN jsonb_build_object(
        'exito', true,
        'mensaje', 'Estado de ayudantía actualizado exitosamente',
        'id', p_id
    );

EXCEPTION
    WHEN unique_violation THEN
        RETURN jsonb_build_object('exito', false, 'mensaje', 'Error de duplicidad: el nombre o código ya existe');
    WHEN OTHERS THEN
        RETURN jsonb_build_object('exito', false, 'mensaje', 'Error al actualizar estado de ayudantía: ' || SQLERRM);
END;
$$;


--
-- Name: fn_actualizar_tipo_estado_evidencia(integer, character varying, text, character varying); Type: FUNCTION; Schema: ayudantia; Owner: -
--

CREATE FUNCTION ayudantia.fn_actualizar_tipo_estado_evidencia(p_id integer, p_nombre_estado character varying, p_descripcion text, p_codigo character varying) RETURNS jsonb
    LANGUAGE plpgsql SECURITY DEFINER
    AS $$
BEGIN
    -- Validar que el registro exista
    IF NOT EXISTS (
        SELECT 1 FROM ayudantia.tipo_estado_evidencia
        WHERE id_tipo_estado_evidencia = p_id
    ) THEN
        RETURN jsonb_build_object('exito', false, 'mensaje', 'No se encontró el estado de evidencia con el ID especificado');
    END IF;

    -- Validar que el nombre no exista en otro registro (case-insensitive)
    IF EXISTS (
        SELECT 1 FROM ayudantia.tipo_estado_evidencia
        WHERE LOWER(TRIM(nombre_estado)) = LOWER(TRIM(p_nombre_estado))
        AND id_tipo_estado_evidencia != p_id
    ) THEN
        RETURN jsonb_build_object('exito', false, 'mensaje', 'Ya existe otro estado de evidencia con el nombre especificado');
    END IF;

    -- Validar que el código no exista en otro registro (case-insensitive)
    IF EXISTS (
        SELECT 1 FROM ayudantia.tipo_estado_evidencia
        WHERE LOWER(TRIM(codigo)) = LOWER(TRIM(p_codigo))
        AND id_tipo_estado_evidencia != p_id
    ) THEN
        RETURN jsonb_build_object('exito', false, 'mensaje', 'Ya existe otro estado de evidencia con el código especificado');
    END IF;

    -- Actualizar el registro
    UPDATE ayudantia.tipo_estado_evidencia
    SET nombre_estado = TRIM(p_nombre_estado),
        descripcion = TRIM(p_descripcion),
        codigo = UPPER(TRIM(p_codigo))
    WHERE id_tipo_estado_evidencia = p_id;

    RETURN jsonb_build_object(
        'exito', true,
        'mensaje', 'Estado de evidencia actualizado exitosamente',
        'id', p_id
    );

EXCEPTION
    WHEN unique_violation THEN
        RETURN jsonb_build_object('exito', false, 'mensaje', 'Error de duplicidad: el nombre o código ya existe');
    WHEN OTHERS THEN
        RETURN jsonb_build_object('exito', false, 'mensaje', 'Error al actualizar estado de evidencia: ' || SQLERRM);
END;
$$;


--
-- Name: fn_actualizar_tipo_estado_registro(integer, character varying, text, character varying); Type: FUNCTION; Schema: ayudantia; Owner: -
--

CREATE FUNCTION ayudantia.fn_actualizar_tipo_estado_registro(p_id integer, p_nombre_estado character varying, p_descripcion text, p_codigo character varying) RETURNS jsonb
    LANGUAGE plpgsql SECURITY DEFINER
    AS $$
BEGIN
    -- Validar que el registro exista
    IF NOT EXISTS (
        SELECT 1 FROM ayudantia.tipo_estado_registro
        WHERE id_tipo_estado_registro = p_id
    ) THEN
        RETURN jsonb_build_object('exito', false, 'mensaje', 'No se encontró el estado de registro con el ID especificado');
    END IF;

    -- Validar que el nombre no exista en otro registro (case-insensitive)
    IF EXISTS (
        SELECT 1 FROM ayudantia.tipo_estado_registro
        WHERE LOWER(TRIM(nombre_estado)) = LOWER(TRIM(p_nombre_estado))
        AND id_tipo_estado_registro != p_id
    ) THEN
        RETURN jsonb_build_object('exito', false, 'mensaje', 'Ya existe otro estado de registro con el nombre especificado');
    END IF;

    -- Validar que el código no exista en otro registro (case-insensitive)
    IF EXISTS (
        SELECT 1 FROM ayudantia.tipo_estado_registro
        WHERE LOWER(TRIM(codigo)) = LOWER(TRIM(p_codigo))
        AND id_tipo_estado_registro != p_id
    ) THEN
        RETURN jsonb_build_object('exito', false, 'mensaje', 'Ya existe otro estado de registro con el código especificado');
    END IF;

    -- Actualizar el registro
    UPDATE ayudantia.tipo_estado_registro
    SET nombre_estado = TRIM(p_nombre_estado),
        descripcion = TRIM(p_descripcion),
        codigo = UPPER(TRIM(p_codigo))
    WHERE id_tipo_estado_registro = p_id;

    RETURN jsonb_build_object(
        'exito', true,
        'mensaje', 'Estado de registro actualizado exitosamente',
        'id', p_id
    );

EXCEPTION
    WHEN unique_violation THEN
        RETURN jsonb_build_object('exito', false, 'mensaje', 'Error de duplicidad: el nombre o código ya existe');
    WHEN OTHERS THEN
        RETURN jsonb_build_object('exito', false, 'mensaje', 'Error al actualizar estado de registro: ' || SQLERRM);
END;
$$;


--
-- Name: fn_actualizar_tipo_evidencia(integer, character varying, character varying, character varying); Type: FUNCTION; Schema: ayudantia; Owner: -
--

CREATE FUNCTION ayudantia.fn_actualizar_tipo_evidencia(p_id integer, p_nombre character varying, p_extension_permitida character varying, p_codigo character varying) RETURNS jsonb
    LANGUAGE plpgsql SECURITY DEFINER
    AS $$
BEGIN
    -- Validar que el registro exista
    IF NOT EXISTS (
        SELECT 1 FROM ayudantia.tipo_evidencia
        WHERE id_tipo_evidencia = p_id
    ) THEN
        RETURN jsonb_build_object('exito', false, 'mensaje', 'No se encontró el tipo de evidencia con el ID especificado');
    END IF;

    -- Validar que el nombre no exista en otro registro (case-insensitive)
    IF EXISTS (
        SELECT 1 FROM ayudantia.tipo_evidencia
        WHERE LOWER(TRIM(nombre)) = LOWER(TRIM(p_nombre))
        AND id_tipo_evidencia != p_id
    ) THEN
        RETURN jsonb_build_object('exito', false, 'mensaje', 'Ya existe otro tipo de evidencia con el nombre especificado');
    END IF;

    -- Validar que el código no exista en otro registro (case-insensitive)
    IF EXISTS (
        SELECT 1 FROM ayudantia.tipo_evidencia
        WHERE LOWER(TRIM(codigo)) = LOWER(TRIM(p_codigo))
        AND id_tipo_evidencia != p_id
    ) THEN
        RETURN jsonb_build_object('exito', false, 'mensaje', 'Ya existe otro tipo de evidencia con el código especificado');
    END IF;

    -- Actualizar el registro
    UPDATE ayudantia.tipo_evidencia
    SET nombre = TRIM(p_nombre),
        extension_permitida = LOWER(TRIM(p_extension_permitida)),
        codigo = UPPER(TRIM(p_codigo))
    WHERE id_tipo_evidencia = p_id;

    RETURN jsonb_build_object(
        'exito', true,
        'mensaje', 'Tipo de evidencia actualizado exitosamente',
        'id', p_id
    );

EXCEPTION
    WHEN unique_violation THEN
        RETURN jsonb_build_object('exito', false, 'mensaje', 'Error de duplicidad: el nombre o código ya existe');
    WHEN OTHERS THEN
        RETURN jsonb_build_object('exito', false, 'mensaje', 'Error al actualizar tipo de evidencia: ' || SQLERRM);
END;
$$;


--
-- Name: fn_actualizar_tipo_sancion_ayudante_catedra(integer, character varying, character varying); Type: FUNCTION; Schema: ayudantia; Owner: -
--

CREATE FUNCTION ayudantia.fn_actualizar_tipo_sancion_ayudante_catedra(p_id integer, p_nombre_tipo_sancion character varying, p_codigo character varying) RETURNS jsonb
    LANGUAGE plpgsql SECURITY DEFINER
    AS $$
BEGIN
    -- Validar que el registro exista
    IF NOT EXISTS (
        SELECT 1 FROM ayudantia.tipo_sancion_ayudante_catedra
        WHERE id_tipo_sancion_ayudante_catedra = p_id
    ) THEN
        RETURN jsonb_build_object('exito', false, 'mensaje', 'No se encontró el tipo de sanción con el ID especificado');
    END IF;

    -- Validar que el nombre no exista en otro registro (case-insensitive)
    IF EXISTS (
        SELECT 1 FROM ayudantia.tipo_sancion_ayudante_catedra
        WHERE LOWER(TRIM(nombre_tipo_sancion)) = LOWER(TRIM(p_nombre_tipo_sancion))
        AND id_tipo_sancion_ayudante_catedra != p_id
    ) THEN
        RETURN jsonb_build_object('exito', false, 'mensaje', 'Ya existe otro tipo de sanción con el nombre especificado');
    END IF;

    -- Validar que el código no exista en otro registro (case-insensitive)
    IF EXISTS (
        SELECT 1 FROM ayudantia.tipo_sancion_ayudante_catedra
        WHERE LOWER(TRIM(codigo)) = LOWER(TRIM(p_codigo))
        AND id_tipo_sancion_ayudante_catedra != p_id
    ) THEN
        RETURN jsonb_build_object('exito', false, 'mensaje', 'Ya existe otro tipo de sanción con el código especificado');
    END IF;

    -- Actualizar el registro
    UPDATE ayudantia.tipo_sancion_ayudante_catedra
    SET nombre_tipo_sancion = TRIM(p_nombre_tipo_sancion),
        codigo = UPPER(TRIM(p_codigo))
    WHERE id_tipo_sancion_ayudante_catedra = p_id;

    RETURN jsonb_build_object(
        'exito', true,
        'mensaje', 'Tipo de sanción actualizado exitosamente',
        'id', p_id
    );

EXCEPTION
    WHEN unique_violation THEN
        RETURN jsonb_build_object('exito', false, 'mensaje', 'Error de duplicidad: el nombre o código ya existe');
    WHEN OTHERS THEN
        RETURN jsonb_build_object('exito', false, 'mensaje', 'Error al actualizar tipo de sanción: ' || SQLERRM);
END;
$$;


--
-- Name: fn_alertas_observaciones(integer); Type: FUNCTION; Schema: ayudantia; Owner: -
--

CREATE FUNCTION ayudantia.fn_alertas_observaciones(p_id_ayudantia integer) RETURNS TABLE(id_registro integer, fecha date, tema_tratado text, estado_revision character varying, descripcion text)
    LANGUAGE plpgsql
    AS $$
BEGIN
    RETURN QUERY
    SELECT
        ra.id_registro_actividad,
        ra.fecha,
        ra.tema_tratado,
        ra.estado_revision,
        ra.descripcion_actividad
    FROM ayudantia.registro_actividad ra
    WHERE ra.id_ayudantia = p_id_ayudantia
      AND ra.estado_revision IN ('OBSERVADO', 'RECHAZADO')
    ORDER BY ra.fecha DESC;
END;
$$;


--
-- Name: fn_cargar_evidencia_sesion(integer, integer, character varying, character varying, character varying, integer, integer); Type: FUNCTION; Schema: ayudantia; Owner: -
--

CREATE FUNCTION ayudantia.fn_cargar_evidencia_sesion(p_id_usuario integer, p_id_registro integer, p_nombre_archivo character varying, p_ruta_archivo character varying, p_mime_type character varying, p_tamanio_bytes integer, p_id_tipo_evidencia integer) RETURNS seguridad.res_operacion
    LANGUAGE plpgsql
    AS $$
DECLARE
    v_id_ayudantia_user INTEGER;
    v_id_nuevo_ev INTEGER;
    v_estado_sesion VARCHAR;
BEGIN
    SELECT a.id_ayudantia INTO v_id_ayudantia_user
    FROM ayudantia.ayudantia a
             JOIN postulacion.postulacion p ON a.id_postulacion = p.id_postulacion
             JOIN academico.estudiante e ON p.id_estudiante = e.id_estudiante
    WHERE e.id_usuario = p_id_usuario
      AND a.id_tipo_estado_ayudantia = (SELECT id_tipo_estado_ayudantia FROM ayudantia.tipo_estado_ayudantia WHERE codigo = 'ACTIVO')
    LIMIT 1;

    SELECT tr.codigo INTO v_estado_sesion
    FROM ayudantia.registro_actividad ra
             JOIN ayudantia.tipo_estado_registro tr ON ra.id_tipo_estado_registro = tr.id_tipo_estado_registro
    WHERE ra.id_registro_actividad = p_id_registro AND ra.id_ayudantia = v_id_ayudantia_user;

    IF v_estado_sesion IS NULL THEN
        RETURN (FALSE, 'No tiene permisos para subir archivos a esta sesión.', NULL)::seguridad.res_operacion;
    ELSIF v_estado_sesion NOT IN ('PLANIFICADO', 'RECHAZADO') THEN
        RETURN (FALSE, 'No se pueden agregar evidencias a una sesión que ya está en revisión o aprobada.', NULL)::seguridad.res_operacion;
    END IF;

    INSERT INTO ayudantia.evidencia_registro_actividad (
        id_registro_actividad, nombre_archivo, ruta_archivo, mime_type,
        tamanio_bytes, id_tipo_evidencia, id_tipo_estado_evidencia, fecha_subida, activo
    )
    VALUES (
               p_id_registro, p_nombre_archivo, p_ruta_archivo, p_mime_type,
               p_tamanio_bytes, p_id_tipo_evidencia,
               (SELECT id_tipo_estado_evidencia FROM ayudantia.tipo_estado_evidencia WHERE codigo = 'SUBIDO'),
               CURRENT_TIMESTAMP, TRUE
           )
    RETURNING id_evidencia_registro_actividad INTO v_id_nuevo_ev;

    RETURN (TRUE, 'Archivo adjuntado correctamente.', json_build_object('id_evidencia', v_id_nuevo_ev))::seguridad.res_operacion;

EXCEPTION WHEN OTHERS THEN
    RETURN (FALSE, 'Error al registrar evidencia: ' || SQLERRM, NULL)::seguridad.res_operacion;
END;
$$;


--
-- Name: fn_cargar_participantes_masivo(integer, jsonb); Type: FUNCTION; Schema: ayudantia; Owner: -
--

CREATE FUNCTION ayudantia.fn_cargar_participantes_masivo(p_id_ayudantia integer, p_participantes jsonb) RETURNS jsonb
    LANGUAGE plpgsql SECURITY DEFINER
    AS $$
DECLARE
    v_item       JSONB;
    v_nombre     TEXT;
    v_curso      TEXT;
    v_paralelo   TEXT;
    v_insertados INTEGER := 0;
    v_duplicados INTEGER := 0;
BEGIN
    -- ── Validaciones previas ─────────────────────────────────────────────
    IF NOT EXISTS (SELECT 1 FROM ayudantia.ayudantia WHERE id_ayudantia = p_id_ayudantia) THEN
        RAISE EXCEPTION 'VALIDACION: La ayudantía con ID % no existe.', p_id_ayudantia;
    END IF;

    IF p_participantes IS NULL OR jsonb_array_length(p_participantes) = 0 THEN
        RAISE EXCEPTION 'VALIDACION: La lista de participantes no puede estar vacía.';
    END IF;

    -- ── Inserción transaccional ──────────────────────────────────────────
    FOR v_item IN SELECT * FROM jsonb_array_elements(p_participantes)
        LOOP
            v_nombre   := TRIM(v_item->>'nombreCompleto');
            v_curso    := TRIM(COALESCE(v_item->>'curso',    ''));
            v_paralelo := TRIM(COALESCE(v_item->>'paralelo', ''));

            IF v_nombre IS NULL OR v_nombre = '' THEN
                RAISE EXCEPTION 'VALIDACION: El nombre completo es obligatorio en todos los registros.';
            END IF;

            INSERT INTO ayudantia.participante_ayudantia
            (id_ayudantia, nombre_completo, curso, paralelo, activo)
            VALUES
                (p_id_ayudantia, v_nombre, v_curso, v_paralelo, TRUE)
            ON CONFLICT (id_ayudantia, nombre_completo, curso, paralelo) DO NOTHING;

            IF FOUND THEN
                v_insertados := v_insertados + 1;
            ELSE
                v_duplicados := v_duplicados + 1;
            END IF;
        END LOOP;

    RETURN jsonb_build_object(
            'exito',      TRUE,
            'mensaje',    format('Carga completada. Nuevos: %s. Duplicados omitidos: %s.',
                                 v_insertados, v_duplicados),
            'insertados', v_insertados,
            'duplicados', v_duplicados,
            'total',      jsonb_array_length(p_participantes)
           );

EXCEPTION WHEN OTHERS THEN
    RAISE EXCEPTION '%', SQLERRM;
END;
$$;


--
-- Name: fn_completar_actividad(integer, text); Type: FUNCTION; Schema: ayudantia; Owner: -
--

CREATE FUNCTION ayudantia.fn_completar_actividad(p_id_registro_actividad integer, p_descripcion_actividad text) RETURNS boolean
    LANGUAGE plpgsql
    AS $$
DECLARE
    v_id_estado_actual INTEGER;
    v_id_estado_planificada INTEGER;
    v_id_estado_rechazada INTEGER;
    v_id_estado_pendiente INTEGER;
    v_fecha_actividad DATE;
BEGIN
    SELECT id_tipo_estado_registro, fecha
    INTO v_id_estado_actual, v_fecha_actividad
    FROM ayudantia.registro_actividad
    WHERE id_registro_actividad = p_id_registro_actividad;

    IF NOT FOUND THEN
        RAISE EXCEPTION 'El registro de actividad no existe.';
    END IF;

    IF v_fecha_actividad > CURRENT_DATE THEN
        RAISE EXCEPTION 'No puedes enviar evidencias de una sesión planificada para el futuro.';
    END IF;

    SELECT id_tipo_estado_registro INTO v_id_estado_planificada FROM ayudantia.tipo_estado_registro WHERE codigo = 'PLANIFICADO';
    SELECT id_tipo_estado_registro INTO v_id_estado_rechazada FROM ayudantia.tipo_estado_registro WHERE codigo = 'RECHAZADO';

    IF v_id_estado_actual NOT IN (v_id_estado_planificada, v_id_estado_rechazada) THEN
        RAISE EXCEPTION 'Solo se pueden enviar a revisión sesiones planificadas o devueltas para corrección.';
    END IF;

    SELECT id_tipo_estado_registro INTO v_id_estado_pendiente FROM ayudantia.tipo_estado_registro WHERE codigo = 'PENDIENTE';

    UPDATE ayudantia.registro_actividad
    SET descripcion_actividad = p_descripcion_actividad,
        id_tipo_estado_registro = v_id_estado_pendiente
    WHERE id_registro_actividad = p_id_registro_actividad;

    RETURN TRUE;
END;
$$;


--
-- Name: fn_consultar_asistencia(integer); Type: FUNCTION; Schema: ayudantia; Owner: -
--

CREATE FUNCTION ayudantia.fn_consultar_asistencia(p_id_registro integer) RETURNS jsonb
    LANGUAGE plpgsql STABLE SECURITY DEFINER
    AS $$
BEGIN
    RETURN COALESCE(
            (SELECT jsonb_agg(
                            jsonb_build_object(
                                    'idDetalle',      d.id_detalle_asistencia_actividad,
                                    'idParticipante', p.id_participante_ayudantia,
                                    'nombreCompleto', p.nombre_completo,
                                    'curso',          COALESCE(p.curso,    ''),
                                    'paralelo',       COALESCE(p.paralelo, ''),
                                    'asistio',        d.asistio
                            ) ORDER BY p.nombre_completo
                    )
             FROM ayudantia.detalle_asistencia_actividad d
                      JOIN ayudantia.participante_ayudantia p
                           ON p.id_participante_ayudantia = d.id_participante_ayudantia
             WHERE d.id_registro_actividad = p_id_registro),
            '[]'::JSONB
           );

EXCEPTION WHEN OTHERS THEN
    RAISE EXCEPTION '[ERROR] fn_consultar_asistencia: %', SQLERRM;
END;
$$;


--
-- Name: fn_consultar_participantes(integer); Type: FUNCTION; Schema: ayudantia; Owner: -
--

CREATE FUNCTION ayudantia.fn_consultar_participantes(p_id_ayudantia integer) RETURNS jsonb
    LANGUAGE plpgsql STABLE SECURITY DEFINER
    AS $$
BEGIN
    RETURN COALESCE(
            (SELECT jsonb_agg(
                            jsonb_build_object(
                                    'idParticipante', pa.id_participante_ayudantia,
                                    'idAyudantia',    pa.id_ayudantia,
                                    'nombreCompleto', pa.nombre_completo,
                                    'curso',          COALESCE(pa.curso,    ''),
                                    'paralelo',       COALESCE(pa.paralelo, ''),
                                    'activo',         pa.activo
                            ) ORDER BY pa.nombre_completo
                    )
             FROM ayudantia.participante_ayudantia pa
             WHERE pa.id_ayudantia = p_id_ayudantia
               AND pa.activo = TRUE),
            '[]'::JSONB
           );

EXCEPTION WHEN OTHERS THEN
    RAISE EXCEPTION '[ERROR] fn_consultar_participantes: %', SQLERRM;
END;
$$;


--
-- Name: fn_control_semanal(integer); Type: FUNCTION; Schema: ayudantia; Owner: -
--

CREATE FUNCTION ayudantia.fn_control_semanal(p_id_usuario integer) RETURNS TABLE(semana_inicio date, semana_fin date, horas_registradas numeric, horas_aprobadas_semana numeric, horas_pendientes_semana numeric, limite_semanal numeric, horas_disponibles numeric, supera_limite boolean, sesiones_esta_semana bigint)
    LANGUAGE plpgsql
    AS $$
DECLARE
    v_id_ayudantia  INTEGER;
    v_sem_ini DATE := DATE_TRUNC('week', CURRENT_DATE)::DATE;
    v_sem_fin DATE := (DATE_TRUNC('week', CURRENT_DATE) + INTERVAL '6 days')::DATE;
BEGIN
    v_id_ayudantia := ayudantia.fn_obtener_id_ayudantia(p_id_usuario);

    -- Si no hay ayudantía, devolvemos datos vacíos con las fechas actuales
    IF v_id_ayudantia IS NULL THEN
        RETURN QUERY SELECT
                         v_sem_ini, v_sem_fin, 0::NUMERIC, 0::NUMERIC, 0::NUMERIC,
                         0::NUMERIC, 0::NUMERIC, FALSE, 0::BIGINT;
        RETURN;
    END IF;

    RETURN QUERY
        SELECT
            v_sem_ini,
            v_sem_fin,
            COALESCE(SUM(ra.horas_dedicadas), 0)::NUMERIC AS horas_registradas,
            COALESCE(SUM(ra.horas_dedicadas) FILTER (WHERE ter.codigo = 'APROBADO'), 0)::NUMERIC AS horas_aprobadas_semana,
            COALESCE(SUM(ra.horas_dedicadas) FILTER (WHERE ter.codigo = 'PENDIENTE'), 0)::NUMERIC AS horas_pendientes_semana,
            a.horas_semanales_max::NUMERIC AS limite_semanal,
            GREATEST(a.horas_semanales_max - COALESCE(SUM(ra.horas_dedicadas), 0), 0)::NUMERIC AS horas_disponibles,
            COALESCE(SUM(ra.horas_dedicadas), 0) > a.horas_semanales_max AS supera_limite,
            COUNT(ra.id_registro_actividad) AS sesiones_esta_semana
        FROM ayudantia.ayudantia a
                 LEFT JOIN ayudantia.registro_actividad ra ON ra.id_ayudantia = a.id_ayudantia
            AND ra.fecha BETWEEN v_sem_ini AND v_sem_fin
                 LEFT JOIN ayudantia.tipo_estado_registro ter ON ter.id_tipo_estado_registro = ra.id_tipo_estado_registro
        WHERE a.id_ayudantia = v_id_ayudantia
        GROUP BY a.id_ayudantia, a.horas_semanales_max;
END;
$$;


--
-- Name: fn_crear_tipo_estado_ayudantia(character varying, text, character varying); Type: FUNCTION; Schema: ayudantia; Owner: -
--

CREATE FUNCTION ayudantia.fn_crear_tipo_estado_ayudantia(p_nombre_estado character varying, p_descripcion text, p_codigo character varying) RETURNS jsonb
    LANGUAGE plpgsql SECURITY DEFINER
    AS $$
DECLARE
    v_id INTEGER;
BEGIN
    -- Validar que el nombre no exista (case-insensitive)
    IF EXISTS (
        SELECT 1 FROM ayudantia.tipo_estado_ayudantia
        WHERE LOWER(TRIM(nombre_estado)) = LOWER(TRIM(p_nombre_estado))
    ) THEN
        RETURN jsonb_build_object('exito', false, 'mensaje', 'Ya existe un estado de ayudantía con el nombre especificado');
    END IF;

    -- Validar que el código no exista (case-insensitive)
    IF EXISTS (
        SELECT 1 FROM ayudantia.tipo_estado_ayudantia
        WHERE LOWER(TRIM(codigo)) = LOWER(TRIM(p_codigo))
    ) THEN
        RETURN jsonb_build_object('exito', false, 'mensaje', 'Ya existe un estado de ayudantía con el código especificado');
    END IF;

    -- Insertar el nuevo registro
    INSERT INTO ayudantia.tipo_estado_ayudantia (nombre_estado, descripcion, codigo, activo)
    VALUES (TRIM(p_nombre_estado), TRIM(p_descripcion), UPPER(TRIM(p_codigo)), TRUE)
    RETURNING id_tipo_estado_ayudantia INTO v_id;

    RETURN jsonb_build_object(
        'exito', true,
        'mensaje', 'Estado de ayudantía creado exitosamente',
        'id', v_id
    );

EXCEPTION
    WHEN unique_violation THEN
        RETURN jsonb_build_object('exito', false, 'mensaje', 'Error de duplicidad: el nombre o código ya existe');
    WHEN OTHERS THEN
        RETURN jsonb_build_object('exito', false, 'mensaje', 'Error al crear estado de ayudantía: ' || SQLERRM);
END;
$$;


--
-- Name: fn_crear_tipo_estado_evidencia(character varying, text, character varying); Type: FUNCTION; Schema: ayudantia; Owner: -
--

CREATE FUNCTION ayudantia.fn_crear_tipo_estado_evidencia(p_nombre_estado character varying, p_descripcion text, p_codigo character varying) RETURNS jsonb
    LANGUAGE plpgsql SECURITY DEFINER
    AS $$
DECLARE
    v_id INTEGER;
BEGIN
    -- Validar que el nombre no exista (case-insensitive)
    IF EXISTS (
        SELECT 1 FROM ayudantia.tipo_estado_evidencia
        WHERE LOWER(TRIM(nombre_estado)) = LOWER(TRIM(p_nombre_estado))
    ) THEN
        RETURN jsonb_build_object('exito', false, 'mensaje', 'Ya existe un estado de evidencia con el nombre especificado');
    END IF;

    -- Validar que el código no exista (case-insensitive)
    IF EXISTS (
        SELECT 1 FROM ayudantia.tipo_estado_evidencia
        WHERE LOWER(TRIM(codigo)) = LOWER(TRIM(p_codigo))
    ) THEN
        RETURN jsonb_build_object('exito', false, 'mensaje', 'Ya existe un estado de evidencia con el código especificado');
    END IF;

    -- Insertar el nuevo registro
    INSERT INTO ayudantia.tipo_estado_evidencia (nombre_estado, descripcion, codigo, activo)
    VALUES (TRIM(p_nombre_estado), TRIM(p_descripcion), UPPER(TRIM(p_codigo)), TRUE)
    RETURNING id_tipo_estado_evidencia INTO v_id;

    RETURN jsonb_build_object(
        'exito', true,
        'mensaje', 'Estado de evidencia creado exitosamente',
        'id', v_id
    );

EXCEPTION
    WHEN unique_violation THEN
        RETURN jsonb_build_object('exito', false, 'mensaje', 'Error de duplicidad: el nombre o código ya existe');
    WHEN OTHERS THEN
        RETURN jsonb_build_object('exito', false, 'mensaje', 'Error al crear estado de evidencia: ' || SQLERRM);
END;
$$;


--
-- Name: fn_crear_tipo_estado_registro(character varying, text, character varying); Type: FUNCTION; Schema: ayudantia; Owner: -
--

CREATE FUNCTION ayudantia.fn_crear_tipo_estado_registro(p_nombre_estado character varying, p_descripcion text, p_codigo character varying) RETURNS jsonb
    LANGUAGE plpgsql SECURITY DEFINER
    AS $$
DECLARE
    v_id INTEGER;
BEGIN
    -- Validar que el nombre no exista (case-insensitive)
    IF EXISTS (
        SELECT 1 FROM ayudantia.tipo_estado_registro
        WHERE LOWER(TRIM(nombre_estado)) = LOWER(TRIM(p_nombre_estado))
    ) THEN
        RETURN jsonb_build_object('exito', false, 'mensaje', 'Ya existe un estado de registro con el nombre especificado');
    END IF;

    -- Validar que el código no exista (case-insensitive)
    IF EXISTS (
        SELECT 1 FROM ayudantia.tipo_estado_registro
        WHERE LOWER(TRIM(codigo)) = LOWER(TRIM(p_codigo))
    ) THEN
        RETURN jsonb_build_object('exito', false, 'mensaje', 'Ya existe un estado de registro con el código especificado');
    END IF;

    -- Insertar el nuevo registro
    INSERT INTO ayudantia.tipo_estado_registro (nombre_estado, descripcion, codigo, activo)
    VALUES (TRIM(p_nombre_estado), TRIM(p_descripcion), UPPER(TRIM(p_codigo)), TRUE)
    RETURNING id_tipo_estado_registro INTO v_id;

    RETURN jsonb_build_object(
        'exito', true,
        'mensaje', 'Estado de registro creado exitosamente',
        'id', v_id
    );

EXCEPTION
    WHEN unique_violation THEN
        RETURN jsonb_build_object('exito', false, 'mensaje', 'Error de duplicidad: el nombre o código ya existe');
    WHEN OTHERS THEN
        RETURN jsonb_build_object('exito', false, 'mensaje', 'Error al crear estado de registro: ' || SQLERRM);
END;
$$;


--
-- Name: fn_crear_tipo_evidencia(character varying, character varying, character varying); Type: FUNCTION; Schema: ayudantia; Owner: -
--

CREATE FUNCTION ayudantia.fn_crear_tipo_evidencia(p_nombre character varying, p_extension_permitida character varying, p_codigo character varying) RETURNS jsonb
    LANGUAGE plpgsql SECURITY DEFINER
    AS $$
DECLARE
    v_id INTEGER;
BEGIN
    -- Validar que el nombre no exista (case-insensitive)
    IF EXISTS (
        SELECT 1 FROM ayudantia.tipo_evidencia
        WHERE LOWER(TRIM(nombre)) = LOWER(TRIM(p_nombre))
    ) THEN
        RETURN jsonb_build_object('exito', false, 'mensaje', 'Ya existe un tipo de evidencia con el nombre especificado');
    END IF;

    -- Validar que el código no exista (case-insensitive)
    IF EXISTS (
        SELECT 1 FROM ayudantia.tipo_evidencia
        WHERE LOWER(TRIM(codigo)) = LOWER(TRIM(p_codigo))
    ) THEN
        RETURN jsonb_build_object('exito', false, 'mensaje', 'Ya existe un tipo de evidencia con el código especificado');
    END IF;

    -- Insertar el nuevo registro
    INSERT INTO ayudantia.tipo_evidencia (nombre, extension_permitida, codigo, activo)
    VALUES (TRIM(p_nombre), LOWER(TRIM(p_extension_permitida)), UPPER(TRIM(p_codigo)), TRUE)
    RETURNING id_tipo_evidencia INTO v_id;

    RETURN jsonb_build_object(
        'exito', true,
        'mensaje', 'Tipo de evidencia creado exitosamente',
        'id', v_id
    );

EXCEPTION
    WHEN unique_violation THEN
        RETURN jsonb_build_object('exito', false, 'mensaje', 'Error de duplicidad: el nombre o código ya existe');
    WHEN OTHERS THEN
        RETURN jsonb_build_object('exito', false, 'mensaje', 'Error al crear tipo de evidencia: ' || SQLERRM);
END;
$$;


--
-- Name: fn_crear_tipo_sancion_ayudante_catedra(character varying, character varying); Type: FUNCTION; Schema: ayudantia; Owner: -
--

CREATE FUNCTION ayudantia.fn_crear_tipo_sancion_ayudante_catedra(p_nombre_tipo_sancion character varying, p_codigo character varying) RETURNS jsonb
    LANGUAGE plpgsql SECURITY DEFINER
    AS $$
DECLARE
    v_id INTEGER;
BEGIN
    -- Validar que el nombre no exista (case-insensitive)
    IF EXISTS (
        SELECT 1 FROM ayudantia.tipo_sancion_ayudante_catedra
        WHERE LOWER(TRIM(nombre_tipo_sancion)) = LOWER(TRIM(p_nombre_tipo_sancion))
    ) THEN
        RETURN jsonb_build_object('exito', false, 'mensaje', 'Ya existe un tipo de sanción con el nombre especificado');
    END IF;

    -- Validar que el código no exista (case-insensitive)
    IF EXISTS (
        SELECT 1 FROM ayudantia.tipo_sancion_ayudante_catedra
        WHERE LOWER(TRIM(codigo)) = LOWER(TRIM(p_codigo))
    ) THEN
        RETURN jsonb_build_object('exito', false, 'mensaje', 'Ya existe un tipo de sanción con el código especificado');
    END IF;

    -- Insertar el nuevo registro
    INSERT INTO ayudantia.tipo_sancion_ayudante_catedra (nombre_tipo_sancion, codigo, activo)
    VALUES (TRIM(p_nombre_tipo_sancion), UPPER(TRIM(p_codigo)), TRUE)
    RETURNING id_tipo_sancion_ayudante_catedra INTO v_id;

    RETURN jsonb_build_object(
        'exito', true,
        'mensaje', 'Tipo de sanción creado exitosamente',
        'id', v_id
    );

EXCEPTION
    WHEN unique_violation THEN
        RETURN jsonb_build_object('exito', false, 'mensaje', 'Error de duplicidad: el nombre o código ya existe');
    WHEN OTHERS THEN
        RETURN jsonb_build_object('exito', false, 'mensaje', 'Error al crear tipo de sanción: ' || SQLERRM);
END;
$$;


--
-- Name: fn_detalle_sesion(integer, integer); Type: FUNCTION; Schema: ayudantia; Owner: -
--

CREATE FUNCTION ayudantia.fn_detalle_sesion(p_id_usuario integer, p_id_registro integer) RETURNS TABLE(id_registro integer, fecha date, tema_tratado text, descripcion text, numero_asistentes integer, horas_dedicadas numeric, estado character varying, nombre_asignatura character varying, nombre_docente text, nombre_periodo character varying)
    LANGUAGE plpgsql
    AS $$
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
$$;


--
-- Name: fn_eliminar_documento(integer); Type: FUNCTION; Schema: ayudantia; Owner: -
--

CREATE FUNCTION ayudantia.fn_eliminar_documento(p_id_documento integer) RETURNS seguridad.res_operacion
    LANGUAGE plpgsql
    AS $$
DECLARE
    v_ruta_archivo varchar;
BEGIN
    UPDATE ayudantia.documento_academico
    SET activo = false
    WHERE id_documento = p_id_documento
    RETURNING ruta_archivo INTO v_ruta_archivo;

    IF NOT FOUND THEN
        RETURN (false, 'El documento no existe o ya fue eliminado'::text, NULL)::seguridad.res_operacion;
    END IF;

    RETURN (true, 'Documento eliminado de la base de datos'::text, json_build_object('ruta', v_ruta_archivo))::seguridad.res_operacion;

EXCEPTION
    WHEN OTHERS THEN
        RETURN (false, 'Error al eliminar: ' || SQLERRM, NULL)::seguridad.res_operacion;
END;
$$;


--
-- Name: fn_eliminar_evidencia_sesion(integer, integer); Type: FUNCTION; Schema: ayudantia; Owner: -
--

CREATE FUNCTION ayudantia.fn_eliminar_evidencia_sesion(p_id_usuario integer, p_id_evidencia integer) RETURNS seguridad.res_operacion
    LANGUAGE plpgsql
    AS $$
DECLARE
    v_id_ayudantia_user INTEGER;
    v_id_registro_asoc INTEGER;
    v_estado_sesion VARCHAR;
    v_ruta_eliminada VARCHAR;
BEGIN
    SELECT a.id_ayudantia INTO v_id_ayudantia_user
    FROM ayudantia.ayudantia a
             JOIN postulacion.postulacion p ON a.id_postulacion = p.id_postulacion
             JOIN academico.estudiante e ON p.id_estudiante = e.id_estudiante
    WHERE e.id_usuario = p_id_usuario LIMIT 1;

    SELECT ra.id_registro_actividad, tr.codigo
    INTO v_id_registro_asoc, v_estado_sesion
    FROM ayudantia.evidencia_registro_actividad ev
             JOIN ayudantia.registro_actividad ra ON ev.id_registro_actividad = ra.id_registro_actividad
             JOIN ayudantia.tipo_estado_registro tr ON ra.id_tipo_estado_registro = tr.id_tipo_estado_registro
    WHERE ev.id_evidencia_registro_actividad = p_id_evidencia AND ra.id_ayudantia = v_id_ayudantia_user;

    IF v_id_registro_asoc IS NULL THEN
        RETURN (FALSE, 'La evidencia no existe o no tiene permisos.', NULL)::seguridad.res_operacion;
    ELSIF v_estado_sesion NOT IN ('PLANIFICADO', 'RECHAZADO') THEN
        RETURN (FALSE, 'No puede eliminar archivos de una sesión enviada a revisión.', NULL)::seguridad.res_operacion;
    END IF;

    DELETE FROM ayudantia.evidencia_registro_actividad
    WHERE id_evidencia_registro_actividad = p_id_evidencia
    RETURNING ruta_archivo INTO v_ruta_eliminada;

    RETURN (TRUE, 'Archivo eliminado de la sesión.', json_build_object(
            'id_evidencia_eliminada', p_id_evidencia,
            'ruta_archivo', v_ruta_eliminada
                                                     ))::seguridad.res_operacion;

EXCEPTION WHEN OTHERS THEN
    RETURN (FALSE, 'Error al eliminar evidencia: ' || SQLERRM, NULL)::seguridad.res_operacion;
END;
$$;


--
-- Name: fn_eliminar_tipo_estado_ayudantia(integer); Type: FUNCTION; Schema: ayudantia; Owner: -
--

CREATE FUNCTION ayudantia.fn_eliminar_tipo_estado_ayudantia(p_id integer) RETURNS jsonb
    LANGUAGE plpgsql SECURITY DEFINER
    AS $$
BEGIN
    -- Validar que el registro exista
    IF NOT EXISTS (
        SELECT 1 FROM ayudantia.tipo_estado_ayudantia
        WHERE id_tipo_estado_ayudantia = p_id
    ) THEN
        RETURN jsonb_build_object('exito', false, 'mensaje', 'No se encontró el estado de ayudantía con el ID especificado');
    END IF;

    -- Eliminación lógica: setear activo = false
    UPDATE ayudantia.tipo_estado_ayudantia
    SET activo = FALSE
    WHERE id_tipo_estado_ayudantia = p_id;

    RETURN jsonb_build_object(
        'exito', true,
        'mensaje', 'Estado de ayudantía desactivado exitosamente',
        'id', p_id
    );

EXCEPTION
    WHEN OTHERS THEN
        RETURN jsonb_build_object('exito', false, 'mensaje', 'Error al desactivar estado de ayudantía: ' || SQLERRM);
END;
$$;


--
-- Name: fn_eliminar_tipo_estado_evidencia(integer); Type: FUNCTION; Schema: ayudantia; Owner: -
--

CREATE FUNCTION ayudantia.fn_eliminar_tipo_estado_evidencia(p_id integer) RETURNS jsonb
    LANGUAGE plpgsql SECURITY DEFINER
    AS $$
BEGIN
    -- Validar que el registro exista
    IF NOT EXISTS (
        SELECT 1 FROM ayudantia.tipo_estado_evidencia
        WHERE id_tipo_estado_evidencia = p_id
    ) THEN
        RETURN jsonb_build_object('exito', false, 'mensaje', 'No se encontró el estado de evidencia con el ID especificado');
    END IF;

    -- Eliminación lógica: setear activo = false
    UPDATE ayudantia.tipo_estado_evidencia
    SET activo = FALSE
    WHERE id_tipo_estado_evidencia = p_id;

    RETURN jsonb_build_object(
        'exito', true,
        'mensaje', 'Estado de evidencia desactivado exitosamente',
        'id', p_id
    );

EXCEPTION
    WHEN OTHERS THEN
        RETURN jsonb_build_object('exito', false, 'mensaje', 'Error al desactivar estado de evidencia: ' || SQLERRM);
END;
$$;


--
-- Name: fn_eliminar_tipo_estado_registro(integer); Type: FUNCTION; Schema: ayudantia; Owner: -
--

CREATE FUNCTION ayudantia.fn_eliminar_tipo_estado_registro(p_id integer) RETURNS jsonb
    LANGUAGE plpgsql SECURITY DEFINER
    AS $$
BEGIN
    -- Validar que el registro exista
    IF NOT EXISTS (
        SELECT 1 FROM ayudantia.tipo_estado_registro
        WHERE id_tipo_estado_registro = p_id
    ) THEN
        RETURN jsonb_build_object('exito', false, 'mensaje', 'No se encontró el estado de registro con el ID especificado');
    END IF;

    -- Eliminación lógica: setear activo = false
    UPDATE ayudantia.tipo_estado_registro
    SET activo = FALSE
    WHERE id_tipo_estado_registro = p_id;

    RETURN jsonb_build_object(
        'exito', true,
        'mensaje', 'Estado de registro desactivado exitosamente',
        'id', p_id
    );

EXCEPTION
    WHEN OTHERS THEN
        RETURN jsonb_build_object('exito', false, 'mensaje', 'Error al desactivar estado de registro: ' || SQLERRM);
END;
$$;


--
-- Name: fn_eliminar_tipo_evidencia(integer); Type: FUNCTION; Schema: ayudantia; Owner: -
--

CREATE FUNCTION ayudantia.fn_eliminar_tipo_evidencia(p_id integer) RETURNS jsonb
    LANGUAGE plpgsql SECURITY DEFINER
    AS $$
BEGIN
    -- Validar que el registro exista
    IF NOT EXISTS (
        SELECT 1 FROM ayudantia.tipo_evidencia
        WHERE id_tipo_evidencia = p_id
    ) THEN
        RETURN jsonb_build_object('exito', false, 'mensaje', 'No se encontró el tipo de evidencia con el ID especificado');
    END IF;

    -- Eliminación lógica: setear activo = false
    UPDATE ayudantia.tipo_evidencia
    SET activo = FALSE
    WHERE id_tipo_evidencia = p_id;

    RETURN jsonb_build_object(
        'exito', true,
        'mensaje', 'Tipo de evidencia desactivado exitosamente',
        'id', p_id
    );

EXCEPTION
    WHEN OTHERS THEN
        RETURN jsonb_build_object('exito', false, 'mensaje', 'Error al desactivar tipo de evidencia: ' || SQLERRM);
END;
$$;


--
-- Name: fn_eliminar_tipo_sancion_ayudante_catedra(integer); Type: FUNCTION; Schema: ayudantia; Owner: -
--

CREATE FUNCTION ayudantia.fn_eliminar_tipo_sancion_ayudante_catedra(p_id integer) RETURNS jsonb
    LANGUAGE plpgsql SECURITY DEFINER
    AS $$
BEGIN
    -- Validar que el registro exista
    IF NOT EXISTS (
        SELECT 1 FROM ayudantia.tipo_sancion_ayudante_catedra
        WHERE id_tipo_sancion_ayudante_catedra = p_id
    ) THEN
        RETURN jsonb_build_object('exito', false, 'mensaje', 'No se encontró el tipo de sanción con el ID especificado');
    END IF;

    -- Eliminación lógica: setear activo = false
    UPDATE ayudantia.tipo_sancion_ayudante_catedra
    SET activo = FALSE
    WHERE id_tipo_sancion_ayudante_catedra = p_id;

    RETURN jsonb_build_object(
        'exito', true,
        'mensaje', 'Tipo de sanción desactivado exitosamente',
        'id', p_id
    );

EXCEPTION
    WHEN OTHERS THEN
        RETURN jsonb_build_object('exito', false, 'mensaje', 'Error al desactivar tipo de sanción: ' || SQLERRM);
END;
$$;


--
-- Name: fn_evaluar_actividad(integer, character varying, text); Type: FUNCTION; Schema: ayudantia; Owner: -
--

CREATE FUNCTION ayudantia.fn_evaluar_actividad(p_id_registro_actividad integer, p_codigo_nuevo_estado character varying, p_observaciones text) RETURNS boolean
    LANGUAGE plpgsql
    AS $$
DECLARE
    v_id_estado_actual INTEGER;
    v_id_estado_pendiente INTEGER;
    v_id_nuevo_estado INTEGER;
    v_horas_dedicadas NUMERIC(5,2);
    v_id_ayudantia INTEGER;
BEGIN
    SELECT id_tipo_estado_registro, horas_dedicadas, id_ayudantia
    INTO v_id_estado_actual, v_horas_dedicadas, v_id_ayudantia
    FROM ayudantia.registro_actividad
    WHERE id_registro_actividad = p_id_registro_actividad;

    SELECT id_tipo_estado_registro INTO v_id_estado_pendiente FROM ayudantia.tipo_estado_registro WHERE codigo = 'PENDIENTE';
    IF v_id_estado_actual != v_id_estado_pendiente THEN
        RAISE EXCEPTION 'Solo se pueden evaluar sesiones que estén pendientes de revisión.';
    END IF;

    SELECT id_tipo_estado_registro INTO v_id_nuevo_estado FROM ayudantia.tipo_estado_registro WHERE codigo = p_codigo_nuevo_estado;
    IF v_id_nuevo_estado IS NULL THEN
        RAISE EXCEPTION 'El estado de evaluación no es válido.';
    END IF;

    IF p_codigo_nuevo_estado = 'RECHAZADO' AND (p_observaciones IS NULL OR trim(p_observaciones) = '') THEN
        RAISE EXCEPTION 'Para rechazar/observar una sesión, es obligatorio ingresar el motivo.';
    END IF;

    UPDATE ayudantia.registro_actividad
    SET id_tipo_estado_registro = v_id_nuevo_estado,
        observaciones = p_observaciones,
        fecha_observacion = CURRENT_DATE
    WHERE id_registro_actividad = p_id_registro_actividad;

    IF p_codigo_nuevo_estado = 'APROBADO' THEN
        UPDATE ayudantia.ayudantia
        SET horas_cumplidas = COALESCE(horas_cumplidas, 0) + v_horas_dedicadas
        WHERE id_ayudantia = v_id_ayudantia;
    END IF;

    RETURN TRUE;
END;
$$;


--
-- Name: fn_evidencias_sesion(integer, integer); Type: FUNCTION; Schema: ayudantia; Owner: -
--

CREATE FUNCTION ayudantia.fn_evidencias_sesion(p_id_usuario integer, p_id_registro integer) RETURNS TABLE(id_evidencia integer, nombre_archivo character varying, ruta_archivo character varying, mime_type character varying, tamanio_bytes integer, tipo_evidencia character varying, estado_evidencia character varying, fecha_subida date)
    LANGUAGE plpgsql
    AS $$
DECLARE
    v_id_ayudantia INTEGER;
BEGIN
    v_id_ayudantia := ayudantia.fn_obtener_id_ayudantia(p_id_usuario);

    IF v_id_ayudantia IS NULL THEN
        RETURN;
    END IF;

    -- Verificar que el registro pertenece a este ayudante
    PERFORM 1
    FROM ayudantia.registro_actividad
    WHERE id_registro_actividad = p_id_registro
      AND id_ayudantia = v_id_ayudantia;

    IF NOT FOUND THEN
        RETURN;
    END IF;

    RETURN QUERY
    SELECT
        era.id_evidencia_registro_actividad,
        era.nombre_archivo,
        era.ruta_archivo,
        era.mime_type,
        era.tamanio_bytes,
        te.nombre,
        tee.nombre_estado,
        era.fecha_subida
    FROM ayudantia.evidencia_registro_actividad era
    JOIN ayudantia.tipo_evidencia        te  ON te.id_tipo_evidencia        = era.id_tipo_evidencia
    JOIN ayudantia.tipo_estado_evidencia tee ON tee.id_tipo_estado_evidencia = era.id_tipo_estado_evidencia
    WHERE era.id_registro_actividad = p_id_registro
      AND era.activo = true
    ORDER BY era.fecha_subida;
END;
$$;


--
-- Name: fn_finalizar_sesion(integer, integer, text); Type: FUNCTION; Schema: ayudantia; Owner: -
--

CREATE FUNCTION ayudantia.fn_finalizar_sesion(p_id_usuario integer, p_id_registro integer, p_descripcion text) RETURNS seguridad.res_operacion
    LANGUAGE plpgsql
    AS $$
DECLARE
    v_id_ayudantia_user INTEGER;
    v_sesion_actual ayudantia.registro_actividad%ROWTYPE;
    v_conteo_evidencias INTEGER;
    -- Variables para el resumen de asistencia
    v_total_estudiantes INTEGER;
    v_asistieron INTEGER;
    v_faltaron INTEGER;
BEGIN
    SELECT a.id_ayudantia INTO v_id_ayudantia_user
    FROM ayudantia.ayudantia a
             JOIN postulacion.postulacion p ON a.id_postulacion = p.id_postulacion
             JOIN academico.estudiante e ON p.id_estudiante = e.id_estudiante
    WHERE e.id_usuario = p_id_usuario
      AND a.id_tipo_estado_ayudantia = (SELECT id_tipo_estado_ayudantia FROM ayudantia.tipo_estado_ayudantia WHERE codigo = 'ACTIVO')
    LIMIT 1;

    SELECT * INTO v_sesion_actual FROM ayudantia.registro_actividad WHERE id_registro_actividad = p_id_registro;

    IF v_sesion_actual.id_registro_actividad IS NULL THEN
        RETURN (FALSE, 'La sesión no existe.', NULL)::seguridad.res_operacion;
    END IF;

    IF v_sesion_actual.id_ayudantia != v_id_ayudantia_user THEN
        RETURN (FALSE, 'No tiene permisos para finalizar esta sesión.', NULL)::seguridad.res_operacion;
    END IF;

    IF v_sesion_actual.id_tipo_estado_registro NOT IN (
        SELECT id_tipo_estado_registro FROM ayudantia.tipo_estado_registro WHERE codigo IN ('PLANIFICADO', 'RECHAZADO')
    ) THEN
        RETURN (FALSE, 'Esta sesión ya ha sido procesada o está en revisión.', NULL)::seguridad.res_operacion;
    END IF;

    IF LENGTH(TRIM(p_descripcion)) < 20 THEN
        RETURN (FALSE, 'La descripción de la actividad es insuficiente para el reporte docente.', NULL)::seguridad.res_operacion;
    END IF;

    SELECT COUNT(*) INTO v_conteo_evidencias
    FROM ayudantia.evidencia_registro_actividad
    WHERE id_registro_actividad = p_id_registro;

    IF v_conteo_evidencias = 0 THEN
        RETURN (FALSE, 'Es obligatorio adjuntar al menos una evidencia antes de finalizar.', NULL)::seguridad.res_operacion;
    END IF;

    SELECT
        COUNT(*),
        COUNT(*) FILTER (WHERE asistio = TRUE),
        COUNT(*) FILTER (WHERE asistio = FALSE)
    INTO v_total_estudiantes, v_asistieron, v_faltaron
    FROM ayudantia.detalle_asistencia_actividad
    WHERE id_registro_actividad = p_id_registro;

    IF v_total_estudiantes = 0 THEN
        RETURN (FALSE, 'No se puede finalizar una sesión sin un registro de asistencia generado.', NULL)::seguridad.res_operacion;
    END IF;

    UPDATE ayudantia.registro_actividad
    SET descripcion_actividad = p_descripcion,
        id_tipo_estado_registro = (SELECT id_tipo_estado_registro FROM ayudantia.tipo_estado_registro WHERE codigo = 'PENDIENTE'),
        fecha_observacion = NULL,
        observaciones = NULL
    WHERE id_registro_actividad = p_id_registro;

    RETURN (
            TRUE,
            'Sesión finalizada correctamente. Enviada para revisión del docente.',
            json_build_object(
                    'id_registro', p_id_registro,
                    'resumen_asistencia', json_build_object(
                            'total', v_total_estudiantes,
                            'asistieron', v_asistieron,
                            'faltaron', v_faltaron
                                          ),
                    'evidencias_adjuntas', v_conteo_evidencias
            )
        )::seguridad.res_operacion;

EXCEPTION WHEN OTHERS THEN
    RETURN (FALSE, 'Error crítico al finalizar sesión: ' || SQLERRM, NULL)::seguridad.res_operacion;
END;
$$;


--
-- Name: fn_generar_snapshot_asistencia(integer); Type: FUNCTION; Schema: ayudantia; Owner: -
--

CREATE FUNCTION ayudantia.fn_generar_snapshot_asistencia(p_id_registro integer) RETURNS seguridad.res_operacion
    LANGUAGE plpgsql
    AS $$
DECLARE
    v_id_ayudantia_reg INTEGER;
    v_total_estudiantes INTEGER;
BEGIN
    SELECT id_ayudantia INTO v_id_ayudantia_reg FROM ayudantia.registro_actividad WHERE id_registro_actividad = p_id_registro;

    INSERT INTO ayudantia.detalle_asistencia_actividad (id_registro_actividad, id_participante_ayudantia, asistio)
    SELECT p_id_registro, id_participante_ayudantia, FALSE
    FROM ayudantia.participante_ayudantia
    WHERE id_ayudantia = v_id_ayudantia_reg
      AND activo = TRUE
      AND id_participante_ayudantia NOT IN (
        SELECT id_participante_ayudantia FROM ayudantia.detalle_asistencia_actividad WHERE id_registro_actividad = p_id_registro
    );

    GET DIAGNOSTICS v_total_estudiantes = ROW_COUNT;

    IF v_total_estudiantes = 0 THEN
        IF NOT EXISTS (SELECT 1 FROM ayudantia.participante_ayudantia WHERE id_ayudantia = v_id_ayudantia_reg AND activo = TRUE) THEN
            RETURN (TRUE, 'Sesión creada, pero su padrón de estudiantes está vacío. Regístrelos para poder tomar asistencia.', json_build_object('total', 0))::seguridad.res_operacion;
        END IF;
    END IF;

    RETURN (TRUE, format('Asistencia preparada con %s estudiantes.', v_total_estudiantes), json_build_object('total', v_total_estudiantes))::seguridad.res_operacion;
END;
$$;


--
-- Name: fn_gestionar_participante(character varying, integer, character varying, character varying, character varying, integer); Type: FUNCTION; Schema: ayudantia; Owner: -
--

CREATE FUNCTION ayudantia.fn_gestionar_participante(p_accion character varying, p_id_usuario integer, p_nombre character varying, p_curso character varying, p_paralelo character varying, p_id_participante integer DEFAULT NULL::integer) RETURNS seguridad.res_operacion
    LANGUAGE plpgsql
    AS $$
DECLARE
    v_id_ayudantia INTEGER;
    v_id_ayudante_cat INTEGER;
    v_mensaje VARCHAR;
    v_id_nuevo INTEGER;
BEGIN
    SELECT id_rol_especifico INTO v_id_ayudante_cat
    FROM seguridad.fn_identidad_usuario(p_id_usuario)
    WHERE nombre_rol = 'AYUDANTE_CATEDRA';

    IF v_id_ayudante_cat IS NULL THEN
        RETURN (FALSE, 'El usuario no tiene un perfil de Ayudante de Cátedra activo.', NULL)::seguridad.res_operacion;
    END IF;

    SELECT a.id_ayudantia INTO v_id_ayudantia
    FROM ayudantia.ayudantia a
             JOIN postulacion.postulacion p ON a.id_postulacion = p.id_postulacion
             JOIN academico.estudiante e ON p.id_estudiante = e.id_estudiante
    WHERE e.id_usuario = p_id_usuario
      AND a.id_tipo_estado_ayudantia = (SELECT id_tipo_estado_ayudantia FROM ayudantia.tipo_estado_ayudantia WHERE codigo = 'ACTIVO')
    LIMIT 1;

    IF v_id_ayudantia IS NULL THEN
        RETURN (FALSE, 'No se encontró una ayudantía activa vinculada a su cuenta.', NULL)::seguridad.res_operacion;
    END IF;

    IF p_accion = 'INS' THEN
        IF EXISTS (SELECT 1 FROM ayudantia.participante_ayudantia WHERE id_ayudantia = v_id_ayudantia AND nombre_completo = p_nombre AND curso = p_curso AND paralelo = p_paralelo) THEN
            RETURN (FALSE, 'El estudiante ya está en su lista de este curso.', NULL)::seguridad.res_operacion;
        END IF;

        INSERT INTO ayudantia.participante_ayudantia (id_ayudantia, nombre_completo, curso, paralelo, activo)
        VALUES (v_id_ayudantia, p_nombre, p_curso, p_paralelo, true)
        RETURNING id_participante_ayudantia INTO v_id_nuevo;

        v_mensaje := 'Estudiante registrado en su padrón exitosamente.';

    ELSIF p_accion = 'UPD' THEN
        IF p_id_participante IS NULL THEN
            RETURN (FALSE, 'ID de participante requerido para actualización.', NULL)::seguridad.res_operacion;
        END IF;

        UPDATE ayudantia.participante_ayudantia
        SET nombre_completo = p_nombre, curso = p_curso, paralelo = p_paralelo
        WHERE id_participante_ayudantia = p_id_participante
          AND id_ayudantia = v_id_ayudantia;

        IF NOT FOUND THEN
            RETURN (FALSE, 'No tiene permisos para modificar este registro o no existe.', NULL)::seguridad.res_operacion;
        END IF;

        v_mensaje := 'Datos del estudiante actualizados correctamente.';

    ELSIF p_accion = 'DEL' THEN
        IF p_id_participante IS NULL THEN
            RETURN (FALSE, 'ID de participante requerido para eliminación.', NULL)::seguridad.res_operacion;
        END IF;

        DELETE FROM ayudantia.detalle_asistencia_actividad WHERE id_participante_ayudantia = p_id_participante;
        DELETE FROM ayudantia.participante_ayudantia WHERE id_participante_ayudantia = p_id_participante AND id_ayudantia = v_id_ayudantia;

        v_mensaje := 'Estudiante e historial eliminados de su padrón.';

    ELSE
        RETURN (FALSE, 'Acción no válida. Use INS, UPD o DEL.', NULL)::seguridad.res_operacion;
    END IF;

    RETURN (
            TRUE,
            v_mensaje,
            json_build_object('id', COALESCE(v_id_nuevo, p_id_participante))
        )::seguridad.res_operacion;

EXCEPTION
    WHEN OTHERS THEN
        -- RECUERDO: Captura de errores internos con el tipo personalizado
        RETURN (FALSE, 'Error crítico en fn_gestionar_participante: ' || SQLERRM, NULL)::seguridad.res_operacion;
END;
$$;


--
-- Name: fn_get_tipos_documento_activos(); Type: FUNCTION; Schema: ayudantia; Owner: -
--

CREATE FUNCTION ayudantia.fn_get_tipos_documento_activos() RETURNS seguridad.res_operacion
    LANGUAGE plpgsql STABLE
    AS $$
DECLARE
    v_datos json;
BEGIN
    SELECT json_agg(t)
    INTO v_datos
    FROM (
             SELECT id_tipo_documento AS id,
                    nombre,
                    codigo
             FROM ayudantia.tipo_documento
             WHERE activo = true
             ORDER BY nombre ASC
         ) t;

    RETURN (
            true,
            'Tipos de documento obtenidos exitosamente'::text,
            COALESCE(v_datos, '[]'::json)
        )::seguridad.res_operacion;

EXCEPTION
    WHEN OTHERS THEN
        RETURN (
                false,
                'Error al listar tipos de documento: ' || SQLERRM,
                NULL
            )::seguridad.res_operacion;
END;
$$;


--
-- Name: fn_guardar_asistencias(integer, jsonb); Type: FUNCTION; Schema: ayudantia; Owner: -
--

CREATE FUNCTION ayudantia.fn_guardar_asistencias(p_id_registro integer, p_asistencias jsonb) RETURNS jsonb
    LANGUAGE plpgsql SECURITY DEFINER
    AS $$
DECLARE
    v_item      JSONB;
    v_presentes INTEGER := 0;
    v_total     INTEGER := 0;
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM ayudantia.registro_actividad
        WHERE id_registro_actividad = p_id_registro
    ) THEN
        RAISE EXCEPTION 'VALIDACION: El registro de actividad % no existe.', p_id_registro;
    END IF;

    IF p_asistencias IS NULL OR jsonb_array_length(p_asistencias) = 0 THEN
        RAISE EXCEPTION 'VALIDACION: No se enviaron registros de asistencia.';
    END IF;

    -- ── Actualizar cada registro ─────────────────────────────────────────
    FOR v_item IN SELECT * FROM jsonb_array_elements(p_asistencias)
        LOOP
            UPDATE ayudantia.detalle_asistencia_actividad
            SET asistio = (v_item->>'asistio')::BOOLEAN
            WHERE id_registro_actividad    = p_id_registro
              AND id_participante_ayudantia = (v_item->>'idParticipante')::INTEGER;

            v_total := v_total + 1;
            IF (v_item->>'asistio')::BOOLEAN THEN
                v_presentes := v_presentes + 1;
            END IF;
        END LOOP;

    RETURN jsonb_build_object(
            'exito',     TRUE,
            'mensaje',   format('Asistencia guardada. Presentes: %s de %s.', v_presentes, v_total),
            'presentes', v_presentes,
            'total',     v_total
           );

EXCEPTION WHEN OTHERS THEN
    RAISE EXCEPTION '%', SQLERRM;
END;
$$;


--
-- Name: fn_guardar_progreso_sesion(integer, integer, text); Type: FUNCTION; Schema: ayudantia; Owner: -
--

CREATE FUNCTION ayudantia.fn_guardar_progreso_sesion(p_id_usuario integer, p_id_registro integer, p_descripcion text) RETURNS seguridad.res_operacion
    LANGUAGE plpgsql
    AS $$
DECLARE
    v_id_ayudantia_user INTEGER;
BEGIN
    SELECT a.id_ayudantia INTO v_id_ayudantia_user
    FROM ayudantia.ayudantia a
             JOIN postulacion.postulacion p ON a.id_postulacion = p.id_postulacion
             JOIN academico.estudiante e ON p.id_estudiante = e.id_estudiante
    WHERE e.id_usuario = p_id_usuario LIMIT 1;

    UPDATE ayudantia.registro_actividad
    SET descripcion_actividad = p_descripcion
    WHERE id_registro_actividad = p_id_registro
      AND id_ayudantia = v_id_ayudantia_user
      AND id_tipo_estado_registro IN (
        SELECT id_tipo_estado_registro FROM ayudantia.tipo_estado_registro WHERE codigo IN ('PLANIFICADO', 'RECHAZADO')
    );

    IF NOT FOUND THEN
        RETURN (FALSE, 'No se pudo guardar el progreso. Es posible que la sesión ya esté en revisión.', NULL)::seguridad.res_operacion;
    END IF;

    RETURN (TRUE, 'Progreso guardado localmente.', NULL)::seguridad.res_operacion;
END;
$$;


--
-- Name: fn_horas_pendientes_observadas(integer); Type: FUNCTION; Schema: ayudantia; Owner: -
--

CREATE FUNCTION ayudantia.fn_horas_pendientes_observadas(p_id_ayudantia integer) RETURNS TABLE(id_ayudantia integer, horas_pendientes numeric, horas_observadas numeric)
    LANGUAGE plpgsql
    AS $$
BEGIN
    RETURN QUERY
    SELECT
        p_id_ayudantia,
        COALESCE(SUM(ra.horas_dedicadas) FILTER (WHERE ra.estado_revision = 'PENDIENTE'), 0),
        COALESCE(SUM(ra.horas_dedicadas) FILTER (WHERE ra.estado_revision = 'OBSERVADO'), 0)
    FROM ayudantia.registro_actividad ra
    WHERE ra.id_ayudantia = p_id_ayudantia;
END;
$$;


--
-- Name: fn_horas_validadas(integer); Type: FUNCTION; Schema: ayudantia; Owner: -
--

CREATE FUNCTION ayudantia.fn_horas_validadas(p_id_ayudantia integer) RETURNS TABLE(id_ayudantia integer, horas_aprobadas numeric)
    LANGUAGE plpgsql
    AS $$
BEGIN
    RETURN QUERY
    SELECT
        ra.id_ayudantia,
        COALESCE(SUM(ra.horas_dedicadas), 0) AS horas_aprobadas
    FROM ayudantia.registro_actividad ra
    WHERE ra.id_ayudantia = p_id_ayudantia
      AND ra.estado_revision = 'APROBADO'
    GROUP BY ra.id_ayudantia;
END;
$$;


--
-- Name: fn_info_general_ayudantia(integer); Type: FUNCTION; Schema: ayudantia; Owner: -
--

CREATE FUNCTION ayudantia.fn_info_general_ayudantia(p_id_ayudantia integer) RETURNS TABLE(id_ayudantia integer, nombre_asignatura character varying, semestre integer, nombre_docente text, correo_docente character varying, nombre_periodo character varying, fecha_inicio_periodo date, fecha_fin_periodo date, estado_periodo character varying)
    LANGUAGE plpgsql
    AS $$
BEGIN
    RETURN QUERY
    SELECT
        a.id_ayudantia,
        asi.nombre_asignatura,
        asi.semestre,
        (u.nombres || ' ' || u.apellidos)   AS nombre_docente,
        u.correo                            AS correo_docente,
        pa.nombre_periodo,
        pa.fecha_inicio,
        pa.fecha_fin,
        pa.estado
    FROM ayudantia.ayudantia a
    JOIN postulacion.postulacion     pp  ON pp.id_postulacion    = a.id_postulacion
    JOIN convocatoria.convocatoria   co  ON co.id_convocatoria   = pp.id_convocatoria
    JOIN academico.asignatura        asi ON asi.id_asignatura     = co.id_asignatura
    JOIN academico.docente           d   ON d.id_docente          = co.id_docente
    JOIN seguridad.usuario           u   ON u.id_usuario          = d.id_usuario
    JOIN academico.periodo_academico pa  ON pa.id_periodo_academico = co.id_periodo_academico
    WHERE a.id_ayudantia = p_id_ayudantia;
END;
$$;


--
-- Name: fn_inicializar_asistencia(integer); Type: FUNCTION; Schema: ayudantia; Owner: -
--

CREATE FUNCTION ayudantia.fn_inicializar_asistencia(p_id_registro integer) RETURNS jsonb
    LANGUAGE plpgsql SECURITY DEFINER
    AS $$
DECLARE
    v_id_ayudantia INTEGER;
    v_insertados   INTEGER := 0;
BEGIN
    -- Obtener ayudantía desde el registro de actividad
    SELECT id_ayudantia INTO v_id_ayudantia
    FROM ayudantia.registro_actividad
    WHERE id_registro_actividad = p_id_registro;

    IF NOT FOUND THEN
        RAISE EXCEPTION 'VALIDACION: El registro de actividad % no existe.', p_id_registro;
    END IF;

    -- Crear un detalle por cada participante activo (idempotente gracias a ON CONFLICT)
    INSERT INTO ayudantia.detalle_asistencia_actividad
    (id_registro_actividad, id_participante_ayudantia, asistio)
    SELECT
        p_id_registro,
        pa.id_participante_ayudantia,
        FALSE
    FROM ayudantia.participante_ayudantia pa
    WHERE pa.id_ayudantia = v_id_ayudantia
      AND pa.activo       = TRUE
    ON CONFLICT (id_registro_actividad, id_participante_ayudantia) DO NOTHING;

    GET DIAGNOSTICS v_insertados = ROW_COUNT;

    RETURN jsonb_build_object(
            'exito',                    TRUE,
            'mensaje',                  format('Asistencia inicializada para %s participantes.', v_insertados),
            'participantesRegistrados', v_insertados
           );

EXCEPTION WHEN OTHERS THEN
    RAISE EXCEPTION '%', SQLERRM;
END;
$$;


--
-- Name: fn_insertar_documento(character varying, character varying, character varying, integer, integer, integer, integer, integer); Type: FUNCTION; Schema: ayudantia; Owner: -
--

CREATE FUNCTION ayudantia.fn_insertar_documento(p_nombre_mostrar character varying, p_ruta_archivo character varying, p_extension character varying, p_peso_bytes integer, p_id_tipo_doc integer, p_id_usuario integer, p_id_facultad integer DEFAULT 0, p_id_carrera integer DEFAULT 0) RETURNS seguridad.res_operacion
    LANGUAGE plpgsql
    AS $$
DECLARE
    v_id_periodo     INTEGER;
    v_id_nuevo       INTEGER;
    v_id_fac_coord   INTEGER;
    v_id_car_coord   INTEGER;
    v_es_coordinador BOOLEAN := FALSE;
BEGIN
    v_id_periodo := academico.fn_obtener_id_periodo_activo();
    IF v_id_periodo IS NULL THEN
        RETURN (FALSE, 'No existe un periodo académico activo configurado', NULL)::seguridad.res_operacion;
    END IF;

    SELECT c.id_carrera, car.id_facultad, TRUE
    INTO v_id_car_coord, v_id_fac_coord, v_es_coordinador
    FROM academico.coordinador c
             JOIN academico.carrera car ON c.id_carrera = car.id_carrera
    WHERE c.id_usuario = p_id_usuario AND c.activo = TRUE;

    IF v_es_coordinador THEN
        IF p_id_carrera = 1 THEN
            p_id_carrera := v_id_car_coord;
            p_id_facultad := v_id_fac_coord;
        ELSIF p_id_facultad = 1 THEN
            p_id_facultad := v_id_fac_coord;
            p_id_carrera := NULL;
        ELSE
            p_id_facultad := NULL;
            p_id_carrera := NULL;
        END IF;
    ELSE
        IF p_id_facultad = 0 THEN p_id_facultad := NULL; END IF;
        IF p_id_carrera = 0 THEN p_id_carrera := NULL; END IF;
    END IF;

    INSERT INTO ayudantia.documento_academico (
        nombre_mostrar, ruta_archivo, extension, peso_bytes,
        id_tipo_documento, id_periodo, id_facultad, id_carrera, id_usuario_sube
    )
    VALUES (
               p_nombre_mostrar, p_ruta_archivo, p_extension, p_peso_bytes,
               p_id_tipo_doc, v_id_periodo, p_id_facultad, p_id_carrera, p_id_usuario
           )
    RETURNING id_documento INTO v_id_nuevo;

    RETURN (TRUE, 'Documento registrado exitosamente', json_build_object('id', v_id_nuevo))::seguridad.res_operacion;

EXCEPTION
    WHEN OTHERS THEN
        RETURN (FALSE, 'Error al insertar documento: ' || SQLERRM, NULL)::seguridad.res_operacion;
END;
$$;


--
-- Name: fn_listar_documentos_visor(integer, character varying); Type: FUNCTION; Schema: ayudantia; Owner: -
--

CREATE FUNCTION ayudantia.fn_listar_documentos_visor(p_id_usuario integer, p_nombre_rol character varying) RETURNS seguridad.res_operacion
    LANGUAGE plpgsql
    AS $$
DECLARE
    v_id_periodo  integer;
    v_id_facultad integer := NULL;
    v_id_carrera  integer := NULL;
    v_es_admin    boolean := false;
    v_datos       json;
BEGIN
    v_id_periodo := academico.fn_obtener_id_periodo_activo();
    IF v_id_periodo IS NULL THEN
        RETURN (false, 'No hay un periodo académico activo'::text, NULL)::seguridad.res_operacion;
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM seguridad.usuario_tipo_rol utr
                          JOIN seguridad.tipo_rol tr ON utr.id_tipo_rol = tr.id_tipo_rol
        WHERE utr.id_usuario = p_id_usuario
          AND tr.nombre_tipo_rol = p_nombre_rol
          AND utr.activo = true
          AND tr.activo = true
    ) THEN
        RETURN (false, 'Usuario o Rol no válidos para esta sesión'::text, NULL)::seguridad.res_operacion;
    END IF;

    IF p_nombre_rol = 'ADMINISTRADOR' THEN
        v_es_admin := true;

    ELSIF p_nombre_rol = 'DECANO' THEN
        SELECT id_facultad INTO v_id_facultad
        FROM academico.decano
        WHERE id_usuario = p_id_usuario AND activo = true;

    ELSIF p_nombre_rol = 'COORDINADOR' THEN
        SELECT c.id_carrera, c.id_facultad INTO v_id_carrera, v_id_facultad
        FROM academico.coordinador co
                 JOIN academico.carrera c ON co.id_carrera = c.id_carrera
        WHERE co.id_usuario = p_id_usuario AND co.activo = true;

    ELSIF p_nombre_rol IN ('ESTUDIANTE', 'AYUDANTE') THEN
        SELECT e.id_carrera INTO v_id_carrera
        FROM academico.estudiante e
        WHERE e.id_usuario = p_id_usuario;

        SELECT id_facultad INTO v_id_facultad FROM academico.carrera WHERE id_carrera = v_id_carrera;
    END IF;

    SELECT json_agg(t)
    INTO v_datos
    FROM (
             SELECT
                 d.id_documento,
                 d.nombre_mostrar,
                 d.ruta_archivo,
                 d.extension,
                 d.peso_bytes,
                 d.fecha_subida,
                 td.nombre AS tipo_documento,
                 f.nombre_facultad,
                 c.nombre_carrera
             FROM ayudantia.documento_academico d
                      INNER JOIN ayudantia.tipo_documento td ON d.id_tipo_documento = td.id_tipo_documento
                      LEFT JOIN academico.facultad f ON d.id_facultad = f.id_facultad
                      LEFT JOIN academico.carrera c ON d.id_carrera = c.id_carrera
             WHERE d.id_periodo = v_id_periodo
               AND d.activo = true
               AND (
                 v_es_admin
                     OR (d.id_facultad IS NULL AND d.id_carrera IS NULL)
                     OR (d.id_facultad = v_id_facultad AND d.id_carrera IS NULL)
                     OR (d.id_carrera = v_id_carrera)
                 )
             ORDER BY d.fecha_subida DESC
         ) t;

    RETURN (true, 'Documentos listados correctamente'::text, COALESCE(v_datos, '[]'::json))::seguridad.res_operacion;
EXCEPTION
    WHEN OTHERS THEN
        RETURN (false, 'Error en visor: ' || SQLERRM, NULL)::seguridad.res_operacion;
END;
$$;


--
-- Name: fn_listar_padron_ayudante(integer); Type: FUNCTION; Schema: ayudantia; Owner: -
--

CREATE FUNCTION ayudantia.fn_listar_padron_ayudante(p_id_usuario integer) RETURNS TABLE(id_participante_ayudantia integer, nombre_completo character varying, curso character varying, paralelo character varying, activo boolean)
    LANGUAGE plpgsql
    AS $$
DECLARE
    v_id_ayudantia INTEGER;
BEGIN
    SELECT a.id_ayudantia INTO v_id_ayudantia
    FROM ayudantia.ayudantia a
             JOIN postulacion.postulacion p ON a.id_postulacion = p.id_postulacion
             JOIN academico.estudiante e ON p.id_estudiante = e.id_estudiante
    WHERE e.id_usuario = p_id_usuario
      AND a.id_tipo_estado_ayudantia = (SELECT id_tipo_estado_ayudantia FROM ayudantia.tipo_estado_ayudantia WHERE codigo = 'ACTIVO')
    LIMIT 1;

    RETURN QUERY
        SELECT pa.id_participante_ayudantia, pa.nombre_completo, pa.curso, pa.paralelo, pa.activo
        FROM ayudantia.participante_ayudantia pa
        WHERE pa.id_ayudantia = v_id_ayudantia
        ORDER BY pa.nombre_completo ASC;
END;
$$;


--
-- Name: fn_listar_sesiones(integer, date, date, character varying); Type: FUNCTION; Schema: ayudantia; Owner: -
--

CREATE FUNCTION ayudantia.fn_listar_sesiones(p_id_ayudantia integer, p_fecha_desde date, p_fecha_hasta date, p_estado_codigo character varying) RETURNS TABLE(id_registro_actividad integer, fecha date, hora_inicio time without time zone, hora_fin time without time zone, horas_dedicadas numeric, tema_tratado text, descripcion_actividad text, lugar character varying, codigo_estado character varying, nombre_estado character varying, observaciones character varying, fecha_observacion date)
    LANGUAGE plpgsql
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
            ra.descripcion_actividad,
            ra.lugar,
            ter.codigo AS codigo_estado,
            ter.nombre_estado,
            ra.observaciones, -- <--- SELECCIONADO SIN ALIAS
            ra.fecha_observacion
        FROM ayudantia.registro_actividad ra
                 JOIN ayudantia.tipo_estado_registro ter ON ra.id_tipo_estado_registro = ter.id_tipo_estado_registro
        WHERE ra.id_ayudantia = p_id_ayudantia
          AND (p_fecha_desde IS NULL OR ra.fecha >= p_fecha_desde)
          AND (p_fecha_hasta IS NULL OR ra.fecha <= p_fecha_hasta)
          AND (p_estado_codigo IS NULL OR p_estado_codigo = '' OR ter.codigo = p_estado_codigo)
        ORDER BY ra.fecha DESC, ra.hora_inicio DESC;
END;
$$;


--
-- Name: fn_listar_tipo_estado_ayudantia(); Type: FUNCTION; Schema: ayudantia; Owner: -
--

CREATE FUNCTION ayudantia.fn_listar_tipo_estado_ayudantia() RETURNS jsonb
    LANGUAGE plpgsql SECURITY DEFINER
    AS $$
DECLARE
    v_resultado JSONB;
BEGIN
    SELECT COALESCE(jsonb_agg(
        jsonb_build_object(
            'id', id_tipo_estado_ayudantia,
            'nombre_estado', nombre_estado,
            'descripcion', descripcion,
            'codigo', codigo,
            'activo', activo
        ) ORDER BY nombre_estado
    ), '[]'::jsonb)
    INTO v_resultado
    FROM ayudantia.tipo_estado_ayudantia
    WHERE activo = TRUE;

    RETURN jsonb_build_object(
        'exito', true,
        'datos', v_resultado
    );

EXCEPTION
    WHEN OTHERS THEN
        RETURN jsonb_build_object('exito', false, 'mensaje', 'Error al listar estados de ayudantía: ' || SQLERRM);
END;
$$;


--
-- Name: fn_listar_tipo_estado_evidencia(); Type: FUNCTION; Schema: ayudantia; Owner: -
--

CREATE FUNCTION ayudantia.fn_listar_tipo_estado_evidencia() RETURNS jsonb
    LANGUAGE plpgsql SECURITY DEFINER
    AS $$
DECLARE
    v_resultado JSONB;
BEGIN
    SELECT COALESCE(jsonb_agg(
        jsonb_build_object(
            'id', id_tipo_estado_evidencia,
            'nombre_estado', nombre_estado,
            'descripcion', descripcion,
            'codigo', codigo,
            'activo', activo
        ) ORDER BY nombre_estado
    ), '[]'::jsonb)
    INTO v_resultado
    FROM ayudantia.tipo_estado_evidencia
    WHERE activo = TRUE;

    RETURN jsonb_build_object(
        'exito', true,
        'datos', v_resultado
    );

EXCEPTION
    WHEN OTHERS THEN
        RETURN jsonb_build_object('exito', false, 'mensaje', 'Error al listar estados de evidencia: ' || SQLERRM);
END;
$$;


--
-- Name: fn_listar_tipo_estado_registro(); Type: FUNCTION; Schema: ayudantia; Owner: -
--

CREATE FUNCTION ayudantia.fn_listar_tipo_estado_registro() RETURNS jsonb
    LANGUAGE plpgsql SECURITY DEFINER
    AS $$
DECLARE
    v_resultado JSONB;
BEGIN
    SELECT COALESCE(jsonb_agg(
        jsonb_build_object(
            'id', id_tipo_estado_registro,
            'nombre_estado', nombre_estado,
            'descripcion', descripcion,
            'codigo', codigo,
            'activo', activo
        ) ORDER BY nombre_estado
    ), '[]'::jsonb)
    INTO v_resultado
    FROM ayudantia.tipo_estado_registro
    WHERE activo = TRUE;

    RETURN jsonb_build_object(
        'exito', true,
        'datos', v_resultado
    );

EXCEPTION
    WHEN OTHERS THEN
        RETURN jsonb_build_object('exito', false, 'mensaje', 'Error al listar estados de registro: ' || SQLERRM);
END;
$$;


--
-- Name: fn_listar_tipo_evidencia(); Type: FUNCTION; Schema: ayudantia; Owner: -
--

CREATE FUNCTION ayudantia.fn_listar_tipo_evidencia() RETURNS jsonb
    LANGUAGE plpgsql SECURITY DEFINER
    AS $$
DECLARE
    v_resultado JSONB;
BEGIN
    SELECT COALESCE(jsonb_agg(
        jsonb_build_object(
            'id', id_tipo_evidencia,
            'nombre', nombre,
            'extension_permitida', extension_permitida,
            'codigo', codigo,
            'activo', activo
        ) ORDER BY nombre
    ), '[]'::jsonb)
    INTO v_resultado
    FROM ayudantia.tipo_evidencia
    WHERE activo = TRUE;

    RETURN jsonb_build_object(
        'exito', true,
        'datos', v_resultado
    );

EXCEPTION
    WHEN OTHERS THEN
        RETURN jsonb_build_object('exito', false, 'mensaje', 'Error al listar tipos de evidencia: ' || SQLERRM);
END;
$$;


--
-- Name: fn_listar_tipo_sancion_ayudante_catedra(); Type: FUNCTION; Schema: ayudantia; Owner: -
--

CREATE FUNCTION ayudantia.fn_listar_tipo_sancion_ayudante_catedra() RETURNS jsonb
    LANGUAGE plpgsql SECURITY DEFINER
    AS $$
DECLARE
    v_resultado JSONB;
BEGIN
    SELECT COALESCE(jsonb_agg(
        jsonb_build_object(
            'id', id_tipo_sancion_ayudante_catedra,
            'nombre_tipo_sancion', nombre_tipo_sancion,
            'codigo', codigo,
            'activo', activo
        ) ORDER BY nombre_tipo_sancion
    ), '[]'::jsonb)
    INTO v_resultado
    FROM ayudantia.tipo_sancion_ayudante_catedra
    WHERE activo = TRUE;

    RETURN jsonb_build_object(
        'exito', true,
        'datos', v_resultado
    );

EXCEPTION
    WHEN OTHERS THEN
        RETURN jsonb_build_object('exito', false, 'mensaje', 'Error al listar tipos de sanción: ' || SQLERRM);
END;
$$;


--
-- Name: fn_marcar_asistencia(integer, boolean); Type: FUNCTION; Schema: ayudantia; Owner: -
--

CREATE FUNCTION ayudantia.fn_marcar_asistencia(p_id_detalle integer, p_asistio boolean) RETURNS seguridad.res_operacion
    LANGUAGE plpgsql
    AS $$
DECLARE
    v_sesion ayudantia.registro_actividad%ROWTYPE;
BEGIN
    SELECT ra.* INTO v_sesion
    FROM ayudantia.detalle_asistencia_actividad da
             JOIN ayudantia.registro_actividad ra ON da.id_registro_actividad = ra.id_registro_actividad
    WHERE da.id_detalle_asistencia_actividad = p_id_detalle;

    IF v_sesion.id_registro_actividad IS NULL THEN
        RETURN (FALSE, 'El registro de asistencia no existe.', NULL)::seguridad.res_operacion;
    END IF;

    IF NOT (
        v_sesion.fecha = (CURRENT_TIMESTAMP AT TIME ZONE 'America/Guayaquil')::DATE
            AND (CURRENT_TIMESTAMP AT TIME ZONE 'America/Guayaquil')::TIME
            BETWEEN v_sesion.hora_inicio AND v_sesion.hora_fin
        ) THEN
        RETURN (FALSE, 'Operación denegada: Fuera del horario programado para esta sesión.', NULL)::seguridad.res_operacion;
    END IF;

    UPDATE ayudantia.detalle_asistencia_actividad
    SET asistio = p_asistio
    WHERE id_detalle_asistencia_actividad = p_id_detalle;

    RETURN (TRUE, 'Asistencia marcada correctamente.', json_build_object('id_detalle', p_id_detalle, 'estado', p_asistio))::seguridad.res_operacion;

EXCEPTION WHEN OTHERS THEN
    RETURN (FALSE, 'Error al marcar asistencia: ' || SQLERRM, NULL)::seguridad.res_operacion;
END;
$$;


--
-- Name: fn_obtener_asistencia_sesion_actual(integer); Type: FUNCTION; Schema: ayudantia; Owner: -
--

CREATE FUNCTION ayudantia.fn_obtener_asistencia_sesion_actual(p_id_usuario integer) RETURNS seguridad.res_operacion
    LANGUAGE plpgsql
    AS $$
DECLARE
    v_id_ayudantia INTEGER;
    v_sesion RECORD;
    v_puede_editar BOOLEAN := FALSE;
    v_estudiantes_json JSON;
BEGIN
    SELECT a.id_ayudantia INTO v_id_ayudantia
    FROM ayudantia.ayudantia a
             JOIN postulacion.postulacion p ON a.id_postulacion = p.id_postulacion
             JOIN academico.estudiante e ON p.id_estudiante = e.id_estudiante
    WHERE e.id_usuario = p_id_usuario
      AND a.id_tipo_estado_ayudantia = (SELECT id_tipo_estado_ayudantia FROM ayudantia.tipo_estado_ayudantia WHERE codigo = 'ACTIVO')
    LIMIT 1;

    IF v_id_ayudantia IS NULL THEN
        RETURN (FALSE, 'No posee una ayudantía activa.', NULL)::seguridad.res_operacion;
    END IF;

    SELECT * INTO v_sesion
    FROM ayudantia.registro_actividad
    WHERE id_ayudantia = v_id_ayudantia
      AND id_tipo_estado_registro = (SELECT id_tipo_estado_registro FROM ayudantia.tipo_estado_registro WHERE codigo = 'PLANIFICADO')
      AND fecha >= CURRENT_DATE
    ORDER BY fecha ASC, hora_inicio ASC
    LIMIT 1;

    IF v_sesion.id_registro_actividad IS NULL THEN
        RETURN (FALSE, 'No tiene sesiones próximas planificadas.', NULL)::seguridad.res_operacion;
    END IF;

    IF (v_sesion.fecha = (CURRENT_TIMESTAMP AT TIME ZONE 'America/Guayaquil')::DATE
        AND (CURRENT_TIMESTAMP AT TIME ZONE 'America/Guayaquil')::TIME
            BETWEEN v_sesion.hora_inicio AND v_sesion.hora_fin) THEN
        v_puede_editar := TRUE;
    END IF;

    SELECT json_agg(t) INTO v_estudiantes_json
    FROM (
             SELECT
                 da.id_detalle_asistencia_actividad as id_detalle,
                 p.nombre_completo,
                 p.curso,
                 p.paralelo,
                 da.asistio
             FROM ayudantia.detalle_asistencia_actividad da
                      JOIN ayudantia.participante_ayudantia p ON da.id_participante_ayudantia = p.id_participante_ayudantia
             WHERE da.id_registro_actividad = v_sesion.id_registro_actividad
             ORDER BY p.nombre_completo ASC
         ) t;

    RETURN (
            TRUE,
            CASE WHEN v_puede_editar THEN 'Sesión activa: Puede marcar asistencia.' ELSE 'Sesión fuera de horario: Modo lectura.' END,
            json_build_object(
                    'sesion', json_build_object(
                    'id_registro', v_sesion.id_registro_actividad,
                    'tema', v_sesion.tema_tratado,
                    'fecha', v_sesion.fecha,
                    'horario', v_sesion.hora_inicio || ' - ' || v_sesion.hora_fin,
                    'lugar', v_sesion.lugar,
                    'puede_editar', v_puede_editar
                              ),
                    'estudiantes', COALESCE(v_estudiantes_json, '[]'::json)
            )
        )::seguridad.res_operacion;

END;
$$;


--
-- Name: fn_obtener_borrador_sesion(integer, integer); Type: FUNCTION; Schema: ayudantia; Owner: -
--

CREATE FUNCTION ayudantia.fn_obtener_borrador_sesion(p_id_usuario integer, p_id_registro integer) RETURNS seguridad.res_operacion
    LANGUAGE plpgsql
    AS $$
DECLARE
    v_id_ayudantia_user INTEGER;
    v_sesion_data JSON;
    v_evidencias_json JSON;
BEGIN
    SELECT a.id_ayudantia INTO v_id_ayudantia_user
    FROM ayudantia.ayudantia a
             JOIN postulacion.postulacion p ON a.id_postulacion = p.id_postulacion
             JOIN academico.estudiante e ON p.id_estudiante = e.id_estudiante
    WHERE e.id_usuario = p_id_usuario LIMIT 1;

    SELECT json_build_object(
                   'id_registro', id_registro_actividad,
                   'tema', tema_tratado,
                   'fecha', fecha,
                   'lugar', lugar,
                   'descripcion_actual', COALESCE(descripcion_actividad, ''),
                   'codigo_estado', (SELECT codigo FROM ayudantia.tipo_estado_registro WHERE id_tipo_estado_registro = ra.id_tipo_estado_registro)
           ) INTO v_sesion_data
    FROM ayudantia.registro_actividad ra
    WHERE id_registro_actividad = p_id_registro AND id_ayudantia = v_id_ayudantia_user;

    IF v_sesion_data IS NULL THEN
        RETURN (FALSE, 'No tiene acceso a esta sesión o el registro no existe.', NULL)::seguridad.res_operacion;
    END IF;

    SELECT json_agg(ev) INTO v_evidencias_json
    FROM (
             SELECT
                 id_evidencia_registro_actividad as id_evidencia,
                 nombre_archivo,
                 ruta_archivo,
                 mime_type,
                 tamanio_bytes,
                 fecha_subida
             FROM ayudantia.evidencia_registro_actividad
             WHERE id_registro_actividad = p_id_registro
             ORDER BY fecha_subida DESC
         ) ev;

    RETURN (
            TRUE,
            'Borrador recuperado correctamente.',
            json_build_object(
                    'detalle', v_sesion_data,
                    'evidencias', COALESCE(v_evidencias_json, '[]'::json)
            )
        )::seguridad.res_operacion;

END;
$$;


--
-- Name: fn_obtener_id_ayudantia(integer); Type: FUNCTION; Schema: ayudantia; Owner: -
--

CREATE FUNCTION ayudantia.fn_obtener_id_ayudantia(p_id_usuario integer) RETURNS integer
    LANGUAGE plpgsql
    AS $$
DECLARE
    v_id_ayudantia INTEGER;
BEGIN
    SELECT a.id_ayudantia INTO v_id_ayudantia
    FROM ayudantia.ayudantia a
    JOIN postulacion.postulacion  pp  ON pp.id_postulacion = a.id_postulacion
    JOIN academico.estudiante     est ON est.id_estudiante  = pp.id_estudiante
    JOIN ayudantia.tipo_estado_ayudantia tea
        ON tea.id_tipo_estado_ayudantia = a.id_tipo_estado_ayudantia
    WHERE est.id_usuario = p_id_usuario
      AND tea.nombre_estado = 'EN_PROGRESO'
    LIMIT 1;

    RETURN v_id_ayudantia;
END;
$$;


--
-- Name: fn_obtener_id_ayudantia_por_usuario(integer); Type: FUNCTION; Schema: ayudantia; Owner: -
--

CREATE FUNCTION ayudantia.fn_obtener_id_ayudantia_por_usuario(p_id_usuario integer) RETURNS integer
    LANGUAGE plpgsql STABLE
    AS $$
DECLARE
    v_id_ayudantia INTEGER;
BEGIN
    SELECT a.id_ayudantia
    INTO   v_id_ayudantia
    FROM   seguridad.usuario           u
               JOIN   academico.estudiante        e   ON e.id_usuario  = u.id_usuario
               JOIN   postulacion.postulacion     p   ON p.id_estudiante = e.id_estudiante
               JOIN   postulacion.tipo_estado_postulacion tep
                      ON tep.id_tipo_estado_postulacion = p.id_tipo_estado_postulacion
               JOIN   ayudantia.ayudantia         a   ON a.id_postulacion = p.id_postulacion
               JOIN   ayudantia.tipo_estado_ayudantia tea
                      ON tea.id_tipo_estado_ayudantia = a.id_tipo_estado_ayudantia
    WHERE  u.id_usuario  = p_id_usuario
      AND  tep.codigo = 'SELECCIONADO'
      AND  tea.codigo IN ('ACTIVO', 'EN_PROCESO')
    ORDER BY a.id_ayudantia DESC
    LIMIT  1;

    RETURN v_id_ayudantia;

EXCEPTION
    WHEN OTHERS THEN
        RAISE EXCEPTION 'VALIDACION: Error al obtener ayudantía del usuario %: %',
            p_id_usuario, SQLERRM;
END;
$$;


--
-- Name: fn_obtener_id_registro_activo_por_ayudantia(integer); Type: FUNCTION; Schema: ayudantia; Owner: -
--

CREATE FUNCTION ayudantia.fn_obtener_id_registro_activo_por_ayudantia(p_id_ayudantia integer) RETURNS integer
    LANGUAGE plpgsql STABLE
    AS $$
DECLARE
    v_id_registro INTEGER;
BEGIN
    SELECT ra.id_registro_actividad
    INTO   v_id_registro
    FROM   ayudantia.registro_actividad  ra
               JOIN   ayudantia.tipo_estado_registro ter
                      ON ter.id_tipo_estado_registro = ra.id_tipo_estado_registro
    WHERE  ra.id_ayudantia = p_id_ayudantia
      AND  ter.codigo      IN ('PENDIENTE', 'EN_REVISION', 'BORRADOR', 'OBSERVADO', 'PLANIFICADO')
    ORDER BY ra.fecha DESC NULLS LAST, ra.id_registro_actividad DESC
    LIMIT 1;

    RETURN v_id_registro;

EXCEPTION
    WHEN OTHERS THEN
        RAISE EXCEPTION 'VALIDACION: Error al obtener registro activo de ayudantía %: %',
            p_id_ayudantia, SQLERRM;
END;
$$;


--
-- Name: fn_obtener_matriz_asistencia(integer); Type: FUNCTION; Schema: ayudantia; Owner: -
--

CREATE FUNCTION ayudantia.fn_obtener_matriz_asistencia(p_id_ayudantia integer) RETURNS jsonb
    LANGUAGE plpgsql STABLE SECURITY DEFINER
    AS $$
BEGIN
    RETURN jsonb_build_object(

        -- ── Lista de sesiones ordenadas cronológicamente ──────────────
            'sesiones', COALESCE(
                    (SELECT jsonb_agg(
                                    jsonb_build_object(
                                            'id',    ra.id_registro_actividad,
                                            'fecha', ra.fecha,
                                            'tema',  COALESCE(ra.tema_tratado, ''),
                                            'horas', ra.horas_dedicadas
                                    ) ORDER BY ra.fecha ASC, ra.id_registro_actividad ASC
                            )
                     FROM ayudantia.registro_actividad ra
                     WHERE ra.id_ayudantia = p_id_ayudantia),
                    '[]'::jsonb
                        ),

        -- ── Lista de participantes con su mapa de asistencias ─────────
        -- Clave del mapa: id_registro_actividad como texto (JSON obliga texto)
        -- Valor: true/false/null
        --   true  → presente
        --   false → ausente
        --   null  → no existía registro de asistencia (se unió después)
            'estudiantes', COALESCE(
                    (SELECT jsonb_agg(
                                    jsonb_build_object(
                                            'idParticipante', pa.id_participante_ayudantia,
                                            'nombre',         pa.nombre_completo,
                                            'curso',          COALESCE(pa.curso,    ''),
                                            'paralelo',       COALESCE(pa.paralelo, ''),
                                            'asistencias', (
                                                -- jsonb_object_agg preserva valores null como JSON null
                                                SELECT jsonb_object_agg(
                                                               ra2.id_registro_actividad::text,
                                                               da.asistio
                                                       )
                                                FROM ayudantia.registro_actividad ra2
                                                         LEFT JOIN ayudantia.detalle_asistencia_actividad da
                                                                   ON da.id_registro_actividad    = ra2.id_registro_actividad
                                                                       AND da.id_participante_ayudantia = pa.id_participante_ayudantia
                                                WHERE ra2.id_ayudantia = p_id_ayudantia
                                            )
                                    ) ORDER BY pa.nombre_completo ASC
                            )
                     FROM ayudantia.participante_ayudantia pa
                     WHERE pa.id_ayudantia = p_id_ayudantia
                       AND pa.activo       = TRUE),
                    '[]'::jsonb
                           )
           );

EXCEPTION WHEN OTHERS THEN
    RAISE EXCEPTION '[ERROR] fn_obtener_matriz_asistencia: %', SQLERRM;
END;
$$;


--
-- Name: fn_planificar_actividad(integer, date, time without time zone, time without time zone, character varying, text); Type: FUNCTION; Schema: ayudantia; Owner: -
--

CREATE FUNCTION ayudantia.fn_planificar_actividad(p_id_ayudantia integer, p_fecha date, p_hora_inicio time without time zone, p_hora_fin time without time zone, p_lugar character varying, p_tema_tratado text) RETURNS integer
    LANGUAGE plpgsql
    AS $$
DECLARE
    v_id_estado_planificada INTEGER;
    v_horas_dedicadas NUMERIC(5,2);
    v_id_registro_creado INTEGER;
    v_id_ayudantia INTEGER;
BEGIN
    SELECT a.id_ayudantia
    INTO v_id_ayudantia
    FROM academico.estudiante e
    JOIN postulacion.postulacion p on e.id_estudiante = p.id_estudiante
    JOIN ayudantia.ayudantia a on p.id_postulacion = a.id_postulacion
    WHERE e.id_usuario = 152;

    IF v_id_ayudantia IS NULL THEN
        RAISE EXCEPTION 'No se encontró una ayudantía asociada al estudiante con id_usuario %', p_id_ayudantia;
    END IF;

    IF p_fecha < CURRENT_DATE THEN
        RAISE EXCEPTION 'No se pueden planificar sesiones en fechas pasadas.';
    END IF;

    IF p_hora_fin <= p_hora_inicio THEN
        RAISE EXCEPTION 'La hora de fin debe ser posterior a la hora de inicio.';
    END IF;

    IF EXISTS (
        SELECT 1 FROM ayudantia.registro_actividad
        WHERE id_ayudantia = v_id_ayudantia
          AND fecha = p_fecha
          AND (p_hora_inicio < hora_fin AND p_hora_fin > hora_inicio)
          AND id_tipo_estado_registro <> (SELECT id_tipo_estado_registro FROM ayudantia.tipo_estado_registro WHERE codigo = 'RECHAZADO')
    ) THEN
        RAISE EXCEPTION 'Ya existe una sesión planificada que se cruza con este horario.';
    END IF;

    v_horas_dedicadas := EXTRACT(EPOCH FROM (p_hora_fin - p_hora_inicio)) / 3600.0;

    SELECT id_tipo_estado_registro INTO v_id_estado_planificada
    FROM ayudantia.tipo_estado_registro WHERE codigo = 'PLANIFICADO';

    INSERT INTO ayudantia.registro_actividad(
        id_ayudantia, fecha, hora_inicio, hora_fin, lugar,
        tema_tratado, horas_dedicadas, id_tipo_estado_registro
    ) VALUES (
                     v_id_ayudantia, p_fecha, p_hora_inicio, p_hora_fin, p_lugar,
                 p_tema_tratado, v_horas_dedicadas, v_id_estado_planificada
             ) RETURNING id_registro_actividad INTO v_id_registro_creado;

    RETURN v_id_registro_creado;
END;
$$;


--
-- Name: fn_planificar_sesion(integer, date, time without time zone, time without time zone, character varying, text); Type: FUNCTION; Schema: ayudantia; Owner: -
--

CREATE FUNCTION ayudantia.fn_planificar_sesion(p_id_usuario integer, p_fecha date, p_hora_inicio time without time zone, p_hora_fin time without time zone, p_lugar character varying, p_tema text) RETURNS seguridad.res_operacion
    LANGUAGE plpgsql
    AS $$
DECLARE
    v_id_ayudantia INTEGER;
    v_id_nuevo_reg INTEGER;
    v_id_estado_plan INTEGER;
    v_horas_limite NUMERIC;
    v_horas_actuales_semana NUMERIC;
    v_horas_nueva_sesion NUMERIC;
    v_res_asistencia seguridad.res_operacion; -- Para capturar el resultado del snapshot
BEGIN
    SELECT a.id_ayudantia, a.horas_semanales_max INTO v_id_ayudantia, v_horas_limite
    FROM ayudantia.ayudantia a
             JOIN postulacion.postulacion p ON a.id_postulacion = p.id_postulacion
             JOIN academico.estudiante e ON p.id_estudiante = e.id_estudiante
    WHERE e.id_usuario = p_id_usuario
      AND a.id_tipo_estado_ayudantia = (SELECT id_tipo_estado_ayudantia FROM ayudantia.tipo_estado_ayudantia WHERE codigo = 'ACTIVO')
    LIMIT 1;

    IF v_id_ayudantia IS NULL THEN
        RETURN (FALSE, 'No posee una ayudantía activa vinculada a su cuenta.', NULL)::seguridad.res_operacion;
    END IF;

    v_horas_nueva_sesion := EXTRACT(EPOCH FROM (p_hora_fin::TIME - p_hora_inicio::TIME)) / 3600.0;

    SELECT COALESCE(SUM(horas_dedicadas), 0) INTO v_horas_actuales_semana
    FROM ayudantia.registro_actividad
    WHERE id_ayudantia = v_id_ayudantia AND date_trunc('week', fecha) = date_trunc('week', p_fecha)
      AND id_tipo_estado_registro != (SELECT id_tipo_estado_registro FROM ayudantia.tipo_estado_registro WHERE codigo = 'RECHAZADO');

    IF (v_horas_actuales_semana + v_horas_nueva_sesion) > v_horas_limite THEN
        RETURN (FALSE, format('Límite excedido (%s/%s h). Reduzca el tiempo o elija otra semana.', ROUND(v_horas_actuales_semana, 2), v_horas_limite), NULL)::seguridad.res_operacion;
    END IF;

    IF EXISTS (SELECT 1 FROM ayudantia.registro_actividad WHERE id_ayudantia = v_id_ayudantia AND fecha = p_fecha AND (p_hora_inicio::TIME, p_hora_fin::TIME) OVERLAPS (hora_inicio, hora_fin)) THEN
        RETURN (FALSE, 'Ya existe una sesión programada en este horario.', NULL)::seguridad.res_operacion;
    END IF;

    SELECT id_tipo_estado_registro INTO v_id_estado_plan FROM ayudantia.tipo_estado_registro WHERE codigo = 'PLANIFICADO';

    INSERT INTO ayudantia.registro_actividad (id_ayudantia, fecha, hora_inicio, hora_fin, lugar, tema_tratado, id_tipo_estado_registro, horas_dedicadas)
    VALUES (v_id_ayudantia, p_fecha, p_hora_inicio, p_hora_fin, p_lugar, p_tema, v_id_estado_plan, ROUND(v_horas_nueva_sesion, 2))
    RETURNING id_registro_actividad INTO v_id_nuevo_reg;

    v_res_asistencia := ayudantia.fn_generar_snapshot_asistencia(v_id_nuevo_reg);

    RETURN (
            TRUE,
            'Sesión planificada. ' || v_res_asistencia.mensaje,
            json_build_object('id_registro', v_id_nuevo_reg, 'detalle_asistencia', v_res_asistencia.datos)
        )::seguridad.res_operacion;

EXCEPTION WHEN OTHERS THEN
    RETURN (FALSE, 'Error crítico en flujo de planificación: ' || SQLERRM, NULL)::seguridad.res_operacion;
END;
$$;


--
-- Name: fn_progreso_ayudantia(integer); Type: FUNCTION; Schema: ayudantia; Owner: -
--

CREATE FUNCTION ayudantia.fn_progreso_ayudantia(p_id_ayudantia integer) RETURNS TABLE(horas_aprobadas numeric, horas_totales_esperadas numeric, porcentaje_avance_global numeric, semana_inicio date, semana_fin date, horas_semana_actual numeric, horas_disponibles_semana numeric, supera_limite_semanal boolean)
    LANGUAGE plpgsql
    AS $$
DECLARE
    v_limite_semanal CONSTANT NUMERIC := 20;
BEGIN
    RETURN QUERY
        SELECT
            COALESCE(SUM(ra.horas_dedicadas)
                     FILTER (WHERE ra.estado_revision = 'APROBADO'), 0)
                                         AS horas_aprobadas,

            ROUND(
                    CEIL((a.fecha_fin - a.fecha_inicio) / 7.0) * v_limite_semanal, 2
            ) AS horas_totales_esperadas,

            CASE
                WHEN CEIL((a.fecha_fin - a.fecha_inicio) / 7.0) = 0 THEN 0
                ELSE ROUND(
                        COALESCE(SUM(ra.horas_dedicadas)
                                 FILTER (WHERE ra.estado_revision = 'APROBADO'), 0)
                            / (CEIL((a.fecha_fin - a.fecha_inicio) / 7.0) * v_limite_semanal)
                            * 100, 2
                     )
                END AS porcentaje_avance_global,

            DATE_TRUNC('week', CURRENT_DATE)::DATE
                                         AS semana_inicio,
            (DATE_TRUNC('week', CURRENT_DATE) + INTERVAL '6 days')::DATE
                                         AS semana_fin,

            COALESCE(SUM(ra.horas_dedicadas) FILTER (
                WHERE ra.fecha >= DATE_TRUNC('week', CURRENT_DATE)
                    AND ra.fecha <= DATE_TRUNC('week', CURRENT_DATE) + INTERVAL '6 days'
                ), 0) AS horas_semana_actual,

            GREATEST(
                    v_limite_semanal - COALESCE(SUM(ra.horas_dedicadas) FILTER (
                        WHERE ra.fecha >= DATE_TRUNC('week', CURRENT_DATE)
                            AND ra.fecha <= DATE_TRUNC('week', CURRENT_DATE) + INTERVAL '6 days'
                        ), 0), 0
            ) AS horas_disponibles_semana,

            COALESCE(SUM(ra.horas_dedicadas) FILTER (
                WHERE ra.fecha >= DATE_TRUNC('week', CURRENT_DATE)
                    AND ra.fecha <= DATE_TRUNC('week', CURRENT_DATE) + INTERVAL '6 days'
                ), 0) > v_limite_semanal AS supera_limite_semanal

        FROM ayudantia.ayudantia a
                 LEFT JOIN ayudantia.registro_actividad ra
                           ON ra.id_ayudantia = a.id_ayudantia
        WHERE a.id_ayudantia = p_id_ayudantia
        GROUP BY a.id_ayudantia, a.fecha_inicio, a.fecha_fin;
END;
$$;


--
-- Name: fn_progreso_general(integer); Type: FUNCTION; Schema: ayudantia; Owner: -
--

CREATE FUNCTION ayudantia.fn_progreso_general(p_id_usuario integer) RETURNS TABLE(horas_aprobadas numeric, horas_pendientes numeric, horas_observadas numeric, horas_totales_registradas numeric, horas_maximas numeric, porcentaje_avance numeric, total_sesiones bigint, sesiones_aprobadas bigint, sesiones_pendientes bigint, sesiones_observadas bigint)
    LANGUAGE plpgsql
    AS $$
DECLARE
    v_id_ayudantia INTEGER;
BEGIN
    -- Obtenemos el ID usando la lógica de contexto
    v_id_ayudantia := ayudantia.fn_obtener_id_ayudantia(p_id_usuario);

    -- Si no hay ayudantía, devolvemos una fila de ceros para evitar errores en el Backend
    IF v_id_ayudantia IS NULL THEN
        RETURN QUERY SELECT
                         0::NUMERIC, 0::NUMERIC, 0::NUMERIC, 0::NUMERIC, 0::NUMERIC,
                         0::NUMERIC, 0::BIGINT, 0::BIGINT, 0::BIGINT, 0::BIGINT;
        RETURN;
    END IF;

    RETURN QUERY
        SELECT
            COALESCE(SUM(ra.horas_dedicadas) FILTER (WHERE ter.codigo = 'APROBADO'), 0)::NUMERIC AS horas_aprobadas,
            COALESCE(SUM(ra.horas_dedicadas) FILTER (WHERE ter.codigo = 'PENDIENTE'), 0)::NUMERIC AS horas_pendientes,
            COALESCE(SUM(ra.horas_dedicadas) FILTER (WHERE ter.codigo = 'OBSERVADO'), 0)::NUMERIC AS horas_observadas,
            COALESCE(SUM(ra.horas_dedicadas), 0)::NUMERIC AS horas_totales_registradas,

            -- Cálculo de horas máximas totales del contrato
            ROUND(CEIL((a.fecha_fin - a.fecha_inicio) / 7.0)::NUMERIC * a.horas_semanales_max, 2) AS horas_maximas,

            -- Porcentaje de avance evitando división por cero
            CASE
                WHEN (a.fecha_fin - a.fecha_inicio) <= 0 OR a.horas_semanales_max = 0 THEN 0::NUMERIC
                ELSE ROUND(
                        (COALESCE(SUM(ra.horas_dedicadas) FILTER (WHERE ter.codigo = 'APROBADO'), 0) /
                         (CEIL((a.fecha_fin - a.fecha_inicio) / 7.0) * a.horas_semanales_max)::NUMERIC) * 100, 2
                     )
                END AS porcentaje_avance,

            COUNT(ra.id_registro_actividad) AS total_sesiones,
            COUNT(ra.id_registro_actividad) FILTER (WHERE ter.codigo = 'APROBADO') AS sesiones_aprobadas,
            COUNT(ra.id_registro_actividad) FILTER (WHERE ter.codigo = 'PENDIENTE') AS sesiones_pendientes,
            COUNT(ra.id_registro_actividad) FILTER (WHERE ter.codigo = 'OBSERVADO') AS sesiones_observadas

        FROM ayudantia.ayudantia a
                 LEFT JOIN ayudantia.registro_actividad ra ON ra.id_ayudantia = a.id_ayudantia
                 LEFT JOIN ayudantia.tipo_estado_registro ter ON ter.id_tipo_estado_registro = ra.id_tipo_estado_registro
        WHERE a.id_ayudantia = v_id_ayudantia
        GROUP BY a.id_ayudantia, a.fecha_inicio, a.fecha_fin, a.horas_semanales_max;
END;
$$;


--
-- Name: fn_registrar_actividad(integer, integer, text, text, date, integer, numeric, jsonb); Type: FUNCTION; Schema: ayudantia; Owner: -
--

CREATE FUNCTION ayudantia.fn_registrar_actividad(p_id_usuario integer, p_id_ayudantia integer, p_descripcion text, p_tema_tratado text, p_fecha date, p_numero_asistentes integer, p_horas_dedicadas numeric, p_evidencias jsonb) RETURNS TABLE(exito boolean, mensaje text, id_registro_creado integer)
    LANGUAGE plpgsql
    AS $$
DECLARE
    v_val              public.resultado_validacion;
    v_evidencia        JSONB;
    v_id_registro      INTEGER;
    v_count_evidencias INTEGER := 0;
    v_id_estado_pend   INTEGER;
    v_id_estado_sub    INTEGER;
    v_id_tipo_rol      INTEGER;
BEGIN
    v_val := ayudantia.fn_validar_identidad_ayudante(p_id_usuario, p_id_ayudantia);
    IF NOT v_val.valido THEN
        RETURN QUERY SELECT false, v_val.mensaje::text, NULL::integer;
        RETURN;
    END IF;

    v_val := ayudantia.fn_validar_estado_ayudantia(p_id_ayudantia);
    IF NOT v_val.valido THEN
        RETURN QUERY SELECT false, v_val.mensaje::text, NULL::integer;        RETURN;
    END IF;

    v_val := ayudantia.fn_validar_fecha_actividad(p_id_ayudantia, p_fecha);
    IF NOT v_val.valido THEN
        RETURN QUERY SELECT false, v_val.mensaje::text, NULL::integer;
        RETURN;
    END IF;

    v_val := ayudantia.fn_validar_horas_actividad(p_id_ayudantia, p_horas_dedicadas);
    IF NOT v_val.valido THEN
        RETURN QUERY SELECT false, v_val.mensaje::text, NULL::integer;
        RETURN;
    END IF;

    v_val := ayudantia.fn_validar_evidencias(p_evidencias);
    IF NOT v_val.valido THEN
        RETURN QUERY SELECT false, v_val.mensaje::text, NULL::integer;
        RETURN;
    END IF;

    SELECT id_tipo_estado_registro INTO v_id_estado_pend
    FROM ayudantia.tipo_estado_registro
    WHERE codigo = 'PENDIENTE';

    SELECT id_tipo_estado_evidencia INTO v_id_estado_sub
    FROM ayudantia.tipo_estado_evidencia
    WHERE codigo = 'PENDIENTE';

    INSERT INTO ayudantia.registro_actividad (
        id_ayudantia, descripcion_actividad, tema_tratado,
        fecha, numero_asistentes, horas_dedicadas, id_tipo_estado_registro
    )
    VALUES (
               p_id_ayudantia, p_descripcion, p_tema_tratado,
               p_fecha, p_numero_asistentes, p_horas_dedicadas, v_id_estado_pend
           )
    RETURNING id_registro_actividad INTO v_id_registro;

    FOR v_evidencia IN SELECT * FROM JSONB_ARRAY_ELEMENTS(p_evidencias)
        LOOP
            INSERT INTO ayudantia.evidencia_registro_actividad (
                id_registro_actividad,
                id_tipo_evidencia,
                id_tipo_estado_evidencia,
                nombre_archivo,
                ruta_archivo,
                mime_type,
                tamanio_bytes
            )
            VALUES (
                       v_id_registro,
                       (v_evidencia->>'id_tipo_evidencia')::INTEGER,
                       v_id_estado_sub,
                       v_evidencia->>'nombre_archivo',
                       v_evidencia->>'ruta_archivo',
                       v_evidencia->>'mime_type',
                       (v_evidencia->>'tamanio_bytes')::INTEGER
                   );
            v_count_evidencias := v_count_evidencias + 1;
        END LOOP;

    UPDATE ayudantia.ayudantia
    SET horas_cumplidas = horas_cumplidas + p_horas_dedicadas
    WHERE id_ayudantia = p_id_ayudantia;

    SELECT utr.id_tipo_rol INTO v_id_tipo_rol
    FROM seguridad.usuario_tipo_rol utr
             JOIN seguridad.tipo_rol tr ON tr.id_tipo_rol = utr.id_tipo_rol
    WHERE utr.id_usuario = p_id_usuario
      AND tr.nombre_tipo_rol = 'AYUDANTE_CATEDRA';

    INSERT INTO academico.log_auditoria (
        id_usuario, id_tipo_rol, accion, tabla_afectada,
        registro_afectado, fecha_hora, valor_nuevo
    )
    VALUES (
               p_id_usuario,
               v_id_tipo_rol,
               'INSERT',
               'registro_actividad',
               v_id_registro,
               CURRENT_TIMESTAMP,
               FORMAT(
                       '{"id_ayudantia":%s,"fecha":"%s","horas":%s,"evidencias":%s}',
                       p_id_ayudantia, p_fecha, p_horas_dedicadas, v_count_evidencias
               )
           );

    RETURN QUERY SELECT
                     true,
                     FORMAT('Actividad registrada exitosamente. %s evidencia(s) adjuntada(s).', v_count_evidencias),
                     v_id_registro;

END;
$$;


--
-- Name: fn_reporte_global_ayudantes(); Type: FUNCTION; Schema: ayudantia; Owner: -
--

CREATE FUNCTION ayudantia.fn_reporte_global_ayudantes() RETURNS TABLE(estudiante text, asignatura character varying, docente text, horas numeric, estado character varying)
    LANGUAGE plpgsql
    AS $$
BEGIN
    RETURN QUERY
    SELECT 
        u_est.nombres || ' ' || u_est.apellidos AS estudiante,
        a.nombre_asignatura::VARCHAR,
        u_doc.nombres || ' ' || u_doc.apellidos AS docente,
        COALESCE(ay.horas_cumplidas, 0)::NUMERIC AS horas,
        tea.nombre_estado::VARCHAR AS estado
    FROM ayudantia.ayudantia ay
    JOIN postulacion.postulacion p ON ay.id_postulacion = p.id_postulacion
    JOIN academico.estudiante e ON p.id_estudiante = e.id_estudiante
    JOIN seguridad.usuario u_est ON e.id_usuario = u_est.id_usuario
    JOIN convocatoria.convocatoria cv ON p.id_convocatoria = cv.id_convocatoria
    JOIN academico.asignatura a ON cv.id_asignatura = a.id_asignatura
    JOIN academico.docente d ON cv.id_docente = d.id_docente
    JOIN seguridad.usuario u_doc ON d.id_usuario = u_doc.id_usuario
    JOIN ayudantia.tipo_estado_ayudantia tea ON ay.id_tipo_estado_ayudantia = tea.id_tipo_estado_ayudantia
    WHERE ay.activo = TRUE OR tea.nombre_estado = 'ACTIVO'
    ORDER BY estudiante;
END;
$$;


--
-- Name: fn_total_horas_ayudante(integer); Type: FUNCTION; Schema: ayudantia; Owner: -
--

CREATE FUNCTION ayudantia.fn_total_horas_ayudante(p_id_ayudantia integer) RETURNS TABLE(id_ayudantia integer, total_horas numeric)
    LANGUAGE plpgsql
    AS $$
BEGIN
    RETURN QUERY
    SELECT
        ra.id_ayudantia,
        COALESCE(SUM(ra.horas_dedicadas), 0) AS total_horas
    FROM ayudantia.registro_actividad ra
    WHERE ra.id_ayudantia = p_id_ayudantia
    GROUP BY ra.id_ayudantia;
END;
$$;


--
-- Name: fn_ultimas_actividades(integer); Type: FUNCTION; Schema: ayudantia; Owner: -
--

CREATE FUNCTION ayudantia.fn_ultimas_actividades(p_id_ayudantia integer) RETURNS TABLE(id_registro integer, fecha date, tema_tratado text, horas_dedicadas numeric, estado_revision character varying)
    LANGUAGE plpgsql
    AS $$
BEGIN
    RETURN QUERY
    SELECT
        ra.id_registro_actividad,
        ra.fecha,
        ra.tema_tratado,
        ra.horas_dedicadas,
        ra.estado_revision
    FROM ayudantia.registro_actividad ra
    WHERE ra.id_ayudantia = p_id_ayudantia
    ORDER BY ra.fecha DESC
    LIMIT 5;
END;
$$;


--
-- Name: fn_validar_estado_ayudantia(integer); Type: FUNCTION; Schema: ayudantia; Owner: -
--

CREATE FUNCTION ayudantia.fn_validar_estado_ayudantia(p_id_ayudantia integer) RETURNS public.resultado_validacion
    LANGUAGE plpgsql
    AS $$
DECLARE
    v_id_estado_ayudantia INTEGER;
    v_nombre_estado       VARCHAR(50);
    v_estado_periodo      VARCHAR(30);
    v_resultado           public.resultado_validacion;
BEGIN
    SELECT a.id_tipo_estado_ayudantia, tea.nombre_estado
    INTO v_id_estado_ayudantia, v_nombre_estado
    FROM ayudantia.ayudantia a
             INNER JOIN ayudantia.tipo_estado_ayudantia tea
                        ON tea.id_tipo_estado_ayudantia = a.id_tipo_estado_ayudantia
    WHERE a.id_ayudantia = p_id_ayudantia;

    IF NOT FOUND THEN
        v_resultado.valido  := false;
        v_resultado.mensaje := 'La ayudantía no existe.';
        RETURN v_resultado;
    END IF;

    IF v_id_estado_ayudantia <> 21 THEN
        v_resultado.valido  := false;
        v_resultado.mensaje := FORMAT('La ayudantía no está activa. Estado actual: %s.', v_nombre_estado);
        RETURN v_resultado;
    END IF;

    SELECT pa.estado INTO v_estado_periodo
    FROM ayudantia.ayudantia a
             INNER JOIN postulacion.postulacion pp ON pp.id_postulacion = a.id_postulacion
             INNER JOIN convocatoria.convocatoria co ON co.id_convocatoria = pp.id_convocatoria
             INNER JOIN academico.periodo_academico pa ON pa.id_periodo_academico = co.id_periodo_academico
    WHERE a.id_ayudantia = p_id_ayudantia;

    IF TRIM(UPPER(v_estado_periodo)) <> 'EN PROCESO' THEN
        v_resultado.valido  := false;
        v_resultado.mensaje := 'El periodo académico no está en proceso.';
        RETURN v_resultado;
    END IF;

    v_resultado.valido  := true;
    v_resultado.mensaje := 'OK';
    RETURN v_resultado;
END;
$$;


--
-- Name: fn_validar_evidencias(jsonb); Type: FUNCTION; Schema: ayudantia; Owner: -
--

CREATE FUNCTION ayudantia.fn_validar_evidencias(p_evidencias jsonb) RETURNS public.resultado_validacion
    LANGUAGE plpgsql
    AS $$
DECLARE
    v_evidencia  JSONB;
    v_idx        INTEGER := 0;
    v_ext_perm   VARCHAR(10);
    v_nombre_f   TEXT;
    v_id_tipo    INTEGER;
    v_resultado  public.resultado_validacion;
BEGIN
    -- 1. Validación de nulidad y mínimo
    IF p_evidencias IS NULL OR JSONB_ARRAY_LENGTH(p_evidencias) = 0 THEN
        v_resultado.valido  := false;
        v_resultado.mensaje := 'Debe adjuntar al menos una evidencia.'::text;
        RETURN v_resultado;
    END IF;

    -- 2. Validación de cantidad máxima
    IF JSONB_ARRAY_LENGTH(p_evidencias) > 5 THEN
        v_resultado.valido  := false;
        v_resultado.mensaje := FORMAT(
                'Máximo 5 evidencias por actividad. Intentó adjuntar %s.',
                JSONB_ARRAY_LENGTH(p_evidencias)
                               )::text;
        RETURN v_resultado;
    END IF;

    -- 3. Iteración sobre evidencias
    FOR v_evidencia IN SELECT * FROM JSONB_ARRAY_ELEMENTS(p_evidencias)
        LOOP
            v_idx := v_idx + 1;
            v_nombre_f := v_evidencia->>'nombre_archivo';
            v_id_tipo  := (v_evidencia->>'id_tipo_evidencia')::INTEGER;

            -- Validar presencia de datos básicos
            IF TRIM(COALESCE(v_nombre_f, '')) = '' THEN
                v_resultado.valido  := false;
                v_resultado.mensaje := FORMAT('La evidencia #%s no tiene nombre de archivo.', v_idx)::text;
                RETURN v_resultado;
            END IF;

            IF TRIM(COALESCE(v_evidencia->>'ruta_archivo', '')) = '' THEN
                v_resultado.valido  := false;
                v_resultado.mensaje := FORMAT('La evidencia #%s no tiene ruta de archivo.', v_idx)::text;
                RETURN v_resultado;
            END IF;

            -- 4. Validar tipo de evidencia y obtener su extensión permitida (Ej: .pdf, .jpg)
            SELECT extension_permitida INTO v_ext_perm
            FROM ayudantia.tipo_evidencia
            WHERE id_tipo_evidencia = v_id_tipo
              AND activo = true;

            IF NOT FOUND THEN
                v_resultado.valido  := false;
                v_resultado.mensaje := FORMAT('El tipo de evidencia (ID: %s) de la evidencia #%s no es válido o está inactivo.', v_id_tipo, v_idx)::text;
                RETURN v_resultado;
            END IF;

            -- 5. Validar que el nombre del archivo termine con la extensión permitida
            -- Usamos LOWER para evitar problemas con .JPG vs .jpg
            IF LOWER(v_nombre_f) NOT LIKE '%' || LOWER(v_ext_perm) THEN
                v_resultado.valido  := false;
                v_resultado.mensaje := FORMAT(
                        'La evidencia #%s (%s) no tiene una extensión válida. Se esperaba: %s',
                        v_idx, v_nombre_f, v_ext_perm
                                       )::text;
                RETURN v_resultado;
            END IF;

        END LOOP;

    -- 6. Respuesta exitosa
    v_resultado.valido  := true;
    v_resultado.mensaje := 'OK'::text;
    RETURN v_resultado;
END;
$$;


--
-- Name: fn_validar_fecha_actividad(integer, date); Type: FUNCTION; Schema: ayudantia; Owner: -
--

CREATE FUNCTION ayudantia.fn_validar_fecha_actividad(p_id_ayudantia integer, p_fecha date) RETURNS public.resultado_validacion
    LANGUAGE plpgsql
    AS $$
DECLARE
    v_ayudantia RECORD;
    v_resultado public.resultado_validacion; -- Variable para estandarizar el retorno
BEGIN
    -- 1. Obtener el rango de fechas de la ayudantía
    SELECT fecha_inicio, fecha_fin INTO v_ayudantia
    FROM ayudantia.ayudantia
    WHERE id_ayudantia = p_id_ayudantia;

    -- 2. Validación: Fecha futura
    IF p_fecha > CURRENT_DATE THEN
        v_resultado.valido := false;
        v_resultado.mensaje := 'No se puede registrar una actividad con fecha futura.'::text;
        RETURN v_resultado;
    END IF;

    -- 3. Validación: Rango de la ayudantía
    IF p_fecha < v_ayudantia.fecha_inicio OR p_fecha > v_ayudantia.fecha_fin THEN
        v_resultado.valido := false;
        v_resultado.mensaje := FORMAT(
                'La fecha %s está fuera del rango de la ayudantía (%s - %s).',
                p_fecha, v_ayudantia.fecha_inicio, v_ayudantia.fecha_fin
                               )::text;
        RETURN v_resultado;
    END IF;

    -- 4. Validación: Registro duplicado para la misma fecha
    PERFORM 1
    FROM ayudantia.registro_actividad
    WHERE id_ayudantia = p_id_ayudantia
      AND fecha = p_fecha;

    IF FOUND THEN
        v_resultado.valido := false;
        v_resultado.mensaje := FORMAT(
                'Ya existe un registro de actividad para la fecha %s.', p_fecha
                               )::text;
        RETURN v_resultado;
    END IF;

    -- 5. Respuesta exitosa
    v_resultado.valido := true;
    v_resultado.mensaje := 'OK'::text;
    RETURN v_resultado;
END;
$$;


--
-- Name: fn_validar_horas_actividad(integer, numeric); Type: FUNCTION; Schema: ayudantia; Owner: -
--

CREATE FUNCTION ayudantia.fn_validar_horas_actividad(p_id_ayudantia integer, p_horas numeric) RETURNS public.resultado_validacion
    LANGUAGE plpgsql
    AS $$
DECLARE
    v_ayudantia     RECORD;
    v_horas_semana  NUMERIC(6,2);
    v_resultado     public.resultado_validacion; -- Variable de control
BEGIN
    SELECT horas_cumplidas, horas_maximas, horas_semanales_max
    INTO v_ayudantia
    FROM ayudantia.ayudantia
    WHERE id_ayudantia = p_id_ayudantia;

    -- Validación de rango
    IF p_horas <= 0 OR p_horas > 24 THEN
        v_resultado.valido := false;
        v_resultado.mensaje := 'Las horas deben ser un valor entre 0.01 y 24.'::text;
        RETURN v_resultado;
    END IF;

    -- Cálculo semanal
    SELECT COALESCE(SUM(horas_dedicadas), 0) INTO v_horas_semana
    FROM ayudantia.registro_actividad
    WHERE id_ayudantia = p_id_ayudantia
      AND fecha >= DATE_TRUNC('week', CURRENT_DATE)
      AND fecha <= DATE_TRUNC('week', CURRENT_DATE) + INTERVAL '6 days';

    -- Validación límite semanal
    IF (v_horas_semana + p_horas) > v_ayudantia.horas_semanales_max THEN
        v_resultado.valido := false;
        v_resultado.mensaje := FORMAT(
                'Supera el límite semanal. Llevas %.2fh y el límite es %.2fh. Disponibles: %.2fh.',
                v_horas_semana, v_ayudantia.horas_semanales_max, (v_ayudantia.horas_semanales_max - v_horas_semana)
                               )::text;
        RETURN v_resultado;
    END IF;

    -- Validación total acumulado
    IF v_ayudantia.horas_maximas IS NOT NULL THEN
        IF (v_ayudantia.horas_cumplidas + p_horas) > v_ayudantia.horas_maximas THEN
            v_resultado.valido := false;
            v_resultado.mensaje := FORMAT(
                    'Supera el total permitido. Llevas %.2fh de %.2fh máximas.',
                    v_ayudantia.horas_cumplidas, v_ayudantia.horas_maximas
                                   )::text;
            RETURN v_resultado;
        END IF;
    END IF;

    v_resultado.valido := true;
    v_resultado.mensaje := 'OK'::text;
    RETURN v_resultado;
END;
$$;


--
-- Name: fn_validar_identidad_ayudante(integer, integer); Type: FUNCTION; Schema: ayudantia; Owner: -
--

CREATE FUNCTION ayudantia.fn_validar_identidad_ayudante(p_id_usuario integer, p_id_ayudantia integer) RETURNS public.resultado_validacion
    LANGUAGE plpgsql
    AS $$
DECLARE
    v_resultado public.resultado_validacion;
BEGIN
    -- Validar perfil de ayudante
    PERFORM 1
    FROM seguridad.usuario_tipo_rol utr
    JOIN seguridad.tipo_rol tr ON tr.id_tipo_rol = utr.id_tipo_rol
    WHERE utr.id_usuario = p_id_usuario
      AND tr.nombre_tipo_rol = 'AYUDANTE_CATEDRA'
      AND utr.activo = true;

    IF NOT FOUND THEN
        v_resultado.valido := false;
        v_resultado.mensaje := 'El usuario no tiene perfil activo de ayudante de cátedra.';
        RETURN v_resultado;
    END IF;

    -- Validar pertenencia a la ayudantía
    PERFORM 1
    FROM ayudantia.ayudantia a
    JOIN postulacion.postulacion pp ON pp.id_postulacion = a.id_postulacion
    JOIN academico.estudiante est ON est.id_estudiante = pp.id_estudiante
    WHERE a.id_ayudantia = p_id_ayudantia
      AND est.id_usuario = p_id_usuario;

    IF NOT FOUND THEN
        v_resultado.valido := false;
        v_resultado.mensaje := 'El ayudante no pertenece a esta ayudantía.';
        RETURN v_resultado;
    END IF;

    -- Si todo está bien
    v_resultado.valido := true;
    v_resultado.mensaje := 'OK';
    RETURN v_resultado;
END;
$$;


--
-- Name: fn_validar_limite_semanal(integer); Type: FUNCTION; Schema: ayudantia; Owner: -
--

CREATE FUNCTION ayudantia.fn_validar_limite_semanal(p_id_ayudantia integer) RETURNS TABLE(semana_inicio date, semana_fin date, horas_semana_actual numeric, horas_disponibles numeric, supera_limite boolean)
    LANGUAGE plpgsql
    AS $$
DECLARE
    v_limite_semanal CONSTANT NUMERIC := 20;
BEGIN
    RETURN QUERY
    SELECT
        DATE_TRUNC('week', CURRENT_DATE)::DATE              AS semana_inicio,
        (DATE_TRUNC('week', CURRENT_DATE) + INTERVAL '6 days')::DATE AS semana_fin,
        COALESCE(SUM(ra.horas_dedicadas), 0)               AS horas_semana_actual,
        GREATEST(v_limite_semanal - COALESCE(SUM(ra.horas_dedicadas), 0), 0) AS horas_disponibles,
        COALESCE(SUM(ra.horas_dedicadas), 0) > v_limite_semanal AS supera_limite
    FROM ayudantia.registro_actividad ra
    WHERE ra.id_ayudantia = p_id_ayudantia
      AND ra.fecha >= DATE_TRUNC('week', CURRENT_DATE)
      AND ra.fecha <= DATE_TRUNC('week', CURRENT_DATE) + INTERVAL '6 days';
END;
$$;


--
-- Name: fn_actualizar_convocatoria(jsonb, text); Type: FUNCTION; Schema: convocatoria; Owner: -
--

CREATE FUNCTION convocatoria.fn_actualizar_convocatoria(p_datos jsonb, p_tipo_edicion text) RETURNS jsonb
    LANGUAGE plpgsql SECURITY DEFINER
    AS $$
DECLARE
    v_id            INTEGER;
    v_cupos         INTEGER;
    v_estado        TEXT;
    v_id_docente    INTEGER;
    v_id_asignatura INTEGER;
    v_check_post    JSONB;
    v_fase_check    JSONB;
BEGIN
    -- Normalizar tipo
    p_tipo_edicion := UPPER(TRIM(COALESCE(p_tipo_edicion, '')));
    IF p_tipo_edicion NOT IN ('PARCIAL', 'COMPLETA') THEN
        RETURN jsonb_build_object('exito', false, 'mensaje', 'p_tipo_edicion debe ser PARCIAL o COMPLETA.');
    END IF;

    v_id    := (p_datos->>'idConvocatoria')::integer;
    v_cupos := (p_datos->>'cuposDisponibles')::integer;
    v_estado := NULLIF(TRIM(COALESCE(p_datos->>'estado', '')), '');

    IF v_id IS NULL THEN
        RETURN jsonb_build_object('exito', false, 'mensaje', 'idConvocatoria es obligatorio.');
    END IF;

    IF NOT EXISTS (SELECT 1 FROM convocatoria.convocatoria WHERE id_convocatoria = v_id) THEN
        RETURN jsonb_build_object('exito', false, 'mensaje', 'Convocatoria no encontrada.');
    END IF;

    -- ── EDICIÓN PARCIAL ────────────────────────────────────────
    IF p_tipo_edicion = 'PARCIAL' THEN
        IF v_cupos IS NOT NULL AND v_cupos < 1 THEN
            RETURN jsonb_build_object('exito', false, 'mensaje', 'cuposDisponibles debe ser ≥ 1.');
        END IF;

        UPDATE convocatoria.convocatoria
        SET cupos_disponibles = COALESCE(v_cupos,  cupos_disponibles),
            estado            = COALESCE(v_estado, estado)
        WHERE id_convocatoria = v_id;

        RETURN jsonb_build_object('exito', true,
                                  'mensaje', 'Convocatoria actualizada (edición parcial).', 'id', v_id);
    END IF;

    -- ── EDICIÓN COMPLETA ───────────────────────────────────────

    -- 1. Validar cero postulantes
    v_check_post := convocatoria.fn_verificar_postulantes(v_id);
    IF NOT (v_check_post->>'exito')::boolean THEN
        RETURN v_check_post;
    END IF;
    IF (v_check_post->>'tienePostulantes')::boolean THEN
        RETURN jsonb_build_object(
                'exito',   false,
                'mensaje', '[BLOQUEO] No se puede editar completamente: ' || (v_check_post->>'mensaje')
               );
    END IF;

    -- 2. Validar fase del cronograma
    v_fase_check := convocatoria.fn_verificar_restricciones_fase();
    IF NOT (v_fase_check->>'valido')::boolean THEN
        RETURN jsonb_build_object(
                'exito',   false,
                'mensaje', '[FASE] Edición completa rechazada. ' || (v_fase_check->>'mensaje')
               );
    END IF;

    v_id_docente    := (p_datos->>'idDocente')::integer;
    v_id_asignatura := (p_datos->>'idAsignatura')::integer;

    IF v_cupos IS NOT NULL AND v_cupos < 1 THEN
        RETURN jsonb_build_object('exito', false, 'mensaje', 'cuposDisponibles debe ser ≥ 1.');
    END IF;

    IF v_id_docente IS NOT NULL AND NOT EXISTS (
        SELECT 1 FROM academico.docente WHERE id_docente = v_id_docente AND activo = TRUE
    ) THEN
        RETURN jsonb_build_object('exito', false, 'mensaje', 'El docente indicado no existe o no está activo.');
    END IF;

    IF v_id_asignatura IS NOT NULL AND NOT EXISTS (
        SELECT 1 FROM academico.asignatura WHERE id_asignatura = v_id_asignatura AND activo = TRUE
    ) THEN
        RETURN jsonb_build_object('exito', false, 'mensaje', 'La asignatura indicada no existe o no está activa.');
    END IF;

    UPDATE convocatoria.convocatoria
    SET id_docente        = COALESCE(v_id_docente,    id_docente),
        id_asignatura     = COALESCE(v_id_asignatura, id_asignatura),
        cupos_disponibles = COALESCE(v_cupos,         cupos_disponibles),
        estado            = COALESCE(v_estado,        estado)
    WHERE id_convocatoria = v_id;

    RETURN jsonb_build_object('exito', true,
                              'mensaje', 'Convocatoria actualizada (edición completa).', 'id', v_id);

EXCEPTION WHEN OTHERS THEN
    RETURN jsonb_build_object('exito', false, 'mensaje', '[ERROR] ' || SQLERRM);
END;
$$;


--
-- Name: fn_actualizar_tipo_estado_requisito(integer, character varying, text, character varying); Type: FUNCTION; Schema: convocatoria; Owner: -
--

CREATE FUNCTION convocatoria.fn_actualizar_tipo_estado_requisito(p_id integer, p_nombre_estado character varying, p_descripcion text, p_codigo character varying) RETURNS jsonb
    LANGUAGE plpgsql SECURITY DEFINER
    AS $$
BEGIN
    -- Validar que el registro exista
    IF NOT EXISTS (
        SELECT 1 FROM convocatoria.tipo_estado_requisito
        WHERE id_tipo_estado_requisito = p_id
    ) THEN
        RETURN jsonb_build_object('exito', false, 'mensaje', 'No se encontró el estado de requisito con el ID especificado');
    END IF;

    -- Validar que el nombre no exista en otro registro (case-insensitive)
    IF EXISTS (
        SELECT 1 FROM convocatoria.tipo_estado_requisito
        WHERE LOWER(TRIM(nombre_estado)) = LOWER(TRIM(p_nombre_estado))
        AND id_tipo_estado_requisito != p_id
    ) THEN
        RETURN jsonb_build_object('exito', false, 'mensaje', 'Ya existe otro estado de requisito con el nombre especificado');
    END IF;

    -- Validar que el código no exista en otro registro (case-insensitive)
    IF EXISTS (
        SELECT 1 FROM convocatoria.tipo_estado_requisito
        WHERE LOWER(TRIM(codigo)) = LOWER(TRIM(p_codigo))
        AND id_tipo_estado_requisito != p_id
    ) THEN
        RETURN jsonb_build_object('exito', false, 'mensaje', 'Ya existe otro estado de requisito con el código especificado');
    END IF;

    -- Actualizar el registro
    UPDATE convocatoria.tipo_estado_requisito
    SET nombre_estado = TRIM(p_nombre_estado),
        descripcion = TRIM(p_descripcion),
        codigo = UPPER(TRIM(p_codigo))
    WHERE id_tipo_estado_requisito = p_id;

    RETURN jsonb_build_object(
        'exito', true,
        'mensaje', 'Estado de requisito actualizado exitosamente',
        'id', p_id
    );

EXCEPTION
    WHEN unique_violation THEN
        RETURN jsonb_build_object('exito', false, 'mensaje', 'Error de duplicidad: el nombre o código ya existe');
    WHEN OTHERS THEN
        RETURN jsonb_build_object('exito', false, 'mensaje', 'Error al actualizar estado de requisito: ' || SQLERRM);
END;
$$;


--
-- Name: fn_calcular_cronograma_convocatoria(date, date); Type: FUNCTION; Schema: convocatoria; Owner: -
--

CREATE FUNCTION convocatoria.fn_calcular_cronograma_convocatoria(p_fecha_publicacion date, p_fecha_cierre date) RETURNS jsonb
    LANGUAGE plpgsql SECURITY DEFINER
    AS $$
DECLARE
    v_fecha_revision_inicio DATE;
    v_fecha_revision_fin DATE;
    v_fecha_resultados DATE;
    v_cronograma JSONB;
BEGIN
    IF p_fecha_publicacion IS NULL OR p_fecha_cierre IS NULL THEN
        RETURN jsonb_build_object(
                'exito', FALSE,
                'mensaje', 'Las fechas de publicación y cierre son requeridas'
               );
    END IF;

    v_fecha_revision_inicio := p_fecha_cierre + INTERVAL '1 day';
    v_fecha_revision_fin := p_fecha_cierre + INTERVAL '7 days';

    v_fecha_resultados := v_fecha_revision_fin + INTERVAL '1 day';

    v_cronograma := jsonb_build_object(
            'exito', TRUE,
            'etapas', jsonb_build_array(
                    jsonb_build_object(
                            'numero', 1,
                            'nombre', 'Postulación',
                            'descripcion', 'Período para enviar postulación y documentos',
                            'fecha_inicio', p_fecha_publicacion,
                            'fecha_fin', p_fecha_cierre,
                            'estado', CASE
                                          WHEN CURRENT_DATE < p_fecha_publicacion THEN 'PENDIENTE'
                                          WHEN CURRENT_DATE BETWEEN p_fecha_publicacion AND p_fecha_cierre THEN 'EN_CURSO'
                                          ELSE 'COMPLETADA'
                                END
                    ),
                    jsonb_build_object(
                            'numero', 2,
                            'nombre', 'Revisión',
                            'descripcion', 'Revisión de documentos por el coordinador',
                            'fecha_inicio', v_fecha_revision_inicio,
                            'fecha_fin', v_fecha_revision_fin,
                            'estado', CASE
                                          WHEN CURRENT_DATE < v_fecha_revision_inicio THEN 'PENDIENTE'
                                          WHEN CURRENT_DATE BETWEEN v_fecha_revision_inicio AND v_fecha_revision_fin THEN 'EN_CURSO'
                                          ELSE 'COMPLETADA'
                                END
                    ),
                    jsonb_build_object(
                            'numero', 3,
                            'nombre', 'Resultados',
                            'descripcion', 'Publicación de resultados finales',
                            'fecha_inicio', v_fecha_resultados,
                            'fecha_fin', v_fecha_resultados,
                            'estado', CASE
                                          WHEN CURRENT_DATE < v_fecha_resultados THEN 'PENDIENTE'
                                          WHEN CURRENT_DATE = v_fecha_resultados THEN 'EN_CURSO'
                                          ELSE 'COMPLETADA'
                                END
                    )
                      )
                    );

    RETURN v_cronograma;
END;
$$;


--
-- Name: fn_crear_convocatoria(jsonb); Type: FUNCTION; Schema: convocatoria; Owner: -
--

CREATE FUNCTION convocatoria.fn_crear_convocatoria(p_datos jsonb) RETURNS jsonb
    LANGUAGE plpgsql SECURITY DEFINER
    AS $$
DECLARE
    v_fase_check    JSONB;
    v_id_periodo    INTEGER;
    v_id_asignatura INTEGER;
    v_id_docente    INTEGER;
    v_cupos         INTEGER;
    v_estado        TEXT;
    v_nueva_id      INTEGER;
BEGIN
    -- 1. Validar fase del cronograma
    v_fase_check := convocatoria.fn_verificar_restricciones_fase();
    IF NOT (v_fase_check->>'valido')::boolean THEN
        RETURN jsonb_build_object('exito', false, 'mensaje', v_fase_check->>'mensaje');
    END IF;

    v_id_periodo := (v_fase_check->>'idPeriodo')::integer;

    -- 2. Extraer y validar campos
    v_id_asignatura := (p_datos->>'idAsignatura')::integer;
    v_id_docente    := (p_datos->>'idDocente')::integer;
    v_cupos         := (p_datos->>'cuposDisponibles')::integer;
    v_estado        := COALESCE(NULLIF(TRIM(p_datos->>'estado'), ''), 'ABIERTA');

    IF v_id_asignatura IS NULL THEN
        RETURN jsonb_build_object('exito', false, 'mensaje', '[VALIDACIÓN] El campo idAsignatura es obligatorio.');
    END IF;
    IF v_id_docente IS NULL THEN
        RETURN jsonb_build_object('exito', false, 'mensaje', '[VALIDACIÓN] El campo idDocente es obligatorio.');
    END IF;
    IF v_cupos IS NULL OR v_cupos < 1 THEN
        RETURN jsonb_build_object('exito', false, 'mensaje', '[VALIDACIÓN] cuposDisponibles debe ser un entero mayor a 0.');
    END IF;

    -- 3. Verificar existencia y estado de asignatura
    IF NOT EXISTS (
        SELECT 1 FROM academico.asignatura WHERE id_asignatura = v_id_asignatura AND activo = TRUE
    ) THEN
        RETURN jsonb_build_object('exito', false, 'mensaje', 'La asignatura indicada no existe o no está activa.');
    END IF;

    -- 4. Verificar existencia y estado de docente
    IF NOT EXISTS (
        SELECT 1 FROM academico.docente WHERE id_docente = v_id_docente AND activo = TRUE
    ) THEN
        RETURN jsonb_build_object('exito', false, 'mensaje', 'El docente indicado no existe o no está activo.');
    END IF;

    -- 5. Verificar duplicado en el mismo periodo
    IF EXISTS (
        SELECT 1 FROM convocatoria.convocatoria
        WHERE id_periodo_academico = v_id_periodo
          AND id_asignatura        = v_id_asignatura
          AND id_docente           = v_id_docente
          AND activo               = TRUE
    ) THEN
        RETURN jsonb_build_object(
                'exito',   false,
                'mensaje', 'Ya existe una convocatoria activa para ese docente y asignatura en el período vigente.'
               );
    END IF;

    -- 6. Insertar
    INSERT INTO convocatoria.convocatoria (
        id_periodo_academico, id_asignatura, id_docente,
        cupos_disponibles, estado, activo
    ) VALUES (
                 v_id_periodo, v_id_asignatura, v_id_docente,
                 v_cupos, v_estado, TRUE
             )
    RETURNING id_convocatoria INTO v_nueva_id;

    RETURN jsonb_build_object(
            'exito',   true,
            'mensaje', 'Convocatoria creada exitosamente.',
            'id',      v_nueva_id
           );

EXCEPTION WHEN OTHERS THEN
    RETURN jsonb_build_object('exito', false, 'mensaje', '[ERROR] ' || SQLERRM);
END;
$$;


--
-- Name: fn_crear_tipo_estado_requisito(character varying, text, character varying); Type: FUNCTION; Schema: convocatoria; Owner: -
--

CREATE FUNCTION convocatoria.fn_crear_tipo_estado_requisito(p_nombre_estado character varying, p_descripcion text, p_codigo character varying) RETURNS jsonb
    LANGUAGE plpgsql SECURITY DEFINER
    AS $$
DECLARE
    v_id INTEGER;
BEGIN
    -- Validar que el nombre no exista (case-insensitive)
    IF EXISTS (
        SELECT 1 FROM convocatoria.tipo_estado_requisito
        WHERE LOWER(TRIM(nombre_estado)) = LOWER(TRIM(p_nombre_estado))
    ) THEN
        RETURN jsonb_build_object('exito', false, 'mensaje', 'Ya existe un estado de requisito con el nombre especificado');
    END IF;

    -- Validar que el código no exista (case-insensitive)
    IF EXISTS (
        SELECT 1 FROM convocatoria.tipo_estado_requisito
        WHERE LOWER(TRIM(codigo)) = LOWER(TRIM(p_codigo))
    ) THEN
        RETURN jsonb_build_object('exito', false, 'mensaje', 'Ya existe un estado de requisito con el código especificado');
    END IF;

    -- Insertar el nuevo registro
    INSERT INTO convocatoria.tipo_estado_requisito (nombre_estado, descripcion, codigo, activo)
    VALUES (TRIM(p_nombre_estado), TRIM(p_descripcion), UPPER(TRIM(p_codigo)), TRUE)
    RETURNING id_tipo_estado_requisito INTO v_id;

    RETURN jsonb_build_object(
        'exito', true,
        'mensaje', 'Estado de requisito creado exitosamente',
        'id', v_id
    );

EXCEPTION
    WHEN unique_violation THEN
        RETURN jsonb_build_object('exito', false, 'mensaje', 'Error de duplicidad: el nombre o código ya existe');
    WHEN OTHERS THEN
        RETURN jsonb_build_object('exito', false, 'mensaje', 'Error al crear estado de requisito: ' || SQLERRM);
END;
$$;


--
-- Name: fn_desactivar_convocatoria(integer); Type: FUNCTION; Schema: convocatoria; Owner: -
--

CREATE FUNCTION convocatoria.fn_desactivar_convocatoria(p_id integer) RETURNS jsonb
    LANGUAGE plpgsql SECURITY DEFINER
    AS $$
DECLARE
    v_check JSONB;
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM convocatoria.convocatoria WHERE id_convocatoria = p_id
    ) THEN
        RETURN jsonb_build_object('exito', false, 'mensaje', 'Convocatoria no encontrada con ID ' || p_id);
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM convocatoria.convocatoria WHERE id_convocatoria = p_id AND activo = TRUE
    ) THEN
        RETURN jsonb_build_object('exito', false, 'mensaje', 'La convocatoria ya se encuentra inactiva.');
    END IF;

    v_check := convocatoria.fn_verificar_postulantes(p_id);
    IF NOT (v_check->>'exito')::boolean THEN
        RETURN v_check;
    END IF;

    IF (v_check->>'tienePostulantes')::boolean THEN
        RETURN jsonb_build_object(
                'exito',   false,
                'mensaje', '[BLOQUEO] No se puede desactivar: ' || (v_check->>'mensaje')
                    || ' Primero gestione las postulaciones activas.'
               );
    END IF;

    UPDATE convocatoria.convocatoria
    SET activo = FALSE,
        estado = 'CANCELADA'
    WHERE id_convocatoria = p_id;

    RETURN jsonb_build_object(
            'exito',   true,
            'mensaje', 'Convocatoria desactivada correctamente.',
            'id',      p_id
           );

EXCEPTION WHEN OTHERS THEN
    RETURN jsonb_build_object('exito', false, 'mensaje', '[ERROR] ' || SQLERRM);
END;
$$;


--
-- Name: fn_eliminar_tipo_estado_requisito(integer); Type: FUNCTION; Schema: convocatoria; Owner: -
--

CREATE FUNCTION convocatoria.fn_eliminar_tipo_estado_requisito(p_id integer) RETURNS jsonb
    LANGUAGE plpgsql SECURITY DEFINER
    AS $$
BEGIN
    -- Validar que el registro exista
    IF NOT EXISTS (
        SELECT 1 FROM convocatoria.tipo_estado_requisito
        WHERE id_tipo_estado_requisito = p_id
    ) THEN
        RETURN jsonb_build_object('exito', false, 'mensaje', 'No se encontró el estado de requisito con el ID especificado');
    END IF;

    -- Eliminación lógica: setear activo = false
    UPDATE convocatoria.tipo_estado_requisito
    SET activo = FALSE
    WHERE id_tipo_estado_requisito = p_id;

    RETURN jsonb_build_object(
        'exito', true,
        'mensaje', 'Estado de requisito desactivado exitosamente',
        'id', p_id
    );

EXCEPTION
    WHEN OTHERS THEN
        RETURN jsonb_build_object('exito', false, 'mensaje', 'Error al desactivar estado de requisito: ' || SQLERRM);
END;
$$;


--
-- Name: fn_es_periodo_subsanacion(integer); Type: FUNCTION; Schema: convocatoria; Owner: -
--

CREATE FUNCTION convocatoria.fn_es_periodo_subsanacion(p_id_convocatoria integer) RETURNS boolean
    LANGUAGE plpgsql SECURITY DEFINER
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


--
-- Name: FUNCTION fn_es_periodo_subsanacion(p_id_convocatoria integer); Type: COMMENT; Schema: convocatoria; Owner: -
--

COMMENT ON FUNCTION convocatoria.fn_es_periodo_subsanacion(p_id_convocatoria integer) IS 'Verifica si la fecha actual permite subsanación: periodo ACTIVO/EN PROCESO y dentro de POSTULACION hasta EVALUACION_REQUISITOS.';


--
-- Name: fn_listar_convocatorias_activas_periodo(); Type: FUNCTION; Schema: convocatoria; Owner: -
--

CREATE FUNCTION convocatoria.fn_listar_convocatorias_activas_periodo() RETURNS TABLE(id_convocatoria integer, nombre_asignatura character varying, nombre_docente text, estado character varying, cupos_disponibles integer)
    LANGUAGE plpgsql STABLE
    AS $$
DECLARE
    v_id_periodo INTEGER;
BEGIN
    -- ✅ Mismo fix aplicado de forma consistente
    SELECT academico.fn_obtener_id_periodo_activo()
    INTO   v_id_periodo;

    IF v_id_periodo IS NULL THEN
        RETURN;
    END IF;

    RETURN QUERY
        SELECT
            c.id_convocatoria,
            a.nombre_asignatura,
            (u.nombres || ' ' || u.apellidos)::TEXT  AS nombre_docente,
            c.estado,
            c.cupos_disponibles
        FROM   convocatoria.convocatoria  c
                   JOIN   academico.asignatura       a  ON a.id_asignatura = c.id_asignatura
                   JOIN   academico.docente          d  ON d.id_docente    = c.id_docente
                   JOIN   seguridad.usuario          u  ON u.id_usuario    = d.id_usuario
        WHERE  c.id_periodo_academico = v_id_periodo
          AND  c.activo  = TRUE
          AND  c.estado IN ('ABIERTA', 'EN_EVALUACION')
        ORDER  BY a.nombre_asignatura;
END;
$$;


--
-- Name: fn_listar_convocatorias_estudiante(integer); Type: FUNCTION; Schema: convocatoria; Owner: -
--

CREATE FUNCTION convocatoria.fn_listar_convocatorias_estudiante(p_id_usuario integer) RETURNS TABLE(id_convocatoria integer, nombre_asignatura character varying, semestre_asignatura integer, nombre_carrera character varying, nombre_docente character varying, cupos_disponibles integer, fecha_inicio_postulacion date, fecha_fin_postulacion date, estado_convocatoria character varying, puede_postular boolean)
    LANGUAGE plpgsql STABLE
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


--
-- Name: fn_listar_tipo_estado_requisito(); Type: FUNCTION; Schema: convocatoria; Owner: -
--

CREATE FUNCTION convocatoria.fn_listar_tipo_estado_requisito() RETURNS jsonb
    LANGUAGE plpgsql SECURITY DEFINER
    AS $$
DECLARE
    v_resultado JSONB;
BEGIN
    SELECT COALESCE(jsonb_agg(
        jsonb_build_object(
            'id', id_tipo_estado_requisito,
            'nombre_estado', nombre_estado,
            'descripcion', descripcion,
            'codigo', codigo,
            'activo', activo
        ) ORDER BY nombre_estado
    ), '[]'::jsonb)
    INTO v_resultado
    FROM convocatoria.tipo_estado_requisito
    WHERE activo = TRUE;

    RETURN jsonb_build_object(
        'exito', true,
        'datos', v_resultado
    );

EXCEPTION
    WHEN OTHERS THEN
        RETURN jsonb_build_object('exito', false, 'mensaje', 'Error al listar estados de requisito: ' || SQLERRM);
END;
$$;


--
-- Name: fn_verificar_postulantes(integer); Type: FUNCTION; Schema: convocatoria; Owner: -
--

CREATE FUNCTION convocatoria.fn_verificar_postulantes(p_id_convocatoria integer) RETURNS jsonb
    LANGUAGE plpgsql STABLE SECURITY DEFINER
    AS $$
DECLARE
    v_total INTEGER;
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM convocatoria.convocatoria WHERE id_convocatoria = p_id_convocatoria
    ) THEN
        RETURN jsonb_build_object(
                'exito',   false,
                'mensaje', 'Convocatoria no encontrada con ID ' || p_id_convocatoria
               );
    END IF;

    SELECT COUNT(*)
    INTO   v_total
    FROM   postulacion.postulacion
    WHERE  id_convocatoria = p_id_convocatoria
      AND  activo = TRUE;

    RETURN jsonb_build_object(
            'exito',            true,
            'tienePostulantes', v_total > 0,
            'totalPostulantes', v_total,
            'mensaje',          CASE WHEN v_total > 0
                                         THEN 'La convocatoria tiene ' || v_total || ' postulante(s) activo(s).'
                                     ELSE 'La convocatoria no tiene postulantes activos.'
                END
           );

EXCEPTION WHEN OTHERS THEN
    RETURN jsonb_build_object('exito', false, 'mensaje', SQLERRM);
END;
$$;


--
-- Name: fn_verificar_restricciones_fase(); Type: FUNCTION; Schema: convocatoria; Owner: -
--

CREATE FUNCTION convocatoria.fn_verificar_restricciones_fase() RETURNS jsonb
    LANGUAGE plpgsql STABLE SECURITY DEFINER
    AS $$
DECLARE
    v_fase RECORD;
BEGIN
    SELECT
        pa.id_periodo_academico,
        pa.nombre_periodo,
        tf.codigo   AS codigo_fase,
        tf.nombre   AS nombre_fase,
        pf.fecha_inicio,
        pf.fecha_fin
    INTO v_fase
    FROM  academico.periodo_academico  pa
              JOIN  planificacion.periodo_fase   pf ON pf.id_periodo_academico = pa.id_periodo_academico
              JOIN  planificacion.tipo_fase      tf ON tf.id_tipo_fase          = pf.id_tipo_fase
    WHERE pa.estado = 'EN PROCESO'
      AND pa.activo = TRUE
      AND tf.codigo IN ('PLANIFICACION_CONVOCATORI', 'PUBLICACION_OFERTA')
      AND CURRENT_DATE BETWEEN pf.fecha_inicio AND pf.fecha_fin
    ORDER BY tf.orden ASC
    LIMIT 1;

    IF NOT FOUND THEN
        RETURN jsonb_build_object(
                'valido',  false,
                'mensaje', 'No es posible gestionar convocatorias en este momento. '
                               || 'La fecha actual está fuera de las fases habilitadas '
                    || '(Creación/Borrador y Publicación de Oferta Académica).'
               );
    END IF;

    RETURN jsonb_build_object(
            'valido',        true,
            'mensaje',       'Fase habilitada: ' || v_fase.nombre_fase,
            'idPeriodo',     v_fase.id_periodo_academico,
            'nombrePeriodo', v_fase.nombre_periodo,
            'codigoFase',    v_fase.codigo_fase,
            'nombreFase',    v_fase.nombre_fase,
            'faseInicio',    to_char(v_fase.fecha_inicio, 'YYYY-MM-DD'),
            'faseFin',       to_char(v_fase.fecha_fin,    'YYYY-MM-DD')
           );
END;
$$;


--
-- Name: fn_auditoria_global(); Type: FUNCTION; Schema: notificacion; Owner: -
--

CREATE FUNCTION notificacion.fn_auditoria_global() RETURNS TABLE(id_log_auditoria integer, fecha_hora timestamp without time zone, usuario text, roles text, facultad text, carrera text, accion text, tabla_afectada text, detalle text)
    LANGUAGE plpgsql
    AS $$
BEGIN
    RETURN QUERY
    SELECT 
        l.id_log_auditoria,
        l.fecha_hora,
        (u.nombres || ' ' || u.apellidos)::TEXT as usuario,
        (SELECT STRING_AGG(tr.nombre_tipo_rol, ', ') 
         FROM seguridad.usuario_tipo_rol utr 
         JOIN seguridad.tipo_rol tr ON tr.id_tipo_rol = utr.id_tipo_rol 
         WHERE utr.id_usuario = u.id_usuario AND utr.activo = true)::TEXT as roles,
        COALESCE(f_dec.nombre_facultad, f_coor.nombre_facultad, f_doc_data.nombre_facultad, f_est.nombre_facultad, 'N/A')::TEXT as facultad,
        COALESCE(c_coor.nombre_carrera, c_doc_data.nombre_carrera, c_est.nombre_carrera, 'N/A')::TEXT as carrera,
        l.accion::TEXT,
        l.tabla_afectada::TEXT,
        ('Anterior: ' || COALESCE(l.valor_anterior, 'N/A') || ' | Nuevo: ' || COALESCE(l.valor_nuevo, 'N/A'))::TEXT as detalle
    FROM notificacion.log_auditoria l
    JOIN seguridad.usuario u ON u.id_usuario = l.id_usuario
    -- Decano
    LEFT JOIN academico.decano d ON d.id_usuario = u.id_usuario AND d.activo = true
    LEFT JOIN academico.facultad f_dec ON f_dec.id_facultad = d.id_facultad
    -- Coordinador
    LEFT JOIN academico.coordinador coor ON coor.id_usuario = u.id_usuario AND coor.activo = true
    LEFT JOIN academico.carrera c_coor ON c_coor.id_carrera = coor.id_carrera
    LEFT JOIN academico.facultad f_coor ON f_coor.id_facultad = c_coor.id_facultad
    -- Docente (Contexto de su primera asignatura asignada)
    LEFT JOIN LATERAL (
        SELECT ca.nombre_carrera, fa.nombre_facultad
        FROM academico.docente doc
        JOIN academico.docente_asignatura da ON da.id_docente = doc.id_docente
        JOIN academico.asignatura a ON a.id_asignatura = da.id_asignatura
        JOIN academico.carrera ca ON ca.id_carrera = a.id_carrera
        JOIN academico.facultad fa ON fa.id_facultad = ca.id_facultad
        WHERE doc.id_usuario = u.id_usuario AND doc.activo = true
        LIMIT 1
    ) f_doc_data ON true
    -- Estudiante
    LEFT JOIN academico.estudiante e ON e.id_usuario = u.id_usuario
    LEFT JOIN academico.carrera c_est ON c_est.id_carrera = e.id_carrera
    LEFT JOIN academico.facultad f_est ON f_est.id_facultad = c_est.id_facultad
    ORDER BY l.fecha_hora DESC;
END;
$$;


--
-- Name: FUNCTION fn_auditoria_global(); Type: COMMENT; Schema: notificacion; Owner: -
--

COMMENT ON FUNCTION notificacion.fn_auditoria_global() IS 'Retorna el historial completo de auditoría enriquecido con el rol y contexto académico del usuario responsable.';


--
-- Name: fn_actualizar_tipo_fase(integer, character varying, character varying, text, integer); Type: FUNCTION; Schema: planificacion; Owner: -
--

CREATE FUNCTION planificacion.fn_actualizar_tipo_fase(p_id integer, p_codigo character varying, p_nombre character varying, p_descripcion text, p_orden integer) RETURNS jsonb
    LANGUAGE plpgsql SECURITY DEFINER
    AS $$
BEGIN
    -- Validar que el orden sea positivo
    IF p_orden <= 0 THEN
        RETURN jsonb_build_object('exito', false, 'mensaje', 'El orden debe ser un número positivo mayor a cero');
    END IF;

    -- Validar que el registro exista
    IF NOT EXISTS (
        SELECT 1 FROM planificacion.tipo_fase
        WHERE id_tipo_fase = p_id
    ) THEN
        RETURN jsonb_build_object('exito', false, 'mensaje', 'No se encontró el tipo de fase con el ID especificado');
    END IF;

    -- Validar que el código no exista en otro registro (case-insensitive)
    IF EXISTS (
        SELECT 1 FROM planificacion.tipo_fase
        WHERE LOWER(TRIM(codigo)) = LOWER(TRIM(p_codigo))
        AND id_tipo_fase != p_id
    ) THEN
        RETURN jsonb_build_object('exito', false, 'mensaje', 'Ya existe otra fase con el código especificado');
    END IF;

    -- Validar que el nombre no exista en otro registro (case-insensitive)
    IF EXISTS (
        SELECT 1 FROM planificacion.tipo_fase
        WHERE LOWER(TRIM(nombre)) = LOWER(TRIM(p_nombre))
        AND id_tipo_fase != p_id
    ) THEN
        RETURN jsonb_build_object('exito', false, 'mensaje', 'Ya existe otra fase con el nombre especificado');
    END IF;

    -- Validar que el orden no exista en otro registro
    IF EXISTS (
        SELECT 1 FROM planificacion.tipo_fase
        WHERE orden = p_orden
        AND id_tipo_fase != p_id
    ) THEN
        RETURN jsonb_build_object('exito', false, 'mensaje', 'Ya existe otra fase con el orden especificado');
    END IF;

    -- Actualizar el registro
    UPDATE planificacion.tipo_fase
    SET codigo = UPPER(TRIM(p_codigo)),
        nombre = TRIM(p_nombre),
        descripcion = TRIM(p_descripcion),
        orden = p_orden
    WHERE id_tipo_fase = p_id;

    RETURN jsonb_build_object(
        'exito', true,
        'mensaje', 'Tipo de fase actualizado exitosamente',
        'id', p_id
    );

EXCEPTION
    WHEN unique_violation THEN
        RETURN jsonb_build_object('exito', false, 'mensaje', 'Error de duplicidad: el código, nombre u orden ya existe');
    WHEN check_violation THEN
        RETURN jsonb_build_object('exito', false, 'mensaje', 'El orden debe ser un número positivo mayor a cero');
    WHEN OTHERS THEN
        RETURN jsonb_build_object('exito', false, 'mensaje', 'Error al actualizar tipo de fase: ' || SQLERRM);
END;
$$;


--
-- Name: fn_ajustar_cronograma_lote(integer, jsonb); Type: FUNCTION; Schema: planificacion; Owner: -
--

CREATE FUNCTION planificacion.fn_ajustar_cronograma_lote(p_id_periodo integer, p_fases_json jsonb) RETURNS jsonb
    LANGUAGE plpgsql SECURITY DEFINER
    AS $$
DECLARE
    v_periodo               RECORD;
    v_fase                  RECORD;
    v_total_fases_json      INTEGER;
    v_total_fases_sistema   INTEGER;

BEGIN
    SELECT
        id_periodo_academico,
        nombre_periodo,
        fecha_inicio,
        fecha_fin,
        estado,
        activo
    INTO v_periodo
    FROM academico.periodo_academico
    WHERE id_periodo_academico = p_id_periodo;

    IF NOT FOUND THEN
        RETURN jsonb_build_object(
                'exito',   false,
                'mensaje', 'No se encontró el período académico con ID ' || p_id_periodo
               );
    END IF;


    IF v_periodo.estado NOT IN ('PLANIFICACION', 'CONFIGURADO') THEN
        RETURN jsonb_build_object(
                'exito',   false,
                'mensaje', 'El período "' || v_periodo.nombre_periodo
                               || '" tiene estado "' || v_periodo.estado
                               || '" y no admite ajustes de cronograma. '
                    || 'Solo los períodos en estado PLANIFICACION o CONFIGURADO pueden modificarse.'
               );
    END IF;

    IF p_fases_json IS NULL OR jsonb_typeof(p_fases_json) != 'array' THEN
        RETURN jsonb_build_object(
                'exito',   false,
                'mensaje', 'El parámetro p_fases_json debe ser un arreglo JSON válido'
               );
    END IF;

    v_total_fases_json := jsonb_array_length(p_fases_json);

    IF v_total_fases_json = 0 THEN
        RETURN jsonb_build_object(
                'exito',   false,
                'mensaje', 'El arreglo de fases no puede estar vacío. '
                    || 'Debe incluir todas las fases del cronograma.'
               );
    END IF;

    SELECT COUNT(*) INTO v_total_fases_sistema
    FROM planificacion.tipo_fase
    WHERE activo = TRUE;

    IF v_total_fases_json != v_total_fases_sistema THEN
        RETURN jsonb_build_object(
                'exito',   false,
                'mensaje', 'Se enviaron ' || v_total_fases_json || ' fase(s) pero el sistema '
                               || 'tiene ' || v_total_fases_sistema || ' fase(s) activa(s). '
                    || 'El cronograma debe incluir exactamente todas las fases activas.'
               );
    END IF;

    IF EXISTS (
        SELECT 1
        FROM   jsonb_array_elements(p_fases_json) AS elem
        WHERE  NOT EXISTS (
            SELECT 1
            FROM   planificacion.tipo_fase tf
            WHERE  tf.id_tipo_fase = (elem->>'id_tipo_fase')::INTEGER
              AND  tf.activo = TRUE
        )
    ) THEN
        RETURN jsonb_build_object(
                'exito',   false,
                'mensaje', 'El arreglo contiene id_tipo_fase inválidos o pertenecientes a fases inactivas. '
                    || 'Verifique los identificadores enviados.'
               );
    END IF;

    DELETE FROM planificacion.periodo_fase
    WHERE  id_periodo_academico = p_id_periodo;

    FOR v_fase IN
        SELECT
            (elem->>'id_tipo_fase')::INTEGER  AS id_tipo_fase,
            (elem->>'fecha_inicio')::DATE     AS fecha_inicio,
            (elem->>'fecha_fin')::DATE        AS fecha_fin,
            tf.orden                           AS orden
        FROM   jsonb_array_elements(p_fases_json) AS elem
                   JOIN   planificacion.tipo_fase tf
                          ON tf.id_tipo_fase = (elem->>'id_tipo_fase')::INTEGER
        ORDER BY tf.orden ASC
        LOOP
            INSERT INTO planificacion.periodo_fase (
                id_periodo_academico,
                id_tipo_fase,
                fecha_inicio,
                fecha_fin
            )
            VALUES (
                       p_id_periodo,
                       v_fase.id_tipo_fase,
                       v_fase.fecha_inicio,
                       v_fase.fecha_fin
                   );
        END LOOP;

    UPDATE academico.periodo_academico
    SET    estado = 'CONFIGURADO'
    WHERE  id_periodo_academico = p_id_periodo;

    RETURN jsonb_build_object(
            'exito',   true,
            'mensaje', 'Cronograma del período "' || v_periodo.nombre_periodo
                           || '" ajustado exitosamente con ' || v_total_fases_json
                || ' fase(s). Estado actualizado a CONFIGURADO.',
            'id',      p_id_periodo
           );

EXCEPTION
    WHEN OTHERS THEN
        RETURN jsonb_build_object(
                'exito',   false,
                'mensaje', SQLERRM
               );
END;
$$;


--
-- Name: fn_crear_tipo_fase(character varying, character varying, text, integer); Type: FUNCTION; Schema: planificacion; Owner: -
--

CREATE FUNCTION planificacion.fn_crear_tipo_fase(p_codigo character varying, p_nombre character varying, p_descripcion text, p_orden integer) RETURNS jsonb
    LANGUAGE plpgsql SECURITY DEFINER
    AS $$
DECLARE
    v_id INTEGER;
BEGIN
    -- Validar que el orden sea positivo
    IF p_orden <= 0 THEN
        RETURN jsonb_build_object('exito', false, 'mensaje', 'El orden debe ser un número positivo mayor a cero');
    END IF;

    -- Validar que el código no exista (case-insensitive)
    IF EXISTS (
        SELECT 1 FROM planificacion.tipo_fase
        WHERE LOWER(TRIM(codigo)) = LOWER(TRIM(p_codigo))
    ) THEN
        RETURN jsonb_build_object('exito', false, 'mensaje', 'Ya existe una fase con el código especificado');
    END IF;

    -- Validar que el nombre no exista (case-insensitive)
    IF EXISTS (
        SELECT 1 FROM planificacion.tipo_fase
        WHERE LOWER(TRIM(nombre)) = LOWER(TRIM(p_nombre))
    ) THEN
        RETURN jsonb_build_object('exito', false, 'mensaje', 'Ya existe una fase con el nombre especificado');
    END IF;

    -- Validar que el orden no exista
    IF EXISTS (
        SELECT 1 FROM planificacion.tipo_fase
        WHERE orden = p_orden
    ) THEN
        RETURN jsonb_build_object('exito', false, 'mensaje', 'Ya existe una fase con el orden especificado');
    END IF;

    -- Insertar el nuevo registro
    INSERT INTO planificacion.tipo_fase (codigo, nombre, descripcion, orden, activo)
    VALUES (UPPER(TRIM(p_codigo)), TRIM(p_nombre), TRIM(p_descripcion), p_orden, TRUE)
    RETURNING id_tipo_fase INTO v_id;

    RETURN jsonb_build_object(
        'exito', true,
        'mensaje', 'Tipo de fase creado exitosamente',
        'id', v_id
    );

EXCEPTION
    WHEN unique_violation THEN
        RETURN jsonb_build_object('exito', false, 'mensaje', 'Error de duplicidad: el código, nombre u orden ya existe');
    WHEN check_violation THEN
        RETURN jsonb_build_object('exito', false, 'mensaje', 'El orden debe ser un número positivo mayor a cero');
    WHEN OTHERS THEN
        RETURN jsonb_build_object('exito', false, 'mensaje', 'Error al crear tipo de fase: ' || SQLERRM);
END;
$$;


--
-- Name: fn_eliminar_tipo_fase(integer); Type: FUNCTION; Schema: planificacion; Owner: -
--

CREATE FUNCTION planificacion.fn_eliminar_tipo_fase(p_id integer) RETURNS jsonb
    LANGUAGE plpgsql SECURITY DEFINER
    AS $$
BEGIN
    -- Validar que el registro exista
    IF NOT EXISTS (
        SELECT 1 FROM planificacion.tipo_fase
        WHERE id_tipo_fase = p_id
    ) THEN
        RETURN jsonb_build_object('exito', false, 'mensaje', 'No se encontró el tipo de fase con el ID especificado');
    END IF;

    -- Eliminación lógica: setear activo = false
    UPDATE planificacion.tipo_fase
    SET activo = FALSE
    WHERE id_tipo_fase = p_id;

    RETURN jsonb_build_object(
        'exito', true,
        'mensaje', 'Tipo de fase desactivado exitosamente',
        'id', p_id
    );

EXCEPTION
    WHEN OTHERS THEN
        RETURN jsonb_build_object('exito', false, 'mensaje', 'Error al desactivar tipo de fase: ' || SQLERRM);
END;
$$;


--
-- Name: fn_listar_tipo_fase(); Type: FUNCTION; Schema: planificacion; Owner: -
--

CREATE FUNCTION planificacion.fn_listar_tipo_fase() RETURNS jsonb
    LANGUAGE plpgsql SECURITY DEFINER
    AS $$
DECLARE
    v_resultado JSONB;
BEGIN
    SELECT COALESCE(jsonb_agg(
        jsonb_build_object(
            'id', id_tipo_fase,
            'codigo', codigo,
            'nombre', nombre,
            'descripcion', descripcion,
            'orden', orden,
            'activo', activo
        ) ORDER BY orden
    ), '[]'::jsonb)
    INTO v_resultado
    FROM planificacion.tipo_fase
    WHERE activo = TRUE;

    RETURN jsonb_build_object(
        'exito', true,
        'datos', v_resultado
    );

EXCEPTION
    WHEN OTHERS THEN
        RETURN jsonb_build_object('exito', false, 'mensaje', 'Error al listar tipos de fase: ' || SQLERRM);
END;
$$;


--
-- Name: fn_obtener_cronograma_activo(); Type: FUNCTION; Schema: planificacion; Owner: -
--

CREATE FUNCTION planificacion.fn_obtener_cronograma_activo() RETURNS jsonb
    LANGUAGE plpgsql STABLE SECURITY DEFINER
    AS $$
DECLARE
    v_periodo    RECORD;
    v_fases      JSONB;
    v_dias_total INTEGER;
    v_dias_trans INTEGER;
    v_pct        NUMERIC(5,2);
BEGIN
    SELECT
        id_periodo_academico,
        nombre_periodo,
        fecha_inicio,
        fecha_fin
    INTO v_periodo
    FROM academico.periodo_academico
    WHERE estado  = 'EN PROCESO'
      AND activo  = TRUE
    ORDER BY fecha_inicio DESC
    LIMIT 1;

    IF NOT FOUND THEN
        RETURN jsonb_build_object(
                'exito',   false,
                'mensaje', 'No existe un período académico activo en este momento. '
                    || 'Consulte con el administrador del sistema.'
               );
    END IF;

    v_dias_total := (v_periodo.fecha_fin - v_periodo.fecha_inicio) + 1;
    v_dias_trans := GREATEST(0,
                             LEAST(
                                     (CURRENT_DATE - v_periodo.fecha_inicio) + 1,
                                     v_dias_total
                             )
                    );
    v_pct := ROUND((v_dias_trans::NUMERIC / v_dias_total::NUMERIC) * 100, 2);

    SELECT jsonb_agg(
                   jsonb_build_object(
                           'idPeriodoFase', pf.id_periodo_fase,
                           'idTipoFase',    pf.id_tipo_fase,
                           'orden',         tf.orden,
                           'codigo',        tf.codigo,
                           'nombre',        tf.nombre,
                           'descripcion',   COALESCE(tf.descripcion, ''),
                           'fechaInicio',   to_char(pf.fecha_inicio, 'YYYY-MM-DD'),
                           'fechaFin',      to_char(pf.fecha_fin,    'YYYY-MM-DD'),
                           'duracionDias',  (pf.fecha_fin - pf.fecha_inicio) + 1,
                           'esActual',      (CURRENT_DATE BETWEEN pf.fecha_inicio AND pf.fecha_fin)
                   )
                   ORDER BY tf.orden ASC
           )
    INTO v_fases
    FROM planificacion.periodo_fase pf
             JOIN planificacion.tipo_fase    tf ON tf.id_tipo_fase = pf.id_tipo_fase
    WHERE pf.id_periodo_academico = v_periodo.id_periodo_academico;

    IF v_fases IS NULL THEN
        v_fases := '[]'::jsonb;
    END IF;

    RETURN jsonb_build_object(
            'exito',   true,
            'periodo', jsonb_build_object(
                    'id',                v_periodo.id_periodo_academico,
                    'nombre',            v_periodo.nombre_periodo,
                    'fechaInicio',       to_char(v_periodo.fecha_inicio, 'YYYY-MM-DD'),
                    'fechaFin',          to_char(v_periodo.fecha_fin,    'YYYY-MM-DD'),
                    'diasTranscurridos', v_dias_trans,
                    'diasTotales',       v_dias_total,
                    'porcentajeAvance',  v_pct
                       ),
            'fases',   v_fases
           );

EXCEPTION
    WHEN OTHERS THEN
        RETURN jsonb_build_object(
                'exito',   false,
                'mensaje', 'Error al obtener el cronograma activo: ' || SQLERRM
               );
END;
$$;


--
-- Name: fn_validar_periodo_fase(); Type: FUNCTION; Schema: planificacion; Owner: -
--

CREATE FUNCTION planificacion.fn_validar_periodo_fase() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
DECLARE
    v_periodo_inicio        DATE;
    v_periodo_fin           DATE;
    v_nombre_periodo        VARCHAR;
    v_orden_nueva_fase      INTEGER;
    v_fase_anterior_fin     DATE;
    v_fase_anterior_nombre  VARCHAR;
    v_fase_siguiente_inicio DATE;
    v_fase_siguiente_nombre VARCHAR;
    v_solape_fase_nombre    VARCHAR;
BEGIN
    SELECT pa.fecha_inicio, pa.fecha_fin, pa.nombre_periodo
    INTO   v_periodo_inicio, v_periodo_fin, v_nombre_periodo
    FROM   academico.periodo_academico pa
    WHERE  pa.id_periodo_academico = NEW.id_periodo_academico;

    IF NOT FOUND THEN
        RAISE EXCEPTION '[CALENDARIO] El período académico % no existe.',
            NEW.id_periodo_academico;
    END IF;

    IF NEW.fecha_inicio < v_periodo_inicio
        OR NEW.fecha_fin    > v_periodo_fin
    THEN
        RAISE EXCEPTION
            '[CONTENCIÓN] La fase % (% → %) está fuera del período "%" (% → %). '
                'Ajuste las fechas para que queden dentro del período académico.',
            (SELECT codigo FROM planificacion.tipo_fase WHERE id_tipo_fase = NEW.id_tipo_fase),
            NEW.fecha_inicio, NEW.fecha_fin,
            v_nombre_periodo, v_periodo_inicio, v_periodo_fin;
    END IF;

    SELECT tf.orden INTO v_orden_nueva_fase
    FROM   planificacion.tipo_fase tf
    WHERE  tf.id_tipo_fase = NEW.id_tipo_fase;


    SELECT tf.nombre, pf.fecha_fin
    INTO   v_fase_anterior_nombre, v_fase_anterior_fin
    FROM   planificacion.periodo_fase pf
               JOIN   planificacion.tipo_fase    tf ON tf.id_tipo_fase = pf.id_tipo_fase
    WHERE  pf.id_periodo_academico = NEW.id_periodo_academico
      AND  pf.id_periodo_fase      <> NEW.id_periodo_fase  -- excluir la fila actual en UPDATE
      AND  tf.orden = (
        SELECT MAX(tf2.orden)
        FROM   planificacion.periodo_fase  pf2
                   JOIN   planificacion.tipo_fase     tf2 ON tf2.id_tipo_fase = pf2.id_tipo_fase
        WHERE  pf2.id_periodo_academico = NEW.id_periodo_academico
          AND  pf2.id_periodo_fase      <> NEW.id_periodo_fase
          AND  tf2.orden < v_orden_nueva_fase
    );

    IF FOUND AND NEW.fecha_inicio < v_fase_anterior_fin THEN
        RAISE EXCEPTION
            '[SECUENCIA] La fase "%" no puede iniciar el % '
                'porque la fase anterior "%" aún no ha terminado (termina el %). '
                'Solapamiento de % día(s).',
            (SELECT codigo FROM planificacion.tipo_fase WHERE id_tipo_fase = NEW.id_tipo_fase),
            NEW.fecha_inicio,
            v_fase_anterior_nombre,
            v_fase_anterior_fin,
            (v_fase_anterior_fin - NEW.fecha_inicio);
    END IF;

    SELECT tf.nombre, pf.fecha_inicio
    INTO   v_fase_siguiente_nombre, v_fase_siguiente_inicio
    FROM   planificacion.periodo_fase pf
               JOIN   planificacion.tipo_fase    tf ON tf.id_tipo_fase = pf.id_tipo_fase
    WHERE  pf.id_periodo_academico = NEW.id_periodo_academico
      AND  pf.id_periodo_fase      <> NEW.id_periodo_fase
      AND  tf.orden = (
        SELECT MIN(tf2.orden)
        FROM   planificacion.periodo_fase  pf2
                   JOIN   planificacion.tipo_fase     tf2 ON tf2.id_tipo_fase = pf2.id_tipo_fase
        WHERE  pf2.id_periodo_academico = NEW.id_periodo_academico
          AND  pf2.id_periodo_fase      <> NEW.id_periodo_fase
          AND  tf2.orden > v_orden_nueva_fase
    );

    IF FOUND AND NEW.fecha_fin > v_fase_siguiente_inicio THEN
        RAISE EXCEPTION
            '[SECUENCIA] La fase "%" no puede terminar el % '
                'porque la fase siguiente "%" ya inicia el %. '
                'Solapamiento de % día(s).',
            (SELECT codigo FROM planificacion.tipo_fase WHERE id_tipo_fase = NEW.id_tipo_fase),
            NEW.fecha_fin,
            v_fase_siguiente_nombre,
            v_fase_siguiente_inicio,
            (NEW.fecha_fin - v_fase_siguiente_inicio);
    END IF;


    SELECT tf.nombre
    INTO   v_solape_fase_nombre
    FROM   planificacion.periodo_fase pf
               JOIN   planificacion.tipo_fase    tf ON tf.id_tipo_fase = pf.id_tipo_fase
    WHERE  pf.id_periodo_academico = NEW.id_periodo_academico
      AND  pf.id_periodo_fase      <> NEW.id_periodo_fase  -- excluir la fila actual
      AND  NEW.fecha_inicio        <= pf.fecha_fin          -- rango nuevo empieza antes de que termine el existente
      AND  NEW.fecha_fin           >= pf.fecha_inicio       -- rango nuevo termina después de que empiece el existente
    LIMIT 1;

    IF FOUND THEN
        RAISE EXCEPTION
            '[SOLAPAMIENTO] La fase "%" se solapa con la fase "%" en el período %. '
                'No pueden existir dos fases activas simultáneamente.',
            (SELECT codigo FROM planificacion.tipo_fase WHERE id_tipo_fase = NEW.id_tipo_fase),
            v_solape_fase_nombre,
            NEW.id_periodo_academico;
    END IF;

    RETURN NEW;
END;
$$;


--
-- Name: FUNCTION fn_validar_periodo_fase(); Type: COMMENT; Schema: planificacion; Owner: -
--

COMMENT ON FUNCTION planificacion.fn_validar_periodo_fase() IS 'Valida tres reglas de integridad temporal: (1) Contención dentro del período,
     (2) Secuencia estricta entre fases, (3) No solapamiento de rangos de fecha.';


--
-- Name: fn_verificar_fase_actual(text); Type: FUNCTION; Schema: planificacion; Owner: -
--

CREATE FUNCTION planificacion.fn_verificar_fase_actual(p_nombre_fase text) RETURNS boolean
    LANGUAGE sql STABLE PARALLEL SAFE
    AS $$
SELECT EXISTS (
    SELECT 1
    FROM   planificacion.periodo_fase pf
               JOIN   planificacion.tipo_fase    tf  ON tf.id_tipo_fase = pf.id_tipo_fase
               JOIN   academico.periodo_academico pa ON pa.id_periodo_academico = pf.id_periodo_academico
    WHERE  UPPER(tf.codigo)  = UPPER(p_nombre_fase)
      AND  CURRENT_DATE BETWEEN pf.fecha_inicio AND pf.fecha_fin
      AND  pa.activo = TRUE
);
$$;


--
-- Name: fn_verificar_fase_actual_detalle(text, integer); Type: FUNCTION; Schema: planificacion; Owner: -
--

CREATE FUNCTION planificacion.fn_verificar_fase_actual_detalle(p_nombre_fase text, p_id_periodo_academico integer) RETURNS boolean
    LANGUAGE sql STABLE PARALLEL SAFE
    AS $$
SELECT EXISTS (
    SELECT 1
    FROM   planificacion.periodo_fase pf
               JOIN   planificacion.tipo_fase    tf
                      ON tf.id_tipo_fase = pf.id_tipo_fase
    WHERE  pf.id_periodo_academico = p_id_periodo_academico
      AND  UPPER(tf.codigo)        = UPPER(p_nombre_fase)   -- insensible a mayúsculas
      AND  CURRENT_DATE BETWEEN pf.fecha_inicio AND pf.fecha_fin
);
$$;


--
-- Name: fn_actualizar_tipo_estado_postulacion(integer, character varying, character varying, character varying); Type: FUNCTION; Schema: postulacion; Owner: -
--

CREATE FUNCTION postulacion.fn_actualizar_tipo_estado_postulacion(p_id integer, p_codigo character varying, p_nombre character varying, p_descripcion character varying) RETURNS jsonb
    LANGUAGE plpgsql SECURITY DEFINER
    AS $$
BEGIN
    -- Validar que el registro exista
    IF NOT EXISTS (
        SELECT 1 FROM postulacion.tipo_estado_postulacion
        WHERE id_tipo_estado_postulacion = p_id
    ) THEN
        RETURN jsonb_build_object('exito', false, 'mensaje', 'No se encontró el estado de postulación con el ID especificado');
    END IF;

    -- Validar que el código no exista en otro registro (case-insensitive)
    IF EXISTS (
        SELECT 1 FROM postulacion.tipo_estado_postulacion
        WHERE LOWER(TRIM(codigo)) = LOWER(TRIM(p_codigo))
        AND id_tipo_estado_postulacion != p_id
    ) THEN
        RETURN jsonb_build_object('exito', false, 'mensaje', 'Ya existe otro estado de postulación con el código especificado');
    END IF;

    -- Validar que el nombre no exista en otro registro (case-insensitive)
    IF EXISTS (
        SELECT 1 FROM postulacion.tipo_estado_postulacion
        WHERE LOWER(TRIM(nombre)) = LOWER(TRIM(p_nombre))
        AND id_tipo_estado_postulacion != p_id
    ) THEN
        RETURN jsonb_build_object('exito', false, 'mensaje', 'Ya existe otro estado de postulación con el nombre especificado');
    END IF;

    -- Actualizar el registro
    UPDATE postulacion.tipo_estado_postulacion
    SET codigo = UPPER(TRIM(p_codigo)),
        nombre = TRIM(p_nombre),
        descripcion = TRIM(p_descripcion)
    WHERE id_tipo_estado_postulacion = p_id;

    RETURN jsonb_build_object(
        'exito', true,
        'mensaje', 'Estado de postulación actualizado exitosamente',
        'id', p_id
    );

EXCEPTION
    WHEN unique_violation THEN
        RETURN jsonb_build_object('exito', false, 'mensaje', 'Error de duplicidad: el código ya existe');
    WHEN OTHERS THEN
        RETURN jsonb_build_object('exito', false, 'mensaje', 'Error al actualizar estado de postulación: ' || SQLERRM);
END;
$$;


--
-- Name: fn_cambiar_estado_evaluacion(integer, text); Type: FUNCTION; Schema: postulacion; Owner: -
--

CREATE FUNCTION postulacion.fn_cambiar_estado_evaluacion(p_id_evaluacion_oposicion integer, p_accion text) RETURNS jsonb
    LANGUAGE plpgsql SECURITY DEFINER
    AS $$
DECLARE
    v_codigo_actual   TEXT;
    v_id_estado_nuevo INTEGER;
    v_puntaje_total   NUMERIC(5,2);
    v_id_comision     INTEGER;
    v_hora_inicio_now TIME;
    v_ts_inicio       TEXT;
BEGIN
    p_accion := UPPER(TRIM(COALESCE(p_accion, '')));

    SELECT tee.codigo
    INTO   v_codigo_actual
    FROM   postulacion.evaluacion_oposicion   eo
               JOIN   postulacion.tipo_estado_evaluacion tee
                      ON tee.id_tipo_estado_evaluacion = eo.id_tipo_estado_evaluacion
    WHERE  eo.id_evaluacion_oposicion = p_id_evaluacion_oposicion;

    IF NOT FOUND THEN
        RETURN jsonb_build_object('exito', false, 'mensaje', 'Evaluación no encontrada.');
    END IF;


    IF p_accion = 'INICIAR' THEN
        IF v_codigo_actual != 'PROGRAMADA' THEN
            RETURN jsonb_build_object(
                    'exito',   false,
                    'mensaje', 'Solo se puede iniciar una evaluación PROGRAMADA. Estado actual: ' || v_codigo_actual
                   );
        END IF;

        -- Resolver la comisión vinculada a esta convocatoria
        SELECT cs.id_comision_seleccion INTO v_id_comision
        FROM   postulacion.evaluacion_oposicion eo
                   JOIN   postulacion.postulacion          p  ON p.id_postulacion   = eo.id_postulacion
                   JOIN   postulacion.comision_seleccion   cs ON cs.id_convocatoria = p.id_convocatoria
        WHERE  eo.id_evaluacion_oposicion = p_id_evaluacion_oposicion
          AND  cs.activo = TRUE
        LIMIT 1;

        IF v_id_comision IS NOT NULL THEN
            UPDATE seguridad.usuario_comision
            SET    id_evaluacion_oposicion = p_id_evaluacion_oposicion,
                   finalizo_calificacion   = FALSE,
                   puntaje_material        = NULL,
                   puntaje_exposicion      = NULL,
                   puntaje_respuestas      = NULL,
                   fecha_evaluacion        = NULL
            WHERE  id_comision_seleccion = v_id_comision
              AND  activo               = TRUE;
        END IF;

        -- Cambiar estado y registrar hora real
        SELECT id_tipo_estado_evaluacion INTO v_id_estado_nuevo
        FROM   postulacion.tipo_estado_evaluacion WHERE codigo = 'EN_CURSO';

        v_hora_inicio_now := LOCALTIME;
        v_ts_inicio := to_char(NOW() AT TIME ZONE 'UTC', 'YYYY-MM-DD"T"HH24:MI:SS"Z"');

        UPDATE postulacion.evaluacion_oposicion
        SET    id_tipo_estado_evaluacion = v_id_estado_nuevo,
               hora_inicio_real          = v_hora_inicio_now
        WHERE  id_evaluacion_oposicion   = p_id_evaluacion_oposicion;

        RETURN jsonb_build_object(
                'exito',           true,
                'mensaje',         'Evaluación iniciada. El tribunal puede comenzar a calificar.',
                'horaReal',        to_char(v_hora_inicio_now, 'HH24:MI:SS'),
                'serverTimestamp', v_ts_inicio
               );
    END IF;

    -- ── NO_PRESENTO ────────────────────────────────────────────────────
    IF p_accion = 'NO_PRESENTO' THEN
        IF v_codigo_actual != 'PROGRAMADA' THEN
            RETURN jsonb_build_object(
                    'exito',   false,
                    'mensaje', 'Solo se puede marcar como No Presentó desde estado PROGRAMADA.'
                   );
        END IF;

        SELECT id_tipo_estado_evaluacion INTO v_id_estado_nuevo
        FROM   postulacion.tipo_estado_evaluacion WHERE codigo = 'NO_PRESENTO';

        UPDATE postulacion.evaluacion_oposicion
        SET    id_tipo_estado_evaluacion = v_id_estado_nuevo
        WHERE  id_evaluacion_oposicion   = p_id_evaluacion_oposicion;

        RETURN jsonb_build_object('exito', true, 'mensaje', 'Postulante marcado como No Presentó.');
    END IF;

    -- ── FINALIZAR ──────────────────────────────────────────────────────
    IF p_accion = 'FINALIZAR' THEN
        IF v_codigo_actual != 'EN_CURSO' THEN
            RETURN jsonb_build_object(
                    'exito',   false,
                    'mensaje', 'Solo se puede finalizar una evaluación EN CURSO. Estado actual: ' || v_codigo_actual
                   );
        END IF;

        SELECT cs.id_comision_seleccion INTO v_id_comision
        FROM   postulacion.evaluacion_oposicion eo
                   JOIN   postulacion.postulacion          p  ON p.id_postulacion   = eo.id_postulacion
                   JOIN   postulacion.comision_seleccion   cs ON cs.id_convocatoria = p.id_convocatoria
        WHERE  eo.id_evaluacion_oposicion = p_id_evaluacion_oposicion
          AND  cs.activo = TRUE
        LIMIT 1;

        SELECT ROUND(
                       COALESCE(SUM(
                                        COALESCE(uc.puntaje_material,   0) +
                                        COALESCE(uc.puntaje_exposicion, 0) +
                                        COALESCE(uc.puntaje_respuestas, 0)
                                ), 0) / 3.0
                   , 2)
        INTO   v_puntaje_total
        FROM   seguridad.usuario_comision uc
        WHERE  uc.id_comision_seleccion    = v_id_comision
          AND  uc.id_evaluacion_oposicion  = p_id_evaluacion_oposicion
          AND  uc.activo                   = TRUE;

        UPDATE seguridad.usuario_comision
        SET    finalizo_calificacion = TRUE
        WHERE  id_comision_seleccion   = v_id_comision
          AND  id_evaluacion_oposicion = p_id_evaluacion_oposicion
          AND  activo = TRUE;

        SELECT id_tipo_estado_evaluacion INTO v_id_estado_nuevo
        FROM   postulacion.tipo_estado_evaluacion WHERE codigo = 'FINALIZADA';

        UPDATE postulacion.evaluacion_oposicion
        SET    puntaje_total_oposicion   = COALESCE(v_puntaje_total, 0),
               hora_fin_real             = LOCALTIME,
               id_tipo_estado_evaluacion = v_id_estado_nuevo
        WHERE  id_evaluacion_oposicion   = p_id_evaluacion_oposicion;

        RETURN jsonb_build_object(
                'exito',        true,
                'mensaje',      'Evaluación finalizada y bloqueada.',
                'puntajeFinal', COALESCE(v_puntaje_total, 0),
                'horaFin',      to_char(LOCALTIME, 'HH24:MI:SS')
               );
    END IF;

    RETURN jsonb_build_object('exito', false, 'mensaje', 'Acción no reconocida: ' || p_accion);

EXCEPTION WHEN OTHERS THEN
    RETURN jsonb_build_object('exito', false, 'mensaje', '[ERROR] ' || SQLERRM);
END;
$$;


--
-- Name: fn_cambiar_estado_postulacion_revision(integer, integer); Type: FUNCTION; Schema: postulacion; Owner: -
--

CREATE FUNCTION postulacion.fn_cambiar_estado_postulacion_revision(p_id_usuario integer, p_id_postulacion integer) RETURNS jsonb
    LANGUAGE plpgsql
    AS $$
DECLARE
    v_id_coordinador INTEGER;
    v_id_carrera INTEGER;
    v_postulacion_carrera INTEGER;
    v_estado_actual_codigo VARCHAR;
    v_id_estado_revision INTEGER;
BEGIN
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
$$;


--
-- Name: fn_consultar_comision_detalle(integer, text); Type: FUNCTION; Schema: postulacion; Owner: -
--

CREATE FUNCTION postulacion.fn_consultar_comision_detalle(p_id_usuario integer, p_rol text) RETURNS jsonb
    LANGUAGE plpgsql STABLE SECURITY DEFINER
    AS $$
DECLARE
    v_resultado JSONB;
BEGIN
    p_rol := UPPER(TRIM(COALESCE(p_rol, '')));

    -- ════════════════════════════════════════════════════════
    -- VISTA ESTUDIANTE
    -- ════════════════════════════════════════════════════════
    IF p_rol = 'ESTUDIANTE' THEN

        SELECT jsonb_build_object(
                       'rol',       'ESTUDIANTE',
                       'comisiones', COALESCE(jsonb_agg(bloque ORDER BY bloque->>'nombreComision'), '[]'::jsonb)
               )
        INTO v_resultado
        FROM (
                 SELECT jsonb_build_object(
                                'idComision',         cs.id_comision_seleccion,
                                'idConvocatoria',     c.id_convocatoria,
                                'nombreAsignatura',   a.nombre_asignatura,
                                'nombreComision',     cs.nombre_comision,
                                'fechaConformacion',  to_char(cs.fecha_conformacion, 'YYYY-MM-DD'),
                                'miembros', (
                                    SELECT COALESCE(jsonb_agg(jsonb_build_object(
                                                                      'idUsuario',  u2.id_usuario,
                                                                      'nombres',    u2.nombres,
                                                                      'apellidos',  u2.apellidos,
                                                                      'cargo',      uc.rol_integrante,
                                                                      'correo',     u2.correo
                                                              ) ORDER BY uc.rol_integrante), '[]'::jsonb)
                                    FROM  seguridad.usuario_comision uc
                                              JOIN  seguridad.usuario           u2 ON u2.id_usuario = uc.id_usuario
                                    WHERE uc.id_comision_seleccion = cs.id_comision_seleccion
                                )
                        ) AS bloque
                 FROM  academico.estudiante           e
                           JOIN  postulacion.postulacion         po ON po.id_estudiante   = e.id_estudiante
                           JOIN  postulacion.comision_seleccion  cs ON cs.id_convocatoria = po.id_convocatoria
                           JOIN  convocatoria.convocatoria        c  ON c.id_convocatoria  = cs.id_convocatoria
                           JOIN  academico.asignatura             a  ON a.id_asignatura   = c.id_asignatura
                 WHERE e.id_usuario = p_id_usuario
                   AND po.activo    = TRUE
                   AND cs.activo    = TRUE
             ) sub;

        IF v_resultado IS NULL OR v_resultado->>'comisiones' = '[]' THEN
            RETURN jsonb_build_object(
                    'exito',   false,
                    'mensaje', 'No se encontró comisión asignada para este estudiante.',
                    'rol',     'ESTUDIANTE'
                   );
        END IF;

        RETURN jsonb_build_object('exito', true) || v_resultado;
    END IF;

    -- ════════════════════════════════════════════════════════
    -- VISTA MIEMBRO DEL COMITÉ (DECANO / COORDINADOR / DOCENTE)
    -- ════════════════════════════════════════════════════════
    IF p_rol IN ('DECANO', 'COORDINADOR', 'DOCENTE') THEN

        SELECT jsonb_build_object(
                       'rol',           p_rol,
                       'convocatorias', COALESCE(jsonb_agg(bloque ORDER BY bloque->>'nombreAsignatura'), '[]'::jsonb)
               )
        INTO v_resultado
        FROM (
                 SELECT jsonb_build_object(
                                'idConvocatoria',   c.id_convocatoria,
                                'nombreAsignatura', a.nombre_asignatura,
                                'idComision',       cs.id_comision_seleccion,
                                'nombreComision',   cs.nombre_comision,
                                'fechaConformacion', to_char(cs.fecha_conformacion, 'YYYY-MM-DD'),
                            -- Fechas de la fase "Evaluación de Méritos y Oposición"
                                'faseEvaluacion', (
                                    SELECT jsonb_build_object(
                                                   'nombreFase', tf.nombre,
                                                   'codigoFase', tf.codigo,
                                                   'fechaInicio', to_char(pf.fecha_inicio, 'YYYY-MM-DD'),
                                                   'fechaFin',    to_char(pf.fecha_fin,    'YYYY-MM-DD')
                                           )
                                    FROM  planificacion.periodo_fase pf
                                              JOIN  planificacion.tipo_fase    tf ON tf.id_tipo_fase = pf.id_tipo_fase
                                    WHERE pf.id_periodo_academico = c.id_periodo_academico
                                      AND (tf.codigo = 'EVALUACION_MER_OPO' OR tf.nombre ILIKE '%meritos%' OR tf.nombre ILIKE '%oposici%')
                                    ORDER BY tf.orden ASC
                                    LIMIT 1
                                ),
                            -- Lista de postulantes de esa convocatoria
                                'postulantes', (
                                    SELECT COALESCE(jsonb_agg(jsonb_build_object(
                                                                      'idPostulacion',    po.id_postulacion,
                                                                      'nombres',          u2.nombres,
                                                                      'apellidos',        u2.apellidos,
                                                                      'correo',           u2.correo,
                                                                      'fechaPostulacion', to_char(po.fecha_postulacion, 'YYYY-MM-DD'),
                                                                      'estadoPostulacion', COALESCE(tep.nombre, 'PENDIENTE'),
                                                                      'codigoEstado',      COALESCE(tep.codigo, 'PENDIENTE')
                                                              ) ORDER BY u2.apellidos, u2.nombres), '[]'::jsonb)
                                    FROM  postulacion.postulacion              po
                                              JOIN  academico.estudiante                 est ON est.id_estudiante = po.id_estudiante
                                              JOIN  seguridad.usuario                    u2  ON u2.id_usuario     = est.id_usuario
                                              LEFT  JOIN postulacion.tipo_estado_postulacion tep
                                                         ON tep.id_tipo_estado_postulacion = po.id_tipo_estado_postulacion
                                    WHERE po.id_convocatoria = c.id_convocatoria
                                      AND po.activo = TRUE
                                )
                        ) AS bloque
                 FROM  seguridad.usuario_comision     uc
                           JOIN  postulacion.comision_seleccion cs ON cs.id_comision_seleccion = uc.id_comision_seleccion
                           JOIN  convocatoria.convocatoria       c  ON c.id_convocatoria        = cs.id_convocatoria
                           JOIN  academico.asignatura            a  ON a.id_asignatura          = c.id_asignatura
                 WHERE uc.id_usuario     = p_id_usuario
                   AND uc.rol_integrante = p_rol
                   AND cs.activo         = TRUE
             ) sub;

        IF v_resultado IS NULL OR v_resultado->>'convocatorias' = '[]' THEN
            RETURN jsonb_build_object(
                    'exito',   false,
                    'mensaje', 'No se encontraron comisiones asignadas para este usuario con rol ' || p_rol || '.',
                    'rol',     p_rol
                   );
        END IF;

        RETURN jsonb_build_object('exito', true) || v_resultado;
    END IF;

    -- Rol no reconocido
    RETURN jsonb_build_object(
            'exito',   false,
            'mensaje', 'Rol no reconocido. Valores aceptados: ESTUDIANTE, DECANO, COORDINADOR, DOCENTE.',
            'rol',     p_rol
           );

EXCEPTION WHEN OTHERS THEN
    RETURN jsonb_build_object(
            'exito',   false,
            'mensaje', '[ERROR] ' || SQLERRM
           );
END;
$$;


--
-- Name: fn_consultar_cronograma_oposicion(integer); Type: FUNCTION; Schema: postulacion; Owner: -
--

CREATE FUNCTION postulacion.fn_consultar_cronograma_oposicion(p_id_convocatoria integer) RETURNS jsonb
    LANGUAGE plpgsql STABLE SECURITY DEFINER
    AS $$
BEGIN
    RETURN jsonb_build_object(
            'exito', true,
            'cronograma', COALESCE(
                    (SELECT jsonb_agg(
                                    jsonb_build_object(
                                            'idEvaluacionOposicion', eo.id_evaluacion_oposicion,
                                            'orden',                 eo.orden_exposicion,
                                            'nombres',               u.nombres,
                                            'apellidos',             u.apellidos,
                                            'correo',                u.correo,
                                            'tema',                  eo.tema_exposicion,
                                            'fecha',                 to_char(eo.fecha_evaluacion, 'YYYY-MM-DD'),
                                            'horaInicio',            to_char(eo.hora_inicio,      'HH24:MI'),
                                            'horaFin',               to_char(eo.hora_fin,         'HH24:MI'),
                                            'horaInicioReal',        to_char(eo.hora_inicio_real, 'HH24:MI:SS'),
                                            'horaFinReal',           to_char(eo.hora_fin_real,    'HH24:MI'),
                                            'lugar',                 eo.lugar,
                                            'estado',                tee.codigo,
                                            'nombreEstado',          tee.nombre,
                                            'puntajeFinal',          eo.puntaje_total_oposicion,
                                        -- ← nuevo: ISO-8601 UTC del momento en que empezó este turno
                                            'serverTimestamp',       CASE WHEN tee.codigo = 'EN_CURSO'
                                                                              THEN to_char(
                                                (eo.fecha_evaluacion + eo.hora_inicio_real) AT TIME ZONE 'America/Guayaquil' AT TIME ZONE 'UTC',
                                                'YYYY-MM-DD"T"HH24:MI:SS"Z"'
                                                                                   )
                                                                          ELSE NULL END,
                                            'jurados', (
                                                SELECT COALESCE(jsonb_agg(jsonb_build_object(
                                                                                  'idUsuario',           uj.id_usuario,
                                                                                  'nombres',             uj2.nombres,
                                                                                  'apellidos',           uj2.apellidos,
                                                                                  'rol',                 uj.rol_integrante,
                                                                                  'puntajeMaterial',     uj.puntaje_material,
                                                                                  'puntajeExposicion',   uj.puntaje_exposicion,
                                                                                  'puntajeRespuestas',   uj.puntaje_respuestas,
                                                                                  'subtotal',            COALESCE(uj.puntaje_material, 0)
                                                                                      + COALESCE(uj.puntaje_exposicion, 0)
                                                                                      + COALESCE(uj.puntaje_respuestas, 0),
                                                                                  'finalizo',            uj.finalizo_calificacion
                                                                          ) ORDER BY uj.rol_integrante), '[]'::jsonb)
                                                FROM   seguridad.usuario_comision uj
                                                           JOIN   seguridad.usuario          uj2 ON uj2.id_usuario = uj.id_usuario
                                                WHERE  uj.id_evaluacion_oposicion = eo.id_evaluacion_oposicion
                                                   OR (uj.id_evaluacion_oposicion IS NULL
                                                    AND uj.id_comision_seleccion = (
                                                        SELECT cs.id_comision_seleccion
                                                        FROM   postulacion.comision_seleccion cs
                                                        WHERE  cs.id_convocatoria = p_id_convocatoria
                                                          AND  cs.activo = TRUE
                                                        LIMIT 1
                                                    ))
                                            )
                                    ) ORDER BY eo.orden_exposicion
                            )
                     FROM  postulacion.evaluacion_oposicion   eo
                               JOIN  postulacion.postulacion              p   ON p.id_postulacion   = eo.id_postulacion
                               JOIN  academico.estudiante                 est ON est.id_estudiante  = p.id_estudiante
                               JOIN  seguridad.usuario                    u   ON u.id_usuario       = est.id_usuario
                               JOIN  postulacion.tipo_estado_evaluacion   tee ON tee.id_tipo_estado_evaluacion = eo.id_tipo_estado_evaluacion
                     WHERE p.id_convocatoria = p_id_convocatoria
                    ),
                    '[]'::jsonb
                          )
           );

EXCEPTION WHEN OTHERS THEN
    RETURN jsonb_build_object('exito', false, 'mensaje', '[ERROR] ' || SQLERRM);
END;
$$;


--
-- Name: fn_consultar_mi_turno(integer, integer); Type: FUNCTION; Schema: postulacion; Owner: -
--

CREATE FUNCTION postulacion.fn_consultar_mi_turno(p_id_usuario integer, p_id_convocatoria integer) RETURNS jsonb
    LANGUAGE plpgsql STABLE SECURITY DEFINER
    AS $$
DECLARE
    v_resultado JSONB;
BEGIN
    -- La cadena de joins: usuario → estudiante → postulacion → evaluacion_oposicion
    -- Esto garantiza que el estudiante solo puede ver SU turno.
    -- Si el usuario no pertenece a la convocatoria o no tiene turno, devuelve exito=false.
    SELECT jsonb_build_object(
                   'exito',                  true,
                   'idEvaluacionOposicion',  eo.id_evaluacion_oposicion,
                   'orden',                  eo.orden_exposicion,
                   'tema',                   eo.tema_exposicion,
                   'fecha',                  to_char(eo.fecha_evaluacion, 'YYYY-MM-DD'),
                   'horaInicio',             to_char(eo.hora_inicio,      'HH24:MI'),
                   'horaFin',                to_char(eo.hora_fin,         'HH24:MI'),
                   'horaInicioReal',         to_char(eo.hora_inicio_real, 'HH24:MI'),
                   'lugar',                  eo.lugar,
                   'estado',                 tee.codigo,
                   'nombreEstado',           tee.nombre,
                   'puntajeFinal',           eo.puntaje_total_oposicion,
               -- Incluimos los jurados solo si la evaluación está FINALIZADA
               -- para proteger la privacidad del proceso en curso.
                   'jurados', CASE
                                  WHEN tee.codigo = 'FINALIZADA' THEN (
                                      SELECT COALESCE(jsonb_agg(jsonb_build_object(
                                                                        'rol',              uc.rol_integrante,
                                                                        'puntajeMaterial',  uc.puntaje_material,
                                                                        'puntajeExposicion',uc.puntaje_exposicion,
                                                                        'puntajeRespuestas',uc.puntaje_respuestas,
                                                                        'subtotal',         COALESCE(uc.puntaje_material,0)
                                                                            + COALESCE(uc.puntaje_exposicion,0)
                                                                            + COALESCE(uc.puntaje_respuestas,0)
                                                                ) ORDER BY uc.rol_integrante), '[]'::jsonb)
                                      FROM seguridad.usuario_comision uc
                                      WHERE uc.id_evaluacion_oposicion = eo.id_evaluacion_oposicion
                                        AND uc.activo = TRUE
                                  )
                                  ELSE '[]'::jsonb  -- No mostramos puntajes parciales al estudiante
                       END
           )
    INTO v_resultado
    FROM  academico.estudiante          est
              JOIN  postulacion.postulacion       po  ON po.id_estudiante  = est.id_estudiante
              JOIN  postulacion.evaluacion_oposicion eo ON eo.id_postulacion = po.id_postulacion
              JOIN  postulacion.tipo_estado_evaluacion tee
                    ON tee.id_tipo_estado_evaluacion = eo.id_tipo_estado_evaluacion
    WHERE est.id_usuario       = p_id_usuario
      AND po.id_convocatoria   = p_id_convocatoria
      AND po.activo            = TRUE
    LIMIT 1;  -- Un estudiante solo puede tener un turno por convocatoria

    IF v_resultado IS NULL THEN
        RETURN jsonb_build_object(
                'exito',   false,
                'mensaje', 'No se encontró un turno asignado para este estudiante en la convocatoria indicada.'
               );
    END IF;

    RETURN v_resultado;

EXCEPTION WHEN OTHERS THEN
    RETURN jsonb_build_object('exito', false, 'mensaje', '[ERROR] ' || SQLERRM);
END;
$$;


--
-- Name: fn_crear_tipo_estado_postulacion(character varying, character varying, character varying); Type: FUNCTION; Schema: postulacion; Owner: -
--

CREATE FUNCTION postulacion.fn_crear_tipo_estado_postulacion(p_codigo character varying, p_nombre character varying, p_descripcion character varying) RETURNS jsonb
    LANGUAGE plpgsql SECURITY DEFINER
    AS $$
DECLARE
    v_id INTEGER;
BEGIN
    -- Validar que el código no exista (case-insensitive)
    IF EXISTS (
        SELECT 1 FROM postulacion.tipo_estado_postulacion
        WHERE LOWER(TRIM(codigo)) = LOWER(TRIM(p_codigo))
    ) THEN
        RETURN jsonb_build_object('exito', false, 'mensaje', 'Ya existe un estado de postulación con el código especificado');
    END IF;

    -- Validar que el nombre no exista (case-insensitive)
    IF EXISTS (
        SELECT 1 FROM postulacion.tipo_estado_postulacion
        WHERE LOWER(TRIM(nombre)) = LOWER(TRIM(p_nombre))
    ) THEN
        RETURN jsonb_build_object('exito', false, 'mensaje', 'Ya existe un estado de postulación con el nombre especificado');
    END IF;

    -- Insertar el nuevo registro
    INSERT INTO postulacion.tipo_estado_postulacion (codigo, nombre, descripcion, activo, fecha_creacion)
    VALUES (UPPER(TRIM(p_codigo)), TRIM(p_nombre), TRIM(p_descripcion), TRUE, NOW())
    RETURNING id_tipo_estado_postulacion INTO v_id;

    RETURN jsonb_build_object(
        'exito', true,
        'mensaje', 'Estado de postulación creado exitosamente',
        'id', v_id
    );

EXCEPTION
    WHEN unique_violation THEN
        RETURN jsonb_build_object('exito', false, 'mensaje', 'Error de duplicidad: el código ya existe');
    WHEN OTHERS THEN
        RETURN jsonb_build_object('exito', false, 'mensaje', 'Error al crear estado de postulación: ' || SQLERRM);
END;
$$;


--
-- Name: fn_dictaminar_postulacion(integer, integer, character varying, text); Type: FUNCTION; Schema: postulacion; Owner: -
--

CREATE FUNCTION postulacion.fn_dictaminar_postulacion(p_id_usuario integer, p_id_postulacion integer, p_accion character varying, p_observacion text DEFAULT NULL::text) RETURNS jsonb
    LANGUAGE plpgsql
    AS $$
DECLARE
    v_id_coordinador INTEGER;
    v_id_carrera INTEGER;
    v_postulacion_carrera INTEGER;
    v_id_estudiante INTEGER;
    v_id_usuario_estudiante INTEGER;
    v_todos_validados BOOLEAN;
    v_id_nuevo_estado INTEGER;
    v_estado_codigo VARCHAR;
    v_estado_actual_codigo VARCHAR;
BEGIN
    -- 1. Validar coordinador activo
    SELECT c.id_coordinador, c.id_carrera
    INTO v_id_coordinador, v_id_carrera
    FROM academico.coordinador c
    WHERE c.id_usuario = p_id_usuario AND c.activo = TRUE;

    IF v_id_coordinador IS NULL THEN
        RETURN jsonb_build_object('exito', FALSE, 'mensaje', 'Acceso denegado: El usuario no es un coordinador activo');
    END IF;

    -- 2. Obtener información de postulación Y ESTADO ACTUAL
    SELECT car.id_carrera, p.id_estudiante, e.id_usuario, tep.codigo
    INTO v_postulacion_carrera, v_id_estudiante, v_id_usuario_estudiante, v_estado_actual_codigo
    FROM postulacion.postulacion p
    JOIN convocatoria.convocatoria cv ON p.id_convocatoria = cv.id_convocatoria
    JOIN academico.asignatura a ON cv.id_asignatura = a.id_asignatura
    JOIN academico.carrera car ON a.id_carrera = car.id_carrera
    JOIN academico.estudiante e ON p.id_estudiante = e.id_estudiante
    LEFT JOIN postulacion.tipo_estado_postulacion tep ON p.id_tipo_estado_postulacion = tep.id_tipo_estado_postulacion
    WHERE p.id_postulacion = p_id_postulacion;

    IF v_postulacion_carrera IS NULL THEN
        RETURN jsonb_build_object('exito', FALSE, 'mensaje', 'La postulación no existe');
    END IF;

    IF v_postulacion_carrera != v_id_carrera THEN
        RETURN jsonb_build_object('exito', FALSE, 'mensaje', 'No tiene permisos para dictaminar esta postulación');
    END IF;

    -- 3. NUEVA VALIDACIÓN: Verificar que la postulación no esté ya finalizada
    IF UPPER(COALESCE(v_estado_actual_codigo, '')) IN ('APROBADA', 'RECHAZADA') THEN
        RETURN jsonb_build_object(
            'exito', FALSE,
            'mensaje', 'Esta postulación ya fue ' || v_estado_actual_codigo || ' y no se pueden realizar más acciones.',
            'estado_actual', v_estado_actual_codigo
        );
    END IF;

    -- 4. Lógica según la acción
    CASE UPPER(p_accion)
        WHEN 'APROBAR' THEN
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
$$;


--
-- Name: fn_ejecutar_sorteo_oposicion(integer, date, time without time zone, text); Type: FUNCTION; Schema: postulacion; Owner: -
--

CREATE FUNCTION postulacion.fn_ejecutar_sorteo_oposicion(p_id_convocatoria integer, p_fecha date, p_hora_inicio time without time zone, p_lugar text) RETURNS jsonb
    LANGUAGE plpgsql SECURITY DEFINER
    AS $$
DECLARE
    v_total_aptos   INTEGER;
    v_total_temas   INTEGER;
    v_id_estado_prog INTEGER;
    v_minutos_bloque INTEGER := 35;  -- 20 expo + 10 preguntas + 5 transición
    v_contador      INTEGER := 0;
BEGIN
    -- ── Validar convocatoria ──────────────────────────────────
    IF NOT EXISTS (
        SELECT 1 FROM convocatoria.convocatoria
        WHERE id_convocatoria = p_id_convocatoria AND activo = TRUE
    ) THEN
        RETURN jsonb_build_object('exito', false, 'mensaje', 'Convocatoria no existe o inactiva.');
    END IF;

    -- ── Contar postulantes APTOS ──────────────────────────────
    SELECT COUNT(*) INTO v_total_aptos
    FROM  postulacion.postulacion              po
              JOIN  postulacion.tipo_estado_postulacion  tep
                    ON tep.id_tipo_estado_postulacion = po.id_tipo_estado_postulacion
    WHERE po.id_convocatoria = p_id_convocatoria
      AND po.activo = TRUE
      AND tep.codigo = 'APROBADA';

    IF v_total_aptos = 0 THEN
        RETURN jsonb_build_object(
                'exito', false,
                'mensaje', 'No hay postulantes con estado APROBADA en esta convocatoria.'
               );
    END IF;

    -- ── Contar temas disponibles ──────────────────────────────
    SELECT COUNT(*) INTO v_total_temas
    FROM postulacion.banco_temas
    WHERE id_convocatoria = p_id_convocatoria AND activo = TRUE;

    -- Regla crítica: N >= P (temas >= postulantes)
    IF v_total_temas < v_total_aptos THEN
        RETURN jsonb_build_object(
                'exito',   false,
                'mensaje', 'Faltan temas: hay ' || v_total_aptos || ' postulante(s) aptos pero solo '
                               || v_total_temas || ' tema(s). Se necesitan al menos ' || v_total_aptos || '.',
                'totalAptos', v_total_aptos,
                'totalTemas', v_total_temas
               );
    END IF;

    IF EXISTS (
        SELECT 1
        FROM  postulacion.evaluacion_oposicion eo
                  JOIN  postulacion.tipo_estado_evaluacion tee
                        ON tee.id_tipo_estado_evaluacion = eo.id_tipo_estado_evaluacion
                  JOIN  postulacion.postulacion p ON p.id_postulacion = eo.id_postulacion
        WHERE p.id_convocatoria = p_id_convocatoria
          AND tee.codigo NOT IN ('PROGRAMADA')
    ) THEN
        RETURN jsonb_build_object(
                'exito', false,
                'mensaje', 'No se puede re-sortear: existen evaluaciones en curso o finalizadas.'
               );
    END IF;

    -- ── Obtener id del estado PROGRAMADA ─────────────────────
    SELECT id_tipo_estado_evaluacion INTO v_id_estado_prog
    FROM postulacion.tipo_estado_evaluacion
    WHERE codigo = 'PROGRAMADA';

    -- ── Idempotencia: eliminar sorteo previo (solo PROGRAMADA) ─
    DELETE FROM postulacion.evaluacion_oposicion
    WHERE id_postulacion IN (
        SELECT id_postulacion FROM postulacion.postulacion
        WHERE id_convocatoria = p_id_convocatoria
    )
      AND id_tipo_estado_evaluacion = v_id_estado_prog;

    -- ── Doble shuffle + emparejamiento + cálculo de horarios ──
    --
    -- La lógica es:
    --   postulantes_shuffle: postulantes APTOS en orden random, numerados 1..P
    --   temas_shuffle:       temas activos en orden random, numerados 1..N
    --   Se emparejan por rn (1 con 1, 2 con 2, etc.)
    --   hora_inicio del turno = p_hora_inicio + (rn - 1) * 35 minutos
    --   hora_fin del turno    = hora_inicio del turno + 30 minutos (5 min de transición
    --                           no se cuenta como tiempo "del postulante")
    --
    INSERT INTO postulacion.evaluacion_oposicion (
        id_postulacion,
        tema_exposicion,
        fecha_evaluacion,
        hora_inicio,
        hora_fin,
        lugar,
        orden_exposicion,
        id_tipo_estado_evaluacion
    )
    SELECT
        ps.id_postulacion,
        ts.descripcion_tema,
        p_fecha,
        -- hora_inicio del turno
        (p_hora_inicio + ((ps.rn - 1) * v_minutos_bloque || ' minutes')::interval)::time,
        -- hora_fin del turno (20 expo + 10 preguntas = 30 min de evaluación activa)
        (p_hora_inicio + ((ps.rn - 1) * v_minutos_bloque || ' minutes')::interval
            + '30 minutes'::interval)::time,
        p_lugar,
        ps.rn,                   -- orden_exposicion = número de turno
        v_id_estado_prog
    FROM (
             -- Postulantes APTOS en orden aleatorio, numerados desde 1
             SELECT
                 po.id_postulacion,
                 ROW_NUMBER() OVER (ORDER BY random()) AS rn
             FROM  postulacion.postulacion             po
                       JOIN  postulacion.tipo_estado_postulacion tep
                             ON tep.id_tipo_estado_postulacion = po.id_tipo_estado_postulacion
             WHERE po.id_convocatoria = p_id_convocatoria
               AND po.activo = TRUE
               AND tep.codigo = 'APROBADA'
         ) ps
             JOIN (
        -- Temas en orden aleatorio, numerados desde 1 (solo se usan los primeros P)
        SELECT
            bt.descripcion_tema,
            ROW_NUMBER() OVER (ORDER BY random()) AS rn
        FROM postulacion.banco_temas bt
        WHERE bt.id_convocatoria = p_id_convocatoria
          AND bt.activo = TRUE
    ) ts ON ts.rn = ps.rn;      -- emparejamiento por posición

    GET DIAGNOSTICS v_contador = ROW_COUNT;

    RETURN jsonb_build_object(
            'exito',      true,
            'mensaje',    'Sorteo ejecutado. ' || v_contador || ' turno(s) programado(s).',
            'turnos',     v_contador,
            'fecha',      to_char(p_fecha, 'YYYY-MM-DD'),
            'horaInicio', to_char(p_hora_inicio, 'HH24:MI'),
            'lugar',      p_lugar,
        -- Retornar el cronograma completo para que el frontend lo muestre
            'cronograma', (
                SELECT jsonb_agg(jsonb_build_object(
                                         'orden',         eo.orden_exposicion,
                                         'idPostulacion', eo.id_postulacion,
                                         'nombres',       u.nombres,
                                         'apellidos',     u.apellidos,
                                         'tema',          eo.tema_exposicion,
                                         'horaInicio',    to_char(eo.hora_inicio, 'HH24:MI'),
                                         'horaFin',       to_char(eo.hora_fin, 'HH24:MI')
                                 ) ORDER BY eo.orden_exposicion)
                FROM  postulacion.evaluacion_oposicion eo
                          JOIN  postulacion.postulacion          p  ON p.id_postulacion  = eo.id_postulacion
                          JOIN  academico.estudiante             est ON est.id_estudiante = p.id_estudiante
                          JOIN  seguridad.usuario                u   ON u.id_usuario      = est.id_usuario
                WHERE p.id_convocatoria = p_id_convocatoria
            )
           );

EXCEPTION WHEN OTHERS THEN
    RETURN jsonb_build_object('exito', false, 'mensaje', '[ERROR] ' || SQLERRM);
END;
$$;


--
-- Name: fn_eliminar_tipo_estado_postulacion(integer); Type: FUNCTION; Schema: postulacion; Owner: -
--

CREATE FUNCTION postulacion.fn_eliminar_tipo_estado_postulacion(p_id integer) RETURNS jsonb
    LANGUAGE plpgsql SECURITY DEFINER
    AS $$
BEGIN
    -- Validar que el registro exista
    IF NOT EXISTS (
        SELECT 1 FROM postulacion.tipo_estado_postulacion
        WHERE id_tipo_estado_postulacion = p_id
    ) THEN
        RETURN jsonb_build_object('exito', false, 'mensaje', 'No se encontró el estado de postulación con el ID especificado');
    END IF;

    -- Eliminación lógica: setear activo = false
    UPDATE postulacion.tipo_estado_postulacion
    SET activo = FALSE
    WHERE id_tipo_estado_postulacion = p_id;

    RETURN jsonb_build_object(
        'exito', true,
        'mensaje', 'Estado de postulación desactivado exitosamente',
        'id', p_id
    );

EXCEPTION
    WHEN OTHERS THEN
        RETURN jsonb_build_object('exito', false, 'mensaje', 'Error al desactivar estado de postulación: ' || SQLERRM);
END;
$$;


--
-- Name: fn_evaluar_documento_individual(integer, integer, character varying, text); Type: FUNCTION; Schema: postulacion; Owner: -
--

CREATE FUNCTION postulacion.fn_evaluar_documento_individual(p_id_usuario integer, p_id_requisito_adjunto integer, p_accion character varying, p_observacion text DEFAULT NULL::text) RETURNS jsonb
    LANGUAGE plpgsql
    AS $$
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
$$;


--
-- Name: fn_finalizar_proceso_seleccion(integer); Type: FUNCTION; Schema: postulacion; Owner: -
--

CREATE FUNCTION postulacion.fn_finalizar_proceso_seleccion(p_id_convocatoria integer) RETURNS jsonb
    LANGUAGE plpgsql SECURITY DEFINER
    AS $$
DECLARE
    v_cupos            INTEGER;
    v_posicion         INTEGER := 0;
    v_id_sel           INTEGER;
    v_id_ele           INTEGER;
    v_id_no_sel        INTEGER;
    v_id_est_activa    INTEGER;
    v_horas_semanales  NUMERIC(5,2) := 20.00;   -- horas semanales por defecto
    v_horas_maximas    NUMERIC(5,2);
    v_periodo_academico    INTEGER;
    v_seleccionados    INTEGER := 0;
    v_semanas          INTEGER :=16;
    v_elegibles        INTEGER := 0;
    v_no_sel           INTEGER := 0;
    v_rec              RECORD;
BEGIN

    -- ── Validar que la convocatoria existe y sigue activa ─────────────
    SELECT cupos_disponibles, id_periodo_academico
    INTO   v_cupos, v_periodo_academico
    FROM   convocatoria.convocatoria
    WHERE  id_convocatoria = p_id_convocatoria
      AND  activo = TRUE;

    IF NOT FOUND THEN
        RAISE EXCEPTION 'VALIDACION: La convocatoria % no existe o ya fue cerrada.', p_id_convocatoria;
    END IF;

    -- ── Recuperar IDs de estados de postulación ────────────────────────
    SELECT id_tipo_estado_postulacion INTO v_id_sel
    FROM   postulacion.tipo_estado_postulacion WHERE codigo = 'SELECCIONADO' LIMIT 1;

    SELECT id_tipo_estado_postulacion INTO v_id_ele
    FROM   postulacion.tipo_estado_postulacion WHERE codigo = 'ELEGIBLE' LIMIT 1;

    SELECT id_tipo_estado_postulacion INTO v_id_no_sel
    FROM   postulacion.tipo_estado_postulacion WHERE codigo = 'NO_SELECCIONADO' LIMIT 1;

    IF v_id_sel IS NULL OR v_id_ele IS NULL OR v_id_no_sel IS NULL THEN
        RAISE EXCEPTION
            'VALIDACION: Los estados SELECCIONADO / ELEGIBLE / NO_SELECCIONADO no están configurados en tipo_estado_postulacion.';
    END IF;

    -- ── Recuperar ID del estado ACTIVA de ayudantía ────────────────────
    SELECT id_tipo_estado_ayudantia INTO v_id_est_activa
    FROM   ayudantia.tipo_estado_ayudantia WHERE codigo = 'ACTIVO' LIMIT 1;

    IF v_id_est_activa IS NULL THEN
        RAISE EXCEPTION
            'VALIDACION: El estado ACTIVA no está configurado en tipo_estado_ayudantia.';
    END IF;


    SELECT
        COALESCE((pf.fecha_fin - pf.fecha_inicio) / 7, 16)
    INTO v_semanas
    FROM planificacion.periodo_fase pf
             JOIN planificacion.tipo_fase tf ON tf.id_tipo_fase = pf.id_tipo_fase
    WHERE pf.id_periodo_academico = v_periodo_academico
      AND tf.codigo = 'EJECUCION_ACTIVIDADES';

    v_horas_maximas := v_horas_semanales * v_semanas;

    FOR v_rec IN
        SELECT
            po.id_postulacion,
            u.nombre_usuario,
            u.nombres || ' ' || u.apellidos            AS nombre_completo,
            ROUND(
                    COALESCE(em.nota_total_meritos,          0) +
                    COALESCE(eo.puntaje_total_oposicion,     0)
                , 2)::NUMERIC(5,2)                         AS puntaje_total
        FROM   postulacion.postulacion               po
                   JOIN   academico.estudiante                  est ON est.id_estudiante = po.id_estudiante
                   JOIN   seguridad.usuario                     u   ON u.id_usuario      = est.id_usuario
            -- Evaluación de méritos más reciente (cualquier estado)
                   LEFT JOIN LATERAL (
            SELECT nota_total_meritos
            FROM   postulacion.evaluacion_meritos
            WHERE  id_postulacion = po.id_postulacion
            ORDER  BY id_evaluacion_meritos DESC
            LIMIT  1
            ) em ON TRUE
                   LEFT JOIN LATERAL (
            SELECT eo2.puntaje_total_oposicion
            FROM   postulacion.evaluacion_oposicion  eo2
                       JOIN   postulacion.tipo_estado_evaluacion tee
                              ON tee.id_tipo_estado_evaluacion = eo2.id_tipo_estado_evaluacion
            WHERE  eo2.id_postulacion = po.id_postulacion
              AND  tee.codigo         = 'FINALIZADA'
            ORDER  BY eo2.id_evaluacion_oposicion DESC
            LIMIT  1
            ) eo ON TRUE
        WHERE  po.id_convocatoria = p_id_convocatoria
          AND  po.activo          = TRUE
        ORDER  BY puntaje_total DESC,
                  nombre_completo ASC
        LOOP
            v_posicion := v_posicion + 1;

            IF v_rec.puntaje_total < 25.00 THEN
                UPDATE postulacion.postulacion
                SET    id_tipo_estado_postulacion = v_id_no_sel
                WHERE  id_postulacion             = v_rec.id_postulacion;
                v_no_sel := v_no_sel + 1;

            ELSIF v_posicion <= v_cupos THEN
                UPDATE postulacion.postulacion
                SET    id_tipo_estado_postulacion = v_id_sel
                WHERE  id_postulacion             = v_rec.id_postulacion;

                CALL seguridad.sp_promover_estudiante_a_ayudante(
                        v_rec.nombre_usuario,
                        v_horas_semanales
                     );

                IF NOT EXISTS (
                    SELECT 1 FROM ayudantia.ayudantia
                    WHERE id_postulacion = v_rec.id_postulacion
                ) THEN
                    INSERT INTO ayudantia.ayudantia (
                        id_tipo_estado_ayudantia,
                        id_postulacion,
                        fecha_inicio,
                        horas_semanales_max,
                        horas_maximas
                    ) VALUES (
                                 v_id_est_activa,
                                 v_rec.id_postulacion,
                                 CURRENT_DATE,
                                 v_horas_semanales,
                                 v_horas_maximas
                             );
                END IF;

                v_seleccionados := v_seleccionados + 1;

            ELSE
                UPDATE postulacion.postulacion
                SET    id_tipo_estado_postulacion = v_id_ele
                WHERE  id_postulacion             = v_rec.id_postulacion;
                v_elegibles := v_elegibles + 1;
            END IF;

        END LOOP;

    UPDATE convocatoria.convocatoria
    SET    activo = FALSE,
           estado = 'RESUELTA'
    WHERE  id_convocatoria = p_id_convocatoria;

    RAISE NOTICE '[fn_finalizar_proceso_seleccion] conv=% → sel=%, ele=%, noSel=%',
        p_id_convocatoria, v_seleccionados, v_elegibles, v_no_sel;

    RETURN jsonb_build_object(
            'exito',           TRUE,
            'seleccionados',   v_seleccionados,
            'elegibles',       v_elegibles,
            'noSeleccionados', v_no_sel,
            'mensaje',
            format(
                    'Proceso finalizado. %s seleccionado(s), %s elegible(s), %s no seleccionado(s). Convocatoria cerrada.',
                    v_seleccionados, v_elegibles, v_no_sel
            )
           );

EXCEPTION
    WHEN OTHERS THEN
        IF SQLERRM LIKE 'VALIDACION:%' OR SQLERRM LIKE 'ACCESO:%' THEN RAISE; END IF;
        RAISE EXCEPTION 'Error en fn_finalizar_proceso_seleccion(conv=%): % (SQLSTATE: %)',
            p_id_convocatoria, SQLERRM, SQLSTATE;
END;
$$;


--
-- Name: FUNCTION fn_finalizar_proceso_seleccion(p_id_convocatoria integer); Type: COMMENT; Schema: postulacion; Owner: -
--

COMMENT ON FUNCTION postulacion.fn_finalizar_proceso_seleccion(p_id_convocatoria integer) IS 'Cierre maestro de la fase de selección. Asigna SELECCIONADO / ELEGIBLE / NO_SELECCIONADO, promueve ganadores a AYUDANTE_CATEDRA, crea su ayudantia y marca la convocatoria como RESUELTA.';


--
-- Name: fn_generar_comisiones_automaticas(); Type: FUNCTION; Schema: postulacion; Owner: -
--

CREATE FUNCTION postulacion.fn_generar_comisiones_automaticas() RETURNS jsonb
    LANGUAGE plpgsql SECURITY DEFINER
    AS $$
DECLARE
    v_conv              RECORD;
    v_id_comision       INTEGER;
    v_id_usr_decano     INTEGER;
    v_id_usr_coord      INTEGER;
    v_id_usr_docente    INTEGER;
    v_contador          INTEGER := 0;
    v_omitidas          INTEGER := 0;
    v_nombre_comision   TEXT;
BEGIN
    FOR v_conv IN
        SELECT
            c.id_convocatoria,
            c.id_periodo_academico,
            a.nombre_asignatura,
            a.id_carrera,
            ca.id_facultad,
            d.id_usuario AS id_usuario_docente
        FROM  convocatoria.convocatoria   c
                  JOIN  academico.asignatura        a  ON a.id_asignatura = c.id_asignatura
                  JOIN  academico.carrera           ca ON ca.id_carrera   = a.id_carrera
                  JOIN  academico.docente           d  ON d.id_docente    = c.id_docente
        WHERE c.activo = TRUE

          AND EXISTS (
            SELECT 1 FROM postulacion.postulacion p
            WHERE  p.id_convocatoria = c.id_convocatoria
              AND  p.activo = TRUE
        )
          AND NOT EXISTS (
            SELECT 1 FROM postulacion.comision_seleccion cs
            WHERE  cs.id_convocatoria = c.id_convocatoria
              AND  cs.activo = TRUE
        )
          AND EXISTS (
            SELECT 1
            FROM  postulacion.postulacion          p
                      JOIN  postulacion.tipo_estado_postulacion tep
                            ON tep.id_tipo_estado_postulacion = p.id_tipo_estado_postulacion
            WHERE p.id_convocatoria = c.id_convocatoria
              AND p.activo          = TRUE
              AND tep.codigo        = 'APROBADA'
        )
        ORDER BY c.id_convocatoria
        LOOP

            SELECT de.id_usuario INTO v_id_usr_decano
            FROM   academico.decano de
            WHERE  de.id_facultad = v_conv.id_facultad
              AND  de.activo = TRUE
            ORDER  BY de.id_decano DESC
            LIMIT  1;

            IF v_id_usr_decano IS NULL THEN
                RAISE WARNING '[COMISION] Sin decano activo — facultad=%, convocatoria=%',
                    v_conv.id_facultad, v_conv.id_convocatoria;
                v_omitidas := v_omitidas + 1;
                CONTINUE;
            END IF;

            SELECT co.id_usuario INTO v_id_usr_coord
            FROM   academico.coordinador co
            WHERE  co.id_carrera = v_conv.id_carrera
              AND  co.activo = TRUE
            ORDER  BY co.id_coordinador DESC
            LIMIT  1;

            IF v_id_usr_coord IS NULL THEN
                RAISE WARNING '[COMISION] Sin coordinador activo — carrera=%, convocatoria=%',
                    v_conv.id_carrera, v_conv.id_convocatoria;
                v_omitidas := v_omitidas + 1;
                CONTINUE;
            END IF;

            v_id_usr_docente := v_conv.id_usuario_docente;

            v_nombre_comision :=
                    'Comisión · ' || v_conv.nombre_asignatura
                        || ' · ' || to_char(CURRENT_DATE, 'DD/MM/YYYY');


            INSERT INTO postulacion.comision_seleccion (
                id_convocatoria, nombre_comision, fecha_conformacion, activo
            ) VALUES (
                         v_conv.id_convocatoria,
                         v_nombre_comision,
                         CURRENT_DATE,
                         TRUE
                     )
            RETURNING id_comision_seleccion INTO v_id_comision;

            INSERT INTO seguridad.usuario_comision
            (id_comision_seleccion, id_usuario, rol_integrante, activo)
            VALUES
                (v_id_comision, v_id_usr_decano,  'DECANO',      TRUE),
                (v_id_comision, v_id_usr_coord,   'COORDINADOR', TRUE),
                (v_id_comision, v_id_usr_docente, 'DOCENTE',     TRUE);

            v_contador := v_contador + 1;
        END LOOP;

    RETURN jsonb_build_object(
            'exito',             true,
            'mensaje',           'Proceso completado. Creadas: ' || v_contador
                                     || '. Omitidas (miembro faltante): ' || v_omitidas || '.',
            'comisiones_creadas', v_contador,
            'convocatorias_omitidas', v_omitidas
           );

EXCEPTION WHEN OTHERS THEN
    RETURN jsonb_build_object(
            'exito',              false,
            'mensaje',            '[ERROR] ' || SQLERRM,
            'comisiones_creadas', 0,
            'convocatorias_omitidas', 0
           );
END;
$$;


--
-- Name: fn_gestionar_banco_temas(integer, text, jsonb); Type: FUNCTION; Schema: postulacion; Owner: -
--

CREATE FUNCTION postulacion.fn_gestionar_banco_temas(p_id_convocatoria integer, p_accion text, p_temas_json jsonb DEFAULT '[]'::jsonb) RETURNS jsonb
    LANGUAGE plpgsql SECURITY DEFINER
    AS $$
DECLARE
    v_tema          JSONB;
    v_contador      INTEGER := 0;
    v_total_temas   INTEGER;
    v_total_aptos   INTEGER;
BEGIN
    p_accion := UPPER(TRIM(COALESCE(p_accion, '')));

    -- Verificar que la convocatoria exista y esté activa
    IF NOT EXISTS (
        SELECT 1 FROM convocatoria.convocatoria
        WHERE id_convocatoria = p_id_convocatoria AND activo = TRUE
    ) THEN
        RETURN jsonb_build_object(
                'exito', false,
                'mensaje', 'La convocatoria no existe o está inactiva.'
               );
    END IF;

    IF p_accion = 'LISTAR' THEN
        SELECT COUNT(*) INTO v_total_aptos
        FROM  postulacion.postulacion po
                  JOIN  postulacion.tipo_estado_postulacion tep
                        ON tep.id_tipo_estado_postulacion = po.id_tipo_estado_postulacion
        WHERE po.id_convocatoria = p_id_convocatoria
          AND po.activo = TRUE
          AND tep.codigo = 'APROBADA';

        SELECT COUNT(*) INTO v_total_temas
        FROM postulacion.banco_temas
        WHERE id_convocatoria = p_id_convocatoria
          AND activo = TRUE;

        RETURN jsonb_build_object(
                'exito',           true,
                'temas',           COALESCE(
                        (SELECT jsonb_agg(jsonb_build_object(
                                                  'idTema',          bt.id_tema,
                                                  'descripcionTema', bt.descripcion_tema,
                                                  'activo',          bt.activo
                                          ) ORDER BY bt.id_tema)
                         FROM postulacion.banco_temas bt
                         WHERE bt.id_convocatoria = p_id_convocatoria
                           AND bt.activo = TRUE),
                        '[]'::jsonb
                                   ),
                'totalTemas',      v_total_temas,
                'totalAptos',      v_total_aptos,
                'listoParaSorteo', v_total_temas >= v_total_aptos AND v_total_aptos > 0
               );
    END IF;

    -- ── LIMPIAR ───────────────────────────────────────────────
    -- Solo borra si NO hay evaluaciones ya iniciadas (estado distinto a PROGRAMADA)
    IF p_accion = 'LIMPIAR' THEN
        IF EXISTS (
            SELECT 1
            FROM  postulacion.evaluacion_oposicion eo
                      JOIN  postulacion.tipo_estado_evaluacion tee
                            ON tee.id_tipo_estado_evaluacion = eo.id_tipo_estado_evaluacion
                      JOIN  postulacion.postulacion p ON p.id_postulacion = eo.id_postulacion
            WHERE p.id_convocatoria = p_id_convocatoria
              AND tee.codigo NOT IN ('PROGRAMADA')
        ) THEN
            RETURN jsonb_build_object(
                    'exito', false,
                    'mensaje', 'No se puede limpiar: ya existen evaluaciones en curso o finalizadas.'
                   );
        END IF;

        -- Desactivar temas (soft delete)
        UPDATE postulacion.banco_temas
        SET activo = FALSE
        WHERE id_convocatoria = p_id_convocatoria;

        GET DIAGNOSTICS v_contador = ROW_COUNT;
        RETURN jsonb_build_object(
                'exito',   true,
                'mensaje', v_contador || ' tema(s) eliminado(s) del banco.'
               );
    END IF;

    -- ── REGISTRAR ─────────────────────────────────────────────
    IF p_accion = 'REGISTRAR' THEN
        IF jsonb_array_length(p_temas_json) = 0 THEN
            RETURN jsonb_build_object('exito', false, 'mensaje', 'Debes enviar al menos un tema.');
        END IF;

        -- Insertar cada tema del array JSON
        FOR v_tema IN SELECT * FROM jsonb_array_elements(p_temas_json)
            LOOP
                INSERT INTO postulacion.banco_temas (id_convocatoria, descripcion_tema, activo)
                VALUES (
                           p_id_convocatoria,
                           TRIM(v_tema->>'descripcionTema'),
                           TRUE
                       );
                v_contador := v_contador + 1;
            END LOOP;

        -- Informar también cuántos temas hay ahora vs postulantes aptos
        SELECT COUNT(*) INTO v_total_temas
        FROM postulacion.banco_temas
        WHERE id_convocatoria = p_id_convocatoria AND activo = TRUE;

        SELECT COUNT(*) INTO v_total_aptos
        FROM  postulacion.postulacion          po
                  JOIN  postulacion.tipo_estado_postulacion tep
                        ON tep.id_tipo_estado_postulacion = po.id_tipo_estado_postulacion
        WHERE po.id_convocatoria = p_id_convocatoria
          AND po.activo = TRUE
          AND tep.codigo = 'APROBADA';

        RETURN jsonb_build_object(
                'exito',          true,
                'mensaje',        v_contador || ' tema(s) registrado(s) exitosamente.',
                'totalTemas',     v_total_temas,
                'totalAptos',     v_total_aptos,
                'listoParaSorteo', v_total_temas >= v_total_aptos AND v_total_aptos > 0
               );
    END IF;

    RETURN jsonb_build_object('exito', false, 'mensaje', 'Acción no reconocida: ' || p_accion);

EXCEPTION WHEN OTHERS THEN
    RETURN jsonb_build_object('exito', false, 'mensaje', '[ERROR] ' || SQLERRM);
END;
$$;


--
-- Name: fn_guardar_evaluacion_meritos(integer, integer, numeric, jsonb, numeric, numeric, boolean); Type: FUNCTION; Schema: postulacion; Owner: -
--

CREATE FUNCTION postulacion.fn_guardar_evaluacion_meritos(p_id_postulacion integer, p_id_usuario integer, p_nota_asignatura_raw numeric, p_semestres_json jsonb, p_nota_experiencia numeric, p_nota_eventos numeric, p_finalizar boolean DEFAULT false) RETURNS jsonb
    LANGUAGE plpgsql SECURITY DEFINER
    AS $$
DECLARE
    v_id_periodo           INTEGER;
    v_fase_activa          BOOLEAN;
    v_id_carrera           INTEGER;
    v_nota_asignatura      NUMERIC(5,2);
    v_nota_semestres       NUMERIC(5,2);
    v_nota_total           NUMERIC(5,2);
    v_id_evaluacion        INTEGER;
    v_estado_actual        TEXT;
    v_id_estado_borrador   INTEGER;
    v_id_estado_finalizada INTEGER;
    v_id_estado_en_eval    INTEGER;
BEGIN
    -- ── Validar fase ──────────────────────────────────────────────
    SELECT pa.id_periodo_academico INTO v_id_periodo
    FROM   academico.periodo_academico pa
    WHERE  pa.activo = TRUE AND pa.estado = 'EN PROCESO'
    LIMIT  1;

    IF v_id_periodo IS NULL THEN
        RAISE EXCEPTION 'VALIDACION: No existe un período académico activo.';
    END IF;

    SELECT CURRENT_DATE BETWEEN pf.fecha_inicio AND pf.fecha_fin
    INTO   v_fase_activa
    FROM   planificacion.periodo_fase pf
               JOIN   planificacion.tipo_fase tf ON tf.id_tipo_fase = pf.id_tipo_fase
    WHERE  pf.id_periodo_academico = v_id_periodo
      AND  tf.codigo               = 'EVALUACION_MER_OPO'
    LIMIT  1;

    IF NOT COALESCE(v_fase_activa, FALSE) THEN
        RAISE EXCEPTION 'VALIDACION: El período de calificación de méritos no está activo.';
    END IF;

    -- ── Validar acceso del coordinador ────────────────────────────
    SELECT a.id_carrera INTO v_id_carrera
    FROM   postulacion.postulacion    po
               JOIN   convocatoria.convocatoria c  ON c.id_convocatoria = po.id_convocatoria
               JOIN   academico.asignatura      a  ON a.id_asignatura   = c.id_asignatura
    WHERE  po.id_postulacion = p_id_postulacion AND po.activo = TRUE;

    IF NOT FOUND THEN
        RAISE EXCEPTION 'VALIDACION: La postulación no existe o está inactiva.';
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM academico.coordinador co
        WHERE  co.id_usuario = p_id_usuario
          AND  co.id_carrera  = v_id_carrera
          AND  co.activo      = TRUE
    ) THEN
        RAISE EXCEPTION 'ACCESO: No tienes permiso para evaluar esta postulación.';
    END IF;

    -- ── Validar rangos de los campos manuales ─────────────────────
    IF p_nota_experiencia IS NULL OR p_nota_experiencia < 0 OR p_nota_experiencia > 4 THEN
        RAISE EXCEPTION 'VALIDACION: La nota de experiencia debe estar entre 0.00 y 4.00.';
    END IF;
    IF p_nota_eventos IS NULL OR p_nota_eventos < 0 OR p_nota_eventos > 2 THEN
        RAISE EXCEPTION 'VALIDACION: La nota de eventos debe estar entre 0.00 y 2.00.';
    END IF;
    IF p_nota_asignatura_raw IS NULL OR p_nota_asignatura_raw < 0 OR p_nota_asignatura_raw > 10 THEN
        RAISE EXCEPTION 'VALIDACION: La nota de aprobación de la asignatura debe estar entre 0.00 y 10.00.';
    END IF;

    -- ── Calcular nota_asignatura (tabla de conversión) ────────────
    -- 9.50–10.00 → 10 pts | 9.00–9.49 → 9 pts
    -- 8.50–8.99  →  8 pts | 8.00–8.49 → 7 pts | <8.00 → 0 pts
    v_nota_asignatura := CASE
                             WHEN p_nota_asignatura_raw >= 9.50 THEN 10.00
                             WHEN p_nota_asignatura_raw >= 9.00 THEN  9.00
                             WHEN p_nota_asignatura_raw >= 8.50 THEN  8.00
                             WHEN p_nota_asignatura_raw >= 8.00 THEN  7.00
                             ELSE                                      0.00
        END;

    -- ── Calcular nota_semestres (acumulado, tope 4.00) ────────────
    -- Cada semestre aporta según su rango:
    --   [9.50, 10]  → 1.00 pts | [9.00, 9.49] → 0.70 pts
    --   [8.50, 8.99]→ 0.50 pts | [8.00, 8.49] → 0.25 pts
    SELECT LEAST(
                   COALESCE(
                           SUM(
                                   CASE
                                       WHEN n::NUMERIC >= 9.50 THEN 1.00
                                       WHEN n::NUMERIC >= 9.00 THEN 0.70
                                       WHEN n::NUMERIC >= 8.50 THEN 0.50
                                       WHEN n::NUMERIC >= 8.00 THEN 0.25
                                       ELSE                         0.00
                                       END
                           ), 0.00
                   ), 4.00
           )::NUMERIC(5,2)
    INTO   v_nota_semestres
    FROM   jsonb_array_elements_text(COALESCE(p_semestres_json, '[]'::jsonb)) n;

    -- ── Total ──────────────────────────────────────────────────────
    v_nota_total := ROUND(
            v_nota_asignatura + v_nota_semestres + p_nota_experiencia + p_nota_eventos,
            2
                    );

    -- ── IDs de estados ─────────────────────────────────────────────
    SELECT id_tipo_estado_evaluacion INTO v_id_estado_borrador
    FROM   postulacion.tipo_estado_evaluacion WHERE codigo = 'BORRADOR' LIMIT 1;

    SELECT id_tipo_estado_evaluacion INTO v_id_estado_finalizada
    FROM   postulacion.tipo_estado_evaluacion WHERE codigo = 'FINALIZADA' LIMIT 1;

    -- ── Verificar evaluación existente ────────────────────────────
    SELECT em.id_evaluacion_meritos, tee.codigo
    INTO   v_id_evaluacion, v_estado_actual
    FROM   postulacion.evaluacion_meritos         em
               JOIN   postulacion.tipo_estado_evaluacion tee ON tee.id_tipo_estado_evaluacion = em.id_tipo_estado_evaluacion
    WHERE  em.id_postulacion = p_id_postulacion
    ORDER  BY em.id_evaluacion_meritos DESC
    LIMIT  1;

    IF FOUND AND v_estado_actual = 'FINALIZADA' THEN
        RAISE EXCEPTION
            'VALIDACION: La evaluación de méritos ya fue finalizada y no puede modificarse. Use la opción "Reabrir Evaluación" para corregir.';
    END IF;

    IF v_id_evaluacion IS NULL THEN
        -- ── CREAR nuevo registro ──────────────────────────────────
        -- nota_total_meritos es GENERATED ALWAYS AS (...) STORED → PostgreSQL la calcula sola
        INSERT INTO postulacion.evaluacion_meritos (
            id_postulacion,
            nota_asignatura,
            nota_semestres,
            nota_experiencia,
            nota_eventos,
            fecha_evaluacion,
            id_tipo_estado_evaluacion
        ) VALUES (
                     p_id_postulacion,
                     v_nota_asignatura,
                     v_nota_semestres,
                     p_nota_experiencia,
                     p_nota_eventos,
                     CURRENT_DATE,
                     CASE WHEN p_finalizar THEN v_id_estado_finalizada ELSE v_id_estado_borrador END
                 )
        RETURNING id_evaluacion_meritos INTO v_id_evaluacion;

        -- Actualizar postulación → EN_EVALUACION al crear el primer borrador
        SELECT id_tipo_estado_postulacion INTO v_id_estado_en_eval
        FROM   postulacion.tipo_estado_postulacion WHERE codigo = 'EN_EVALUACION' LIMIT 1;

        IF v_id_estado_en_eval IS NOT NULL THEN
            UPDATE postulacion.postulacion
            SET    id_tipo_estado_postulacion = v_id_estado_en_eval
            WHERE  id_postulacion = p_id_postulacion;
        END IF;

    ELSE
        -- ── ACTUALIZAR borrador existente ──────────────────────────
        -- nota_total_meritos es columna generada → se recalcula automáticamente al actualizar
        UPDATE postulacion.evaluacion_meritos
        SET    nota_asignatura            = v_nota_asignatura,
               nota_semestres             = v_nota_semestres,
               nota_experiencia           = p_nota_experiencia,
               nota_eventos               = p_nota_eventos,
               fecha_evaluacion           = CURRENT_DATE,
               id_tipo_estado_evaluacion  = CASE WHEN p_finalizar THEN v_id_estado_finalizada ELSE v_id_estado_borrador END
        WHERE  id_evaluacion_meritos = v_id_evaluacion;
    END IF;

    RETURN jsonb_build_object(
            'exito',               true,
            'mensaje',             CASE WHEN p_finalizar
                                            THEN 'Evaluación de méritos finalizada exitosamente. El puntaje es ahora inmutable.'
                                        ELSE 'Borrador guardado correctamente.'
                END,
            'idEvaluacionMeritos', v_id_evaluacion,
            'notaAsignatura',      v_nota_asignatura,
            'notaSemestres',       v_nota_semestres,
            'notaExperiencia',     p_nota_experiencia,
            'notaEventos',         p_nota_eventos,
            'notaTotal',           v_nota_total,
            'finalizada',          p_finalizar
           );

EXCEPTION
    WHEN OTHERS THEN
        -- Re-lanzar excepciones con prefijo para que GlobalExceptionHandler las maneje
        IF SQLERRM LIKE 'VALIDACION:%' OR SQLERRM LIKE 'ACCESO:%' THEN
            RAISE;
        END IF;
        RETURN jsonb_build_object('exito', false, 'mensaje', '[ERROR] ' || SQLERRM);
END;
$$;


--
-- Name: fn_listar_convocatorias_para_oposicion(); Type: FUNCTION; Schema: postulacion; Owner: -
--

CREATE FUNCTION postulacion.fn_listar_convocatorias_para_oposicion() RETURNS jsonb
    LANGUAGE plpgsql STABLE SECURITY DEFINER
    AS $$
DECLARE
    v_resultado JSONB;
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM   academico.periodo_academico
        WHERE  activo  = TRUE
          AND  estado  = 'EN PROCESO'
    ) THEN
        RETURN jsonb_build_object(
                'exito',   false,
                'mensaje', 'No existe un período académico activo en estado EN PROCESO.'
               );
    END IF;

    SELECT COALESCE(
                   jsonb_agg(
                           jsonb_build_object(
                                   'idConvocatoria',       c.id_convocatoria,
                                   'nombreAsignatura',     a.nombre_asignatura,
                                   'semestreAsignatura',   a.semestre,
                                   'nombreCarrera',        ca.nombre_carrera,
                                   'nombreFacultad',       f.nombre_facultad,
                                   'nombreDocente',        u.nombres || ' ' || u.apellidos,
                                   'cuposDisponibles',     c.cupos_disponibles,
                                   'estadoConvocatoria',   c.estado,
                                   'totalPostulantesAptos', (
                                       SELECT COUNT(*)
                                       FROM   postulacion.postulacion          po
                                                  JOIN   postulacion.tipo_estado_postulacion tep
                                                         ON tep.id_tipo_estado_postulacion = po.id_tipo_estado_postulacion
                                       WHERE  po.id_convocatoria = c.id_convocatoria
                                         AND  po.activo          = TRUE
                                         AND  UPPER(tep.codigo) IN ('APROBADA', 'EN_EVALUACION')
                                   ),
                                   'tieneComision', EXISTS (
                               SELECT 1
                               FROM   postulacion.comision_seleccion cs
                               WHERE  cs.id_convocatoria = c.id_convocatoria
                                 AND  cs.activo          = TRUE
                           ),
                                   'tieneSorteo', EXISTS (
                               SELECT 1
                               FROM   postulacion.evaluacion_oposicion eo
                                          JOIN   postulacion.postulacion            po
                                                 ON po.id_postulacion = eo.id_postulacion
                               WHERE  po.id_convocatoria = c.id_convocatoria
                           )
                           ) ORDER BY a.nombre_asignatura ASC
                   ),
                   '[]'::jsonb
           )
    INTO v_resultado
    FROM  convocatoria.convocatoria       c
              JOIN  academico.asignatura        a   ON a.id_asignatura   = c.id_asignatura
              JOIN  academico.carrera           ca  ON ca.id_carrera     = a.id_carrera
              JOIN  academico.facultad          f   ON f.id_facultad     = ca.id_facultad
              JOIN  academico.docente           d   ON d.id_docente      = c.id_docente
              JOIN  seguridad.usuario           u   ON u.id_usuario      = d.id_usuario
              JOIN  academico.periodo_academico p   ON p.id_periodo_academico = c.id_periodo_academico
    WHERE  c.activo      = TRUE
      AND  p.activo      = TRUE
      AND  p.estado      = 'EN PROCESO'
      AND  UPPER(c.estado) IN ('ABIERTA')
      AND  EXISTS (
        SELECT 1
        FROM   postulacion.postulacion              po2
                   JOIN   postulacion.tipo_estado_postulacion  tep2
                          ON tep2.id_tipo_estado_postulacion = po2.id_tipo_estado_postulacion
        WHERE  po2.id_convocatoria = c.id_convocatoria
          AND  po2.activo          = TRUE
          AND  UPPER(tep2.codigo)  IN ('APROBADA', 'EN_EVALUACION')
    );

    IF jsonb_array_length(v_resultado) = 0 THEN
        RETURN jsonb_build_object(
                'exito',   false,
                'mensaje', 'No hay convocatorias con postulantes aprobados en el período activo.'
               );
    END IF;

    RETURN jsonb_build_object(
            'exito', true,
            'datos', v_resultado
           );

EXCEPTION WHEN OTHERS THEN
    RETURN jsonb_build_object(
            'exito',   false,
            'mensaje', '[ERROR] ' || SQLERRM
           );
END;
$$;


--
-- Name: fn_listar_postulaciones_coordinador(integer); Type: FUNCTION; Schema: postulacion; Owner: -
--

CREATE FUNCTION postulacion.fn_listar_postulaciones_coordinador(p_id_usuario integer) RETURNS TABLE(id_postulacion integer, id_convocatoria integer, id_estudiante integer, nombre_estudiante character varying, matricula character varying, semestre integer, nombre_asignatura character varying, nombre_carrera character varying, fecha_postulacion date, estado_codigo character varying, estado_nombre character varying, requiere_atencion boolean, total_documentos bigint, documentos_pendientes bigint, documentos_aprobados bigint, documentos_observados bigint, observaciones character varying)
    LANGUAGE plpgsql
    AS $$
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
$$;


--
-- Name: fn_listar_postulaciones_para_meritos(integer); Type: FUNCTION; Schema: postulacion; Owner: -
--

CREATE FUNCTION postulacion.fn_listar_postulaciones_para_meritos(p_id_usuario integer) RETURNS jsonb
    LANGUAGE plpgsql STABLE SECURITY DEFINER
    AS $$
DECLARE
    v_id_periodo  INTEGER;
    v_fase_activa BOOLEAN := FALSE;
    v_resultado   JSONB;
BEGIN
    -- Período activo
    SELECT pa.id_periodo_academico INTO v_id_periodo
    FROM   academico.periodo_academico pa
    WHERE  pa.activo = TRUE AND pa.estado = 'EN PROCESO'
    ORDER  BY pa.id_periodo_academico DESC
    LIMIT  1;

    IF v_id_periodo IS NULL THEN
        RETURN jsonb_build_object(
                'exito',   false,
                'mensaje', 'No existe un período académico activo en estado EN PROCESO.'
               );
    END IF;

    -- ¿Fase de evaluación activa?
    SELECT CURRENT_DATE BETWEEN pf.fecha_inicio AND pf.fecha_fin
    INTO   v_fase_activa
    FROM   planificacion.periodo_fase pf
               JOIN   planificacion.tipo_fase tf ON tf.id_tipo_fase = pf.id_tipo_fase
    WHERE  pf.id_periodo_academico = v_id_periodo
      AND  tf.codigo               = 'EVALUACION_MER_OPO'
    LIMIT 1;

    -- Lista de postulaciones
    SELECT COALESCE(
                   jsonb_agg(
                           jsonb_build_object(
                                   'idPostulacion',     po.id_postulacion,
                                   'nombres',           u.nombres,
                                   'apellidos',         u.apellidos,
                                   'correo',            u.correo,
                                   'matricula',         est.matricula,
                                   'semestreEstudiante',est.semestre,
                                   'nombreAsignatura',  a.nombre_asignatura,
                                   'semestreAsignatura',a.semestre,
                                   'nombreCarrera',     ca.nombre_carrera,
                                   'estadoPostulacion', tep.codigo,
                               -- Estado de la evaluación de méritos (si ya existe)
                                   'estadoEvaluacion',  tee.codigo,
                                   'nombreEstadoEval',  tee.nombre,
                                   'notaTotal',         em.nota_total_meritos,
                                   'fechaEvaluacion',   to_char(em.fecha_evaluacion, 'YYYY-MM-DD'),
                                   'idEvaluacionMeritos', em.id_evaluacion_meritos
                           ) ORDER BY ca.nombre_carrera, a.nombre_asignatura, u.apellidos
                   ),
                   '[]'::jsonb
           )
    INTO   v_resultado
    FROM   postulacion.postulacion                  po
               JOIN   convocatoria.convocatoria          c    ON c.id_convocatoria  = po.id_convocatoria
               JOIN   academico.asignatura               a    ON a.id_asignatura    = c.id_asignatura
               JOIN   academico.carrera                  ca   ON ca.id_carrera      = a.id_carrera
               JOIN   academico.estudiante               est  ON est.id_estudiante  = po.id_estudiante
               JOIN   seguridad.usuario                  u    ON u.id_usuario       = est.id_usuario
               JOIN   postulacion.tipo_estado_postulacion tep ON tep.id_tipo_estado_postulacion = po.id_tipo_estado_postulacion
        -- Evaluación de méritos más reciente (puede ser NULL)
               LEFT JOIN LATERAL (
        SELECT em2.id_evaluacion_meritos,
               em2.nota_total_meritos,
               em2.fecha_evaluacion,
               em2.id_tipo_estado_evaluacion
        FROM   postulacion.evaluacion_meritos em2
        WHERE  em2.id_postulacion = po.id_postulacion
        ORDER  BY em2.id_evaluacion_meritos DESC
        LIMIT  1
        ) lat ON TRUE
               LEFT JOIN postulacion.evaluacion_meritos     em  ON em.id_evaluacion_meritos = lat.id_evaluacion_meritos
               LEFT JOIN postulacion.tipo_estado_evaluacion tee ON tee.id_tipo_estado_evaluacion = em.id_tipo_estado_evaluacion
    WHERE  po.activo = TRUE
      AND  c.activo  = TRUE
      AND  c.id_periodo_academico = v_id_periodo
      AND  tep.codigo IN ('APROBADA', 'EN_EVALUACION')
      -- Solo postulaciones en carreras del coordinador
      AND  ca.id_carrera IN (
        SELECT co.id_carrera
        FROM   academico.coordinador co
        WHERE  co.id_usuario = p_id_usuario
          AND  co.activo     = TRUE
    );

    RETURN jsonb_build_object(
            'exito',         true,
            'faseActiva',    COALESCE(v_fase_activa, FALSE),
            'postulaciones', v_resultado
           );

EXCEPTION WHEN OTHERS THEN
    RETURN jsonb_build_object('exito', false, 'mensaje', '[ERROR] ' || SQLERRM);
END;
$$;


--
-- Name: fn_listar_tipo_estado_postulacion(); Type: FUNCTION; Schema: postulacion; Owner: -
--

CREATE FUNCTION postulacion.fn_listar_tipo_estado_postulacion() RETURNS jsonb
    LANGUAGE plpgsql SECURITY DEFINER
    AS $$
DECLARE
    v_resultado JSONB;
BEGIN
    SELECT COALESCE(jsonb_agg(
        jsonb_build_object(
            'id', id_tipo_estado_postulacion,
            'codigo', codigo,
            'nombre', nombre,
            'descripcion', descripcion,
            'activo', activo,
            'fecha_creacion', fecha_creacion
        ) ORDER BY nombre
    ), '[]'::jsonb)
    INTO v_resultado
    FROM postulacion.tipo_estado_postulacion
    WHERE activo = TRUE;

    RETURN jsonb_build_object(
        'exito', true,
        'datos', v_resultado
    );

EXCEPTION
    WHEN OTHERS THEN
        RETURN jsonb_build_object('exito', false, 'mensaje', 'Error al listar estados de postulación: ' || SQLERRM);
END;
$$;


--
-- Name: fn_obtener_detalle_postulacion_coordinador(integer, integer); Type: FUNCTION; Schema: postulacion; Owner: -
--

CREATE FUNCTION postulacion.fn_obtener_detalle_postulacion_coordinador(p_id_usuario integer, p_id_postulacion integer) RETURNS jsonb
    LANGUAGE plpgsql
    AS $$
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

    -- 3. Construir respuesta JSON con estado_codigo para documentos
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
                    -- CORREGIDO: Incluir ambos campos
                    'estado_codigo', UPPER(ter.codigo),
                    'estado_nombre', ter.codigo,
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
$$;


--
-- Name: fn_obtener_documentos_postulacion(integer); Type: FUNCTION; Schema: postulacion; Owner: -
--

CREATE FUNCTION postulacion.fn_obtener_documentos_postulacion(p_id_postulacion integer) RETURNS jsonb
    LANGUAGE plpgsql SECURITY DEFINER
    AS $$
DECLARE
    v_documentos JSONB;
    v_count INTEGER;
BEGIN
    IF NOT EXISTS (SELECT 1 FROM postulacion.postulacion WHERE id_postulacion = p_id_postulacion) THEN
        RETURN jsonb_build_object(
                'exito', FALSE,
                'mensaje', 'La postulación no existe'
               );
    END IF;

    SELECT jsonb_agg(
                   jsonb_build_object(
                           'id_requisito_adjunto', ra.id_requisito_adjunto,
                           'id_tipo_requisito', trp.id_tipo_requisito_postulacion,
                           'nombre_requisito', trp.nombre_requisito,
                           'descripcion_requisito', trp.descripcion,
                           'tipo_documento_permitido', trp.tipo_documento_permitido,
                           'nombre_archivo', ra.nombre_archivo,
                           'fecha_subida', ra.fecha_subida,
                           'estado', ter.nombre_estado,
                           'id_tipo_estado_requisito', ter.id_tipo_estado_requisito,
                           'observacion', COALESCE(ra.observacion, ''),
                           'es_editable', (ter.nombre_estado = 'OBSERVADO'),
                           'tiene_archivo', (ra.archivo IS NOT NULL)
                   )
                   ORDER BY trp.id_tipo_requisito_postulacion
           )
    INTO v_documentos
    FROM postulacion.requisito_adjunto ra
             INNER JOIN convocatoria.tipo_requisito_postulacion trp
                        ON ra.id_tipo_requisito_postulacion = trp.id_tipo_requisito_postulacion
             INNER JOIN convocatoria.tipo_estado_requisito ter
                        ON ra.id_tipo_estado_requisito = ter.id_tipo_estado_requisito
    WHERE ra.id_postulacion = p_id_postulacion;

    SELECT COUNT(*) INTO v_count
    FROM postulacion.requisito_adjunto
    WHERE id_postulacion = p_id_postulacion;

    RETURN jsonb_build_object(
            'exito', TRUE,
            'total_documentos', v_count,
            'documentos', COALESCE(v_documentos, '[]'::jsonb)
           );
END;
$$;


--
-- Name: fn_obtener_evaluacion_meritos(integer, integer); Type: FUNCTION; Schema: postulacion; Owner: -
--

CREATE FUNCTION postulacion.fn_obtener_evaluacion_meritos(p_id_postulacion integer, p_id_usuario integer) RETURNS jsonb
    LANGUAGE plpgsql STABLE SECURITY DEFINER
    AS $$
DECLARE
    v_id_periodo  INTEGER;
    v_fase_activa BOOLEAN := FALSE;
    v_resultado   JSONB;
BEGIN
    SELECT pa.id_periodo_academico INTO v_id_periodo
    FROM   academico.periodo_academico pa
    WHERE  pa.activo = TRUE AND pa.estado = 'EN PROCESO'
    LIMIT  1;

    SELECT CURRENT_DATE BETWEEN pf.fecha_inicio AND pf.fecha_fin
    INTO   v_fase_activa
    FROM   planificacion.periodo_fase pf
               JOIN   planificacion.tipo_fase tf ON tf.id_tipo_fase = pf.id_tipo_fase
    WHERE  pf.id_periodo_academico = v_id_periodo
      AND  tf.codigo               = 'EVALUACION_MER_OPO'
    LIMIT  1;

    SELECT jsonb_build_object(
                   'idPostulacion',      po.id_postulacion,
                   'nombres',            u.nombres,
                   'apellidos',          u.apellidos,
                   'correo',             u.correo,
                   'matricula',          est.matricula,
                   'semestreEstudiante', est.semestre,
                   'nombreAsignatura',   a.nombre_asignatura,
                   'semestreAsignatura', a.semestre,
                   'nombreCarrera',      ca.nombre_carrera,
                   'estadoPostulacion',  tep.codigo,
                   'faseActiva',         COALESCE(v_fase_activa, FALSE),
                   'evaluacion', CASE
                                     WHEN em.id_evaluacion_meritos IS NULL THEN NULL
                                     ELSE jsonb_build_object(
                                             'idEvaluacionMeritos', em.id_evaluacion_meritos,
                                             'notaAsignatura',      em.nota_asignatura,
                                             'notaSemestres',       em.nota_semestres,
                                             'notaExperiencia',     em.nota_experiencia,
                                             'notaEventos',         em.nota_eventos,
                                             'notaTotal',           em.nota_total_meritos,
                                             'estado',              tee.codigo,
                                             'nombreEstado',        tee.nombre,
                                             'fechaEvaluacion',     to_char(em.fecha_evaluacion, 'YYYY-MM-DD')
                                          )
                       END
           )
    INTO   v_resultado
    FROM   postulacion.postulacion                  po
               JOIN   convocatoria.convocatoria          c    ON c.id_convocatoria  = po.id_convocatoria
               JOIN   academico.asignatura               a    ON a.id_asignatura    = c.id_asignatura
               JOIN   academico.carrera                  ca   ON ca.id_carrera      = a.id_carrera
               JOIN   academico.estudiante               est  ON est.id_estudiante  = po.id_estudiante
               JOIN   seguridad.usuario                  u    ON u.id_usuario       = est.id_usuario
               JOIN   postulacion.tipo_estado_postulacion tep ON tep.id_tipo_estado_postulacion = po.id_tipo_estado_postulacion
               LEFT JOIN LATERAL (
        SELECT * FROM postulacion.evaluacion_meritos em2
        WHERE  em2.id_postulacion = po.id_postulacion
        ORDER  BY em2.id_evaluacion_meritos DESC
        LIMIT  1
        ) em  ON TRUE
               LEFT JOIN postulacion.tipo_estado_evaluacion tee ON tee.id_tipo_estado_evaluacion = em.id_tipo_estado_evaluacion
    WHERE  po.id_postulacion = p_id_postulacion;

    IF v_resultado IS NULL THEN
        RETURN jsonb_build_object('exito', false, 'mensaje', 'Postulación no encontrada.');
    END IF;

    RETURN jsonb_build_object('exito', true) || v_resultado;

EXCEPTION WHEN OTHERS THEN
    RETURN jsonb_build_object('exito', false, 'mensaje', '[ERROR] ' || SQLERRM);
END;
$$;


--
-- Name: fn_obtener_mi_turno(integer, integer); Type: FUNCTION; Schema: postulacion; Owner: -
--

CREATE FUNCTION postulacion.fn_obtener_mi_turno(p_id_convocatoria integer, p_id_usuario integer) RETURNS jsonb
    LANGUAGE plpgsql STABLE SECURITY DEFINER
    AS $$
DECLARE
    v_turno JSONB;
BEGIN
    SELECT jsonb_build_object(
                   'idEvaluacionOposicion', eo.id_evaluacion_oposicion,
                   'orden',                 eo.orden_exposicion,
                   'nombres',               u.nombres,
                   'apellidos',             u.apellidos,
                   'correo',                u.correo,
                   'tema',                  eo.tema_exposicion,
                   'fecha',                 to_char(eo.fecha_evaluacion, 'YYYY-MM-DD'),
                   'horaInicio',            to_char(eo.hora_inicio, 'HH24:MI'),
                   'horaFin',               to_char(eo.hora_fin, 'HH24:MI'),
                   'horaInicioReal',        to_char(eo.hora_inicio_real, 'HH24:MI'),
                   'horaFinReal',           to_char(eo.hora_fin_real, 'HH24:MI'),
                   'lugar',                 eo.lugar,
                   'estado',                tee.codigo,
                   'nombreEstado',          tee.nombre,
                   'puntajeFinal',          eo.puntaje_total_oposicion,
               -- Desglose de notas de cada jurado (visible al postulante solo si FINALIZADA)
                   'jurados', (
                       SELECT COALESCE(
                                      jsonb_agg(jsonb_build_object(
                                                        'idUsuario',           uj.id_usuario,
                                                        'nombres',             uj2.nombres,
                                                        'apellidos',           uj2.apellidos,
                                                        'rol',                 uj.rol_integrante,
                                                    -- Solo revelar notas individuales si la evaluación ya terminó
                                                        'puntajeMaterial',  CASE WHEN tee.codigo = 'FINALIZADA'
                                                                                     THEN uj.puntaje_material   ELSE NULL END,
                                                        'puntajeExposicion',CASE WHEN tee.codigo = 'FINALIZADA'
                                                                                     THEN uj.puntaje_exposicion ELSE NULL END,
                                                        'puntajeRespuestas',CASE WHEN tee.codigo = 'FINALIZADA'
                                                                                     THEN uj.puntaje_respuestas ELSE NULL END,
                                                        'subtotal', CASE WHEN tee.codigo = 'FINALIZADA'
                                                                             THEN COALESCE(uj.puntaje_material,   0) +
                                                                                  COALESCE(uj.puntaje_exposicion, 0) +
                                                                                  COALESCE(uj.puntaje_respuestas, 0)
                                                                         ELSE 0 END,
                                                        'finalizo', uj.finalizo_calificacion
                                                ) ORDER BY uj.rol_integrante),
                                      '[]'::jsonb
                              )
                       FROM   seguridad.usuario_comision uj
                                  JOIN   seguridad.usuario          uj2 ON uj2.id_usuario = uj.id_usuario
                                  JOIN   postulacion.comision_seleccion cs
                                         ON cs.id_comision_seleccion = uj.id_comision_seleccion
                                             AND cs.id_convocatoria = p_id_convocatoria
                                             AND cs.activo = TRUE
                   )
           )
    INTO v_turno
    FROM   postulacion.evaluacion_oposicion   eo
               JOIN   postulacion.postulacion             p   ON p.id_postulacion   = eo.id_postulacion
               JOIN   academico.estudiante                est ON est.id_estudiante  = p.id_estudiante
               JOIN   seguridad.usuario                   u   ON u.id_usuario       = est.id_usuario
               JOIN   postulacion.tipo_estado_evaluacion  tee
                      ON tee.id_tipo_estado_evaluacion = eo.id_tipo_estado_evaluacion
    WHERE  p.id_convocatoria = p_id_convocatoria
      AND  est.id_usuario    = p_id_usuario
      AND  p.activo          = TRUE;

    IF v_turno IS NULL THEN
        RETURN jsonb_build_object(
                'exito',   false,
                'mensaje', 'No se encontró un turno de oposición asignado para tu postulación en esta convocatoria.'
               );
    END IF;

    RETURN jsonb_build_object('exito', true, 'turno', v_turno);

EXCEPTION WHEN OTHERS THEN
    RETURN jsonb_build_object('exito', false, 'mensaje', '[ERROR] ' || SQLERRM);
END;
$$;


--
-- Name: fn_obtener_ranking_resultados(integer, text); Type: FUNCTION; Schema: postulacion; Owner: -
--

CREATE FUNCTION postulacion.fn_obtener_ranking_resultados(p_id_usuario integer, p_rol text) RETURNS jsonb
    LANGUAGE plpgsql STABLE SECURITY DEFINER
    AS $$
DECLARE
    v_rol        TEXT;
    v_id_periodo INTEGER;
    v_ventana_ok BOOLEAN := FALSE;
    v_resultados JSONB;
BEGIN
    v_rol := UPPER(TRIM(COALESCE(p_rol, '')));

    SELECT pa.id_periodo_academico
    INTO   v_id_periodo
    FROM   academico.periodo_academico pa
    WHERE  pa.activo = TRUE
      AND  pa.estado = 'EN PROCESO'
    ORDER  BY pa.id_periodo_academico DESC
    LIMIT  1;

    IF v_id_periodo IS NULL THEN
        RETURN jsonb_build_object(
                'exito',   false,
                'mensaje', 'No existe un período académico activo en estado EN PROCESO.'
               );
    END IF;

    SELECT
                CURRENT_DATE >= pf_ini.fecha_inicio
            AND CURRENT_DATE <= pf_fin.fecha_fin
    INTO v_ventana_ok
    FROM
        planificacion.periodo_fase pf_ini
            JOIN planificacion.tipo_fase tf_ini
                 ON tf_ini.id_tipo_fase = pf_ini.id_tipo_fase
                     AND tf_ini.codigo      = 'RESULTADOS_FINALES',
        planificacion.periodo_fase pf_fin
            JOIN planificacion.tipo_fase tf_fin
                 ON tf_fin.id_tipo_fase = pf_fin.id_tipo_fase
                     AND tf_fin.codigo      = 'CIERRE_ADMINISTRATIVO'
    WHERE pf_ini.id_periodo_academico = v_id_periodo
      AND pf_fin.id_periodo_academico = v_id_periodo;

    IF NOT COALESCE(v_ventana_ok, FALSE) THEN
        RETURN jsonb_build_object(
                'exito',           false,
                'faseNoPublicada', true,
                'mensaje',         'Los resultados están siendo procesados y validados por el tribunal.'
               );
    END IF;

    WITH base AS (
        SELECT
            po.id_postulacion,
            po.id_convocatoria,
            est.id_usuario,
            u.nombres || ' ' || u.apellidos                              AS postulante,
            COALESCE(em.nota_total_meritos,          0.0)::NUMERIC(5,2)  AS meritos,
            COALESCE(eo_fin.puntaje_total_oposicion, 0.0)::NUMERIC(5,2)  AS oposicion,
            ROUND(
                    COALESCE(em.nota_total_meritos,          0.0) +
                    COALESCE(eo_fin.puntaje_total_oposicion, 0.0)
                , 2)::NUMERIC(5,2)                                            AS total,
            c.cupos_disponibles,
            a.nombre_asignatura,
            a.semestre,
            a.id_asignatura,
            ca.nombre_carrera,
            ca.id_carrera,
            fa.nombre_facultad,
            fa.id_facultad
        FROM   postulacion.postulacion          po
                   JOIN convocatoria.convocatoria      c   ON c.id_convocatoria  = po.id_convocatoria
                   JOIN academico.asignatura           a   ON a.id_asignatura    = c.id_asignatura
                   JOIN academico.carrera              ca  ON ca.id_carrera      = a.id_carrera
                   JOIN academico.facultad             fa  ON fa.id_facultad     = ca.id_facultad
                   JOIN academico.estudiante           est ON est.id_estudiante  = po.id_estudiante
                   JOIN seguridad.usuario              u   ON u.id_usuario       = est.id_usuario
                   LEFT JOIN LATERAL (
            SELECT nota_total_meritos
            FROM   postulacion.evaluacion_meritos
            WHERE  id_postulacion = po.id_postulacion
            ORDER  BY id_evaluacion_meritos DESC
            LIMIT  1
            ) em ON TRUE
                   LEFT JOIN LATERAL (
            SELECT eo.puntaje_total_oposicion
            FROM   postulacion.evaluacion_oposicion   eo
                       JOIN postulacion.tipo_estado_evaluacion tee
                            ON tee.id_tipo_estado_evaluacion = eo.id_tipo_estado_evaluacion
            WHERE  eo.id_postulacion = po.id_postulacion
              AND  tee.codigo        = 'FINALIZADA'
            ORDER  BY eo.id_evaluacion_oposicion DESC
            LIMIT  1
            ) eo_fin ON TRUE
        WHERE po.activo = TRUE
          AND (c.activo  = TRUE OR c.estado = 'CERRADA' OR c.estado = 'RESUELTA')
          AND c.id_periodo_academico = v_id_periodo
    ),

         ranked AS (
             SELECT
                 b.*,
                 RANK() OVER (
                     PARTITION BY b.id_convocatoria
                     ORDER BY b.total DESC, b.postulante ASC
                     )::INTEGER AS posicion
             FROM base b
         ),
         con_estado AS (
             SELECT
                 r.*,
                 CASE
                     WHEN r.total < 25.00                                THEN 'NO_SELECCIONADO'
                     WHEN r.posicion <= r.cupos_disponibles              THEN 'SELECCIONADO'
                     ELSE                                                     'ELEGIBLE'
                     END AS estado
             FROM ranked r
         )

    -- ── 4. Filtrado por rol ───────────────────────────────────────────
    SELECT COALESCE(jsonb_agg(
                            jsonb_build_object(
                                    'posicion',         ce.posicion,
                                    'postulante',       ce.postulante,
                                    'meritos',          ce.meritos,
                                    'oposicion',        ce.oposicion,
                                    'total',            ce.total,
                                    'estado',           ce.estado,
                                    'asignatura',       ce.nombre_asignatura,
                                    'semestre',         ce.semestre,
                                    'carrera',          ce.nombre_carrera,
                                    'facultad',         ce.nombre_facultad,
                                    'cuposDisponibles', ce.cupos_disponibles
                            ) ORDER BY ce.nombre_asignatura, ce.posicion
                    ), '[]'::jsonb)
    INTO v_resultados
    FROM con_estado ce
    WHERE CASE v_rol
              WHEN 'ESTUDIANTE' THEN
                  ce.id_usuario = p_id_usuario

              WHEN 'COORDINADOR' THEN
                  ce.id_carrera IN (
                      SELECT co.id_carrera
                      FROM   academico.coordinador co
                      WHERE  co.id_usuario = p_id_usuario AND co.activo = TRUE
                  )

              WHEN 'DECANO' THEN
                  ce.id_facultad IN (
                      SELECT de.id_facultad
                      FROM   academico.decano de
                      WHERE  de.id_usuario = p_id_usuario AND de.activo = TRUE
                  )

              WHEN 'DOCENTE' THEN
                  ce.id_convocatoria IN (
                      SELECT DISTINCT cs.id_convocatoria
                      FROM   postulacion.comision_seleccion  cs
                                 JOIN seguridad.usuario_comision    uc
                                      ON uc.id_comision_seleccion = cs.id_comision_seleccion
                      WHERE  uc.id_usuario = p_id_usuario
                        AND  cs.activo     = TRUE
                        AND  uc.activo     = TRUE
                  )

              WHEN 'ADMINISTRADOR' THEN TRUE
              ELSE FALSE
              END = TRUE;

    RETURN jsonb_build_object(
            'exito',      true,
            'resultados', v_resultados
           );

EXCEPTION WHEN OTHERS THEN
    RETURN jsonb_build_object('exito', false, 'mensaje', '[ERROR] ' || SQLERRM);
END;
$$;


--
-- Name: fn_obtener_tribunal_evaluacion(integer); Type: FUNCTION; Schema: postulacion; Owner: -
--

CREATE FUNCTION postulacion.fn_obtener_tribunal_evaluacion(p_id_usuario integer) RETURNS seguridad.res_operacion
    LANGUAGE plpgsql
    AS $$
DECLARE
    v_id_estudiante  INTEGER;
    v_datos_tribunal JSON;
BEGIN

    SELECT id_rol_especifico
    INTO v_id_estudiante
    FROM seguridad.fn_identidad_usuario(p_id_usuario) WHERE nombre_rol = 'ESTUDIANTE';

    IF v_id_estudiante IS NULL THEN
        RETURN (FALSE, 'El usuario no existe en el sistema o no tiene rol de ESTUDIANTE.', NULL)::seguridad.res_operacion;
    END IF;

    SELECT json_build_object(
                   'comision', cs.nombre_comision,
                   'evaluacion', json_build_object(
                           'tema_exposicion', eo.tema_exposicion,
                           'lugar', eo.lugar,
                           'fecha_evaluacion', eo.fecha_evaluacion,
                           'hora_inicio', eo.hora_inicio,
                           'hora_fin', eo.hora_fin,
                           'orden_exposicion', eo.orden_exposicion
                                 ),
                   'miembros', json_agg(
                           json_build_object(
                                   'nombre', u.nombres || ' ' || u.apellidos,
                                   'rol', uc.rol_integrante
                           )
                               )
           )
    INTO v_datos_tribunal
    FROM postulacion.postulacion p
             JOIN postulacion.tipo_estado_postulacion tep ON p.id_tipo_estado_postulacion = tep.id_tipo_estado_postulacion
             JOIN convocatoria.convocatoria c ON p.id_convocatoria = c.id_convocatoria
             JOIN academico.periodo_academico pa ON c.id_periodo_academico = pa.id_periodo_academico
             JOIN postulacion.evaluacion_oposicion eo ON eo.id_postulacion = p.id_postulacion
             JOIN postulacion.comision_seleccion cs ON cs.id_convocatoria = c.id_convocatoria
             JOIN seguridad.usuario_comision uc ON uc.id_comision_seleccion = cs.id_comision_seleccion
             JOIN seguridad.usuario u ON u.id_usuario = uc.id_usuario
    WHERE p.id_estudiante = v_id_estudiante
      AND p.activo = true
      AND tep.codigo = 'APROBADA'
      AND pa.estado = 'EN PROCESO'
      AND pa.activo = true
    GROUP BY
        cs.nombre_comision,
        eo.tema_exposicion,
        eo.lugar,
        eo.fecha_evaluacion,
        eo.hora_inicio,
        eo.hora_fin,
        eo.orden_exposicion;

    IF v_datos_tribunal IS NULL THEN
        RETURN (FALSE, 'No se encontró una evaluación programada para una postulación aprobada en el periodo actual.', NULL)::seguridad.res_operacion;
    END IF;

    RETURN (TRUE, 'Datos del tribunal y evaluación obtenidos correctamente.', v_datos_tribunal)::seguridad.res_operacion;

EXCEPTION
    WHEN OTHERS THEN
        RETURN (FALSE, 'Error al obtener los datos del tribunal: ' || SQLERRM, NULL)::seguridad.res_operacion;
END;
$$;


--
-- Name: fn_reabrir_evaluacion_meritos(integer, integer); Type: FUNCTION; Schema: postulacion; Owner: -
--

CREATE FUNCTION postulacion.fn_reabrir_evaluacion_meritos(p_id_postulacion integer, p_id_usuario integer) RETURNS jsonb
    LANGUAGE plpgsql SECURITY DEFINER
    AS $$
DECLARE
    v_id_periodo           INTEGER;
    v_fase_activa          BOOLEAN;
    v_id_evaluacion        INTEGER;
    v_estado_actual        TEXT;
    v_id_carrera           INTEGER;
    v_id_estado_borrador   INTEGER;
BEGIN
    -- Validar fase
    SELECT pa.id_periodo_academico INTO v_id_periodo
    FROM   academico.periodo_academico pa
    WHERE  pa.activo = TRUE AND pa.estado = 'EN PROCESO' LIMIT 1;

    SELECT CURRENT_DATE BETWEEN pf.fecha_inicio AND pf.fecha_fin INTO v_fase_activa
    FROM   planificacion.periodo_fase pf
               JOIN   planificacion.tipo_fase tf ON tf.id_tipo_fase = pf.id_tipo_fase
    WHERE  pf.id_periodo_academico = v_id_periodo
      AND  tf.codigo = 'EVALUACION_MER_OPO'
    LIMIT  1;

    IF NOT COALESCE(v_fase_activa, FALSE) THEN
        RAISE EXCEPTION 'VALIDACION: El período de calificación de méritos no está activo. No se puede reabrir la evaluación.';
    END IF;

    -- Validar acceso del coordinador
    SELECT a.id_carrera INTO v_id_carrera
    FROM   postulacion.postulacion    po
               JOIN   convocatoria.convocatoria c  ON c.id_convocatoria = po.id_convocatoria
               JOIN   academico.asignatura      a  ON a.id_asignatura   = c.id_asignatura
    WHERE  po.id_postulacion = p_id_postulacion;

    IF NOT EXISTS (
        SELECT 1 FROM academico.coordinador co
        WHERE  co.id_usuario = p_id_usuario AND co.id_carrera = v_id_carrera AND co.activo = TRUE
    ) THEN
        RAISE EXCEPTION 'ACCESO: No tienes permiso para reabrir esta evaluación.';
    END IF;

    -- Obtener evaluación actual
    SELECT em.id_evaluacion_meritos, tee.codigo
    INTO   v_id_evaluacion, v_estado_actual
    FROM   postulacion.evaluacion_meritos em
               JOIN   postulacion.tipo_estado_evaluacion tee ON tee.id_tipo_estado_evaluacion = em.id_tipo_estado_evaluacion
    WHERE  em.id_postulacion = p_id_postulacion
    ORDER  BY em.id_evaluacion_meritos DESC
    LIMIT  1;

    IF NOT FOUND THEN
        RAISE EXCEPTION 'VALIDACION: No existe una evaluación de méritos para esta postulación.';
    END IF;

    IF v_estado_actual != 'FINALIZADA' THEN
        RAISE EXCEPTION 'VALIDACION: Solo se puede reabrir una evaluación con estado FINALIZADA. Estado actual: %', v_estado_actual;
    END IF;

    SELECT id_tipo_estado_evaluacion INTO v_id_estado_borrador
    FROM   postulacion.tipo_estado_evaluacion WHERE codigo = 'BORRADOR' LIMIT 1;

    UPDATE postulacion.evaluacion_meritos
    SET    id_tipo_estado_evaluacion = v_id_estado_borrador
    WHERE  id_evaluacion_meritos     = v_id_evaluacion;

    RETURN jsonb_build_object(
            'exito',   true,
            'mensaje', 'Evaluación reabierta exitosamente. Puedes corregir los puntajes.'
           );

EXCEPTION
    WHEN OTHERS THEN
        IF SQLERRM LIKE 'VALIDACION:%' OR SQLERRM LIKE 'ACCESO:%' THEN RAISE; END IF;
        RETURN jsonb_build_object('exito', false, 'mensaje', '[ERROR] ' || SQLERRM);
END;
$$;


--
-- Name: fn_registrar_puntaje_jurado(integer, integer, numeric, numeric, numeric, boolean); Type: FUNCTION; Schema: postulacion; Owner: -
--

CREATE FUNCTION postulacion.fn_registrar_puntaje_jurado(p_id_evaluacion_oposicion integer, p_id_usuario integer, p_puntaje_material numeric, p_puntaje_exposicion numeric, p_puntaje_respuestas numeric, p_finalizar boolean DEFAULT false) RETURNS jsonb
    LANGUAGE plpgsql SECURITY DEFINER
    AS $$
DECLARE
    v_codigo_estado     TEXT;
    v_finalizo          BOOLEAN;
    v_id_comision       INTEGER;
    v_todos_finalizaron BOOLEAN;
    v_puntaje_total     NUMERIC(5,2);
BEGIN
    -- ── Validación de rangos con RAISE (causa HTTP 400 vía GlobalExceptionHandler) ──
    IF p_puntaje_material IS NULL OR p_puntaje_exposicion IS NULL OR p_puntaje_respuestas IS NULL THEN
        RAISE EXCEPTION 'VALIDACION: Los tres criterios de puntaje son obligatorios.';
    END IF;
    IF p_puntaje_material < 0 OR p_puntaje_material > 10 THEN
        RAISE EXCEPTION 'VALIDACION: El puntaje de material debe estar entre 0.00 y 10.00. Recibido: %', p_puntaje_material;
    END IF;
    IF p_puntaje_exposicion < 0 OR p_puntaje_exposicion > 4 THEN
        RAISE EXCEPTION 'VALIDACION: El puntaje de exposición debe estar entre 0.00 y 4.00. Recibido: %', p_puntaje_exposicion;
    END IF;
    IF p_puntaje_respuestas < 0 OR p_puntaje_respuestas > 6 THEN
        RAISE EXCEPTION 'VALIDACION: El puntaje de respuestas debe estar entre 0.00 y 6.00. Recibido: %', p_puntaje_respuestas;
    END IF;

    -- ── Estado de la evaluación ────────────────────────────────────────
    SELECT tee.codigo
    INTO   v_codigo_estado
    FROM   postulacion.evaluacion_oposicion   eo
               JOIN   postulacion.tipo_estado_evaluacion tee
                      ON tee.id_tipo_estado_evaluacion = eo.id_tipo_estado_evaluacion
    WHERE  eo.id_evaluacion_oposicion = p_id_evaluacion_oposicion;

    IF NOT FOUND THEN
        RAISE EXCEPTION 'VALIDACION: Evaluación con id % no encontrada.', p_id_evaluacion_oposicion;
    END IF;

    IF v_codigo_estado != 'EN_CURSO' THEN
        RAISE EXCEPTION 'VALIDACION: Solo se puede calificar cuando la evaluación está EN CURSO. Estado actual: %', v_codigo_estado;
    END IF;

    -- ── Verificar miembro de comisión ──────────────────────────────────
    SELECT uc.id_comision_seleccion, uc.finalizo_calificacion
    INTO   v_id_comision, v_finalizo
    FROM   seguridad.usuario_comision      uc
               JOIN   postulacion.comision_seleccion  cs ON cs.id_comision_seleccion = uc.id_comision_seleccion
               JOIN   postulacion.postulacion          p  ON p.id_convocatoria       = cs.id_convocatoria
               JOIN   postulacion.evaluacion_oposicion eo ON eo.id_postulacion       = p.id_postulacion
    WHERE  eo.id_evaluacion_oposicion = p_id_evaluacion_oposicion
      AND  uc.id_usuario              = p_id_usuario
      AND  uc.activo                  = TRUE
      AND  cs.activo                  = TRUE
    LIMIT 1;

    IF NOT FOUND THEN
        RAISE EXCEPTION 'ACCESO: El usuario % no pertenece al tribunal de esta convocatoria.', p_id_usuario;
    END IF;

    IF v_finalizo THEN
        RAISE EXCEPTION 'VALIDACION: El jurado ya finalizó su calificación y no puede modificarla.';
    END IF;

    -- ── Guardar notas ──────────────────────────────────────────────────
    UPDATE seguridad.usuario_comision
    SET    puntaje_material        = p_puntaje_material,
           puntaje_exposicion      = p_puntaje_exposicion,
           puntaje_respuestas      = p_puntaje_respuestas,
           id_evaluacion_oposicion = p_id_evaluacion_oposicion,
           fecha_evaluacion        = CURRENT_DATE,
           finalizo_calificacion   = p_finalizar
    WHERE  id_usuario            = p_id_usuario
      AND  id_comision_seleccion = v_id_comision
      AND  activo                = TRUE;

    IF p_finalizar THEN
        SELECT BOOL_AND(uc.finalizo_calificacion) INTO v_todos_finalizaron
        FROM   seguridad.usuario_comision uc
        WHERE  uc.id_comision_seleccion = v_id_comision
          AND  uc.activo = TRUE;

        IF v_todos_finalizaron THEN
            SELECT ROUND(
                           COALESCE(SUM(
                                            COALESCE(uc.puntaje_material,   0) +
                                            COALESCE(uc.puntaje_exposicion, 0) +
                                            COALESCE(uc.puntaje_respuestas, 0)
                                    ), 0) / 3.0
                       , 2)
            INTO   v_puntaje_total
            FROM   seguridad.usuario_comision uc
            WHERE  uc.id_comision_seleccion = v_id_comision
              AND  uc.activo = TRUE;

            UPDATE postulacion.evaluacion_oposicion
            SET    puntaje_total_oposicion   = v_puntaje_total,
                   id_tipo_estado_evaluacion = (
                       SELECT id_tipo_estado_evaluacion
                       FROM   postulacion.tipo_estado_evaluacion
                       WHERE  codigo = 'FINALIZADA'
                   )
            WHERE  id_evaluacion_oposicion = p_id_evaluacion_oposicion;

            RETURN jsonb_build_object(
                    'exito',            true,
                    'mensaje',          'Todos los jurados finalizaron. Nota final calculada.',
                    'todosFinalizaron', true,
                    'puntajeFinal',     v_puntaje_total
                   );
        END IF;
    END IF;

    RETURN jsonb_build_object(
            'exito',            true,
            'mensaje',          CASE WHEN p_finalizar THEN 'Calificación finalizada y bloqueada.'
                                     ELSE 'Puntaje guardado correctamente.' END,
            'todosFinalizaron', false,
            'subtotal',         p_puntaje_material + p_puntaje_exposicion + p_puntaje_respuestas
           );

-- No EXCEPTION genérico aquí: queremos que el RAISE llegue intacto a Spring
END;
$$;


--
-- Name: fn_reporte_global_postulantes(); Type: FUNCTION; Schema: postulacion; Owner: -
--

CREATE FUNCTION postulacion.fn_reporte_global_postulantes() RETURNS TABLE(estudiante text, cedula character varying, asignatura character varying, periodo character varying, estado character varying)
    LANGUAGE plpgsql
    AS $$
BEGIN
    RETURN QUERY
    SELECT 
        u.nombres || ' ' || u.apellidos AS estudiante,
        u.cedula::VARCHAR,
        a.nombre_asignatura::VARCHAR,
        pa.nombre_periodo::VARCHAR,
        COALESCE(tep.nombre, 'PENDIENTE')::VARCHAR AS estado
    FROM postulacion.postulacion p
    JOIN academico.estudiante e ON p.id_estudiante = e.id_estudiante
    JOIN seguridad.usuario u ON e.id_usuario = u.id_usuario
    JOIN convocatoria.convocatoria cv ON p.id_convocatoria = cv.id_convocatoria
    JOIN academico.asignatura a ON cv.id_asignatura = a.id_asignatura
    JOIN academico.periodo_academico pa ON cv.id_periodo_academico = pa.id_periodo_academico
    LEFT JOIN postulacion.tipo_estado_postulacion tep ON p.id_tipo_estado_postulacion = tep.id_tipo_estado_postulacion
    WHERE p.activo = TRUE
    ORDER BY p.fecha_postulacion DESC;
END;
$$;


--
-- Name: fn_resolver_mi_sala(integer); Type: FUNCTION; Schema: postulacion; Owner: -
--

CREATE FUNCTION postulacion.fn_resolver_mi_sala(p_id_usuario integer) RETURNS jsonb
    LANGUAGE plpgsql STABLE SECURITY DEFINER
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


--
-- Name: fn_resolver_sala_usuario(integer); Type: FUNCTION; Schema: postulacion; Owner: -
--

CREATE FUNCTION postulacion.fn_resolver_sala_usuario(p_id_usuario integer) RETURNS jsonb
    LANGUAGE plpgsql STABLE SECURITY DEFINER
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


--
-- Name: fn_subsanar_documento_estudiante(integer, integer, bytea, character varying); Type: FUNCTION; Schema: postulacion; Owner: -
--

CREATE FUNCTION postulacion.fn_subsanar_documento_estudiante(p_id_usuario integer, p_id_requisito_adjunto integer, p_archivo bytea, p_nombre_archivo character varying) RETURNS jsonb
    LANGUAGE plpgsql SECURITY DEFINER
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


--
-- Name: FUNCTION fn_subsanar_documento_estudiante(p_id_usuario integer, p_id_requisito_adjunto integer, p_archivo bytea, p_nombre_archivo character varying); Type: COMMENT; Schema: postulacion; Owner: -
--

COMMENT ON FUNCTION postulacion.fn_subsanar_documento_estudiante(p_id_usuario integer, p_id_requisito_adjunto integer, p_archivo bytea, p_nombre_archivo character varying) IS 'Permite a un estudiante subsanar un documento observado durante el periodo de revisión.
Valida estado y fechas, actualiza el documento y notifica al coordinador.';


--
-- Name: fn_ver_detalle_postulacion(integer); Type: FUNCTION; Schema: postulacion; Owner: -
--

CREATE FUNCTION postulacion.fn_ver_detalle_postulacion(p_id_usuario integer) RETURNS jsonb
    LANGUAGE plpgsql SECURITY DEFINER
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
           OR tep.codigo IS NULL
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


--
-- Name: FUNCTION fn_ver_detalle_postulacion(p_id_usuario integer); Type: COMMENT; Schema: postulacion; Owner: -
--

COMMENT ON FUNCTION postulacion.fn_ver_detalle_postulacion(p_id_usuario integer) IS 'Obtiene el detalle de la postulación activa del estudiante con información de cronograma, documentos y estado de subsanación (24h).';


--
-- Name: sp_finalizar_proceso_seleccion(integer); Type: PROCEDURE; Schema: postulacion; Owner: -
--

CREATE PROCEDURE postulacion.sp_finalizar_proceso_seleccion(IN p_id_convocatoria integer)
    LANGUAGE plpgsql SECURITY DEFINER
    AS $$
DECLARE
    v_cupos              INTEGER;
    v_id_asignatura      INTEGER;
    v_id_periodo         INTEGER;
    v_fecha_inicio_conv  DATE;
    v_fecha_fin_conv     DATE;
    v_id_estado_seleccionado    INTEGER;
    v_id_estado_elegible        INTEGER;
    v_id_estado_no_seleccionado INTEGER;
    v_id_estado_ayudantia_pendiente INTEGER;
    r_ganador RECORD;

BEGIN
    SELECT c.cupos_disponibles,
           c.id_asignatura,
           c.id_periodo_academico,
           pa.fecha_inicio,
           pa.fecha_fin
    INTO   v_cupos, v_id_asignatura, v_id_periodo,
        v_fecha_inicio_conv, v_fecha_fin_conv
    FROM   convocatoria.convocatoria c
               JOIN academico.periodo_academico pa
                    ON pa.id_periodo_academico = c.id_periodo_academico
    WHERE  c.id_convocatoria = p_id_convocatoria
      AND  c.activo          = TRUE;

    IF NOT FOUND THEN
        RAISE EXCEPTION 'VALIDACION: La convocatoria % no existe o no está activa.', p_id_convocatoria;
    END IF;

    SELECT id_tipo_estado_postulacion INTO v_id_estado_seleccionado FROM postulacion.tipo_estado_postulacion WHERE codigo = 'SELECCIONADO';
    SELECT id_tipo_estado_postulacion INTO v_id_estado_elegible FROM postulacion.tipo_estado_postulacion WHERE codigo = 'ELEGIBLE';
    SELECT id_tipo_estado_postulacion INTO v_id_estado_no_seleccionado FROM postulacion.tipo_estado_postulacion WHERE codigo = 'NO_SELECCIONADO';
    SELECT id_tipo_estado_ayudantia INTO v_id_estado_ayudantia_pendiente FROM ayudantia.tipo_estado_ayudantia WHERE codigo = 'PENDIENTE';

    WITH base AS (
        SELECT
            po.id_postulacion,
            u.nombre_usuario,
            u.nombres || ' ' || u.apellidos AS nombre_completo,
            COALESCE(em.nota_total_meritos, 0.0)::NUMERIC(5,2) AS meritos,
            COALESCE(eo_fin.puntaje_total_oposicion, 0.0)::NUMERIC(5,2) AS oposicion,
            ROUND(COALESCE(em.nota_total_meritos, 0.0) + COALESCE(eo_fin.puntaje_total_oposicion, 0.0), 2)::NUMERIC(5,2) AS total
        FROM   postulacion.postulacion po
                   JOIN academico.estudiante est ON est.id_estudiante = po.id_estudiante
                   JOIN seguridad.usuario u ON u.id_usuario = est.id_usuario
                   LEFT JOIN LATERAL (
            SELECT nota_total_meritos FROM postulacion.evaluacion_meritos
            WHERE id_postulacion = po.id_postulacion ORDER BY id_evaluacion_meritos DESC LIMIT 1
            ) em ON TRUE
                   LEFT JOIN LATERAL (
            SELECT eo.puntaje_total_oposicion FROM postulacion.evaluacion_oposicion eo
                                                       JOIN postulacion.tipo_estado_evaluacion tee ON tee.id_tipo_estado_evaluacion = eo.id_tipo_estado_evaluacion
            WHERE eo.id_postulacion = po.id_postulacion AND tee.codigo = 'FINALIZADA'
            ORDER BY eo.id_evaluacion_oposicion DESC LIMIT 1
            ) eo_fin ON TRUE
        WHERE  po.id_convocatoria = p_id_convocatoria AND po.activo = TRUE
    ),
         ranked AS (
             SELECT
                 b.*,
                 RANK() OVER (ORDER BY b.total DESC, b.nombre_completo ASC)::INTEGER AS posicion
             FROM base b
         ),
         clasificados AS (
             SELECT
                 r.*,
                 CASE
                     WHEN r.total < 25.00       THEN v_id_estado_no_seleccionado
                     WHEN r.posicion <= v_cupos THEN v_id_estado_seleccionado
                     ELSE                            v_id_estado_elegible
                     END AS nuevo_id_estado
             FROM ranked r
         )
    UPDATE postulacion.postulacion po
    SET id_tipo_estado_postulacion = cl.nuevo_id_estado
    FROM clasificados cl
    WHERE cl.id_postulacion = po.id_postulacion;

    FOR r_ganador IN
        SELECT po.id_postulacion, u.nombre_usuario
        FROM   postulacion.postulacion po
                   JOIN   academico.estudiante est ON est.id_estudiante = po.id_estudiante
                   JOIN   seguridad.usuario u ON u.id_usuario = est.id_usuario
        WHERE  po.id_convocatoria = p_id_convocatoria
          AND  po.id_tipo_estado_postulacion = v_id_estado_seleccionado
        LOOP
            CALL sp_promover_estudiante_a_ayudante(r_ganador.nombre_usuario, 0);

            INSERT INTO ayudantia.ayudantia (
                id_postulacion, fecha_inicio, fecha_fin, horas_cumplidas,
                id_tipo_estado_ayudantia, horas_semanales_max
            )
            VALUES (
                       r_ganador.id_postulacion, CURRENT_DATE, v_fecha_fin_conv, 0,
                       v_id_estado_ayudantia_pendiente, 20
                   )
            ON CONFLICT (id_postulacion) DO UPDATE
                SET id_tipo_estado_ayudantia = EXCLUDED.id_tipo_estado_ayudantia;
        END LOOP;

EXCEPTION
    WHEN OTHERS THEN
        RAISE EXCEPTION 'ERROR en sp_finalizar_proceso_seleccion: %', SQLERRM;
END;
$$;


--
-- Name: sp_notificar_coordinador_subsanacion(integer, integer, text, text); Type: FUNCTION; Schema: postulacion; Owner: -
--

CREATE FUNCTION postulacion.sp_notificar_coordinador_subsanacion(p_id_postulacion integer, p_id_requisito integer, p_nombre_estudiante text, p_nombre_requisito text) RETURNS integer
    LANGUAGE plpgsql SECURITY DEFINER
    AS $$
DECLARE
    v_id_usuario_coordinador INTEGER;
    v_id_carrera INTEGER;
    v_mensaje TEXT;
    v_id_notificacion INTEGER;
BEGIN
    -- Obtener el id_usuario del coordinador (NO el id_coordinador)
    SELECT co.id_usuario, ca.id_carrera
    INTO v_id_usuario_coordinador, v_id_carrera
    FROM postulacion.postulacion p
    INNER JOIN convocatoria.convocatoria c ON p.id_convocatoria = c.id_convocatoria
    INNER JOIN academico.asignatura a ON c.id_asignatura = a.id_asignatura
    INNER JOIN academico.carrera ca ON a.id_carrera = ca.id_carrera
    INNER JOIN academico.coordinador co ON co.id_carrera = ca.id_carrera AND co.activo = TRUE
    WHERE p.id_postulacion = p_id_postulacion
    LIMIT 1;

    IF v_id_usuario_coordinador IS NULL THEN
        -- Intentar obtener coordinador activo de la carrera
        SELECT co.id_usuario INTO v_id_usuario_coordinador
        FROM academico.coordinador co
        WHERE co.id_carrera = v_id_carrera
        AND co.activo = TRUE
        LIMIT 1;
    END IF;

    IF v_id_usuario_coordinador IS NULL THEN
        RETURN NULL; -- No hay coordinador asignado
    END IF;

    -- Crear mensaje de notificación
    v_mensaje := 'El estudiante ' || p_nombre_estudiante ||
                 ' ha corregido el documento "' || p_nombre_requisito ||
                 '". Pendiente de nueva revisión.';

    -- Insertar notificación (usando id_usuario, no id_coordinador)
    INSERT INTO notificacion.notificacion_ws (
        id_usuario,
        titulo,
        mensaje,
        tipo,
        id_referencia,
        leido,
        fecha_creacion
    ) VALUES (
        v_id_usuario_coordinador,
        'Documento Subsanado',
        v_mensaje,
        'SUBSANACION_DOCUMENTO',
        p_id_postulacion,
        FALSE,
        NOW()
    ) RETURNING id_notificacion INTO v_id_notificacion;

    RETURN v_id_usuario_coordinador;
END;
$$;


--
-- Name: FUNCTION sp_notificar_coordinador_subsanacion(p_id_postulacion integer, p_id_requisito integer, p_nombre_estudiante text, p_nombre_requisito text); Type: COMMENT; Schema: postulacion; Owner: -
--

COMMENT ON FUNCTION postulacion.sp_notificar_coordinador_subsanacion(p_id_postulacion integer, p_id_requisito integer, p_nombre_estudiante text, p_nombre_requisito text) IS 'Crea una notificación para el coordinador cuando un estudiante subsana un documento observado';


--
-- Name: fn_activar_periodo_academico(integer); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.fn_activar_periodo_academico(p_id integer) RETURNS integer
    LANGUAGE plpgsql
    AS $$
DECLARE
    v_existe INTEGER;
BEGIN
    SELECT COUNT(*) INTO v_existe
    FROM academico.periodo_academico
    WHERE id_periodo_academico = p_id;

    IF v_existe = 0 THEN
        RETURN -1; -- No existe
    END IF;

    UPDATE academico.periodo_academico
    SET estado = 'EN PROCESO',
        activo = TRUE
    WHERE id_periodo_academico = p_id;

    RETURN p_id;
END;
$$;


--
-- Name: fn_actualizar_asignatura(integer, integer, character varying, integer); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.fn_actualizar_asignatura(p_id integer, p_id_carrera integer, p_nombre character varying, p_semestre integer) RETURNS integer
    LANGUAGE plpgsql
    AS $$
BEGIN
    IF p_semestre IS NULL OR p_semestre < 1 THEN
        RETURN -1;
    END IF;

    IF NOT EXISTS (
        SELECT 1
          FROM academico.carrera c
         WHERE c.id_carrera = p_id_carrera
           AND COALESCE(c.activo, true) = true
    ) THEN
        RETURN -1;
    END IF;

    UPDATE academico.carrera
       SET id_carrera = p_id_carrera,
           nombre_asignatura = trim(p_nombre),
           semestre = p_semestre
     WHERE id_asignatura = p_id
       AND COALESCE(activo, true) = true;

    IF FOUND THEN
        RETURN 1;
    END IF;

    RETURN -1;
EXCEPTION WHEN OTHERS THEN
    RETURN -1;
END;
$$;


--
-- Name: fn_actualizar_carrera(integer, integer, character varying); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.fn_actualizar_carrera(p_id integer, p_id_facultad integer, p_nombre character varying) RETURNS integer
    LANGUAGE plpgsql
    AS $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
          FROM academico.facultad f
         WHERE f.id_facultad = p_id_facultad
           AND COALESCE(f.activo, true) = true
    ) THEN
        RETURN -1;
    END IF;

    UPDATE academico.carrera
       SET id_facultad = p_id_facultad,
           nombre_carrera = trim(p_nombre)
     WHERE id_carrera = p_id
       AND COALESCE(activo, true) = true;

    IF FOUND THEN
        RETURN 1;
    END IF;

    RETURN -1;
EXCEPTION WHEN OTHERS THEN
    RETURN -1;
END;
$$;


--
-- Name: fn_actualizar_facultad(integer, character varying); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.fn_actualizar_facultad(p_id integer, p_nombre character varying) RETURNS integer
    LANGUAGE plpgsql
    AS $$
BEGIN
    UPDATE academico.facultad
       SET nombre_facultad = trim(p_nombre)
     WHERE id_facultad = p_id
       AND COALESCE(activo, true) = true;

    IF FOUND THEN
        RETURN 1;
    END IF;

    RETURN -1;
EXCEPTION WHEN OTHERS THEN
    RETURN -1;
END;
$$;


--
-- Name: fn_actualizar_periodo_academico(integer, character varying, date, date, character varying); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.fn_actualizar_periodo_academico(p_id integer, p_nombre character varying, p_inicio date, p_fin date, p_estado character varying) RETURNS integer
    LANGUAGE plpgsql
    AS $$
BEGIN
    IF p_inicio IS NULL OR p_fin IS NULL OR p_inicio > p_fin THEN
        RETURN -1;
    END IF;

    UPDATE academico.periodo_academico
       SET nombre_periodo = trim(p_nombre),
           fecha_inicio = p_inicio,
           fecha_fin = p_fin,
           estado = upper(trim(p_estado))
     WHERE id_periodo_academico = p_id
       AND COALESCE(activo, true) = true;

    IF FOUND THEN
        RETURN 1;
    END IF;

    RETURN -1;
EXCEPTION WHEN OTHERS THEN
    RAISE EXCEPTION 'fn_crear_periodo_academico -> % (SQLSTATE: %)', SQLERRM, SQLSTATE;
END;
$$;


--
-- Name: fn_actualizar_tipo_estado_evidencia_ayudantia(integer, character varying, text); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.fn_actualizar_tipo_estado_evidencia_ayudantia(p_id integer, p_nombre_estado character varying, p_descripcion text) RETURNS integer
    LANGUAGE plpgsql
    AS $$
BEGIN
    -- Validación básica: el nombre no debe ser nulo ni vacío
    IF p_nombre_estado IS NULL OR trim(p_nombre_estado) = '' THEN
        RETURN -1;
    END IF;

    UPDATE ayudantia.tipo_estado_evidencia_ayudantia
       SET nombre_estado = trim(p_nombre_estado),
           descripcion = trim(p_descripcion)
     WHERE id_tipo_estado_evidencia_ayudantia = p_id
       AND COALESCE(activo, true) = true;

    IF FOUND THEN
        RETURN 1;
    END IF;

    RETURN -1;
EXCEPTION WHEN OTHERS THEN
    RETURN -1;
END;
$$;


--
-- Name: fn_actualizar_tipo_estado_requisito(integer, character varying, text); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.fn_actualizar_tipo_estado_requisito(p_id integer, p_nombre_estado character varying, p_descripcion text) RETURNS integer
    LANGUAGE plpgsql
    AS $$
BEGIN
    -- Validación básica: el nombre no debe ser nulo ni vacío
    IF p_nombre_estado IS NULL OR trim(p_nombre_estado) = '' THEN
        RETURN -1;
    END IF;

    UPDATE convocatoria.tipo_estado_requisito
       SET nombre_estado = trim(p_nombre_estado),
           descripcion = trim(p_descripcion)
     WHERE id_tipo_estado_requisito = p_id
       AND COALESCE(activo, true) = true;

    IF FOUND THEN
        RETURN 1;
    END IF;

    RETURN -1;
EXCEPTION WHEN OTHERS THEN
    RETURN -1;
END;
$$;


--
-- Name: fn_actualizar_tipo_requisito_postulacion(integer, character varying, text); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.fn_actualizar_tipo_requisito_postulacion(p_id integer, p_nombre_requisito character varying, p_descripcion text) RETURNS integer
    LANGUAGE plpgsql
    AS $$
BEGIN
    -- Validación básica: el nombre del requisito no debe ser nulo ni vacío
    IF p_nombre_requisito IS NULL OR trim(p_nombre_requisito) = '' THEN
        RETURN -1;
    END IF;

    UPDATE convocatoria.tipo_requisito_postulacion
       SET nombre_requisito = trim(p_nombre_requisito),
           descripcion = trim(p_descripcion)
     WHERE id_tipo_requisito_postulacion = p_id
       AND COALESCE(activo, true) = true;

    IF FOUND THEN
        RETURN 1;
    END IF;

    RETURN -1;
EXCEPTION WHEN OTHERS THEN
    RETURN -1;
END;
$$;


--
-- Name: fn_actualizar_tipo_requisito_postulacion(integer, character varying, text, character varying); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.fn_actualizar_tipo_requisito_postulacion(p_id integer, p_nombre character varying, p_descripcion text, p_tipo_documento_permitido character varying DEFAULT NULL::character varying) RETURNS integer
    LANGUAGE plpgsql
    AS $$
BEGIN
    UPDATE convocatoria.tipo_requisito_postulacion
    SET nombre_requisito = p_nombre,
        descripcion = p_descripcion,
        tipo_documento_permitido = p_tipo_documento_permitido
    WHERE id_tipo_requisito_postulacion = p_id;
    
    RETURN p_id;
END;
$$;


--
-- Name: fn_actualizar_tipo_rol(integer, character varying); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.fn_actualizar_tipo_rol(p_id integer, p_nombre_tipo_rol character varying) RETURNS integer
    LANGUAGE plpgsql
    AS $$
BEGIN
    IF p_nombre_tipo_rol IS NULL OR trim(p_nombre_tipo_rol) = '' THEN
        RETURN -1;
    END IF;

    UPDATE seguridad.tipo_rol
       SET nombre_tipo_rol = trim(p_nombre_tipo_rol)
     WHERE id_tipo_rol = p_id
       AND COALESCE(activo, true) = true;

    IF FOUND THEN
        RETURN 1;
    END IF;

    RETURN -1;
EXCEPTION WHEN OTHERS THEN
    RETURN -1;
END;
$$;


--
-- Name: fn_actualizar_tipo_sancion_ayudante_catedra(integer, character varying); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.fn_actualizar_tipo_sancion_ayudante_catedra(p_id integer, p_nombre_tipo_sancion character varying) RETURNS integer
    LANGUAGE plpgsql
    AS $$
BEGIN
    -- Validación básica: el nombre de la sanción no debe ser nulo ni vacío
    IF p_nombre_tipo_sancion IS NULL OR trim(p_nombre_tipo_sancion) = '' THEN
        RETURN -1;
    END IF;

    UPDATE ayudantia.tipo_sancion_ayudante_catedra
       SET nombre_tipo_sancion = trim(p_nombre_tipo_sancion)
     WHERE id_tipo_sancion_ayudante_catedra = p_id
       AND COALESCE(activo, true) = true;

    IF FOUND THEN
        RETURN 1;
    END IF;

    RETURN -1;
EXCEPTION WHEN OTHERS THEN
    RETURN -1;
END;
$$;


--
-- Name: fn_crear_asignatura(integer, character varying, integer); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.fn_crear_asignatura(p_id_carrera integer, p_nombre character varying, p_semestre integer) RETURNS integer
    LANGUAGE plpgsql
    AS $$
DECLARE
    v_id integer;
BEGIN
    IF p_semestre IS NULL OR p_semestre < 1 THEN
        RETURN -1;
    END IF;

    IF NOT EXISTS (
        SELECT 1
          FROM academico.carrera c
         WHERE c.id_carrera = p_id_carrera
           AND COALESCE(c.activo, true) = true
    ) THEN
        RETURN -1;
    END IF;

    INSERT INTO academico.asignatura (id_carrera, nombre_asignatura, semestre, activo)
    VALUES (p_id_carrera, trim(p_nombre), p_semestre, true)
    RETURNING id_asignatura INTO v_id;

    RETURN v_id;
EXCEPTION WHEN OTHERS THEN
    RETURN -1;
END;
$$;


--
-- Name: fn_crear_carrera(integer, character varying); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.fn_crear_carrera(p_id_facultad integer, p_nombre character varying) RETURNS integer
    LANGUAGE plpgsql
    AS $$
DECLARE
    v_id integer;
BEGIN
    IF NOT EXISTS (
        SELECT 1
          FROM academico.facultad f
         WHERE f.id_facultad = p_id_facultad
           AND COALESCE(f.activo, true) = true
    ) THEN
        RETURN -1;
    END IF;

    INSERT INTO academico.carrera (id_facultad, nombre_carrera, activo)
    VALUES (p_id_facultad, trim(p_nombre), true)
    RETURNING id_carrera INTO v_id;

    RETURN v_id;
EXCEPTION WHEN OTHERS THEN
    RETURN -1;
END;
$$;


--
-- Name: fn_crear_facultad(character varying); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.fn_crear_facultad(p_nombre character varying) RETURNS integer
    LANGUAGE plpgsql
    AS $$
DECLARE
    v_id integer;
BEGIN
    INSERT INTO academico.facultad (nombre_facultad, activo)
    VALUES (trim(p_nombre), true)
    RETURNING id_facultad INTO v_id;

    RETURN v_id;
EXCEPTION WHEN OTHERS THEN
    RETURN -1;
END;
$$;


--
-- Name: fn_crear_periodo_academico(character varying, date, date, character varying); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.fn_crear_periodo_academico(p_nombre character varying, p_inicio date, p_fin date, p_estado character varying) RETURNS integer
    LANGUAGE plpgsql
    AS $$
DECLARE
    v_id integer;
BEGIN
    IF p_inicio IS NULL OR p_fin IS NULL OR p_inicio > p_fin THEN
        RETURN -1;
    END IF;

    INSERT INTO academico.periodo_academico (
        nombre_periodo,
        fecha_inicio,
        fecha_fin,
        estado,
        activo
    )
    VALUES (
        trim(p_nombre),
        p_inicio,
        p_fin,
        upper(trim(p_estado)),
        true
    )
    RETURNING id_periodo_academico INTO v_id;

    RETURN v_id;
EXCEPTION WHEN OTHERS THEN
    RAISE EXCEPTION 'fn_crear_periodo_academico -> % (SQLSTATE: %)', SQLERRM, SQLSTATE;
END;
$$;


--
-- Name: fn_crear_tipo_estado_evidencia_ayudantia(character varying, text); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.fn_crear_tipo_estado_evidencia_ayudantia(p_nombre_estado character varying, p_descripcion text) RETURNS integer
    LANGUAGE plpgsql
    AS $$
DECLARE
    v_id integer;
BEGIN
    IF p_nombre_estado IS NULL OR trim(p_nombre_estado) = '' THEN
        RETURN -1;
    END IF;

    INSERT INTO ayudantia.tipo_estado_evidencia_ayudantia (nombre_estado, descripcion, activo)
    VALUES (trim(p_nombre_estado), trim(p_descripcion), true)
    RETURNING id_tipo_estado_evidencia_ayudantia INTO v_id;

    RETURN v_id;
EXCEPTION WHEN OTHERS THEN
    RETURN -1;
END;
$$;


--
-- Name: fn_crear_tipo_estado_requisito(character varying, text); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.fn_crear_tipo_estado_requisito(p_nombre_estado character varying, p_descripcion text) RETURNS integer
    LANGUAGE plpgsql
    AS $$
DECLARE
    v_id integer;
BEGIN
    IF p_nombre_estado IS NULL OR trim(p_nombre_estado) = '' THEN
        RETURN -1;
    END IF;

    INSERT INTO convocatoria.tipo_estado_requisito (nombre_estado, descripcion, activo)
    VALUES (trim(p_nombre_estado), trim(p_descripcion), true)
    RETURNING id_tipo_estado_requisito INTO v_id;

    RETURN v_id;
EXCEPTION WHEN OTHERS THEN
    RETURN -1;
END;
$$;


--
-- Name: fn_crear_tipo_requisito_postulacion(character varying, text); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.fn_crear_tipo_requisito_postulacion(p_nombre_requisito character varying, p_descripcion text) RETURNS integer
    LANGUAGE plpgsql
    AS $$
DECLARE
    v_id integer;
BEGIN
    IF p_nombre_requisito IS NULL OR trim(p_nombre_requisito) = '' THEN
        RETURN -1;
    END IF;

    INSERT INTO convocatoria.tipo_requisito_postulacion (nombre_requisito, descripcion, activo)
    VALUES (trim(p_nombre_requisito), trim(p_descripcion), true)
    RETURNING id_tipo_requisito_postulacion INTO v_id;

    RETURN v_id;
EXCEPTION WHEN OTHERS THEN
    RETURN -1;
END;
$$;


--
-- Name: fn_crear_tipo_requisito_postulacion(character varying, text, character varying); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.fn_crear_tipo_requisito_postulacion(p_nombre character varying, p_descripcion text, p_tipo_documento_permitido character varying DEFAULT NULL::character varying) RETURNS integer
    LANGUAGE plpgsql
    AS $$
DECLARE
    v_id integer;
BEGIN
    INSERT INTO convocatoria.tipo_requisito_postulacion (nombre_requisito, descripcion, activo, tipo_documento_permitido)
    VALUES (p_nombre, p_descripcion, true, p_tipo_documento_permitido)
    RETURNING id_tipo_requisito_postulacion INTO v_id;
    
    RETURN v_id;
END;
$$;


--
-- Name: fn_crear_tipo_rol(character varying); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.fn_crear_tipo_rol(p_nombre_tipo_rol character varying) RETURNS integer
    LANGUAGE plpgsql SECURITY DEFINER
    AS $$
DECLARE
    v_id_tipo_rol integer;
    v_id_rol_bd integer;
    v_nombre_rol_bd text;
BEGIN
    IF p_nombre_tipo_rol IS NULL OR trim(p_nombre_tipo_rol) = '' THEN
        RETURN -1;
    END IF;

    v_nombre_rol_bd := 'role_' || lower(trim(p_nombre_tipo_rol));

    IF NOT EXISTS (SELECT FROM pg_catalog.pg_roles WHERE rolname = v_nombre_rol_bd) THEN
        EXECUTE 'CREATE ROLE ' || quote_ident(v_nombre_rol_bd) || ' NOLOGIN';
    END IF;

    SELECT id_rol_bd INTO v_id_rol_bd 
    FROM seguridad.rol_bd 
    WHERE nombre_rol_bd = v_nombre_rol_bd;

    IF v_id_rol_bd IS NULL THEN
        INSERT INTO seguridad.rol_bd (nombre_rol_bd, descripcion)
        VALUES (v_nombre_rol_bd, 'Rol físico para ' || trim(p_nombre_tipo_rol))
        RETURNING id_rol_bd INTO v_id_rol_bd;
    END IF;

    INSERT INTO seguridad.tipo_rol (nombre_tipo_rol, activo, id_rol_bd)
    VALUES (trim(p_nombre_tipo_rol), true, v_id_rol_bd)
    RETURNING id_tipo_rol INTO v_id_tipo_rol;

    RETURN v_id_tipo_rol;
EXCEPTION WHEN OTHERS THEN
    RAISE EXCEPTION 'Error creando rol: %', SQLERRM;
END;
$$;


--
-- Name: fn_crear_tipo_sancion_ayudante_catedra(character varying); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.fn_crear_tipo_sancion_ayudante_catedra(p_nombre_tipo_sancion character varying) RETURNS integer
    LANGUAGE plpgsql
    AS $$
DECLARE
    v_id integer;
BEGIN
    IF p_nombre_tipo_sancion IS NULL OR trim(p_nombre_tipo_sancion) = '' THEN
        RETURN -1;
    END IF;

    INSERT INTO ayudantia.tipo_sancion_ayudante_catedra (nombre_tipo_sancion, activo)
    VALUES (trim(p_nombre_tipo_sancion), true)
    RETURNING id_tipo_sancion_ayudante_catedra INTO v_id;

    RETURN v_id;
EXCEPTION WHEN OTHERS THEN
    RETURN -1;
END;
$$;


--
-- Name: fn_desactivar_asignatura(integer); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.fn_desactivar_asignatura(p_id integer) RETURNS integer
    LANGUAGE plpgsql
    AS $$
BEGIN
    UPDATE academico.asignatura
       SET activo = false
     WHERE id_asignatura = p_id
       AND COALESCE(activo, true) = true;

    IF FOUND THEN
        RETURN 1;
    END IF;

    RETURN -1;
EXCEPTION WHEN OTHERS THEN
    RETURN -1;
END;
$$;


--
-- Name: fn_desactivar_carrera(integer); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.fn_desactivar_carrera(p_id integer) RETURNS integer
    LANGUAGE plpgsql
    AS $$
BEGIN
    UPDATE academico.carrera
       SET activo = false
     WHERE id_carrera = p_id
       AND COALESCE(activo, true) = true;

    IF FOUND THEN
        RETURN 1;
    END IF;

    RETURN -1;
EXCEPTION WHEN OTHERS THEN
    RETURN -1;
END;
$$;


--
-- Name: fn_desactivar_facultad(integer); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.fn_desactivar_facultad(p_id integer) RETURNS integer
    LANGUAGE plpgsql
    AS $$
BEGIN
    UPDATE academico.facultad
       SET activo = false
     WHERE id_facultad = p_id
       AND COALESCE(activo, true) = true;

    IF FOUND THEN
        RETURN 1;
    END IF;

    RETURN -1;
EXCEPTION WHEN OTHERS THEN
    RETURN -1;
END;
$$;


--
-- Name: fn_desactivar_periodo_academico(integer); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.fn_desactivar_periodo_academico(p_id integer) RETURNS integer
    LANGUAGE plpgsql
    AS $$
BEGIN
    UPDATE academico.periodo_academico
       SET estado = 'INACTIVO',
           activo = false
     WHERE id_periodo_academico = p_id
       AND COALESCE(activo, true) = true;

    IF FOUND THEN
        RETURN 1;
    END IF;

    RETURN -1;
EXCEPTION WHEN OTHERS THEN
    RAISE EXCEPTION 'fn_crear_periodo_academico -> % (SQLSTATE: %)', SQLERRM, SQLSTATE;
END;
$$;


--
-- Name: fn_desactivar_tipo_estado_evidencia_ayudantia(integer); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.fn_desactivar_tipo_estado_evidencia_ayudantia(p_id integer) RETURNS integer
    LANGUAGE plpgsql
    AS $$
BEGIN
    UPDATE ayudantia.tipo_estado_evidencia_ayudantia
       SET activo = false
     WHERE id_tipo_estado_evidencia_ayudantia = p_id
       AND COALESCE(activo, true) = true;

    IF FOUND THEN
        RETURN 1;
    END IF;

    RETURN -1;
EXCEPTION WHEN OTHERS THEN
    RETURN -1;
END;
$$;


--
-- Name: fn_desactivar_tipo_estado_requisito(integer); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.fn_desactivar_tipo_estado_requisito(p_id integer) RETURNS integer
    LANGUAGE plpgsql
    AS $$
BEGIN
    UPDATE convocatoria.tipo_estado_requisito
       SET activo = false
     WHERE id_tipo_estado_requisito = p_id
       AND COALESCE(activo, true) = true;

    IF FOUND THEN
        RETURN 1;
    END IF;

    RETURN -1;
EXCEPTION WHEN OTHERS THEN
    RETURN -1;
END;
$$;


--
-- Name: fn_desactivar_tipo_requisito_postulacion(integer); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.fn_desactivar_tipo_requisito_postulacion(p_id integer) RETURNS integer
    LANGUAGE plpgsql
    AS $$
BEGIN
    UPDATE convocatoria.tipo_requisito_postulacion
       SET activo = false
     WHERE id_tipo_requisito_postulacion = p_id
       AND COALESCE(activo, true) = true;

    IF FOUND THEN
        RETURN 1;
    END IF;

    RETURN -1;
EXCEPTION WHEN OTHERS THEN
    RETURN -1;
END;
$$;


--
-- Name: fn_desactivar_tipo_rol(integer); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.fn_desactivar_tipo_rol(p_id integer) RETURNS integer
    LANGUAGE plpgsql
    AS $$
BEGIN
    UPDATE seguridad.tipo_rol
       SET activo = false
     WHERE id_tipo_rol = p_id
       AND COALESCE(activo, true) = true;

    IF FOUND THEN
        RETURN 1;
    END IF;

    RETURN -1;
EXCEPTION WHEN OTHERS THEN
    RETURN -1;
END;
$$;


--
-- Name: fn_desactivar_tipo_sancion_ayudante_catedra(integer); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.fn_desactivar_tipo_sancion_ayudante_catedra(p_id integer) RETURNS integer
    LANGUAGE plpgsql
    AS $$
BEGIN
    UPDATE ayudantia.tipo_sancion_ayudante_catedra
       SET activo = false
     WHERE id_tipo_sancion_ayudante_catedra = p_id
       AND COALESCE(activo, true) = true;

    IF FOUND THEN
        RETURN 1;
    END IF;

    RETURN -1;
EXCEPTION WHEN OTHERS THEN
    RETURN -1;
END;
$$;


--
-- Name: fn_inactivar_periodos_vencidos(); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.fn_inactivar_periodos_vencidos() RETURNS integer
    LANGUAGE plpgsql
    AS $$
DECLARE
    v_filas_afectadas INTEGER;
BEGIN
    UPDATE academico.periodo_academico
    SET estado = 'INACTIVO',
        activo = FALSE
    WHERE fecha_fin < CURRENT_DATE
      AND estado = 'EN PROCESO';

    GET DIAGNOSTICS v_filas_afectadas = ROW_COUNT;
    RETURN v_filas_afectadas;
END;
$$;


--
-- Name: fn_login_sgac(character varying, character varying); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.fn_login_sgac(p_usuario character varying, p_contrasenia character varying) RETURNS TABLE(id_usuario integer, nombres character varying, apellidos character varying, correo character varying, nombre_usuario character varying, roles character varying)
    LANGUAGE plpgsql SECURITY DEFINER
    SET search_path TO 'public', 'seguridad'
    AS $$
BEGIN
    RETURN QUERY
    SELECT 
        u.id_usuario,
        u.nombres,
        u.apellidos,
        u.correo,
        u.nombre_usuario,
        COALESCE(
            string_agg(tr.nombre_tipo_rol, ',' ORDER BY utr.fecha_creacion ASC),
            ''
        )::character varying AS roles
    FROM seguridad.usuario u
    LEFT JOIN seguridad.usuario_tipo_rol utr 
        ON utr.id_usuario = u.id_usuario 
        AND utr.activo = TRUE
    LEFT JOIN seguridad.tipo_rol tr 
        ON tr.id_tipo_rol = utr.id_tipo_rol 
        AND tr.activo = TRUE
    WHERE 
        (u.nombre_usuario = p_usuario OR u.correo = p_usuario)
        AND u.activo = TRUE
        AND u.contrasenia_usuario = crypt(p_contrasenia, u.contrasenia_usuario)
    GROUP BY 
        u.id_usuario, 
        u.nombres, 
        u.apellidos, 
        u.correo, 
        u.nombre_usuario;
END;
$$;


--
-- Name: fn_notif_cambio_estado_postulacion(); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.fn_notif_cambio_estado_postulacion() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
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
$$;


--
-- Name: fn_permisos_actuales(); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.fn_permisos_actuales() RETURNS TABLE(rol text, objeto text, permiso text)
    LANGUAGE plpgsql
    AS $$
BEGIN
    RETURN QUERY
    SELECT 
        grantee::text AS rol,
        table_name::text AS objeto,
        privilege_type::text AS permiso
    FROM information_schema.role_table_grants
    WHERE grantee = current_user;
END;
$$;


--
-- Name: fn_validar_estado_ayudantia(integer); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.fn_validar_estado_ayudantia(p_id_ayudantia integer) RETURNS public.resultado_validacion
    LANGUAGE plpgsql
    AS $$
DECLARE
    v_id_estado_ayudantia INTEGER;
    v_nombre_estado       VARCHAR(50);
    v_estado_periodo      VARCHAR(30);
    v_resultado           public.resultado_validacion;
BEGIN
    SELECT a.id_tipo_estado_ayudantia, tea.nombre_estado
    INTO v_id_estado_ayudantia, v_nombre_estado
    FROM ayudantia.ayudantia a
             INNER JOIN ayudantia.tipo_estado_ayudantia tea
                        ON tea.id_tipo_estado_ayudantia = a.id_tipo_estado_ayudantia
    WHERE a.id_ayudantia = p_id_ayudantia;

    IF NOT FOUND THEN
        v_resultado.valido  := false;
        v_resultado.mensaje := 'La ayudantía no existe.';
        RETURN v_resultado;
    END IF;

    IF v_id_estado_ayudantia <> 21 THEN
        v_resultado.valido  := false;
        v_resultado.mensaje := FORMAT('La ayudantía no está activa. Estado actual: %s.', v_nombre_estado);
        RETURN v_resultado;
    END IF;

    -- 2. Validar estado del periodo académico
    SELECT pa.estado INTO v_estado_periodo
    FROM ayudantia.ayudantia a
             INNER JOIN postulacion.postulacion pp ON pp.id_postulacion = a.id_postulacion
             INNER JOIN convocatoria.convocatoria co ON co.id_convocatoria = pp.id_convocatoria
             INNER JOIN academico.periodo_academico pa ON pa.id_periodo_academico = co.id_periodo_academico
    WHERE a.id_ayudantia = p_id_ayudantia;

    IF TRIM(UPPER(v_estado_periodo)) <> 'EN PROCESO' THEN
        v_resultado.valido  := false;
        v_resultado.mensaje := 'El periodo académico no está en proceso.';
        RETURN v_resultado;
    END IF;

    v_resultado.valido  := true;
    v_resultado.mensaje := 'OK';
    RETURN v_resultado;
END;
$$;


--
-- Name: fn_validar_fecha_actividad(integer, date); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.fn_validar_fecha_actividad(p_id_ayudantia integer, p_fecha date) RETURNS public.resultado_validacion
    LANGUAGE plpgsql
    AS $$
DECLARE
    v_fecha_inicio      DATE;
    v_fecha_fin_limite  DATE;
    v_resultado         public.resultado_validacion;
BEGIN
    SELECT
        a.fecha_inicio,
        COALESCE(a.fecha_fin, fc.fecha_fin) -- Prioriza la fecha fin de la tabla, sino usa la de la fase
    INTO v_fecha_inicio, v_fecha_fin_limite
    FROM ayudantia.ayudantia a
             INNER JOIN postulacion.postulacion pp ON pp.id_postulacion = a.id_postulacion
             INNER JOIN convocatoria.convocatoria co ON co.id_convocatoria = pp.id_convocatoria
             INNER JOIN planificacion.periodo_fase fc ON fc.id_periodo_academico = co.id_periodo_academico
            INNER JOIN planificacion.tipo_fase tf on tf.id_tipo_fase = fc.id_tipo_fase
    WHERE a.id_ayudantia = p_id_ayudantia
      AND tf.codigo = 'EJECUCION_ACTIVIDADES';

    IF NOT FOUND THEN
        v_resultado.valido := false;
        v_resultado.mensaje := 'No se pudo determinar el rango de validez (Fase EJECUCION_ACTIVIDADES no encontrada).';
        RETURN v_resultado;
    END IF;

    IF p_fecha > CURRENT_DATE THEN
        v_resultado.valido := false;
        v_resultado.mensaje := 'No se puede registrar una actividad con fecha futura.';
        RETURN v_resultado;
    END IF;

    -- 3. Validación: Rango permitido (Desde inicio de ayudantía hasta fin de fase)
    IF p_fecha < v_fecha_inicio OR p_fecha > v_fecha_fin_limite THEN
        v_resultado.valido := false;
        v_resultado.mensaje := FORMAT(
                'La fecha %s está fuera del periodo permitido. Su ayudantía inició el %s y la ejecución termina el %s.',
                TO_CHAR(p_fecha, 'DD/MM/YYYY'),
                TO_CHAR(v_fecha_inicio, 'DD/MM/YYYY'),
                TO_CHAR(v_fecha_fin_limite, 'DD/MM/YYYY')
                               );
        RETURN v_resultado;
    END IF;

    -- 4. Validación: Registro duplicado para la misma fecha
    PERFORM 1
    FROM ayudantia.registro_actividad
    WHERE id_ayudantia = p_id_ayudantia
      AND fecha = p_fecha;

    IF FOUND THEN
        v_resultado.valido := false;
        v_resultado.mensaje := FORMAT(
                'Ya existe un registro de actividad para la fecha %s.',
                TO_CHAR(p_fecha, 'DD/MM/YYYY')
                               );
        RETURN v_resultado;
    END IF;

    -- 5. Respuesta exitosa
    v_resultado.valido := true;
    v_resultado.mensaje := 'OK';
    RETURN v_resultado;
END;
$$;


--
-- Name: sp_actividades_ayudante_docente(integer); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.sp_actividades_ayudante_docente(p_id_ayudantia integer) RETURNS TABLE(id_registro_actividad integer, descripcion_actividad text, tema_tratado text, fecha date, numero_asistentes integer, horas_dedicadas numeric, estado_revision character varying, observaciones character varying, fecha_observacion date)
    LANGUAGE sql STABLE
    AS $$
    SELECT
        ra.id_registro_actividad,
        ra.descripcion_actividad,
        ra.tema_tratado,
        ra.fecha,
        ra.numero_asistentes,
        ra.horas_dedicadas,
        ter.nombre_estado     AS estado_revision,
        ra.observaciones,
        ra.fecha_observacion
    FROM ayudantia.registro_actividad ra
    JOIN ayudantia.tipo_estado_registro ter
                              ON ter.id_tipo_estado_registro = ra.id_tipo_estado_registro
    WHERE ra.id_ayudantia = p_id_ayudantia
    ORDER BY ra.fecha DESC, ra.id_registro_actividad DESC;
$$;


--
-- Name: sp_actualizar_asignatura(integer, integer, character varying, integer); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.sp_actualizar_asignatura(p_id integer, p_id_carrera integer, p_nombre character varying, p_semestre integer) RETURNS integer
    LANGUAGE plpgsql
    AS $$
BEGIN
    RETURN public.fn_actualizar_asignatura(p_id, p_id_carrera, p_nombre, p_semestre);
END; $$;


--
-- Name: sp_actualizar_ayudante_catedra(integer, integer, numeric); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.sp_actualizar_ayudante_catedra(p_id_ayudante_catedra integer, p_id_usuario integer, p_horas_ayudante numeric) RETURNS integer
    LANGUAGE plpgsql
    AS $$
BEGIN
    UPDATE public.ayudante_catedra
    SET id_usuario = p_id_usuario,
        horas_ayudante = p_horas_ayudante
    WHERE id_ayudante_catedra = p_id_ayudante_catedra;

    IF NOT FOUND THEN
        RETURN -1;
    END IF;

    RETURN p_id_ayudante_catedra;

EXCEPTION WHEN OTHERS THEN
    RAISE NOTICE 'Error: %', SQLERRM;
    RETURN -1;
END;
$$;


--
-- Name: sp_actualizar_ayudantia(integer, integer, date, date, integer); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.sp_actualizar_ayudantia(p_id_ayudantia integer, p_id_tipo_estado integer, p_fecha_inicio date, p_fecha_fin date, p_horas_cumplidas integer) RETURNS integer
    LANGUAGE plpgsql
    AS $$
BEGIN
    UPDATE public.ayudantia
    SET id_tipo_estado_evidencia_ayudantia = p_id_tipo_estado,
        fecha_inicio = p_fecha_inicio,
        fecha_fin = p_fecha_fin,
        horas_cumplidas = p_horas_cumplidas
    WHERE id_ayudantia = p_id_ayudantia;

    RETURN p_id_ayudantia;

EXCEPTION WHEN OTHERS THEN
    RAISE NOTICE 'Error: %', SQLERRM;
    RETURN -1;
END;
$$;


--
-- Name: sp_actualizar_carrera(integer, integer, character varying); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.sp_actualizar_carrera(p_id integer, p_id_facultad integer, p_nombre character varying) RETURNS integer
    LANGUAGE plpgsql
    AS $$
BEGIN
    RETURN public.fn_actualizar_carrera(p_id, p_id_facultad, p_nombre);
END; $$;


--
-- Name: sp_actualizar_certificado(integer, character varying, bytea); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.sp_actualizar_certificado(p_id_certificado integer, p_estado character varying, p_archivo bytea) RETURNS integer
    LANGUAGE plpgsql
    AS $$
BEGIN
    UPDATE public.certificado
    SET estado = p_estado,
        archivo = p_archivo
    WHERE id_certificado = p_id_certificado;

    RETURN p_id_certificado;

EXCEPTION WHEN OTHERS THEN
    RAISE NOTICE 'Error: %', SQLERRM;
    RETURN -1;
END;
$$;


--
-- Name: sp_actualizar_comision(integer, character varying, date); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.sp_actualizar_comision(p_id integer, p_nombre_comision character varying, p_fecha_conformacion date) RETURNS integer
    LANGUAGE plpgsql
    AS $$
BEGIN
    UPDATE postulacion.comision_seleccion
    SET nombre_comision    = p_nombre_comision,
        fecha_conformacion = p_fecha_conformacion
    WHERE id_comision_seleccion = p_id;
    RETURN p_id;
EXCEPTION
    WHEN OTHERS THEN RETURN -1;
END;
$$;


--
-- Name: sp_actualizar_convocatoria(integer, integer, date, character varying); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.sp_actualizar_convocatoria(p_id_convocatoria integer, p_cupos integer, p_fecha_cierre date, p_estado character varying) RETURNS integer
    LANGUAGE plpgsql
    AS $$
BEGIN
    UPDATE public.convocatoria
    SET cupos_disponibles = p_cupos,
        fecha_cierre = p_fecha_cierre,
        estado = p_estado
    WHERE id_convocatoria = p_id_convocatoria;

    RETURN p_id_convocatoria;

EXCEPTION WHEN OTHERS THEN
    RAISE NOTICE 'Error: %', SQLERRM;
    RETURN -1;
END;
$$;


--
-- Name: sp_actualizar_coordinador(integer, integer, date, date); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.sp_actualizar_coordinador(p_id_coordinador integer, p_id_carrera integer, p_fecha_inicio date, p_fecha_fin date) RETURNS integer
    LANGUAGE plpgsql
    AS $$
BEGIN
    UPDATE public.coordinador
    SET id_carrera = p_id_carrera,
        fecha_inicio = p_fecha_inicio,
        fecha_fin = p_fecha_fin
    WHERE id_coordinador = p_id_coordinador;

    RETURN p_id_coordinador;

EXCEPTION WHEN OTHERS THEN
    RAISE NOTICE 'Error: %', SQLERRM;
    RETURN -1;
END;
$$;


--
-- Name: sp_actualizar_decano(integer, integer, date, date); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.sp_actualizar_decano(p_id_decano integer, p_id_facultad integer, p_fecha_inicio_gestion date, p_fecha_fin_gestion date) RETURNS integer
    LANGUAGE plpgsql
    AS $$
BEGIN
    UPDATE public.decano
    SET id_facultad = p_id_facultad,
        fecha_inicio_gestion = p_fecha_inicio_gestion,
        fecha_fin_gestion = p_fecha_fin_gestion
    WHERE id_decano = p_id_decano;

    RETURN p_id_decano;

EXCEPTION WHEN OTHERS THEN
    RAISE NOTICE 'Error: %', SQLERRM;
    RETURN -1;
END;
$$;


--
-- Name: sp_actualizar_docente(integer, date, date); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.sp_actualizar_docente(p_id_docente integer, p_fecha_inicio date, p_fecha_fin date) RETURNS integer
    LANGUAGE plpgsql
    AS $$
BEGIN
    UPDATE public.docente
    SET fecha_inicio = p_fecha_inicio,
        fecha_fin = p_fecha_fin
    WHERE id_docente = p_id_docente;

    RETURN p_id_docente;

EXCEPTION WHEN OTHERS THEN
    RAISE NOTICE 'Error: %', SQLERRM;
    RETURN -1;
END;
$$;


--
-- Name: sp_actualizar_docente_asignatura(integer, integer); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.sp_actualizar_docente_asignatura(p_id_docente_asignatura integer, p_id_asignatura integer) RETURNS integer
    LANGUAGE plpgsql
    AS $$
BEGIN
    UPDATE public.docente_asignatura
    SET id_asignatura = p_id_asignatura
    WHERE id_docente_asignatura = p_id_docente_asignatura;

    RETURN p_id_docente_asignatura;

EXCEPTION WHEN OTHERS THEN
    RAISE NOTICE 'Error: %', SQLERRM;
    RETURN -1;
END;
$$;


--
-- Name: sp_actualizar_estado_rol_usuario(integer, integer, boolean); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.sp_actualizar_estado_rol_usuario(p_id_usuario integer, p_id_tipo_rol integer, p_activo boolean) RETURNS integer
    LANGUAGE plpgsql
    AS $$
BEGIN
    UPDATE public.usuario_tipo_rol
    SET activo = p_activo
    WHERE id_usuario = p_id_usuario AND id_tipo_rol = p_id_tipo_rol;

    RETURN p_id_usuario;

EXCEPTION WHEN OTHERS THEN
    RAISE NOTICE 'Error: %', SQLERRM;
    RETURN -1;
END;
$$;


--
-- Name: sp_actualizar_estudiante(integer, integer, integer, character varying); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.sp_actualizar_estudiante(p_id_estudiante integer, p_id_carrera integer, p_semestre integer, p_estado_academico character varying) RETURNS integer
    LANGUAGE plpgsql
    AS $$
BEGIN
    UPDATE public.estudiante
    SET id_carrera = p_id_carrera,
        semestre = p_semestre,
        estado_academico = p_estado_academico
    WHERE id_estudiante = p_id_estudiante;

    RETURN p_id_estudiante;

EXCEPTION WHEN OTHERS THEN
    RAISE NOTICE 'Error: %', SQLERRM;
    RETURN -1;
END;
$$;


--
-- Name: sp_actualizar_evaluacion_meritos(integer, numeric, numeric, numeric, numeric); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.sp_actualizar_evaluacion_meritos(p_id_evaluacion integer, p_nota_asignatura numeric, p_nota_semestres numeric, p_nota_eventos numeric, p_nota_experiencia numeric) RETURNS integer
    LANGUAGE plpgsql
    AS $$
BEGIN
    UPDATE public.evaluacion_meritos
    SET nota_asignatura = p_nota_asignatura,
        nota_semestres = p_nota_semestres,
        nota_eventos = p_nota_eventos,
        nota_experiencia = p_nota_experiencia
    WHERE id_evaluacion_meritos = p_id_evaluacion;

    RETURN p_id_evaluacion;

EXCEPTION WHEN OTHERS THEN
    RAISE NOTICE 'Error: %', SQLERRM;
    RETURN -1;
END;
$$;


--
-- Name: sp_actualizar_evaluacion_oposicion(integer, character varying, date, time without time zone, time without time zone, character varying, character varying); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.sp_actualizar_evaluacion_oposicion(p_id integer, p_tema_exposicion character varying, p_fecha_evaluacion date, p_hora_inicio time without time zone, p_hora_fin time without time zone, p_lugar character varying, p_estado character varying) RETURNS integer
    LANGUAGE plpgsql
    AS $$
BEGIN
    UPDATE postulacion.evaluacion_oposicion
    SET tema_exposicion = COALESCE(p_tema_exposicion, tema_exposicion),
        fecha_evaluacion = COALESCE(p_fecha_evaluacion, fecha_evaluacion),
        hora_inicio = COALESCE(p_hora_inicio, hora_inicio),
        hora_fin = COALESCE(p_hora_fin, hora_fin),
        lugar = COALESCE(p_lugar, lugar),
        estado = COALESCE(p_estado, estado)
    WHERE id_evaluacion_oposicion = p_id;
    RETURN p_id;
EXCEPTION
    WHEN OTHERS THEN RETURN -1;
END;
$$;


--
-- Name: sp_actualizar_evidencia_actividad(integer, character varying, character varying, bytea); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.sp_actualizar_evidencia_actividad(p_id_evidencia integer, p_tipo_evidencia character varying, p_nombre_archivo character varying, p_archivo bytea) RETURNS integer
    LANGUAGE plpgsql
    AS $$
BEGIN
    UPDATE public.evidencia_registro_actividad
    SET tipo_evidencia = p_tipo_evidencia,
        nombre_archivo = p_nombre_archivo,
        archivo = p_archivo
    WHERE id_evidencia_registro_actividad = p_id_evidencia;

    RETURN p_id_evidencia;

EXCEPTION WHEN OTHERS THEN
    RAISE NOTICE 'Error: %', SQLERRM;
    RETURN -1;
END;
$$;


--
-- Name: sp_actualizar_facultad(integer, character varying); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.sp_actualizar_facultad(p_id integer, p_nombre character varying) RETURNS integer
    LANGUAGE plpgsql
    AS $$
BEGIN
    RETURN public.fn_actualizar_facultad(p_id, p_nombre);
END; $$;


--
-- Name: sp_actualizar_log_auditoria(integer, character varying, text); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.sp_actualizar_log_auditoria(p_id_log integer, p_accion character varying, p_valor_nuevo text) RETURNS integer
    LANGUAGE plpgsql
    AS $$
BEGIN
    UPDATE public.log_auditoria
    SET accion = p_accion,
        valor_nuevo = p_valor_nuevo
    WHERE id_log_auditoria = p_id_log;

    RETURN p_id_log;

EXCEPTION WHEN OTHERS THEN
    RAISE NOTICE 'Error: %', SQLERRM;
    RETURN -1;
END;
$$;


--
-- Name: sp_actualizar_periodo_academico(integer, character varying, date, date, character varying); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.sp_actualizar_periodo_academico(p_id integer, p_nombre character varying, p_inicio date, p_fin date, p_estado character varying) RETURNS integer
    LANGUAGE plpgsql
    AS $$
BEGIN
    RETURN public.fn_actualizar_periodo_academico(p_id, p_nombre, p_inicio, p_fin, p_estado);
END; $$;


--
-- Name: sp_actualizar_periodo_requisito(integer, boolean, integer); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.sp_actualizar_periodo_requisito(p_id_relacion integer, p_obligatorio boolean, p_orden integer) RETURNS integer
    LANGUAGE plpgsql
    AS $$
BEGIN
    UPDATE public.periodo_academico_requisito_postulacion
    SET obligatorio = p_obligatorio,
        orden = p_orden
    WHERE id_periodo_academico_requisito_postulacion = p_id_relacion;

    RETURN p_id_relacion;

EXCEPTION WHEN OTHERS THEN
    RAISE NOTICE 'Error: %', SQLERRM;
    RETURN -1;
END;
$$;


--
-- Name: sp_actualizar_postulacion(integer, character varying, character varying); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.sp_actualizar_postulacion(p_id_postulacion integer, p_estado_postulacion character varying, p_observaciones character varying) RETURNS integer
    LANGUAGE plpgsql
    AS $$
BEGIN
    UPDATE postulacion.postulacion
    SET estado_postulacion = p_estado_postulacion,
        observaciones = p_observaciones
    WHERE id_postulacion = p_id_postulacion;

    RETURN p_id_postulacion;

EXCEPTION WHEN OTHERS THEN
    RAISE NOTICE 'Error: %', SQLERRM;
    RETURN -1;
END;
$$;


--
-- Name: sp_actualizar_registro_actividad(integer, text, text, numeric, character varying); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.sp_actualizar_registro_actividad(p_id_registro integer, p_descripcion text, p_tema_tratado text, p_horas_dedicadas numeric, p_estado_revision character varying) RETURNS integer
    LANGUAGE plpgsql
    AS $$
BEGIN
    UPDATE public.registro_actividad
    SET descripcion_actividad = p_descripcion,
        tema_tratado = p_tema_tratado,
        horas_dedicadas = p_horas_dedicadas,
        estado_revision = p_estado_revision
    WHERE id_registro_actividad = p_id_registro;

    RETURN p_id_registro;

EXCEPTION WHEN OTHERS THEN
    RAISE NOTICE 'Error: %', SQLERRM;
    RETURN -1;
END;
$$;


--
-- Name: sp_actualizar_requisito_adjunto(integer, integer, text); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.sp_actualizar_requisito_adjunto(p_id_requisito integer, p_id_tipo_estado integer, p_observacion text) RETURNS integer
    LANGUAGE plpgsql
    AS $$
BEGIN
    UPDATE public.requisito_adjunto
    SET id_tipo_estado_requisito = p_id_tipo_estado,
        observacion = p_observacion
    WHERE id_requisito_adjunto = p_id_requisito;

    RETURN p_id_requisito;

EXCEPTION WHEN OTHERS THEN
    RAISE NOTICE 'Error: %', SQLERRM;
    RETURN -1;
END;
$$;


--
-- Name: sp_actualizar_sancion(integer, character varying, date); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.sp_actualizar_sancion(p_id_sancion integer, p_motivo character varying, p_fecha_sancion date) RETURNS integer
    LANGUAGE plpgsql
    AS $$
BEGIN
    UPDATE public.sancion_ayudante_catedra
    SET motivo = p_motivo,
        fecha_sancion = p_fecha_sancion
    WHERE id_sancion_ayudante_catedra = p_id_sancion;

    RETURN p_id_sancion;

EXCEPTION WHEN OTHERS THEN
    RAISE NOTICE 'Error: %', SQLERRM;
    RETURN -1;
END;
$$;


--
-- Name: sp_actualizar_usuario(integer, character varying, character varying, character varying); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.sp_actualizar_usuario(p_id_usuario integer, p_nombres character varying, p_apellidos character varying, p_correo character varying) RETURNS integer
    LANGUAGE plpgsql
    AS $$
BEGIN
    UPDATE public.usuario
    SET nombres = p_nombres,
        apellidos = p_apellidos,
        correo = p_correo
    WHERE id_usuario = p_id_usuario;

    RETURN p_id_usuario;

EXCEPTION WHEN OTHERS THEN
    RAISE NOTICE 'Error: %', SQLERRM;
    RETURN -1;
END;
$$;


--
-- Name: sp_actualizar_usuario_comision(integer, numeric, numeric, numeric, date); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.sp_actualizar_usuario_comision(p_id integer, p_puntaje_material numeric DEFAULT NULL::numeric, p_puntaje_respuestas numeric DEFAULT NULL::numeric, p_puntaje_exposicion numeric DEFAULT NULL::numeric, p_fecha_evaluacion date DEFAULT NULL::date) RETURNS integer
    LANGUAGE plpgsql
    AS $$
BEGIN
    UPDATE seguridad.usuario_comision
    SET puntaje_material   = COALESCE(p_puntaje_material,   puntaje_material),
        puntaje_respuestas = COALESCE(p_puntaje_respuestas, puntaje_respuestas),
        puntaje_exposicion = COALESCE(p_puntaje_exposicion, puntaje_exposicion),
        fecha_evaluacion   = COALESCE(p_fecha_evaluacion,   fecha_evaluacion)
    WHERE id_usuario_comision = p_id;
    RETURN p_id;
EXCEPTION
    WHEN OTHERS THEN RETURN -1;
END;
$$;


--
-- Name: sp_asignar_rol_usuario(integer, integer); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.sp_asignar_rol_usuario(p_id_usuario integer, p_id_tipo_rol integer) RETURNS integer
    LANGUAGE plpgsql
    AS $$
BEGIN
    IF EXISTS (SELECT 1 FROM public.usuario_tipo_rol WHERE id_usuario = p_id_usuario AND id_tipo_rol = p_id_tipo_rol) THEN
        UPDATE public.usuario_tipo_rol
        SET activo = TRUE
        WHERE id_usuario = p_id_usuario AND id_tipo_rol = p_id_tipo_rol;
    ELSE
        INSERT INTO public.usuario_tipo_rol (id_usuario, id_tipo_rol, activo)
        VALUES (p_id_usuario, p_id_tipo_rol, TRUE);
    END IF;

    RETURN p_id_usuario;

EXCEPTION WHEN OTHERS THEN
    RAISE NOTICE 'Error: %', SQLERRM;
    RETURN -1;
END;
$$;


--
-- Name: sp_calcular_resultado_final(integer); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.sp_calcular_resultado_final(p_id_postulacion integer) RETURNS TABLE(id_postulacion integer, nombre_estudiante text, puntaje_meritos numeric, puntaje_oposicion numeric, puntaje_total numeric, posicion bigint)
    LANGUAGE plpgsql
    AS $$
BEGIN
    RETURN QUERY
    SELECT
        p.id_postulacion,
        (u.nombres || ' ' || u.apellidos)::TEXT AS nombre_estudiante,
        COALESCE(em.puntaje_total, 0)::NUMERIC  AS puntaje_meritos,
        COALESCE(eo.puntaje_total, 0)::NUMERIC  AS puntaje_oposicion,
        (COALESCE(em.puntaje_total, 0) + COALESCE(eo.puntaje_total, 0))::NUMERIC AS puntaje_total,
        RANK() OVER (
            PARTITION BY p.id_convocatoria
            ORDER BY (COALESCE(em.puntaje_total, 0) + COALESCE(eo.puntaje_total, 0)) DESC
        ) AS posicion
    FROM postulacion.postulacion p
    JOIN academico.estudiante e    ON e.id_estudiante = p.id_estudiante
    JOIN seguridad.usuario u       ON u.id_usuario    = e.id_usuario
    LEFT JOIN postulacion.evaluacion_meritos em   ON em.id_postulacion = p.id_postulacion
    LEFT JOIN postulacion.evaluacion_oposicion eo ON eo.id_postulacion = p.id_postulacion
    WHERE p.id_postulacion = p_id_postulacion;
END;
$$;


--
-- Name: sp_crear_asignatura(integer, character varying, integer); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.sp_crear_asignatura(p_id_carrera integer, p_nombre character varying, p_semestre integer) RETURNS integer
    LANGUAGE plpgsql
    AS $$
BEGIN
    RETURN public.fn_crear_asignatura(p_id_carrera, p_nombre, p_semestre);
END; $$;


--
-- Name: sp_crear_ayudante_catedra(integer, numeric); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.sp_crear_ayudante_catedra(p_id_usuario integer, p_horas_ayudante numeric) RETURNS integer
    LANGUAGE plpgsql
    AS $$
DECLARE
    v_id integer;
BEGIN
    INSERT INTO public.ayudante_catedra (id_usuario, horas_ayudante)
    VALUES (p_id_usuario, p_horas_ayudante)
    RETURNING id_ayudante_catedra INTO v_id;

    RETURN v_id;

EXCEPTION WHEN OTHERS THEN
    RAISE NOTICE 'Error: %', SQLERRM;
    RETURN -1;
END;
$$;


--
-- Name: sp_crear_ayudantia(integer, integer, date, date, integer); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.sp_crear_ayudantia(p_id_tipo_estado integer, p_id_postulacion integer, p_fecha_inicio date, p_fecha_fin date, p_horas_cumplidas integer) RETURNS integer
    LANGUAGE plpgsql
    AS $$
DECLARE
    v_id integer;
BEGIN
    INSERT INTO public.ayudantia (
        id_tipo_estado_evidencia_ayudantia, id_postulacion, fecha_inicio, fecha_fin, horas_cumplidas
    ) VALUES (
        p_id_tipo_estado, p_id_postulacion, p_fecha_inicio, p_fecha_fin, p_horas_cumplidas
    ) RETURNING id_ayudantia INTO v_id;

    RETURN v_id;

EXCEPTION WHEN OTHERS THEN
    RAISE NOTICE 'Error: %', SQLERRM;
    RETURN -1;
END;
$$;


--
-- Name: sp_crear_carrera(integer, character varying); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.sp_crear_carrera(p_id_facultad integer, p_nombre character varying) RETURNS integer
    LANGUAGE plpgsql
    AS $$
BEGIN
    RETURN public.fn_crear_carrera(p_id_facultad, p_nombre);
END; $$;


--
-- Name: sp_crear_certificado(integer, integer, character varying, date, integer, bytea, character varying); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.sp_crear_certificado(p_id_ayudantia integer, p_id_usuario integer, p_codigo_verificacion character varying, p_fecha_emision date, p_total_horas integer, p_archivo bytea, p_estado character varying) RETURNS integer
    LANGUAGE plpgsql
    AS $$
DECLARE
    v_id integer;
BEGIN
    INSERT INTO public.certificado (
        id_ayudantia, id_usuario, codigo_verificacion, fecha_emision, 
        total_horas_certificadas, archivo, estado, activo
    ) VALUES (
        p_id_ayudantia, p_id_usuario, p_codigo_verificacion, p_fecha_emision, 
        p_total_horas, p_archivo, p_estado, TRUE
    ) RETURNING id_certificado INTO v_id;

    RETURN v_id;

EXCEPTION WHEN OTHERS THEN
    RAISE NOTICE 'Error: %', SQLERRM;
    RETURN -1;
END;
$$;


--
-- Name: sp_crear_comision(integer, character varying, date); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.sp_crear_comision(p_id_convocatoria integer, p_nombre_comision character varying, p_fecha_conformacion date) RETURNS integer
    LANGUAGE plpgsql
    AS $$
DECLARE
    v_id INTEGER;
BEGIN
    INSERT INTO postulacion.comision_seleccion
        (id_convocatoria, nombre_comision, fecha_conformacion, activo)
    VALUES
        (p_id_convocatoria, p_nombre_comision, p_fecha_conformacion, TRUE)
    RETURNING id_comision_seleccion INTO v_id;
    RETURN v_id;
EXCEPTION
    WHEN OTHERS THEN RETURN -1;
END;
$$;


--
-- Name: sp_crear_convocatoria(integer, integer, integer, integer, date, date, character varying); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.sp_crear_convocatoria(p_id_periodo integer, p_id_asignatura integer, p_id_docente integer, p_cupos integer, p_fecha_pub date, p_fecha_cierre date, p_estado character varying) RETURNS integer
    LANGUAGE plpgsql
    AS $$
DECLARE
    v_id integer;
BEGIN
    INSERT INTO public.convocatoria (
        id_periodo_academico, id_asignatura, id_docente,
        cupos_disponibles, fecha_publicacion, fecha_cierre, estado, activo
    ) VALUES (
        p_id_periodo, p_id_asignatura, p_id_docente,
        p_cupos, p_fecha_pub, p_fecha_cierre, p_estado, TRUE
    ) RETURNING id_convocatoria INTO v_id;

    RETURN v_id;

EXCEPTION WHEN OTHERS THEN
    RAISE NOTICE 'Error: %', SQLERRM;
    RETURN -1;
END;
$$;


--
-- Name: sp_crear_coordinador(integer, integer, date, date); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.sp_crear_coordinador(p_id_usuario integer, p_id_carrera integer, p_fecha_inicio date, p_fecha_fin date) RETURNS integer
    LANGUAGE plpgsql
    AS $$
DECLARE
    v_id integer;
BEGIN
    INSERT INTO public.coordinador (
        id_usuario, id_carrera, fecha_inicio, fecha_fin, activo
    ) VALUES (
        p_id_usuario, p_id_carrera, p_fecha_inicio, p_fecha_fin, TRUE
    ) RETURNING id_coordinador INTO v_id;

    RETURN v_id;

EXCEPTION WHEN OTHERS THEN
    RAISE NOTICE 'Error: %', SQLERRM;
    RETURN -1;
END;
$$;


--
-- Name: sp_crear_decano(integer, integer, date, date); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.sp_crear_decano(p_id_usuario integer, p_id_facultad integer, p_fecha_inicio_gestion date, p_fecha_fin_gestion date) RETURNS integer
    LANGUAGE plpgsql
    AS $$
DECLARE
    v_id integer;
BEGIN
    INSERT INTO public.decano (
        id_usuario, id_facultad, fecha_inicio_gestion, fecha_fin_gestion, activo
    ) VALUES (
        p_id_usuario, p_id_facultad, p_fecha_inicio_gestion, p_fecha_fin_gestion, TRUE
    ) RETURNING id_decano INTO v_id;

    RETURN v_id;

EXCEPTION WHEN OTHERS THEN
    RAISE NOTICE 'Error: %', SQLERRM;
    RETURN -1;
END;
$$;


--
-- Name: sp_crear_docente(integer, date); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.sp_crear_docente(p_id_usuario integer, p_fecha_inicio date) RETURNS integer
    LANGUAGE plpgsql
    AS $$
DECLARE
    v_id integer;
BEGIN
    INSERT INTO public.docente (
        id_usuario, fecha_inicio, activo
    ) VALUES (
        p_id_usuario, p_fecha_inicio, TRUE
    ) RETURNING id_docente INTO v_id;

    RETURN v_id;

EXCEPTION WHEN OTHERS THEN
    RAISE NOTICE 'Error: %', SQLERRM;
    RETURN -1;
END;
$$;


--
-- Name: sp_crear_docente_asignatura(integer, integer); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.sp_crear_docente_asignatura(p_id_docente integer, p_id_asignatura integer) RETURNS integer
    LANGUAGE plpgsql
    AS $$
DECLARE
    v_id integer;
BEGIN
    INSERT INTO public.docente_asignatura (
        id_docente, id_asignatura, activo
    ) VALUES (
        p_id_docente, p_id_asignatura, TRUE
    ) RETURNING id_docente_asignatura INTO v_id;

    RETURN v_id;

EXCEPTION WHEN OTHERS THEN
    RAISE NOTICE 'Error: %', SQLERRM;
    RETURN -1;
END;
$$;


--
-- Name: sp_crear_estudiante(integer, integer, character varying, integer, character varying); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.sp_crear_estudiante(p_id_usuario integer, p_id_carrera integer, p_matricula character varying, p_semestre integer, p_estado_academico character varying) RETURNS integer
    LANGUAGE plpgsql
    AS $$
DECLARE
    v_id integer;
BEGIN
    INSERT INTO public.estudiante (
        id_usuario, id_carrera, matricula, semestre, estado_academico
    ) VALUES (
        p_id_usuario, p_id_carrera, p_matricula, p_semestre, p_estado_academico
    ) RETURNING id_estudiante INTO v_id;

    RETURN v_id;

EXCEPTION WHEN OTHERS THEN
    RAISE NOTICE 'Error: %', SQLERRM;
    RETURN -1;
END;
$$;


--
-- Name: sp_crear_evaluacion_meritos(integer, numeric, numeric, numeric, numeric, date); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.sp_crear_evaluacion_meritos(p_id_postulacion integer, p_nota_asignatura numeric, p_nota_semestres numeric, p_nota_eventos numeric, p_nota_experiencia numeric, p_fecha_evaluacion date) RETURNS integer
    LANGUAGE plpgsql
    AS $$
DECLARE
    v_id integer;
BEGIN
    INSERT INTO public.evaluacion_meritos (
        id_postulacion, nota_asignatura, nota_semestres, 
        nota_eventos, nota_experiencia, fecha_evaluacion
    ) VALUES (
        p_id_postulacion, p_nota_asignatura, p_nota_semestres, 
        p_nota_eventos, p_nota_experiencia, p_fecha_evaluacion
    ) RETURNING id_evaluacion_meritos INTO v_id;

    RETURN v_id;

EXCEPTION WHEN OTHERS THEN
    RAISE NOTICE 'Error: %', SQLERRM;
    RETURN -1;
END;
$$;


--
-- Name: sp_crear_evaluacion_oposicion(integer, character varying, date, time without time zone, time without time zone, character varying, character varying); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.sp_crear_evaluacion_oposicion(p_id_postulacion integer, p_tema_exposicion character varying, p_fecha_evaluacion date, p_hora_inicio time without time zone, p_hora_fin time without time zone, p_lugar character varying, p_estado character varying) RETURNS integer
    LANGUAGE plpgsql
    AS $$
DECLARE
    v_id INTEGER;
    v_id_estado_evaluacion INTEGER;
BEGIN
    INSERT INTO postulacion.evaluacion_oposicion
        (id_postulacion, tema_exposicion, fecha_evaluacion, hora_inicio, hora_fin, lugar, estado)
    VALUES
        (p_id_postulacion, p_tema_exposicion, p_fecha_evaluacion, p_hora_inicio, p_hora_fin, p_lugar, p_estado)
    RETURNING id_evaluacion_oposicion INTO v_id;
    
    -- Actualizar estado de la postulación (CORREGIDO: estado_postulacion e id_tipo_estado_postulacion)
    UPDATE postulacion.postulacion
    SET id_tipo_estado_postulacion =
        (select id_tipo_estado_postulacion from postulacion.tipo_estado_postulacion where codigo = 'EN_EVALUACION')
    WHERE id_postulacion = p_id_postulacion;
    
    RETURN v_id;
END;
$$;


--
-- Name: sp_crear_evidencia_actividad(integer, character varying, bytea, character varying, date); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.sp_crear_evidencia_actividad(p_id_registro_actividad integer, p_tipo_evidencia character varying, p_archivo bytea, p_nombre_archivo character varying, p_fecha_subida date) RETURNS integer
    LANGUAGE plpgsql
    AS $$
DECLARE
    v_id integer;
BEGIN
    INSERT INTO public.evidencia_registro_actividad (
        id_registro_actividad, tipo_evidencia, archivo, 
        nombre_archivo, fecha_subida, activo
    ) VALUES (
        p_id_registro_actividad, p_tipo_evidencia, p_archivo, 
        p_nombre_archivo, p_fecha_subida, TRUE
    ) RETURNING id_evidencia_registro_actividad INTO v_id;

    RETURN v_id;

EXCEPTION WHEN OTHERS THEN
    RAISE NOTICE 'Error: %', SQLERRM;
    RETURN -1;
END;
$$;


--
-- Name: sp_crear_facultad(character varying); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.sp_crear_facultad(p_nombre character varying) RETURNS integer
    LANGUAGE plpgsql
    AS $$
BEGIN
    RETURN public.fn_crear_facultad(p_nombre);
END; $$;


--
-- Name: sp_crear_log_auditoria(integer, character varying, character varying, integer, character varying, text, text); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.sp_crear_log_auditoria(p_id_usuario integer, p_accion character varying, p_tabla_afectada character varying, p_registro_afectado integer, p_ip_origen character varying, p_valor_anterior text, p_valor_nuevo text) RETURNS integer
    LANGUAGE plpgsql
    AS $$
DECLARE
    v_id integer;
BEGIN
    INSERT INTO public.log_auditoria (
        id_usuario, accion, tabla_afectada, registro_afectado, 
        fecha_hora, ip_origen, valor_anterior, valor_nuevo
    ) VALUES (
        p_id_usuario, p_accion, p_tabla_afectada, p_registro_afectado, 
        CURRENT_TIMESTAMP, p_ip_origen, p_valor_anterior, p_valor_nuevo
    ) RETURNING id_log_auditoria INTO v_id;

    RETURN v_id;

EXCEPTION WHEN OTHERS THEN
    RAISE NOTICE 'Error: %', SQLERRM;
    RETURN -1;
END;
$$;


--
-- Name: sp_crear_periodo_academico(character varying, date, date, character varying); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.sp_crear_periodo_academico(p_nombre character varying, p_inicio date, p_fin date, p_estado character varying) RETURNS integer
    LANGUAGE plpgsql
    AS $$
BEGIN
    RETURN public.fn_crear_periodo_academico(p_nombre, p_inicio, p_fin, p_estado);
END; $$;


--
-- Name: sp_crear_periodo_requisito(integer, integer, boolean, integer); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.sp_crear_periodo_requisito(p_id_periodo integer, p_id_tipo_requisito integer, p_obligatorio boolean, p_orden integer) RETURNS integer
    LANGUAGE plpgsql
    AS $$
DECLARE
    v_id integer;
BEGIN
    INSERT INTO public.periodo_academico_requisito_postulacion (
        id_periodo_academico, id_tipo_requisito_postulacion, 
        obligatorio, orden, activo
    ) VALUES (
        p_id_periodo, p_id_tipo_requisito, 
        p_obligatorio, p_orden, TRUE
    ) RETURNING id_periodo_academico_requisito_postulacion INTO v_id;

    RETURN v_id;

EXCEPTION WHEN OTHERS THEN
    RAISE NOTICE 'Error: %', SQLERRM;
    RETURN -1;
END;
$$;


--
-- Name: sp_crear_postulacion(integer, integer, date, character varying, character varying); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.sp_crear_postulacion(p_id_convocatoria integer, p_id_estudiante integer, p_fecha_postulacion date, p_estado_postulacion character varying, p_observaciones character varying) RETURNS integer
    LANGUAGE plpgsql
    AS $$
DECLARE
    v_id integer;
BEGIN
    INSERT INTO postulacion.postulacion (
        id_convocatoria, id_estudiante,
        fecha_postulacion, id_tipo_estado_postulacion, activo
    ) VALUES (
                 p_id_convocatoria, p_id_estudiante,
                 p_fecha_postulacion, (select id_tipo_estado_postulacion from postulacion.tipo_estado_postulacion where codigo = 'PENDIENTE' limit 1), TRUE
             ) RETURNING id_postulacion INTO v_id;

    RETURN v_id;
EXCEPTION WHEN OTHERS THEN
    RAISE NOTICE 'Error en sp_crear_postulacion: %', SQLERRM;
    RETURN -1;
END;
$$;


--
-- Name: sp_crear_registro_actividad(integer, text, text, date, integer, numeric, character varying); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.sp_crear_registro_actividad(p_id_ayudantia integer, p_descripcion text, p_tema_tratado text, p_fecha date, p_numero_asistentes integer, p_horas_dedicadas numeric, p_estado_revision character varying) RETURNS integer
    LANGUAGE plpgsql
    AS $$
DECLARE
    v_id integer;
BEGIN
    INSERT INTO public.registro_actividad (
        id_ayudantia, descripcion_actividad, tema_tratado, 
        fecha, numero_asistentes, horas_dedicadas, estado_revision
    ) VALUES (
        p_id_ayudantia, p_descripcion, p_tema_tratado, 
        p_fecha, p_numero_asistentes, p_horas_dedicadas, p_estado_revision
    ) RETURNING id_registro_actividad INTO v_id;

    RETURN v_id;

EXCEPTION WHEN OTHERS THEN
    RAISE NOTICE 'Error: %', SQLERRM;
    RETURN -1;
END;
$$;


--
-- Name: sp_crear_requisito_adjunto(integer, integer, integer, bytea, character varying, date); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.sp_crear_requisito_adjunto(p_id_postulacion integer, p_id_tipo_requisito integer, p_id_tipo_estado integer, p_archivo bytea, p_nombre_archivo character varying, p_fecha_subida date) RETURNS integer
    LANGUAGE plpgsql
    AS $$
DECLARE
    v_id integer;
BEGIN
    INSERT INTO postulacion.requisito_adjunto (
        id_postulacion, id_tipo_requisito_postulacion, id_tipo_estado_requisito,
        archivo, nombre_archivo, fecha_subida
    ) VALUES (
                 p_id_postulacion, p_id_tipo_requisito,
              (select id_tipo_estado_requisito from convocatoria.tipo_estado_requisito where codigo = 'PENDIENTE' limit 1),
                 p_archivo, p_nombre_archivo, p_fecha_subida
             ) RETURNING id_requisito_adjunto INTO v_id;

    RETURN v_id;
EXCEPTION WHEN OTHERS THEN
    RAISE NOTICE 'Error en sp_crear_requisito_adjunto: %', SQLERRM;
    RETURN -1;
END;
$$;


--
-- Name: sp_crear_sancion(integer, integer, date, character varying); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.sp_crear_sancion(p_id_tipo_sancion integer, p_id_ayudante integer, p_fecha_sancion date, p_motivo character varying) RETURNS integer
    LANGUAGE plpgsql
    AS $$
DECLARE
    v_id integer;
BEGIN
    INSERT INTO public.sancion_ayudante_catedra (
        id_tipo_sancion_ayudante_catedra, id_ayudante_catedra, 
        fecha_sancion, motivo, activo
    ) VALUES (
        p_id_tipo_sancion, p_id_ayudante, 
        p_fecha_sancion, p_motivo, TRUE
    ) RETURNING id_sancion_ayudante_catedra INTO v_id;

    RETURN v_id;

EXCEPTION WHEN OTHERS THEN
    RAISE NOTICE 'Error: %', SQLERRM;
    RETURN -1;
END;
$$;


--
-- Name: sp_crear_usuario(character varying, character varying, character varying, character varying, character varying, character varying); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.sp_crear_usuario(p_nombres character varying, p_apellidos character varying, p_cedula character varying, p_correo character varying, p_nombre_usuario character varying, p_contrasenia character varying) RETURNS integer
    LANGUAGE plpgsql
    AS $$
DECLARE
    v_id integer;
BEGIN
    INSERT INTO public.usuario (
        nombres, apellidos, cedula, correo, 
        nombre_usuario, contrasenia_usuario, fecha_creacion, activo
    ) VALUES (
        p_nombres, p_apellidos, p_cedula, p_correo, 
        p_nombre_usuario, crypt(p_contrasenia, public.gen_salt('bf')), CURRENT_DATE, TRUE
    ) RETURNING id_usuario INTO v_id;

    RETURN v_id;

EXCEPTION WHEN OTHERS THEN
    RAISE NOTICE 'Error: %', SQLERRM;
    RETURN -1;
END;
$$;


--
-- Name: sp_crear_usuario_comision(integer, integer, integer, character varying, numeric, numeric, numeric, date); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.sp_crear_usuario_comision(p_id_comision integer, p_id_usuario integer, p_id_evaluacion_oposicion integer DEFAULT NULL::integer, p_rol_integrante character varying DEFAULT NULL::character varying, p_puntaje_material numeric DEFAULT NULL::numeric, p_puntaje_respuestas numeric DEFAULT NULL::numeric, p_puntaje_exposicion numeric DEFAULT NULL::numeric, p_fecha_evaluacion date DEFAULT NULL::date) RETURNS integer
    LANGUAGE plpgsql
    AS $$
DECLARE
    v_id INTEGER;
BEGIN
    INSERT INTO seguridad.usuario_comision
        (id_comision_seleccion, id_usuario, id_evaluacion_oposicion, rol_integrante,
         puntaje_material, puntaje_respuestas, puntaje_exposicion, fecha_evaluacion)
    VALUES
        (p_id_comision, p_id_usuario, p_id_evaluacion_oposicion, p_rol_integrante,
         p_puntaje_material, p_puntaje_respuestas, p_puntaje_exposicion, p_fecha_evaluacion)
    RETURNING id_usuario_comision INTO v_id;
    RETURN v_id;
EXCEPTION
    WHEN OTHERS THEN RETURN -1;
END;
$$;


--
-- Name: sp_desactivar_asignatura(integer); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.sp_desactivar_asignatura(p_id integer) RETURNS integer
    LANGUAGE plpgsql
    AS $$
BEGIN
    RETURN public.fn_desactivar_asignatura(p_id);
END; $$;


--
-- Name: sp_desactivar_carrera(integer); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.sp_desactivar_carrera(p_id integer) RETURNS integer
    LANGUAGE plpgsql
    AS $$
BEGIN
    RETURN public.fn_desactivar_carrera(p_id);
END; $$;


--
-- Name: sp_desactivar_certificado(integer); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.sp_desactivar_certificado(p_id_certificado integer) RETURNS integer
    LANGUAGE plpgsql
    AS $$
BEGIN
    UPDATE public.certificado
    SET activo = FALSE
    WHERE id_certificado = p_id_certificado;

    RETURN p_id_certificado;

EXCEPTION WHEN OTHERS THEN
    RAISE NOTICE 'Error: %', SQLERRM;
    RETURN -1;
END;
$$;


--
-- Name: sp_desactivar_comision(integer); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.sp_desactivar_comision(p_id integer) RETURNS integer
    LANGUAGE plpgsql
    AS $$
BEGIN
    UPDATE postulacion.comision_seleccion
    SET activo = FALSE
    WHERE id_comision_seleccion = p_id;
    RETURN p_id;
EXCEPTION
    WHEN OTHERS THEN RETURN -1;
END;
$$;


--
-- Name: sp_desactivar_convocatoria(integer); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.sp_desactivar_convocatoria(p_id_convocatoria integer) RETURNS integer
    LANGUAGE plpgsql
    AS $$
BEGIN
    UPDATE public.convocatoria
    SET activo = FALSE
    WHERE id_convocatoria = p_id_convocatoria;

    RETURN p_id_convocatoria;

EXCEPTION WHEN OTHERS THEN
    RAISE NOTICE 'Error: %', SQLERRM;
    RETURN -1;
END;
$$;


--
-- Name: sp_desactivar_coordinador(integer); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.sp_desactivar_coordinador(p_id_coordinador integer) RETURNS integer
    LANGUAGE plpgsql
    AS $$
BEGIN
    UPDATE public.coordinador
    SET activo = FALSE,
        fecha_fin = CURRENT_DATE
    WHERE id_coordinador = p_id_coordinador;

    RETURN p_id_coordinador;

EXCEPTION WHEN OTHERS THEN
    RAISE NOTICE 'Error: %', SQLERRM;
    RETURN -1;
END;
$$;


--
-- Name: sp_desactivar_decano(integer); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.sp_desactivar_decano(p_id_decano integer) RETURNS integer
    LANGUAGE plpgsql
    AS $$
BEGIN
    UPDATE public.decano
    SET activo = FALSE
    WHERE id_decano = p_id_decano;

    RETURN p_id_decano;

EXCEPTION WHEN OTHERS THEN
    RAISE NOTICE 'Error: %', SQLERRM;
    RETURN -1;
END;
$$;


--
-- Name: sp_desactivar_docente(integer); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.sp_desactivar_docente(p_id_docente integer) RETURNS integer
    LANGUAGE plpgsql
    AS $$
BEGIN
    UPDATE public.docente
    SET activo = FALSE,
        fecha_fin = CURRENT_DATE
    WHERE id_docente = p_id_docente;

    RETURN p_id_docente;

EXCEPTION WHEN OTHERS THEN
    RAISE NOTICE 'Error: %', SQLERRM;
    RETURN -1;
END;
$$;


--
-- Name: sp_desactivar_docente_asignatura(integer); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.sp_desactivar_docente_asignatura(p_id_docente_asignatura integer) RETURNS integer
    LANGUAGE plpgsql
    AS $$
BEGIN
    UPDATE public.docente_asignatura
    SET activo = FALSE
    WHERE id_docente_asignatura = p_id_docente_asignatura;

    RETURN p_id_docente_asignatura;

EXCEPTION WHEN OTHERS THEN
    RAISE NOTICE 'Error: %', SQLERRM;
    RETURN -1;
END;
$$;


--
-- Name: sp_desactivar_evidencia_actividad(integer); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.sp_desactivar_evidencia_actividad(p_id_evidencia integer) RETURNS integer
    LANGUAGE plpgsql
    AS $$
BEGIN
    UPDATE public.evidencia_registro_actividad
    SET activo = FALSE
    WHERE id_evidencia_registro_actividad = p_id_evidencia;

    RETURN p_id_evidencia;

EXCEPTION WHEN OTHERS THEN
    RAISE NOTICE 'Error: %', SQLERRM;
    RETURN -1;
END;
$$;


--
-- Name: sp_desactivar_facultad(integer); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.sp_desactivar_facultad(p_id integer) RETURNS integer
    LANGUAGE plpgsql
    AS $$
BEGIN
    RETURN public.fn_desactivar_facultad(p_id);
END; $$;


--
-- Name: sp_desactivar_periodo_academico(integer); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.sp_desactivar_periodo_academico(p_id integer) RETURNS integer
    LANGUAGE plpgsql
    AS $$
BEGIN
    RETURN public.fn_desactivar_periodo_academico(p_id);
END; $$;


--
-- Name: sp_desactivar_periodo_requisito(integer); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.sp_desactivar_periodo_requisito(p_id_relacion integer) RETURNS integer
    LANGUAGE plpgsql
    AS $$
BEGIN
    UPDATE public.periodo_academico_requisito_postulacion
    SET activo = FALSE
    WHERE id_periodo_academico_requisito_postulacion = p_id_relacion;

    RETURN p_id_relacion;

EXCEPTION WHEN OTHERS THEN
    RAISE NOTICE 'Error: %', SQLERRM;
    RETURN -1;
END;
$$;


--
-- Name: sp_desactivar_postulacion(integer); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.sp_desactivar_postulacion(p_id_postulacion integer) RETURNS integer
    LANGUAGE plpgsql
    AS $$
BEGIN
    UPDATE public.postulacion
    SET activo = FALSE,
        estado_postulacion = 'ANULADA'
    WHERE id_postulacion = p_id_postulacion;

    RETURN p_id_postulacion;

EXCEPTION WHEN OTHERS THEN
    RAISE NOTICE 'Error: %', SQLERRM;
    RETURN -1;
END;
$$;


--
-- Name: sp_desactivar_rol_usuario(integer, integer); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.sp_desactivar_rol_usuario(p_id_usuario integer, p_id_tipo_rol integer) RETURNS integer
    LANGUAGE plpgsql
    AS $$
BEGIN
    UPDATE public.usuario_tipo_rol
    SET activo = FALSE
    WHERE id_usuario = p_id_usuario AND id_tipo_rol = p_id_tipo_rol;

    RETURN p_id_usuario;

EXCEPTION WHEN OTHERS THEN
    RAISE NOTICE 'Error: %', SQLERRM;
    RETURN -1;
END;
$$;


--
-- Name: sp_desactivar_sancion(integer); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.sp_desactivar_sancion(p_id_sancion integer) RETURNS integer
    LANGUAGE plpgsql
    AS $$
BEGIN
    UPDATE public.sancion_ayudante_catedra
    SET activo = FALSE
    WHERE id_sancion_ayudante_catedra = p_id_sancion;

    RETURN p_id_sancion;

EXCEPTION WHEN OTHERS THEN
    RAISE NOTICE 'Error: %', SQLERRM;
    RETURN -1;
END;
$$;


--
-- Name: sp_desactivar_usuario(integer); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.sp_desactivar_usuario(p_id_usuario integer) RETURNS integer
    LANGUAGE plpgsql
    AS $$
BEGIN
    UPDATE public.usuario
    SET activo = FALSE
    WHERE id_usuario = p_id_usuario;

    RETURN p_id_usuario;

EXCEPTION WHEN OTHERS THEN
    RAISE NOTICE 'Error: %', SQLERRM;
    RETURN -1;
END;
$$;


--
-- Name: sp_desactivar_usuario_comision(integer); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.sp_desactivar_usuario_comision(p_id integer) RETURNS integer
    LANGUAGE plpgsql
    AS $$
BEGIN
    DELETE FROM seguridad.usuario_comision WHERE id_usuario_comision = p_id;
    RETURN p_id;
EXCEPTION
    WHEN OTHERS THEN RETURN -1;
END;
$$;


--
-- Name: sp_enviar_notificacion_masiva(text, character varying, character varying, integer, integer); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.sp_enviar_notificacion_masiva(p_mensaje text, p_tipo character varying, p_tipo_notificacion character varying, p_id_rol integer DEFAULT NULL::integer, p_id_convocatoria integer DEFAULT NULL::integer) RETURNS integer
    LANGUAGE plpgsql
    AS $$
DECLARE
    v_count INTEGER := 0;
    v_uid   INTEGER;
BEGIN
    FOR v_uid IN
        SELECT DISTINCT u.id_usuario
        FROM seguridad.usuario u
        JOIN seguridad.usuario_tipo_rol utr ON utr.id_usuario = u.id_usuario
        WHERE u.activo = TRUE
          AND (p_id_rol IS NULL OR utr.id_tipo_rol = p_id_rol)
    LOOP
        INSERT INTO notificacion.notificacion
            (id_usuario_destino, mensaje, fecha_envio, leido, tipo, tipo_notificacion, id_convocatoria)
        VALUES
            (v_uid, p_mensaje, NOW(), false, p_tipo, p_tipo_notificacion, p_id_convocatoria);
        v_count := v_count + 1;
    END LOOP;
    RETURN v_count;
END;
$$;


--
-- Name: sp_evidencias_actividad_docente(integer); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.sp_evidencias_actividad_docente(p_id_registro integer) RETURNS TABLE(id_evidencia_registro_actividad integer, tipo_evidencia character varying, nombre_archivo character varying, ruta_archivo character varying, mime_type character varying, fecha_subida date, estado_evidencia character varying, observaciones character varying, fecha_observacion date)
    LANGUAGE sql STABLE
    AS $$
    SELECT
        e.id_evidencia_registro_actividad,
        te.nombre                     AS tipo_evidencia,
        e.nombre_archivo,
        e.ruta_archivo,
        e.mime_type,
        e.fecha_subida,
        tee.nombre_estado             AS estado_evidencia,
        e.observaciones,
        e.fecha_observacion
    FROM ayudantia.evidencia_registro_actividad e
    JOIN ayudantia.tipo_evidencia te
                                      ON te.id_tipo_evidencia = e.id_tipo_evidencia
    JOIN ayudantia.tipo_estado_evidencia tee
                                      ON tee.id_tipo_estado_evidencia = e.id_tipo_estado_evidencia
    WHERE e.id_registro_actividad = p_id_registro
      AND e.activo = TRUE
    ORDER BY e.fecha_subida;
$$;


--
-- Name: sp_importar_requisitos_periodo(integer, integer); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.sp_importar_requisitos_periodo(p_id_periodo_origen integer, p_id_periodo_destino integer) RETURNS integer
    LANGUAGE plpgsql
    AS $$
DECLARE
    v_count INTEGER := 0;
    r RECORD;
BEGIN
    FOR r IN
        SELECT id_tipo_requisito_postulacion, obligatorio, orden
        FROM convocatoria.periodo_academico_requisito_postulacion
        WHERE id_periodo_academico = p_id_periodo_origen
          AND activo = TRUE
    LOOP
        -- Solo insertar si no existe ya para este periodo destino
        IF NOT EXISTS (
            SELECT 1 FROM convocatoria.periodo_academico_requisito_postulacion
            WHERE id_periodo_academico = p_id_periodo_destino
              AND id_tipo_requisito_postulacion = r.id_tipo_requisito_postulacion
              AND activo = TRUE
        ) THEN
            PERFORM public.sp_crear_periodo_requisito(
                p_id_periodo_destino,
                r.id_tipo_requisito_postulacion,
                r.obligatorio,
                r.orden
            );
            v_count := v_count + 1;
        END IF;
    END LOOP;

    RETURN v_count;
END;
$$;


SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- Name: registro_actividad; Type: TABLE; Schema: ayudantia; Owner: -
--

CREATE TABLE ayudantia.registro_actividad (
    id_registro_actividad integer NOT NULL,
    id_ayudantia integer NOT NULL,
    descripcion_actividad text,
    tema_tratado text,
    fecha date,
    horas_dedicadas numeric(5,2),
    id_tipo_estado_registro integer NOT NULL,
    observaciones character varying(500),
    fecha_observacion date,
    hora_inicio time without time zone,
    hora_fin time without time zone,
    lugar character varying(255),
    numero_asistentes integer DEFAULT 0,
    CONSTRAINT chk_horas_validas CHECK (((horas_dedicadas > (0)::numeric) AND (horas_dedicadas <= (24)::numeric)))
);


--
-- Name: sp_listar_actividades_ayudantia(integer); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.sp_listar_actividades_ayudantia(p_id_ayudantia integer) RETURNS SETOF ayudantia.registro_actividad
    LANGUAGE plpgsql
    AS $$
BEGIN
    RETURN QUERY 
    SELECT * FROM public.registro_actividad 
    WHERE id_ayudantia = p_id_ayudantia AND estado_revision <> 'ANULADO';
END;
$$;


--
-- Name: sp_listar_asignaturas_docente(integer); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.sp_listar_asignaturas_docente(p_id_docente integer) RETURNS TABLE(id_relacion integer, id_asig integer)
    LANGUAGE plpgsql
    AS $$
BEGIN
    RETURN QUERY 
    SELECT id_docente_asignatura, id_asignatura
    FROM public.docente_asignatura
    WHERE id_docente = p_id_docente AND activo = TRUE;
END;
$$;


--
-- Name: sp_listar_ayudantes_docente(integer); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.sp_listar_ayudantes_docente(p_id_usuario_docente integer) RETURNS TABLE(id_ayudantia integer, id_usuario integer, nombre_completo text, correo character varying, nombre_asignatura character varying, estado_ayudantia character varying, horas_cumplidas integer, actividades_total bigint, actividades_pendientes bigint)
    LANGUAGE sql STABLE
    AS $$
    SELECT
        ay.id_ayudantia,
        u.id_usuario,
        CONCAT(u.nombres, ' ', u.apellidos)      AS nombre_completo,
        u.correo                                  AS correo,
        '—'::VARCHAR                              AS nombre_asignatura,
        tea.nombre_estado                         AS estado_ayudantia,
        ay.horas_cumplidas,
        COUNT(ra.id_registro_actividad)           AS actividades_total,
        COUNT(ra.id_registro_actividad)
            FILTER (WHERE UPPER(ter.nombre_estado) = 'PENDIENTE') AS actividades_pendientes
    FROM ayudantia.ayudantia ay
    JOIN postulacion.postulacion pp      ON pp.id_postulacion  = ay.id_postulacion
    JOIN convocatoria.convocatoria cv    ON cv.id_convocatoria = pp.id_convocatoria
    JOIN academico.docente dc            ON dc.id_docente      = cv.id_docente
    JOIN seguridad.usuario u_doc         ON u_doc.id_usuario   = dc.id_usuario
    JOIN academico.estudiante est        ON est.id_estudiante  = pp.id_estudiante
    JOIN seguridad.usuario u             ON u.id_usuario       = est.id_usuario
    JOIN ayudantia.tipo_estado_ayudantia tea
                                         ON tea.id_tipo_estado_ayudantia = ay.id_tipo_estado_ayudantia
    LEFT JOIN ayudantia.registro_actividad ra
                                         ON ra.id_ayudantia = ay.id_ayudantia
    LEFT JOIN ayudantia.tipo_estado_registro ter
                                         ON ter.id_tipo_estado_registro = ra.id_tipo_estado_registro
    WHERE u_doc.id_usuario = p_id_usuario_docente
    GROUP BY ay.id_ayudantia, u.id_usuario, u.nombres, u.apellidos,
             u.correo, tea.nombre_estado, ay.horas_cumplidas
    ORDER BY actividades_pendientes DESC, nombre_completo;
$$;


--
-- Name: sp_listar_certificados(); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.sp_listar_certificados() RETURNS TABLE(id integer, codigo character varying, fecha date, horas integer, estado_cert character varying)
    LANGUAGE plpgsql
    AS $$
BEGIN
    RETURN QUERY 
    SELECT id_certificado, codigo_verificacion, fecha_emision, total_horas_certificadas, estado
    FROM public.certificado
    WHERE activo = TRUE;
END;
$$;


--
-- Name: sp_listar_convocatorias_activas(); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.sp_listar_convocatorias_activas() RETURNS TABLE(id integer, asignatura_id integer, docente_id integer, cupos integer, estado_actual character varying)
    LANGUAGE plpgsql
    AS $$
BEGIN
    RETURN QUERY 
    SELECT id_convocatoria, id_asignatura, id_docente, cupos_disponibles, estado
    FROM public.convocatoria
    WHERE activo = TRUE;
END;
$$;


--
-- Name: sp_listar_coordinadores(); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.sp_listar_coordinadores() RETURNS TABLE(id integer, usuario_id integer, carrera_id integer, inicio date, fin date)
    LANGUAGE plpgsql
    AS $$
BEGIN
    RETURN QUERY 
    SELECT id_coordinador, id_usuario, id_carrera, fecha_inicio, fecha_fin
    FROM public.coordinador
    WHERE activo = TRUE;
END;
$$;


--
-- Name: sp_listar_evaluaciones_oposicion(); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.sp_listar_evaluaciones_oposicion() RETURNS TABLE(id_evaluacion_oposicion integer, tema_exposicion character varying, fecha_evaluacion date, estado character varying)
    LANGUAGE plpgsql
    AS $$
BEGIN
    RETURN QUERY
    SELECT e.id_evaluacion_oposicion, e.tema_exposicion, e.fecha_evaluacion, e.estado
    FROM postulacion.evaluacion_oposicion e;
END;
$$;


--
-- Name: sp_listar_evaluadores_comision(integer); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.sp_listar_evaluadores_comision(p_id_comision integer) RETURNS TABLE(id_usuario_comision integer, id_comision_seleccion integer, id_usuario integer, id_evaluacion_oposicion integer, rol_integrante character varying, puntaje_material numeric, puntaje_respuestas numeric, puntaje_exposicion numeric, fecha_evaluacion date)
    LANGUAGE plpgsql
    AS $$
BEGIN
    RETURN QUERY
    SELECT uc.id_usuario_comision,
           uc.id_comision_seleccion,
           uc.id_usuario,
           uc.id_evaluacion_oposicion,
           uc.rol_integrante,
           uc.puntaje_material,
           uc.puntaje_respuestas,
           uc.puntaje_exposicion,
           uc.fecha_evaluacion
    FROM seguridad.usuario_comision uc
    WHERE uc.id_comision_seleccion = p_id_comision;
END;
$$;


--
-- Name: sp_listar_logs_auditoria(); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.sp_listar_logs_auditoria() RETURNS TABLE(id integer, usuario integer, accion_log character varying, fecha timestamp without time zone)
    LANGUAGE plpgsql
    AS $$
BEGIN
    RETURN QUERY 
    SELECT id_log_auditoria, id_usuario, accion, fecha_hora
    FROM public.log_auditoria
    ORDER BY fecha_hora DESC;
END;
$$;


--
-- Name: sp_listar_postulaciones_por_estudiante(integer); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.sp_listar_postulaciones_por_estudiante(p_id_estudiante integer) RETURNS TABLE(id integer, convocatoria_id integer, fecha date, estado character varying)
    LANGUAGE plpgsql
    AS $$
BEGIN
    RETURN QUERY 
    SELECT p.id_postulacion, p.id_convocatoria, p.fecha_postulacion, tep.codigo
    FROM postulacion.postulacion p
    JOIN postulacion.tipo_estado_postulacion tep on p.id_tipo_estado_postulacion = tep.id_tipo_estado_postulacion
    WHERE p.id_estudiante = p_id_estudiante AND p.activo = TRUE;
END;
$$;


--
-- Name: sp_listar_requisitos_periodo(integer); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.sp_listar_requisitos_periodo(p_id_periodo integer) RETURNS TABLE(id_relacion integer, id_req_tipo integer, es_obligatorio boolean, num_orden integer)
    LANGUAGE plpgsql
    AS $$
BEGIN
    RETURN QUERY 
    SELECT id_periodo_academico_requisito_postulacion, id_tipo_requisito_postulacion, obligatorio, orden
    FROM public.periodo_academico_requisito_postulacion
    WHERE id_periodo_academico = p_id_periodo AND activo = TRUE
    ORDER BY orden ASC;
END;
$$;


--
-- Name: sancion_ayudante_catedra; Type: TABLE; Schema: ayudantia; Owner: -
--

CREATE TABLE ayudantia.sancion_ayudante_catedra (
    id_sancion_ayudante_catedra integer NOT NULL,
    id_tipo_sancion_ayudante_catedra integer,
    id_ayudante_catedra integer,
    fecha_sancion date,
    activo boolean,
    motivo character varying(150)
);


--
-- Name: sp_listar_sanciones_ayudante(integer); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.sp_listar_sanciones_ayudante(p_id_ayudante integer) RETURNS SETOF ayudantia.sancion_ayudante_catedra
    LANGUAGE plpgsql
    AS $$
BEGIN
    RETURN QUERY 
    SELECT * FROM public.sancion_ayudante_catedra
    WHERE id_ayudante_catedra = p_id_ayudante AND activo = TRUE;
END;
$$;


--
-- Name: sp_metricas_convocatoria(integer); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.sp_metricas_convocatoria(p_id_carrera integer) RETURNS TABLE(total_convocatorias bigint, convocatorias_activas bigint, total_postulaciones bigint, postulaciones_pendientes bigint, postulaciones_aprobadas bigint)
    LANGUAGE plpgsql
    AS $$
BEGIN
    RETURN QUERY
        SELECT
            (SELECT COUNT(*)
             FROM convocatoria.convocatoria cv
                      JOIN academico.asignatura a ON cv.id_asignatura = a.id_asignatura
                      JOIN academico.periodo_academico pa ON cv.id_periodo_academico = pa.id_periodo_academico
             WHERE a.id_carrera = p_id_carrera
               AND cv.activo = TRUE
               AND pa.activo = TRUE)::BIGINT,

            (SELECT COUNT(*)
             FROM convocatoria.convocatoria cv
                      JOIN academico.asignatura a ON cv.id_asignatura = a.id_asignatura
                      JOIN academico.periodo_academico pa ON cv.id_periodo_academico = pa.id_periodo_academico
             WHERE a.id_carrera = p_id_carrera
               AND cv.activo = TRUE
               AND UPPER(cv.estado) IN ('ABIERTA','ACTIVA','PUBLICADA')
               AND pa.activo = TRUE)::BIGINT,

            (SELECT COUNT(*)
             FROM postulacion.postulacion p
                      JOIN convocatoria.convocatoria cv ON p.id_convocatoria = cv.id_convocatoria
                      JOIN academico.asignatura a      ON cv.id_asignatura   = a.id_asignatura
                      JOIN academico.periodo_academico pa ON cv.id_periodo_academico = pa.id_periodo_academico
             WHERE a.id_carrera = p_id_carrera
               AND pa.activo = TRUE)::BIGINT,

            (SELECT COUNT(*)
             FROM postulacion.postulacion p
                      JOIN postulacion.tipo_estado_postulacion tep ON p.id_tipo_estado_postulacion = tep.id_tipo_estado_postulacion
                      JOIN convocatoria.convocatoria cv ON p.id_convocatoria = cv.id_convocatoria
                      JOIN academico.asignatura a      ON cv.id_asignatura   = a.id_asignatura
                      JOIN academico.periodo_academico pa ON cv.id_periodo_academico = pa.id_periodo_academico
             WHERE a.id_carrera = p_id_carrera
               AND UPPER(tep.codigo) = 'PENDIENTE'
               AND pa.activo = TRUE)::BIGINT,

            (SELECT COUNT(*)
             FROM postulacion.postulacion p
                      JOIN postulacion.tipo_estado_postulacion tep ON p.id_tipo_estado_postulacion = tep.id_tipo_estado_postulacion
                      JOIN convocatoria.convocatoria cv ON p.id_convocatoria = cv.id_convocatoria
                      JOIN academico.asignatura a      ON cv.id_asignatura   = a.id_asignatura
                      JOIN academico.periodo_academico pa ON cv.id_periodo_academico = pa.id_periodo_academico
             WHERE a.id_carrera = p_id_carrera
               AND UPPER(tep.codigo) = 'APROBADO'
               AND pa.activo = TRUE)::BIGINT;
END;
$$;


--
-- Name: sp_metricas_postulante(integer); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.sp_metricas_postulante(p_id_usuario integer) RETURNS TABLE(total_postulaciones bigint, postulaciones_pendientes bigint, postulaciones_aprobadas bigint, postulaciones_rechazadas bigint)
    LANGUAGE plpgsql
    AS $$
BEGIN
    RETURN QUERY
    SELECT
        (SELECT COUNT(*)
         FROM postulacion.postulacion p
         JOIN academico.estudiante e ON p.id_estudiante = e.id_estudiante
         JOIN convocatoria.convocatoria cv ON p.id_convocatoria = cv.id_convocatoria
         JOIN academico.periodo_academico pa ON cv.id_periodo_academico = pa.id_periodo_academico
         WHERE e.id_usuario = p_id_usuario
           AND pa.activo = TRUE)::BIGINT,

        (SELECT COUNT(*)
         FROM postulacion.postulacion p
         JOIN academico.estudiante e ON p.id_estudiante = e.id_estudiante
         JOIN convocatoria.convocatoria cv ON p.id_convocatoria = cv.id_convocatoria
         JOIN academico.periodo_academico pa ON cv.id_periodo_academico = pa.id_periodo_academico
         WHERE e.id_usuario = p_id_usuario
           AND UPPER(p.estado_postulacion) = 'PENDIENTE'
           AND pa.activo = TRUE)::BIGINT,

        (SELECT COUNT(*)
         FROM postulacion.postulacion p
         JOIN academico.estudiante e ON p.id_estudiante = e.id_estudiante
         JOIN convocatoria.convocatoria cv ON p.id_convocatoria = cv.id_convocatoria
         JOIN academico.periodo_academico pa ON cv.id_periodo_academico = pa.id_periodo_academico
         WHERE e.id_usuario = p_id_usuario
           AND UPPER(p.estado_postulacion) = 'APROBADO'
           AND pa.activo = TRUE)::BIGINT,

        (SELECT COUNT(*)
         FROM postulacion.postulacion p
         JOIN academico.estudiante e ON p.id_estudiante = e.id_estudiante
         JOIN convocatoria.convocatoria cv ON p.id_convocatoria = cv.id_convocatoria
         JOIN academico.periodo_academico pa ON cv.id_periodo_academico = pa.id_periodo_academico
         WHERE e.id_usuario = p_id_usuario
           AND UPPER(p.estado_postulacion) = 'RECHAZADO'
           AND pa.activo = TRUE)::BIGINT;
END;
$$;


--
-- Name: sp_obtener_ayudantes_catedra(); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.sp_obtener_ayudantes_catedra() RETURNS TABLE(id_ayudante_catedra integer, id_usuario integer, horas_ayudante numeric)
    LANGUAGE plpgsql
    AS $$
BEGIN
    RETURN QUERY 
    SELECT ac.id_ayudante_catedra, ac.id_usuario, ac.horas_ayudante
    FROM public.ayudante_catedra ac;
END;
$$;


--
-- Name: ayudantia; Type: TABLE; Schema: ayudantia; Owner: -
--

CREATE TABLE ayudantia.ayudantia (
    id_ayudantia integer NOT NULL,
    id_postulacion integer NOT NULL,
    fecha_inicio date,
    fecha_fin date,
    horas_cumplidas integer,
    id_tipo_estado_ayudantia integer NOT NULL,
    horas_semanales_max numeric(5,2) DEFAULT 20 NOT NULL,
    horas_maximas numeric(5,2)
);


--
-- Name: sp_obtener_ayudantia_por_id(integer); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.sp_obtener_ayudantia_por_id(p_id integer) RETURNS SETOF ayudantia.ayudantia
    LANGUAGE plpgsql
    AS $$
BEGIN
    RETURN QUERY SELECT * FROM public.ayudantia WHERE id_ayudantia = p_id;
END;
$$;


--
-- Name: comision_seleccion; Type: TABLE; Schema: postulacion; Owner: -
--

CREATE TABLE postulacion.comision_seleccion (
    id_comision_seleccion integer NOT NULL,
    id_convocatoria integer NOT NULL,
    nombre_comision character varying(100),
    fecha_conformacion date,
    activo boolean
);


--
-- Name: sp_obtener_comision(integer); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.sp_obtener_comision(p_id integer) RETURNS SETOF postulacion.comision_seleccion
    LANGUAGE plpgsql
    AS $$
BEGIN
    RETURN QUERY SELECT * FROM public.comision_seleccion 
    WHERE id_comision_seleccion = p_id AND activo = TRUE;
END;
$$;


--
-- Name: decano; Type: TABLE; Schema: academico; Owner: -
--

CREATE TABLE academico.decano (
    id_decano integer NOT NULL,
    id_usuario integer NOT NULL,
    id_facultad integer NOT NULL,
    fecha_inicio_gestion date,
    fecha_fin_gestion date,
    activo boolean
);


--
-- Name: sp_obtener_decanos_activos(); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.sp_obtener_decanos_activos() RETURNS SETOF academico.decano
    LANGUAGE plpgsql
    AS $$
BEGIN
    RETURN QUERY
        SELECT *
        FROM academico.decano d
        WHERE d.activo = true;
END;
$$;


--
-- Name: docente; Type: TABLE; Schema: academico; Owner: -
--

CREATE TABLE academico.docente (
    id_docente integer NOT NULL,
    id_usuario integer NOT NULL,
    fecha_inicio date,
    fecha_fin date,
    activo boolean
);


--
-- Name: sp_obtener_docente_por_id(integer); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.sp_obtener_docente_por_id(p_id integer) RETURNS SETOF academico.docente
    LANGUAGE plpgsql
    AS $$
BEGIN
    RETURN QUERY SELECT * FROM public.docente WHERE id_docente = p_id;
END;
$$;


--
-- Name: estudiante; Type: TABLE; Schema: academico; Owner: -
--

CREATE TABLE academico.estudiante (
    id_estudiante integer NOT NULL,
    id_usuario integer NOT NULL,
    id_carrera integer NOT NULL,
    matricula character varying(30) NOT NULL,
    semestre integer NOT NULL,
    estado_academico character varying(30)
);


--
-- Name: sp_obtener_estudiante_por_matricula(character varying); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.sp_obtener_estudiante_por_matricula(p_matricula character varying) RETURNS SETOF academico.estudiante
    LANGUAGE plpgsql
    AS $$
BEGIN
    RETURN QUERY 
    SELECT * FROM public.estudiante 
    WHERE matricula = p_matricula;
END;
$$;


--
-- Name: evaluacion_meritos; Type: TABLE; Schema: postulacion; Owner: -
--

CREATE TABLE postulacion.evaluacion_meritos (
    id_evaluacion_meritos integer NOT NULL,
    id_postulacion integer NOT NULL,
    nota_asignatura numeric(5,2),
    nota_semestres numeric(5,2),
    nota_eventos numeric(5,2),
    nota_experiencia numeric(5,2),
    fecha_evaluacion date,
    nota_total_meritos numeric(5,2) GENERATED ALWAYS AS ((((nota_asignatura + nota_semestres) + nota_eventos) + nota_experiencia)) STORED,
    id_tipo_estado_evaluacion integer
);


--
-- Name: sp_obtener_evaluacion_meritos(integer); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.sp_obtener_evaluacion_meritos(p_id_postulacion integer) RETURNS SETOF postulacion.evaluacion_meritos
    LANGUAGE plpgsql
    AS $$
BEGIN
    RETURN QUERY 
    SELECT * FROM postulacion.evaluacion_meritos 
    WHERE id_postulacion = p_id_postulacion;
END;
$$;


--
-- Name: evidencia_registro_actividad; Type: TABLE; Schema: ayudantia; Owner: -
--

CREATE TABLE ayudantia.evidencia_registro_actividad (
    id_evidencia_registro_actividad integer CONSTRAINT evidencia_registro_activida_id_evidencia_registro_acti_not_null NOT NULL,
    id_registro_actividad integer NOT NULL,
    nombre_archivo character varying(150) NOT NULL,
    fecha_subida date DEFAULT CURRENT_DATE NOT NULL,
    activo boolean DEFAULT true NOT NULL,
    id_tipo_estado_evidencia integer NOT NULL,
    id_tipo_evidencia integer NOT NULL,
    ruta_archivo character varying(500) NOT NULL,
    mime_type character varying(100),
    tamanio_bytes integer,
    observaciones character varying(500),
    fecha_observacion date,
    CONSTRAINT chk_tamanio_positivo CHECK (((tamanio_bytes IS NULL) OR (tamanio_bytes > 0)))
);


--
-- Name: sp_obtener_evidencias_actividad(integer); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.sp_obtener_evidencias_actividad(p_id_actividad integer) RETURNS SETOF ayudantia.evidencia_registro_actividad
    LANGUAGE plpgsql
    AS $$
BEGIN
    RETURN QUERY 
    SELECT * FROM public.evidencia_registro_actividad 
    WHERE id_registro_actividad = p_id_actividad AND activo = TRUE;
END;
$$;


--
-- Name: sp_obtener_requisitos_postulacion(integer); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.sp_obtener_requisitos_postulacion(p_id_postulacion integer) RETURNS TABLE(id integer, nombre_archivo character varying, fecha date, estado_id integer)
    LANGUAGE plpgsql
    AS $$
BEGIN
    RETURN QUERY 
    SELECT ra.id_requisito_adjunto, 
           ra.nombre_archivo, 
           ra.fecha_subida, 
           ra.id_tipo_estado_requisito
    FROM postulacion.requisito_adjunto ra   
    WHERE ra.id_postulacion = p_id_postulacion;
END;
$$;


--
-- Name: sp_obtener_roles_usuario(integer); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.sp_obtener_roles_usuario(p_id_usuario integer) RETURNS TABLE(id_rol integer, estado boolean)
    LANGUAGE plpgsql
    AS $$
BEGIN
    RETURN QUERY 
    SELECT id_tipo_rol, activo
    FROM public.usuario_tipo_rol
    WHERE id_usuario = p_id_usuario AND activo = TRUE;
END;
$$;


--
-- Name: sp_promover_estudiante_a_ayudante(character varying, numeric); Type: PROCEDURE; Schema: public; Owner: -
--

CREATE PROCEDURE public.sp_promover_estudiante_a_ayudante(IN p_username_estudiante character varying, IN p_horas_asignadas numeric)
    LANGUAGE plpgsql
    AS $$
DECLARE
    v_id_usuario INTEGER;
    v_id_rol_ayudante INTEGER;
    v_user_lower VARCHAR;
BEGIN
    v_user_lower := LOWER(p_username_estudiante);

    SELECT id_usuario INTO v_id_usuario FROM public.usuario 
    WHERE nombre_usuario = v_user_lower;

    IF v_id_usuario IS NULL THEN
        RAISE EXCEPTION 'Usuario % no existe.', v_user_lower;
    END IF;

    SELECT id_tipo_rol INTO v_id_rol_ayudante FROM public.tipo_rol 
    WHERE nombre_tipo_rol = 'AYUDANTE_CATEDRA';

    IF v_id_rol_ayudante IS NULL THEN RAISE EXCEPTION 'Rol AYUDANTE no existe.'; END IF;

    INSERT INTO public.usuario_tipo_rol (id_usuario, id_tipo_rol, activo, fecha_creacion)
    VALUES (v_id_usuario, v_id_rol_ayudante, TRUE, CURRENT_DATE)
    ON CONFLICT (id_usuario, id_tipo_rol) DO NOTHING;

    INSERT INTO public.ayudante_catedra (id_usuario, horas_ayudante) 
    VALUES (v_id_usuario, p_horas_asignadas);

    EXECUTE format('GRANT role_ayudante_catedra TO %I', v_user_lower);
        
    RAISE NOTICE 'Estudiante % promovido a Ayudante.', v_user_lower;

EXCEPTION
    WHEN OTHERS THEN
        RAISE EXCEPTION 'Error promoviendo estudiante: %', SQLERRM;
END;
$$;


--
-- Name: sp_ranking_convocatoria(integer); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.sp_ranking_convocatoria(p_id_convocatoria integer) RETURNS TABLE(id_postulacion integer, nombre_estudiante text, matricula text, puntaje_meritos numeric, puntaje_oposicion numeric, puntaje_total numeric, posicion bigint)
    LANGUAGE plpgsql
    AS $$
BEGIN
    RETURN QUERY
    SELECT
        p.id_postulacion,
        (u.nombres || ' ' || u.apellidos)::TEXT AS nombre_estudiante,
        est.matricula::TEXT,
        COALESCE(em.puntaje_total, 0)::NUMERIC  AS puntaje_meritos,
        COALESCE(eo.puntaje_total, 0)::NUMERIC  AS puntaje_oposicion,
        (COALESCE(em.puntaje_total, 0) + COALESCE(eo.puntaje_total, 0))::NUMERIC AS puntaje_total,
        RANK() OVER (
            ORDER BY (COALESCE(em.puntaje_total, 0) + COALESCE(eo.puntaje_total, 0)) DESC
        ) AS posicion
    FROM postulacion.postulacion p
    JOIN academico.estudiante est  ON est.id_estudiante = p.id_estudiante
    JOIN seguridad.usuario u       ON u.id_usuario      = est.id_usuario
    LEFT JOIN postulacion.evaluacion_meritos em   ON em.id_postulacion = p.id_postulacion
    LEFT JOIN postulacion.evaluacion_oposicion eo ON eo.id_postulacion = p.id_postulacion
    WHERE p.id_convocatoria = p_id_convocatoria
    ORDER BY puntaje_total DESC;
END;
$$;


--
-- Name: sp_reemplazar_requisito_adjunto(integer, bytea, character varying, date); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.sp_reemplazar_requisito_adjunto(p_id_adjunto integer, p_archivo bytea, p_nombre character varying, p_fecha_subida date) RETURNS integer
    LANGUAGE plpgsql
    AS $$
DECLARE
    v_id_estado_pendiente INTEGER;
BEGIN
    -- Obtener el id del estado PENDIENTE/ENTREGADO (id = 1 por convención del proyecto)
    v_id_estado_pendiente := 1;

    UPDATE postulacion.requisito_adjunto
    SET archivo                = p_archivo,
        nombre_archivo         = p_nombre,
        fecha_subida           = p_fecha_subida,
        id_tipo_estado_requisito = v_id_estado_pendiente,
        observacion            = NULL
    WHERE id_requisito_adjunto = p_id_adjunto;

    IF NOT FOUND THEN
        RETURN -1;
    END IF;

    RETURN p_id_adjunto;
END;
$$;


--
-- Name: sp_registrar_administrador(character varying, character varying, character varying, character varying, character varying, character varying); Type: PROCEDURE; Schema: public; Owner: -
--

CREATE PROCEDURE public.sp_registrar_administrador(IN p_nombres character varying, IN p_apellidos character varying, IN p_cedula character varying, IN p_correo character varying, IN p_username character varying, IN p_password character varying)
    LANGUAGE plpgsql
    AS $$
DECLARE
    v_id_usuario_creado INTEGER;
    v_id_rol_admin INTEGER;
    v_user_lower VARCHAR;
BEGIN
    v_user_lower := LOWER(p_username);

    SELECT id_tipo_rol INTO v_id_rol_admin FROM seguridad.tipo_rol WHERE nombre_tipo_rol = 'ADMINISTRADOR';
    IF v_id_rol_admin IS NULL THEN
        RAISE EXCEPTION 'El rol ADMINISTRADOR no existe en la BD.';
    END IF;

    INSERT INTO seguridad.usuario (
        nombres, apellidos, cedula, correo, nombre_usuario, contrasenia_usuario, fecha_creacion, activo
    ) VALUES (
        p_nombres, p_apellidos, p_cedula, p_correo, v_user_lower, 
        public.crypt(p_password, public.gen_salt('bf')),
        CURRENT_DATE, TRUE
    ) RETURNING id_usuario INTO v_id_usuario_creado;

    INSERT INTO seguridad.usuario_tipo_rol (id_usuario, id_tipo_rol, activo, fecha_creacion)
    VALUES (v_id_usuario_creado, v_id_rol_admin, TRUE, CURRENT_DATE);

    EXECUTE format('CREATE USER %I WITH PASSWORD %L', v_user_lower, p_password);
    EXECUTE format('GRANT role_administrador TO %I', v_user_lower);
    EXECUTE format('GRANT %I TO app_user_default', v_user_lower);

    RAISE NOTICE 'Administrador % registrado exitosamente.', v_user_lower;

EXCEPTION
    WHEN OTHERS THEN
        RAISE EXCEPTION 'Error al registrar Administrador: % (SQLSTATE: %)', SQLERRM, SQLSTATE;
END;
$$;


--
-- Name: sp_registrar_ayudante_directo(character varying, character varying, character varying, character varying, character varying, character varying, numeric); Type: PROCEDURE; Schema: public; Owner: -
--

CREATE PROCEDURE public.sp_registrar_ayudante_directo(IN p_nombres character varying, IN p_apellidos character varying, IN p_cedula character varying, IN p_correo character varying, IN p_username character varying, IN p_password character varying, IN p_horas_asignadas numeric)
    LANGUAGE plpgsql
    AS $$
DECLARE
    v_id_usuario_creado INTEGER;
    v_id_rol_ayudante INTEGER;
    v_user_lower VARCHAR;
BEGIN
    v_user_lower := LOWER(p_username);

    SELECT id_tipo_rol INTO v_id_rol_ayudante FROM seguridad.tipo_rol WHERE nombre_tipo_rol = 'AYUDANTE_CATEDRA';
    IF v_id_rol_ayudante IS NULL THEN RAISE EXCEPTION 'Rol AYUDANTE_CATEDRA no encontrado'; END IF;

    INSERT INTO seguridad.usuario (
        nombres, apellidos, cedula, correo, nombre_usuario, 
        contrasenia_usuario, fecha_creacion, activo
    ) VALUES (
        p_nombres, p_apellidos, p_cedula, p_correo, v_user_lower, 
        public.crypt(p_password, public.gen_salt('bf')), 
        CURRENT_DATE, TRUE
    ) RETURNING id_usuario INTO v_id_usuario_creado;

    INSERT INTO seguridad.usuario_tipo_rol (id_usuario, id_tipo_rol, activo, fecha_creacion)
    VALUES (v_id_usuario_creado, v_id_rol_ayudante, TRUE, CURRENT_DATE);

    INSERT INTO ayudantia.ayudante_catedra (id_usuario, horas_ayudante)
    VALUES (v_id_usuario_creado, p_horas_asignadas);

    EXECUTE format('CREATE USER %I WITH PASSWORD %L', v_user_lower, p_password);
    EXECUTE format('GRANT role_ayudante_catedra TO %I', v_user_lower);
	EXECUTE format('GRANT %I TO app_user_default', v_user_lower);

    RAISE NOTICE 'Ayudante % registrado correctamente.', v_user_lower;

EXCEPTION
    WHEN OTHERS THEN
        RAISE EXCEPTION 'Error registro Ayudante: %', SQLERRM;
END;
$$;


--
-- Name: sp_registrar_coordinador(character varying, character varying, character varying, character varying, character varying, character varying, integer); Type: PROCEDURE; Schema: public; Owner: -
--

CREATE PROCEDURE public.sp_registrar_coordinador(IN p_nombres character varying, IN p_apellidos character varying, IN p_cedula character varying, IN p_correo character varying, IN p_username character varying, IN p_password character varying, IN p_id_carrera integer)
    LANGUAGE plpgsql
    AS $$
DECLARE
    v_id_usuario_creado INTEGER;
    v_id_rol_coordinador INTEGER;
    v_user_lower VARCHAR;
BEGIN
    v_user_lower := LOWER(p_username);

    SELECT id_tipo_rol INTO v_id_rol_coordinador FROM seguridad.tipo_rol WHERE nombre_tipo_rol = 'COORDINADOR';
    IF v_id_rol_coordinador IS NULL THEN RAISE EXCEPTION 'Rol COORDINADOR no encontrado'; END IF;

    INSERT INTO seguridad.usuario (
        nombres, apellidos, cedula, correo, nombre_usuario, contrasenia_usuario, fecha_creacion, activo
    ) VALUES (
        p_nombres, p_apellidos, p_cedula, p_correo, v_user_lower, 
        public.crypt(p_password, public.gen_salt('bf')), 
        CURRENT_DATE, TRUE
    ) RETURNING id_usuario INTO v_id_usuario_creado;

    INSERT INTO seguridad.usuario_tipo_rol (id_usuario, id_tipo_rol, activo, fecha_creacion)
    VALUES (v_id_usuario_creado, v_id_rol_coordinador, TRUE, CURRENT_DATE);

    INSERT INTO academico.coordinador (id_usuario, id_carrera, fecha_inicio, fecha_fin, activo)
    VALUES (v_id_usuario_creado, p_id_carrera, CURRENT_DATE, NULL, TRUE);

    EXECUTE format('CREATE USER %I WITH PASSWORD %L', v_user_lower, p_password);
    EXECUTE format('GRANT role_coordinador TO %I', v_user_lower);
    EXECUTE format('GRANT %I TO app_user_default', v_user_lower);

    RAISE NOTICE 'Coordinador % creado.', v_user_lower;

EXCEPTION
    WHEN OTHERS THEN RAISE EXCEPTION 'Fallo Coordinador: %', SQLERRM;
END;
$$;


--
-- Name: sp_registrar_decano(character varying, character varying, character varying, character varying, character varying, character varying, integer); Type: PROCEDURE; Schema: public; Owner: -
--

CREATE PROCEDURE public.sp_registrar_decano(IN p_nombres character varying, IN p_apellidos character varying, IN p_cedula character varying, IN p_correo character varying, IN p_username character varying, IN p_password character varying, IN p_id_facultad integer)
    LANGUAGE plpgsql
    AS $$
DECLARE
    v_id_usuario_creado INTEGER;
    v_id_rol_decano INTEGER;
    v_user_lower VARCHAR;
BEGIN
    v_user_lower := LOWER(p_username);

    SELECT id_tipo_rol 
    INTO v_id_rol_decano 
    FROM seguridad.tipo_rol 
    WHERE nombre_tipo_rol = 'DECANO';
    
    IF v_id_rol_decano IS NULL THEN
        RAISE EXCEPTION 
        'Error: El rol DECANO no existe en la tabla tipo_rol.';
    END IF;

    INSERT INTO seguridad.usuario (
        nombres, apellidos, cedula, correo, 
        nombre_usuario, contrasenia_usuario, 
        fecha_creacion, activo
    ) VALUES (
        p_nombres, p_apellidos, p_cedula, p_correo, 
        v_user_lower, 
        crypt(p_password, gen_salt('bf')), 
        CURRENT_DATE, TRUE
    ) 
    RETURNING id_usuario INTO v_id_usuario_creado;

    INSERT INTO seguridad.usuario_tipo_rol (
        id_usuario, id_tipo_rol, activo, fecha_creacion
    )
    VALUES (
        v_id_usuario_creado, v_id_rol_decano, TRUE, CURRENT_DATE
    );

    INSERT INTO academico.decano (
        id_usuario, id_facultad, fecha_inicio_gestion, activo
    )
    VALUES (
        v_id_usuario_creado, p_id_facultad, CURRENT_DATE, TRUE
    );

    EXECUTE format('CREATE USER %I WITH PASSWORD %L', v_user_lower, p_password);
    EXECUTE format('GRANT role_decano TO %I', v_user_lower);
    EXECUTE format('GRANT %I TO app_user_default', v_user_lower);
    RAISE NOTICE 'Decano % registrado exitosamente.', v_user_lower;

EXCEPTION
    WHEN OTHERS THEN
        RAISE EXCEPTION 
        'Error al registrar Decano: % (SQLSTATE: %)', SQLERRM, SQLSTATE;
END;
$$;


--
-- Name: sp_registrar_docente(character varying, character varying, character varying, character varying, character varying, character varying); Type: PROCEDURE; Schema: public; Owner: -
--

CREATE PROCEDURE public.sp_registrar_docente(IN p_nombres character varying, IN p_apellidos character varying, IN p_cedula character varying, IN p_correo character varying, IN p_username character varying, IN p_password character varying)
    LANGUAGE plpgsql
    AS $$
DECLARE
    v_id_usuario_creado INTEGER;
    v_id_rol_docente INTEGER;
    v_user_lower VARCHAR;
BEGIN
    v_user_lower := LOWER(p_username);

    SELECT id_tipo_rol 
    INTO v_id_rol_docente 
    FROM seguridad.tipo_rol 
    WHERE nombre_tipo_rol = 'DOCENTE';
    
    IF v_id_rol_docente IS NULL THEN
        RAISE EXCEPTION 'Error: El rol DOCENTE no existe en la tabla tipo_rol.';
    END IF;

    INSERT INTO seguridad.usuario (
        nombres, apellidos, cedula, correo, 
        nombre_usuario, contrasenia_usuario, 
        fecha_creacion, activo
    ) VALUES (
        p_nombres, p_apellidos, p_cedula, p_correo, 
        v_user_lower, 
        crypt(p_password, gen_salt('bf')), 
        CURRENT_DATE, TRUE
    ) 
    RETURNING id_usuario INTO v_id_usuario_creado;

    INSERT INTO seguridad.usuario_tipo_rol (
        id_usuario, id_tipo_rol, activo, fecha_creacion
    )
    VALUES (
        v_id_usuario_creado, v_id_rol_docente, TRUE, CURRENT_DATE
    );

    INSERT INTO academico.docente (
        id_usuario, fecha_inicio, activo
    )
    VALUES (
        v_id_usuario_creado, CURRENT_DATE, TRUE
    );

    EXECUTE format('CREATE USER %I WITH PASSWORD %L', v_user_lower, p_password);
    EXECUTE format('GRANT role_docente TO %I', v_user_lower);
    EXECUTE format('GRANT %I TO app_user_default', v_user_lower);
    RAISE NOTICE 'Docente % registrado exitosamente con permisos de BD.', v_user_lower;

EXCEPTION
    WHEN OTHERS THEN
        RAISE EXCEPTION 
        'Error al registrar Docente: % (SQLSTATE: %)', SQLERRM, SQLSTATE;
END;
$$;


--
-- Name: sp_registrar_estudiante(character varying, character varying, character varying, character varying, character varying, character varying, integer, character varying, integer); Type: PROCEDURE; Schema: public; Owner: -
--

CREATE PROCEDURE public.sp_registrar_estudiante(IN p_nombres character varying, IN p_apellidos character varying, IN p_cedula character varying, IN p_correo character varying, IN p_username character varying, IN p_password character varying, IN p_id_carrera integer, IN p_matricula character varying, IN p_semestre integer)
    LANGUAGE plpgsql
    AS $$
DECLARE
    v_id_usuario_creado INTEGER;
    v_id_rol_estudiante INTEGER;
    v_user_lower VARCHAR;
BEGIN
    v_user_lower := LOWER(p_username);

    SELECT id_tipo_rol INTO v_id_rol_estudiante 
    FROM seguridad.tipo_rol 
    WHERE nombre_tipo_rol = 'ESTUDIANTE';

    IF v_id_rol_estudiante IS NULL THEN 
        RAISE EXCEPTION 'Rol ESTUDIANTE no encontrado'; 
    END IF;

    INSERT INTO seguridad.usuario (
        nombres, apellidos, cedula, correo, nombre_usuario, 
        contrasenia_usuario, fecha_creacion, activo
    ) VALUES (
        p_nombres, p_apellidos, p_cedula, p_correo, v_user_lower, 
        crypt(p_password, gen_salt('bf')),
        CURRENT_DATE, TRUE
    ) RETURNING id_usuario INTO v_id_usuario_creado;

    INSERT INTO seguridad.usuario_tipo_rol 
        (id_usuario, id_tipo_rol, activo, fecha_creacion)
    VALUES 
        (v_id_usuario_creado, v_id_rol_estudiante, TRUE, CURRENT_DATE);

    INSERT INTO academico.estudiante (
        id_usuario, id_carrera, matricula, semestre, estado_academico
    ) VALUES (
        v_id_usuario_creado, p_id_carrera, p_matricula, p_semestre, 'ACTIVO'
    );

    EXECUTE format('CREATE USER %I WITH PASSWORD %L', v_user_lower, p_password);
    EXECUTE format('GRANT role_estudiante TO %I', v_user_lower);
    EXECUTE format('GRANT %I TO app_user_default', v_user_lower);

END;
$$;


--
-- Name: sp_resumen_docente(integer); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.sp_resumen_docente(p_id_usuario_docente integer) RETURNS TABLE(total_ayudantes bigint, actividades_pendientes bigint, actividades_aceptadas bigint, actividades_rechazadas bigint, actividades_observadas bigint, total_actividades bigint)
    LANGUAGE sql STABLE
    AS $$
    SELECT
        COUNT(DISTINCT ay.id_ayudantia)                                        AS total_ayudantes,
        COUNT(ra.id_registro_actividad)
            FILTER (WHERE UPPER(ter.nombre_estado) = 'PENDIENTE')              AS actividades_pendientes,
        COUNT(ra.id_registro_actividad)
            FILTER (WHERE UPPER(ter.nombre_estado) = 'ACEPTADO')               AS actividades_aceptadas,
        COUNT(ra.id_registro_actividad)
            FILTER (WHERE UPPER(ter.nombre_estado) = 'RECHAZADO')              AS actividades_rechazadas,
        COUNT(ra.id_registro_actividad)
            FILTER (WHERE UPPER(ter.nombre_estado) = 'OBSERVADO')              AS actividades_observadas,
        COUNT(ra.id_registro_actividad)                                        AS total_actividades
    FROM ayudantia.ayudantia ay
    JOIN postulacion.postulacion pp      ON pp.id_postulacion  = ay.id_postulacion
    JOIN convocatoria.convocatoria cv    ON cv.id_convocatoria = pp.id_convocatoria
    JOIN academico.docente dc            ON dc.id_docente      = cv.id_docente
    JOIN seguridad.usuario u_doc         ON u_doc.id_usuario   = dc.id_usuario
    LEFT JOIN ayudantia.registro_actividad ra
                                         ON ra.id_ayudantia    = ay.id_ayudantia
    LEFT JOIN ayudantia.tipo_estado_registro ter
                                         ON ter.id_tipo_estado_registro = ra.id_tipo_estado_registro
    WHERE u_doc.id_usuario = p_id_usuario_docente;
$$;


--
-- Name: sp_validar_usuario(character varying); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.sp_validar_usuario(p_nombre_usuario character varying) RETURNS TABLE(id integer, pass character varying, es_activo boolean)
    LANGUAGE plpgsql
    AS $$
BEGIN
    RETURN QUERY 
    SELECT id_usuario, contrasenia_usuario, activo
    FROM public.usuario
    WHERE nombre_usuario = p_nombre_usuario;
END;
$$;


--
-- Name: fn_actualizar_privilegio(integer, character varying, character varying, text); Type: FUNCTION; Schema: seguridad; Owner: -
--

CREATE FUNCTION seguridad.fn_actualizar_privilegio(p_id integer, p_nombre_privilegio character varying, p_codigo_interno character varying, p_descripcion text) RETURNS jsonb
    LANGUAGE plpgsql SECURITY DEFINER
    AS $$
BEGIN
    -- Validar que el registro exista
    IF NOT EXISTS (
        SELECT 1 FROM seguridad.privilegio
        WHERE id_privilegio = p_id
    ) THEN
        RETURN jsonb_build_object('exito', false, 'mensaje', 'No se encontró el privilegio con el ID especificado');
    END IF;

    -- Validar que el nombre no exista en otro registro (case-insensitive)
    IF EXISTS (
        SELECT 1 FROM seguridad.privilegio
        WHERE LOWER(TRIM(nombre_privilegio)) = LOWER(TRIM(p_nombre_privilegio))
        AND id_privilegio != p_id
    ) THEN
        RETURN jsonb_build_object('exito', false, 'mensaje', 'Ya existe otro privilegio con el nombre especificado');
    END IF;

    -- Validar que el código interno no exista en otro registro si está definido (case-insensitive)
    IF p_codigo_interno IS NOT NULL AND EXISTS (
        SELECT 1 FROM seguridad.privilegio
        WHERE LOWER(TRIM(codigo_interno)) = LOWER(TRIM(p_codigo_interno))
        AND id_privilegio != p_id
    ) THEN
        RETURN jsonb_build_object('exito', false, 'mensaje', 'Ya existe otro privilegio con el código interno especificado');
    END IF;

    -- Actualizar el registro
    UPDATE seguridad.privilegio
    SET nombre_privilegio = UPPER(TRIM(p_nombre_privilegio)),
        codigo_interno = UPPER(TRIM(p_codigo_interno)),
        descripcion = TRIM(p_descripcion)
    WHERE id_privilegio = p_id;

    RETURN jsonb_build_object(
        'exito', true,
        'mensaje', 'Privilegio actualizado exitosamente',
        'id', p_id
    );

EXCEPTION
    WHEN unique_violation THEN
        RETURN jsonb_build_object('exito', false, 'mensaje', 'Error de duplicidad: el nombre de privilegio ya existe');
    WHEN OTHERS THEN
        RETURN jsonb_build_object('exito', false, 'mensaje', 'Error al actualizar privilegio: ' || SQLERRM);
END;
$$;


--
-- Name: fn_consultar_permisos_rol(character varying, character varying, character varying, character varying); Type: FUNCTION; Schema: seguridad; Owner: -
--

CREATE FUNCTION seguridad.fn_consultar_permisos_rol(p_rol_bd character varying, p_esquema character varying DEFAULT 'todo'::character varying, p_categoria character varying DEFAULT 'todo'::character varying, p_privilegio character varying DEFAULT 'todo'::character varying) RETURNS TABLE(esquema character varying, elemento character varying, categoria character varying, privilegio character varying)
    LANGUAGE plpgsql SECURITY DEFINER
    AS $$
BEGIN
    RETURN QUERY
        WITH acl_data AS (
            -- 1. Permisos de Tablas y Vistas
            SELECT
                n.nspname::varchar as esc,
                c.relname::varchar as elm,
                CASE
                    WHEN c.relkind = 'r' THEN 'TABLA'
                    WHEN c.relkind = 'v' THEN 'VISTA'
                    WHEN c.relkind = 'm' THEN 'VISTA_MATERIALIZADA'
                    ELSE 'OTRO'
                    END::text as cat, -- Lo tratamos como text internamente
                (aclexplode(COALESCE(c.relacl, acldefault('r', c.relnamespace)))).grantee as grantee_oid,
                (aclexplode(COALESCE(c.relacl, acldefault('r', c.relnamespace)))).privilege_type::text as priv
            FROM pg_class c
                     JOIN pg_namespace n ON n.oid = c.relnamespace
            WHERE c.relkind IN ('r', 'v', 'm')
              AND n.nspname NOT IN ('pg_catalog', 'information_schema')

            UNION ALL

            -- 2. Permisos de Funciones y Procedimientos
            SELECT
                n.nspname::varchar as esc,
                p.proname::varchar as elm,
                CASE WHEN p.prokind = 'p' THEN 'PROCEDIMIENTO' ELSE 'FUNCION' END::text as cat,
                (aclexplode(COALESCE(p.proacl, acldefault('f', p.pronamespace)))).grantee as grantee_oid,
                (aclexplode(COALESCE(p.proacl, acldefault('f', p.pronamespace)))).privilege_type::text as priv
            FROM pg_proc p
                     JOIN pg_namespace n ON n.oid = p.pronamespace
            WHERE n.nspname NOT IN ('pg_catalog', 'information_schema')
        )
        SELECT
            ad.esc::varchar,     -- Forzamos casting final a varchar
            ad.elm::varchar,     -- Forzamos casting final a varchar
            ad.cat::varchar,     -- Esto resuelve el error en la posición 3
            ad.priv::varchar     -- Forzamos casting final a varchar
        FROM acl_data ad
        WHERE lower(pg_get_userbyid(ad.grantee_oid)) = lower(p_rol_bd)
          AND (p_esquema = 'todo' OR lower(ad.esc) = lower(p_esquema))
          AND (p_categoria = 'todo' OR lower(ad.cat) = lower(p_categoria))
          AND (p_privilegio = 'todo' OR lower(ad.priv) = lower(p_privilegio))
        ORDER BY ad.esc, ad.elm, ad.priv;
END;
$$;


--
-- Name: fn_crear_privilegio(character varying, character varying, text); Type: FUNCTION; Schema: seguridad; Owner: -
--

CREATE FUNCTION seguridad.fn_crear_privilegio(p_nombre_privilegio character varying, p_codigo_interno character varying, p_descripcion text) RETURNS jsonb
    LANGUAGE plpgsql SECURITY DEFINER
    AS $$
DECLARE
    v_id INTEGER;
BEGIN
    -- Validar que el nombre no exista (case-insensitive)
    IF EXISTS (
        SELECT 1 FROM seguridad.privilegio
        WHERE LOWER(TRIM(nombre_privilegio)) = LOWER(TRIM(p_nombre_privilegio))
    ) THEN
        RETURN jsonb_build_object('exito', false, 'mensaje', 'Ya existe un privilegio con el nombre especificado');
    END IF;

    -- Validar que el código interno no exista si está definido (case-insensitive)
    IF p_codigo_interno IS NOT NULL AND EXISTS (
        SELECT 1 FROM seguridad.privilegio
        WHERE LOWER(TRIM(codigo_interno)) = LOWER(TRIM(p_codigo_interno))
    ) THEN
        RETURN jsonb_build_object('exito', false, 'mensaje', 'Ya existe un privilegio con el código interno especificado');
    END IF;

    -- Insertar el nuevo registro
    INSERT INTO seguridad.privilegio (nombre_privilegio, codigo_interno, descripcion, activo)
    VALUES (UPPER(TRIM(p_nombre_privilegio)), UPPER(TRIM(p_codigo_interno)), TRIM(p_descripcion), TRUE)
    RETURNING id_privilegio INTO v_id;

    RETURN jsonb_build_object(
        'exito', true,
        'mensaje', 'Privilegio creado exitosamente',
        'id', v_id
    );

EXCEPTION
    WHEN unique_violation THEN
        RETURN jsonb_build_object('exito', false, 'mensaje', 'Error de duplicidad: el nombre de privilegio ya existe');
    WHEN OTHERS THEN
        RETURN jsonb_build_object('exito', false, 'mensaje', 'Error al crear privilegio: ' || SQLERRM);
END;
$$;


--
-- Name: fn_crear_roles_iniciales(); Type: FUNCTION; Schema: seguridad; Owner: -
--

CREATE FUNCTION seguridad.fn_crear_roles_iniciales() RETURNS void
    LANGUAGE plpgsql SECURITY DEFINER
    AS $$
BEGIN
    IF NOT EXISTS (SELECT FROM pg_catalog.pg_roles WHERE rolname = 'role_administrador') THEN
        CREATE ROLE role_administrador NOLOGIN;

    END IF;
END;
$$;


--
-- Name: fn_eliminar_privilegio(integer); Type: FUNCTION; Schema: seguridad; Owner: -
--

CREATE FUNCTION seguridad.fn_eliminar_privilegio(p_id integer) RETURNS jsonb
    LANGUAGE plpgsql SECURITY DEFINER
    AS $$
BEGIN
    -- Validar que el registro exista
    IF NOT EXISTS (
        SELECT 1 FROM seguridad.privilegio
        WHERE id_privilegio = p_id
    ) THEN
        RETURN jsonb_build_object('exito', false, 'mensaje', 'No se encontró el privilegio con el ID especificado');
    END IF;

    -- Eliminación lógica: setear activo = false
    UPDATE seguridad.privilegio
    SET activo = FALSE
    WHERE id_privilegio = p_id;

    RETURN jsonb_build_object(
        'exito', true,
        'mensaje', 'Privilegio desactivado exitosamente',
        'id', p_id
    );

EXCEPTION
    WHEN OTHERS THEN
        RETURN jsonb_build_object('exito', false, 'mensaje', 'Error al desactivar privilegio: ' || SQLERRM);
END;
$$;


--
-- Name: fn_finalizar_proceso_seleccion(integer); Type: FUNCTION; Schema: seguridad; Owner: -
--

CREATE FUNCTION seguridad.fn_finalizar_proceso_seleccion(p_id_convocatoria integer) RETURNS jsonb
    LANGUAGE plpgsql SECURITY DEFINER
    AS $$
DECLARE
    v_cupos            INTEGER;
    v_posicion         INTEGER := 0;
    v_id_sel           INTEGER;
    v_id_ele           INTEGER;
    v_id_no_sel        INTEGER;
    v_id_est_activa    INTEGER;
    v_horas_semanales  NUMERIC(5,2) := 20.00;   -- horas semanales por defecto
    v_horas_maximas    NUMERIC(5,2);
    v_periodo_academico    INTEGER;
    v_seleccionados    INTEGER := 0;
    v_semanas          INTEGER :=16;
    v_elegibles        INTEGER := 0;
    v_no_sel           INTEGER := 0;
    v_rec              RECORD;
BEGIN

    -- ── Validar que la convocatoria existe y sigue activa ─────────────
    SELECT cupos_disponibles, id_periodo_academico
    INTO   v_cupos, v_periodo_academico
    FROM   convocatoria.convocatoria
    WHERE  id_convocatoria = p_id_convocatoria
      AND  activo = TRUE;

    IF NOT FOUND THEN
        RAISE EXCEPTION 'VALIDACION: La convocatoria % no existe o ya fue cerrada.', p_id_convocatoria;
    END IF;

    -- ── Recuperar IDs de estados de postulación ────────────────────────
    SELECT id_tipo_estado_postulacion INTO v_id_sel
    FROM   postulacion.tipo_estado_postulacion WHERE codigo = 'SELECCIONADO' LIMIT 1;

    SELECT id_tipo_estado_postulacion INTO v_id_ele
    FROM   postulacion.tipo_estado_postulacion WHERE codigo = 'ELEGIBLE' LIMIT 1;

    SELECT id_tipo_estado_postulacion INTO v_id_no_sel
    FROM   postulacion.tipo_estado_postulacion WHERE codigo = 'NO_SELECCIONADO' LIMIT 1;

    IF v_id_sel IS NULL OR v_id_ele IS NULL OR v_id_no_sel IS NULL THEN
        RAISE EXCEPTION
            'VALIDACION: Los estados SELECCIONADO / ELEGIBLE / NO_SELECCIONADO no están configurados en tipo_estado_postulacion.';
    END IF;

    -- ── Recuperar ID del estado ACTIVA de ayudantía ────────────────────
    SELECT id_tipo_estado_ayudantia INTO v_id_est_activa
    FROM   ayudantia.tipo_estado_ayudantia WHERE codigo = 'ACTIVO' LIMIT 1;

    IF v_id_est_activa IS NULL THEN
        RAISE EXCEPTION
            'VALIDACION: El estado ACTIVA no está configurado en tipo_estado_ayudantia.';
    END IF;


    SELECT
        COALESCE(
                EXTRACT(DAY FROM (pf.fecha_fin - pf.fecha_inicio))::INTEGER / 7,
                16
        )
    INTO v_semanas
    FROM planificacion.periodo_fase pf
             JOIN planificacion.tipo_fase tf ON tf.id_tipo_fase = pf.id_tipo_fase
    WHERE pf.id_periodo_academico = v_periodo_academico
      AND tf.codigo = 'EJECUCION_ACTIVIDADES';

    v_horas_maximas := v_horas_semanales * v_semanas;

    FOR v_rec IN
        SELECT
            po.id_postulacion,
            u.nombre_usuario,
            u.nombres || ' ' || u.apellidos            AS nombre_completo,
            ROUND(
                    COALESCE(em.nota_total_meritos,          0) +
                    COALESCE(eo.puntaje_total_oposicion,     0)
                , 2)::NUMERIC(5,2)                         AS puntaje_total
        FROM   postulacion.postulacion               po
                   JOIN   academico.estudiante                  est ON est.id_estudiante = po.id_estudiante
                   JOIN   seguridad.usuario                     u   ON u.id_usuario      = est.id_usuario
            -- Evaluación de méritos más reciente (cualquier estado)
                   LEFT JOIN LATERAL (
            SELECT nota_total_meritos
            FROM   postulacion.evaluacion_meritos
            WHERE  id_postulacion = po.id_postulacion
            ORDER  BY id_evaluacion_meritos DESC
            LIMIT  1
            ) em ON TRUE
                   LEFT JOIN LATERAL (
            SELECT eo2.puntaje_total_oposicion
            FROM   postulacion.evaluacion_oposicion  eo2
                       JOIN   postulacion.tipo_estado_evaluacion tee
                              ON tee.id_tipo_estado_evaluacion = eo2.id_tipo_estado_evaluacion
            WHERE  eo2.id_postulacion = po.id_postulacion
              AND  tee.codigo         = 'FINALIZADA'
            ORDER  BY eo2.id_evaluacion_oposicion DESC
            LIMIT  1
            ) eo ON TRUE
        WHERE  po.id_convocatoria = p_id_convocatoria
          AND  po.activo          = TRUE
        ORDER  BY puntaje_total DESC,
                  nombre_completo ASC
        LOOP
            v_posicion := v_posicion + 1;

            IF v_rec.puntaje_total < 25.00 THEN
                UPDATE postulacion.postulacion
                SET    id_tipo_estado_postulacion = v_id_no_sel
                WHERE  id_postulacion             = v_rec.id_postulacion;
                v_no_sel := v_no_sel + 1;

            ELSIF v_posicion <= v_cupos THEN
                UPDATE postulacion.postulacion
                SET    id_tipo_estado_postulacion = v_id_sel
                WHERE  id_postulacion             = v_rec.id_postulacion;

                CALL sp_promover_estudiante_a_ayudante(
                        v_rec.nombre_usuario,
                        v_horas_semanales
                     );

                IF NOT EXISTS (
                    SELECT 1 FROM ayudantia.ayudantia
                    WHERE id_postulacion = v_rec.id_postulacion
                ) THEN
                    INSERT INTO ayudantia.ayudantia (
                        id_tipo_estado_ayudantia,
                        id_postulacion,
                        fecha_inicio,
                        horas_semanales_max,
                        horas_maximas
                    ) VALUES (
                                 v_id_est_activa,
                                 v_rec.id_postulacion,
                                 CURRENT_DATE,
                                 v_horas_semanales,
                                 v_horas_maximas
                             );
                END IF;

                v_seleccionados := v_seleccionados + 1;

            ELSE
                UPDATE postulacion.postulacion
                SET    id_tipo_estado_postulacion = v_id_ele
                WHERE  id_postulacion             = v_rec.id_postulacion;
                v_elegibles := v_elegibles + 1;
            END IF;

        END LOOP;

    UPDATE convocatoria.convocatoria
    SET    activo = FALSE,
           estado = 'RESUELTA'
    WHERE  id_convocatoria = p_id_convocatoria;

    RAISE NOTICE '[fn_finalizar_proceso_seleccion] conv=% → sel=%, ele=%, noSel=%',
        p_id_convocatoria, v_seleccionados, v_elegibles, v_no_sel;

    RETURN jsonb_build_object(
            'exito',           TRUE,
            'seleccionados',   v_seleccionados,
            'elegibles',       v_elegibles,
            'noSeleccionados', v_no_sel,
            'mensaje',
            format(
                    'Proceso finalizado. %s seleccionado(s), %s elegible(s), %s no seleccionado(s). Convocatoria cerrada.',
                    v_seleccionados, v_elegibles, v_no_sel
            )
           );

EXCEPTION
    WHEN OTHERS THEN
        IF SQLERRM LIKE 'VALIDACION:%' OR SQLERRM LIKE 'ACCESO:%' THEN RAISE; END IF;
        RAISE EXCEPTION 'Error en fn_finalizar_proceso_seleccion(conv=%): % (SQLSTATE: %)',
            p_id_convocatoria, SQLERRM, SQLSTATE;
END;
$$;


--
-- Name: FUNCTION fn_finalizar_proceso_seleccion(p_id_convocatoria integer); Type: COMMENT; Schema: seguridad; Owner: -
--

COMMENT ON FUNCTION seguridad.fn_finalizar_proceso_seleccion(p_id_convocatoria integer) IS 'Cierre maestro de la fase de selección. Asigna SELECCIONADO / ELEGIBLE / NO_SELECCIONADO, promueve ganadores a AYUDANTE_CATEDRA, crea su ayudantia y marca la convocatoria como RESUELTA.';


--
-- Name: fn_gestionar_banco_temas(integer, text, jsonb); Type: FUNCTION; Schema: seguridad; Owner: -
--

CREATE FUNCTION seguridad.fn_gestionar_banco_temas(p_id_convocatoria integer, p_accion text, p_temas_json jsonb DEFAULT '[]'::jsonb) RETURNS jsonb
    LANGUAGE plpgsql SECURITY DEFINER
    AS $$
DECLARE
    v_tema          JSONB;
    v_contador      INTEGER := 0;
    v_total_temas   INTEGER;
    v_total_aptos   INTEGER;
BEGIN
    p_accion := UPPER(TRIM(COALESCE(p_accion, '')));

    -- Verificar que la convocatoria exista y esté activa
    IF NOT EXISTS (
        SELECT 1 FROM convocatoria.convocatoria
        WHERE id_convocatoria = p_id_convocatoria AND activo = TRUE
    ) THEN
        RETURN jsonb_build_object(
                'exito', false,
                'mensaje', 'La convocatoria no existe o está inactiva.'
               );
    END IF;

    IF p_accion = 'LISTAR' THEN
        SELECT COUNT(*) INTO v_total_aptos
        FROM  postulacion.postulacion po
                  JOIN  postulacion.tipo_estado_postulacion tep
                        ON tep.id_tipo_estado_postulacion = po.id_tipo_estado_postulacion
        WHERE po.id_convocatoria = p_id_convocatoria
          AND po.activo = TRUE
          AND tep.codigo = 'APROBADA';

        SELECT COUNT(*) INTO v_total_temas
        FROM postulacion.banco_temas
        WHERE id_convocatoria = p_id_convocatoria
          AND activo = TRUE;

        RETURN jsonb_build_object(
                'exito',           true,
                'temas',           COALESCE(
                        (SELECT jsonb_agg(jsonb_build_object(
                                                  'idTema',          bt.id_tema,
                                                  'descripcionTema', bt.descripcion_tema,
                                                  'activo',          bt.activo
                                          ) ORDER BY bt.id_tema)
                         FROM postulacion.banco_temas bt
                         WHERE bt.id_convocatoria = p_id_convocatoria
                           AND bt.activo = TRUE),
                        '[]'::jsonb
                                   ),
                'totalTemas',      v_total_temas,
                'totalAptos',      v_total_aptos,
                'listoParaSorteo', v_total_temas >= v_total_aptos AND v_total_aptos > 0
               );
    END IF;

    -- ── LIMPIAR ───────────────────────────────────────────────
    -- Solo borra si NO hay evaluaciones ya iniciadas (estado distinto a PROGRAMADA)
    IF p_accion = 'LIMPIAR' THEN
        IF EXISTS (
            SELECT 1
            FROM  postulacion.evaluacion_oposicion eo
                      JOIN  postulacion.tipo_estado_evaluacion tee
                            ON tee.id_tipo_estado_evaluacion = eo.id_tipo_estado_evaluacion
                      JOIN  postulacion.postulacion p ON p.id_postulacion = eo.id_postulacion
            WHERE p.id_convocatoria = p_id_convocatoria
              AND tee.codigo NOT IN ('PROGRAMADA')
        ) THEN
            RETURN jsonb_build_object(
                    'exito', false,
                    'mensaje', 'No se puede limpiar: ya existen evaluaciones en curso o finalizadas.'
                   );
        END IF;

        -- Desactivar temas (soft delete)
        UPDATE postulacion.banco_temas
        SET activo = FALSE
        WHERE id_convocatoria = p_id_convocatoria;

        GET DIAGNOSTICS v_contador = ROW_COUNT;
        RETURN jsonb_build_object(
                'exito',   true,
                'mensaje', v_contador || ' tema(s) eliminado(s) del banco.'
               );
    END IF;

    -- ── REGISTRAR ─────────────────────────────────────────────
    IF p_accion = 'REGISTRAR' THEN
        IF jsonb_array_length(p_temas_json) = 0 THEN
            RETURN jsonb_build_object('exito', false, 'mensaje', 'Debes enviar al menos un tema.');
        END IF;

        -- Insertar cada tema del array JSON
        FOR v_tema IN SELECT * FROM jsonb_array_elements(p_temas_json)
            LOOP
                INSERT INTO postulacion.banco_temas (id_convocatoria, descripcion_tema, activo)
                VALUES (
                           p_id_convocatoria,
                           TRIM(v_tema->>'descripcionTema'),
                           TRUE
                       );
                v_contador := v_contador + 1;
            END LOOP;

        -- Informar también cuántos temas hay ahora vs postulantes aptos
        SELECT COUNT(*) INTO v_total_temas
        FROM postulacion.banco_temas
        WHERE id_convocatoria = p_id_convocatoria AND activo = TRUE;

        SELECT COUNT(*) INTO v_total_aptos
        FROM  postulacion.postulacion          po
                  JOIN  postulacion.tipo_estado_postulacion tep
                        ON tep.id_tipo_estado_postulacion = po.id_tipo_estado_postulacion
        WHERE po.id_convocatoria = p_id_convocatoria
          AND po.activo = TRUE
          AND tep.codigo = 'APROBADA';

        RETURN jsonb_build_object(
                'exito',          true,
                'mensaje',        v_contador || ' tema(s) registrado(s) exitosamente.',
                'totalTemas',     v_total_temas,
                'totalAptos',     v_total_aptos,
                'listoParaSorteo', v_total_temas >= v_total_aptos AND v_total_aptos > 0
               );
    END IF;

    RETURN jsonb_build_object('exito', false, 'mensaje', 'Acción no reconocida: ' || p_accion);

EXCEPTION WHEN OTHERS THEN
    RETURN jsonb_build_object('exito', false, 'mensaje', '[ERROR] ' || SQLERRM);
END;
$$;


--
-- Name: fn_gestionar_permiso_rol(character varying, character varying, character varying, character varying, boolean); Type: FUNCTION; Schema: seguridad; Owner: -
--

CREATE FUNCTION seguridad.fn_gestionar_permiso_rol(p_nombre_rol character varying, p_esquema character varying, p_tabla character varying, p_privilegio character varying, p_otorgar boolean) RETURNS boolean
    LANGUAGE plpgsql SECURITY DEFINER
    AS $$
DECLARE
    v_rol_bd text;
    v_sql text;
BEGIN
    v_rol_bd := 'role_' || lower(trim(p_nombre_rol));

    IF upper(p_privilegio) NOT IN ('SELECT', 'INSERT', 'UPDATE', 'DELETE') THEN
        RAISE EXCEPTION 'Privilegio no permitido: %', p_privilegio;
    END IF;

    IF p_otorgar THEN
        v_sql := format('GRANT %s ON TABLE %I.%I TO %I', 
                        upper(p_privilegio), p_esquema, p_tabla, v_rol_bd);
    ELSE
        v_sql := format('REVOKE %s ON TABLE %I.%I FROM %I', 
                        upper(p_privilegio), p_esquema, p_tabla, v_rol_bd);
    END IF;

    EXECUTE v_sql;

    RETURN true;
EXCEPTION WHEN OTHERS THEN
    RAISE WARNING 'Error al gestionar permiso: %', SQLERRM;
    RETURN false;
END;
$$;


--
-- Name: fn_gestionar_permisos_elemento(character varying, character varying, character varying, character varying, character varying, boolean); Type: FUNCTION; Schema: seguridad; Owner: -
--

CREATE FUNCTION seguridad.fn_gestionar_permisos_elemento(p_rol_bd character varying, p_esquema character varying, p_elemento character varying, p_categoria character varying, p_privilegio character varying, p_otorgar boolean) RETURNS boolean
    LANGUAGE plpgsql SECURITY DEFINER
    AS $$
DECLARE
    v_sql text;
    v_tipo_objeto text;
BEGIN
    -- 1. Determinar el tipo de objeto SQL según la categoría de Angular
    IF p_categoria IN ('TABLAS', 'VISTAS', 'VISTAS MATERIALIZADAS') THEN
        v_tipo_objeto := 'TABLE';
    ELSIF p_categoria = 'SECUENCIAS' THEN
        v_tipo_objeto := 'SEQUENCE';
    ELSIF p_categoria IN ('FUNCIONES', 'PROCEDIMIENTOS') THEN
        v_tipo_objeto := 'ROUTINE';
    ELSIF p_categoria = 'TRIGGERS' THEN
        RAISE NOTICE 'Los triggers no reciben permisos directos.';
        RETURN true; 
    ELSE
        RAISE EXCEPTION 'Categoría no soportada: %', p_categoria;
    END IF;

    IF upper(p_privilegio) NOT IN ('SELECT', 'INSERT', 'UPDATE', 'DELETE', 'TRUNCATE', 'EXECUTE', 'USAGE', 'ALL') THEN
        RAISE EXCEPTION 'Privilegio no válido: %', p_privilegio;
    END IF;

    IF p_otorgar THEN
        v_sql := format('GRANT %s ON %s %I.%I TO %I', 
                        upper(p_privilegio), v_tipo_objeto, p_esquema, p_elemento, p_rol_bd);
    ELSE
        v_sql := format('REVOKE %s ON %s %I.%I FROM %I', 
                        upper(p_privilegio), v_tipo_objeto, p_esquema, p_elemento, p_rol_bd);
    END IF;

    EXECUTE v_sql;

    RETURN true;
EXCEPTION WHEN OTHERS THEN
    RAISE WARNING 'Error al gestionar permiso: % - Objeto: %.% - Privilegio: %', 
                   SQLERRM, p_esquema, p_elemento, p_privilegio;
    RETURN false;
END;
$$;


--
-- Name: fn_gestionar_permisos_elemento2(character varying, character varying, character varying, character varying, character varying, boolean); Type: FUNCTION; Schema: seguridad; Owner: -
--

CREATE FUNCTION seguridad.fn_gestionar_permisos_elemento2(p_rol_bd character varying, p_esquema character varying, p_elemento character varying, p_categoria character varying, p_privilegio character varying, p_otorgar boolean) RETURNS boolean
    LANGUAGE plpgsql SECURITY DEFINER
    AS $$

DECLARE
    v_sql text;
    v_tipo_objeto text;
    v_cat character varying := upper(p_categoria);
    v_priv character varying := upper(p_privilegio);
BEGIN
    IF v_cat = 'ESQUEMA' THEN
        v_tipo_objeto := 'SCHEMA';
        IF p_otorgar THEN
            v_sql := format('GRANT %s ON %s %I TO %I', v_priv, v_tipo_objeto, p_esquema, p_rol_bd);
        ELSE
            v_sql := format('REVOKE %s ON %s %I FROM %I', v_priv, v_tipo_objeto, p_esquema, p_rol_bd);
        END IF;

        EXECUTE v_sql;
        RETURN true;
    END IF;

    IF v_cat IN ('TABLA', 'TABLAS', 'VISTA', 'VISTAS', 'VISTA_MATERIALIZADA', 'VISTAS MATERIALIZADAS') THEN
        v_tipo_objeto := 'TABLE';
    ELSIF v_cat IN ('FUNCION', 'FUNCIONES', 'PROCEDIMIENTO', 'PROCEDIMIENTOS') THEN
        v_tipo_objeto := 'ROUTINE';
    ELSIF v_cat IN ('SECUENCIA', 'SECUENCIAS') THEN
        v_tipo_objeto := 'SEQUENCE';
    ELSE
        RAISE EXCEPTION 'Categoría no soportada: %', p_categoria;
    END IF;

    IF v_priv NOT IN ('SELECT', 'INSERT', 'UPDATE', 'DELETE', 'TRUNCATE', 'EXECUTE', 'USAGE', 'CREATE', 'REFERENCES', 'TRIGGER', 'ALL') THEN
        RAISE EXCEPTION 'Privilegio no válido: %', p_privilegio;
    END IF;

    IF p_otorgar THEN
        v_sql := format('GRANT %s ON %s %I.%I TO %I', v_priv, v_tipo_objeto, p_esquema, p_elemento, p_rol_bd);
    ELSE
        v_sql := format('REVOKE %s ON %s %I.%I FROM %I', v_priv, v_tipo_objeto, p_esquema, p_elemento, p_rol_bd);
    END IF;

    EXECUTE v_sql;
    RETURN true;
END;
$$;


--
-- Name: fn_identidad_usuario(integer); Type: FUNCTION; Schema: seguridad; Owner: -
--

CREATE FUNCTION seguridad.fn_identidad_usuario(p_id_usuario integer) RETURNS TABLE(id_usuario integer, id_rol_especifico integer, nombre_rol character varying)
    LANGUAGE sql STABLE PARALLEL SAFE
    AS $$
    SELECT
        u.id_usuario,

        CASE tr.nombre_tipo_rol
            WHEN 'ESTUDIANTE'       THEN est.id_estudiante
            WHEN 'DOCENTE'          THEN doc.id_docente
            WHEN 'COORDINADOR'      THEN coo.id_coordinador
            WHEN 'DECANO'           THEN dec.id_decano
            WHEN 'AYUDANTE_CATEDRA' THEN ayu.id_ayudante_catedra
        END::INTEGER AS id_rol_especifico,

        tr.nombre_tipo_rol::VARCHAR AS nombre_rol

    FROM seguridad.usuario u

        JOIN seguridad.usuario_tipo_rol utr
            ON utr.id_usuario = u.id_usuario
           AND utr.activo = TRUE

        JOIN seguridad.tipo_rol tr
            ON tr.id_tipo_rol = utr.id_tipo_rol
           AND tr.activo = TRUE

        LEFT JOIN academico.estudiante est
            ON tr.nombre_tipo_rol = 'ESTUDIANTE'
           AND est.id_usuario = u.id_usuario

        LEFT JOIN academico.docente doc
            ON tr.nombre_tipo_rol = 'DOCENTE'
           AND doc.id_usuario = u.id_usuario
           AND doc.activo = TRUE

        LEFT JOIN academico.coordinador coo
            ON tr.nombre_tipo_rol = 'COORDINADOR'
           AND coo.id_usuario = u.id_usuario
           AND coo.activo = TRUE

        LEFT JOIN academico.decano dec
            ON tr.nombre_tipo_rol = 'DECANO'
           AND dec.id_usuario = u.id_usuario
           AND dec.activo = TRUE

        LEFT JOIN academico.ayudante_catedra ayu
            ON tr.nombre_tipo_rol = 'AYUDANTE_CATEDRA'
           AND ayu.id_usuario = u.id_usuario

    WHERE u.id_usuario = p_id_usuario
      AND u.activo = TRUE;
$$;


--
-- Name: fn_identidad_usuario_safe(integer); Type: FUNCTION; Schema: seguridad; Owner: -
--

CREATE FUNCTION seguridad.fn_identidad_usuario_safe(p_id_usuario integer) RETURNS TABLE(id_usuario integer, id_rol_especifico integer, nombre_rol character varying)
    LANGUAGE plpgsql STABLE PARALLEL SAFE
    AS $$
BEGIN
    RETURN QUERY
        SELECT * FROM seguridad.fn_identidad_usuario(p_id_usuario);

    IF NOT FOUND THEN
        RAISE EXCEPTION 'El usuario con id % no existe o se encuentra inactivo.', p_id_usuario;
    END IF;
END;
$$;


--
-- Name: fn_listar_elementos_por_tipo_de_objeto(character varying, character varying); Type: FUNCTION; Schema: seguridad; Owner: -
--

CREATE FUNCTION seguridad.fn_listar_elementos_por_tipo_de_objeto(p_esquema character varying, p_tipo_objeto character varying) RETURNS TABLE(nombre_elemento character varying)
    LANGUAGE plpgsql
    AS $$
BEGIN

    IF UPPER(p_tipo_objeto) = 'TABLA' THEN
        RETURN QUERY
            SELECT table_name::VARCHAR
            FROM information_schema.tables
            WHERE table_schema = p_esquema
              AND table_type = 'BASE TABLE'
            ORDER BY table_name;

    ELSIF UPPER(p_tipo_objeto) = 'VISTA' THEN
        RETURN QUERY
            SELECT table_name::VARCHAR
            FROM information_schema.tables
            WHERE table_schema = p_esquema
              AND table_type = 'VIEW'
            ORDER BY table_name;

    ELSIF UPPER(p_tipo_objeto) = 'VISTA_MATERIALIZADA' THEN
        RETURN QUERY
            SELECT matviewname::VARCHAR
            FROM pg_matviews
            WHERE schemaname = p_esquema
            ORDER BY matviewname;

    ELSIF UPPER(p_tipo_objeto) = 'FUNCION' THEN
        RETURN QUERY
            SELECT routine_name::VARCHAR
            FROM information_schema.routines
            WHERE routine_schema = p_esquema
              AND routine_type = 'FUNCTION'
            ORDER BY routine_name;

    ELSIF UPPER(p_tipo_objeto) = 'PROCEDIMIENTO' THEN
        RETURN QUERY
            SELECT routine_name::VARCHAR
            FROM information_schema.routines
            WHERE routine_schema = p_esquema
              AND routine_type = 'PROCEDURE'
            ORDER BY routine_name;

    ELSIF UPPER(p_tipo_objeto) = 'ESQUEMA' THEN
        RETURN QUERY
            SELECT schema_name::VARCHAR
            FROM information_schema.schemata
            WHERE schema_name = p_esquema;

    ELSE
        RAISE EXCEPTION 'Tipo de objeto no válido: %', p_tipo_objeto;
    END IF;

END;
$$;


--
-- Name: fn_listar_esquemas(); Type: FUNCTION; Schema: seguridad; Owner: -
--

CREATE FUNCTION seguridad.fn_listar_esquemas() RETURNS TABLE(nombre_esquema character varying)
    LANGUAGE sql
    AS $$
SELECT
    n.nspname
FROM pg_namespace n
WHERE n.nspname NOT LIKE 'pg_%'
  AND n.nspname <> 'information_schema'
ORDER BY n.nspname;
$$;


--
-- Name: fn_listar_esquemas_bd(); Type: FUNCTION; Schema: seguridad; Owner: -
--

CREATE FUNCTION seguridad.fn_listar_esquemas_bd() RETURNS TABLE(nombre_esquema character varying)
    LANGUAGE sql
    AS $$
SELECT
    n.nspname
FROM pg_namespace n
WHERE n.nspname NOT LIKE 'pg_%'
  AND n.nspname <> 'information_schema'
ORDER BY n.nspname;
$$;


--
-- Name: fn_listar_privilegio(); Type: FUNCTION; Schema: seguridad; Owner: -
--

CREATE FUNCTION seguridad.fn_listar_privilegio() RETURNS jsonb
    LANGUAGE plpgsql SECURITY DEFINER
    AS $$
DECLARE
    v_resultado JSONB;
BEGIN
    SELECT COALESCE(jsonb_agg(
        jsonb_build_object(
            'id', id_privilegio,
            'nombre_privilegio', nombre_privilegio,
            'codigo_interno', codigo_interno,
            'descripcion', descripcion,
            'activo', activo
        ) ORDER BY nombre_privilegio
    ), '[]'::jsonb)
    INTO v_resultado
    FROM seguridad.privilegio
    WHERE activo = TRUE;

    RETURN jsonb_build_object(
        'exito', true,
        'datos', v_resultado
    );

EXCEPTION
    WHEN OTHERS THEN
        RETURN jsonb_build_object('exito', false, 'mensaje', 'Error al listar privilegios: ' || SQLERRM);
END;
$$;


--
-- Name: fn_listar_privilegios_por_tipo_objeto(integer); Type: FUNCTION; Schema: seguridad; Owner: -
--

CREATE FUNCTION seguridad.fn_listar_privilegios_por_tipo_objeto(p_id_tipo_objeto integer) RETURNS TABLE(id_privilegio integer, nombre_privilegio character varying, codigo_interno character)
    LANGUAGE sql
    AS $$
SELECT
    p.id_privilegio,
    p.nombre_privilegio,
    p.codigo_interno
FROM seguridad.tipo_objeto_seguridad_privilegio tp
         INNER JOIN seguridad.privilegio p
                    ON tp.id_privilegio = p.id_privilegio
WHERE tp.id_tipo_objeto_seguridad = p_id_tipo_objeto
ORDER BY p.nombre_privilegio;
$$;


--
-- Name: fn_listar_roles_activos(); Type: FUNCTION; Schema: seguridad; Owner: -
--

CREATE FUNCTION seguridad.fn_listar_roles_activos() RETURNS jsonb
    LANGUAGE sql STABLE SECURITY DEFINER
    AS $$
SELECT COALESCE(
               jsonb_agg(
                       jsonb_build_object(
                               'idTipoRol',     id_tipo_rol,
                               'nombreTipoRol', nombre_tipo_rol
                       )
                       ORDER BY nombre_tipo_rol
               ),
               '[]'::JSONB
       )
FROM seguridad.tipo_rol
WHERE activo = TRUE;
$$;


--
-- Name: fn_listar_tipos_objeto_seguridad(); Type: FUNCTION; Schema: seguridad; Owner: -
--

CREATE FUNCTION seguridad.fn_listar_tipos_objeto_seguridad() RETURNS TABLE(id_tipo_objeto_seguridad integer, nombre_tipo_objeto character varying)
    LANGUAGE sql
    AS $$
SELECT
    t.id_tipo_objeto_seguridad,
    t.nombre_tipo_objeto
FROM seguridad.tipo_objeto_seguridad t
ORDER BY t.id_tipo_objeto_seguridad;
$$;


--
-- Name: fn_notif_cambio_estado_postulacion(); Type: FUNCTION; Schema: seguridad; Owner: -
--

CREATE FUNCTION seguridad.fn_notif_cambio_estado_postulacion() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
DECLARE
    v_id_usuario_destino INTEGER;
    v_titulo VARCHAR(150);
    v_mensaje TEXT;
BEGIN
    IF (NEW.id_tipo_estado_postulacion IS DISTINCT FROM OLD.id_tipo_estado_postulacion)
        OR (NEW.observaciones IS DISTINCT FROM OLD.observaciones) THEN

        -- Obtener el id_usuario del estudiante
        SELECT e.id_usuario INTO v_id_usuario_destino
        FROM academico.estudiante e
        WHERE e.id_estudiante = NEW.id_estudiante;

        IF v_id_usuario_destino IS NOT NULL THEN
            -- Determinar título y mensaje según el estado
            v_titulo := 'Actualización de Postulación';
            v_mensaje := 'Tu postulación fue actualizada a estado: ' || COALESCE(NEW.id_tipo_estado_postulacion, 'N/A') || '. Revisa el estado y observaciones en la plataforma.';

            -- Usar notificacion_ws con columnas correctas
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
$$;


--
-- Name: fn_registrar_usuario_global(jsonb); Type: FUNCTION; Schema: seguridad; Owner: -
--

CREATE FUNCTION seguridad.fn_registrar_usuario_global(p_data jsonb) RETURNS jsonb
    LANGUAGE plpgsql SECURITY DEFINER
    AS $$
DECLARE
    v_id_usuario     INTEGER;
    v_user_lower     VARCHAR(50);
    v_password_hash  VARCHAR(255);
    v_password_plain VARCHAR(100);
    v_rol_record     RECORD;
    v_db_role_name   TEXT;
    v_roles_ids      JSONB;
    v_rol_id         INTEGER;
    v_rol_nombre     VARCHAR(50);
    v_id_carrera     INTEGER;
    v_id_facultad    INTEGER;
    v_matricula      VARCHAR(30);
    v_semestre       INTEGER;
    v_horas_ayudante NUMERIC(5,2);
    v_result         JSONB;
BEGIN
    -- ── Extracción y normalización ───────────────────────────────────
    v_user_lower     := LOWER(TRIM(p_data->>'username'));
    v_password_hash  := p_data->>'passwordHash';
    v_password_plain := p_data->>'passwordPlain';
    v_roles_ids      := p_data->'rolesIds';

    IF v_user_lower IS NULL OR v_user_lower = '' THEN
        RAISE EXCEPTION 'El campo username es obligatorio.';
    END IF;
    IF v_password_hash IS NULL OR v_password_hash = '' THEN
        RAISE EXCEPTION 'El hash de la contraseña es obligatorio.';
    END IF;
    IF v_password_plain IS NULL OR v_password_plain = '' THEN
        RAISE EXCEPTION 'La contraseña en texto plano es obligatoria para crear el usuario de BD.';
    END IF;
    IF v_roles_ids IS NULL OR jsonb_array_length(v_roles_ids) = 0 THEN
        RAISE EXCEPTION 'Debe seleccionar al menos un rol para el usuario.';
    END IF;

    -- ── 1. Insertar en seguridad.usuario ────────────────────────────
    INSERT INTO seguridad.usuario (
        nombres, apellidos, cedula, correo,
        nombre_usuario, contrasenia_usuario, fecha_creacion, activo
    ) VALUES (
                 TRIM(p_data->>'nombres'),
                 TRIM(p_data->>'apellidos'),
                 TRIM(p_data->>'cedula'),
                 LOWER(TRIM(p_data->>'correo')),
                 v_user_lower,
                 v_password_hash,
                 CURRENT_DATE,
                 TRUE
             )
    RETURNING id_usuario INTO v_id_usuario;

    -- ── 2. Crear usuario de base de datos ───────────────────────────
    EXECUTE format('CREATE USER %I WITH PASSWORD %L', v_user_lower, v_password_plain);
    EXECUTE format('GRANT %I TO app_user_default', v_user_lower);

    -- ── 3. Iterar sobre roles ────────────────────────────────────────
    FOR v_rol_id IN
        SELECT jsonb_array_elements_text(v_roles_ids)::INTEGER
        LOOP
            -- Validar que el rol existe y está activo
            SELECT nombre_tipo_rol
            INTO v_rol_nombre
            FROM seguridad.tipo_rol
            WHERE id_tipo_rol = v_rol_id
              AND activo = TRUE;

            IF NOT FOUND THEN
                RAISE EXCEPTION 'El rol con ID % no existe o no está activo.', v_rol_id;
            END IF;

            -- 3a. Relación usuario ↔ tipo_rol
            INSERT INTO seguridad.usuario_tipo_rol (id_usuario, id_tipo_rol, activo, fecha_creacion)
            VALUES (v_id_usuario, v_rol_id, TRUE, CURRENT_TIMESTAMP)
            ON CONFLICT (id_usuario, id_tipo_rol) DO NOTHING;

            -- 3b. Conceder rol de BD (normalizar nombre)
            v_db_role_name := CASE v_rol_nombre
                                  WHEN 'AYUDANTE_CATEDRA' THEN 'role_ayudante_catedra'
                                  ELSE 'role_' || LOWER(v_rol_nombre)
                END;
            EXECUTE format('GRANT %I TO %I', v_db_role_name, v_user_lower);

            -- 3c. Inserciones condicionales en tablas especializadas
            CASE v_rol_nombre

                WHEN 'ESTUDIANTE' THEN
                    v_id_carrera := (p_data->>'idCarrera')::INTEGER;
                    v_matricula  := TRIM(p_data->>'matricula');
                    v_semestre   := (p_data->>'semestre')::INTEGER;

                    IF v_id_carrera IS NULL THEN
                        RAISE EXCEPTION 'Se requiere idCarrera para el rol ESTUDIANTE.';
                    END IF;
                    IF v_matricula IS NULL OR v_matricula = '' THEN
                        RAISE EXCEPTION 'Se requiere matrícula para el rol ESTUDIANTE.';
                    END IF;
                    IF v_semestre IS NULL OR v_semestre < 1 OR v_semestre > 10 THEN
                        RAISE EXCEPTION 'El semestre debe estar entre 1 y 10.';
                    END IF;

                    INSERT INTO academico.estudiante
                    (id_usuario, id_carrera, matricula, semestre, estado_academico)
                    VALUES
                        (v_id_usuario, v_id_carrera, v_matricula, v_semestre, 'ACTIVO');

                WHEN 'DOCENTE' THEN
                    INSERT INTO academico.docente (id_usuario, fecha_inicio, activo)
                    VALUES (v_id_usuario, CURRENT_DATE, TRUE);

                WHEN 'COORDINADOR' THEN
                    v_id_carrera := (p_data->>'idCarrera')::INTEGER;
                    IF v_id_carrera IS NULL THEN
                        RAISE EXCEPTION 'Se requiere idCarrera para el rol COORDINADOR.';
                    END IF;
                    INSERT INTO academico.coordinador (id_usuario, id_carrera, fecha_inicio, activo)
                    VALUES (v_id_usuario, v_id_carrera, CURRENT_DATE, TRUE);

                WHEN 'DECANO' THEN
                    v_id_facultad := (p_data->>'idFacultad')::INTEGER;
                    IF v_id_facultad IS NULL THEN
                        RAISE EXCEPTION 'Se requiere idFacultad para el rol DECANO.';
                    END IF;
                    INSERT INTO academico.decano (id_usuario, id_facultad, fecha_inicio_gestion, activo)
                    VALUES (v_id_usuario, v_id_facultad, CURRENT_DATE, TRUE);

                WHEN 'AYUDANTE_CATEDRA' THEN
                    v_horas_ayudante := (p_data->>'horasAyudante')::NUMERIC(5,2);
                    IF v_horas_ayudante IS NULL OR v_horas_ayudante <= 0 THEN
                        RAISE EXCEPTION 'Se requiere horasAyudante > 0 para el rol AYUDANTE_CATEDRA.';
                    END IF;
                    INSERT INTO academico.ayudante_catedra (id_usuario, horas_ayudante)
                    VALUES (v_id_usuario, v_horas_ayudante);

                WHEN 'ADMINISTRADOR' THEN
                    NULL; -- El administrador no tiene tabla especializada

                ELSE
                    RAISE NOTICE 'Rol % no tiene tabla especializada asociada.', v_rol_nombre;
                END CASE;

        END LOOP;

    -- ── 4. Construir respuesta ───────────────────────────────────────
    v_result := jsonb_build_object(
            'exito',        TRUE,
            'mensaje',      format('Usuario "%s" registrado con %s rol(es).', v_user_lower, jsonb_array_length(v_roles_ids)),
            'correo',       p_data->>'correo',
            'nombreUsuario', v_user_lower,
            'idUsuario',    v_id_usuario
                );

    RAISE NOTICE 'fn_registrar_usuario_global: %', v_result;
    RETURN v_result;

EXCEPTION
    WHEN OTHERS THEN
        RAISE EXCEPTION 'Error al registrar usuario global: % (SQLSTATE: %)', SQLERRM, SQLSTATE;
END;
$$;


--
-- Name: fn_validar_contexto_estudiante(integer); Type: FUNCTION; Schema: seguridad; Owner: -
--

CREATE FUNCTION seguridad.fn_validar_contexto_estudiante(p_id_usuario integer, OUT p_id_estudiante integer, OUT p_es_valido boolean, OUT p_mensaje text) RETURNS record
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


--
-- Name: sp_crear_postulacion(integer, integer, date, character varying, character varying); Type: FUNCTION; Schema: seguridad; Owner: -
--

CREATE FUNCTION seguridad.sp_crear_postulacion(p_id_convocatoria integer, p_id_estudiante integer, p_fecha_postulacion date, p_estado_postulacion character varying, p_observaciones character varying) RETURNS integer
    LANGUAGE plpgsql
    AS $$
DECLARE
    v_id integer;
BEGIN
    INSERT INTO postulacion.postulacion (
        id_convocatoria, id_estudiante,
        fecha_postulacion, id_tipo_estado_postulacion, activo
    ) VALUES (
                 p_id_convocatoria, p_id_estudiante,
                 p_fecha_postulacion, (select postulacion.id_tipo_estado_postulacion from postulacion.tipo_estado_postulacion where codigo = 'PENDIENTE' limit 1), TRUE
             ) RETURNING id_postulacion INTO v_id;

    RETURN v_id;
EXCEPTION WHEN OTHERS THEN
    RAISE NOTICE 'Error en sp_crear_postulacion: %', SQLERRM;
    RETURN -1;
END;
$$;


--
-- Name: sp_promover_estudiante_a_ayudante(character varying, numeric); Type: PROCEDURE; Schema: seguridad; Owner: -
--

CREATE PROCEDURE seguridad.sp_promover_estudiante_a_ayudante(IN p_username_estudiante character varying, IN p_horas_asignadas numeric)
    LANGUAGE plpgsql
    AS $$
DECLARE
    v_id_usuario INTEGER;
    v_id_rol_ayudante INTEGER;
    v_user_lower VARCHAR;
BEGIN
    v_user_lower := LOWER(p_username_estudiante);

    SELECT id_usuario INTO v_id_usuario FROM seguridad.usuario
    WHERE nombre_usuario = v_user_lower;

    IF v_id_usuario IS NULL THEN
        RAISE EXCEPTION 'Usuario % no existe en la tabla public.usuario.', v_user_lower;
    END IF;

    SELECT id_tipo_rol INTO v_id_rol_ayudante FROM seguridad.tipo_rol
    WHERE nombre_tipo_rol = 'AYUDANTE_CATEDRA';

    IF v_id_rol_ayudante IS NULL THEN
        RAISE EXCEPTION 'El rol AYUDANTE_CATEDRA no está definido en el sistema.';
    END IF;

    INSERT INTO seguridad.usuario_tipo_rol (id_usuario, id_tipo_rol, activo, fecha_creacion)
    VALUES (v_id_usuario, v_id_rol_ayudante, TRUE, CURRENT_DATE)
    ON CONFLICT (id_usuario, id_tipo_rol)
        DO UPDATE SET activo = TRUE, fecha_creacion = CURRENT_DATE;

    INSERT INTO academico.ayudante_catedra (id_usuario, horas_ayudante)
    VALUES (v_id_usuario, p_horas_asignadas)
    ON CONFLICT (id_usuario)
        DO UPDATE SET horas_ayudante = p_horas_asignadas;

    EXECUTE format('GRANT role_ayudante_catedra TO %I', v_user_lower);

    RAISE NOTICE 'Estudiante % procesado correctamente: Rol Activado/Creado.', v_user_lower;

EXCEPTION
    WHEN OTHERS THEN
        RAISE EXCEPTION 'Error en el proceso de promoción: %', SQLERRM;
END;
$$;


--
-- Name: asignatura; Type: TABLE; Schema: academico; Owner: -
--

CREATE TABLE academico.asignatura (
    id_asignatura integer NOT NULL,
    id_carrera integer NOT NULL,
    nombre_asignatura character varying(150) NOT NULL,
    semestre integer NOT NULL,
    activo boolean
);


--
-- Name: asignatura_id_asignatura_seq; Type: SEQUENCE; Schema: academico; Owner: -
--

ALTER TABLE academico.asignatura ALTER COLUMN id_asignatura ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME academico.asignatura_id_asignatura_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: ayudante_catedra; Type: TABLE; Schema: academico; Owner: -
--

CREATE TABLE academico.ayudante_catedra (
    id_ayudante_catedra integer NOT NULL,
    horas_ayudante numeric(5,2),
    id_usuario integer
);


--
-- Name: ayudante_catedra_id_ayudante_catedra_seq; Type: SEQUENCE; Schema: academico; Owner: -
--

ALTER TABLE academico.ayudante_catedra ALTER COLUMN id_ayudante_catedra ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME academico.ayudante_catedra_id_ayudante_catedra_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: carrera; Type: TABLE; Schema: academico; Owner: -
--

CREATE TABLE academico.carrera (
    id_carrera integer NOT NULL,
    id_facultad integer NOT NULL,
    nombre_carrera character varying(150) NOT NULL,
    activo boolean
);


--
-- Name: carrera_id_carrera_seq; Type: SEQUENCE; Schema: academico; Owner: -
--

ALTER TABLE academico.carrera ALTER COLUMN id_carrera ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME academico.carrera_id_carrera_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: coordinador; Type: TABLE; Schema: academico; Owner: -
--

CREATE TABLE academico.coordinador (
    id_coordinador integer NOT NULL,
    id_usuario integer NOT NULL,
    id_carrera integer NOT NULL,
    fecha_inicio date,
    fecha_fin date,
    activo boolean
);


--
-- Name: coordinador_id_coordinador_seq; Type: SEQUENCE; Schema: academico; Owner: -
--

ALTER TABLE academico.coordinador ALTER COLUMN id_coordinador ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME academico.coordinador_id_coordinador_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: decano_id_decano_seq; Type: SEQUENCE; Schema: academico; Owner: -
--

ALTER TABLE academico.decano ALTER COLUMN id_decano ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME academico.decano_id_decano_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: docente_asignatura; Type: TABLE; Schema: academico; Owner: -
--

CREATE TABLE academico.docente_asignatura (
    id_docente_asignatura integer NOT NULL,
    id_docente integer NOT NULL,
    id_asignatura integer NOT NULL,
    activo boolean
);


--
-- Name: docente_asignatura_id_docente_asignatura_seq; Type: SEQUENCE; Schema: academico; Owner: -
--

ALTER TABLE academico.docente_asignatura ALTER COLUMN id_docente_asignatura ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME academico.docente_asignatura_id_docente_asignatura_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: docente_id_docente_seq; Type: SEQUENCE; Schema: academico; Owner: -
--

ALTER TABLE academico.docente ALTER COLUMN id_docente ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME academico.docente_id_docente_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: estudiante_id_estudiante_seq; Type: SEQUENCE; Schema: academico; Owner: -
--

ALTER TABLE academico.estudiante ALTER COLUMN id_estudiante ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME academico.estudiante_id_estudiante_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: facultad; Type: TABLE; Schema: academico; Owner: -
--

CREATE TABLE academico.facultad (
    id_facultad integer NOT NULL,
    nombre_facultad character varying(150) NOT NULL,
    activo boolean
);


--
-- Name: facultad_id_facultad_seq; Type: SEQUENCE; Schema: academico; Owner: -
--

ALTER TABLE academico.facultad ALTER COLUMN id_facultad ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME academico.facultad_id_facultad_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: log_auditoria; Type: TABLE; Schema: academico; Owner: -
--

CREATE TABLE academico.log_auditoria (
    id_log_auditoria bigint NOT NULL,
    id_usuario integer,
    id_tipo_rol integer,
    accion character varying(20) NOT NULL,
    tabla_afectada character varying(100) NOT NULL,
    registro_afectado integer,
    fecha_hora timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    valor_anterior text,
    valor_nuevo text
);


--
-- Name: log_auditoria_id_log_auditoria_seq; Type: SEQUENCE; Schema: academico; Owner: -
--

CREATE SEQUENCE academico.log_auditoria_id_log_auditoria_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: log_auditoria_id_log_auditoria_seq; Type: SEQUENCE OWNED BY; Schema: academico; Owner: -
--

ALTER SEQUENCE academico.log_auditoria_id_log_auditoria_seq OWNED BY academico.log_auditoria.id_log_auditoria;


--
-- Name: periodo_academico; Type: TABLE; Schema: academico; Owner: -
--

CREATE TABLE academico.periodo_academico (
    id_periodo_academico integer NOT NULL,
    nombre_periodo character varying(100) NOT NULL,
    fecha_inicio date NOT NULL,
    fecha_fin date NOT NULL,
    estado character varying(30) NOT NULL,
    activo boolean
);


--
-- Name: periodo_academico_id_periodo_academico_seq; Type: SEQUENCE; Schema: academico; Owner: -
--

ALTER TABLE academico.periodo_academico ALTER COLUMN id_periodo_academico ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME academico.periodo_academico_id_periodo_academico_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: ayudante_catedra; Type: TABLE; Schema: ayudantia; Owner: -
--

CREATE TABLE ayudantia.ayudante_catedra (
    id_ayudante_catedra integer NOT NULL,
    id_usuario integer,
    horas_ayudante numeric(5,2)
);


--
-- Name: ayudante_catedra_id_ayudante_catedra_seq; Type: SEQUENCE; Schema: ayudantia; Owner: -
--

ALTER TABLE ayudantia.ayudante_catedra ALTER COLUMN id_ayudante_catedra ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME ayudantia.ayudante_catedra_id_ayudante_catedra_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: ayudantia_id_ayudantia_seq; Type: SEQUENCE; Schema: ayudantia; Owner: -
--

ALTER TABLE ayudantia.ayudantia ALTER COLUMN id_ayudantia ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME ayudantia.ayudantia_id_ayudantia_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: certificado; Type: TABLE; Schema: ayudantia; Owner: -
--

CREATE TABLE ayudantia.certificado (
    id_certificado integer NOT NULL,
    id_ayudantia integer NOT NULL,
    id_usuario integer,
    codigo_verificacion character varying(50),
    fecha_emision date,
    total_horas_certificadas integer,
    archivo bytea,
    estado character varying(30),
    activo boolean
);


--
-- Name: certificado_id_certificado_seq; Type: SEQUENCE; Schema: ayudantia; Owner: -
--

ALTER TABLE ayudantia.certificado ALTER COLUMN id_certificado ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME ayudantia.certificado_id_certificado_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: detalle_asistencia_actividad; Type: TABLE; Schema: ayudantia; Owner: -
--

CREATE TABLE ayudantia.detalle_asistencia_actividad (
    id_detalle_asistencia_actividad integer CONSTRAINT detalle_asistencia_activida_id_detalle_asistencia_acti_not_null NOT NULL,
    id_registro_actividad integer NOT NULL,
    id_participante_ayudantia integer NOT NULL,
    asistio boolean
);


--
-- Name: detalle_asistencia_actividad_id_detalle_asistencia_activida_seq; Type: SEQUENCE; Schema: ayudantia; Owner: -
--

ALTER TABLE ayudantia.detalle_asistencia_actividad ALTER COLUMN id_detalle_asistencia_actividad ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME ayudantia.detalle_asistencia_actividad_id_detalle_asistencia_activida_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: documento_academico; Type: TABLE; Schema: ayudantia; Owner: -
--

CREATE TABLE ayudantia.documento_academico (
    id_documento integer NOT NULL,
    nombre_mostrar character varying(150) NOT NULL,
    ruta_archivo character varying(500) NOT NULL,
    extension character varying(10),
    peso_bytes integer,
    fecha_subida timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    id_tipo_documento integer NOT NULL,
    id_periodo integer NOT NULL,
    id_facultad integer,
    id_carrera integer,
    id_usuario_sube integer NOT NULL,
    activo boolean DEFAULT true
);


--
-- Name: documento_academico_id_documento_seq; Type: SEQUENCE; Schema: ayudantia; Owner: -
--

ALTER TABLE ayudantia.documento_academico ALTER COLUMN id_documento ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME ayudantia.documento_academico_id_documento_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: evidencia_registro_actividad_id_evidencia_registro_activida_seq; Type: SEQUENCE; Schema: ayudantia; Owner: -
--

ALTER TABLE ayudantia.evidencia_registro_actividad ALTER COLUMN id_evidencia_registro_actividad ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME ayudantia.evidencia_registro_actividad_id_evidencia_registro_activida_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: participante_ayudantia; Type: TABLE; Schema: ayudantia; Owner: -
--

CREATE TABLE ayudantia.participante_ayudantia (
    id_participante_ayudantia integer NOT NULL,
    id_ayudantia integer NOT NULL,
    nombre_completo character varying(255) NOT NULL,
    curso character varying(100),
    paralelo character varying(20),
    activo boolean DEFAULT true
);


--
-- Name: participante_ayudantia_id_participante_ayudantia_seq; Type: SEQUENCE; Schema: ayudantia; Owner: -
--

ALTER TABLE ayudantia.participante_ayudantia ALTER COLUMN id_participante_ayudantia ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME ayudantia.participante_ayudantia_id_participante_ayudantia_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: registro_actividad_id_registro_actividad_seq; Type: SEQUENCE; Schema: ayudantia; Owner: -
--

ALTER TABLE ayudantia.registro_actividad ALTER COLUMN id_registro_actividad ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME ayudantia.registro_actividad_id_registro_actividad_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: sancion_ayudante_catedra_id_sancion_ayudante_catedra_seq; Type: SEQUENCE; Schema: ayudantia; Owner: -
--

ALTER TABLE ayudantia.sancion_ayudante_catedra ALTER COLUMN id_sancion_ayudante_catedra ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME ayudantia.sancion_ayudante_catedra_id_sancion_ayudante_catedra_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: tipo_documento; Type: TABLE; Schema: ayudantia; Owner: -
--

CREATE TABLE ayudantia.tipo_documento (
    id_tipo_documento integer NOT NULL,
    nombre character varying(50) NOT NULL,
    codigo character varying(25) NOT NULL,
    activo boolean DEFAULT true
);


--
-- Name: tipo_documento_id_tipo_documento_seq; Type: SEQUENCE; Schema: ayudantia; Owner: -
--

ALTER TABLE ayudantia.tipo_documento ALTER COLUMN id_tipo_documento ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME ayudantia.tipo_documento_id_tipo_documento_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: tipo_estado_ayudantia; Type: TABLE; Schema: ayudantia; Owner: -
--

CREATE TABLE ayudantia.tipo_estado_ayudantia (
    id_tipo_estado_ayudantia integer NOT NULL,
    nombre_estado character varying(50) NOT NULL,
    descripcion text,
    activo boolean,
    codigo character varying(25) NOT NULL
);


--
-- Name: tipo_estado_ayudantia_id_tipo_estado_ayudantia_seq; Type: SEQUENCE; Schema: ayudantia; Owner: -
--

ALTER TABLE ayudantia.tipo_estado_ayudantia ALTER COLUMN id_tipo_estado_ayudantia ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME ayudantia.tipo_estado_ayudantia_id_tipo_estado_ayudantia_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: tipo_estado_evidencia; Type: TABLE; Schema: ayudantia; Owner: -
--

CREATE TABLE ayudantia.tipo_estado_evidencia (
    id_tipo_estado_evidencia integer NOT NULL,
    nombre_estado character varying(50) NOT NULL,
    descripcion text,
    activo boolean,
    codigo character varying(25) NOT NULL
);


--
-- Name: tipo_estado_evidencia_id_tipo_estado_evidencia_seq; Type: SEQUENCE; Schema: ayudantia; Owner: -
--

ALTER TABLE ayudantia.tipo_estado_evidencia ALTER COLUMN id_tipo_estado_evidencia ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME ayudantia.tipo_estado_evidencia_id_tipo_estado_evidencia_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: tipo_estado_registro; Type: TABLE; Schema: ayudantia; Owner: -
--

CREATE TABLE ayudantia.tipo_estado_registro (
    id_tipo_estado_registro integer NOT NULL,
    nombre_estado character varying(50) NOT NULL,
    descripcion text,
    activo boolean,
    codigo character varying(25) NOT NULL
);


--
-- Name: COLUMN tipo_estado_registro.codigo; Type: COMMENT; Schema: ayudantia; Owner: -
--

COMMENT ON COLUMN ayudantia.tipo_estado_registro.codigo IS 'Código único identificador del estado de registro';


--
-- Name: tipo_estado_registro_id_tipo_estado_registro_seq; Type: SEQUENCE; Schema: ayudantia; Owner: -
--

ALTER TABLE ayudantia.tipo_estado_registro ALTER COLUMN id_tipo_estado_registro ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME ayudantia.tipo_estado_registro_id_tipo_estado_registro_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: tipo_evidencia; Type: TABLE; Schema: ayudantia; Owner: -
--

CREATE TABLE ayudantia.tipo_evidencia (
    id_tipo_evidencia integer NOT NULL,
    nombre character varying(50) NOT NULL,
    extension_permitida character varying(10),
    activo boolean,
    codigo character varying(25) NOT NULL
);


--
-- Name: COLUMN tipo_evidencia.codigo; Type: COMMENT; Schema: ayudantia; Owner: -
--

COMMENT ON COLUMN ayudantia.tipo_evidencia.codigo IS 'Código único identificador del tipo de evidencia';


--
-- Name: tipo_evidencia_id_tipo_evidencia_seq; Type: SEQUENCE; Schema: ayudantia; Owner: -
--

ALTER TABLE ayudantia.tipo_evidencia ALTER COLUMN id_tipo_evidencia ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME ayudantia.tipo_evidencia_id_tipo_evidencia_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: tipo_sancion_ayudante_catedra; Type: TABLE; Schema: ayudantia; Owner: -
--

CREATE TABLE ayudantia.tipo_sancion_ayudante_catedra (
    id_tipo_sancion_ayudante_catedra integer CONSTRAINT tipo_sancion_ayudante_cated_id_tipo_sancion_ayudante_c_not_null NOT NULL,
    nombre_tipo_sancion character varying(100),
    activo boolean,
    codigo character varying(25) NOT NULL
);


--
-- Name: tipo_sancion_ayudante_catedra_id_tipo_sancion_ayudante_cate_seq; Type: SEQUENCE; Schema: ayudantia; Owner: -
--

ALTER TABLE ayudantia.tipo_sancion_ayudante_catedra ALTER COLUMN id_tipo_sancion_ayudante_catedra ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME ayudantia.tipo_sancion_ayudante_catedra_id_tipo_sancion_ayudante_cate_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: convocatoria; Type: TABLE; Schema: convocatoria; Owner: -
--

CREATE TABLE convocatoria.convocatoria (
    id_convocatoria integer NOT NULL,
    id_periodo_academico integer NOT NULL,
    id_asignatura integer NOT NULL,
    id_docente integer NOT NULL,
    cupos_disponibles integer NOT NULL,
    estado character varying(30),
    activo boolean
);


--
-- Name: convocatoria_id_convocatoria_seq; Type: SEQUENCE; Schema: convocatoria; Owner: -
--

ALTER TABLE convocatoria.convocatoria ALTER COLUMN id_convocatoria ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME convocatoria.convocatoria_id_convocatoria_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: periodo_academico_requisito_postulacion; Type: TABLE; Schema: convocatoria; Owner: -
--

CREATE TABLE convocatoria.periodo_academico_requisito_postulacion (
    id_periodo_academico_requisito_postulacion integer CONSTRAINT periodo_academico_requisito_id_periodo_academico_requi_not_null NOT NULL,
    id_periodo_academico integer CONSTRAINT periodo_academico_requisito_postu_id_periodo_academico_not_null NOT NULL,
    id_tipo_requisito_postulacion integer CONSTRAINT periodo_academico_requisito_id_tipo_requisito_postulac_not_null NOT NULL,
    obligatorio boolean,
    orden integer,
    activo boolean
);


--
-- Name: periodo_academico_requisito_p_id_periodo_academico_requisit_seq; Type: SEQUENCE; Schema: convocatoria; Owner: -
--

ALTER TABLE convocatoria.periodo_academico_requisito_postulacion ALTER COLUMN id_periodo_academico_requisito_postulacion ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME convocatoria.periodo_academico_requisito_p_id_periodo_academico_requisit_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: tipo_estado_requisito; Type: TABLE; Schema: convocatoria; Owner: -
--

CREATE TABLE convocatoria.tipo_estado_requisito (
    id_tipo_estado_requisito integer NOT NULL,
    nombre_estado character varying(50),
    descripcion text,
    activo boolean,
    codigo character varying(25) NOT NULL
);


--
-- Name: tipo_estado_requisito_id_tipo_estado_requisito_seq; Type: SEQUENCE; Schema: convocatoria; Owner: -
--

ALTER TABLE convocatoria.tipo_estado_requisito ALTER COLUMN id_tipo_estado_requisito ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME convocatoria.tipo_estado_requisito_id_tipo_estado_requisito_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: tipo_requisito_postulacion; Type: TABLE; Schema: convocatoria; Owner: -
--

CREATE TABLE convocatoria.tipo_requisito_postulacion (
    id_tipo_requisito_postulacion integer CONSTRAINT tipo_requisito_postulacion_id_tipo_requisito_postulaci_not_null NOT NULL,
    nombre_requisito character varying(100) NOT NULL,
    descripcion text,
    activo boolean,
    tipo_documento_permitido character varying(100) DEFAULT 'PDF'::character varying
);


--
-- Name: tipo_requisito_postulacion_id_tipo_requisito_postulacion_seq; Type: SEQUENCE; Schema: convocatoria; Owner: -
--

ALTER TABLE convocatoria.tipo_requisito_postulacion ALTER COLUMN id_tipo_requisito_postulacion ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME convocatoria.tipo_requisito_postulacion_id_tipo_requisito_postulacion_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: log_auditoria; Type: TABLE; Schema: notificacion; Owner: -
--

CREATE TABLE notificacion.log_auditoria (
    id_log_auditoria integer NOT NULL,
    id_usuario integer NOT NULL,
    accion character varying(100),
    tabla_afectada character varying(100),
    registro_afectado integer,
    fecha_hora timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    ip_origen character varying(50),
    valor_anterior text,
    valor_nuevo text,
    id_tipo_rol integer
);


--
-- Name: log_auditoria_id_log_auditoria_seq; Type: SEQUENCE; Schema: notificacion; Owner: -
--

ALTER TABLE notificacion.log_auditoria ALTER COLUMN id_log_auditoria ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME notificacion.log_auditoria_id_log_auditoria_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: notificacion; Type: TABLE; Schema: notificacion; Owner: -
--

CREATE TABLE notificacion.notificacion (
    id_notificacion integer NOT NULL,
    fecha_envio timestamp(6) without time zone NOT NULL,
    leido boolean NOT NULL,
    mensaje character varying(255) NOT NULL,
    tipo character varying(30),
    id_usuario_destino integer NOT NULL,
    tipo_notificacion character varying(30) DEFAULT 'INDIVIDUAL'::character varying,
    id_convocatoria integer,
    fecha_creacion timestamp(6) with time zone DEFAULT CURRENT_TIMESTAMP,
    fecha_lectura timestamp(6) with time zone,
    id_referencia integer,
    titulo character varying(150) NOT NULL,
    id_usuario integer NOT NULL
);


--
-- Name: notificacion_id_notificacion_seq; Type: SEQUENCE; Schema: notificacion; Owner: -
--

ALTER TABLE notificacion.notificacion ALTER COLUMN id_notificacion ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME notificacion.notificacion_id_notificacion_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: notificacion_ws; Type: TABLE; Schema: notificacion; Owner: -
--

CREATE TABLE notificacion.notificacion_ws (
    id_notificacion integer NOT NULL,
    id_usuario integer NOT NULL,
    titulo character varying(150) NOT NULL,
    mensaje text NOT NULL,
    tipo character varying(30) NOT NULL,
    id_referencia integer,
    leido boolean DEFAULT false,
    fecha_creacion timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    fecha_lectura timestamp without time zone
);


--
-- Name: notificacion_ws_id_notificacion_seq; Type: SEQUENCE; Schema: notificacion; Owner: -
--

CREATE SEQUENCE notificacion.notificacion_ws_id_notificacion_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: notificacion_ws_id_notificacion_seq; Type: SEQUENCE OWNED BY; Schema: notificacion; Owner: -
--

ALTER SEQUENCE notificacion.notificacion_ws_id_notificacion_seq OWNED BY notificacion.notificacion_ws.id_notificacion;


--
-- Name: periodo_fase; Type: TABLE; Schema: planificacion; Owner: -
--

CREATE TABLE planificacion.periodo_fase (
    id_periodo_fase integer NOT NULL,
    id_periodo_academico integer NOT NULL,
    id_tipo_fase integer NOT NULL,
    fecha_inicio date NOT NULL,
    fecha_fin date NOT NULL,
    CONSTRAINT chk_fecha_coherencia CHECK ((fecha_fin >= fecha_inicio))
);


--
-- Name: periodo_fase_id_periodo_fase_seq; Type: SEQUENCE; Schema: planificacion; Owner: -
--

CREATE SEQUENCE planificacion.periodo_fase_id_periodo_fase_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: periodo_fase_id_periodo_fase_seq; Type: SEQUENCE OWNED BY; Schema: planificacion; Owner: -
--

ALTER SEQUENCE planificacion.periodo_fase_id_periodo_fase_seq OWNED BY planificacion.periodo_fase.id_periodo_fase;


--
-- Name: tipo_fase; Type: TABLE; Schema: planificacion; Owner: -
--

CREATE TABLE planificacion.tipo_fase (
    id_tipo_fase integer NOT NULL,
    nombre character varying(120) NOT NULL,
    descripcion text,
    orden integer NOT NULL,
    activo boolean DEFAULT true NOT NULL,
    codigo character varying(25) NOT NULL,
    CONSTRAINT tipo_fase_orden_check CHECK ((orden > 0))
);


--
-- Name: tipo_fase_id_tipo_fase_seq; Type: SEQUENCE; Schema: planificacion; Owner: -
--

CREATE SEQUENCE planificacion.tipo_fase_id_tipo_fase_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: tipo_fase_id_tipo_fase_seq; Type: SEQUENCE OWNED BY; Schema: planificacion; Owner: -
--

ALTER SEQUENCE planificacion.tipo_fase_id_tipo_fase_seq OWNED BY planificacion.tipo_fase.id_tipo_fase;


--
-- Name: banco_temas; Type: TABLE; Schema: postulacion; Owner: -
--

CREATE TABLE postulacion.banco_temas (
    id_tema integer NOT NULL,
    id_convocatoria integer NOT NULL,
    descripcion_tema character varying(255) NOT NULL,
    activo boolean DEFAULT true
);


--
-- Name: banco_temas_id_tema_seq; Type: SEQUENCE; Schema: postulacion; Owner: -
--

ALTER TABLE postulacion.banco_temas ALTER COLUMN id_tema ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME postulacion.banco_temas_id_tema_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: comision_seleccion_id_comision_seleccion_seq; Type: SEQUENCE; Schema: postulacion; Owner: -
--

ALTER TABLE postulacion.comision_seleccion ALTER COLUMN id_comision_seleccion ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME postulacion.comision_seleccion_id_comision_seleccion_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: configuracion_oposicion; Type: TABLE; Schema: postulacion; Owner: -
--

CREATE TABLE postulacion.configuracion_oposicion (
    id_configuracion integer NOT NULL,
    id_convocatoria integer NOT NULL,
    max_puntaje_material numeric(5,2) DEFAULT 10.00 NOT NULL,
    max_puntaje_exposicion numeric(5,2) DEFAULT 4.00 NOT NULL,
    max_puntaje_respuestas numeric(5,2) DEFAULT 6.00 NOT NULL,
    minutos_exposicion integer DEFAULT 20 NOT NULL,
    minutos_preguntas integer DEFAULT 10 NOT NULL,
    minutos_transicion integer DEFAULT 5 NOT NULL,
    creado_en timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    activo boolean DEFAULT true,
    CONSTRAINT chk_puntajes_positivos CHECK (((max_puntaje_material > (0)::numeric) AND (max_puntaje_exposicion > (0)::numeric) AND (max_puntaje_respuestas > (0)::numeric)))
);


--
-- Name: configuracion_oposicion_id_configuracion_seq; Type: SEQUENCE; Schema: postulacion; Owner: -
--

ALTER TABLE postulacion.configuracion_oposicion ALTER COLUMN id_configuracion ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME postulacion.configuracion_oposicion_id_configuracion_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: evaluacion_meritos_id_evaluacion_meritos_seq; Type: SEQUENCE; Schema: postulacion; Owner: -
--

ALTER TABLE postulacion.evaluacion_meritos ALTER COLUMN id_evaluacion_meritos ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME postulacion.evaluacion_meritos_id_evaluacion_meritos_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: evaluacion_oposicion; Type: TABLE; Schema: postulacion; Owner: -
--

CREATE TABLE postulacion.evaluacion_oposicion (
    id_evaluacion_oposicion integer NOT NULL,
    id_postulacion integer NOT NULL,
    tema_exposicion character varying(150),
    fecha_evaluacion date,
    hora_inicio time without time zone,
    hora_fin time without time zone,
    lugar character varying(100),
    orden_exposicion integer,
    hora_inicio_real time without time zone,
    hora_fin_real time without time zone,
    puntaje_total_oposicion numeric(5,2),
    id_tipo_estado_evaluacion integer
);


--
-- Name: evaluacion_oposicion_id_evaluacion_oposicion_seq; Type: SEQUENCE; Schema: postulacion; Owner: -
--

ALTER TABLE postulacion.evaluacion_oposicion ALTER COLUMN id_evaluacion_oposicion ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME postulacion.evaluacion_oposicion_id_evaluacion_oposicion_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: postulacion; Type: TABLE; Schema: postulacion; Owner: -
--

CREATE TABLE postulacion.postulacion (
    id_postulacion integer NOT NULL,
    id_convocatoria integer NOT NULL,
    id_estudiante integer NOT NULL,
    fecha_postulacion date,
    observaciones character varying(500),
    activo boolean,
    id_tipo_estado_postulacion integer
);


--
-- Name: postulacion_id_postulacion_seq; Type: SEQUENCE; Schema: postulacion; Owner: -
--

ALTER TABLE postulacion.postulacion ALTER COLUMN id_postulacion ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME postulacion.postulacion_id_postulacion_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: requisito_adjunto; Type: TABLE; Schema: postulacion; Owner: -
--

CREATE TABLE postulacion.requisito_adjunto (
    id_requisito_adjunto integer NOT NULL,
    id_postulacion integer NOT NULL,
    id_tipo_requisito_postulacion integer NOT NULL,
    id_tipo_estado_requisito integer NOT NULL,
    archivo bytea,
    nombre_archivo character varying(150),
    fecha_subida date,
    observacion text,
    fecha_observacion timestamp without time zone
);


--
-- Name: COLUMN requisito_adjunto.fecha_observacion; Type: COMMENT; Schema: postulacion; Owner: -
--

COMMENT ON COLUMN postulacion.requisito_adjunto.fecha_observacion IS 'Timestamp del momento en que el coordinador marcó el documento como OBSERVADO. Usado para calcular la ventana de 24h de subsanación.';


--
-- Name: requisito_adjunto_id_requisito_adjunto_seq; Type: SEQUENCE; Schema: postulacion; Owner: -
--

ALTER TABLE postulacion.requisito_adjunto ALTER COLUMN id_requisito_adjunto ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME postulacion.requisito_adjunto_id_requisito_adjunto_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: tipo_estado_evaluacion; Type: TABLE; Schema: postulacion; Owner: -
--

CREATE TABLE postulacion.tipo_estado_evaluacion (
    id_tipo_estado_evaluacion integer NOT NULL,
    nombre character varying(50) NOT NULL,
    codigo character varying(30) NOT NULL,
    descripcion text,
    activo boolean DEFAULT true
);


--
-- Name: tipo_estado_evaluacion_id_tipo_estado_evaluacion_seq; Type: SEQUENCE; Schema: postulacion; Owner: -
--

ALTER TABLE postulacion.tipo_estado_evaluacion ALTER COLUMN id_tipo_estado_evaluacion ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME postulacion.tipo_estado_evaluacion_id_tipo_estado_evaluacion_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: tipo_estado_postulacion; Type: TABLE; Schema: postulacion; Owner: -
--

CREATE TABLE postulacion.tipo_estado_postulacion (
    id_tipo_estado_postulacion integer NOT NULL,
    codigo character varying(25) NOT NULL,
    nombre character varying(100) NOT NULL,
    descripcion character varying(255),
    activo boolean DEFAULT true,
    fecha_creacion timestamp without time zone DEFAULT now()
);


--
-- Name: tipo_estado_postulacion_id_tipo_estado_postulacion_seq; Type: SEQUENCE; Schema: postulacion; Owner: -
--

CREATE SEQUENCE postulacion.tipo_estado_postulacion_id_tipo_estado_postulacion_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: tipo_estado_postulacion_id_tipo_estado_postulacion_seq; Type: SEQUENCE OWNED BY; Schema: postulacion; Owner: -
--

ALTER SEQUENCE postulacion.tipo_estado_postulacion_id_tipo_estado_postulacion_seq OWNED BY postulacion.tipo_estado_postulacion.id_tipo_estado_postulacion;


--
-- Name: p_id_estudiante; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.p_id_estudiante (
    id_estudiante integer
);


--
-- Name: v_estado_ayudantia; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.v_estado_ayudantia (
    codigo character varying(25)
);


--
-- Name: v_estado_periodo; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.v_estado_periodo (
    estado character varying(30)
);


--
-- Name: v_id_ayudantia; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.v_id_ayudantia (
    id_ayudantia integer
);


--
-- Name: v_id_usr_decano; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.v_id_usr_decano (
    id_usuario integer
);


--
-- Name: v_sesion_data; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.v_sesion_data (
    json_build_object json
);


--
-- Name: v_tiene_rol; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.v_tiene_rol (
    "exists" boolean
);


--
-- Name: flyway_schema_history; Type: TABLE; Schema: seguridad; Owner: -
--

CREATE TABLE seguridad.flyway_schema_history (
    installed_rank integer NOT NULL,
    version character varying(50),
    description character varying(200) NOT NULL,
    type character varying(20) NOT NULL,
    script character varying(1000) NOT NULL,
    checksum integer,
    installed_by character varying(100) NOT NULL,
    installed_on timestamp without time zone DEFAULT now() NOT NULL,
    execution_time integer NOT NULL,
    success boolean NOT NULL
);


--
-- Name: privilegio; Type: TABLE; Schema: seguridad; Owner: -
--

CREATE TABLE seguridad.privilegio (
    id_privilegio integer NOT NULL,
    nombre_privilegio character varying(50) NOT NULL,
    codigo_interno character varying(1),
    descripcion text,
    activo boolean DEFAULT true NOT NULL
);


--
-- Name: privilegio_id_privilegio_seq; Type: SEQUENCE; Schema: seguridad; Owner: -
--

CREATE SEQUENCE seguridad.privilegio_id_privilegio_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: privilegio_id_privilegio_seq; Type: SEQUENCE OWNED BY; Schema: seguridad; Owner: -
--

ALTER SEQUENCE seguridad.privilegio_id_privilegio_seq OWNED BY seguridad.privilegio.id_privilegio;


--
-- Name: rol_bd; Type: TABLE; Schema: seguridad; Owner: -
--

CREATE TABLE seguridad.rol_bd (
    id_rol_bd integer NOT NULL,
    nombre_rol_bd character varying(100) NOT NULL,
    descripcion character varying(255)
);


--
-- Name: rol_bd_id_rol_bd_seq; Type: SEQUENCE; Schema: seguridad; Owner: -
--

CREATE SEQUENCE seguridad.rol_bd_id_rol_bd_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: rol_bd_id_rol_bd_seq; Type: SEQUENCE OWNED BY; Schema: seguridad; Owner: -
--

ALTER SEQUENCE seguridad.rol_bd_id_rol_bd_seq OWNED BY seguridad.rol_bd.id_rol_bd;


--
-- Name: tipo_objeto_seguridad; Type: TABLE; Schema: seguridad; Owner: -
--

CREATE TABLE seguridad.tipo_objeto_seguridad (
    id_tipo_objeto_seguridad integer NOT NULL,
    nombre_tipo_objeto character varying(50) NOT NULL,
    descripcion text
);


--
-- Name: tipo_objeto_seguridad_id_tipo_objeto_seguridad_seq; Type: SEQUENCE; Schema: seguridad; Owner: -
--

CREATE SEQUENCE seguridad.tipo_objeto_seguridad_id_tipo_objeto_seguridad_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: tipo_objeto_seguridad_id_tipo_objeto_seguridad_seq; Type: SEQUENCE OWNED BY; Schema: seguridad; Owner: -
--

ALTER SEQUENCE seguridad.tipo_objeto_seguridad_id_tipo_objeto_seguridad_seq OWNED BY seguridad.tipo_objeto_seguridad.id_tipo_objeto_seguridad;


--
-- Name: tipo_objeto_seguridad_privilegio; Type: TABLE; Schema: seguridad; Owner: -
--

CREATE TABLE seguridad.tipo_objeto_seguridad_privilegio (
    id_tipo_objeto_seguridad integer CONSTRAINT tipo_objeto_seguridad_privile_id_tipo_objeto_seguridad_not_null NOT NULL,
    id_privilegio integer NOT NULL
);


--
-- Name: tipo_rol; Type: TABLE; Schema: seguridad; Owner: -
--

CREATE TABLE seguridad.tipo_rol (
    id_tipo_rol integer NOT NULL,
    nombre_tipo_rol character varying(50) NOT NULL,
    activo boolean NOT NULL,
    id_rol_bd integer
);


--
-- Name: tipo_rol_id_tipo_rol_seq; Type: SEQUENCE; Schema: seguridad; Owner: -
--

ALTER TABLE seguridad.tipo_rol ALTER COLUMN id_tipo_rol ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME seguridad.tipo_rol_id_tipo_rol_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: usuario; Type: TABLE; Schema: seguridad; Owner: -
--

CREATE TABLE seguridad.usuario (
    id_usuario integer NOT NULL,
    nombres character varying(100) NOT NULL,
    apellidos character varying(100) NOT NULL,
    cedula character varying(20) NOT NULL,
    correo character varying(150) NOT NULL,
    nombre_usuario character varying(50) NOT NULL,
    contrasenia_usuario character varying(255) NOT NULL,
    fecha_creacion date DEFAULT CURRENT_DATE NOT NULL,
    activo boolean NOT NULL
);


--
-- Name: usuario_comision; Type: TABLE; Schema: seguridad; Owner: -
--

CREATE TABLE seguridad.usuario_comision (
    id_usuario_comision integer NOT NULL,
    id_comision_seleccion integer NOT NULL,
    id_usuario integer NOT NULL,
    id_evaluacion_oposicion integer,
    rol_integrante character varying(50),
    puntaje_material numeric(5,2),
    puntaje_respuestas numeric(5,2),
    puntaje_exposicion numeric(5,2),
    fecha_evaluacion date,
    activo boolean,
    finalizo_calificacion boolean DEFAULT false,
    CONSTRAINT chk_nota_exposicion CHECK ((puntaje_exposicion <= 4.00)),
    CONSTRAINT chk_nota_material CHECK ((puntaje_material <= 10.00)),
    CONSTRAINT chk_nota_respuestas CHECK ((puntaje_respuestas <= 6.00))
);


--
-- Name: usuario_comision_id_usuario_comision_seq; Type: SEQUENCE; Schema: seguridad; Owner: -
--

ALTER TABLE seguridad.usuario_comision ALTER COLUMN id_usuario_comision ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME seguridad.usuario_comision_id_usuario_comision_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: usuario_id_usuario_seq; Type: SEQUENCE; Schema: seguridad; Owner: -
--

ALTER TABLE seguridad.usuario ALTER COLUMN id_usuario ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME seguridad.usuario_id_usuario_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: usuario_tipo_rol; Type: TABLE; Schema: seguridad; Owner: -
--

CREATE TABLE seguridad.usuario_tipo_rol (
    id_usuario integer NOT NULL,
    id_tipo_rol integer NOT NULL,
    activo boolean NOT NULL,
    fecha_creacion timestamp without time zone NOT NULL
);


--
-- Name: v_lista_usuarios; Type: VIEW; Schema: seguridad; Owner: -
--

CREATE VIEW seguridad.v_lista_usuarios AS
 SELECT id_usuario,
    nombres,
    apellidos,
    cedula,
    correo,
    nombre_usuario,
    contrasenia_usuario,
    fecha_creacion,
    activo
   FROM seguridad.usuario;


--
-- Name: vw_elementos_bd; Type: VIEW; Schema: seguridad; Owner: -
--

CREATE VIEW seguridad.vw_elementos_bd AS
 SELECT (((tables.table_schema)::text || '.'::text) || (tables.table_name)::text) AS id_elemento,
    tables.table_schema AS esquema,
    tables.table_name AS nombre_elemento,
    'TABLAS'::text AS categoria
   FROM information_schema.tables
  WHERE (((tables.table_schema)::name <> ALL (ARRAY['information_schema'::name, 'pg_catalog'::name, 'pg_toast'::name])) AND ((tables.table_type)::text = 'BASE TABLE'::text))
UNION ALL
 SELECT (((tables.table_schema)::text || '.'::text) || (tables.table_name)::text) AS id_elemento,
    tables.table_schema AS esquema,
    tables.table_name AS nombre_elemento,
    'VISTAS'::text AS categoria
   FROM information_schema.tables
  WHERE (((tables.table_schema)::name <> ALL (ARRAY['information_schema'::name, 'pg_catalog'::name, 'pg_toast'::name])) AND ((tables.table_type)::text = 'VIEW'::text))
UNION ALL
 SELECT (((pg_matviews.schemaname)::text || '.'::text) || (pg_matviews.matviewname)::text) AS id_elemento,
    pg_matviews.schemaname AS esquema,
    pg_matviews.matviewname AS nombre_elemento,
    'VISTAS MATERIALIZADAS'::text AS categoria
   FROM pg_matviews
  WHERE (pg_matviews.schemaname <> ALL (ARRAY['information_schema'::name, 'pg_catalog'::name, 'pg_toast'::name]))
UNION ALL
 SELECT (((routines.routine_schema)::text || '.'::text) || (routines.routine_name)::text) AS id_elemento,
    routines.routine_schema AS esquema,
    routines.routine_name AS nombre_elemento,
    'FUNCIONES'::text AS categoria
   FROM information_schema.routines
  WHERE (((routines.routine_schema)::name <> ALL (ARRAY['information_schema'::name, 'pg_catalog'::name, 'pg_toast'::name])) AND ((routines.routine_type)::text = 'FUNCTION'::text))
UNION ALL
 SELECT (((routines.routine_schema)::text || '.'::text) || (routines.routine_name)::text) AS id_elemento,
    routines.routine_schema AS esquema,
    routines.routine_name AS nombre_elemento,
    'PROCEDIMIENTOS'::text AS categoria
   FROM information_schema.routines
  WHERE (((routines.routine_schema)::name <> ALL (ARRAY['information_schema'::name, 'pg_catalog'::name, 'pg_toast'::name])) AND ((routines.routine_type)::text = 'PROCEDURE'::text))
UNION ALL
 SELECT (((sequences.sequence_schema)::text || '.'::text) || (sequences.sequence_name)::text) AS id_elemento,
    sequences.sequence_schema AS esquema,
    sequences.sequence_name AS nombre_elemento,
    'SECUENCIAS'::text AS categoria
   FROM information_schema.sequences
  WHERE ((sequences.sequence_schema)::name <> ALL (ARRAY['information_schema'::name, 'pg_catalog'::name, 'pg_toast'::name]))
UNION ALL
 SELECT DISTINCT (((triggers.trigger_schema)::text || '.'::text) || (triggers.trigger_name)::text) AS id_elemento,
    triggers.trigger_schema AS esquema,
    triggers.trigger_name AS nombre_elemento,
    'TRIGGERS'::text AS categoria
   FROM information_schema.triggers
  WHERE ((triggers.trigger_schema)::name <> ALL (ARRAY['information_schema'::name, 'pg_catalog'::name, 'pg_toast'::name]));


--
-- Name: vw_elementos_bd2; Type: VIEW; Schema: seguridad; Owner: -
--

CREATE VIEW seguridad.vw_elementos_bd2 AS
 SELECT (((t.table_schema)::text || '.'::text) || (t.table_name)::text) AS id_elemento,
    t.table_schema AS esquema,
    t.table_name AS nombre_elemento,
    ( SELECT tipo_objeto_seguridad.id_tipo_objeto_seguridad
           FROM seguridad.tipo_objeto_seguridad
          WHERE ((tipo_objeto_seguridad.nombre_tipo_objeto)::text = 'TABLAS'::text)) AS id_tipo_objeto_seguridad
   FROM information_schema.tables t
  WHERE (((t.table_schema)::name <> ALL (ARRAY['information_schema'::name, 'pg_catalog'::name, 'pg_toast'::name])) AND ((t.table_type)::text = 'BASE TABLE'::text))
UNION ALL
 SELECT (((t.table_schema)::text || '.'::text) || (t.table_name)::text) AS id_elemento,
    t.table_schema AS esquema,
    t.table_name AS nombre_elemento,
    ( SELECT tipo_objeto_seguridad.id_tipo_objeto_seguridad
           FROM seguridad.tipo_objeto_seguridad
          WHERE ((tipo_objeto_seguridad.nombre_tipo_objeto)::text = 'VISTAS'::text)) AS id_tipo_objeto_seguridad
   FROM information_schema.tables t
  WHERE (((t.table_schema)::name <> ALL (ARRAY['information_schema'::name, 'pg_catalog'::name, 'pg_toast'::name])) AND ((t.table_type)::text = 'VIEW'::text))
UNION ALL
 SELECT (((m.schemaname)::text || '.'::text) || (m.matviewname)::text) AS id_elemento,
    m.schemaname AS esquema,
    m.matviewname AS nombre_elemento,
    ( SELECT tipo_objeto_seguridad.id_tipo_objeto_seguridad
           FROM seguridad.tipo_objeto_seguridad
          WHERE ((tipo_objeto_seguridad.nombre_tipo_objeto)::text = 'VISTAS MATERIALIZADAS'::text)) AS id_tipo_objeto_seguridad
   FROM pg_matviews m
  WHERE (m.schemaname <> ALL (ARRAY['information_schema'::name, 'pg_catalog'::name, 'pg_toast'::name]))
UNION ALL
 SELECT (((r.routine_schema)::text || '.'::text) || (r.routine_name)::text) AS id_elemento,
    r.routine_schema AS esquema,
    r.routine_name AS nombre_elemento,
    ( SELECT tipo_objeto_seguridad.id_tipo_objeto_seguridad
           FROM seguridad.tipo_objeto_seguridad
          WHERE ((tipo_objeto_seguridad.nombre_tipo_objeto)::text = 'FUNCIONES'::text)) AS id_tipo_objeto_seguridad
   FROM information_schema.routines r
  WHERE (((r.routine_schema)::name <> ALL (ARRAY['information_schema'::name, 'pg_catalog'::name, 'pg_toast'::name])) AND ((r.routine_type)::text = 'FUNCTION'::text))
UNION ALL
 SELECT (((r.routine_schema)::text || '.'::text) || (r.routine_name)::text) AS id_elemento,
    r.routine_schema AS esquema,
    r.routine_name AS nombre_elemento,
    ( SELECT tipo_objeto_seguridad.id_tipo_objeto_seguridad
           FROM seguridad.tipo_objeto_seguridad
          WHERE ((tipo_objeto_seguridad.nombre_tipo_objeto)::text = 'PROCEDIMIENTOS'::text)) AS id_tipo_objeto_seguridad
   FROM information_schema.routines r
  WHERE (((r.routine_schema)::name <> ALL (ARRAY['information_schema'::name, 'pg_catalog'::name, 'pg_toast'::name])) AND ((r.routine_type)::text = 'PROCEDURE'::text))
UNION ALL
 SELECT (((s.sequence_schema)::text || '.'::text) || (s.sequence_name)::text) AS id_elemento,
    s.sequence_schema AS esquema,
    s.sequence_name AS nombre_elemento,
    ( SELECT tipo_objeto_seguridad.id_tipo_objeto_seguridad
           FROM seguridad.tipo_objeto_seguridad
          WHERE ((tipo_objeto_seguridad.nombre_tipo_objeto)::text = 'SECUENCIAS'::text)) AS id_tipo_objeto_seguridad
   FROM information_schema.sequences s
  WHERE ((s.sequence_schema)::name <> ALL (ARRAY['information_schema'::name, 'pg_catalog'::name, 'pg_toast'::name]))
UNION ALL
 SELECT DISTINCT (((tr.trigger_schema)::text || '.'::text) || (tr.trigger_name)::text) AS id_elemento,
    tr.trigger_schema AS esquema,
    tr.trigger_name AS nombre_elemento,
    ( SELECT tipo_objeto_seguridad.id_tipo_objeto_seguridad
           FROM seguridad.tipo_objeto_seguridad
          WHERE ((tipo_objeto_seguridad.nombre_tipo_objeto)::text = 'TRIGGERS'::text)) AS id_tipo_objeto_seguridad
   FROM information_schema.triggers tr
  WHERE ((tr.trigger_schema)::name <> ALL (ARRAY['information_schema'::name, 'pg_catalog'::name, 'pg_toast'::name]));


--
-- Name: vw_lista_rol; Type: VIEW; Schema: seguridad; Owner: -
--

CREATE VIEW seguridad.vw_lista_rol AS
 SELECT id_tipo_rol,
    nombre_tipo_rol,
    activo,
    id_rol_bd
   FROM seguridad.tipo_rol;


--
-- Name: log_auditoria id_log_auditoria; Type: DEFAULT; Schema: academico; Owner: -
--

ALTER TABLE ONLY academico.log_auditoria ALTER COLUMN id_log_auditoria SET DEFAULT nextval('academico.log_auditoria_id_log_auditoria_seq'::regclass);


--
-- Name: notificacion_ws id_notificacion; Type: DEFAULT; Schema: notificacion; Owner: -
--

ALTER TABLE ONLY notificacion.notificacion_ws ALTER COLUMN id_notificacion SET DEFAULT nextval('notificacion.notificacion_ws_id_notificacion_seq'::regclass);


--
-- Name: periodo_fase id_periodo_fase; Type: DEFAULT; Schema: planificacion; Owner: -
--

ALTER TABLE ONLY planificacion.periodo_fase ALTER COLUMN id_periodo_fase SET DEFAULT nextval('planificacion.periodo_fase_id_periodo_fase_seq'::regclass);


--
-- Name: tipo_fase id_tipo_fase; Type: DEFAULT; Schema: planificacion; Owner: -
--

ALTER TABLE ONLY planificacion.tipo_fase ALTER COLUMN id_tipo_fase SET DEFAULT nextval('planificacion.tipo_fase_id_tipo_fase_seq'::regclass);


--
-- Name: tipo_estado_postulacion id_tipo_estado_postulacion; Type: DEFAULT; Schema: postulacion; Owner: -
--

ALTER TABLE ONLY postulacion.tipo_estado_postulacion ALTER COLUMN id_tipo_estado_postulacion SET DEFAULT nextval('postulacion.tipo_estado_postulacion_id_tipo_estado_postulacion_seq'::regclass);


--
-- Name: privilegio id_privilegio; Type: DEFAULT; Schema: seguridad; Owner: -
--

ALTER TABLE ONLY seguridad.privilegio ALTER COLUMN id_privilegio SET DEFAULT nextval('seguridad.privilegio_id_privilegio_seq'::regclass);


--
-- Name: rol_bd id_rol_bd; Type: DEFAULT; Schema: seguridad; Owner: -
--

ALTER TABLE ONLY seguridad.rol_bd ALTER COLUMN id_rol_bd SET DEFAULT nextval('seguridad.rol_bd_id_rol_bd_seq'::regclass);


--
-- Name: tipo_objeto_seguridad id_tipo_objeto_seguridad; Type: DEFAULT; Schema: seguridad; Owner: -
--

ALTER TABLE ONLY seguridad.tipo_objeto_seguridad ALTER COLUMN id_tipo_objeto_seguridad SET DEFAULT nextval('seguridad.tipo_objeto_seguridad_id_tipo_objeto_seguridad_seq'::regclass);


--
-- Name: asignatura asignatura_pkey; Type: CONSTRAINT; Schema: academico; Owner: -
--

ALTER TABLE ONLY academico.asignatura
    ADD CONSTRAINT asignatura_pkey PRIMARY KEY (id_asignatura);


--
-- Name: ayudante_catedra ayudante_catedra_pkey; Type: CONSTRAINT; Schema: academico; Owner: -
--

ALTER TABLE ONLY academico.ayudante_catedra
    ADD CONSTRAINT ayudante_catedra_pkey PRIMARY KEY (id_ayudante_catedra);


--
-- Name: carrera carrera_pkey; Type: CONSTRAINT; Schema: academico; Owner: -
--

ALTER TABLE ONLY academico.carrera
    ADD CONSTRAINT carrera_pkey PRIMARY KEY (id_carrera);


--
-- Name: coordinador coordinador_pkey; Type: CONSTRAINT; Schema: academico; Owner: -
--

ALTER TABLE ONLY academico.coordinador
    ADD CONSTRAINT coordinador_pkey PRIMARY KEY (id_coordinador);


--
-- Name: decano decano_pkey; Type: CONSTRAINT; Schema: academico; Owner: -
--

ALTER TABLE ONLY academico.decano
    ADD CONSTRAINT decano_pkey PRIMARY KEY (id_decano);


--
-- Name: docente_asignatura docente_asignatura_pkey; Type: CONSTRAINT; Schema: academico; Owner: -
--

ALTER TABLE ONLY academico.docente_asignatura
    ADD CONSTRAINT docente_asignatura_pkey PRIMARY KEY (id_docente_asignatura);


--
-- Name: docente docente_pkey; Type: CONSTRAINT; Schema: academico; Owner: -
--

ALTER TABLE ONLY academico.docente
    ADD CONSTRAINT docente_pkey PRIMARY KEY (id_docente);


--
-- Name: estudiante estudiante_matricula_key; Type: CONSTRAINT; Schema: academico; Owner: -
--

ALTER TABLE ONLY academico.estudiante
    ADD CONSTRAINT estudiante_matricula_key UNIQUE (matricula);


--
-- Name: estudiante estudiante_pkey; Type: CONSTRAINT; Schema: academico; Owner: -
--

ALTER TABLE ONLY academico.estudiante
    ADD CONSTRAINT estudiante_pkey PRIMARY KEY (id_estudiante);


--
-- Name: facultad facultad_pkey; Type: CONSTRAINT; Schema: academico; Owner: -
--

ALTER TABLE ONLY academico.facultad
    ADD CONSTRAINT facultad_pkey PRIMARY KEY (id_facultad);


--
-- Name: log_auditoria log_auditoria_pkey; Type: CONSTRAINT; Schema: academico; Owner: -
--

ALTER TABLE ONLY academico.log_auditoria
    ADD CONSTRAINT log_auditoria_pkey PRIMARY KEY (id_log_auditoria);


--
-- Name: periodo_academico periodo_academico_pkey; Type: CONSTRAINT; Schema: academico; Owner: -
--

ALTER TABLE ONLY academico.periodo_academico
    ADD CONSTRAINT periodo_academico_pkey PRIMARY KEY (id_periodo_academico);


--
-- Name: ayudante_catedra uq_ayudante_usuario; Type: CONSTRAINT; Schema: academico; Owner: -
--

ALTER TABLE ONLY academico.ayudante_catedra
    ADD CONSTRAINT uq_ayudante_usuario UNIQUE (id_usuario);


--
-- Name: ayudante_catedra ayudante_catedra_pkey; Type: CONSTRAINT; Schema: ayudantia; Owner: -
--

ALTER TABLE ONLY ayudantia.ayudante_catedra
    ADD CONSTRAINT ayudante_catedra_pkey PRIMARY KEY (id_ayudante_catedra);


--
-- Name: ayudantia ayudantia_pkey; Type: CONSTRAINT; Schema: ayudantia; Owner: -
--

ALTER TABLE ONLY ayudantia.ayudantia
    ADD CONSTRAINT ayudantia_pkey PRIMARY KEY (id_ayudantia);


--
-- Name: certificado certificado_pkey; Type: CONSTRAINT; Schema: ayudantia; Owner: -
--

ALTER TABLE ONLY ayudantia.certificado
    ADD CONSTRAINT certificado_pkey PRIMARY KEY (id_certificado);


--
-- Name: detalle_asistencia_actividad detalle_asistencia_actividad_pkey; Type: CONSTRAINT; Schema: ayudantia; Owner: -
--

ALTER TABLE ONLY ayudantia.detalle_asistencia_actividad
    ADD CONSTRAINT detalle_asistencia_actividad_pkey PRIMARY KEY (id_detalle_asistencia_actividad);


--
-- Name: documento_academico documento_academico_pkey; Type: CONSTRAINT; Schema: ayudantia; Owner: -
--

ALTER TABLE ONLY ayudantia.documento_academico
    ADD CONSTRAINT documento_academico_pkey PRIMARY KEY (id_documento);


--
-- Name: evidencia_registro_actividad evidencia_registro_actividad_pkey; Type: CONSTRAINT; Schema: ayudantia; Owner: -
--

ALTER TABLE ONLY ayudantia.evidencia_registro_actividad
    ADD CONSTRAINT evidencia_registro_actividad_pkey PRIMARY KEY (id_evidencia_registro_actividad);


--
-- Name: participante_ayudantia participante_ayudantia_pkey; Type: CONSTRAINT; Schema: ayudantia; Owner: -
--

ALTER TABLE ONLY ayudantia.participante_ayudantia
    ADD CONSTRAINT participante_ayudantia_pkey PRIMARY KEY (id_participante_ayudantia);


--
-- Name: registro_actividad registro_actividad_pkey; Type: CONSTRAINT; Schema: ayudantia; Owner: -
--

ALTER TABLE ONLY ayudantia.registro_actividad
    ADD CONSTRAINT registro_actividad_pkey PRIMARY KEY (id_registro_actividad);


--
-- Name: sancion_ayudante_catedra sancion_ayudante_catedra_pkey; Type: CONSTRAINT; Schema: ayudantia; Owner: -
--

ALTER TABLE ONLY ayudantia.sancion_ayudante_catedra
    ADD CONSTRAINT sancion_ayudante_catedra_pkey PRIMARY KEY (id_sancion_ayudante_catedra);


--
-- Name: tipo_documento tipo_documento_codigo_key; Type: CONSTRAINT; Schema: ayudantia; Owner: -
--

ALTER TABLE ONLY ayudantia.tipo_documento
    ADD CONSTRAINT tipo_documento_codigo_key UNIQUE (codigo);


--
-- Name: tipo_documento tipo_documento_pkey; Type: CONSTRAINT; Schema: ayudantia; Owner: -
--

ALTER TABLE ONLY ayudantia.tipo_documento
    ADD CONSTRAINT tipo_documento_pkey PRIMARY KEY (id_tipo_documento);


--
-- Name: tipo_estado_ayudantia tipo_estado_ayudantia_codigo_key; Type: CONSTRAINT; Schema: ayudantia; Owner: -
--

ALTER TABLE ONLY ayudantia.tipo_estado_ayudantia
    ADD CONSTRAINT tipo_estado_ayudantia_codigo_key UNIQUE (codigo);


--
-- Name: tipo_estado_ayudantia tipo_estado_ayudantia_nombre_estado_key; Type: CONSTRAINT; Schema: ayudantia; Owner: -
--

ALTER TABLE ONLY ayudantia.tipo_estado_ayudantia
    ADD CONSTRAINT tipo_estado_ayudantia_nombre_estado_key UNIQUE (nombre_estado);


--
-- Name: tipo_estado_ayudantia tipo_estado_ayudantia_pkey; Type: CONSTRAINT; Schema: ayudantia; Owner: -
--

ALTER TABLE ONLY ayudantia.tipo_estado_ayudantia
    ADD CONSTRAINT tipo_estado_ayudantia_pkey PRIMARY KEY (id_tipo_estado_ayudantia);


--
-- Name: tipo_estado_evidencia tipo_estado_evidencia_codigo_key; Type: CONSTRAINT; Schema: ayudantia; Owner: -
--

ALTER TABLE ONLY ayudantia.tipo_estado_evidencia
    ADD CONSTRAINT tipo_estado_evidencia_codigo_key UNIQUE (codigo);


--
-- Name: tipo_estado_evidencia tipo_estado_evidencia_nombre_estado_key; Type: CONSTRAINT; Schema: ayudantia; Owner: -
--

ALTER TABLE ONLY ayudantia.tipo_estado_evidencia
    ADD CONSTRAINT tipo_estado_evidencia_nombre_estado_key UNIQUE (nombre_estado);


--
-- Name: tipo_estado_evidencia tipo_estado_evidencia_pkey; Type: CONSTRAINT; Schema: ayudantia; Owner: -
--

ALTER TABLE ONLY ayudantia.tipo_estado_evidencia
    ADD CONSTRAINT tipo_estado_evidencia_pkey PRIMARY KEY (id_tipo_estado_evidencia);


--
-- Name: tipo_estado_registro tipo_estado_registro_codigo_key; Type: CONSTRAINT; Schema: ayudantia; Owner: -
--

ALTER TABLE ONLY ayudantia.tipo_estado_registro
    ADD CONSTRAINT tipo_estado_registro_codigo_key UNIQUE (codigo);


--
-- Name: tipo_estado_registro tipo_estado_registro_nombre_estado_key; Type: CONSTRAINT; Schema: ayudantia; Owner: -
--

ALTER TABLE ONLY ayudantia.tipo_estado_registro
    ADD CONSTRAINT tipo_estado_registro_nombre_estado_key UNIQUE (nombre_estado);


--
-- Name: tipo_estado_registro tipo_estado_registro_pkey; Type: CONSTRAINT; Schema: ayudantia; Owner: -
--

ALTER TABLE ONLY ayudantia.tipo_estado_registro
    ADD CONSTRAINT tipo_estado_registro_pkey PRIMARY KEY (id_tipo_estado_registro);


--
-- Name: tipo_evidencia tipo_evidencia_codigo_key; Type: CONSTRAINT; Schema: ayudantia; Owner: -
--

ALTER TABLE ONLY ayudantia.tipo_evidencia
    ADD CONSTRAINT tipo_evidencia_codigo_key UNIQUE (codigo);


--
-- Name: tipo_evidencia tipo_evidencia_nombre_key; Type: CONSTRAINT; Schema: ayudantia; Owner: -
--

ALTER TABLE ONLY ayudantia.tipo_evidencia
    ADD CONSTRAINT tipo_evidencia_nombre_key UNIQUE (nombre);


--
-- Name: tipo_evidencia tipo_evidencia_pkey; Type: CONSTRAINT; Schema: ayudantia; Owner: -
--

ALTER TABLE ONLY ayudantia.tipo_evidencia
    ADD CONSTRAINT tipo_evidencia_pkey PRIMARY KEY (id_tipo_evidencia);


--
-- Name: tipo_sancion_ayudante_catedra tipo_sancion_ayudante_catedra_codigo_key; Type: CONSTRAINT; Schema: ayudantia; Owner: -
--

ALTER TABLE ONLY ayudantia.tipo_sancion_ayudante_catedra
    ADD CONSTRAINT tipo_sancion_ayudante_catedra_codigo_key UNIQUE (codigo);


--
-- Name: tipo_sancion_ayudante_catedra tipo_sancion_ayudante_catedra_pkey; Type: CONSTRAINT; Schema: ayudantia; Owner: -
--

ALTER TABLE ONLY ayudantia.tipo_sancion_ayudante_catedra
    ADD CONSTRAINT tipo_sancion_ayudante_catedra_pkey PRIMARY KEY (id_tipo_sancion_ayudante_catedra);


--
-- Name: detalle_asistencia_actividad uq_asistencia_por_actividad; Type: CONSTRAINT; Schema: ayudantia; Owner: -
--

ALTER TABLE ONLY ayudantia.detalle_asistencia_actividad
    ADD CONSTRAINT uq_asistencia_por_actividad UNIQUE (id_registro_actividad, id_participante_ayudantia);


--
-- Name: ayudantia uq_ayudantia_postulacion; Type: CONSTRAINT; Schema: ayudantia; Owner: -
--

ALTER TABLE ONLY ayudantia.ayudantia
    ADD CONSTRAINT uq_ayudantia_postulacion UNIQUE (id_postulacion);


--
-- Name: participante_ayudantia uq_participante_curso_ayudantia; Type: CONSTRAINT; Schema: ayudantia; Owner: -
--

ALTER TABLE ONLY ayudantia.participante_ayudantia
    ADD CONSTRAINT uq_participante_curso_ayudantia UNIQUE (id_ayudantia, nombre_completo, curso, paralelo);


--
-- Name: convocatoria convocatoria_pkey; Type: CONSTRAINT; Schema: convocatoria; Owner: -
--

ALTER TABLE ONLY convocatoria.convocatoria
    ADD CONSTRAINT convocatoria_pkey PRIMARY KEY (id_convocatoria);


--
-- Name: periodo_academico_requisito_postulacion periodo_academico_requisito_postulacion_pkey; Type: CONSTRAINT; Schema: convocatoria; Owner: -
--

ALTER TABLE ONLY convocatoria.periodo_academico_requisito_postulacion
    ADD CONSTRAINT periodo_academico_requisito_postulacion_pkey PRIMARY KEY (id_periodo_academico_requisito_postulacion);


--
-- Name: tipo_estado_requisito tipo_estado_requisito_codigo_key; Type: CONSTRAINT; Schema: convocatoria; Owner: -
--

ALTER TABLE ONLY convocatoria.tipo_estado_requisito
    ADD CONSTRAINT tipo_estado_requisito_codigo_key UNIQUE (codigo);


--
-- Name: tipo_estado_requisito tipo_estado_requisito_pkey; Type: CONSTRAINT; Schema: convocatoria; Owner: -
--

ALTER TABLE ONLY convocatoria.tipo_estado_requisito
    ADD CONSTRAINT tipo_estado_requisito_pkey PRIMARY KEY (id_tipo_estado_requisito);


--
-- Name: tipo_requisito_postulacion tipo_requisito_postulacion_pkey; Type: CONSTRAINT; Schema: convocatoria; Owner: -
--

ALTER TABLE ONLY convocatoria.tipo_requisito_postulacion
    ADD CONSTRAINT tipo_requisito_postulacion_pkey PRIMARY KEY (id_tipo_requisito_postulacion);


--
-- Name: log_auditoria log_auditoria_pkey; Type: CONSTRAINT; Schema: notificacion; Owner: -
--

ALTER TABLE ONLY notificacion.log_auditoria
    ADD CONSTRAINT log_auditoria_pkey PRIMARY KEY (id_log_auditoria);


--
-- Name: notificacion notificacion_pkey; Type: CONSTRAINT; Schema: notificacion; Owner: -
--

ALTER TABLE ONLY notificacion.notificacion
    ADD CONSTRAINT notificacion_pkey PRIMARY KEY (id_notificacion);


--
-- Name: notificacion_ws notificacion_ws_pkey; Type: CONSTRAINT; Schema: notificacion; Owner: -
--

ALTER TABLE ONLY notificacion.notificacion_ws
    ADD CONSTRAINT notificacion_ws_pkey PRIMARY KEY (id_notificacion);


--
-- Name: periodo_fase periodo_fase_pkey; Type: CONSTRAINT; Schema: planificacion; Owner: -
--

ALTER TABLE ONLY planificacion.periodo_fase
    ADD CONSTRAINT periodo_fase_pkey PRIMARY KEY (id_periodo_fase);


--
-- Name: tipo_fase tipo_fase_codigo_key; Type: CONSTRAINT; Schema: planificacion; Owner: -
--

ALTER TABLE ONLY planificacion.tipo_fase
    ADD CONSTRAINT tipo_fase_codigo_key UNIQUE (codigo);


--
-- Name: tipo_fase tipo_fase_orden_key; Type: CONSTRAINT; Schema: planificacion; Owner: -
--

ALTER TABLE ONLY planificacion.tipo_fase
    ADD CONSTRAINT tipo_fase_orden_key UNIQUE (orden);


--
-- Name: tipo_fase tipo_fase_pkey; Type: CONSTRAINT; Schema: planificacion; Owner: -
--

ALTER TABLE ONLY planificacion.tipo_fase
    ADD CONSTRAINT tipo_fase_pkey PRIMARY KEY (id_tipo_fase);


--
-- Name: periodo_fase uq_periodo_tipo_fase; Type: CONSTRAINT; Schema: planificacion; Owner: -
--

ALTER TABLE ONLY planificacion.periodo_fase
    ADD CONSTRAINT uq_periodo_tipo_fase UNIQUE (id_periodo_academico, id_tipo_fase);


--
-- Name: banco_temas banco_temas_pkey; Type: CONSTRAINT; Schema: postulacion; Owner: -
--

ALTER TABLE ONLY postulacion.banco_temas
    ADD CONSTRAINT banco_temas_pkey PRIMARY KEY (id_tema);


--
-- Name: comision_seleccion comision_seleccion_pkey; Type: CONSTRAINT; Schema: postulacion; Owner: -
--

ALTER TABLE ONLY postulacion.comision_seleccion
    ADD CONSTRAINT comision_seleccion_pkey PRIMARY KEY (id_comision_seleccion);


--
-- Name: configuracion_oposicion configuracion_oposicion_id_convocatoria_key; Type: CONSTRAINT; Schema: postulacion; Owner: -
--

ALTER TABLE ONLY postulacion.configuracion_oposicion
    ADD CONSTRAINT configuracion_oposicion_id_convocatoria_key UNIQUE (id_convocatoria);


--
-- Name: configuracion_oposicion configuracion_oposicion_pkey; Type: CONSTRAINT; Schema: postulacion; Owner: -
--

ALTER TABLE ONLY postulacion.configuracion_oposicion
    ADD CONSTRAINT configuracion_oposicion_pkey PRIMARY KEY (id_configuracion);


--
-- Name: evaluacion_meritos evaluacion_meritos_pkey; Type: CONSTRAINT; Schema: postulacion; Owner: -
--

ALTER TABLE ONLY postulacion.evaluacion_meritos
    ADD CONSTRAINT evaluacion_meritos_pkey PRIMARY KEY (id_evaluacion_meritos);


--
-- Name: evaluacion_oposicion evaluacion_oposicion_pkey; Type: CONSTRAINT; Schema: postulacion; Owner: -
--

ALTER TABLE ONLY postulacion.evaluacion_oposicion
    ADD CONSTRAINT evaluacion_oposicion_pkey PRIMARY KEY (id_evaluacion_oposicion);


--
-- Name: postulacion postulacion_pkey; Type: CONSTRAINT; Schema: postulacion; Owner: -
--

ALTER TABLE ONLY postulacion.postulacion
    ADD CONSTRAINT postulacion_pkey PRIMARY KEY (id_postulacion);


--
-- Name: requisito_adjunto requisito_adjunto_pkey; Type: CONSTRAINT; Schema: postulacion; Owner: -
--

ALTER TABLE ONLY postulacion.requisito_adjunto
    ADD CONSTRAINT requisito_adjunto_pkey PRIMARY KEY (id_requisito_adjunto);


--
-- Name: tipo_estado_evaluacion tipo_estado_evaluacion_pkey; Type: CONSTRAINT; Schema: postulacion; Owner: -
--

ALTER TABLE ONLY postulacion.tipo_estado_evaluacion
    ADD CONSTRAINT tipo_estado_evaluacion_pkey PRIMARY KEY (id_tipo_estado_evaluacion);


--
-- Name: tipo_estado_postulacion tipo_estado_postulacion_codigo_key; Type: CONSTRAINT; Schema: postulacion; Owner: -
--

ALTER TABLE ONLY postulacion.tipo_estado_postulacion
    ADD CONSTRAINT tipo_estado_postulacion_codigo_key UNIQUE (codigo);


--
-- Name: tipo_estado_postulacion tipo_estado_postulacion_pkey; Type: CONSTRAINT; Schema: postulacion; Owner: -
--

ALTER TABLE ONLY postulacion.tipo_estado_postulacion
    ADD CONSTRAINT tipo_estado_postulacion_pkey PRIMARY KEY (id_tipo_estado_postulacion);


--
-- Name: flyway_schema_history flyway_schema_history_pk; Type: CONSTRAINT; Schema: seguridad; Owner: -
--

ALTER TABLE ONLY seguridad.flyway_schema_history
    ADD CONSTRAINT flyway_schema_history_pk PRIMARY KEY (installed_rank);


--
-- Name: privilegio privilegio_nombre_privilegio_key; Type: CONSTRAINT; Schema: seguridad; Owner: -
--

ALTER TABLE ONLY seguridad.privilegio
    ADD CONSTRAINT privilegio_nombre_privilegio_key UNIQUE (nombre_privilegio);


--
-- Name: privilegio privilegio_pkey; Type: CONSTRAINT; Schema: seguridad; Owner: -
--

ALTER TABLE ONLY seguridad.privilegio
    ADD CONSTRAINT privilegio_pkey PRIMARY KEY (id_privilegio);


--
-- Name: rol_bd rol_bd_nombre_rol_bd_key; Type: CONSTRAINT; Schema: seguridad; Owner: -
--

ALTER TABLE ONLY seguridad.rol_bd
    ADD CONSTRAINT rol_bd_nombre_rol_bd_key UNIQUE (nombre_rol_bd);


--
-- Name: rol_bd rol_bd_pkey; Type: CONSTRAINT; Schema: seguridad; Owner: -
--

ALTER TABLE ONLY seguridad.rol_bd
    ADD CONSTRAINT rol_bd_pkey PRIMARY KEY (id_rol_bd);


--
-- Name: tipo_objeto_seguridad tipo_objeto_seguridad_nombre_tipo_objeto_key; Type: CONSTRAINT; Schema: seguridad; Owner: -
--

ALTER TABLE ONLY seguridad.tipo_objeto_seguridad
    ADD CONSTRAINT tipo_objeto_seguridad_nombre_tipo_objeto_key UNIQUE (nombre_tipo_objeto);


--
-- Name: tipo_objeto_seguridad tipo_objeto_seguridad_pkey; Type: CONSTRAINT; Schema: seguridad; Owner: -
--

ALTER TABLE ONLY seguridad.tipo_objeto_seguridad
    ADD CONSTRAINT tipo_objeto_seguridad_pkey PRIMARY KEY (id_tipo_objeto_seguridad);


--
-- Name: tipo_objeto_seguridad_privilegio tipo_objeto_seguridad_privilegio_pkey; Type: CONSTRAINT; Schema: seguridad; Owner: -
--

ALTER TABLE ONLY seguridad.tipo_objeto_seguridad_privilegio
    ADD CONSTRAINT tipo_objeto_seguridad_privilegio_pkey PRIMARY KEY (id_tipo_objeto_seguridad, id_privilegio);


--
-- Name: tipo_rol tipo_rol_nombre_tipo_rol_key; Type: CONSTRAINT; Schema: seguridad; Owner: -
--

ALTER TABLE ONLY seguridad.tipo_rol
    ADD CONSTRAINT tipo_rol_nombre_tipo_rol_key UNIQUE (nombre_tipo_rol);


--
-- Name: tipo_rol tipo_rol_pkey; Type: CONSTRAINT; Schema: seguridad; Owner: -
--

ALTER TABLE ONLY seguridad.tipo_rol
    ADD CONSTRAINT tipo_rol_pkey PRIMARY KEY (id_tipo_rol);


--
-- Name: usuario_tipo_rol uq_usuario_rol; Type: CONSTRAINT; Schema: seguridad; Owner: -
--

ALTER TABLE ONLY seguridad.usuario_tipo_rol
    ADD CONSTRAINT uq_usuario_rol UNIQUE (id_usuario, id_tipo_rol);


--
-- Name: usuario usuario_cedula_key; Type: CONSTRAINT; Schema: seguridad; Owner: -
--

ALTER TABLE ONLY seguridad.usuario
    ADD CONSTRAINT usuario_cedula_key UNIQUE (cedula);


--
-- Name: usuario_comision usuario_comision_pkey; Type: CONSTRAINT; Schema: seguridad; Owner: -
--

ALTER TABLE ONLY seguridad.usuario_comision
    ADD CONSTRAINT usuario_comision_pkey PRIMARY KEY (id_usuario_comision);


--
-- Name: usuario usuario_correo_key; Type: CONSTRAINT; Schema: seguridad; Owner: -
--

ALTER TABLE ONLY seguridad.usuario
    ADD CONSTRAINT usuario_correo_key UNIQUE (correo);


--
-- Name: usuario usuario_nombre_usuario_key; Type: CONSTRAINT; Schema: seguridad; Owner: -
--

ALTER TABLE ONLY seguridad.usuario
    ADD CONSTRAINT usuario_nombre_usuario_key UNIQUE (nombre_usuario);


--
-- Name: usuario usuario_pkey; Type: CONSTRAINT; Schema: seguridad; Owner: -
--

ALTER TABLE ONLY seguridad.usuario
    ADD CONSTRAINT usuario_pkey PRIMARY KEY (id_usuario);


--
-- Name: usuario_tipo_rol usuario_tipo_rol_pkey; Type: CONSTRAINT; Schema: seguridad; Owner: -
--

ALTER TABLE ONLY seguridad.usuario_tipo_rol
    ADD CONSTRAINT usuario_tipo_rol_pkey PRIMARY KEY (id_usuario, id_tipo_rol);


--
-- Name: idx_ayudante_id_usuario; Type: INDEX; Schema: academico; Owner: -
--

CREATE INDEX idx_ayudante_id_usuario ON academico.ayudante_catedra USING btree (id_usuario);


--
-- Name: idx_coordinador_id_usuario_activo; Type: INDEX; Schema: academico; Owner: -
--

CREATE INDEX idx_coordinador_id_usuario_activo ON academico.coordinador USING btree (id_usuario, activo);


--
-- Name: idx_decano_id_usuario_activo; Type: INDEX; Schema: academico; Owner: -
--

CREATE INDEX idx_decano_id_usuario_activo ON academico.decano USING btree (id_usuario, activo);


--
-- Name: idx_docente_id_usuario_activo; Type: INDEX; Schema: academico; Owner: -
--

CREATE INDEX idx_docente_id_usuario_activo ON academico.docente USING btree (id_usuario, activo);


--
-- Name: idx_estudiante_id_usuario; Type: INDEX; Schema: academico; Owner: -
--

CREATE INDEX idx_estudiante_id_usuario ON academico.estudiante USING btree (id_usuario);


--
-- Name: idx_log_auditoria_usuario_fecha; Type: INDEX; Schema: academico; Owner: -
--

CREATE INDEX idx_log_auditoria_usuario_fecha ON academico.log_auditoria USING btree (id_usuario, fecha_hora DESC);


--
-- Name: uq_docente_asignatura; Type: INDEX; Schema: academico; Owner: -
--

CREATE UNIQUE INDEX uq_docente_asignatura ON academico.docente_asignatura USING btree (id_docente, id_asignatura);


--
-- Name: idx_asistencia_por_registro; Type: INDEX; Schema: ayudantia; Owner: -
--

CREATE INDEX idx_asistencia_por_registro ON ayudantia.detalle_asistencia_actividad USING btree (id_registro_actividad);


--
-- Name: idx_participantes_por_ayudantia; Type: INDEX; Schema: ayudantia; Owner: -
--

CREATE INDEX idx_participantes_por_ayudantia ON ayudantia.participante_ayudantia USING btree (id_ayudantia);


--
-- Name: idx_periodo_fase_fechas; Type: INDEX; Schema: planificacion; Owner: -
--

CREATE INDEX idx_periodo_fase_fechas ON planificacion.periodo_fase USING btree (fecha_inicio, fecha_fin);


--
-- Name: idx_periodo_fase_periodo_tipo; Type: INDEX; Schema: planificacion; Owner: -
--

CREATE INDEX idx_periodo_fase_periodo_tipo ON planificacion.periodo_fase USING btree (id_periodo_academico, id_tipo_fase);


--
-- Name: idx_comision_id_convocatoria; Type: INDEX; Schema: postulacion; Owner: -
--

CREATE INDEX idx_comision_id_convocatoria ON postulacion.comision_seleccion USING btree (id_convocatoria);


--
-- Name: idx_evaluacion_postulacion; Type: INDEX; Schema: postulacion; Owner: -
--

CREATE INDEX idx_evaluacion_postulacion ON postulacion.evaluacion_oposicion USING btree (id_postulacion);


--
-- Name: idx_postulacion_id_convocatoria; Type: INDEX; Schema: postulacion; Owner: -
--

CREATE INDEX idx_postulacion_id_convocatoria ON postulacion.postulacion USING btree (id_convocatoria);


--
-- Name: idx_postulacion_id_estudiante; Type: INDEX; Schema: postulacion; Owner: -
--

CREATE INDEX idx_postulacion_id_estudiante ON postulacion.postulacion USING btree (id_estudiante);


--
-- Name: idx_postulacion_tipo_estado; Type: INDEX; Schema: postulacion; Owner: -
--

CREATE INDEX idx_postulacion_tipo_estado ON postulacion.postulacion USING btree (id_tipo_estado_postulacion);


--
-- Name: flyway_schema_history_s_idx; Type: INDEX; Schema: seguridad; Owner: -
--

CREATE INDEX flyway_schema_history_s_idx ON seguridad.flyway_schema_history USING btree (success);


--
-- Name: idx_tipo_rol_activo; Type: INDEX; Schema: seguridad; Owner: -
--

CREATE INDEX idx_tipo_rol_activo ON seguridad.tipo_rol USING btree (id_tipo_rol, activo);


--
-- Name: idx_tipo_rol_nombre; Type: INDEX; Schema: seguridad; Owner: -
--

CREATE INDEX idx_tipo_rol_nombre ON seguridad.tipo_rol USING btree (nombre_tipo_rol);


--
-- Name: idx_usu_comision_id_comision; Type: INDEX; Schema: seguridad; Owner: -
--

CREATE INDEX idx_usu_comision_id_comision ON seguridad.usuario_comision USING btree (id_comision_seleccion);


--
-- Name: idx_utr_id_usuario_activo; Type: INDEX; Schema: seguridad; Owner: -
--

CREATE INDEX idx_utr_id_usuario_activo ON seguridad.usuario_tipo_rol USING btree (id_usuario, activo);


--
-- Name: periodo_academico trg_verificar_estado_periodo; Type: TRIGGER; Schema: academico; Owner: -
--

CREATE TRIGGER trg_verificar_estado_periodo BEFORE UPDATE ON academico.periodo_academico FOR EACH ROW EXECUTE FUNCTION academico.fn_verificar_estado_periodo();


--
-- Name: periodo_fase trg_validar_periodo_fase; Type: TRIGGER; Schema: planificacion; Owner: -
--

CREATE TRIGGER trg_validar_periodo_fase BEFORE INSERT OR UPDATE ON planificacion.periodo_fase FOR EACH ROW EXECUTE FUNCTION planificacion.fn_validar_periodo_fase();


--
-- Name: asignatura asignatura_id_carrera_fkey; Type: FK CONSTRAINT; Schema: academico; Owner: -
--

ALTER TABLE ONLY academico.asignatura
    ADD CONSTRAINT asignatura_id_carrera_fkey FOREIGN KEY (id_carrera) REFERENCES academico.carrera(id_carrera);


--
-- Name: carrera carrera_id_facultad_fkey; Type: FK CONSTRAINT; Schema: academico; Owner: -
--

ALTER TABLE ONLY academico.carrera
    ADD CONSTRAINT carrera_id_facultad_fkey FOREIGN KEY (id_facultad) REFERENCES academico.facultad(id_facultad);


--
-- Name: coordinador coordinador_id_carrera_fkey; Type: FK CONSTRAINT; Schema: academico; Owner: -
--

ALTER TABLE ONLY academico.coordinador
    ADD CONSTRAINT coordinador_id_carrera_fkey FOREIGN KEY (id_carrera) REFERENCES academico.carrera(id_carrera);


--
-- Name: coordinador coordinador_id_usuario_fkey; Type: FK CONSTRAINT; Schema: academico; Owner: -
--

ALTER TABLE ONLY academico.coordinador
    ADD CONSTRAINT coordinador_id_usuario_fkey FOREIGN KEY (id_usuario) REFERENCES seguridad.usuario(id_usuario);


--
-- Name: decano decano_id_facultad_fkey; Type: FK CONSTRAINT; Schema: academico; Owner: -
--

ALTER TABLE ONLY academico.decano
    ADD CONSTRAINT decano_id_facultad_fkey FOREIGN KEY (id_facultad) REFERENCES academico.facultad(id_facultad);


--
-- Name: decano decano_id_usuario_fkey; Type: FK CONSTRAINT; Schema: academico; Owner: -
--

ALTER TABLE ONLY academico.decano
    ADD CONSTRAINT decano_id_usuario_fkey FOREIGN KEY (id_usuario) REFERENCES seguridad.usuario(id_usuario);


--
-- Name: docente_asignatura docente_asignatura_id_asignatura_fkey; Type: FK CONSTRAINT; Schema: academico; Owner: -
--

ALTER TABLE ONLY academico.docente_asignatura
    ADD CONSTRAINT docente_asignatura_id_asignatura_fkey FOREIGN KEY (id_asignatura) REFERENCES academico.asignatura(id_asignatura);


--
-- Name: docente_asignatura docente_asignatura_id_docente_fkey; Type: FK CONSTRAINT; Schema: academico; Owner: -
--

ALTER TABLE ONLY academico.docente_asignatura
    ADD CONSTRAINT docente_asignatura_id_docente_fkey FOREIGN KEY (id_docente) REFERENCES academico.docente(id_docente);


--
-- Name: docente docente_id_usuario_fkey; Type: FK CONSTRAINT; Schema: academico; Owner: -
--

ALTER TABLE ONLY academico.docente
    ADD CONSTRAINT docente_id_usuario_fkey FOREIGN KEY (id_usuario) REFERENCES seguridad.usuario(id_usuario);


--
-- Name: estudiante estudiante_id_carrera_fkey; Type: FK CONSTRAINT; Schema: academico; Owner: -
--

ALTER TABLE ONLY academico.estudiante
    ADD CONSTRAINT estudiante_id_carrera_fkey FOREIGN KEY (id_carrera) REFERENCES academico.carrera(id_carrera);


--
-- Name: estudiante estudiante_id_usuario_fkey; Type: FK CONSTRAINT; Schema: academico; Owner: -
--

ALTER TABLE ONLY academico.estudiante
    ADD CONSTRAINT estudiante_id_usuario_fkey FOREIGN KEY (id_usuario) REFERENCES seguridad.usuario(id_usuario);


--
-- Name: ayudante_catedra fkbu2mhu5x8oh65cujfyox6o8f8; Type: FK CONSTRAINT; Schema: academico; Owner: -
--

ALTER TABLE ONLY academico.ayudante_catedra
    ADD CONSTRAINT fkbu2mhu5x8oh65cujfyox6o8f8 FOREIGN KEY (id_usuario) REFERENCES seguridad.usuario(id_usuario);


--
-- Name: ayudante_catedra ayudante_catedra_id_usuario_fkey; Type: FK CONSTRAINT; Schema: ayudantia; Owner: -
--

ALTER TABLE ONLY ayudantia.ayudante_catedra
    ADD CONSTRAINT ayudante_catedra_id_usuario_fkey FOREIGN KEY (id_usuario) REFERENCES seguridad.usuario(id_usuario);


--
-- Name: ayudantia ayudantia_id_tipo_estado_ayudantia_fkey; Type: FK CONSTRAINT; Schema: ayudantia; Owner: -
--

ALTER TABLE ONLY ayudantia.ayudantia
    ADD CONSTRAINT ayudantia_id_tipo_estado_ayudantia_fkey FOREIGN KEY (id_tipo_estado_ayudantia) REFERENCES ayudantia.tipo_estado_ayudantia(id_tipo_estado_ayudantia);


--
-- Name: certificado certificado_id_ayudantia_fkey; Type: FK CONSTRAINT; Schema: ayudantia; Owner: -
--

ALTER TABLE ONLY ayudantia.certificado
    ADD CONSTRAINT certificado_id_ayudantia_fkey FOREIGN KEY (id_ayudantia) REFERENCES ayudantia.ayudantia(id_ayudantia);


--
-- Name: certificado certificado_id_usuario_fkey; Type: FK CONSTRAINT; Schema: ayudantia; Owner: -
--

ALTER TABLE ONLY ayudantia.certificado
    ADD CONSTRAINT certificado_id_usuario_fkey FOREIGN KEY (id_usuario) REFERENCES seguridad.usuario(id_usuario);


--
-- Name: detalle_asistencia_actividad detalle_asistencia_actividad_id_participante_ayudantia_fkey; Type: FK CONSTRAINT; Schema: ayudantia; Owner: -
--

ALTER TABLE ONLY ayudantia.detalle_asistencia_actividad
    ADD CONSTRAINT detalle_asistencia_actividad_id_participante_ayudantia_fkey FOREIGN KEY (id_participante_ayudantia) REFERENCES ayudantia.participante_ayudantia(id_participante_ayudantia);


--
-- Name: detalle_asistencia_actividad detalle_asistencia_actividad_id_registro_actividad_fkey; Type: FK CONSTRAINT; Schema: ayudantia; Owner: -
--

ALTER TABLE ONLY ayudantia.detalle_asistencia_actividad
    ADD CONSTRAINT detalle_asistencia_actividad_id_registro_actividad_fkey FOREIGN KEY (id_registro_actividad) REFERENCES ayudantia.registro_actividad(id_registro_actividad) ON DELETE CASCADE;


--
-- Name: documento_academico documento_academico_id_carrera_fkey; Type: FK CONSTRAINT; Schema: ayudantia; Owner: -
--

ALTER TABLE ONLY ayudantia.documento_academico
    ADD CONSTRAINT documento_academico_id_carrera_fkey FOREIGN KEY (id_carrera) REFERENCES academico.carrera(id_carrera);


--
-- Name: documento_academico documento_academico_id_facultad_fkey; Type: FK CONSTRAINT; Schema: ayudantia; Owner: -
--

ALTER TABLE ONLY ayudantia.documento_academico
    ADD CONSTRAINT documento_academico_id_facultad_fkey FOREIGN KEY (id_facultad) REFERENCES academico.facultad(id_facultad);


--
-- Name: documento_academico documento_academico_id_periodo_fkey; Type: FK CONSTRAINT; Schema: ayudantia; Owner: -
--

ALTER TABLE ONLY ayudantia.documento_academico
    ADD CONSTRAINT documento_academico_id_periodo_fkey FOREIGN KEY (id_periodo) REFERENCES academico.periodo_academico(id_periodo_academico);


--
-- Name: documento_academico documento_academico_id_tipo_documento_fkey; Type: FK CONSTRAINT; Schema: ayudantia; Owner: -
--

ALTER TABLE ONLY ayudantia.documento_academico
    ADD CONSTRAINT documento_academico_id_tipo_documento_fkey FOREIGN KEY (id_tipo_documento) REFERENCES ayudantia.tipo_documento(id_tipo_documento);


--
-- Name: documento_academico documento_academico_id_usuario_sube_fkey; Type: FK CONSTRAINT; Schema: ayudantia; Owner: -
--

ALTER TABLE ONLY ayudantia.documento_academico
    ADD CONSTRAINT documento_academico_id_usuario_sube_fkey FOREIGN KEY (id_usuario_sube) REFERENCES seguridad.usuario(id_usuario);


--
-- Name: evidencia_registro_actividad evidencia_registro_actividad_id_registro_actividad_fkey; Type: FK CONSTRAINT; Schema: ayudantia; Owner: -
--

ALTER TABLE ONLY ayudantia.evidencia_registro_actividad
    ADD CONSTRAINT evidencia_registro_actividad_id_registro_actividad_fkey FOREIGN KEY (id_registro_actividad) REFERENCES ayudantia.registro_actividad(id_registro_actividad);


--
-- Name: evidencia_registro_actividad evidencia_registro_actividad_id_tipo_estado_evidencia_fkey; Type: FK CONSTRAINT; Schema: ayudantia; Owner: -
--

ALTER TABLE ONLY ayudantia.evidencia_registro_actividad
    ADD CONSTRAINT evidencia_registro_actividad_id_tipo_estado_evidencia_fkey FOREIGN KEY (id_tipo_estado_evidencia) REFERENCES ayudantia.tipo_estado_evidencia(id_tipo_estado_evidencia);


--
-- Name: evidencia_registro_actividad evidencia_registro_actividad_id_tipo_evidencia_fkey; Type: FK CONSTRAINT; Schema: ayudantia; Owner: -
--

ALTER TABLE ONLY ayudantia.evidencia_registro_actividad
    ADD CONSTRAINT evidencia_registro_actividad_id_tipo_evidencia_fkey FOREIGN KEY (id_tipo_evidencia) REFERENCES ayudantia.tipo_evidencia(id_tipo_evidencia);


--
-- Name: ayudantia fktenwg9alao08j40w2y1pcyjqc; Type: FK CONSTRAINT; Schema: ayudantia; Owner: -
--

ALTER TABLE ONLY ayudantia.ayudantia
    ADD CONSTRAINT fktenwg9alao08j40w2y1pcyjqc FOREIGN KEY (id_postulacion) REFERENCES postulacion.postulacion(id_postulacion);


--
-- Name: participante_ayudantia participante_ayudantia_id_ayudantia_fkey; Type: FK CONSTRAINT; Schema: ayudantia; Owner: -
--

ALTER TABLE ONLY ayudantia.participante_ayudantia
    ADD CONSTRAINT participante_ayudantia_id_ayudantia_fkey FOREIGN KEY (id_ayudantia) REFERENCES ayudantia.ayudantia(id_ayudantia);


--
-- Name: registro_actividad registro_actividad_id_ayudantia_fkey; Type: FK CONSTRAINT; Schema: ayudantia; Owner: -
--

ALTER TABLE ONLY ayudantia.registro_actividad
    ADD CONSTRAINT registro_actividad_id_ayudantia_fkey FOREIGN KEY (id_ayudantia) REFERENCES ayudantia.ayudantia(id_ayudantia);


--
-- Name: registro_actividad registro_actividad_id_tipo_estado_registro_fkey; Type: FK CONSTRAINT; Schema: ayudantia; Owner: -
--

ALTER TABLE ONLY ayudantia.registro_actividad
    ADD CONSTRAINT registro_actividad_id_tipo_estado_registro_fkey FOREIGN KEY (id_tipo_estado_registro) REFERENCES ayudantia.tipo_estado_registro(id_tipo_estado_registro);


--
-- Name: sancion_ayudante_catedra sancion_ayudante_catedra_id_ayudante_catedra_fkey; Type: FK CONSTRAINT; Schema: ayudantia; Owner: -
--

ALTER TABLE ONLY ayudantia.sancion_ayudante_catedra
    ADD CONSTRAINT sancion_ayudante_catedra_id_ayudante_catedra_fkey FOREIGN KEY (id_ayudante_catedra) REFERENCES ayudantia.ayudante_catedra(id_ayudante_catedra);


--
-- Name: sancion_ayudante_catedra sancion_ayudante_catedra_id_tipo_sancion_ayudante_catedra_fkey; Type: FK CONSTRAINT; Schema: ayudantia; Owner: -
--

ALTER TABLE ONLY ayudantia.sancion_ayudante_catedra
    ADD CONSTRAINT sancion_ayudante_catedra_id_tipo_sancion_ayudante_catedra_fkey FOREIGN KEY (id_tipo_sancion_ayudante_catedra) REFERENCES ayudantia.tipo_sancion_ayudante_catedra(id_tipo_sancion_ayudante_catedra);


--
-- Name: convocatoria convocatoria_id_asignatura_fkey; Type: FK CONSTRAINT; Schema: convocatoria; Owner: -
--

ALTER TABLE ONLY convocatoria.convocatoria
    ADD CONSTRAINT convocatoria_id_asignatura_fkey FOREIGN KEY (id_asignatura) REFERENCES academico.asignatura(id_asignatura);


--
-- Name: convocatoria convocatoria_id_docente_fkey; Type: FK CONSTRAINT; Schema: convocatoria; Owner: -
--

ALTER TABLE ONLY convocatoria.convocatoria
    ADD CONSTRAINT convocatoria_id_docente_fkey FOREIGN KEY (id_docente) REFERENCES academico.docente(id_docente);


--
-- Name: convocatoria convocatoria_id_periodo_academico_fkey; Type: FK CONSTRAINT; Schema: convocatoria; Owner: -
--

ALTER TABLE ONLY convocatoria.convocatoria
    ADD CONSTRAINT convocatoria_id_periodo_academico_fkey FOREIGN KEY (id_periodo_academico) REFERENCES academico.periodo_academico(id_periodo_academico);


--
-- Name: periodo_academico_requisito_postulacion periodo_academico_requisito_p_id_tipo_requisito_postulacio_fkey; Type: FK CONSTRAINT; Schema: convocatoria; Owner: -
--

ALTER TABLE ONLY convocatoria.periodo_academico_requisito_postulacion
    ADD CONSTRAINT periodo_academico_requisito_p_id_tipo_requisito_postulacio_fkey FOREIGN KEY (id_tipo_requisito_postulacion) REFERENCES convocatoria.tipo_requisito_postulacion(id_tipo_requisito_postulacion);


--
-- Name: periodo_academico_requisito_postulacion periodo_academico_requisito_postulaci_id_periodo_academico_fkey; Type: FK CONSTRAINT; Schema: convocatoria; Owner: -
--

ALTER TABLE ONLY convocatoria.periodo_academico_requisito_postulacion
    ADD CONSTRAINT periodo_academico_requisito_postulaci_id_periodo_academico_fkey FOREIGN KEY (id_periodo_academico) REFERENCES academico.periodo_academico(id_periodo_academico);


--
-- Name: notificacion_ws fk_notificacion_usuario; Type: FK CONSTRAINT; Schema: notificacion; Owner: -
--

ALTER TABLE ONLY notificacion.notificacion_ws
    ADD CONSTRAINT fk_notificacion_usuario FOREIGN KEY (id_usuario) REFERENCES seguridad.usuario(id_usuario) ON DELETE CASCADE;


--
-- Name: notificacion fkcl7wnfwly3cree0u7ghsy10tu; Type: FK CONSTRAINT; Schema: notificacion; Owner: -
--

ALTER TABLE ONLY notificacion.notificacion
    ADD CONSTRAINT fkcl7wnfwly3cree0u7ghsy10tu FOREIGN KEY (id_usuario) REFERENCES seguridad.usuario(id_usuario) ON DELETE CASCADE;


--
-- Name: notificacion fknusx12rr54u3hkwbdiy0lue3c; Type: FK CONSTRAINT; Schema: notificacion; Owner: -
--

ALTER TABLE ONLY notificacion.notificacion
    ADD CONSTRAINT fknusx12rr54u3hkwbdiy0lue3c FOREIGN KEY (id_usuario_destino) REFERENCES seguridad.usuario(id_usuario);


--
-- Name: log_auditoria log_auditoria_id_tipo_rol_fkey; Type: FK CONSTRAINT; Schema: notificacion; Owner: -
--

ALTER TABLE ONLY notificacion.log_auditoria
    ADD CONSTRAINT log_auditoria_id_tipo_rol_fkey FOREIGN KEY (id_tipo_rol) REFERENCES seguridad.tipo_rol(id_tipo_rol);


--
-- Name: log_auditoria log_auditoria_id_usuario_fkey; Type: FK CONSTRAINT; Schema: notificacion; Owner: -
--

ALTER TABLE ONLY notificacion.log_auditoria
    ADD CONSTRAINT log_auditoria_id_usuario_fkey FOREIGN KEY (id_usuario) REFERENCES seguridad.usuario(id_usuario);


--
-- Name: notificacion notificacion_id_convocatoria_fkey; Type: FK CONSTRAINT; Schema: notificacion; Owner: -
--

ALTER TABLE ONLY notificacion.notificacion
    ADD CONSTRAINT notificacion_id_convocatoria_fkey FOREIGN KEY (id_convocatoria) REFERENCES convocatoria.convocatoria(id_convocatoria);


--
-- Name: periodo_fase periodo_fase_id_periodo_academico_fkey; Type: FK CONSTRAINT; Schema: planificacion; Owner: -
--

ALTER TABLE ONLY planificacion.periodo_fase
    ADD CONSTRAINT periodo_fase_id_periodo_academico_fkey FOREIGN KEY (id_periodo_academico) REFERENCES academico.periodo_academico(id_periodo_academico) ON DELETE RESTRICT;


--
-- Name: periodo_fase periodo_fase_id_tipo_fase_fkey; Type: FK CONSTRAINT; Schema: planificacion; Owner: -
--

ALTER TABLE ONLY planificacion.periodo_fase
    ADD CONSTRAINT periodo_fase_id_tipo_fase_fkey FOREIGN KEY (id_tipo_fase) REFERENCES planificacion.tipo_fase(id_tipo_fase) ON DELETE RESTRICT;


--
-- Name: banco_temas banco_temas_id_convocatoria_fkey; Type: FK CONSTRAINT; Schema: postulacion; Owner: -
--

ALTER TABLE ONLY postulacion.banco_temas
    ADD CONSTRAINT banco_temas_id_convocatoria_fkey FOREIGN KEY (id_convocatoria) REFERENCES convocatoria.convocatoria(id_convocatoria);


--
-- Name: comision_seleccion comision_seleccion_id_convocatoria_fkey; Type: FK CONSTRAINT; Schema: postulacion; Owner: -
--

ALTER TABLE ONLY postulacion.comision_seleccion
    ADD CONSTRAINT comision_seleccion_id_convocatoria_fkey FOREIGN KEY (id_convocatoria) REFERENCES convocatoria.convocatoria(id_convocatoria);


--
-- Name: configuracion_oposicion configuracion_oposicion_id_convocatoria_fkey; Type: FK CONSTRAINT; Schema: postulacion; Owner: -
--

ALTER TABLE ONLY postulacion.configuracion_oposicion
    ADD CONSTRAINT configuracion_oposicion_id_convocatoria_fkey FOREIGN KEY (id_convocatoria) REFERENCES convocatoria.convocatoria(id_convocatoria) ON DELETE CASCADE;


--
-- Name: evaluacion_meritos evaluacion_meritos_id_postulacion_fkey; Type: FK CONSTRAINT; Schema: postulacion; Owner: -
--

ALTER TABLE ONLY postulacion.evaluacion_meritos
    ADD CONSTRAINT evaluacion_meritos_id_postulacion_fkey FOREIGN KEY (id_postulacion) REFERENCES postulacion.postulacion(id_postulacion);


--
-- Name: evaluacion_meritos evaluacion_meritos_id_tipo_estado_evaluacion_fkey; Type: FK CONSTRAINT; Schema: postulacion; Owner: -
--

ALTER TABLE ONLY postulacion.evaluacion_meritos
    ADD CONSTRAINT evaluacion_meritos_id_tipo_estado_evaluacion_fkey FOREIGN KEY (id_tipo_estado_evaluacion) REFERENCES postulacion.tipo_estado_evaluacion(id_tipo_estado_evaluacion);


--
-- Name: evaluacion_oposicion evaluacion_oposicion_id_postulacion_fkey; Type: FK CONSTRAINT; Schema: postulacion; Owner: -
--

ALTER TABLE ONLY postulacion.evaluacion_oposicion
    ADD CONSTRAINT evaluacion_oposicion_id_postulacion_fkey FOREIGN KEY (id_postulacion) REFERENCES postulacion.postulacion(id_postulacion);


--
-- Name: evaluacion_oposicion evaluacion_oposicion_id_tipo_estado_evaluacion_fkey; Type: FK CONSTRAINT; Schema: postulacion; Owner: -
--

ALTER TABLE ONLY postulacion.evaluacion_oposicion
    ADD CONSTRAINT evaluacion_oposicion_id_tipo_estado_evaluacion_fkey FOREIGN KEY (id_tipo_estado_evaluacion) REFERENCES postulacion.tipo_estado_evaluacion(id_tipo_estado_evaluacion);


--
-- Name: postulacion fk_postulacion_tipo_estado; Type: FK CONSTRAINT; Schema: postulacion; Owner: -
--

ALTER TABLE ONLY postulacion.postulacion
    ADD CONSTRAINT fk_postulacion_tipo_estado FOREIGN KEY (id_tipo_estado_postulacion) REFERENCES postulacion.tipo_estado_postulacion(id_tipo_estado_postulacion);


--
-- Name: postulacion postulacion_id_convocatoria_fkey; Type: FK CONSTRAINT; Schema: postulacion; Owner: -
--

ALTER TABLE ONLY postulacion.postulacion
    ADD CONSTRAINT postulacion_id_convocatoria_fkey FOREIGN KEY (id_convocatoria) REFERENCES convocatoria.convocatoria(id_convocatoria);


--
-- Name: postulacion postulacion_id_estudiante_fkey; Type: FK CONSTRAINT; Schema: postulacion; Owner: -
--

ALTER TABLE ONLY postulacion.postulacion
    ADD CONSTRAINT postulacion_id_estudiante_fkey FOREIGN KEY (id_estudiante) REFERENCES academico.estudiante(id_estudiante);


--
-- Name: requisito_adjunto requisito_adjunto_id_postulacion_fkey; Type: FK CONSTRAINT; Schema: postulacion; Owner: -
--

ALTER TABLE ONLY postulacion.requisito_adjunto
    ADD CONSTRAINT requisito_adjunto_id_postulacion_fkey FOREIGN KEY (id_postulacion) REFERENCES postulacion.postulacion(id_postulacion);


--
-- Name: requisito_adjunto requisito_adjunto_id_tipo_estado_requisito_fkey; Type: FK CONSTRAINT; Schema: postulacion; Owner: -
--

ALTER TABLE ONLY postulacion.requisito_adjunto
    ADD CONSTRAINT requisito_adjunto_id_tipo_estado_requisito_fkey FOREIGN KEY (id_tipo_estado_requisito) REFERENCES convocatoria.tipo_estado_requisito(id_tipo_estado_requisito);


--
-- Name: requisito_adjunto requisito_adjunto_id_tipo_requisito_postulacion_fkey; Type: FK CONSTRAINT; Schema: postulacion; Owner: -
--

ALTER TABLE ONLY postulacion.requisito_adjunto
    ADD CONSTRAINT requisito_adjunto_id_tipo_requisito_postulacion_fkey FOREIGN KEY (id_tipo_requisito_postulacion) REFERENCES convocatoria.tipo_requisito_postulacion(id_tipo_requisito_postulacion);


--
-- Name: tipo_objeto_seguridad_privilegio tipo_objeto_seguridad_privilegio_id_privilegio_fkey; Type: FK CONSTRAINT; Schema: seguridad; Owner: -
--

ALTER TABLE ONLY seguridad.tipo_objeto_seguridad_privilegio
    ADD CONSTRAINT tipo_objeto_seguridad_privilegio_id_privilegio_fkey FOREIGN KEY (id_privilegio) REFERENCES seguridad.privilegio(id_privilegio);


--
-- Name: tipo_objeto_seguridad_privilegio tipo_objeto_seguridad_privilegio_id_tipo_objeto_seguridad_fkey; Type: FK CONSTRAINT; Schema: seguridad; Owner: -
--

ALTER TABLE ONLY seguridad.tipo_objeto_seguridad_privilegio
    ADD CONSTRAINT tipo_objeto_seguridad_privilegio_id_tipo_objeto_seguridad_fkey FOREIGN KEY (id_tipo_objeto_seguridad) REFERENCES seguridad.tipo_objeto_seguridad(id_tipo_objeto_seguridad);


--
-- Name: tipo_rol tipo_rol_id_rol_bd_fkey; Type: FK CONSTRAINT; Schema: seguridad; Owner: -
--

ALTER TABLE ONLY seguridad.tipo_rol
    ADD CONSTRAINT tipo_rol_id_rol_bd_fkey FOREIGN KEY (id_rol_bd) REFERENCES seguridad.rol_bd(id_rol_bd);


--
-- Name: usuario_comision usuario_comision_id_comision_seleccion_fkey; Type: FK CONSTRAINT; Schema: seguridad; Owner: -
--

ALTER TABLE ONLY seguridad.usuario_comision
    ADD CONSTRAINT usuario_comision_id_comision_seleccion_fkey FOREIGN KEY (id_comision_seleccion) REFERENCES postulacion.comision_seleccion(id_comision_seleccion);


--
-- Name: usuario_comision usuario_comision_id_evaluacion_oposicion_fkey; Type: FK CONSTRAINT; Schema: seguridad; Owner: -
--

ALTER TABLE ONLY seguridad.usuario_comision
    ADD CONSTRAINT usuario_comision_id_evaluacion_oposicion_fkey FOREIGN KEY (id_evaluacion_oposicion) REFERENCES postulacion.evaluacion_oposicion(id_evaluacion_oposicion);


--
-- Name: usuario_comision usuario_comision_id_usuario_fkey; Type: FK CONSTRAINT; Schema: seguridad; Owner: -
--

ALTER TABLE ONLY seguridad.usuario_comision
    ADD CONSTRAINT usuario_comision_id_usuario_fkey FOREIGN KEY (id_usuario) REFERENCES seguridad.usuario(id_usuario);


--
-- Name: usuario_tipo_rol usuario_tipo_rol_id_tipo_rol_fkey; Type: FK CONSTRAINT; Schema: seguridad; Owner: -
--

ALTER TABLE ONLY seguridad.usuario_tipo_rol
    ADD CONSTRAINT usuario_tipo_rol_id_tipo_rol_fkey FOREIGN KEY (id_tipo_rol) REFERENCES seguridad.tipo_rol(id_tipo_rol);


--
-- Name: usuario_tipo_rol usuario_tipo_rol_id_usuario_fkey; Type: FK CONSTRAINT; Schema: seguridad; Owner: -
--

ALTER TABLE ONLY seguridad.usuario_tipo_rol
    ADD CONSTRAINT usuario_tipo_rol_id_usuario_fkey FOREIGN KEY (id_usuario) REFERENCES seguridad.usuario(id_usuario);


--
-- PostgreSQL database dump complete
--

\unrestrict sNdjeJKsTjJgK38zqK46ewOxMYuBQi2PboonzcBDR5DVHkMnEcDFtEZCQfZtKsb

