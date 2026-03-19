package org.uteq.sgacfinal.dto.response.ayudantia;

import com.fasterxml.jackson.annotation.JsonAlias;

public record PlanificacionResponseDTO(
    @JsonAlias("id_registro") Integer idRegistro,
    @JsonAlias("detalle_asistencia") SnapshotAsistenciaResponseDTO detalleAsistencia
) {}

