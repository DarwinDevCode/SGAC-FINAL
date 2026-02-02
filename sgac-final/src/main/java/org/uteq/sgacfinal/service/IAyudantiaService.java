package org.uteq.sgacfinal.service;

import org.uteq.sgacfinal.dto.Request.AyudantiaRequestDTO;
import org.uteq.sgacfinal.dto.Response.AyudantiaResponseDTO;
import java.util.List;

public interface IAyudantiaService {

    AyudantiaResponseDTO crear(AyudantiaRequestDTO request);

    AyudantiaResponseDTO actualizar(Integer id, AyudantiaRequestDTO request);

    AyudantiaResponseDTO buscarPorId(Integer id);

    AyudantiaResponseDTO buscarPorPostulacion(Integer idPostulacion);

    List<AyudantiaResponseDTO> listarTodas();
}