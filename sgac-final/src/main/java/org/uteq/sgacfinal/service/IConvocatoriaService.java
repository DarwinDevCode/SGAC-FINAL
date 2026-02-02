package org.uteq.sgacfinal.service;

import org.uteq.sgacfinal.dto.Request.ConvocatoriaRequestDTO;
import org.uteq.sgacfinal.dto.Response.ConvocatoriaResponseDTO;
import java.util.List;

public interface IConvocatoriaService {

    ConvocatoriaResponseDTO crear(ConvocatoriaRequestDTO request);

    ConvocatoriaResponseDTO actualizar(Integer id, ConvocatoriaRequestDTO request);

    void desactivar(Integer id);

    ConvocatoriaResponseDTO buscarPorId(Integer id);

    //List<ConvocatoriaResponseDTO> listarActivas();

    List<ConvocatoriaResponseDTO> listarPorPeriodo(Integer idPeriodo);
}