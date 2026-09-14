-- V29: Quitar bloques EXCEPTION silenciosos para depurar errores ocultos 
-- y asegurar permisos de UPDATE en postulación.

CREATE OR REPLACE FUNCTION public.sp_crear_evaluacion_oposicion(
    p_id_postulacion   INTEGER,
    p_tema_exposicion  VARCHAR,
    p_fecha_evaluacion DATE,
    p_hora_inicio      TIME,
    p_hora_fin         TIME,
    p_lugar            VARCHAR,
    p_estado           VARCHAR
)
RETURNS INTEGER AS $$
DECLARE
    v_id INTEGER;
BEGIN
    INSERT INTO postulacion.evaluacion_oposicion
        (id_postulacion, tema_exposicion, fecha_evaluacion, hora_inicio, hora_fin, lugar, estado)
    VALUES
        (p_id_postulacion, p_tema_exposicion, p_fecha_evaluacion, p_hora_inicio, p_hora_fin, p_lugar, p_estado)
    RETURNING id_evaluacion_oposicion INTO v_id;
    
    -- Actualizar estado de la postulación
    UPDATE postulacion.postulacion
    SET estado_postulacion = 'EN_EVALUACION'
    WHERE id_postulacion = p_id_postulacion;
    
    RETURN v_id;
END;
$$ LANGUAGE plpgsql;

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
END;
$$ LANGUAGE plpgsql;

-- Nos aseguramos que el coordinador pueda hacer UPDATE sobre postulación (si no lo tenía),
-- lo cual es requerido por sp_crear_evaluacion_oposicion en la línea 20.
GRANT UPDATE ON TABLE postulacion.postulacion TO role_coordinador;

-- Igualmente validamos los permisos de inserción solo por si acaso
GRANT INSERT ON TABLE postulacion.evaluacion_oposicion TO role_coordinador;
GRANT INSERT ON TABLE seguridad.usuario_comision TO role_coordinador;
