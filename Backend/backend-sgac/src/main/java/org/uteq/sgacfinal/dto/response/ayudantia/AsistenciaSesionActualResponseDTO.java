package org.uteq.sgacfinal.dto.response.ayudantia;

import com.fasterxml.jackson.annotation.JsonAlias;
import org.uteq.sgacfinal.dto.response.MatrizAsistenciaDTO;

import java.util.List;

public record AsistenciaSesionActualResponseDTO(
    @JsonAlias("sesion") MatrizAsistenciaDTO.SesionInfoDTO sesion,
    @JsonAlias("estudiantes") List<EstudianteAsistenciaDTO> estudiantes
) {}