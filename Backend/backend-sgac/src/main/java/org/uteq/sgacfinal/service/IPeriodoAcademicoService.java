package org.uteq.sgacfinal.service;

import org.uteq.sgacfinal.dto.request.PeriodoAcademicoRequestDTO;
import org.uteq.sgacfinal.dto.response.PeriodoAcademicoResponseDTO;
import java.util.List;

public interface IPeriodoAcademicoService {

    PeriodoAcademicoResponseDTO crear(PeriodoAcademicoRequestDTO request);

    PeriodoAcademicoResponseDTO actualizar(Integer id, PeriodoAcademicoRequestDTO request);

    void desactivar(Integer id);

    PeriodoAcademicoResponseDTO buscarPorId(Integer id);

    List<PeriodoAcademicoResponseDTO> listarTodos();
    PeriodoAcademicoResponseDTO obtenerPeriodoActivo();

    /** Activa manualmente un período (accesible desde el controlador) */
    void activar(Integer id);

    /** Inactiva todos los períodos cuya fecha_fin ya pasó (llamado por @Scheduled) */
    int inactivarVencidos();

    /** Copia los requisitos activos del período fuente al destino */
    int importarRequisitos(Integer idDestino, Integer idFuente);
}