package org.uteq.sgacfinal.dto.Response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocenteAsignaturaResponseDTO {
    private Integer idDocenteAsignatura;
    private Integer idDocente;
    private String nombreCompletoDocente;
    private Integer idAsignatura;
    private String nombreAsignatura;
    private Boolean activo;
}