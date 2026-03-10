package org.uteq.sgacfinal.dto.Response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AyudantiaDetalleResponseDTO {
    private Integer idAyudantia;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private Integer horasCumplidas;
    private List<RegistroActividadDetalleDTO> actividades;
}