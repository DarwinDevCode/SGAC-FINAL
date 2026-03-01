package org.uteq.sgacfinal.dto.Request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.uteq.sgacfinal.dto.EvidenciaRequest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class RegistrarSesionRequest {

    @NotNull(message = "La fecha es obligatoria")
    @PastOrPresent(message = "La fecha no puede ser futura")
    private LocalDate fecha;

    @NotBlank(message = "El tema tratado es obligatorio")
    @Size(max = 200)
    private String temaTratado;

    @NotBlank(message = "La descripción es obligatoria")
    private String descripcionActividad;

    @NotNull(message = "El número de asistentes es obligatorio")
    @Min(value = 0, message = "El número de asistentes no puede ser negativo")
    private Integer numeroAsistentes;

    @NotNull(message = "Las horas dedicadas son obligatorias")
    @DecimalMin(value = "0.01", message = "Las horas deben ser mayores a 0")
    @DecimalMax(value = "24.00", message = "Las horas no pueden superar 24 por sesión")
    private BigDecimal horasDedicadas;

    @NotEmpty(message = "Debe adjuntar al menos una evidencia")
    @Size(max = 5, message = "Se permiten máximo 5 evidencias por sesión")
    @Valid
    private List<EvidenciaRequest> evidencias;
}