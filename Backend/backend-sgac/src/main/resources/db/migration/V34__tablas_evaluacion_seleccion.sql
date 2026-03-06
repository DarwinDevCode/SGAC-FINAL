-- ══════════════════════════════════════════════════════════════════
-- V34: Tablas del Módulo de Evaluación y Selección (schema postulacion)
-- ══════════════════════════════════════════════════════════════════

-- 1. Sorteo del tema de oposición por postulante
CREATE TABLE postulacion.sorteo_oposicion (
    id_sorteo            SERIAL PRIMARY KEY,
    id_postulacion       INTEGER NOT NULL REFERENCES postulacion.postulacion(id_postulacion),
    tema_sorteado        VARCHAR(500) NOT NULL,
    semilla_sorteo       BIGINT,            -- para auditoría/reproducibilidad
    fecha_sorteo         TIMESTAMP NOT NULL DEFAULT NOW(),
    notificado           BOOLEAN NOT NULL DEFAULT FALSE
);

-- 2. Calificación individual de cada miembro del tribunal en la oposición
CREATE TABLE postulacion.calificacion_oposicion_individual (
    id_calificacion      SERIAL PRIMARY KEY,
    id_postulacion       INTEGER NOT NULL REFERENCES postulacion.postulacion(id_postulacion),
    id_evaluador         INTEGER NOT NULL,  -- FK a seguridad.usuario (sin FK formal para evitar cross-schema)
    rol_evaluador        VARCHAR(20) NOT NULL CHECK (rol_evaluador IN ('DECANO','COORDINADOR','DOCENTE')),
    criterio_material    NUMERIC(5,2) NOT NULL CHECK (criterio_material BETWEEN 0 AND 10),
    criterio_calidad     NUMERIC(5,2) NOT NULL CHECK (criterio_calidad BETWEEN 0 AND 4),
    criterio_pertinencia NUMERIC(5,2) NOT NULL CHECK (criterio_pertinencia BETWEEN 0 AND 6),
    subtotal             NUMERIC(5,2) GENERATED ALWAYS AS (criterio_material + criterio_calidad + criterio_pertinencia) STORED,
    fecha_registro       TIMESTAMP NOT NULL DEFAULT NOW(),
    UNIQUE (id_postulacion, id_evaluador)
);

-- 3. Resumen final de evaluación por postulante
CREATE TABLE postulacion.resumen_evaluacion (
    id_resumen           SERIAL PRIMARY KEY,
    id_postulacion       INTEGER NOT NULL UNIQUE REFERENCES postulacion.postulacion(id_postulacion),
    total_meritos        NUMERIC(5,2),      -- copiado de evaluacion_meritos.total_meritos
    promedio_oposicion   NUMERIC(5,2),      -- promedio de los 3 subtotales individuales
    total_final          NUMERIC(5,2),      -- total_meritos + promedio_oposicion
    estado               VARCHAR(20) CHECK (estado IN ('GANADOR','APTO','NO_APTO','DESIERTO','PENDIENTE')),
    posicion             INTEGER,
    fecha_calculo        TIMESTAMP NOT NULL DEFAULT NOW()
);

-- 4. Actas de evaluación con estado de confirmación por miembro
CREATE TABLE postulacion.acta_evaluacion (
    id_acta              SERIAL PRIMARY KEY,
    id_postulacion       INTEGER NOT NULL REFERENCES postulacion.postulacion(id_postulacion),
    tipo_acta            VARCHAR(20) NOT NULL CHECK (tipo_acta IN ('MERITOS','OPOSICION')),
    url_documento        VARCHAR(500),
    fecha_generacion     TIMESTAMP,
    confirmado_decano    BOOLEAN NOT NULL DEFAULT FALSE,
    confirmado_coordinador BOOLEAN NOT NULL DEFAULT FALSE,
    confirmado_docente   BOOLEAN NOT NULL DEFAULT FALSE,
    estado               VARCHAR(20) NOT NULL DEFAULT 'PENDIENTE' CHECK (estado IN ('PENDIENTE','CONFIRMADO'))
);

-- ══════════════════════════════════════════════════════════════════
-- Stored Procedures para el módulo
-- ══════════════════════════════════════════════════════════════════

-- SP: Guardar nota de oposición individual y calcular promedio cuando los 3 estén listos
CREATE OR REPLACE FUNCTION public.sp_guardar_oposicion_individual(
    p_id_postulacion    INTEGER,
    p_id_evaluador      INTEGER,
    p_rol_evaluador     VARCHAR,
    p_material          NUMERIC,
    p_calidad           NUMERIC,
    p_pertinencia       NUMERIC
) RETURNS INTEGER AS $$
DECLARE
    v_id INTEGER;
    v_count INTEGER;
    v_avg_material NUMERIC;
    v_avg_calidad NUMERIC;
    v_avg_pertinencia NUMERIC;
    v_promedio NUMERIC;
    v_total_meritos NUMERIC;
    v_total_final NUMERIC;
BEGIN
    -- Insertar o actualizar la nota individual
    INSERT INTO postulacion.calificacion_oposicion_individual
        (id_postulacion, id_evaluador, rol_evaluador, criterio_material, criterio_calidad, criterio_pertinencia)
    VALUES (p_id_postulacion, p_id_evaluador, p_rol_evaluador, p_material, p_calidad, p_pertinencia)
    ON CONFLICT (id_postulacion, id_evaluador)
    DO UPDATE SET
        criterio_material    = EXCLUDED.criterio_material,
        criterio_calidad     = EXCLUDED.criterio_calidad,
        criterio_pertinencia = EXCLUDED.criterio_pertinencia,
        fecha_registro       = NOW()
    RETURNING id_calificacion INTO v_id;

    -- Verificar si ya hay 3 evaluaciones para esta postulación
    SELECT COUNT(*) INTO v_count
    FROM postulacion.calificacion_oposicion_individual
    WHERE id_postulacion = p_id_postulacion;

    -- Si los 3 miembros han calificado, calcular el resumen
    IF v_count >= 3 THEN
        SELECT AVG(subtotal) INTO v_promedio
        FROM postulacion.calificacion_oposicion_individual
        WHERE id_postulacion = p_id_postulacion;

        SELECT COALESCE(em.nota_asignatura, 0) + COALESCE(em.nota_semestres, 0) +
               COALESCE(em.nota_eventos, 0) + COALESCE(em.nota_experiencia, 0)
        INTO v_total_meritos
        FROM postulacion.evaluacion_meritos em
        WHERE em.id_postulacion = p_id_postulacion;

        v_total_final := COALESCE(v_total_meritos, 0) + COALESCE(v_promedio, 0);

        INSERT INTO postulacion.resumen_evaluacion
            (id_postulacion, total_meritos, promedio_oposicion, total_final, estado, fecha_calculo)
        VALUES (p_id_postulacion, v_total_meritos, ROUND(v_promedio, 2), ROUND(v_total_final, 2), 'PENDIENTE', NOW())
        ON CONFLICT (id_postulacion)
        DO UPDATE SET
            promedio_oposicion = ROUND(v_promedio, 2),
            total_final        = ROUND(v_total_final, 2),
            fecha_calculo      = NOW();
    END IF;

    RETURN COALESCE(v_id, -1);
END;
$$ LANGUAGE plpgsql;

-- SP: Calcular ranking y estados de una convocatoria completa
CREATE OR REPLACE FUNCTION public.sp_calcular_ranking_evaluacion(p_id_convocatoria INTEGER)
RETURNS TABLE (
    id_postulacion      INTEGER,
    nombre_estudiante   TEXT,
    matricula           TEXT,
    total_meritos       NUMERIC,
    promedio_oposicion  NUMERIC,
    total_final         NUMERIC,
    mat_sum             NUMERIC,
    pertinencia_sum     NUMERIC,
    estado              VARCHAR,
    posicion            BIGINT
) AS $$
DECLARE
    v_hay_ganador BOOLEAN := FALSE;
    v_min_puntaje NUMERIC := 25;
BEGIN
    -- Verificar si hay algún postulante con puntaje >= 25
    SELECT EXISTS (
        SELECT 1
        FROM postulacion.resumen_evaluacion re
        JOIN postulacion.postulacion p ON p.id_postulacion = re.id_postulacion
        WHERE p.id_convocatoria = p_id_convocatoria AND re.total_final >= v_min_puntaje
    ) INTO v_hay_ganador;

    RETURN QUERY
    WITH ranked AS (
        SELECT
            re.id_postulacion,
            (u.nombres || ' ' || u.apellidos)::TEXT     AS nombre_estudiante,
            est.matricula::TEXT,
            re.total_meritos,
            re.promedio_oposicion,
            re.total_final,
            COALESCE(SUM(coi.criterio_material), 0)     AS mat_sum,
            COALESCE(SUM(coi.criterio_pertinencia), 0)  AS pertinencia_sum,
            ROW_NUMBER() OVER (
                ORDER BY re.total_final DESC,
                         SUM(coi.criterio_material) DESC,
                         SUM(coi.criterio_pertinencia) DESC
            ) AS posicion
        FROM postulacion.resumen_evaluacion re
        JOIN postulacion.postulacion po ON po.id_postulacion = re.id_postulacion
        JOIN academico.estudiante est   ON est.id_estudiante = po.id_estudiante
        JOIN seguridad.usuario u        ON u.id_usuario = est.id_usuario
        LEFT JOIN postulacion.calificacion_oposicion_individual coi
               ON coi.id_postulacion = re.id_postulacion
        WHERE po.id_convocatoria = p_id_convocatoria
        GROUP BY re.id_postulacion, u.nombres, u.apellidos, est.matricula,
                 re.total_meritos, re.promedio_oposicion, re.total_final
    )
    SELECT
        r.id_postulacion,
        r.nombre_estudiante,
        r.matricula,
        r.total_meritos,
        r.promedio_oposicion,
        r.total_final,
        r.mat_sum,
        r.pertinencia_sum,
        CASE
            WHEN NOT v_hay_ganador THEN 'DESIERTO'::VARCHAR
            WHEN r.total_final < v_min_puntaje THEN 'NO_APTO'::VARCHAR
            WHEN r.posicion = 1 AND r.total_final >= v_min_puntaje THEN 'GANADOR'::VARCHAR
            ELSE 'APTO'::VARCHAR
        END AS estado,
        r.posicion
    FROM ranked r
    ORDER BY r.posicion;
END;
$$ LANGUAGE plpgsql;

-- SP: Confirmar acta por un miembro del tribunal
CREATE OR REPLACE FUNCTION public.sp_confirmar_acta(
    p_id_acta       INTEGER,
    p_id_evaluador  INTEGER,
    p_rol           VARCHAR
) RETURNS INTEGER AS $$
BEGIN
    IF p_rol = 'DECANO' THEN
        UPDATE postulacion.acta_evaluacion SET confirmado_decano = TRUE WHERE id_acta = p_id_acta;
    ELSIF p_rol = 'COORDINADOR' THEN
        UPDATE postulacion.acta_evaluacion SET confirmado_coordinador = TRUE WHERE id_acta = p_id_acta;
    ELSIF p_rol = 'DOCENTE' THEN
        UPDATE postulacion.acta_evaluacion SET confirmado_docente = TRUE WHERE id_acta = p_id_acta;
    END IF;

    -- Si los 3 confirmaron, marcar como CONFIRMADO
    UPDATE postulacion.acta_evaluacion
    SET estado = 'CONFIRMADO'
    WHERE id_acta = p_id_acta
      AND confirmado_decano = TRUE
      AND confirmado_coordinador = TRUE
      AND confirmado_docente = TRUE;

    RETURN p_id_acta;
END;
$$ LANGUAGE plpgsql;
