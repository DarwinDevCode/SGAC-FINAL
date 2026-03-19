package org.uteq.sgacfinal.dto.Request.evaluaciones;

import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;
import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class GuardarMeritosRequest {

    @NotNull(message = "El id de postulación es requerido")
    private Integer idPostulacion;

    @NotNull(message = "La nota de aprobación de la asignatura es requerida")
    @DecimalMin(value = "0.0", message = "La nota de aprobación no puede ser negativa")
    @DecimalMax(value = "10.0", message = "La nota de aprobación no puede superar 10.00")
    private BigDecimal notaAprobacionAsignatura;

    @NotNull(message = "La lista de notas por semestre es requerida (puede estar vacía)")
    private List<
            @DecimalMin(value = "0.0", message = "Cada nota de semestre debe ser ≥ 0.00")
            @DecimalMax(value = "10.0", message = "Cada nota de semestre debe ser ≤ 10.00")
            @NotNull(message = "Ninguna nota de semestre puede ser nula")
                    BigDecimal> semestresNotas;

    @NotNull(message = "La nota de experiencia es requerida")
    @DecimalMin(value = "0.0", message = "La nota de experiencia debe ser ≥ 0.00")
    @DecimalMax(value = "4.0", message = "La nota de experiencia no puede superar 4.00")
    private BigDecimal notaExperiencia;

    @NotNull(message = "La nota de eventos es requerida")
    @DecimalMin(value = "0.0", message = "La nota de eventos debe ser ≥ 0.00")
    @DecimalMax(value = "2.0", message = "La nota de eventos no puede superar 2.00")
    private BigDecimal notaEventos;

    @Builder.Default
    private boolean finalizar = false;
}