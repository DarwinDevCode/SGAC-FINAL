package org.uteq.sgacfinal.dto.response.ayudantia;

import com.fasterxml.jackson.annotation.JsonAlias;
import java.util.List;

public record AsistenciaSesionActualResponseDTO(
        @JsonAlias("sesion") SesionInfoDTO sesion,
        @JsonAlias("estudiantes") List<EstudianteAsistenciaDTO> estudiantes
) {}