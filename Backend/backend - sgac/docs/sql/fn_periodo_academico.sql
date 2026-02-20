-- Funciones para periodo_academico (ajustadas al esquema mostrado en BD)
-- Tabla esperada:
--   id_periodo_acad (PK)
--   nombre_periodo
--   fecha_inicio
--   fecha_fin
--   estado
--   activo (boolean)

CREATE OR REPLACE FUNCTION public.fn_crear_periodo_academico(
    p_nombre character varying,
    p_inicio date,
    p_fin date,
    p_estado character varying
)
RETURNS integer
LANGUAGE plpgsql
AS $$
DECLARE
    v_id integer;
BEGIN
    IF p_inicio IS NULL OR p_fin IS NULL OR p_inicio > p_fin THEN
        RETURN -1;
    END IF;

    INSERT INTO public.periodo_academico (
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
    RETURNING id_periodo_acad INTO v_id;

    RETURN v_id;
EXCEPTION WHEN OTHERS THEN
    RETURN -1;
END;
$$;

CREATE OR REPLACE FUNCTION public.fn_actualizar_periodo_academico(
    p_id integer,
    p_nombre character varying,
    p_inicio date,
    p_fin date,
    p_estado character varying
)
RETURNS integer
LANGUAGE plpgsql
AS $$
BEGIN
    IF p_inicio IS NULL OR p_fin IS NULL OR p_inicio > p_fin THEN
        RETURN -1;
    END IF;

    UPDATE public.periodo_academico
       SET nombre_periodo = trim(p_nombre),
           fecha_inicio = p_inicio,
           fecha_fin = p_fin,
           estado = upper(trim(p_estado))
     WHERE id_periodo_acad = p_id
       AND COALESCE(activo, true) = true;

    IF FOUND THEN
        RETURN 1;
    END IF;

    RETURN -1;
EXCEPTION WHEN OTHERS THEN
    RETURN -1;
END;
$$;

CREATE OR REPLACE FUNCTION public.fn_desactivar_periodo_academico(
    p_id integer
)
RETURNS integer
LANGUAGE plpgsql
AS $$
BEGIN
    UPDATE public.periodo_academico
       SET estado = 'INACTIVO',
           activo = false
     WHERE id_periodo_acad = p_id
       AND COALESCE(activo, true) = true;

    IF FOUND THEN
        RETURN 1;
    END IF;

    RETURN -1;
EXCEPTION WHEN OTHERS THEN
    RETURN -1;
END;
$$;

-- Wrappers de compatibilidad para backend actual (usa prefijo sp_)
CREATE OR REPLACE FUNCTION public.sp_crear_periodo_academico(
    p_nombre character varying,
    p_inicio date,
    p_fin date,
    p_estado character varying
)
RETURNS integer
LANGUAGE plpgsql
AS $$
BEGIN
    RETURN public.fn_crear_periodo_academico(p_nombre, p_inicio, p_fin, p_estado);
END;
$$;

CREATE OR REPLACE FUNCTION public.sp_actualizar_periodo_academico(
    p_id integer,
    p_nombre character varying,
    p_inicio date,
    p_fin date,
    p_estado character varying
)
RETURNS integer
LANGUAGE plpgsql
AS $$
BEGIN
    RETURN public.fn_actualizar_periodo_academico(p_id, p_nombre, p_inicio, p_fin, p_estado);
END;
$$;

CREATE OR REPLACE FUNCTION public.sp_desactivar_periodo_academico(
    p_id integer
)
RETURNS integer
LANGUAGE plpgsql
AS $$
BEGIN
    RETURN public.fn_desactivar_periodo_academico(p_id);
END;
$$;
