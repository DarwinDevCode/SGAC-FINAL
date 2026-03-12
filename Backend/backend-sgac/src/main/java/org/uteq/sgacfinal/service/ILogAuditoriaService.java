package org.uteq.sgacfinal.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.uteq.sgacfinal.dto.Request.LogAuditoriaRequestDTO;
import org.uteq.sgacfinal.dto.Response.LogAuditoriaResponseDTO;

import java.time.LocalDateTime;
import java.util.List;

public interface ILogAuditoriaService {

    LogAuditoriaResponseDTO registrar(LogAuditoriaRequestDTO request);

    LogAuditoriaResponseDTO buscarPorId(Integer id);

    List<LogAuditoriaResponseDTO> listarTodos();

    List<LogAuditoriaResponseDTO> listarPorUsuario(Integer idUsuario);

    Page<LogAuditoriaResponseDTO> obtenerLogsPaginados(String queryParams, String tablaAfectada, String accion, LocalDateTime fechaInicio, LocalDateTime fechaFin, Pageable pageable);
}