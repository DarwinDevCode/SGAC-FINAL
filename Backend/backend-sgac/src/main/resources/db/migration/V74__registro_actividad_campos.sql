-- =================================================================================
-- V74: Tema tratado, tareas y plan (Bloque 1D - F1.14)
-- Agrega campos al registro_actividad para el informe de la sesion.
-- =================================================================================

ALTER TABLE ayudantia.registro_actividad 
ADD COLUMN IF NOT EXISTS tema_tratado TEXT,
ADD COLUMN IF NOT EXISTS tareas_asignadas TEXT,
ADD COLUMN IF NOT EXISTS plan_proxima_sesion TEXT;
