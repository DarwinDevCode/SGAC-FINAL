-- V33: Otorgar permisos al rol coordinador sobre el esquema notificacion
GRANT USAGE ON SCHEMA notificacion TO role_coordinador;
GRANT SELECT, INSERT, UPDATE, DELETE ON notificacion.notificacion TO role_coordinador;
GRANT USAGE, SELECT ON SEQUENCE notificacion.notificacion_id_notificacion_seq TO role_coordinador;
