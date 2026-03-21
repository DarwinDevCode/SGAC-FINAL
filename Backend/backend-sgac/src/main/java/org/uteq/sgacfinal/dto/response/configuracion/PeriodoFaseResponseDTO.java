package org.uteq.sgacfinal.dto.response.configuracion;

import lombok.*;

import java.time.LocalDate;

@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PeriodoFaseResponseDTO {
    private Integer idPeriodoFase;
    private Integer idPeriodoAcademico;
    private Integer idTipoFase;
    private String nombreFase;
    private Integer orden;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
}
