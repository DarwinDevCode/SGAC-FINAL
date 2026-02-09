package org.uteq.sgacfinal.service;

import org.uteq.sgacfinal.dto.Request.ConvocatoriaRequestDTO;
import org.uteq.sgacfinal.dto.Response.ConvocatoriaDetalleDTO;
import org.uteq.sgacfinal.dto.Response.ConvocatoriaResponseDTO;
import java.util.List;

public interface IConvocatoriaService {

//    ConvocatoriaResponseDTO crear(ConvocatoriaRequestDTO request);
//    ConvocatoriaResponseDTO actualizar(Integer id, ConvocatoriaRequestDTO request);
//    void desactivar(Integer id);
//    ConvocatoriaResponseDTO buscarPorId(Integer id);
//    List<ConvocatoriaResponseDTO> obtenerTodasLasConvocatorias();
//    List<ConvocatoriaResponseDTO> listarPorPeriodo(Integer idPeriodo);


    ConvocatoriaResponseDTO create(ConvocatoriaRequestDTO dto);
    ConvocatoriaResponseDTO update(ConvocatoriaRequestDTO dto);
    List<ConvocatoriaResponseDTO> findAll();
    ConvocatoriaResponseDTO findById(Integer id);
    void delete(Integer id);
}