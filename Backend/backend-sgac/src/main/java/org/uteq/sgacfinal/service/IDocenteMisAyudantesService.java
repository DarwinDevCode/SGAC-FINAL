package org.uteq.sgacfinal.service;

import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.uteq.sgacfinal.dto.request.EvaluarActividadRequestDTO;
import org.uteq.sgacfinal.dto.request.EvaluarEvidenciaRequestDTO;
import org.uteq.sgacfinal.dto.response.ActividadDetalleDTO;
import org.uteq.sgacfinal.dto.response.AyudanteResponseDTO;

import java.util.List;

public interface IDocenteMisAyudantesService {

    List<AyudanteResponseDTO> listarMisAyudantes();

    List<ActividadDetalleDTO> listarActividadesPorAyudantia(Integer idAyudantia);

    void evaluarActividad(Integer idActividad, EvaluarActividadRequestDTO request);

    void evaluarEvidencia(Integer idEvidencia, EvaluarEvidenciaRequestDTO request);

    ResponseEntity<Resource> descargarEvidencia(Integer idEvidencia);
}

