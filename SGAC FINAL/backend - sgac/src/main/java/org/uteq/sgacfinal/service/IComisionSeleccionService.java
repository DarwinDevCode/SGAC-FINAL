package org.uteq.sgacfinal.service;

import org.uteq.sgacfinal.dto.Request.ComisionSeleccionRequestDTO;
import org.uteq.sgacfinal.dto.Response.ComisionSeleccionResponseDTO;
import java.util.List;

public interface IComisionSeleccionService {

    ComisionSeleccionResponseDTO crear(ComisionSeleccionRequestDTO request);

    ComisionSeleccionResponseDTO actualizar(Integer id, ComisionSeleccionRequestDTO request);

    void desactivar(Integer id);

    ComisionSeleccionResponseDTO buscarPorId(Integer id);

    List<ComisionSeleccionResponseDTO> listarPorConvocatoria(Integer idConvocatoria);
}