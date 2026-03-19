package org.uteq.sgacfinal.dto.request.ayudantia;

import com.fasterxml.jackson.annotation.JsonAlias;

public record ParticipanteIdResponseDTO(
    @JsonAlias("id") Integer id
) {}