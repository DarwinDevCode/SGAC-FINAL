package org.uteq.sgacfinal.dto.Response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MensajeInternoResponseDTO {
    private Integer idMensajeInterno;
    private Integer idAyudantia;
    private Integer idUsuarioEmisor;
    private String nombreEmisor;
    private String mensaje;
    private LocalDateTime fechaEnvio;
    private String rutaArchivoAdjunto;
    private Boolean leido;
}
