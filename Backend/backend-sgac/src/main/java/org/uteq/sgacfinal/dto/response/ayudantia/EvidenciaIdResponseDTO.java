package org.uteq.sgacfinal.dto.response.ayudantia;
import com.fasterxml.jackson.annotation.JsonAlias;

public record EvidenciaIdResponseDTO(
    @JsonAlias("id_evidencia") Integer idEvidencia
) {}