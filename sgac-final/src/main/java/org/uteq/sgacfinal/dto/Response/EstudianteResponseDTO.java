package org.uteq.sgacfinal.dto.Response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EstudianteResponseDTO {
    private Integer idEstudiante;
    private Integer idUsuario;
    private String nombreCompletoUsuario;
    private String cedula;
    private String correo;
    private Integer idCarrera;
    private String matricula;
    private Integer semestre;
    private String estadoAcademico;
}