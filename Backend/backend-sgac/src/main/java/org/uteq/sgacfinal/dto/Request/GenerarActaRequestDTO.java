package org.uteq.sgacfinal.dto.Request;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GenerarActaRequestDTO {
    private Integer idPostulacion;
    private String tipoActa; // MERITOS, OPOSICION
}
