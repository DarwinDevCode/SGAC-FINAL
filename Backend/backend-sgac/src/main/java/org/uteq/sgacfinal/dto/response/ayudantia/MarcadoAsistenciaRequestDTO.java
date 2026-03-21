package org.uteq.sgacfinal.dto.response.ayudantia;

public record MarcadoAsistenciaRequestDTO(
    Integer idUsuario,
    Integer idDetalle,
    Boolean asistio
) {}