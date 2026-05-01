-- ============================================================
--  fn_listar_roles_activos
--  Devuelve todos los roles activos para alimentar el frontend
-- ============================================================
CREATE OR REPLACE FUNCTION seguridad.fn_listar_roles_activos()
    RETURNS JSONB
    LANGUAGE sql
    STABLE
    SECURITY DEFINER
AS $$
SELECT COALESCE(
               jsonb_agg(
                       jsonb_build_object(
                               'idTipoRol',     id_tipo_rol,
                               'nombreTipoRol', nombre_tipo_rol
                       )
                       ORDER BY nombre_tipo_rol
               ),
               '[]'::JSONB
       )
FROM seguridad.tipo_rol
WHERE activo = TRUE;
$$;

-- ============================================================
--  fn_registrar_usuario_global
--  Registro multirrol centralizado. Recibe un JSONB completo
--  y crea el usuario + roles + tablas especializadas + usuario BD.
--
--  Estructura del JSONB esperado:
--  {
--    "nombres":       "Juan Carlos",
--    "apellidos":     "Pérez López",
--    "cedula":        "1234567890",
--    "correo":        "jperez@uteq.edu.ec",
--    "username":      "jperez",
--    "passwordHash":  "$2a$10$...",   -- BCrypt generado en el backend
--    "passwordPlain": "Ab3Xy9Pq2mZk", -- Clave temporal para CREATE USER
--    "rolesIds":      [1, 3],
--    "idCarrera":     2,              -- requerido si ESTUDIANTE o COORDINADOR
--    "matricula":     "2024-A-001",   -- requerido si ESTUDIANTE
--    "semestre":      4,              -- requerido si ESTUDIANTE
--    "idFacultad":    1,              -- requerido si DECANO
--    "horasAyudante": 20.00           -- requerido si AYUDANTE_CATEDRA
--  }
--
--  Retorna JSONB: { exito, mensaje, correo, nombreUsuario, idUsuario }
-- ============================================================
CREATE OR REPLACE FUNCTION seguridad.fn_registrar_usuario_global(p_data JSONB)
    RETURNS JSONB
    LANGUAGE plpgsql
    SECURITY DEFINER
AS $$
DECLARE
    v_id_usuario     INTEGER;
    v_user_lower     VARCHAR(50);
    v_password_hash  VARCHAR(255);
    v_password_plain VARCHAR(100);
    v_rol_record     RECORD;
    v_db_role_name   TEXT;
    v_roles_ids      JSONB;
    v_rol_id         INTEGER;
    v_rol_nombre     VARCHAR(50);
    v_id_carrera     INTEGER;
    v_id_facultad    INTEGER;
    v_matricula      VARCHAR(30);
    v_semestre       INTEGER;
    v_horas_ayudante NUMERIC(5,2);
    v_result         JSONB;
BEGIN
    -- ── Extracción y normalización ───────────────────────────────────
    v_user_lower     := LOWER(TRIM(p_data->>'username'));
    v_password_hash  := p_data->>'passwordHash';
    v_password_plain := p_data->>'passwordPlain';
    v_roles_ids      := p_data->'rolesIds';

    IF v_user_lower IS NULL OR v_user_lower = '' THEN
        RAISE EXCEPTION 'El campo username es obligatorio.';
    END IF;
    IF v_password_hash IS NULL OR v_password_hash = '' THEN
        RAISE EXCEPTION 'El hash de la contraseña es obligatorio.';
    END IF;
    IF v_password_plain IS NULL OR v_password_plain = '' THEN
        RAISE EXCEPTION 'La contraseña en texto plano es obligatoria para crear el usuario de BD.';
    END IF;
    IF v_roles_ids IS NULL OR jsonb_array_length(v_roles_ids) = 0 THEN
        RAISE EXCEPTION 'Debe seleccionar al menos un rol para el usuario.';
    END IF;

    -- ── 1. Insertar en seguridad.usuario ────────────────────────────
    INSERT INTO seguridad.usuario (
        nombres, apellidos, cedula, correo,
        nombre_usuario, contrasenia_usuario, fecha_creacion, activo
    ) VALUES (
                 TRIM(p_data->>'nombres'),
                 TRIM(p_data->>'apellidos'),
                 TRIM(p_data->>'cedula'),
                 LOWER(TRIM(p_data->>'correo')),
                 v_user_lower,
                 v_password_hash,
                 CURRENT_DATE,
                 TRUE
             )
    RETURNING id_usuario INTO v_id_usuario;

    -- ── 2. Crear usuario de base de datos ───────────────────────────
    EXECUTE format('CREATE USER %I WITH PASSWORD %L', v_user_lower, v_password_plain);
    EXECUTE format('GRANT %I TO app_user_default', v_user_lower);

    -- ── 3. Iterar sobre roles ────────────────────────────────────────
    FOR v_rol_id IN
        SELECT jsonb_array_elements_text(v_roles_ids)::INTEGER
        LOOP
            -- Validar que el rol existe y está activo
            SELECT nombre_tipo_rol
            INTO v_rol_nombre
            FROM seguridad.tipo_rol
            WHERE id_tipo_rol = v_rol_id
              AND activo = TRUE;

            IF NOT FOUND THEN
                RAISE EXCEPTION 'El rol con ID % no existe o no está activo.', v_rol_id;
            END IF;

            -- 3a. Relación usuario ↔ tipo_rol
            INSERT INTO seguridad.usuario_tipo_rol (id_usuario, id_tipo_rol, activo, fecha_creacion)
            VALUES (v_id_usuario, v_rol_id, TRUE, CURRENT_TIMESTAMP)
            ON CONFLICT (id_usuario, id_tipo_rol) DO NOTHING;

            -- 3b. Conceder rol de BD (normalizar nombre)
            v_db_role_name := CASE v_rol_nombre
                                  WHEN 'AYUDANTE_CATEDRA' THEN 'role_ayudante_catedra'
                                  ELSE 'role_' || LOWER(v_rol_nombre)
                END;
            EXECUTE format('GRANT %I TO %I', v_db_role_name, v_user_lower);

            -- 3c. Inserciones condicionales en tablas especializadas
            CASE v_rol_nombre

                WHEN 'ESTUDIANTE' THEN
                    v_id_carrera := (p_data->>'idCarrera')::INTEGER;
                    v_matricula  := TRIM(p_data->>'matricula');
                    v_semestre   := (p_data->>'semestre')::INTEGER;

                    IF v_id_carrera IS NULL THEN
                        RAISE EXCEPTION 'Se requiere idCarrera para el rol ESTUDIANTE.';
                    END IF;
                    IF v_matricula IS NULL OR v_matricula = '' THEN
                        RAISE EXCEPTION 'Se requiere matrícula para el rol ESTUDIANTE.';
                    END IF;
                    IF v_semestre IS NULL OR v_semestre < 1 OR v_semestre > 10 THEN
                        RAISE EXCEPTION 'El semestre debe estar entre 1 y 10.';
                    END IF;

                    INSERT INTO academico.estudiante
                    (id_usuario, id_carrera, matricula, semestre, estado_academico)
                    VALUES
                        (v_id_usuario, v_id_carrera, v_matricula, v_semestre, 'ACTIVO');

                WHEN 'DOCENTE' THEN
                    INSERT INTO academico.docente (id_usuario, fecha_inicio, activo)
                    VALUES (v_id_usuario, CURRENT_DATE, TRUE);

                WHEN 'COORDINADOR' THEN
                    v_id_carrera := (p_data->>'idCarrera')::INTEGER;
                    IF v_id_carrera IS NULL THEN
                        RAISE EXCEPTION 'Se requiere idCarrera para el rol COORDINADOR.';
                    END IF;
                    INSERT INTO academico.coordinador (id_usuario, id_carrera, fecha_inicio, activo)
                    VALUES (v_id_usuario, v_id_carrera, CURRENT_DATE, TRUE);

                WHEN 'DECANO' THEN
                    v_id_facultad := (p_data->>'idFacultad')::INTEGER;
                    IF v_id_facultad IS NULL THEN
                        RAISE EXCEPTION 'Se requiere idFacultad para el rol DECANO.';
                    END IF;
                    INSERT INTO academico.decano (id_usuario, id_facultad, fecha_inicio_gestion, activo)
                    VALUES (v_id_usuario, v_id_facultad, CURRENT_DATE, TRUE);

                WHEN 'AYUDANTE_CATEDRA' THEN
                    v_horas_ayudante := (p_data->>'horasAyudante')::NUMERIC(5,2);
                    IF v_horas_ayudante IS NULL OR v_horas_ayudante <= 0 THEN
                        RAISE EXCEPTION 'Se requiere horasAyudante > 0 para el rol AYUDANTE_CATEDRA.';
                    END IF;
                    INSERT INTO academico.ayudante_catedra (id_usuario, horas_ayudante)
                    VALUES (v_id_usuario, v_horas_ayudante);

                WHEN 'ADMINISTRADOR' THEN
                    NULL; -- El administrador no tiene tabla especializada

                ELSE
                    RAISE NOTICE 'Rol % no tiene tabla especializada asociada.', v_rol_nombre;
                END CASE;

        END LOOP;

    -- ── 4. Construir respuesta ───────────────────────────────────────
    v_result := jsonb_build_object(
            'exito',        TRUE,
            'mensaje',      format('Usuario "%s" registrado con %s rol(es).', v_user_lower, jsonb_array_length(v_roles_ids)),
            'correo',       p_data->>'correo',
            'nombreUsuario', v_user_lower,
            'idUsuario',    v_id_usuario
                );

    RAISE NOTICE 'fn_registrar_usuario_global: %', v_result;
    RETURN v_result;

EXCEPTION
    WHEN OTHERS THEN
        RAISE EXCEPTION 'Error al registrar usuario global: % (SQLSTATE: %)', SQLERRM, SQLSTATE;
END;
$$;
