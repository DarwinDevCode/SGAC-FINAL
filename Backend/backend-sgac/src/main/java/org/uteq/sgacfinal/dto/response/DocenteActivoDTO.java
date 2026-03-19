package org.uteq.sgacfinal.dto.response;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class DocenteActivoDTO {
    private Integer idDocente;
    private String  nombres;
    private String  apellidos;
    private String  cedula;
    private String  correo;
    private Long    totalAsignaturas;
}