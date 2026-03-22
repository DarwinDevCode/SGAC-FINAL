-- Permisos para módulo de Informe Mensual

GRANT USAGE ON SCHEMA ayudantia TO role_ayudante_catedra;
GRANT SELECT, INSERT, UPDATE ON ayudantia.informe_mensual TO role_ayudante_catedra;
GRANT SELECT ON ayudantia.tipo_estado_informe TO role_ayudante_catedra;
GRANT USAGE, SELECT ON SEQUENCE ayudantia.informe_mensual_id_informe_mensual_seq TO role_ayudante_catedra;

GRANT USAGE ON SCHEMA ayudantia TO role_docente;
GRANT SELECT, UPDATE ON ayudantia.informe_mensual TO role_docente;
GRANT SELECT ON ayudantia.tipo_estado_informe TO role_docente;

GRANT USAGE ON SCHEMA ayudantia TO role_coordinador;
GRANT SELECT, UPDATE ON ayudantia.informe_mensual TO role_coordinador;
GRANT SELECT ON ayudantia.tipo_estado_informe TO role_coordinador;

GRANT USAGE ON SCHEMA ayudantia TO role_decano;
GRANT SELECT, UPDATE ON ayudantia.informe_mensual TO role_decano;
GRANT SELECT ON ayudantia.tipo_estado_informe TO role_decano;
