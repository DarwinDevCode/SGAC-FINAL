package org.uteq.sgacfinal.dto.response.ayudantia;
import com.fasterxml.jackson.annotation.JsonAlias;
import java.util.List;

public record BorradorSesionResponseDTO(
    @JsonAlias("detalle") DetalleBorradorDTO detalle,
    @JsonAlias("evidencias") List<EvidenciaResponseDTO> evidencias
) {}



