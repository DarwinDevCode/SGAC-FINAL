package org.uteq.sgacfinal.service;

import org.uteq.sgacfinal.dto.Request.LogAuditoriaRequestDTO;
import org.uteq.sgacfinal.dto.Response.LogAuditoriaResponseDTO;
import java.util.List;

public interface ILogAuditoriaService {

    LogAuditoriaResponseDTO registrar(LogAuditoriaRequestDTO request);

    LogAuditoriaResponseDTO buscarPorId(Integer id);

    List<LogAuditoriaResponseDTO> listarTodos();

    List<LogAuditoriaResponseDTO> listarPorUsuario(Integer idUsuario);
}