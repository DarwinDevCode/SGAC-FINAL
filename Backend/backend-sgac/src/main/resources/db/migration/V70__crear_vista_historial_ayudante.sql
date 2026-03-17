-- Migración para crear la vista de historial de ayudantía del estudiante
DROP VIEW IF EXISTS ayudantia.v_historial_estudiante;

CREATE VIEW ayudantia.v_historial_estudiante AS
SELECT 
    a.id_ayudantia,                     -- 0
    p.id_postulacion,                   -- 1
    u.nombres || ' ' || u.apellidos AS nombre_estudiante, -- 2
    u.cedula,                           -- 3
    asign.nombre_asignatura,            -- 4
    CAST(NULL AS VARCHAR) AS codigo_asignatura, -- 5 (Columna no existe en DB)
    pa.nombre_periodo,                  -- 6
    pa.fecha_inicio AS inicio_periodo,  -- 7
    pa.fecha_fin AS fin_periodo,        -- 8
    a.fecha_inicio,                     -- 9
    a.fecha_fin,                        -- 10
    a.horas_cumplidas,                  -- 11
    (SELECT COUNT(*)::BIGINT FROM ayudantia.registro_actividad ra WHERE ra.id_ayudantia = a.id_ayudantia) AS total_sesiones, -- 12
    CASE 
        WHEN a.id_tipo_estado_ayudantia = 2 THEN 'APROBADO'
        WHEN a.id_tipo_estado_ayudantia = 3 THEN 'REPROBADO'
        ELSE 'EN_CURSO'
    END AS resultado_final,              -- 13
    tea.nombre_estado AS estado_ayudantia, -- 14
    u.id_usuario                        -- 15 (Filtro)
FROM ayudantia.ayudantia a
JOIN postulacion.postulacion p ON a.id_postulacion = p.id_postulacion
JOIN academico.estudiante e ON p.id_estudiante = e.id_estudiante
JOIN seguridad.usuario u ON e.id_usuario = u.id_usuario
JOIN convocatoria.convocatoria c ON p.id_convocatoria = c.id_convocatoria
JOIN academico.asignatura asign ON c.id_asignatura = asign.id_asignatura
JOIN academico.periodo_academico pa ON c.id_periodo_academico = pa.id_periodo_academico
JOIN ayudantia.tipo_estado_ayudantia tea ON a.id_tipo_estado_ayudantia = tea.id_tipo_estado_ayudantia;
