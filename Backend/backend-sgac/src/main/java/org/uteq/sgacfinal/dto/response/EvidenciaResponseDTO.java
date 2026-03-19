package org.uteq.sgacfinal.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EvidenciaResponseDTO {
    private Integer idEvidenciaRegistroActividad;
    private String nombreArchivo;
    private String rutaArchivo;
    private String mimeType;
    private Integer tamanioBytes;
    private LocalDate fechaSubida;

    private Integer idTipoEvidencia;
    private String tipoEvidencia;

    private Integer idTipoEstadoEvidencia;
    private String estadoEvidencia;

    // Nuevo: nombre del estado de evidencia (tabla ayudantia.tipo_estado_evidencia)
    private String nombreEstadoEvidencia;

    // Nuevo: observación/retroalimentación del docente sobre la evidencia
    private String observacionDocente;

    // Nuevo: fecha/hora de la observación
    private LocalDate fechaObservacion;
}
