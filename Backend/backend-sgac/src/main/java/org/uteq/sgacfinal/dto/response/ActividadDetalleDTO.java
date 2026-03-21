package org.uteq.sgacfinal.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * DTO limpio para listar actividades (registro_actividad) con sus evidencias.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActividadDetalleDTO {

    private Integer idRegistroActividad;
    private Integer idAyudantia;

    private String descripcionActividad;
    private String temaTratado;
    private LocalDate fecha;

    private Integer numeroAsistentes;
    private BigDecimal horasDedicadas;

    private Integer idTipoEstadoRegistro;
    private String estadoRegistro;

    private String observaciones;
    private LocalDate fechaObservacion;

    private List<EvidenciaDetalleDocenteDTO> evidencias;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EvidenciaDetalleDocenteDTO {
        private Integer idEvidencia;
        private String nombreArchivo;
        private String rutaArchivo;
        private String mimeType;
        private Integer tamanioBytes;
        private LocalDate fechaSubida;

        private Integer idTipoEstadoEvidencia;
        private String estadoEvidencia;

        private String observaciones;
        private LocalDate fechaObservacion;
    }
}

