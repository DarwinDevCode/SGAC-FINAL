package org.uteq.sgacfinal.service;

import org.uteq.sgacfinal.dto.Request.PeriodoAcademicoRequisitoPostulacionRequestDTO;
import org.uteq.sgacfinal.dto.Response.PeriodoAcademicoRequisitoPostulacionResponseDTO;
import java.util.List;

public interface IPeriodoAcademicoRequisitoPostulacionService {

    PeriodoAcademicoRequisitoPostulacionResponseDTO crear(PeriodoAcademicoRequisitoPostulacionRequestDTO request);

    PeriodoAcademicoRequisitoPostulacionResponseDTO actualizar(Integer id, PeriodoAcademicoRequisitoPostulacionRequestDTO request);

    void desactivar(Integer id);

    PeriodoAcademicoRequisitoPostulacionResponseDTO buscarPorId(Integer id);

    //List<PeriodoAcademicoRequisitoPostulacionResponseDTO> listarPorPeriodo(Integer idPeriodo);
}