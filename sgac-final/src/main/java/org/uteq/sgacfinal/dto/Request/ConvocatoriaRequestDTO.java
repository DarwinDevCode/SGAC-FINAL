package org.uteq.sgacfinal.dto.Request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConvocatoriaRequestDTO {
    @NotNull
    private Integer idPeriodoAcademico;
    @NotNull
    private Integer idAsignatura;
    @NotNull
    private Integer idDocente;
    @NotNull
    @Min(1)
    private Integer cuposDisponibles;
    private LocalDate fechaPublicacion;
    private LocalDate fechaCierre;
    private String estado;
    private Boolean activo;
}