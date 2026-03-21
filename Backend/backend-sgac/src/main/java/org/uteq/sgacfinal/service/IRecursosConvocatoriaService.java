package org.uteq.sgacfinal.service;

import org.uteq.sgacfinal.dto.response.*;

import java.util.List;

public interface IRecursosConvocatoriaService {
    List<DocenteResponseDTO> obtenerDocentesParaSelector();
    List<AsignaturaResponseDTO> obtenerAsignaturasParaSelector();
    PeriodoAcademicoResponseDTO obtenerPeriodoActivo();
    List<FacultadResponseDTO> listarFacultades();
    List<CarreraResponseDTO> listarCarreras();
}