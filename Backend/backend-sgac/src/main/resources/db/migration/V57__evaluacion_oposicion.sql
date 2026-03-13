CREATE OR REPLACE FUNCTION postulacion.fn_gestionar_banco_temas(
    p_id_convocatoria INTEGER,
    p_accion          TEXT,
    p_temas_json      JSONB DEFAULT '[]'::jsonb
)
    RETURNS JSONB
    LANGUAGE plpgsql
    SECURITY DEFINER
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

    -- ── LISTAR ────────────────────────────────────────────────
    IF p_accion = 'LISTAR' THEN
        RETURN jsonb_build_object(
                'exito', true,
                'temas', COALESCE(
                        (SELECT jsonb_agg(jsonb_build_object(
                                                  'idTema',          bt.id_tema,
                                                  'descripcionTema', bt.descripcion_tema,
                                                  'activo',          bt.activo
                                          ) ORDER BY bt.id_tema)
                         FROM postulacion.banco_temas bt
                         WHERE bt.id_convocatoria = p_id_convocatoria
                           AND bt.activo = TRUE),
                        '[]'::jsonb
                         )
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
          AND tep.codigo = 'APTO';

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


-- ────────────────────────────────────────────────────────────
-- FUNCIÓN 2: fn_ejecutar_sorteo_oposicion
--
-- Algoritmo de doble shuffle:
--   1. Toma los postulantes APTOS en orden aleatorio → asigna orden_exposicion
--   2. Toma los temas activos en orden aleatorio → asigna tema_exposicion
--   3. Los empareja por posición (rn) → cada postulante recibe un tema único
--   4. Calcula la ventana horaria de 35 minutos por postulante
--   5. Inserta en evaluacion_oposicion con estado PROGRAMADA
--
-- Idempotencia: elimina evaluaciones PROGRAMADA previas antes de re-insertar
-- ────────────────────────────────────────────────────────────
CREATE OR REPLACE FUNCTION postulacion.fn_ejecutar_sorteo_oposicion(
    p_id_convocatoria INTEGER,
    p_fecha           DATE,
    p_hora_inicio     TIME,
    p_lugar           TEXT
)
    RETURNS JSONB
    LANGUAGE plpgsql
    SECURITY DEFINER
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
      AND tep.codigo = 'APTO';

    IF v_total_aptos = 0 THEN
        RETURN jsonb_build_object(
                'exito', false,
                'mensaje', 'No hay postulantes con estado APTO en esta convocatoria.'
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

    -- ── Verificar que no haya evaluaciones ya iniciadas ───────
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
               AND tep.codigo = 'APTO'
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


-- ────────────────────────────────────────────────────────────
-- FUNCIÓN 3: fn_registrar_puntaje_jurado
--
-- Cada miembro del tribunal llama a esta función para guardar
-- sus notas. Puede corregirlas mientras el estado sea EN_CURSO.
--
-- Validaciones:
--   · La evaluación debe estar EN_CURSO
--   · El usuario debe ser miembro activo de esa comisión
--   · Topes: material ≤ 10, exposicion ≤ 4, respuestas ≤ 6
--   · Si el miembro ya finalizó (finalizo_calificacion=true), no puede editar
-- ────────────────────────────────────────────────────────────
CREATE OR REPLACE FUNCTION postulacion.fn_registrar_puntaje_jurado(
    p_id_evaluacion_oposicion INTEGER,
    p_id_usuario              INTEGER,
    p_puntaje_material        NUMERIC(5,2),
    p_puntaje_exposicion      NUMERIC(5,2),
    p_puntaje_respuestas      NUMERIC(5,2),
    p_finalizar               BOOLEAN DEFAULT FALSE
)
    RETURNS JSONB
    LANGUAGE plpgsql
    SECURITY DEFINER
AS $$
DECLARE
    v_codigo_estado  TEXT;
    v_finalizo       BOOLEAN;
    v_id_comision    INTEGER;
    v_todos_finalizaron BOOLEAN;
    v_puntaje_total  NUMERIC(5,2);
BEGIN
    -- ── Obtener estado actual de la evaluación ────────────────
    SELECT tee.codigo
    INTO v_codigo_estado
    FROM  postulacion.evaluacion_oposicion    eo
              JOIN  postulacion.tipo_estado_evaluacion  tee
                    ON tee.id_tipo_estado_evaluacion = eo.id_tipo_estado_evaluacion
    WHERE eo.id_evaluacion_oposicion = p_id_evaluacion_oposicion;

    IF NOT FOUND THEN
        RETURN jsonb_build_object('exito', false, 'mensaje', 'Evaluación no encontrada.');
    END IF;

    IF v_codigo_estado != 'EN_CURSO' THEN
        RETURN jsonb_build_object(
                'exito', false,
                'mensaje', 'Solo se puede calificar cuando la evaluación está EN CURSO. Estado actual: ' || v_codigo_estado
               );
    END IF;

    -- ── Verificar que el usuario es miembro de la comisión ────
    SELECT uc.id_comision_seleccion, uc.finalizo_calificacion
    INTO   v_id_comision, v_finalizo
    FROM   seguridad.usuario_comision      uc
               JOIN   postulacion.comision_seleccion  cs ON cs.id_comision_seleccion = uc.id_comision_seleccion
               JOIN   postulacion.evaluacion_oposicion eo ON eo.id_postulacion IN (
        SELECT id_postulacion FROM postulacion.evaluacion_oposicion
        WHERE id_evaluacion_oposicion = p_id_evaluacion_oposicion
    )
        -- La comisión corresponde a la misma convocatoria de la evaluación
               JOIN   postulacion.postulacion p ON p.id_postulacion = eo.id_postulacion
    WHERE  cs.id_convocatoria = p.id_convocatoria
      AND  uc.id_usuario = p_id_usuario
      AND  uc.activo = TRUE
    LIMIT 1;

    IF NOT FOUND THEN
        RETURN jsonb_build_object(
                'exito', false,
                'mensaje', 'El usuario no pertenece al tribunal de esta convocatoria.'
               );
    END IF;

    IF v_finalizo THEN
        RETURN jsonb_build_object(
                'exito', false,
                'mensaje', 'Ya finalizaste tu calificación. No puedes modificarla.'
               );
    END IF;

    -- ── Validar topes de puntaje ──────────────────────────────
    -- (La BD también los tiene como CHECK, pero validamos antes para dar mensaje claro)
    IF p_puntaje_material > 10.00 THEN
        RETURN jsonb_build_object('exito', false, 'mensaje', 'Material: el puntaje máximo es 10.00');
    END IF;
    IF p_puntaje_exposicion > 4.00 THEN
        RETURN jsonb_build_object('exito', false, 'mensaje', 'Exposición: el puntaje máximo es 4.00');
    END IF;
    IF p_puntaje_respuestas > 6.00 THEN
        RETURN jsonb_build_object('exito', false, 'mensaje', 'Respuestas: el puntaje máximo es 6.00');
    END IF;
    IF p_puntaje_material < 0 OR p_puntaje_exposicion < 0 OR p_puntaje_respuestas < 0 THEN
        RETURN jsonb_build_object('exito', false, 'mensaje', 'Los puntajes no pueden ser negativos.');
    END IF;

    -- ── Guardar puntajes en usuario_comision ──────────────────
    UPDATE seguridad.usuario_comision
    SET puntaje_material        = p_puntaje_material,
        puntaje_exposicion      = p_puntaje_exposicion,
        puntaje_respuestas      = p_puntaje_respuestas,
        id_evaluacion_oposicion = p_id_evaluacion_oposicion,
        fecha_evaluacion        = CURRENT_DATE,
        finalizo_calificacion   = p_finalizar
    WHERE id_usuario             = p_id_usuario
      AND id_comision_seleccion  = v_id_comision
      AND activo                 = TRUE;

    -- ── Si el jurado finaliza, verificar si todos terminaron ──
    -- Si todos los miembros del tribunal finalizaron → calcular promedio automáticamente
    IF p_finalizar THEN
        SELECT BOOL_AND(uc.finalizo_calificacion) INTO v_todos_finalizaron
        FROM   seguridad.usuario_comision uc
        WHERE  uc.id_comision_seleccion = v_id_comision
          AND  uc.activo = TRUE;

        IF v_todos_finalizaron THEN
            -- Calcular promedio de los 3 jurados: suma de totales individuales / 3
            SELECT AVG(uc.puntaje_material + uc.puntaje_exposicion + uc.puntaje_respuestas)
            INTO   v_puntaje_total
            FROM   seguridad.usuario_comision uc
            WHERE  uc.id_comision_seleccion = v_id_comision
              AND  uc.activo = TRUE;

            UPDATE postulacion.evaluacion_oposicion
            SET puntaje_total_oposicion    = ROUND(v_puntaje_total, 2),
                id_tipo_estado_evaluacion  = (
                    SELECT id_tipo_estado_evaluacion
                    FROM   postulacion.tipo_estado_evaluacion
                    WHERE  codigo = 'FINALIZADA'
                )
            WHERE id_evaluacion_oposicion = p_id_evaluacion_oposicion;

            RETURN jsonb_build_object(
                    'exito',           true,
                    'mensaje',         'Puntaje guardado. Todos los jurados finalizaron. Nota final calculada.',
                    'todosFinalizaron', true,
                    'puntajeFinal',    ROUND(v_puntaje_total, 2)
                   );
        END IF;
    END IF;

    RETURN jsonb_build_object(
            'exito',           true,
            'mensaje',         CASE WHEN p_finalizar THEN 'Calificación finalizada y bloqueada.'
                                    ELSE 'Puntaje guardado correctamente.' END,
            'todosFinalizaron', false,
            'subtotal',        p_puntaje_material + p_puntaje_exposicion + p_puntaje_respuestas
           );

EXCEPTION WHEN OTHERS THEN
    RETURN jsonb_build_object('exito', false, 'mensaje', '[ERROR] ' || SQLERRM);
END;
$$;


-- ────────────────────────────────────────────────────────────
-- FUNCIÓN 4: fn_cambiar_estado_evaluacion
--
-- El Coordinador usa esta función para:
--   'INICIAR'    → PROGRAMADA → EN_CURSO (registra hora_inicio_real)
--   'NO_PRESENTO'→ PROGRAMADA → NO_PRESENTO (postulante ausente)
--   'FINALIZAR'  → EN_CURSO  → FINALIZADA (calcula promedio y blinda)
--
-- Solo el coordinador (o admin) debería invocarla.
-- ────────────────────────────────────────────────────────────
CREATE OR REPLACE FUNCTION postulacion.fn_cambiar_estado_evaluacion(
    p_id_evaluacion_oposicion INTEGER,
    p_accion                  TEXT   -- 'INICIAR' | 'NO_PRESENTO' | 'FINALIZAR'
)
    RETURNS JSONB
    LANGUAGE plpgsql
    SECURITY DEFINER
AS $$
DECLARE
    v_codigo_actual   TEXT;
    v_id_estado_nuevo INTEGER;
    v_codigo_nuevo    TEXT;
    v_puntaje_total   NUMERIC(5,2);
    v_id_comision     INTEGER;
BEGIN
    p_accion := UPPER(TRIM(COALESCE(p_accion, '')));

    -- Estado actual
    SELECT tee.codigo
    INTO   v_codigo_actual
    FROM   postulacion.evaluacion_oposicion   eo
               JOIN   postulacion.tipo_estado_evaluacion tee
                      ON tee.id_tipo_estado_evaluacion = eo.id_tipo_estado_evaluacion
    WHERE  eo.id_evaluacion_oposicion = p_id_evaluacion_oposicion;

    IF NOT FOUND THEN
        RETURN jsonb_build_object('exito', false, 'mensaje', 'Evaluación no encontrada.');
    END IF;

    -- ── INICIAR ───────────────────────────────────────────────
    IF p_accion = 'INICIAR' THEN
        IF v_codigo_actual != 'PROGRAMADA' THEN
            RETURN jsonb_build_object(
                    'exito', false,
                    'mensaje', 'Solo se puede iniciar una evaluación PROGRAMADA. Estado actual: ' || v_codigo_actual
                   );
        END IF;

        SELECT id_tipo_estado_evaluacion INTO v_id_estado_nuevo
        FROM   postulacion.tipo_estado_evaluacion WHERE codigo = 'EN_CURSO';

        UPDATE postulacion.evaluacion_oposicion
        SET id_tipo_estado_evaluacion = v_id_estado_nuevo,
            hora_inicio_real          = CURRENT_TIME
        WHERE id_evaluacion_oposicion   = p_id_evaluacion_oposicion;

        RETURN jsonb_build_object(
                'exito',    true,
                'mensaje',  'Evaluación iniciada. El tribunal puede comenzar a calificar.',
                'horaReal', to_char(CURRENT_TIME, 'HH24:MI:SS')
               );
    END IF;

    -- ── NO_PRESENTO ───────────────────────────────────────────
    IF p_accion = 'NO_PRESENTO' THEN
        IF v_codigo_actual != 'PROGRAMADA' THEN
            RETURN jsonb_build_object(
                    'exito', false,
                    'mensaje', 'Solo se puede marcar como No Presentó desde estado PROGRAMADA.'
                   );
        END IF;

        SELECT id_tipo_estado_evaluacion INTO v_id_estado_nuevo
        FROM   postulacion.tipo_estado_evaluacion WHERE codigo = 'NO_PRESENTO';

        UPDATE postulacion.evaluacion_oposicion
        SET id_tipo_estado_evaluacion = v_id_estado_nuevo
        WHERE id_evaluacion_oposicion   = p_id_evaluacion_oposicion;

        RETURN jsonb_build_object('exito', true, 'mensaje', 'Postulante marcado como No Presentó.');
    END IF;

    -- ── FINALIZAR ─────────────────────────────────────────────
    IF p_accion = 'FINALIZAR' THEN
        IF v_codigo_actual != 'EN_CURSO' THEN
            RETURN jsonb_build_object(
                    'exito', false,
                    'mensaje', 'Solo se puede finalizar una evaluación EN CURSO. Estado actual: ' || v_codigo_actual
                   );
        END IF;

        -- Obtener id de la comisión vinculada a esta convocatoria
        SELECT cs.id_comision_seleccion INTO v_id_comision
        FROM   postulacion.evaluacion_oposicion eo
                   JOIN   postulacion.postulacion           p  ON p.id_postulacion   = eo.id_postulacion
                   JOIN   postulacion.comision_seleccion    cs ON cs.id_convocatoria = p.id_convocatoria
        WHERE  eo.id_evaluacion_oposicion = p_id_evaluacion_oposicion
          AND  cs.activo = TRUE
        LIMIT 1;

        -- Calcular promedio de los 3 jurados (solo los que tienen puntajes registrados)
        SELECT AVG(uc.puntaje_material + uc.puntaje_exposicion + uc.puntaje_respuestas)
        INTO   v_puntaje_total
        FROM   seguridad.usuario_comision uc
        WHERE  uc.id_comision_seleccion    = v_id_comision
          AND  uc.id_evaluacion_oposicion  = p_id_evaluacion_oposicion
          AND  uc.activo                   = TRUE
          AND  uc.puntaje_material IS NOT NULL;

        -- Marcar todos los jurados como finalizados
        UPDATE seguridad.usuario_comision
        SET finalizo_calificacion = TRUE
        WHERE id_comision_seleccion   = v_id_comision
          AND id_evaluacion_oposicion = p_id_evaluacion_oposicion
          AND activo = TRUE;

        -- Persistir nota final y cambiar estado → FINALIZADA
        SELECT id_tipo_estado_evaluacion INTO v_id_estado_nuevo
        FROM   postulacion.tipo_estado_evaluacion WHERE codigo = 'FINALIZADA';

        UPDATE postulacion.evaluacion_oposicion
        SET puntaje_total_oposicion    = ROUND(COALESCE(v_puntaje_total, 0), 2),
            hora_fin_real              = CURRENT_TIME,
            id_tipo_estado_evaluacion  = v_id_estado_nuevo
        WHERE id_evaluacion_oposicion    = p_id_evaluacion_oposicion;

        RETURN jsonb_build_object(
                'exito',        true,
                'mensaje',      'Evaluación finalizada y bloqueada.',
                'puntajeFinal', ROUND(COALESCE(v_puntaje_total, 0), 2),
                'horaFin',      to_char(CURRENT_TIME, 'HH24:MI:SS')
               );
    END IF;

    RETURN jsonb_build_object('exito', false, 'mensaje', 'Acción no reconocida: ' || p_accion);

EXCEPTION WHEN OTHERS THEN
    RETURN jsonb_build_object('exito', false, 'mensaje', '[ERROR] ' || SQLERRM);
END;
$$;


-- ────────────────────────────────────────────────────────────
-- FUNCIÓN 5: fn_consultar_cronograma_oposicion
--
-- Devuelve el cronograma completo de oposición de una convocatoria
-- con los puntajes de cada jurado (si ya existen).
-- Útil para la vista del coordinador y del tribunal.
-- ────────────────────────────────────────────────────────────
CREATE OR REPLACE FUNCTION postulacion.fn_consultar_cronograma_oposicion(
    p_id_convocatoria INTEGER
)
    RETURNS JSONB
    LANGUAGE plpgsql
    SECURITY DEFINER
    STABLE
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
                                            'horaInicioReal',        to_char(eo.hora_inicio_real, 'HH24:MI'),
                                            'horaFinReal',           to_char(eo.hora_fin_real,    'HH24:MI'),
                                            'lugar',                 eo.lugar,
                                            'estado',                tee.codigo,
                                            'nombreEstado',          tee.nombre,
                                            'puntajeFinal',          eo.puntaje_total_oposicion,
                                        -- Puntajes individuales de cada jurado
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