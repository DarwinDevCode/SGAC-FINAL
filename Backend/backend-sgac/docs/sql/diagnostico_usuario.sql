-- Consulta para diagnosticar el estado del usuario 'jponceo'
SELECT id_usuario, nombres, apellidos, correo, nombre_usuario, contrasenia_usuario, activo 
FROM seguridad.usuario 
WHERE nombre_usuario = 'jponceo';

-- Consulta para verificar roles del usuario
SELECT u.nombre_usuario, tr.nombre_tipo_rol, utr.activo AS rol_activo
FROM seguridad.usuario u
JOIN seguridad.usuario_tipo_rol utr ON u.id_usuario = utr.id_usuario
JOIN seguridad.tipo_rol tr ON utr.id_tipo_rol = tr.id_tipo_rol
WHERE u.nombre_usuario = 'jponceo';
