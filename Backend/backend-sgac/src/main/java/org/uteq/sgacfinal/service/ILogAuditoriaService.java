package org.uteq.sgacfinal.service;

import org.uteq.sgacfinal.dto.request.LogAuditoriaRequestDTO;
import org.uteq.sgacfinal.dto.response.LogAuditoriaResponseDTO;
import java.util.List;

public interface ILogAuditoriaService {

    LogAuditoriaResponseDTO registrar(LogAuditoriaRequestDTO request);

    LogAuditoriaResponseDTO buscarPorId(Integer id);

    List<LogAuditoriaResponseDTO> listarTodos();

    List<LogAuditoriaResponseDTO> listarPorUsuario(Integer idUsuario);
}