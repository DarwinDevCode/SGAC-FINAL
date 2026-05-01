CREATE OR REPLACE FUNCTION convocatoria.fn_verificar_restricciones_fase()
    RETURNS JSONB
    LANGUAGE plpgsql
    SECURITY DEFINER
    STABLE
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


CREATE OR REPLACE FUNCTION convocatoria.fn_verificar_postulantes(p_id_convocatoria INTEGER)
    RETURNS JSONB
    LANGUAGE plpgsql
    SECURITY DEFINER
    STABLE
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


-- ────────────────────────────────────────────────────────────────────
-- 3. fn_crear_convocatoria(p_datos JSONB)
--    Crea una convocatoria validando la fase del cronograma.
--    Las fechas de vigencia las dicta el periodo activo; NO se reciben.
--
--    p_datos (camelCase):
--    { "idAsignatura": int, "idDocente": int,
--      "cuposDisponibles": int, "estado": text? }
-- ────────────────────────────────────────────────────────────────────
CREATE OR REPLACE FUNCTION convocatoria.fn_crear_convocatoria(p_datos JSONB)
    RETURNS JSONB
    LANGUAGE plpgsql
    SECURITY DEFINER
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


CREATE OR REPLACE FUNCTION convocatoria.fn_actualizar_convocatoria(
    p_datos        JSONB,
    p_tipo_edicion TEXT
)
    RETURNS JSONB
    LANGUAGE plpgsql
    SECURITY DEFINER
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


-- ────────────────────────────────────────────────────────────────────
-- 5. fn_desactivar_convocatoria(p_id INTEGER)
--    Solo permite activo = FALSE si no hay postulaciones activas.
-- ────────────────────────────────────────────────────────────────────
CREATE OR REPLACE FUNCTION convocatoria.fn_desactivar_convocatoria(p_id INTEGER)
    RETURNS JSONB
    LANGUAGE plpgsql
    SECURITY DEFINER
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