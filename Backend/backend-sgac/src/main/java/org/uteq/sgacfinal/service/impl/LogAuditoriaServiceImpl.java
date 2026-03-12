package org.uteq.sgacfinal.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.uteq.sgacfinal.dto.Request.LogAuditoriaRequestDTO;
import org.uteq.sgacfinal.dto.Response.LogAuditoriaResponseDTO;
import org.uteq.sgacfinal.entity.LogAuditoria;
import org.uteq.sgacfinal.entity.Usuario;
import org.uteq.sgacfinal.repository.IUsuariosRepository;
import org.uteq.sgacfinal.repository.LogAuditoriaRepository;
import org.uteq.sgacfinal.service.ILogAuditoriaService;
import org.uteq.sgacfinal.config.UserContext;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.uteq.sgacfinal.specification.LogAuditoriaSpecification;

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
        Usuario usuario = resolveUsuario(request.getIdUsuario());

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

    @Override
    @Transactional(readOnly = true)
    public Page<LogAuditoriaResponseDTO> obtenerLogsPaginados(String queryParams, String tablaAfectada, String accion, LocalDateTime fechaInicio, LocalDateTime fechaFin, Pageable pageable) {
        Specification<LogAuditoria> spec = LogAuditoriaSpecification.conFiltros(queryParams, tablaAfectada, accion, fechaInicio, fechaFin);
        return logRepository.findAll(spec, pageable).map(this::mapearADTO);
    }

    private LogAuditoriaResponseDTO mapearADTO(LogAuditoria entidad) {
        Integer idUsuario = (entidad.getUsuario() != null) ? entidad.getUsuario().getIdUsuario() : null;
        String nombreUsuario = (entidad.getUsuario() != null) ? (entidad.getUsuario().getNombres() + " " + entidad.getUsuario().getApellidos()) : "Sistema / Trigger";

        return LogAuditoriaResponseDTO.builder()
                .idLogAuditoria(entidad.getIdLogAuditoria())
                .idUsuario(idUsuario)
                .nombreUsuario(nombreUsuario)
                .accion(entidad.getAccion())
                .tablaAfectada(entidad.getTablaAfectada())
                .registroAfectado(entidad.getRegistroAfectado())
                .fechaHora(entidad.getFechaHora())
                .ipOrigen(entidad.getIpOrigen())
                .valorAnterior(entidad.getValorAnterior())
                .valorNuevo(entidad.getValorNuevo())
                .build();
    }

    /**
     * Resolve the actor performing the action. If the request didn't include an explicit id,
     * fall back to the authenticated user stored in the JwtAuthenticationFilter via UserContext.
     */
    private Usuario resolveUsuario(Integer idUsuario) {
        if (idUsuario != null) {
            return usuarioRepository.findById(idUsuario)
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + idUsuario));
        }

        String username = UserContext.getUsername();
        if (username != null) {
            return usuarioRepository.findByNombreUsuario(username)
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado para token: " + username));
        }

        throw new RuntimeException("No se pudo identificar al usuario para auditoría");
    }
}
