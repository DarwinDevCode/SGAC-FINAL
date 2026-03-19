package org.uteq.sgacfinal.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PeriodoAcademicoRequisitoPostulacionRequestDTO {
    @NotNull
    private Integer idPeriodoAcademico;
    @NotNull
    private Integer idTipoRequisitoPostulacion;
    private Boolean obligatorio;
    private Integer orden;
    private Boolean activo;
}