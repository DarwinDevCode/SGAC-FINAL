-- V20: Crear stored procedures para gestión de comisiones de selección
-- sp_crear_comision, sp_actualizar_comision, sp_desactivar_comision
-- sp_crear_usuario_comision, sp_actualizar_usuario_comision,
-- sp_desactivar_usuario_comision, sp_listar_evaluadores_comision

-- Hacer nullable id_evaluacion_oposicion (la comisión se designa antes de las evaluaciones)
ALTER TABLE seguridad.usuario_comision
    ALTER COLUMN id_evaluacion_oposicion DROP NOT NULL;

-- Limpiar funciones existentes que pueden tener firmas o nombres de parámetros diferentes
DROP FUNCTION IF EXISTS public.sp_crear_comision(INTEGER, VARCHAR, DATE);
DROP FUNCTION IF EXISTS public.sp_actualizar_comision(INTEGER, VARCHAR, DATE);
DROP FUNCTION IF EXISTS public.sp_desactivar_comision(INTEGER);
DROP FUNCTION IF EXISTS public.sp_crear_usuario_comision(INTEGER, INTEGER, INTEGER, VARCHAR, NUMERIC, NUMERIC, NUMERIC, DATE);
DROP FUNCTION IF EXISTS public.sp_actualizar_usuario_comision(INTEGER, NUMERIC, NUMERIC, NUMERIC, DATE);
DROP FUNCTION IF EXISTS public.sp_desactivar_usuario_comision(INTEGER);
DROP FUNCTION IF EXISTS public.sp_listar_evaluadores_comision(INTEGER);
DROP FUNCTION IF EXISTS public.sp_obtener_decanos_activos();

-- ────────────────────────────────────────────────
-- FIX: Obtener decanos activos usando academico.decano
-- ────────────────────────────────────────────────
CREATE OR REPLACE FUNCTION public.sp_obtener_decanos_activos()
    RETURNS SETOF academico.decano
    LANGUAGE plpgsql
AS $$
BEGIN
    RETURN QUERY
        SELECT *
        FROM academico.decano d
        WHERE d.activo = true;
END;
$$;
DROP FUNCTION IF EXISTS public.sp_crear_usuario_comision(INTEGER, INTEGER, INTEGER, VARCHAR, NUMERIC, NUMERIC, NUMERIC, DATE);
DROP FUNCTION IF EXISTS public.sp_actualizar_usuario_comision(INTEGER, NUMERIC, NUMERIC, NUMERIC, DATE);
DROP FUNCTION IF EXISTS public.sp_desactivar_usuario_comision(INTEGER);
DROP FUNCTION IF EXISTS public.sp_listar_evaluadores_comision(INTEGER);

-- ────────────────────────────────────────────────
-- 1. sp_crear_comision
-- ────────────────────────────────────────────────
CREATE OR REPLACE FUNCTION public.sp_crear_comision(
    p_id_convocatoria    INTEGER,
    p_nombre_comision    VARCHAR(100),
    p_fecha_conformacion DATE
)
RETURNS INTEGER AS $$
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
$$ LANGUAGE plpgsql;

-- ────────────────────────────────────────────────
-- 2. sp_actualizar_comision
-- ────────────────────────────────────────────────
CREATE OR REPLACE FUNCTION public.sp_actualizar_comision(
    p_id                 INTEGER,
    p_nombre_comision    VARCHAR(100),
    p_fecha_conformacion DATE
)
RETURNS INTEGER AS $$
BEGIN
    UPDATE postulacion.comision_seleccion
    SET nombre_comision    = p_nombre_comision,
        fecha_conformacion = p_fecha_conformacion
    WHERE id_comision_seleccion = p_id;
    RETURN p_id;
EXCEPTION
    WHEN OTHERS THEN RETURN -1;
END;
$$ LANGUAGE plpgsql;

-- ────────────────────────────────────────────────
-- 3. sp_desactivar_comision
-- ────────────────────────────────────────────────
CREATE OR REPLACE FUNCTION public.sp_desactivar_comision(p_id INTEGER)
RETURNS INTEGER AS $$
BEGIN
    UPDATE postulacion.comision_seleccion
    SET activo = FALSE
    WHERE id_comision_seleccion = p_id;
    RETURN p_id;
EXCEPTION
    WHEN OTHERS THEN RETURN -1;
END;
$$ LANGUAGE plpgsql;

-- ────────────────────────────────────────────────
-- 4. sp_crear_usuario_comision
-- ────────────────────────────────────────────────
CREATE OR REPLACE FUNCTION public.sp_crear_usuario_comision(
    p_id_comision              INTEGER,
    p_id_usuario               INTEGER,
    p_id_evaluacion_oposicion  INTEGER DEFAULT NULL,
    p_rol_integrante           VARCHAR(50) DEFAULT NULL,
    p_puntaje_material         NUMERIC(5,2) DEFAULT NULL,
    p_puntaje_respuestas       NUMERIC(5,2) DEFAULT NULL,
    p_puntaje_exposicion       NUMERIC(5,2) DEFAULT NULL,
    p_fecha_evaluacion         DATE DEFAULT NULL
)
RETURNS INTEGER AS $$
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
$$ LANGUAGE plpgsql;

-- ────────────────────────────────────────────────
-- 5. sp_actualizar_usuario_comision
-- ────────────────────────────────────────────────
CREATE OR REPLACE FUNCTION public.sp_actualizar_usuario_comision(
    p_id                 INTEGER,
    p_puntaje_material   NUMERIC(5,2) DEFAULT NULL,
    p_puntaje_respuestas NUMERIC(5,2) DEFAULT NULL,
    p_puntaje_exposicion NUMERIC(5,2) DEFAULT NULL,
    p_fecha_evaluacion   DATE DEFAULT NULL
)
RETURNS INTEGER AS $$
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
$$ LANGUAGE plpgsql;

-- ────────────────────────────────────────────────
-- 6. sp_desactivar_usuario_comision
-- ────────────────────────────────────────────────
CREATE OR REPLACE FUNCTION public.sp_desactivar_usuario_comision(p_id INTEGER)
RETURNS INTEGER AS $$
BEGIN
    DELETE FROM seguridad.usuario_comision WHERE id_usuario_comision = p_id;
    RETURN p_id;
EXCEPTION
    WHEN OTHERS THEN RETURN -1;
END;
$$ LANGUAGE plpgsql;

-- ────────────────────────────────────────────────
-- 7. sp_listar_evaluadores_comision
-- Devuelve los integrantes de una comisión (compatible con UsuarioComision entity)
-- ────────────────────────────────────────────────
CREATE OR REPLACE FUNCTION public.sp_listar_evaluadores_comision(p_id_comision INTEGER)
RETURNS TABLE (
    id_usuario_comision        INTEGER,
    id_comision_seleccion      INTEGER,
    id_usuario                 INTEGER,
    id_evaluacion_oposicion    INTEGER,
    rol_integrante             VARCHAR,
    puntaje_material           NUMERIC,
    puntaje_respuestas         NUMERIC,
    puntaje_exposicion         NUMERIC,
    fecha_evaluacion           DATE
) AS $$
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
$$ LANGUAGE plpgsql;

-- Permisos
GRANT EXECUTE ON ALL FUNCTIONS IN SCHEMA public TO app_user_default;
GRANT SELECT, INSERT, UPDATE, DELETE ON seguridad.usuario_comision TO app_user_default;
GRANT SELECT, INSERT, UPDATE, DELETE ON postulacion.comision_seleccion TO app_user_default;
GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA seguridad  TO app_user_default;
GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA postulacion TO app_user_default;
