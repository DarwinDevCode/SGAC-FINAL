-- Conceder acceso al esquema planificacion y a la función recursiva del cronograma
-- a todos los roles de la base de datos ya que el cronograma es información pública para cualquier usuario logueado en cualquier rol.

GRANT USAGE ON SCHEMA planificacion TO PUBLIC;

GRANT EXECUTE ON FUNCTION planificacion.fn_obtener_cronograma_activo() TO PUBLIC;
