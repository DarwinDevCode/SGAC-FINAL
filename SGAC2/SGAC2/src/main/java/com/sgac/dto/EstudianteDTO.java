package com.sgac.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EstudianteDTO {
    private Integer idEstudiante;
    private Integer idUsuario;
    private String nombreCompleto;
    private String correo;
    private Integer idCarrera;
    private String nombreCarrera;
    private String matricula;
    private Integer semestre;
    private String estadoAcademico;
}
