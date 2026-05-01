CREATE SCHEMA IF NOT EXISTS seguridad;

CREATE TABLE IF NOT EXISTS seguridad.tipo_objeto_seguridad (
id_tipo_objeto_seguridad SERIAL PRIMARY KEY,
nombre_tipo_objeto VARCHAR(50) UNIQUE NOT NULL,
descripcion TEXT
);

CREATE TABLE IF NOT EXISTS seguridad.privilegio (
id_privilegio SERIAL PRIMARY KEY,
nombre_privilegio VARCHAR(50) UNIQUE NOT NULL,
codigo_interno CHAR(1),
descripcion TEXT
);

CREATE TABLE IF NOT EXISTS seguridad.tipo_objeto_seguridad_privilegio (
id_tipo_objeto_seguridad INT NOT NULL,
id_privilegio INT NOT NULL,
PRIMARY KEY (id_tipo_objeto_seguridad, id_privilegio),

CONSTRAINT fk_tipo_objeto FOREIGN KEY (id_tipo_objeto_seguridad)
REFERENCES seguridad.tipo_objeto_seguridad(id_tipo_objeto_seguridad) ON DELETE CASCADE,
CONSTRAINT fk_privilegio FOREIGN KEY (id_privilegio)
REFERENCES seguridad.privilegio(id_privilegio) ON DELETE CASCADE
);