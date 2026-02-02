package org.uteq.sgacfinal.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AsignaturaDTO {
    private Integer idAsignatura;

    private Integer idCarrera;
    private String nombreCarrera; // Dato aplanado

    private String nombreAsignatura;
    private Integer semestre;
}