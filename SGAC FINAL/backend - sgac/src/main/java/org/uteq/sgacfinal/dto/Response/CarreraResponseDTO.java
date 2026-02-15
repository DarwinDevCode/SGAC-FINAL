package org.uteq.sgacfinal.dto.Response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CarreraResponseDTO {
    private Integer idCarrera;
    private Integer idFacultad;
    private String nombreFacultad;
    private String nombreCarrera;
}