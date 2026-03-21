package org.uteq.sgacfinal.service;

import org.uteq.sgacfinal.dto.request.PeriodoAcademicoRequisitoPostulacionRequestDTO;
import org.uteq.sgacfinal.dto.response.PeriodoAcademicoRequisitoPostulacionResponseDTO;

public interface IPeriodoAcademicoRequisitoPostulacionService {

    PeriodoAcademicoRequisitoPostulacionResponseDTO crear(PeriodoAcademicoRequisitoPostulacionRequestDTO request);

    PeriodoAcademicoRequisitoPostulacionResponseDTO actualizar(Integer id, PeriodoAcademicoRequisitoPostulacionRequestDTO request);

    void desactivar(Integer id);

    PeriodoAcademicoRequisitoPostulacionResponseDTO buscarPorId(Integer id);

    //List<PeriodoAcademicoRequisitoPostulacionResponseDTO> listarPorPeriodo(Integer idPeriodo);

    /** Ítem 5: importa los requisitos de un periodo origen al destino. Retorna cantidad importada */
    int importarDeOtroPeriodo(Integer idPeriodoOrigen, Integer idPeriodoDestino);
}