package org.uteq.sgacfinal.dto.response.reportes_y_auditoria;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
public class AuditoriaResponseDTO {
    private Integer idLogAuditoria;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime fechaHora;
    private String usuarioEjecutor;
    private String cedula;
    private String accion;
    private String tablaAfectada;
    private String ipOrigen;
    private Map<String, Object> valorAnterior;
    private Map<String, Object> valorNuevo;
}