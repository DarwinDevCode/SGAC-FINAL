-- Bug: fn_guardar_evaluacion_meritos (V67) mueve la postulacion de APROBADA a
-- EN_EVALUACION apenas se finaliza la calificacion de meritos. Pero
-- fn_gestionar_banco_temas y fn_ejecutar_sorteo_oposicion (V57) exigian
-- literalmente estado = 'APROBADA' para contar postulantes aptos y para
-- ejecutar el sorteo. Como el flujo documentado del sistema es meritos
-- ANTES que oposicion, en la practica el sorteo quedaba bloqueado para
-- siempre en cuanto el coordinador calificaba meritos: la postulacion ya
-- no estaba en APROBADA, estaba en EN_EVALUACION (que es justamente el
-- estado "en proceso de evaluacion de oposicion y meritos").
--
-- Fix: ambas funciones ahora aceptan postulaciones en APROBADA o
-- EN_EVALUACION como aptas para el banco de temas y el sorteo.

CREATE OR REPLACE FUNCTION postulacion.fn_gestionar_banco_temas(p_id_convocatoria integer, p_accion text, p_temas_json jsonb DEFAULT '[]'::jsonb)
 RETURNS jsonb
 LANGUAGE plpgsql
 SECURITY DEFINER
AS $function$
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
          AND tep.codigo IN ('APROBADA', 'EN_EVALUACION');

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
          AND tep.codigo IN ('APROBADA', 'EN_EVALUACION');

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
$function$;


CREATE OR REPLACE FUNCTION postulacion.fn_ejecutar_sorteo_oposicion(p_id_convocatoria integer, p_fecha date, p_hora_inicio time without time zone, p_lugar text)
 RETURNS jsonb
 LANGUAGE plpgsql
 SECURITY DEFINER
AS $function$
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
      AND tep.codigo IN ('APROBADA', 'EN_EVALUACION');

    IF v_total_aptos = 0 THEN
        RETURN jsonb_build_object(
                'exito', false,
                'mensaje', 'No hay postulantes con estado APROBADA o EN_EVALUACION en esta convocatoria.'
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
               AND tep.codigo IN ('APROBADA', 'EN_EVALUACION')
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
$function$;
