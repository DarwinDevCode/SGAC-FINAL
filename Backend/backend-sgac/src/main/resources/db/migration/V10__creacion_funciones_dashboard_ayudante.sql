
-- Horas de ayudantía: Funciones para el dashboard del ayudante
CREATE OR REPLACE FUNCTION ayudantia.fn_total_horas_ayudante(p_id_ayudantia INTEGER)
RETURNS TABLE (
    id_ayudantia        INTEGER,
    total_horas         NUMERIC(6,2)
)
LANGUAGE plpgsql AS $$
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


-- Horas validadas (aprobadas por docente/coordinador)
CREATE OR REPLACE FUNCTION ayudantia.fn_horas_validadas(p_id_ayudantia INTEGER)
RETURNS TABLE (
    id_ayudantia        INTEGER,
    horas_aprobadas     NUMERIC(6,2)
)
LANGUAGE plpgsql AS $$
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


-- Horas pendientes de revisión o con observación
CREATE OR REPLACE FUNCTION ayudantia.fn_horas_pendientes_observadas(p_id_ayudantia INTEGER)
RETURNS TABLE (
    id_ayudantia        INTEGER,
    horas_pendientes    NUMERIC(6,2),
    horas_observadas    NUMERIC(6,2)
)
LANGUAGE plpgsql AS $$
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


-- FUNCIÓN 4: Progreso actual (% aprobado sobre el total registrado)
CREATE OR REPLACE FUNCTION ayudantia.fn_progreso_ayudantia(p_id_ayudantia INTEGER)
    RETURNS TABLE (
                      horas_aprobadas          NUMERIC(6,2),
                      horas_totales_esperadas  NUMERIC(6,2),
                      porcentaje_avance_global NUMERIC(5,2),

                      semana_inicio            DATE,
                      semana_fin               DATE,
                      horas_semana_actual      NUMERIC(6,2),
                      horas_disponibles_semana NUMERIC(6,2),
                      supera_limite_semanal    BOOLEAN
                  )
    LANGUAGE plpgsql AS $$
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



-- FUNCIÓN 5: Últimas 5 actividades registradas
CREATE OR REPLACE FUNCTION ayudantia.fn_ultimas_actividades(p_id_ayudantia INTEGER)
RETURNS TABLE (
    id_registro         INTEGER,
    fecha               DATE,
    tema_tratado        TEXT,
    horas_dedicadas     NUMERIC(5,2),
    estado_revision     VARCHAR(30)
)
LANGUAGE plpgsql AS $$
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


-- FUNCIÓN 6: Alertas de observaciones (evidencias rechazadas/observadas)
CREATE OR REPLACE FUNCTION ayudantia.fn_alertas_observaciones(p_id_ayudantia INTEGER)
RETURNS TABLE (
    id_registro         INTEGER,
    fecha               DATE,
    tema_tratado        TEXT,
    estado_revision     VARCHAR(30),
    descripcion         TEXT
)
LANGUAGE plpgsql AS $$
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


-- FUNCIÓN 7 + 8 + 9: Información general de la ayudantía
-- (asignatura, docente tutor, periodo académico)
CREATE OR REPLACE FUNCTION ayudantia.fn_info_general_ayudantia(p_id_ayudantia INTEGER)
RETURNS TABLE (
    id_ayudantia        INTEGER,
    nombre_asignatura   VARCHAR(150),
    semestre            INTEGER,
    nombre_docente      TEXT,
    correo_docente      VARCHAR(150),
    nombre_periodo      VARCHAR(100),
    fecha_inicio_periodo DATE,
    fecha_fin_periodo   DATE,
    estado_periodo      VARCHAR(30)
)
LANGUAGE plpgsql AS $$
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


-- FUNCIÓN 10: Fecha de cierre del mes
-- NOTA: Requiere columna dia_cierre_informe en periodo_academico.
-- Mientras tanto, se usa el último día del mes en curso.
-- ============================================================
-- CREATE OR REPLACE FUNCTION ayudantia.fn_fecha_cierre_mes(p_id_ayudantia INTEGER)
-- RETURNS TABLE (
--     mes_actual          TEXT,
--     fecha_cierre        DATE,
--     dias_restantes      INTEGER
-- )
-- LANGUAGE plpgsql AS $$
-- BEGIN
--     RETURN QUERY
--     SELECT
--         TO_CHAR(CURRENT_DATE, 'Month YYYY')                             AS mes_actual,
--         (DATE_TRUNC('month', CURRENT_DATE) + INTERVAL '1 month - 1 day')::DATE AS fecha_cierre,
--         ((DATE_TRUNC('month', CURRENT_DATE) + INTERVAL '1 month - 1 day')::DATE
--             - CURRENT_DATE)                                             AS dias_restantes;
-- END;
-- $$;


-- FUNCIÓN 11: Validación de horas en la semana actual (límite 20h)
CREATE OR REPLACE FUNCTION ayudantia.fn_validar_limite_semanal(p_id_ayudantia INTEGER)
RETURNS TABLE (
    semana_inicio       DATE,
    semana_fin          DATE,
    horas_semana_actual NUMERIC(6,2),
    horas_disponibles   NUMERIC(6,2),
    supera_limite       BOOLEAN
)
LANGUAGE plpgsql AS $$
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


