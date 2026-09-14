CREATE SCHEMA IF NOT EXISTS planificacion;
CREATE TABLE IF NOT EXISTS planificacion.tipo_fase (
                                                       id_tipo_fase    SERIAL          PRIMARY KEY,
                                                       codigo          VARCHAR(60)     NOT NULL UNIQUE,
                                                       nombre          VARCHAR(120)    NOT NULL,
                                                       descripcion     TEXT,
                                                       orden           INTEGER         NOT NULL UNIQUE CHECK (orden > 0),
                                                       activo          BOOLEAN         NOT NULL DEFAULT TRUE
);


CREATE TABLE IF NOT EXISTS planificacion.periodo_fase (
                                                          id_periodo_fase         SERIAL      PRIMARY KEY,
                                                          id_periodo_academico    INTEGER     NOT NULL
                                                              REFERENCES academico.periodo_academico (id_periodo_academico)
                                                                  ON DELETE RESTRICT,
                                                          id_tipo_fase            INTEGER     NOT NULL
                                                              REFERENCES planificacion.tipo_fase (id_tipo_fase)
                                                                  ON DELETE RESTRICT,
                                                          fecha_inicio            DATE        NOT NULL,
                                                          fecha_fin               DATE        NOT NULL,
                                                          CONSTRAINT chk_fecha_coherencia
                                                              CHECK (fecha_fin >= fecha_inicio),
                                                          CONSTRAINT uq_periodo_tipo_fase
                                                              UNIQUE (id_periodo_academico, id_tipo_fase)
);


CREATE OR REPLACE FUNCTION planificacion.fn_validar_periodo_fase()
    RETURNS TRIGGER
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

CREATE OR REPLACE TRIGGER trg_validar_periodo_fase
    BEFORE INSERT OR UPDATE
    ON planificacion.periodo_fase
    FOR EACH ROW
EXECUTE FUNCTION planificacion.fn_validar_periodo_fase();

COMMENT ON FUNCTION planificacion.fn_validar_periodo_fase() IS
    'Valida tres reglas de integridad temporal: (1) Contención dentro del período,
     (2) Secuencia estricta entre fases, (3) No solapamiento de rangos de fecha.';



CREATE OR REPLACE FUNCTION planificacion.fn_verificar_fase_actual(
    p_nombre_fase           TEXT,
    p_id_periodo_academico  INTEGER
)
    RETURNS BOOLEAN
    LANGUAGE sql
    STABLE
    PARALLEL SAFE
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


CREATE OR REPLACE FUNCTION planificacion.fn_verificar_fase_actual(
    p_nombre_fase TEXT
)
    RETURNS BOOLEAN
    LANGUAGE sql
    STABLE
    PARALLEL SAFE
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


CREATE INDEX IF NOT EXISTS idx_periodo_fase_periodo_tipo
    ON planificacion.periodo_fase (id_periodo_academico, id_tipo_fase);

CREATE INDEX IF NOT EXISTS idx_periodo_fase_fechas
    ON planificacion.periodo_fase (fecha_inicio, fecha_fin);