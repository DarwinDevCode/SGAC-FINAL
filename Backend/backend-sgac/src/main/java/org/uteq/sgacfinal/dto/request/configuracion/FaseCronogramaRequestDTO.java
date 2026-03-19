package org.uteq.sgacfinal.dto.request.configuracion;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FaseCronogramaRequestDTO {

    @NotNull(message = "El ID del tipo de fase es obligatorio")
    @JsonProperty("id_tipo_fase")
    @JsonAlias("idTipoFase")
    private Integer idTipoFase;

    @NotNull(message = "La fecha de inicio de la fase es obligatoria")
    @JsonFormat(pattern = "yyyy-MM-dd")
    @JsonProperty("fecha_inicio")
    @JsonAlias("fechaInicio")
    private LocalDate fechaInicio;

    @NotNull(message = "La fecha de fin de la fase es obligatoria")
    @JsonFormat(pattern = "yyyy-MM-dd")
    @JsonProperty("fecha_fin")
    @JsonAlias("fechaFin")
    private LocalDate fechaFin;
}