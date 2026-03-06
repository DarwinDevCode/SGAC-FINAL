package org.uteq.sgacfinal.dto.Request;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConfirmarActaRequestDTO {
    private Integer idActa;
    private Integer idEvaluador;
    private String rolEvaluador; // DECANO, COORDINADOR, DOCENTE
}
