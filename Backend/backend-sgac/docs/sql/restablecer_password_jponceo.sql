-- Script para restablecer la contraseña del usuario 'jponceo' a '123456'
-- Usando el hash BCrypt correspondiente a '123456'
BEGIN;

UPDATE seguridad.usuario 
SET contrasenia_usuario = '$2a$10$v4lq47Uz76UJLQNqJAgI.OKvVGeXa7CQS.0baIXXT7KcLI0W5ug9C' 
WHERE nombre_usuario = 'jponceo';

COMMIT;

-- Verificar cambio
SELECT nombre_usuario, contrasenia_usuario FROM seguridad.usuario WHERE nombre_usuario = 'jponceo';
