-- V35: Permisos sobre las nuevas tablas del módulo de Evaluación y Selección
-- Roles involucrados: role_coordinador, role_docente, role_decano

-- ── COORDINADOR ─────────────────────────────────────────────────────────────
GRANT SELECT, INSERT, UPDATE ON postulacion.sorteo_oposicion                 TO role_coordinador;
GRANT SELECT, INSERT, UPDATE ON postulacion.calificacion_oposicion_individual TO role_coordinador;
GRANT SELECT, INSERT, UPDATE ON postulacion.resumen_evaluacion                TO role_coordinador;
GRANT SELECT, INSERT, UPDATE ON postulacion.acta_evaluacion                   TO role_coordinador;
GRANT USAGE  ON SEQUENCE postulacion.sorteo_oposicion_id_sorteo_seq          TO role_coordinador;
GRANT USAGE  ON SEQUENCE postulacion.calificacion_oposicion_individual_id_calificacion_seq TO role_coordinador;
GRANT USAGE  ON SEQUENCE postulacion.resumen_evaluacion_id_resumen_seq       TO role_coordinador;
GRANT USAGE  ON SEQUENCE postulacion.acta_evaluacion_id_acta_seq             TO role_coordinador;

-- ── DOCENTE (Secretario) ─────────────────────────────────────────────────────
GRANT SELECT, INSERT, UPDATE ON postulacion.calificacion_oposicion_individual TO role_docente;
GRANT SELECT, INSERT, UPDATE ON postulacion.acta_evaluacion                   TO role_docente;
GRANT SELECT               ON postulacion.sorteo_oposicion                    TO role_docente;
GRANT SELECT               ON postulacion.resumen_evaluacion                  TO role_docente;
GRANT USAGE  ON SEQUENCE postulacion.calificacion_oposicion_individual_id_calificacion_seq TO role_docente;

-- ── DECANO ───────────────────────────────────────────────────────────────────
GRANT SELECT, INSERT, UPDATE ON postulacion.calificacion_oposicion_individual TO role_decano;
GRANT SELECT, UPDATE         ON postulacion.acta_evaluacion                   TO role_decano;
GRANT SELECT                 ON postulacion.sorteo_oposicion                  TO role_decano;
GRANT SELECT                 ON postulacion.resumen_evaluacion                TO role_decano;
GRANT USAGE  ON SEQUENCE postulacion.calificacion_oposicion_individual_id_calificacion_seq TO role_decano;

-- ── Permisos de ejecución en los nuevos SPs ──────────────────────────────────
GRANT EXECUTE ON FUNCTION public.sp_guardar_oposicion_individual(INTEGER,INTEGER,VARCHAR,NUMERIC,NUMERIC,NUMERIC) TO role_coordinador, role_docente, role_decano;
GRANT EXECUTE ON FUNCTION public.sp_calcular_ranking_evaluacion(INTEGER)                                          TO role_coordinador, role_docente, role_decano;
GRANT EXECUTE ON FUNCTION public.sp_confirmar_acta(INTEGER,INTEGER,VARCHAR)                                       TO role_coordinador, role_docente, role_decano;
