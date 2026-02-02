package org.uteq.sgacfinal.dto.Response;

import lombok.*;
import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AyudanteCatedraResponseDTO {
    private Integer idAyudanteCatedra;

    private Integer idUsuario;
    private String nombreCompletoUsuario;
    private String cedulaUsuario;

    private BigDecimal horasAyudante;
}