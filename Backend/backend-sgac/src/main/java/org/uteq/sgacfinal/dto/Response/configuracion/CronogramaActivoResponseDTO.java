package org.uteq.sgacfinal.dto.Response.configuracion;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class CronogramaActivoResponseDTO {

    private boolean exito;
    private String  mensaje;
    private PeriodoInfoDTO    periodo;
    private List<FaseInfoDTO> fases;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PeriodoInfoDTO {
        private Integer    id;
        private String     nombre;
        private String     fechaInicio;
        private String     fechaFin;
        private Integer    diasTranscurridos;
        private Integer    diasTotales;
        private BigDecimal porcentajeAvance;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class FaseInfoDTO {
        private Integer idPeriodoFase;
        private Integer idTipoFase;
        private Integer orden;
        private String  codigo;
        private String  nombre;
        private String  descripcion;
        private String  fechaInicio;
        private String  fechaFin;
        private Integer duracionDias;
        private Boolean esActual;
    }
}