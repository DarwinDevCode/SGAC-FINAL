package org.uteq.sgacfinal.service.impl.reportes_y_auditoria;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.uteq.sgacfinal.dto.response.reportes_y_auditoria.AuditoriaKpiDTO;
import org.uteq.sgacfinal.dto.response.reportes_y_auditoria.AuditoriaResponseDTO;
import org.uteq.sgacfinal.dto.response.reportes_y_auditoria.EvolucionAuditoriaProjection;
import org.uteq.sgacfinal.entity.VistaAuditoria;
import org.uteq.sgacfinal.repository.reportes_y_auditoria.AuditoriaRepository;
import org.uteq.sgacfinal.service.reportes_y_auditoria.AuditoriaService;


import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditoriaServiceImpl implements AuditoriaService {

    private final AuditoriaRepository auditoriaRepository;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional(readOnly = true)
    public Page<AuditoriaResponseDTO> buscarAuditorias(
            String tabla,
            String accion,
            Integer idUsuario,
            LocalDateTime fechaInicio,
            LocalDateTime fechaFin,
            Pageable pageable) {

        Page<VistaAuditoria> paginaEntidades = auditoriaRepository.buscarAuditoriasFiltradas(
                tabla, accion, idUsuario, fechaInicio, fechaFin, pageable
        );

        return paginaEntidades.map(this::mapearADto);
    }

    private AuditoriaResponseDTO mapearADto(VistaAuditoria entidad) {
        return AuditoriaResponseDTO.builder()
                .idLogAuditoria(entidad.getIdLogAuditoria())
                .fechaHora(entidad.getFechaHora())
                .usuarioEjecutor(formatearNombreUsuario(entidad))
                .cedula(entidad.getCedula())
                .accion(entidad.getAccion())
                .tablaAfectada(entidad.getTablaAfectada())
                .ipOrigen(entidad.getIpOrigen())
                .valorAnterior(parsearJson(entidad.getValorAnterior()))
                .valorNuevo(parsearJson(entidad.getValorNuevo()))
                .build();
    }


    private String formatearNombreUsuario(VistaAuditoria entidad) {
        if (entidad.getNombreCompletoUsuario() != null && !entidad.getNombreCompletoUsuario().isBlank()) {
            return entidad.getNombreCompletoUsuario() + " (" + entidad.getNombreUsuario() + ")";
        }
        return entidad.getNombreUsuario() != null ? entidad.getNombreUsuario() : "Usuario Desconocido";
    }

    private Map<String, Object> parsearJson(String jsonStr) {
        if (jsonStr == null || jsonStr.trim().isEmpty()) {
            return null;
        }
        try {
            return objectMapper.readValue(jsonStr, new TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            log.error("Error al parsear JSON de auditoría: {}", jsonStr, e);
            return Map.of("error", "No se pudo formatear el JSON");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<EvolucionAuditoriaProjection> obtenerEvolucion() {
        return auditoriaRepository.obtenerEvolucionUltimosDias();
    }

    @Override
    @Transactional(readOnly = true)
    public AuditoriaKpiDTO obtenerKpis() {
        return auditoriaRepository.obtenerKpisGlobales();
    }
}