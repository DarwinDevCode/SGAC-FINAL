package org.uteq.sgacfinal.dto.Response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SesionResponseDTO {
    private Integer idRegistroActividad;
    private String descripcionActividad;
    private String temaTratado;
    private LocalDate fecha;
    private Integer numeroAsistentes;
    private BigDecimal horasDedicadas;

    private Integer idTipoEstadoRegistro;
    private String estadoRevision;

    // Nuevo: nombre del estado (tabla ayudantia.tipo_estado_registro)
    private String nombreEstado;

    // Nuevo: observación/retroalimentación del docente sobre el registro
    private String observacionDocente;

    // Nuevo: fecha/hora de la observación
    private LocalDate fechaObservacion;

    private List<EvidenciaResponseDTO> evidencias;
}
