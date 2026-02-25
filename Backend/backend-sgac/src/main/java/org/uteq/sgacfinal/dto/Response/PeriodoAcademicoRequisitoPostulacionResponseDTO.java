package org.uteq.sgacfinal.dto.Response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PeriodoAcademicoRequisitoPostulacionResponseDTO {
    private Integer idPeriodoAcademicoRequisitoPostulacion;
    private Integer idPeriodoAcademico;
    private String nombrePeriodo;
    private Integer idTipoRequisitoPostulacion;
    private String nombreRequisito;
    private Boolean obligatorio;
    private Integer orden;
    private Boolean activo;
}