package org.uteq.sgacfinal.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationRequest {

    @NotBlank
    @Size(max = 150)
    private String titulo;

    @NotBlank
    private String mensaje;

    @NotBlank
    @Size(max = 30)
    private String tipo;

    private Integer idReferencia;
}

