package org.uteq.sgacfinal.service;

import org.uteq.sgacfinal.dto.Response.AsignaturaResponseDTO;
import org.uteq.sgacfinal.dto.Response.DocenteResponseDTO;
import org.uteq.sgacfinal.dto.Response.PeriodoAcademicoResponseDTO;

import java.util.List;

public interface IRecursosConvocatoriaService {
    List<DocenteResponseDTO> obtenerDocentesParaSelector();
    List<AsignaturaResponseDTO> obtenerAsignaturasParaSelector();
    PeriodoAcademicoResponseDTO obtenerPeriodoActivo();
}