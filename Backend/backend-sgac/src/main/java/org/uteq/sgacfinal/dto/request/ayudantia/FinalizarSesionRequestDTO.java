package org.uteq.sgacfinal.dto.request.ayudantia;

public record FinalizarSesionRequestDTO(
    Integer idUsuario,
    Integer idRegistro,
    String descripcion
) {}