package org.uteq.sgacfinal.dto.Response;

import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InformeMensualResponse {
    private Integer idInformeMensual;
    private Integer idAyudantia;
    private Integer mes;
    private Integer anio;
    private String contenidoBorrador;
    private String estado;
    private LocalDateTime fechaGeneracion;
    private LocalDateTime fechaEnvio;
    private String observaciones;
}
