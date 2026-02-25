package org.uteq.sgacfinal.dto.Response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FacultadResponseDTO {
    private Integer idFacultad;
    private String nombreFacultad;
    private Boolean activo;
}