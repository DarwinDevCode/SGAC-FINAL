package org.uteq.sgacfinal.service;

import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.uteq.sgacfinal.dto.Request.EvaluarActividadRequestDTO;
import org.uteq.sgacfinal.dto.Request.EvaluarEvidenciaRequestDTO;
import org.uteq.sgacfinal.dto.Response.ActividadDetalleDTO;
import org.uteq.sgacfinal.dto.Response.AyudanteResponseDTO;

import java.util.List;

public interface IDocenteMisAyudantesService {

    List<AyudanteResponseDTO> listarMisAyudantes();

    List<ActividadDetalleDTO> listarActividadesPorAyudantia(Integer idAyudantia);

    void evaluarActividad(Integer idActividad, EvaluarActividadRequestDTO request);

    void evaluarEvidencia(Integer idEvidencia, EvaluarEvidenciaRequestDTO request);

    ResponseEntity<Resource> descargarEvidencia(Integer idEvidencia);
}

