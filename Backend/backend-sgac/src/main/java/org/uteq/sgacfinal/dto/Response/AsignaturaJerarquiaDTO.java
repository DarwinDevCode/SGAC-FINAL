package org.uteq.sgacfinal.dto.Response;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AsignaturaJerarquiaDTO {
    private Integer idAsignatura;
    private String  nombreAsignatura;
    private Integer semestre;
    private Integer idCarrera;
    private String  nombreCarrera;
    private Integer idFacultad;
    private String  nombreFacultad;
    private String  etiqueta;
}