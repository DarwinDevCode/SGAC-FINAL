-- V53: Permisos para módulo de Informe Mensual
-- Otorga acceso de lectura/escritura sobre las tablas de informe mensual
-- a los roles que necesitan interactuar con ellas en la aplicación.

-- role_ayudante_catedra: crea y envía sus propios informes
GRANT USAGE ON SCHEMA ayudantia TO role_ayudante_catedra;
GRANT SELECT, INSERT, UPDATE ON ayudantia.informe_mensual TO role_ayudante_catedra;
GRANT SELECT ON ayudantia.tipo_estado_informe TO role_ayudante_catedra;
GRANT USAGE, SELECT ON SEQUENCE ayudantia.informe_mensual_id_informe_mensual_seq TO role_ayudante_catedra;

-- role_docente: revisa informes de sus ayudantes
GRANT USAGE ON SCHEMA ayudantia TO role_docente;
GRANT SELECT, UPDATE ON ayudantia.informe_mensual TO role_docente;
GRANT SELECT ON ayudantia.tipo_estado_informe TO role_docente;

-- role_coordinador: revisa informes aprobados por docentes
GRANT USAGE ON SCHEMA ayudantia TO role_coordinador;
GRANT SELECT, UPDATE ON ayudantia.informe_mensual TO role_coordinador;
GRANT SELECT ON ayudantia.tipo_estado_informe TO role_coordinador;

-- role_decano: revisión final
GRANT USAGE ON SCHEMA ayudantia TO role_decano;
GRANT SELECT, UPDATE ON ayudantia.informe_mensual TO role_decano;
GRANT SELECT ON ayudantia.tipo_estado_informe TO role_decano;
