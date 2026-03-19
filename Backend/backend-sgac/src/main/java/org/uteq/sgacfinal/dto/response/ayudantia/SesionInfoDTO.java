package org.uteq.sgacfinal.dto.response.ayudantia;

import com.fasterxml.jackson.annotation.JsonAlias;

public record SesionInfoDTO(
    @JsonAlias("id_registro") Integer idRegistro,
    String tema,
    String fecha,
    String horario,
    String lugar,
    @JsonAlias("puede_editar") Boolean puedeEditar
) {}
