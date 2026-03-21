package org.uteq.sgacfinal.dto.request.ayudantia;

public record ParticipanteRequestDTO(
    String accion,
    Integer idUsuario,
    String nombre,
    String curso,
    String paralelo,
    Integer idParticipante
) {}