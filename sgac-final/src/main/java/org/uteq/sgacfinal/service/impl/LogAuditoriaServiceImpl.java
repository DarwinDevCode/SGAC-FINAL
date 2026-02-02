package org.uteq.sgacfinal.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.uteq.sgacfinal.dto.Request.LogAuditoriaRequestDTO;
import org.uteq.sgacfinal.dto.Response.LogAuditoriaResponseDTO;
import org.uteq.sgacfinal.entity.LogAuditoria;
import org.uteq.sgacfinal.repository.LogAuditoriaRepository;
import org.uteq.sgacfinal.service.ILogAuditoriaService;

import java.sql.Timestamp;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class LogAuditoriaServiceImpl implements ILogAuditoriaService {

    private final LogAuditoriaRepository logRepository;

    @Override
    public LogAuditoriaResponseDTO registrar(LogAuditoriaRequestDTO request) {
        Integer idGenerado = logRepository.registrarLog(
                request.getIdUsuario(),
                request.getAccion(),
                request.getTablaAfectada(),
                request.getRegistroAfectado(),
                request.getIpOrigen(),
                request.getValorAnterior(),
                request.getValorNuevo()
        );

        if (idGenerado == -1) {
            throw new RuntimeException("Error al registrar auditoría.");
        }

        return buscarPorId(idGenerado);
    }

    @Override
    @Transactional(readOnly = true)
    public LogAuditoriaResponseDTO buscarPorId(Integer id) {
        LogAuditoria log = logRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Log no encontrado con ID: " + id));
        return mapearADTO(log);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LogAuditoriaResponseDTO> listarTodos() {
        List<Object[]> resultados = logRepository.listarLogsSP();
        return resultados.stream()
                .map(this::mapearDesdeObjectArray)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<LogAuditoriaResponseDTO> listarPorUsuario(Integer idUsuario) {
        return logRepository.findByUsuario_IdUsuario(idUsuario).stream()
                .map(this::mapearADTO)
                .collect(Collectors.toList());
    }

    private LogAuditoriaResponseDTO mapearADTO(LogAuditoria entidad) {
        return LogAuditoriaResponseDTO.builder()
                .idLogAuditoria(entidad.getIdLogAuditoria())
                .idUsuario(entidad.getUsuario().getIdUsuario())
                .nombreUsuario(entidad.getUsuario().getNombreUsuario())
                .accion(entidad.getAccion())
                .tablaAfectada(entidad.getTablaAfectada())
                .registroAfectado(entidad.getRegistroAfectado())
                .fechaHora(entidad.getFechaHora())
                .ipOrigen(entidad.getIpOrigen())
                .valorAnterior(entidad.getValorAnterior())
                .valorNuevo(entidad.getValorNuevo())
                .build();
    }

    private LogAuditoriaResponseDTO mapearDesdeObjectArray(Object[] obj) {
        return LogAuditoriaResponseDTO.builder()
                .idLogAuditoria((Integer) obj[0])
                .idUsuario((Integer) obj[1])
                .accion((String) obj[2])
                .fechaHora(obj[3] != null ? ((Timestamp) obj[3]).toLocalDateTime() : null)
                .build();
    }
}