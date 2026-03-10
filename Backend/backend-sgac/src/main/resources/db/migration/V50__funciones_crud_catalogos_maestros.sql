-- ═══════════════════════════════════════════════════════════════════════════════════════════════════
-- V50: Funciones CRUD para Catálogos Maestros
-- Descripción: Implementación de funciones de persistencia para catálogos maestros con control
--              transaccional, validaciones de duplicados y eliminación lógica.
-- Fecha: 2026-03-10
-- ═══════════════════════════════════════════════════════════════════════════════════════════════════

-- ═══════════════════════════════════════════════════════════════════════════════════════════════════
-- 1. FUNCIONES CRUD PARA: ayudantia.tipo_sancion_ayudante_catedra
-- ═══════════════════════════════════════════════════════════════════════════════════════════════════

-- 1.1 INSERT - Crear tipo de sanción
CREATE OR REPLACE FUNCTION ayudantia.fn_crear_tipo_sancion_ayudante_catedra(
    p_nombre_tipo_sancion VARCHAR(100),
    p_codigo VARCHAR(25)
)
RETURNS JSONB
LANGUAGE plpgsql
SECURITY DEFINER
AS $function$
DECLARE
    v_id INTEGER;
BEGIN
    -- Validar que el nombre no exista (case-insensitive)
    IF EXISTS (
        SELECT 1 FROM ayudantia.tipo_sancion_ayudante_catedra
        WHERE LOWER(TRIM(nombre_tipo_sancion)) = LOWER(TRIM(p_nombre_tipo_sancion))
    ) THEN
        RETURN jsonb_build_object('exito', false, 'mensaje', 'Ya existe un tipo de sanción con el nombre especificado');
    END IF;

    -- Validar que el código no exista (case-insensitive)
    IF EXISTS (
        SELECT 1 FROM ayudantia.tipo_sancion_ayudante_catedra
        WHERE LOWER(TRIM(codigo)) = LOWER(TRIM(p_codigo))
    ) THEN
        RETURN jsonb_build_object('exito', false, 'mensaje', 'Ya existe un tipo de sanción con el código especificado');
    END IF;

    -- Insertar el nuevo registro
    INSERT INTO ayudantia.tipo_sancion_ayudante_catedra (nombre_tipo_sancion, codigo, activo)
    VALUES (TRIM(p_nombre_tipo_sancion), UPPER(TRIM(p_codigo)), TRUE)
    RETURNING id_tipo_sancion_ayudante_catedra INTO v_id;

    RETURN jsonb_build_object(
        'exito', true,
        'mensaje', 'Tipo de sanción creado exitosamente',
        'id', v_id
    );

EXCEPTION
    WHEN unique_violation THEN
        RETURN jsonb_build_object('exito', false, 'mensaje', 'Error de duplicidad: el nombre o código ya existe');
    WHEN OTHERS THEN
        RETURN jsonb_build_object('exito', false, 'mensaje', 'Error al crear tipo de sanción: ' || SQLERRM);
END;
$function$;

-- 1.2 UPDATE - Actualizar tipo de sanción
CREATE OR REPLACE FUNCTION ayudantia.fn_actualizar_tipo_sancion_ayudante_catedra(
    p_id INTEGER,
    p_nombre_tipo_sancion VARCHAR(100),
    p_codigo VARCHAR(25)
)
RETURNS JSONB
LANGUAGE plpgsql
SECURITY DEFINER
AS $function$
BEGIN
    -- Validar que el registro exista
    IF NOT EXISTS (
        SELECT 1 FROM ayudantia.tipo_sancion_ayudante_catedra
        WHERE id_tipo_sancion_ayudante_catedra = p_id
    ) THEN
        RETURN jsonb_build_object('exito', false, 'mensaje', 'No se encontró el tipo de sanción con el ID especificado');
    END IF;

    -- Validar que el nombre no exista en otro registro (case-insensitive)
    IF EXISTS (
        SELECT 1 FROM ayudantia.tipo_sancion_ayudante_catedra
        WHERE LOWER(TRIM(nombre_tipo_sancion)) = LOWER(TRIM(p_nombre_tipo_sancion))
        AND id_tipo_sancion_ayudante_catedra != p_id
    ) THEN
        RETURN jsonb_build_object('exito', false, 'mensaje', 'Ya existe otro tipo de sanción con el nombre especificado');
    END IF;

    -- Validar que el código no exista en otro registro (case-insensitive)
    IF EXISTS (
        SELECT 1 FROM ayudantia.tipo_sancion_ayudante_catedra
        WHERE LOWER(TRIM(codigo)) = LOWER(TRIM(p_codigo))
        AND id_tipo_sancion_ayudante_catedra != p_id
    ) THEN
        RETURN jsonb_build_object('exito', false, 'mensaje', 'Ya existe otro tipo de sanción con el código especificado');
    END IF;

    -- Actualizar el registro
    UPDATE ayudantia.tipo_sancion_ayudante_catedra
    SET nombre_tipo_sancion = TRIM(p_nombre_tipo_sancion),
        codigo = UPPER(TRIM(p_codigo))
    WHERE id_tipo_sancion_ayudante_catedra = p_id;

    RETURN jsonb_build_object(
        'exito', true,
        'mensaje', 'Tipo de sanción actualizado exitosamente',
        'id', p_id
    );

EXCEPTION
    WHEN unique_violation THEN
        RETURN jsonb_build_object('exito', false, 'mensaje', 'Error de duplicidad: el nombre o código ya existe');
    WHEN OTHERS THEN
        RETURN jsonb_build_object('exito', false, 'mensaje', 'Error al actualizar tipo de sanción: ' || SQLERRM);
END;
$function$;

-- 1.3 DELETE (Lógico) - Desactivar tipo de sanción
CREATE OR REPLACE FUNCTION ayudantia.fn_eliminar_tipo_sancion_ayudante_catedra(
    p_id INTEGER
)
RETURNS JSONB
LANGUAGE plpgsql
SECURITY DEFINER
AS $function$
BEGIN
    -- Validar que el registro exista
    IF NOT EXISTS (
        SELECT 1 FROM ayudantia.tipo_sancion_ayudante_catedra
        WHERE id_tipo_sancion_ayudante_catedra = p_id
    ) THEN
        RETURN jsonb_build_object('exito', false, 'mensaje', 'No se encontró el tipo de sanción con el ID especificado');
    END IF;

    -- Eliminación lógica: setear activo = false
    UPDATE ayudantia.tipo_sancion_ayudante_catedra
    SET activo = FALSE
    WHERE id_tipo_sancion_ayudante_catedra = p_id;

    RETURN jsonb_build_object(
        'exito', true,
        'mensaje', 'Tipo de sanción desactivado exitosamente',
        'id', p_id
    );

EXCEPTION
    WHEN OTHERS THEN
        RETURN jsonb_build_object('exito', false, 'mensaje', 'Error al desactivar tipo de sanción: ' || SQLERRM);
END;
$function$;

-- 1.4 SELECT - Listar tipos de sanción activos
CREATE OR REPLACE FUNCTION ayudantia.fn_listar_tipo_sancion_ayudante_catedra()
RETURNS JSONB
LANGUAGE plpgsql
SECURITY DEFINER
AS $function$
DECLARE
    v_resultado JSONB;
BEGIN
    SELECT COALESCE(jsonb_agg(
        jsonb_build_object(
            'id', id_tipo_sancion_ayudante_catedra,
            'nombre_tipo_sancion', nombre_tipo_sancion,
            'codigo', codigo,
            'activo', activo
        ) ORDER BY nombre_tipo_sancion
    ), '[]'::jsonb)
    INTO v_resultado
    FROM ayudantia.tipo_sancion_ayudante_catedra
    WHERE activo = TRUE;

    RETURN jsonb_build_object(
        'exito', true,
        'datos', v_resultado
    );

EXCEPTION
    WHEN OTHERS THEN
        RETURN jsonb_build_object('exito', false, 'mensaje', 'Error al listar tipos de sanción: ' || SQLERRM);
END;
$function$;


-- ═══════════════════════════════════════════════════════════════════════════════════════════════════
-- 2. FUNCIONES CRUD PARA: ayudantia.tipo_estado_ayudantia
-- ═══════════════════════════════════════════════════════════════════════════════════════════════════

-- 2.1 INSERT - Crear tipo de estado de ayudantía
CREATE OR REPLACE FUNCTION ayudantia.fn_crear_tipo_estado_ayudantia(
    p_nombre_estado VARCHAR(50),
    p_descripcion TEXT,
    p_codigo VARCHAR(25)
)
RETURNS JSONB
LANGUAGE plpgsql
SECURITY DEFINER
AS $function$
DECLARE
    v_id INTEGER;
BEGIN
    -- Validar que el nombre no exista (case-insensitive)
    IF EXISTS (
        SELECT 1 FROM ayudantia.tipo_estado_ayudantia
        WHERE LOWER(TRIM(nombre_estado)) = LOWER(TRIM(p_nombre_estado))
    ) THEN
        RETURN jsonb_build_object('exito', false, 'mensaje', 'Ya existe un estado de ayudantía con el nombre especificado');
    END IF;

    -- Validar que el código no exista (case-insensitive)
    IF EXISTS (
        SELECT 1 FROM ayudantia.tipo_estado_ayudantia
        WHERE LOWER(TRIM(codigo)) = LOWER(TRIM(p_codigo))
    ) THEN
        RETURN jsonb_build_object('exito', false, 'mensaje', 'Ya existe un estado de ayudantía con el código especificado');
    END IF;

    -- Insertar el nuevo registro
    INSERT INTO ayudantia.tipo_estado_ayudantia (nombre_estado, descripcion, codigo, activo)
    VALUES (TRIM(p_nombre_estado), TRIM(p_descripcion), UPPER(TRIM(p_codigo)), TRUE)
    RETURNING id_tipo_estado_ayudantia INTO v_id;

    RETURN jsonb_build_object(
        'exito', true,
        'mensaje', 'Estado de ayudantía creado exitosamente',
        'id', v_id
    );

EXCEPTION
    WHEN unique_violation THEN
        RETURN jsonb_build_object('exito', false, 'mensaje', 'Error de duplicidad: el nombre o código ya existe');
    WHEN OTHERS THEN
        RETURN jsonb_build_object('exito', false, 'mensaje', 'Error al crear estado de ayudantía: ' || SQLERRM);
END;
$function$;

-- 2.2 UPDATE - Actualizar tipo de estado de ayudantía
CREATE OR REPLACE FUNCTION ayudantia.fn_actualizar_tipo_estado_ayudantia(
    p_id INTEGER,
    p_nombre_estado VARCHAR(50),
    p_descripcion TEXT,
    p_codigo VARCHAR(25)
)
RETURNS JSONB
LANGUAGE plpgsql
SECURITY DEFINER
AS $function$
BEGIN
    -- Validar que el registro exista
    IF NOT EXISTS (
        SELECT 1 FROM ayudantia.tipo_estado_ayudantia
        WHERE id_tipo_estado_ayudantia = p_id
    ) THEN
        RETURN jsonb_build_object('exito', false, 'mensaje', 'No se encontró el estado de ayudantía con el ID especificado');
    END IF;

    -- Validar que el nombre no exista en otro registro (case-insensitive)
    IF EXISTS (
        SELECT 1 FROM ayudantia.tipo_estado_ayudantia
        WHERE LOWER(TRIM(nombre_estado)) = LOWER(TRIM(p_nombre_estado))
        AND id_tipo_estado_ayudantia != p_id
    ) THEN
        RETURN jsonb_build_object('exito', false, 'mensaje', 'Ya existe otro estado de ayudantía con el nombre especificado');
    END IF;

    -- Validar que el código no exista en otro registro (case-insensitive)
    IF EXISTS (
        SELECT 1 FROM ayudantia.tipo_estado_ayudantia
        WHERE LOWER(TRIM(codigo)) = LOWER(TRIM(p_codigo))
        AND id_tipo_estado_ayudantia != p_id
    ) THEN
        RETURN jsonb_build_object('exito', false, 'mensaje', 'Ya existe otro estado de ayudantía con el código especificado');
    END IF;

    -- Actualizar el registro
    UPDATE ayudantia.tipo_estado_ayudantia
    SET nombre_estado = TRIM(p_nombre_estado),
        descripcion = TRIM(p_descripcion),
        codigo = UPPER(TRIM(p_codigo))
    WHERE id_tipo_estado_ayudantia = p_id;

    RETURN jsonb_build_object(
        'exito', true,
        'mensaje', 'Estado de ayudantía actualizado exitosamente',
        'id', p_id
    );

EXCEPTION
    WHEN unique_violation THEN
        RETURN jsonb_build_object('exito', false, 'mensaje', 'Error de duplicidad: el nombre o código ya existe');
    WHEN OTHERS THEN
        RETURN jsonb_build_object('exito', false, 'mensaje', 'Error al actualizar estado de ayudantía: ' || SQLERRM);
END;
$function$;

-- 2.3 DELETE (Lógico) - Desactivar tipo de estado de ayudantía
CREATE OR REPLACE FUNCTION ayudantia.fn_eliminar_tipo_estado_ayudantia(
    p_id INTEGER
)
RETURNS JSONB
LANGUAGE plpgsql
SECURITY DEFINER
AS $function$
BEGIN
    -- Validar que el registro exista
    IF NOT EXISTS (
        SELECT 1 FROM ayudantia.tipo_estado_ayudantia
        WHERE id_tipo_estado_ayudantia = p_id
    ) THEN
        RETURN jsonb_build_object('exito', false, 'mensaje', 'No se encontró el estado de ayudantía con el ID especificado');
    END IF;

    -- Eliminación lógica: setear activo = false
    UPDATE ayudantia.tipo_estado_ayudantia
    SET activo = FALSE
    WHERE id_tipo_estado_ayudantia = p_id;

    RETURN jsonb_build_object(
        'exito', true,
        'mensaje', 'Estado de ayudantía desactivado exitosamente',
        'id', p_id
    );

EXCEPTION
    WHEN OTHERS THEN
        RETURN jsonb_build_object('exito', false, 'mensaje', 'Error al desactivar estado de ayudantía: ' || SQLERRM);
END;
$function$;

-- 2.4 SELECT - Listar tipos de estado de ayudantía activos
CREATE OR REPLACE FUNCTION ayudantia.fn_listar_tipo_estado_ayudantia()
RETURNS JSONB
LANGUAGE plpgsql
SECURITY DEFINER
AS $function$
DECLARE
    v_resultado JSONB;
BEGIN
    SELECT COALESCE(jsonb_agg(
        jsonb_build_object(
            'id', id_tipo_estado_ayudantia,
            'nombre_estado', nombre_estado,
            'descripcion', descripcion,
            'codigo', codigo,
            'activo', activo
        ) ORDER BY nombre_estado
    ), '[]'::jsonb)
    INTO v_resultado
    FROM ayudantia.tipo_estado_ayudantia
    WHERE activo = TRUE;

    RETURN jsonb_build_object(
        'exito', true,
        'datos', v_resultado
    );

EXCEPTION
    WHEN OTHERS THEN
        RETURN jsonb_build_object('exito', false, 'mensaje', 'Error al listar estados de ayudantía: ' || SQLERRM);
END;
$function$;


-- ═══════════════════════════════════════════════════════════════════════════════════════════════════
-- 3. FUNCIONES CRUD PARA: ayudantia.tipo_estado_registro
-- ═══════════════════════════════════════════════════════════════════════════════════════════════════

-- 3.1 INSERT - Crear tipo de estado de registro
CREATE OR REPLACE FUNCTION ayudantia.fn_crear_tipo_estado_registro(
    p_nombre_estado VARCHAR(50),
    p_descripcion TEXT,
    p_codigo VARCHAR(25)
)
RETURNS JSONB
LANGUAGE plpgsql
SECURITY DEFINER
AS $function$
DECLARE
    v_id INTEGER;
BEGIN
    -- Validar que el nombre no exista (case-insensitive)
    IF EXISTS (
        SELECT 1 FROM ayudantia.tipo_estado_registro
        WHERE LOWER(TRIM(nombre_estado)) = LOWER(TRIM(p_nombre_estado))
    ) THEN
        RETURN jsonb_build_object('exito', false, 'mensaje', 'Ya existe un estado de registro con el nombre especificado');
    END IF;

    -- Validar que el código no exista (case-insensitive)
    IF EXISTS (
        SELECT 1 FROM ayudantia.tipo_estado_registro
        WHERE LOWER(TRIM(codigo)) = LOWER(TRIM(p_codigo))
    ) THEN
        RETURN jsonb_build_object('exito', false, 'mensaje', 'Ya existe un estado de registro con el código especificado');
    END IF;

    -- Insertar el nuevo registro
    INSERT INTO ayudantia.tipo_estado_registro (nombre_estado, descripcion, codigo, activo)
    VALUES (TRIM(p_nombre_estado), TRIM(p_descripcion), UPPER(TRIM(p_codigo)), TRUE)
    RETURNING id_tipo_estado_registro INTO v_id;

    RETURN jsonb_build_object(
        'exito', true,
        'mensaje', 'Estado de registro creado exitosamente',
        'id', v_id
    );

EXCEPTION
    WHEN unique_violation THEN
        RETURN jsonb_build_object('exito', false, 'mensaje', 'Error de duplicidad: el nombre o código ya existe');
    WHEN OTHERS THEN
        RETURN jsonb_build_object('exito', false, 'mensaje', 'Error al crear estado de registro: ' || SQLERRM);
END;
$function$;

-- 3.2 UPDATE - Actualizar tipo de estado de registro
CREATE OR REPLACE FUNCTION ayudantia.fn_actualizar_tipo_estado_registro(
    p_id INTEGER,
    p_nombre_estado VARCHAR(50),
    p_descripcion TEXT,
    p_codigo VARCHAR(25)
)
RETURNS JSONB
LANGUAGE plpgsql
SECURITY DEFINER
AS $function$
BEGIN
    -- Validar que el registro exista
    IF NOT EXISTS (
        SELECT 1 FROM ayudantia.tipo_estado_registro
        WHERE id_tipo_estado_registro = p_id
    ) THEN
        RETURN jsonb_build_object('exito', false, 'mensaje', 'No se encontró el estado de registro con el ID especificado');
    END IF;

    -- Validar que el nombre no exista en otro registro (case-insensitive)
    IF EXISTS (
        SELECT 1 FROM ayudantia.tipo_estado_registro
        WHERE LOWER(TRIM(nombre_estado)) = LOWER(TRIM(p_nombre_estado))
        AND id_tipo_estado_registro != p_id
    ) THEN
        RETURN jsonb_build_object('exito', false, 'mensaje', 'Ya existe otro estado de registro con el nombre especificado');
    END IF;

    -- Validar que el código no exista en otro registro (case-insensitive)
    IF EXISTS (
        SELECT 1 FROM ayudantia.tipo_estado_registro
        WHERE LOWER(TRIM(codigo)) = LOWER(TRIM(p_codigo))
        AND id_tipo_estado_registro != p_id
    ) THEN
        RETURN jsonb_build_object('exito', false, 'mensaje', 'Ya existe otro estado de registro con el código especificado');
    END IF;

    -- Actualizar el registro
    UPDATE ayudantia.tipo_estado_registro
    SET nombre_estado = TRIM(p_nombre_estado),
        descripcion = TRIM(p_descripcion),
        codigo = UPPER(TRIM(p_codigo))
    WHERE id_tipo_estado_registro = p_id;

    RETURN jsonb_build_object(
        'exito', true,
        'mensaje', 'Estado de registro actualizado exitosamente',
        'id', p_id
    );

EXCEPTION
    WHEN unique_violation THEN
        RETURN jsonb_build_object('exito', false, 'mensaje', 'Error de duplicidad: el nombre o código ya existe');
    WHEN OTHERS THEN
        RETURN jsonb_build_object('exito', false, 'mensaje', 'Error al actualizar estado de registro: ' || SQLERRM);
END;
$function$;

-- 3.3 DELETE (Lógico) - Desactivar tipo de estado de registro
CREATE OR REPLACE FUNCTION ayudantia.fn_eliminar_tipo_estado_registro(
    p_id INTEGER
)
RETURNS JSONB
LANGUAGE plpgsql
SECURITY DEFINER
AS $function$
BEGIN
    -- Validar que el registro exista
    IF NOT EXISTS (
        SELECT 1 FROM ayudantia.tipo_estado_registro
        WHERE id_tipo_estado_registro = p_id
    ) THEN
        RETURN jsonb_build_object('exito', false, 'mensaje', 'No se encontró el estado de registro con el ID especificado');
    END IF;

    -- Eliminación lógica: setear activo = false
    UPDATE ayudantia.tipo_estado_registro
    SET activo = FALSE
    WHERE id_tipo_estado_registro = p_id;

    RETURN jsonb_build_object(
        'exito', true,
        'mensaje', 'Estado de registro desactivado exitosamente',
        'id', p_id
    );

EXCEPTION
    WHEN OTHERS THEN
        RETURN jsonb_build_object('exito', false, 'mensaje', 'Error al desactivar estado de registro: ' || SQLERRM);
END;
$function$;

-- 3.4 SELECT - Listar tipos de estado de registro activos
CREATE OR REPLACE FUNCTION ayudantia.fn_listar_tipo_estado_registro()
RETURNS JSONB
LANGUAGE plpgsql
SECURITY DEFINER
AS $function$
DECLARE
    v_resultado JSONB;
BEGIN
    SELECT COALESCE(jsonb_agg(
        jsonb_build_object(
            'id', id_tipo_estado_registro,
            'nombre_estado', nombre_estado,
            'descripcion', descripcion,
            'codigo', codigo,
            'activo', activo
        ) ORDER BY nombre_estado
    ), '[]'::jsonb)
    INTO v_resultado
    FROM ayudantia.tipo_estado_registro
    WHERE activo = TRUE;

    RETURN jsonb_build_object(
        'exito', true,
        'datos', v_resultado
    );

EXCEPTION
    WHEN OTHERS THEN
        RETURN jsonb_build_object('exito', false, 'mensaje', 'Error al listar estados de registro: ' || SQLERRM);
END;
$function$;


-- ═══════════════════════════════════════════════════════════════════════════════════════════════════
-- 4. FUNCIONES CRUD PARA: ayudantia.tipo_estado_evidencia
-- ═══════════════════════════════════════════════════════════════════════════════════════════════════

-- 4.1 INSERT - Crear tipo de estado de evidencia
CREATE OR REPLACE FUNCTION ayudantia.fn_crear_tipo_estado_evidencia(
    p_nombre_estado VARCHAR(50),
    p_descripcion TEXT,
    p_codigo VARCHAR(25)
)
RETURNS JSONB
LANGUAGE plpgsql
SECURITY DEFINER
AS $function$
DECLARE
    v_id INTEGER;
BEGIN
    -- Validar que el nombre no exista (case-insensitive)
    IF EXISTS (
        SELECT 1 FROM ayudantia.tipo_estado_evidencia
        WHERE LOWER(TRIM(nombre_estado)) = LOWER(TRIM(p_nombre_estado))
    ) THEN
        RETURN jsonb_build_object('exito', false, 'mensaje', 'Ya existe un estado de evidencia con el nombre especificado');
    END IF;

    -- Validar que el código no exista (case-insensitive)
    IF EXISTS (
        SELECT 1 FROM ayudantia.tipo_estado_evidencia
        WHERE LOWER(TRIM(codigo)) = LOWER(TRIM(p_codigo))
    ) THEN
        RETURN jsonb_build_object('exito', false, 'mensaje', 'Ya existe un estado de evidencia con el código especificado');
    END IF;

    -- Insertar el nuevo registro
    INSERT INTO ayudantia.tipo_estado_evidencia (nombre_estado, descripcion, codigo, activo)
    VALUES (TRIM(p_nombre_estado), TRIM(p_descripcion), UPPER(TRIM(p_codigo)), TRUE)
    RETURNING id_tipo_estado_evidencia INTO v_id;

    RETURN jsonb_build_object(
        'exito', true,
        'mensaje', 'Estado de evidencia creado exitosamente',
        'id', v_id
    );

EXCEPTION
    WHEN unique_violation THEN
        RETURN jsonb_build_object('exito', false, 'mensaje', 'Error de duplicidad: el nombre o código ya existe');
    WHEN OTHERS THEN
        RETURN jsonb_build_object('exito', false, 'mensaje', 'Error al crear estado de evidencia: ' || SQLERRM);
END;
$function$;

-- 4.2 UPDATE - Actualizar tipo de estado de evidencia
CREATE OR REPLACE FUNCTION ayudantia.fn_actualizar_tipo_estado_evidencia(
    p_id INTEGER,
    p_nombre_estado VARCHAR(50),
    p_descripcion TEXT,
    p_codigo VARCHAR(25)
)
RETURNS JSONB
LANGUAGE plpgsql
SECURITY DEFINER
AS $function$
BEGIN
    -- Validar que el registro exista
    IF NOT EXISTS (
        SELECT 1 FROM ayudantia.tipo_estado_evidencia
        WHERE id_tipo_estado_evidencia = p_id
    ) THEN
        RETURN jsonb_build_object('exito', false, 'mensaje', 'No se encontró el estado de evidencia con el ID especificado');
    END IF;

    -- Validar que el nombre no exista en otro registro (case-insensitive)
    IF EXISTS (
        SELECT 1 FROM ayudantia.tipo_estado_evidencia
        WHERE LOWER(TRIM(nombre_estado)) = LOWER(TRIM(p_nombre_estado))
        AND id_tipo_estado_evidencia != p_id
    ) THEN
        RETURN jsonb_build_object('exito', false, 'mensaje', 'Ya existe otro estado de evidencia con el nombre especificado');
    END IF;

    -- Validar que el código no exista en otro registro (case-insensitive)
    IF EXISTS (
        SELECT 1 FROM ayudantia.tipo_estado_evidencia
        WHERE LOWER(TRIM(codigo)) = LOWER(TRIM(p_codigo))
        AND id_tipo_estado_evidencia != p_id
    ) THEN
        RETURN jsonb_build_object('exito', false, 'mensaje', 'Ya existe otro estado de evidencia con el código especificado');
    END IF;

    -- Actualizar el registro
    UPDATE ayudantia.tipo_estado_evidencia
    SET nombre_estado = TRIM(p_nombre_estado),
        descripcion = TRIM(p_descripcion),
        codigo = UPPER(TRIM(p_codigo))
    WHERE id_tipo_estado_evidencia = p_id;

    RETURN jsonb_build_object(
        'exito', true,
        'mensaje', 'Estado de evidencia actualizado exitosamente',
        'id', p_id
    );

EXCEPTION
    WHEN unique_violation THEN
        RETURN jsonb_build_object('exito', false, 'mensaje', 'Error de duplicidad: el nombre o código ya existe');
    WHEN OTHERS THEN
        RETURN jsonb_build_object('exito', false, 'mensaje', 'Error al actualizar estado de evidencia: ' || SQLERRM);
END;
$function$;

-- 4.3 DELETE (Lógico) - Desactivar tipo de estado de evidencia
CREATE OR REPLACE FUNCTION ayudantia.fn_eliminar_tipo_estado_evidencia(
    p_id INTEGER
)
RETURNS JSONB
LANGUAGE plpgsql
SECURITY DEFINER
AS $function$
BEGIN
    -- Validar que el registro exista
    IF NOT EXISTS (
        SELECT 1 FROM ayudantia.tipo_estado_evidencia
        WHERE id_tipo_estado_evidencia = p_id
    ) THEN
        RETURN jsonb_build_object('exito', false, 'mensaje', 'No se encontró el estado de evidencia con el ID especificado');
    END IF;

    -- Eliminación lógica: setear activo = false
    UPDATE ayudantia.tipo_estado_evidencia
    SET activo = FALSE
    WHERE id_tipo_estado_evidencia = p_id;

    RETURN jsonb_build_object(
        'exito', true,
        'mensaje', 'Estado de evidencia desactivado exitosamente',
        'id', p_id
    );

EXCEPTION
    WHEN OTHERS THEN
        RETURN jsonb_build_object('exito', false, 'mensaje', 'Error al desactivar estado de evidencia: ' || SQLERRM);
END;
$function$;

-- 4.4 SELECT - Listar tipos de estado de evidencia activos
CREATE OR REPLACE FUNCTION ayudantia.fn_listar_tipo_estado_evidencia()
RETURNS JSONB
LANGUAGE plpgsql
SECURITY DEFINER
AS $function$
DECLARE
    v_resultado JSONB;
BEGIN
    SELECT COALESCE(jsonb_agg(
        jsonb_build_object(
            'id', id_tipo_estado_evidencia,
            'nombre_estado', nombre_estado,
            'descripcion', descripcion,
            'codigo', codigo,
            'activo', activo
        ) ORDER BY nombre_estado
    ), '[]'::jsonb)
    INTO v_resultado
    FROM ayudantia.tipo_estado_evidencia
    WHERE activo = TRUE;

    RETURN jsonb_build_object(
        'exito', true,
        'datos', v_resultado
    );

EXCEPTION
    WHEN OTHERS THEN
        RETURN jsonb_build_object('exito', false, 'mensaje', 'Error al listar estados de evidencia: ' || SQLERRM);
END;
$function$;


-- ═══════════════════════════════════════════════════════════════════════════════════════════════════
-- 5. FUNCIONES CRUD PARA: ayudantia.tipo_evidencia
-- Nota: Esta tabla tiene campos: nombre, extension_permitida, codigo, activo
-- ═══════════════════════════════════════════════════════════════════════════════════════════════════

-- 5.1 INSERT - Crear tipo de evidencia
CREATE OR REPLACE FUNCTION ayudantia.fn_crear_tipo_evidencia(
    p_nombre VARCHAR(50),
    p_extension_permitida VARCHAR(10),
    p_codigo VARCHAR(25)
)
RETURNS JSONB
LANGUAGE plpgsql
SECURITY DEFINER
AS $function$
DECLARE
    v_id INTEGER;
BEGIN
    -- Validar que el nombre no exista (case-insensitive)
    IF EXISTS (
        SELECT 1 FROM ayudantia.tipo_evidencia
        WHERE LOWER(TRIM(nombre)) = LOWER(TRIM(p_nombre))
    ) THEN
        RETURN jsonb_build_object('exito', false, 'mensaje', 'Ya existe un tipo de evidencia con el nombre especificado');
    END IF;

    -- Validar que el código no exista (case-insensitive)
    IF EXISTS (
        SELECT 1 FROM ayudantia.tipo_evidencia
        WHERE LOWER(TRIM(codigo)) = LOWER(TRIM(p_codigo))
    ) THEN
        RETURN jsonb_build_object('exito', false, 'mensaje', 'Ya existe un tipo de evidencia con el código especificado');
    END IF;

    -- Insertar el nuevo registro
    INSERT INTO ayudantia.tipo_evidencia (nombre, extension_permitida, codigo, activo)
    VALUES (TRIM(p_nombre), LOWER(TRIM(p_extension_permitida)), UPPER(TRIM(p_codigo)), TRUE)
    RETURNING id_tipo_evidencia INTO v_id;

    RETURN jsonb_build_object(
        'exito', true,
        'mensaje', 'Tipo de evidencia creado exitosamente',
        'id', v_id
    );

EXCEPTION
    WHEN unique_violation THEN
        RETURN jsonb_build_object('exito', false, 'mensaje', 'Error de duplicidad: el nombre o código ya existe');
    WHEN OTHERS THEN
        RETURN jsonb_build_object('exito', false, 'mensaje', 'Error al crear tipo de evidencia: ' || SQLERRM);
END;
$function$;

-- 4.2 UPDATE - Actualizar tipo de evidencia
CREATE OR REPLACE FUNCTION ayudantia.fn_actualizar_tipo_evidencia(
    p_id INTEGER,
    p_nombre VARCHAR(50),
    p_extension_permitida VARCHAR(10),
    p_codigo VARCHAR(25)
)
RETURNS JSONB
LANGUAGE plpgsql
SECURITY DEFINER
AS $function$
BEGIN
    -- Validar que el registro exista
    IF NOT EXISTS (
        SELECT 1 FROM ayudantia.tipo_evidencia
        WHERE id_tipo_evidencia = p_id
    ) THEN
        RETURN jsonb_build_object('exito', false, 'mensaje', 'No se encontró el tipo de evidencia con el ID especificado');
    END IF;

    -- Validar que el nombre no exista en otro registro (case-insensitive)
    IF EXISTS (
        SELECT 1 FROM ayudantia.tipo_evidencia
        WHERE LOWER(TRIM(nombre)) = LOWER(TRIM(p_nombre))
        AND id_tipo_evidencia != p_id
    ) THEN
        RETURN jsonb_build_object('exito', false, 'mensaje', 'Ya existe otro tipo de evidencia con el nombre especificado');
    END IF;

    -- Validar que el código no exista en otro registro (case-insensitive)
    IF EXISTS (
        SELECT 1 FROM ayudantia.tipo_evidencia
        WHERE LOWER(TRIM(codigo)) = LOWER(TRIM(p_codigo))
        AND id_tipo_evidencia != p_id
    ) THEN
        RETURN jsonb_build_object('exito', false, 'mensaje', 'Ya existe otro tipo de evidencia con el código especificado');
    END IF;

    -- Actualizar el registro
    UPDATE ayudantia.tipo_evidencia
    SET nombre = TRIM(p_nombre),
        extension_permitida = LOWER(TRIM(p_extension_permitida)),
        codigo = UPPER(TRIM(p_codigo))
    WHERE id_tipo_evidencia = p_id;

    RETURN jsonb_build_object(
        'exito', true,
        'mensaje', 'Tipo de evidencia actualizado exitosamente',
        'id', p_id
    );

EXCEPTION
    WHEN unique_violation THEN
        RETURN jsonb_build_object('exito', false, 'mensaje', 'Error de duplicidad: el nombre o código ya existe');
    WHEN OTHERS THEN
        RETURN jsonb_build_object('exito', false, 'mensaje', 'Error al actualizar tipo de evidencia: ' || SQLERRM);
END;
$function$;

-- 4.3 DELETE (Lógico) - Desactivar tipo de evidencia
CREATE OR REPLACE FUNCTION ayudantia.fn_eliminar_tipo_evidencia(
    p_id INTEGER
)
RETURNS JSONB
LANGUAGE plpgsql
SECURITY DEFINER
AS $function$
BEGIN
    -- Validar que el registro exista
    IF NOT EXISTS (
        SELECT 1 FROM ayudantia.tipo_evidencia
        WHERE id_tipo_evidencia = p_id
    ) THEN
        RETURN jsonb_build_object('exito', false, 'mensaje', 'No se encontró el tipo de evidencia con el ID especificado');
    END IF;

    -- Eliminación lógica: setear activo = false
    UPDATE ayudantia.tipo_evidencia
    SET activo = FALSE
    WHERE id_tipo_evidencia = p_id;

    RETURN jsonb_build_object(
        'exito', true,
        'mensaje', 'Tipo de evidencia desactivado exitosamente',
        'id', p_id
    );

EXCEPTION
    WHEN OTHERS THEN
        RETURN jsonb_build_object('exito', false, 'mensaje', 'Error al desactivar tipo de evidencia: ' || SQLERRM);
END;
$function$;

-- 4.4 SELECT - Listar tipos de evidencia activos
CREATE OR REPLACE FUNCTION ayudantia.fn_listar_tipo_evidencia()
RETURNS JSONB
LANGUAGE plpgsql
SECURITY DEFINER
AS $function$
DECLARE
    v_resultado JSONB;
BEGIN
    SELECT COALESCE(jsonb_agg(
        jsonb_build_object(
            'id', id_tipo_evidencia,
            'nombre', nombre,
            'extension_permitida', extension_permitida,
            'codigo', codigo,
            'activo', activo
        ) ORDER BY nombre
    ), '[]'::jsonb)
    INTO v_resultado
    FROM ayudantia.tipo_evidencia
    WHERE activo = TRUE;

    RETURN jsonb_build_object(
        'exito', true,
        'datos', v_resultado
    );

EXCEPTION
    WHEN OTHERS THEN
        RETURN jsonb_build_object('exito', false, 'mensaje', 'Error al listar tipos de evidencia: ' || SQLERRM);
END;
$function$;


-- ═══════════════════════════════════════════════════════════════════════════════════════════════════
-- 6. FUNCIONES CRUD PARA: convocatoria.tipo_estado_requisito
-- ═══════════════════════════════════════════════════════════════════════════════════════════════════

-- 6.1 INSERT - Crear tipo de estado de requisito
CREATE OR REPLACE FUNCTION convocatoria.fn_crear_tipo_estado_requisito(
    p_nombre_estado VARCHAR(50),
    p_descripcion TEXT,
    p_codigo VARCHAR(25)
)
RETURNS JSONB
LANGUAGE plpgsql
SECURITY DEFINER
AS $function$
DECLARE
    v_id INTEGER;
BEGIN
    -- Validar que el nombre no exista (case-insensitive)
    IF EXISTS (
        SELECT 1 FROM convocatoria.tipo_estado_requisito
        WHERE LOWER(TRIM(nombre_estado)) = LOWER(TRIM(p_nombre_estado))
    ) THEN
        RETURN jsonb_build_object('exito', false, 'mensaje', 'Ya existe un estado de requisito con el nombre especificado');
    END IF;

    -- Validar que el código no exista (case-insensitive)
    IF EXISTS (
        SELECT 1 FROM convocatoria.tipo_estado_requisito
        WHERE LOWER(TRIM(codigo)) = LOWER(TRIM(p_codigo))
    ) THEN
        RETURN jsonb_build_object('exito', false, 'mensaje', 'Ya existe un estado de requisito con el código especificado');
    END IF;

    -- Insertar el nuevo registro
    INSERT INTO convocatoria.tipo_estado_requisito (nombre_estado, descripcion, codigo, activo)
    VALUES (TRIM(p_nombre_estado), TRIM(p_descripcion), UPPER(TRIM(p_codigo)), TRUE)
    RETURNING id_tipo_estado_requisito INTO v_id;

    RETURN jsonb_build_object(
        'exito', true,
        'mensaje', 'Estado de requisito creado exitosamente',
        'id', v_id
    );

EXCEPTION
    WHEN unique_violation THEN
        RETURN jsonb_build_object('exito', false, 'mensaje', 'Error de duplicidad: el nombre o código ya existe');
    WHEN OTHERS THEN
        RETURN jsonb_build_object('exito', false, 'mensaje', 'Error al crear estado de requisito: ' || SQLERRM);
END;
$function$;

-- 6.2 UPDATE - Actualizar tipo de estado de requisito
CREATE OR REPLACE FUNCTION convocatoria.fn_actualizar_tipo_estado_requisito(
    p_id INTEGER,
    p_nombre_estado VARCHAR(50),
    p_descripcion TEXT,
    p_codigo VARCHAR(25)
)
RETURNS JSONB
LANGUAGE plpgsql
SECURITY DEFINER
AS $function$
BEGIN
    -- Validar que el registro exista
    IF NOT EXISTS (
        SELECT 1 FROM convocatoria.tipo_estado_requisito
        WHERE id_tipo_estado_requisito = p_id
    ) THEN
        RETURN jsonb_build_object('exito', false, 'mensaje', 'No se encontró el estado de requisito con el ID especificado');
    END IF;

    -- Validar que el nombre no exista en otro registro (case-insensitive)
    IF EXISTS (
        SELECT 1 FROM convocatoria.tipo_estado_requisito
        WHERE LOWER(TRIM(nombre_estado)) = LOWER(TRIM(p_nombre_estado))
        AND id_tipo_estado_requisito != p_id
    ) THEN
        RETURN jsonb_build_object('exito', false, 'mensaje', 'Ya existe otro estado de requisito con el nombre especificado');
    END IF;

    -- Validar que el código no exista en otro registro (case-insensitive)
    IF EXISTS (
        SELECT 1 FROM convocatoria.tipo_estado_requisito
        WHERE LOWER(TRIM(codigo)) = LOWER(TRIM(p_codigo))
        AND id_tipo_estado_requisito != p_id
    ) THEN
        RETURN jsonb_build_object('exito', false, 'mensaje', 'Ya existe otro estado de requisito con el código especificado');
    END IF;

    -- Actualizar el registro
    UPDATE convocatoria.tipo_estado_requisito
    SET nombre_estado = TRIM(p_nombre_estado),
        descripcion = TRIM(p_descripcion),
        codigo = UPPER(TRIM(p_codigo))
    WHERE id_tipo_estado_requisito = p_id;

    RETURN jsonb_build_object(
        'exito', true,
        'mensaje', 'Estado de requisito actualizado exitosamente',
        'id', p_id
    );

EXCEPTION
    WHEN unique_violation THEN
        RETURN jsonb_build_object('exito', false, 'mensaje', 'Error de duplicidad: el nombre o código ya existe');
    WHEN OTHERS THEN
        RETURN jsonb_build_object('exito', false, 'mensaje', 'Error al actualizar estado de requisito: ' || SQLERRM);
END;
$function$;

-- 6.3 DELETE (Lógico) - Desactivar tipo de estado de requisito
CREATE OR REPLACE FUNCTION convocatoria.fn_eliminar_tipo_estado_requisito(
    p_id INTEGER
)
RETURNS JSONB
LANGUAGE plpgsql
SECURITY DEFINER
AS $function$
BEGIN
    -- Validar que el registro exista
    IF NOT EXISTS (
        SELECT 1 FROM convocatoria.tipo_estado_requisito
        WHERE id_tipo_estado_requisito = p_id
    ) THEN
        RETURN jsonb_build_object('exito', false, 'mensaje', 'No se encontró el estado de requisito con el ID especificado');
    END IF;

    -- Eliminación lógica: setear activo = false
    UPDATE convocatoria.tipo_estado_requisito
    SET activo = FALSE
    WHERE id_tipo_estado_requisito = p_id;

    RETURN jsonb_build_object(
        'exito', true,
        'mensaje', 'Estado de requisito desactivado exitosamente',
        'id', p_id
    );

EXCEPTION
    WHEN OTHERS THEN
        RETURN jsonb_build_object('exito', false, 'mensaje', 'Error al desactivar estado de requisito: ' || SQLERRM);
END;
$function$;

-- 6.4 SELECT - Listar tipos de estado de requisito activos
CREATE OR REPLACE FUNCTION convocatoria.fn_listar_tipo_estado_requisito()
RETURNS JSONB
LANGUAGE plpgsql
SECURITY DEFINER
AS $function$
DECLARE
    v_resultado JSONB;
BEGIN
    SELECT COALESCE(jsonb_agg(
        jsonb_build_object(
            'id', id_tipo_estado_requisito,
            'nombre_estado', nombre_estado,
            'descripcion', descripcion,
            'codigo', codigo,
            'activo', activo
        ) ORDER BY nombre_estado
    ), '[]'::jsonb)
    INTO v_resultado
    FROM convocatoria.tipo_estado_requisito
    WHERE activo = TRUE;

    RETURN jsonb_build_object(
        'exito', true,
        'datos', v_resultado
    );

EXCEPTION
    WHEN OTHERS THEN
        RETURN jsonb_build_object('exito', false, 'mensaje', 'Error al listar estados de requisito: ' || SQLERRM);
END;
$function$;


-- ═══════════════════════════════════════════════════════════════════════════════════════════════════
-- 7. FUNCIONES CRUD PARA: planificacion.tipo_fase
-- ═══════════════════════════════════════════════════════════════════════════════════════════════════

-- 7.1 INSERT - Crear tipo de fase
CREATE OR REPLACE FUNCTION planificacion.fn_crear_tipo_fase(
    p_codigo VARCHAR(25),
    p_nombre VARCHAR(120),
    p_descripcion TEXT,
    p_orden INTEGER
)
RETURNS JSONB
LANGUAGE plpgsql
SECURITY DEFINER
AS $function$
DECLARE
    v_id INTEGER;
BEGIN
    -- Validar que el orden sea positivo
    IF p_orden <= 0 THEN
        RETURN jsonb_build_object('exito', false, 'mensaje', 'El orden debe ser un número positivo mayor a cero');
    END IF;

    -- Validar que el código no exista (case-insensitive)
    IF EXISTS (
        SELECT 1 FROM planificacion.tipo_fase
        WHERE LOWER(TRIM(codigo)) = LOWER(TRIM(p_codigo))
    ) THEN
        RETURN jsonb_build_object('exito', false, 'mensaje', 'Ya existe una fase con el código especificado');
    END IF;

    -- Validar que el nombre no exista (case-insensitive)
    IF EXISTS (
        SELECT 1 FROM planificacion.tipo_fase
        WHERE LOWER(TRIM(nombre)) = LOWER(TRIM(p_nombre))
    ) THEN
        RETURN jsonb_build_object('exito', false, 'mensaje', 'Ya existe una fase con el nombre especificado');
    END IF;

    -- Validar que el orden no exista
    IF EXISTS (
        SELECT 1 FROM planificacion.tipo_fase
        WHERE orden = p_orden
    ) THEN
        RETURN jsonb_build_object('exito', false, 'mensaje', 'Ya existe una fase con el orden especificado');
    END IF;

    -- Insertar el nuevo registro
    INSERT INTO planificacion.tipo_fase (codigo, nombre, descripcion, orden, activo)
    VALUES (UPPER(TRIM(p_codigo)), TRIM(p_nombre), TRIM(p_descripcion), p_orden, TRUE)
    RETURNING id_tipo_fase INTO v_id;

    RETURN jsonb_build_object(
        'exito', true,
        'mensaje', 'Tipo de fase creado exitosamente',
        'id', v_id
    );

EXCEPTION
    WHEN unique_violation THEN
        RETURN jsonb_build_object('exito', false, 'mensaje', 'Error de duplicidad: el código, nombre u orden ya existe');
    WHEN check_violation THEN
        RETURN jsonb_build_object('exito', false, 'mensaje', 'El orden debe ser un número positivo mayor a cero');
    WHEN OTHERS THEN
        RETURN jsonb_build_object('exito', false, 'mensaje', 'Error al crear tipo de fase: ' || SQLERRM);
END;
$function$;

-- 7.2 UPDATE - Actualizar tipo de fase
CREATE OR REPLACE FUNCTION planificacion.fn_actualizar_tipo_fase(
    p_id INTEGER,
    p_codigo VARCHAR(25),
    p_nombre VARCHAR(120),
    p_descripcion TEXT,
    p_orden INTEGER
)
RETURNS JSONB
LANGUAGE plpgsql
SECURITY DEFINER
AS $function$
BEGIN
    -- Validar que el orden sea positivo
    IF p_orden <= 0 THEN
        RETURN jsonb_build_object('exito', false, 'mensaje', 'El orden debe ser un número positivo mayor a cero');
    END IF;

    -- Validar que el registro exista
    IF NOT EXISTS (
        SELECT 1 FROM planificacion.tipo_fase
        WHERE id_tipo_fase = p_id
    ) THEN
        RETURN jsonb_build_object('exito', false, 'mensaje', 'No se encontró el tipo de fase con el ID especificado');
    END IF;

    -- Validar que el código no exista en otro registro (case-insensitive)
    IF EXISTS (
        SELECT 1 FROM planificacion.tipo_fase
        WHERE LOWER(TRIM(codigo)) = LOWER(TRIM(p_codigo))
        AND id_tipo_fase != p_id
    ) THEN
        RETURN jsonb_build_object('exito', false, 'mensaje', 'Ya existe otra fase con el código especificado');
    END IF;

    -- Validar que el nombre no exista en otro registro (case-insensitive)
    IF EXISTS (
        SELECT 1 FROM planificacion.tipo_fase
        WHERE LOWER(TRIM(nombre)) = LOWER(TRIM(p_nombre))
        AND id_tipo_fase != p_id
    ) THEN
        RETURN jsonb_build_object('exito', false, 'mensaje', 'Ya existe otra fase con el nombre especificado');
    END IF;

    -- Validar que el orden no exista en otro registro
    IF EXISTS (
        SELECT 1 FROM planificacion.tipo_fase
        WHERE orden = p_orden
        AND id_tipo_fase != p_id
    ) THEN
        RETURN jsonb_build_object('exito', false, 'mensaje', 'Ya existe otra fase con el orden especificado');
    END IF;

    -- Actualizar el registro
    UPDATE planificacion.tipo_fase
    SET codigo = UPPER(TRIM(p_codigo)),
        nombre = TRIM(p_nombre),
        descripcion = TRIM(p_descripcion),
        orden = p_orden
    WHERE id_tipo_fase = p_id;

    RETURN jsonb_build_object(
        'exito', true,
        'mensaje', 'Tipo de fase actualizado exitosamente',
        'id', p_id
    );

EXCEPTION
    WHEN unique_violation THEN
        RETURN jsonb_build_object('exito', false, 'mensaje', 'Error de duplicidad: el código, nombre u orden ya existe');
    WHEN check_violation THEN
        RETURN jsonb_build_object('exito', false, 'mensaje', 'El orden debe ser un número positivo mayor a cero');
    WHEN OTHERS THEN
        RETURN jsonb_build_object('exito', false, 'mensaje', 'Error al actualizar tipo de fase: ' || SQLERRM);
END;
$function$;

-- 7.3 DELETE (Lógico) - Desactivar tipo de fase
CREATE OR REPLACE FUNCTION planificacion.fn_eliminar_tipo_fase(
    p_id INTEGER
)
RETURNS JSONB
LANGUAGE plpgsql
SECURITY DEFINER
AS $function$
BEGIN
    -- Validar que el registro exista
    IF NOT EXISTS (
        SELECT 1 FROM planificacion.tipo_fase
        WHERE id_tipo_fase = p_id
    ) THEN
        RETURN jsonb_build_object('exito', false, 'mensaje', 'No se encontró el tipo de fase con el ID especificado');
    END IF;

    -- Eliminación lógica: setear activo = false
    UPDATE planificacion.tipo_fase
    SET activo = FALSE
    WHERE id_tipo_fase = p_id;

    RETURN jsonb_build_object(
        'exito', true,
        'mensaje', 'Tipo de fase desactivado exitosamente',
        'id', p_id
    );

EXCEPTION
    WHEN OTHERS THEN
        RETURN jsonb_build_object('exito', false, 'mensaje', 'Error al desactivar tipo de fase: ' || SQLERRM);
END;
$function$;

-- 7.4 SELECT - Listar tipos de fase activos
CREATE OR REPLACE FUNCTION planificacion.fn_listar_tipo_fase()
RETURNS JSONB
LANGUAGE plpgsql
SECURITY DEFINER
AS $function$
DECLARE
    v_resultado JSONB;
BEGIN
    SELECT COALESCE(jsonb_agg(
        jsonb_build_object(
            'id', id_tipo_fase,
            'codigo', codigo,
            'nombre', nombre,
            'descripcion', descripcion,
            'orden', orden,
            'activo', activo
        ) ORDER BY orden
    ), '[]'::jsonb)
    INTO v_resultado
    FROM planificacion.tipo_fase
    WHERE activo = TRUE;

    RETURN jsonb_build_object(
        'exito', true,
        'datos', v_resultado
    );

EXCEPTION
    WHEN OTHERS THEN
        RETURN jsonb_build_object('exito', false, 'mensaje', 'Error al listar tipos de fase: ' || SQLERRM);
END;
$function$;


-- ═══════════════════════════════════════════════════════════════════════════════════════════════════
-- 8. FUNCIONES CRUD PARA: postulacion.tipo_estado_postulacion
-- ═══════════════════════════════════════════════════════════════════════════════════════════════════

-- 8.1 INSERT - Crear tipo de estado de postulación
CREATE OR REPLACE FUNCTION postulacion.fn_crear_tipo_estado_postulacion(
    p_codigo VARCHAR(25),
    p_nombre VARCHAR(100),
    p_descripcion VARCHAR(255)
)
RETURNS JSONB
LANGUAGE plpgsql
SECURITY DEFINER
AS $function$
DECLARE
    v_id INTEGER;
BEGIN
    -- Validar que el código no exista (case-insensitive)
    IF EXISTS (
        SELECT 1 FROM postulacion.tipo_estado_postulacion
        WHERE LOWER(TRIM(codigo)) = LOWER(TRIM(p_codigo))
    ) THEN
        RETURN jsonb_build_object('exito', false, 'mensaje', 'Ya existe un estado de postulación con el código especificado');
    END IF;

    -- Validar que el nombre no exista (case-insensitive)
    IF EXISTS (
        SELECT 1 FROM postulacion.tipo_estado_postulacion
        WHERE LOWER(TRIM(nombre)) = LOWER(TRIM(p_nombre))
    ) THEN
        RETURN jsonb_build_object('exito', false, 'mensaje', 'Ya existe un estado de postulación con el nombre especificado');
    END IF;

    -- Insertar el nuevo registro
    INSERT INTO postulacion.tipo_estado_postulacion (codigo, nombre, descripcion, activo, fecha_creacion)
    VALUES (UPPER(TRIM(p_codigo)), TRIM(p_nombre), TRIM(p_descripcion), TRUE, NOW())
    RETURNING id_tipo_estado_postulacion INTO v_id;

    RETURN jsonb_build_object(
        'exito', true,
        'mensaje', 'Estado de postulación creado exitosamente',
        'id', v_id
    );

EXCEPTION
    WHEN unique_violation THEN
        RETURN jsonb_build_object('exito', false, 'mensaje', 'Error de duplicidad: el código ya existe');
    WHEN OTHERS THEN
        RETURN jsonb_build_object('exito', false, 'mensaje', 'Error al crear estado de postulación: ' || SQLERRM);
END;
$function$;

-- 8.2 UPDATE - Actualizar tipo de estado de postulación
CREATE OR REPLACE FUNCTION postulacion.fn_actualizar_tipo_estado_postulacion(
    p_id INTEGER,
    p_codigo VARCHAR(25),
    p_nombre VARCHAR(100),
    p_descripcion VARCHAR(255)
)
RETURNS JSONB
LANGUAGE plpgsql
SECURITY DEFINER
AS $function$
BEGIN
    -- Validar que el registro exista
    IF NOT EXISTS (
        SELECT 1 FROM postulacion.tipo_estado_postulacion
        WHERE id_tipo_estado_postulacion = p_id
    ) THEN
        RETURN jsonb_build_object('exito', false, 'mensaje', 'No se encontró el estado de postulación con el ID especificado');
    END IF;

    -- Validar que el código no exista en otro registro (case-insensitive)
    IF EXISTS (
        SELECT 1 FROM postulacion.tipo_estado_postulacion
        WHERE LOWER(TRIM(codigo)) = LOWER(TRIM(p_codigo))
        AND id_tipo_estado_postulacion != p_id
    ) THEN
        RETURN jsonb_build_object('exito', false, 'mensaje', 'Ya existe otro estado de postulación con el código especificado');
    END IF;

    -- Validar que el nombre no exista en otro registro (case-insensitive)
    IF EXISTS (
        SELECT 1 FROM postulacion.tipo_estado_postulacion
        WHERE LOWER(TRIM(nombre)) = LOWER(TRIM(p_nombre))
        AND id_tipo_estado_postulacion != p_id
    ) THEN
        RETURN jsonb_build_object('exito', false, 'mensaje', 'Ya existe otro estado de postulación con el nombre especificado');
    END IF;

    -- Actualizar el registro
    UPDATE postulacion.tipo_estado_postulacion
    SET codigo = UPPER(TRIM(p_codigo)),
        nombre = TRIM(p_nombre),
        descripcion = TRIM(p_descripcion)
    WHERE id_tipo_estado_postulacion = p_id;

    RETURN jsonb_build_object(
        'exito', true,
        'mensaje', 'Estado de postulación actualizado exitosamente',
        'id', p_id
    );

EXCEPTION
    WHEN unique_violation THEN
        RETURN jsonb_build_object('exito', false, 'mensaje', 'Error de duplicidad: el código ya existe');
    WHEN OTHERS THEN
        RETURN jsonb_build_object('exito', false, 'mensaje', 'Error al actualizar estado de postulación: ' || SQLERRM);
END;
$function$;

-- 8.3 DELETE (Lógico) - Desactivar tipo de estado de postulación
CREATE OR REPLACE FUNCTION postulacion.fn_eliminar_tipo_estado_postulacion(
    p_id INTEGER
)
RETURNS JSONB
LANGUAGE plpgsql
SECURITY DEFINER
AS $function$
BEGIN
    -- Validar que el registro exista
    IF NOT EXISTS (
        SELECT 1 FROM postulacion.tipo_estado_postulacion
        WHERE id_tipo_estado_postulacion = p_id
    ) THEN
        RETURN jsonb_build_object('exito', false, 'mensaje', 'No se encontró el estado de postulación con el ID especificado');
    END IF;

    -- Eliminación lógica: setear activo = false
    UPDATE postulacion.tipo_estado_postulacion
    SET activo = FALSE
    WHERE id_tipo_estado_postulacion = p_id;

    RETURN jsonb_build_object(
        'exito', true,
        'mensaje', 'Estado de postulación desactivado exitosamente',
        'id', p_id
    );

EXCEPTION
    WHEN OTHERS THEN
        RETURN jsonb_build_object('exito', false, 'mensaje', 'Error al desactivar estado de postulación: ' || SQLERRM);
END;
$function$;

-- 8.4 SELECT - Listar tipos de estado de postulación activos
CREATE OR REPLACE FUNCTION postulacion.fn_listar_tipo_estado_postulacion()
RETURNS JSONB
LANGUAGE plpgsql
SECURITY DEFINER
AS $function$
DECLARE
    v_resultado JSONB;
BEGIN
    SELECT COALESCE(jsonb_agg(
        jsonb_build_object(
            'id', id_tipo_estado_postulacion,
            'codigo', codigo,
            'nombre', nombre,
            'descripcion', descripcion,
            'activo', activo,
            'fecha_creacion', fecha_creacion
        ) ORDER BY nombre
    ), '[]'::jsonb)
    INTO v_resultado
    FROM postulacion.tipo_estado_postulacion
    WHERE activo = TRUE;

    RETURN jsonb_build_object(
        'exito', true,
        'datos', v_resultado
    );

EXCEPTION
    WHEN OTHERS THEN
        RETURN jsonb_build_object('exito', false, 'mensaje', 'Error al listar estados de postulación: ' || SQLERRM);
END;
$function$;


-- ═══════════════════════════════════════════════════════════════════════════════════════════════════
-- 9. FUNCIONES CRUD PARA: seguridad.privilegio
-- ═══════════════════════════════════════════════════════════════════════════════════════════════════

-- 9.1 INSERT - Crear privilegio
CREATE OR REPLACE FUNCTION seguridad.fn_crear_privilegio(
    p_nombre_privilegio VARCHAR(50),
    p_codigo_interno VARCHAR(1),
    p_descripcion TEXT
)
RETURNS JSONB
LANGUAGE plpgsql
SECURITY DEFINER
AS $function$
DECLARE
    v_id INTEGER;
BEGIN
    -- Validar que el nombre no exista (case-insensitive)
    IF EXISTS (
        SELECT 1 FROM seguridad.privilegio
        WHERE LOWER(TRIM(nombre_privilegio)) = LOWER(TRIM(p_nombre_privilegio))
    ) THEN
        RETURN jsonb_build_object('exito', false, 'mensaje', 'Ya existe un privilegio con el nombre especificado');
    END IF;

    -- Validar que el código interno no exista si está definido (case-insensitive)
    IF p_codigo_interno IS NOT NULL AND EXISTS (
        SELECT 1 FROM seguridad.privilegio
        WHERE LOWER(TRIM(codigo_interno)) = LOWER(TRIM(p_codigo_interno))
    ) THEN
        RETURN jsonb_build_object('exito', false, 'mensaje', 'Ya existe un privilegio con el código interno especificado');
    END IF;

    -- Insertar el nuevo registro
    INSERT INTO seguridad.privilegio (nombre_privilegio, codigo_interno, descripcion, activo)
    VALUES (UPPER(TRIM(p_nombre_privilegio)), UPPER(TRIM(p_codigo_interno)), TRIM(p_descripcion), TRUE)
    RETURNING id_privilegio INTO v_id;

    RETURN jsonb_build_object(
        'exito', true,
        'mensaje', 'Privilegio creado exitosamente',
        'id', v_id
    );

EXCEPTION
    WHEN unique_violation THEN
        RETURN jsonb_build_object('exito', false, 'mensaje', 'Error de duplicidad: el nombre de privilegio ya existe');
    WHEN OTHERS THEN
        RETURN jsonb_build_object('exito', false, 'mensaje', 'Error al crear privilegio: ' || SQLERRM);
END;
$function$;

-- 9.2 UPDATE - Actualizar privilegio
CREATE OR REPLACE FUNCTION seguridad.fn_actualizar_privilegio(
    p_id INTEGER,
    p_nombre_privilegio VARCHAR(50),
    p_codigo_interno VARCHAR(1),
    p_descripcion TEXT
)
RETURNS JSONB
LANGUAGE plpgsql
SECURITY DEFINER
AS $function$
BEGIN
    -- Validar que el registro exista
    IF NOT EXISTS (
        SELECT 1 FROM seguridad.privilegio
        WHERE id_privilegio = p_id
    ) THEN
        RETURN jsonb_build_object('exito', false, 'mensaje', 'No se encontró el privilegio con el ID especificado');
    END IF;

    -- Validar que el nombre no exista en otro registro (case-insensitive)
    IF EXISTS (
        SELECT 1 FROM seguridad.privilegio
        WHERE LOWER(TRIM(nombre_privilegio)) = LOWER(TRIM(p_nombre_privilegio))
        AND id_privilegio != p_id
    ) THEN
        RETURN jsonb_build_object('exito', false, 'mensaje', 'Ya existe otro privilegio con el nombre especificado');
    END IF;

    -- Validar que el código interno no exista en otro registro si está definido (case-insensitive)
    IF p_codigo_interno IS NOT NULL AND EXISTS (
        SELECT 1 FROM seguridad.privilegio
        WHERE LOWER(TRIM(codigo_interno)) = LOWER(TRIM(p_codigo_interno))
        AND id_privilegio != p_id
    ) THEN
        RETURN jsonb_build_object('exito', false, 'mensaje', 'Ya existe otro privilegio con el código interno especificado');
    END IF;

    -- Actualizar el registro
    UPDATE seguridad.privilegio
    SET nombre_privilegio = UPPER(TRIM(p_nombre_privilegio)),
        codigo_interno = UPPER(TRIM(p_codigo_interno)),
        descripcion = TRIM(p_descripcion)
    WHERE id_privilegio = p_id;

    RETURN jsonb_build_object(
        'exito', true,
        'mensaje', 'Privilegio actualizado exitosamente',
        'id', p_id
    );

EXCEPTION
    WHEN unique_violation THEN
        RETURN jsonb_build_object('exito', false, 'mensaje', 'Error de duplicidad: el nombre de privilegio ya existe');
    WHEN OTHERS THEN
        RETURN jsonb_build_object('exito', false, 'mensaje', 'Error al actualizar privilegio: ' || SQLERRM);
END;
$function$;

-- 9.3 DELETE (Lógico) - Desactivar privilegio
CREATE OR REPLACE FUNCTION seguridad.fn_eliminar_privilegio(
    p_id INTEGER
)
RETURNS JSONB
LANGUAGE plpgsql
SECURITY DEFINER
AS $function$
BEGIN
    -- Validar que el registro exista
    IF NOT EXISTS (
        SELECT 1 FROM seguridad.privilegio
        WHERE id_privilegio = p_id
    ) THEN
        RETURN jsonb_build_object('exito', false, 'mensaje', 'No se encontró el privilegio con el ID especificado');
    END IF;

    -- Eliminación lógica: setear activo = false
    UPDATE seguridad.privilegio
    SET activo = FALSE
    WHERE id_privilegio = p_id;

    RETURN jsonb_build_object(
        'exito', true,
        'mensaje', 'Privilegio desactivado exitosamente',
        'id', p_id
    );

EXCEPTION
    WHEN OTHERS THEN
        RETURN jsonb_build_object('exito', false, 'mensaje', 'Error al desactivar privilegio: ' || SQLERRM);
END;
$function$;

-- 9.4 SELECT - Listar privilegios activos
CREATE OR REPLACE FUNCTION seguridad.fn_listar_privilegio()
RETURNS JSONB
LANGUAGE plpgsql
SECURITY DEFINER
AS $function$
DECLARE
    v_resultado JSONB;
BEGIN
    SELECT COALESCE(jsonb_agg(
        jsonb_build_object(
            'id', id_privilegio,
            'nombre_privilegio', nombre_privilegio,
            'codigo_interno', codigo_interno,
            'descripcion', descripcion,
            'activo', activo
        ) ORDER BY nombre_privilegio
    ), '[]'::jsonb)
    INTO v_resultado
    FROM seguridad.privilegio
    WHERE activo = TRUE;

    RETURN jsonb_build_object(
        'exito', true,
        'datos', v_resultado
    );

EXCEPTION
    WHEN OTHERS THEN
        RETURN jsonb_build_object('exito', false, 'mensaje', 'Error al listar privilegios: ' || SQLERRM);
END;
$function$;
