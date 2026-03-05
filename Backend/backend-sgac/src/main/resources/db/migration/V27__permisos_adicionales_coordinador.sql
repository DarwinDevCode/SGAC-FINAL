-- V27: Permisos adicionales requeridos por el coordinador para la asignación de comisiones de evaluación

-- El coordinador necesita consultar las comisiones existentes
GRANT SELECT ON postulacion.comision_seleccion TO role_coordinador;

-- Al listar los integrantes de una comisión (usuario_comision), el ORM (JPA)
-- obtiene o cruza datos con la tabla de usuarios. El coordinador debe tener acceso de lectura a esa tabla.
GRANT SELECT ON seguridad.usuario TO role_coordinador;

-- Es probable que JPA también intente cargar el rol de los usuarios, por lo que aseguramos acceso de lectura a tipo_rol
GRANT SELECT ON seguridad.tipo_rol TO role_coordinador;

-- La entidad Usuario carga los roles de manera eagerly, lo que implica acceso a usuario_tipo_rol
GRANT SELECT ON seguridad.usuario_tipo_rol TO role_coordinador;
