CREATE TABLE ayudantia.tipo_estado_informe (
    id_tipo_estado_informe SERIAL PRIMARY KEY,
    nombre_estado VARCHAR(50) NOT NULL,
    descripcion TEXT,
    activo BOOLEAN DEFAULT true,
    codigo VARCHAR(25) NOT NULL UNIQUE
);

INSERT INTO ayudantia.tipo_estado_informe (nombre_estado, descripcion, codigo) VALUES
('No Iniciado', 'Informe aún no ha sido iniciado por el ayudante', 'NO_INICIADO'),
('En Elaboración', 'Informe en proceso de redacción o con borrador generado', 'EN_ELABORACION'),
('Borrador Generado', 'Borrador generado por IA, pendiente de revisión y envío por el ayudante', 'BORRADOR_GENERADO'),
('En Revisión Docente', 'Informe enviado y pendiente de revisión por el docente', 'EN_REVISION_DOCENTE'),
('En Revisión Coordinador', 'Informe aprobado por docente, pendiente de revisión por el coordinador', 'EN_REVISION_COORDINADOR'),
('En Revisión Decano', 'Informe aprobado por coordinador, pendiente de revisión por el decano', 'EN_REVISION_DECANO'),
('Aprobado', 'Informe aprobado en su totalidad', 'APROBADO'),
('Rezagado', 'Informe no enviado en el plazo establecido', 'REZAGADO');

CREATE TABLE ayudantia.informe_mensual (
    id_informe_mensual SERIAL PRIMARY KEY,
    id_ayudantia INTEGER NOT NULL REFERENCES ayudantia.ayudantia(id_ayudantia),
    id_periodo_academico INTEGER NOT NULL REFERENCES academico.periodo_academico(id_periodo_academico),
    mes INTEGER NOT NULL,
    anio INTEGER NOT NULL,
    contenido_borrador TEXT,
    id_tipo_estado_informe INTEGER NOT NULL REFERENCES ayudantia.tipo_estado_informe(id_tipo_estado_informe),
    fecha_generacion TIMESTAMP,
    fecha_envio TIMESTAMP,
    firma_path VARCHAR(500),
    observaciones TEXT
);
