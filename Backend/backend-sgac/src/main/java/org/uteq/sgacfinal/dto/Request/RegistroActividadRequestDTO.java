package org.uteq.sgacfinal.dto.Request;

import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.uteq.sgacfinal.dto.EvidenciaRequest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegistroActividadRequestDTO {
    @NotNull
    private Integer idAyudantia;
    private String descripcionActividad;
    private String temaTratado;
    @NotNull
    private LocalDate fecha;
    private Integer numeroAsistentes;
    @NotNull
    private BigDecimal horasDedicadas;
    private String estadoRevision;
    private List<EvidenciaRequest> evidencias;
}