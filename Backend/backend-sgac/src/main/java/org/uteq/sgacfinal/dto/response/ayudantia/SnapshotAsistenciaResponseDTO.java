package org.uteq.sgacfinal.dto.response.ayudantia;

import com.fasterxml.jackson.annotation.JsonAlias;

public record SnapshotAsistenciaResponseDTO(
    @JsonAlias("total") Integer total
) {}