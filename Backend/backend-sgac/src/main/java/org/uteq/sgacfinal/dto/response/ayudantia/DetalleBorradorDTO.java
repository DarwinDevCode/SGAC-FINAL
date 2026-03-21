package org.uteq.sgacfinal.dto.response.ayudantia;
import com.fasterxml.jackson.annotation.JsonAlias;

public record DetalleBorradorDTO(
    @JsonAlias("id_registro") Integer idRegistro,
    String tema,
    String fecha,
    String lugar,
    @JsonAlias("descripcion_actual") String descripcionActual,
    @JsonAlias("codigo_estado") String codigoEstado
) {}