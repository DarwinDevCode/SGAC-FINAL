package org.uteq.sgacfinal.service;

import org.uteq.sgacfinal.dto.Request.PeriodoAcademicoRequestDTO;
import org.uteq.sgacfinal.dto.Response.PeriodoAcademicoResponseDTO;
import java.util.List;

public interface IPeriodoAcademicoService {

    PeriodoAcademicoResponseDTO crear(PeriodoAcademicoRequestDTO request);

    PeriodoAcademicoResponseDTO actualizar(Integer id, PeriodoAcademicoRequestDTO request);

    void desactivar(Integer id);

    PeriodoAcademicoResponseDTO buscarPorId(Integer id);

    List<PeriodoAcademicoResponseDTO> listarTodos();

    //List<PeriodoAcademicoResponseDTO> listarPorEstado(String estado);
}