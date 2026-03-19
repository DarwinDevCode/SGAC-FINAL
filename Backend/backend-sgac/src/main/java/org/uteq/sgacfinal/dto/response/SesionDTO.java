package org.uteq.sgacfinal.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SesionDTO {

    private Integer      idRegistroActividad;
    private LocalDate    fecha;
    private LocalTime    horaInicio;
    private LocalTime    horaFin;
    private BigDecimal   horasDedicadas;
    private String       temaTratado;
    private String       lugar;
    private String       descripcionActividad;
    private String       observacionDocente;
    private LocalDate    fechaObservacion;
    private String       codigoEstado;
    private String       nombreEstado;
    private List<EvidenciaDTO> evidencias;



    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class EvidenciaDTO {
        private Integer   idEvidenciaRegistroActividad;
        private String    nombreArchivo;
        private String    rutaArchivo;
        private String    mimeType;
        private LocalDate fechaSubida;
        private String    codigoEstadoEvidencia;
        private String    nombreEstadoEvidencia;

        private String    observacionDocente;
        private LocalDate fechaObservacion;
    }
}