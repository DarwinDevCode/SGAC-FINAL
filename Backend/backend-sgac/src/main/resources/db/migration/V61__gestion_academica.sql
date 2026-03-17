-- ================================================================
--  MÓDULO: Gestión de Carga Académica Global — CORRECCIÓN
--  Problemas resueltos:
--    1. fn_listar_docentes_activos: COUNT anidado en jsonb_agg
--       → Resuelto con CTE que precalcula el conteo.
--    2. fn_gestionar_carga_docente: captura de nombres revocados
--       post-UPDATE inconsistente
--       → Resuelto con CTE RETURNING para capturar antes de perder
--         el estado anterior.
-- ================================================================


-- ────────────────────────────────────────────────────────────────
-- 1. fn_listar_docentes_activos  (CORREGIDA)
--    El COUNT se calcula en una CTE antes de llamar a jsonb_agg,
--    evitando el anidamiento de funciones de agregación.
-- ────────────────────────────────────────────────────────────────
CREATE OR REPLACE FUNCTION academico.fn_listar_docentes_activos()
    RETURNS JSONB
    LANGUAGE sql
    STABLE
    SECURITY DEFINER
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

COMMENT ON FUNCTION academico.fn_listar_docentes_activos() IS
    'Lista todos los docentes activos (con rol DOCENTE activo) '
        'junto con el conteo de asignaturas actualmente asignadas.';


-- ────────────────────────────────────────────────────────────────
-- 2. fn_listar_jerarquia_asignaturas  (sin cambios)
-- ────────────────────────────────────────────────────────────────
CREATE OR REPLACE FUNCTION academico.fn_listar_jerarquia_asignaturas()
    RETURNS JSONB
    LANGUAGE sql
    STABLE
    SECURITY DEFINER
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

COMMENT ON FUNCTION academico.fn_listar_jerarquia_asignaturas() IS
    'Catálogo global de asignaturas con su jerarquía completa '
        '(Asignatura · Carrera · Facultad) para el buscador global.';


-- ────────────────────────────────────────────────────────────────
-- 3. fn_gestionar_carga_docente  (CORREGIDA)
--
--    Problema anterior: la SELECT que capturaba los nombres
--    revocados se ejecutaba DESPUÉS del UPDATE con activo = FALSE,
--    usando exactamente la misma condición negativa que el UPDATE.
--    Esto podía devolver filas que ya estaban inactivas antes de
--    la operación (falsos positivos) o ninguna fila si el UPDATE
--    ya se había confirmado de forma distinta al esperado.
--
--    Solución: usar una CTE con la cláusula RETURNING en el propio
--    UPDATE para capturar atómicamente los IDs afectados, y luego
--    resolver los nombres en una segunda CTE sobre ese resultado.
-- ────────────────────────────────────────────────────────────────
CREATE OR REPLACE FUNCTION academico.fn_gestionar_carga_docente(
    p_id_docente      INTEGER,
    p_asignaturas_ids INTEGER[]
)
    RETURNS JSONB
    LANGUAGE plpgsql
    SECURITY DEFINER
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
-- Índice único requerido por el ON CONFLICT del upsert
CREATE UNIQUE INDEX IF NOT EXISTS uq_docente_asignatura
    ON academico.docente_asignatura (id_docente, id_asignatura);

COMMENT ON FUNCTION academico.fn_gestionar_carga_docente(INTEGER, INTEGER[]) IS
    'Sincronización atómica de carga académica. '
        'Recibe el estado FINAL deseado y aplica revocaciones + asignaciones en una sola TX.';