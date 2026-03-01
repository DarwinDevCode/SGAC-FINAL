package org.uteq.sgacfinal.dto.Response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@Builder
public class EvidenciaResponse {
    private Integer idEvidencia;
    private String nombreArchivo;
    private String rutaArchivo;
    private String mimeType;
    private Integer tamanioBytes;
    private String tipoEvidencia;
    private String estadoEvidencia;
    private LocalDate fechaSubida;
}