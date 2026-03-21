package org.uteq.sgacfinal.dto.response.ayudantia;

import com.fasterxml.jackson.annotation.JsonAlias;

public record ParticipantePadronDTO(
        @JsonAlias("id_participante_ayudantia") Integer idParticipanteAyudantia,
        @JsonAlias("nombre_completo") String nombreCompleto,
        String curso,
        String paralelo,
        Boolean activo
) {}