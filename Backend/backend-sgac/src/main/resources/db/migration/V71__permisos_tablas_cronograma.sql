-- Conceder permiso de lectura (SELECT) en las tablas requeridas por la función
-- planificacion.fn_obtener_cronograma_activo().
-- Aunque la función sea SECURITY DEFINER, algunos entornos de Postgres 
-- o restricciones de herencia de roles requieren el GRANT explícito.

GRANT SELECT ON academico.periodo_academico TO PUBLIC;
GRANT SELECT ON planificacion.periodo_fase TO PUBLIC;
GRANT SELECT ON planificacion.tipo_fase TO PUBLIC;
