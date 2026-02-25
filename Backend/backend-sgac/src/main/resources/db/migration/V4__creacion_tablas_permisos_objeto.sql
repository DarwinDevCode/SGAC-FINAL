CREATE TABLE seguridad.tipo_objeto_seguridad (
   id_tipo_objeto_seguridad SERIAL PRIMARY KEY,
   nombre_tipo_objeto VARCHAR(50) UNIQUE NOT NULL,
   descripcion TEXT
);

CREATE TABLE seguridad.privilegio (
    id_privilegio SERIAL PRIMARY KEY,
    nombre_privilegio VARCHAR(50) UNIQUE NOT NULL,
    codigo_interno CHAR(1),
    descripcion TEXT
);

CREATE TABLE seguridad.tipo_objeto_seguridad_privilegio (
    id_tipo_objeto_seguridad INT NOT NULL REFERENCES seguridad.tipo_objeto_seguridad(id_tipo_objeto_seguridad),
    id_privilegio INT NOT NULL REFERENCES seguridad.privilegio(id_privilegio),
    PRIMARY KEY (id_tipo_objeto_seguridad, id_privilegio)
);
