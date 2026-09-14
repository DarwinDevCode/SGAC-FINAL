-- V67__fn_asistencia_dinamica.sql
-- ─────────────────────────────────────────────────────────────────────────────
-- Módulo: Asistencia Dinámica de Ayudantías
--
-- Nuevas tablas:
--   ayudantia.participante_ayudantia          → padrón de estudiantes
--   ayudantia.detalle_asistencia_actividad    → asistencia por actividad
--
-- Funciones:
--   fn_consultar_participantes(idAyudantia)
--   fn_cargar_participantes_masivo(idAyudantia, participantes JSONB)
--   fn_inicializar_asistencia(idRegistro)
--   fn_guardar_asistencias(idRegistro, asistencias JSONB)
--   fn_consultar_asistencia(idRegistro)
-- ─────────────────────────────────────────────────────────────────────────────

-- ══ 1. Tablas de soporte ═════════════════════════════════════════════════════

CREATE TABLE IF NOT EXISTS ayudantia.participante_ayudantia (
                                                                id_participante_ayudantia INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                                                                id_ayudantia              INTEGER NOT NULL
                                                                    REFERENCES ayudantia.ayudantia(id_ayudantia),
                                                                nombre_completo           VARCHAR(255) NOT NULL,
                                                                curso                     VARCHAR(100),
                                                                paralelo                  VARCHAR(20),
                                                                activo                    BOOLEAN DEFAULT TRUE,
                                                                CONSTRAINT uq_participante_curso_ayudantia
                                                                    UNIQUE (id_ayudantia, nombre_completo, curso, paralelo)
);

CREATE INDEX IF NOT EXISTS idx_participantes_por_ayudantia
    ON ayudantia.participante_ayudantia (id_ayudantia);

CREATE TABLE IF NOT EXISTS ayudantia.detalle_asistencia_actividad (
                                                                      id_detalle_asistencia_actividad INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                                                                      id_registro_actividad           INTEGER NOT NULL
                                                                          REFERENCES ayudantia.registro_actividad(id_registro_actividad) ON DELETE CASCADE,
                                                                      id_participante_ayudantia       INTEGER NOT NULL
                                                                          REFERENCES ayudantia.participante_ayudantia(id_participante_ayudantia),
                                                                      asistio                         BOOLEAN DEFAULT FALSE,
                                                                      CONSTRAINT uq_asistencia_por_actividad
                                                                          UNIQUE (id_registro_actividad, id_participante_ayudantia)
);

CREATE INDEX IF NOT EXISTS idx_asistencia_por_registro
    ON ayudantia.detalle_asistencia_actividad (id_registro_actividad);


-- ══ 2. fn_consultar_participantes ════════════════════════════════════════════
--
-- Devuelve la lista de participantes activos de una ayudantía,
-- ordenados alfabéticamente.
-- ─────────────────────────────────────────────────────────────────────────────
CREATE OR REPLACE FUNCTION ayudantia.fn_consultar_participantes(
    p_id_ayudantia INTEGER
)
    RETURNS JSONB
    LANGUAGE plpgsql
    SECURITY DEFINER
    STABLE
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


-- ══ 3. fn_cargar_participantes_masivo ════════════════════════════════════════
--
-- Carga masiva de participantes con control de duplicados.
--
-- p_participantes: [{nombreCompleto, curso, paralelo}, ...]
--
-- Retorna: { exito, mensaje, insertados, duplicados }
-- ─────────────────────────────────────────────────────────────────────────────
CREATE OR REPLACE FUNCTION ayudantia.fn_cargar_participantes_masivo(
    p_id_ayudantia  INTEGER,
    p_participantes JSONB
)
    RETURNS JSONB
    LANGUAGE plpgsql
    SECURITY DEFINER
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


-- ══ 4. fn_inicializar_asistencia ═════════════════════════════════════════════
--
-- Crea un registro de detalle_asistencia_actividad (asistio = FALSE) por
-- cada participante activo de la ayudantía asociada al registro de actividad.
-- Si ya existen registros (ON CONFLICT DO NOTHING), no genera duplicados.
--
-- Retorna: { exito, mensaje, participantesRegistrados }
-- ─────────────────────────────────────────────────────────────────────────────
CREATE OR REPLACE FUNCTION ayudantia.fn_inicializar_asistencia(
    p_id_registro INTEGER
)
    RETURNS JSONB
    LANGUAGE plpgsql
    SECURITY DEFINER
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


-- ══ 5. fn_guardar_asistencias ════════════════════════════════════════════════
--
-- Actualiza los flags asistio en detalle_asistencia_actividad y actualiza
-- el contador numero_asistentes en registro_actividad.
--
-- p_asistencias: [{idParticipante: N, asistio: true/false}, ...]
--
-- Retorna: { exito, mensaje, presentes, total }
-- ─────────────────────────────────────────────────────────────────────────────
CREATE OR REPLACE FUNCTION ayudantia.fn_guardar_asistencias(
    p_id_registro  INTEGER,
    p_asistencias  JSONB
)
    RETURNS JSONB
    LANGUAGE plpgsql
    SECURITY DEFINER
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

    -- ── Actualizar contador en el registro de actividad ──────────────────
    UPDATE ayudantia.registro_actividad
    SET numero_asistentes = v_presentes
    WHERE id_registro_actividad = p_id_registro;

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


-- ══ 6. fn_consultar_asistencia ═══════════════════════════════════════════════
--
-- Devuelve el detalle de asistencia de un registro de actividad,
-- incluyendo los datos del participante.
-- ─────────────────────────────────────────────────────────────────────────────
CREATE OR REPLACE FUNCTION ayudantia.fn_consultar_asistencia(
    p_id_registro INTEGER
)
    RETURNS JSONB
    LANGUAGE plpgsql
    SECURITY DEFINER
    STABLE
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


GRANT EXECUTE ON FUNCTION ayudantia.fn_consultar_participantes(INTEGER)
    TO role_ayudante_catedra;

GRANT EXECUTE ON FUNCTION ayudantia.fn_cargar_participantes_masivo(INTEGER, JSONB)
    TO role_ayudante_catedra;

GRANT EXECUTE ON FUNCTION ayudantia.fn_inicializar_asistencia(INTEGER)
    TO role_ayudante_catedra;

GRANT EXECUTE ON FUNCTION ayudantia.fn_guardar_asistencias(INTEGER, JSONB)
    TO role_ayudante_catedra;

GRANT EXECUTE ON FUNCTION ayudantia.fn_consultar_asistencia(INTEGER)
    TO role_ayudante_catedra;

-- Permisos sobre las tablas nuevas
GRANT SELECT, INSERT, UPDATE ON ayudantia.participante_ayudantia         TO role_ayudante_catedra;
GRANT SELECT, INSERT, UPDATE ON ayudantia.detalle_asistencia_actividad   TO role_ayudante_catedra;
GRANT USAGE, SELECT ON SEQUENCE ayudantia.participante_ayudantia_id_participante_ayudantia_seq
    TO role_ayudante_catedra;
