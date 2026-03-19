package org.uteq.sgacfinal.dto.response.ayudantia;

public record ResumenAsistenciaDTO(
    Integer total,
    Integer asistieron,
    Integer faltaron
) {}
