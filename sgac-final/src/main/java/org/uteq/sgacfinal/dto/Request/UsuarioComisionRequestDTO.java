package org.uteq.sgacfinal.dto.Request;

import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UsuarioComisionRequestDTO {
    @NotNull
    private Integer idComisionSeleccion;
    @NotNull
    private Integer idUsuario;
    @NotNull
    private Integer idEvaluacionOposicion;
    private String rolIntegrante;
    private BigDecimal puntajeMaterial;
    private BigDecimal puntajeRespuestas;
    private BigDecimal puntajeExposicion;
    private LocalDate fechaEvaluacion;
}