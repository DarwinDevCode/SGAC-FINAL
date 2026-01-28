package org.uteq.sgacfinal.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CarreraDTO {
    private Integer idCarrera;
    private Integer idFacultad;
    private String nombreFacultad;
    private String nombreCarrera;
}
