package org.uteq.sgacfinal.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.uteq.sgacfinal.dto.request.LogAuditoriaRequestDTO;
import org.uteq.sgacfinal.dto.response.LogAuditoriaResponseDTO;
import org.uteq.sgacfinal.entity.LogAuditoria;
import org.uteq.sgacfinal.entity.Usuario;
import org.uteq.sgacfinal.repository.IUsuariosRepository;
import org.uteq.sgacfinal.repository.LogAuditoriaRepository;
import org.uteq.sgacfinal.service.ILogAuditoriaService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LogAuditoriaServiceImpl implements ILogAuditoriaService {

    private final LogAuditoriaRepository logRepository;
    private final IUsuariosRepository usuarioRepository;

    @Override
    @Transactional
    public LogAuditoriaResponseDTO registrar(LogAuditoriaRequestDTO request) {
        Usuario usuario = usuarioRepository.findById(request.getIdUsuario())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + request.getIdUsuario()));

        LogAuditoria log = LogAuditoria.builder()
                .usuario(usuario)
                .accion(request.getAccion())
                .tablaAfectada(request.getTablaAfectada())
                .registroAfectado(request.getRegistroAfectado())
                .ipOrigen(request.getIpOrigen())
                .valorAnterior(request.getValorAnterior())
                .valorNuevo(request.getValorNuevo())
                .fechaHora(LocalDateTime.now())
                .build();

        LogAuditoria guardado = logRepository.save(log);
        return mapearADTO(guardado);
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
        return logRepository.findAll().stream()
                .map(this::mapearADTO)
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
                .nombreUsuario(entidad.getUsuario().getNombres() + " " + entidad.getUsuario().getApellidos())
                .accion(entidad.getAccion())
                .tablaAfectada(entidad.getTablaAfectada())
                .registroAfectado(entidad.getRegistroAfectado())
                .fechaHora(entidad.getFechaHora())
                .ipOrigen(entidad.getIpOrigen())
                .valorAnterior(entidad.getValorAnterior())
                .valorNuevo(entidad.getValorNuevo())
                .build();
    }
}