package org.uteq.sgacfinal.dto.Response;

import lombok.*;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificacionResponseDTO {
    private Integer idNotificacion;
    private String titulo;
    private String mensaje;
    private String tipo;
    private Integer idReferencia;
    private Boolean leido;
    private Instant fechaCreacion;
    private Instant fechaLectura;
}

