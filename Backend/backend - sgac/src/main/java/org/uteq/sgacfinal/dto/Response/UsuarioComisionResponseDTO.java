package org.uteq.sgacfinal.dto.Response;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UsuarioComisionResponseDTO {
    private Integer idUsuarioComision;
    private Integer idComisionSeleccion;
    private String nombreComision;
    private Integer idUsuario;
    private String nombreCompletoUsuario;
    private Integer idEvaluacionOposicion;
    private String rolIntegrante;
    private BigDecimal puntajeMaterial;
    private BigDecimal puntajeRespuestas;
    private BigDecimal puntajeExposicion;
    private LocalDate fechaEvaluacion;
}