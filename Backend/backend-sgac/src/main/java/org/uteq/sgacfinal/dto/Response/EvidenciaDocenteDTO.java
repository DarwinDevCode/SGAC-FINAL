package org.uteq.sgacfinal.dto.Response;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDate;

@Data
@Builder
public class EvidenciaDocenteDTO {
    private Integer idEvidencia;
    private String tipoEvidencia;
    private String nombreArchivo;
    private String rutaArchivo;
    private String mimeType;
    private LocalDate fechaSubida;
    private String estadoEvidencia;
    private String observaciones;
    private LocalDate fechaObservacion;
}
