package org.uteq.sgacfinal.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO limpio para el listado "Mis Ayudantes" (DOCENTE).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AyudanteResponseDTO {

    private Integer idAyudantia;

    private Integer idConvocatoria;

    private Integer idPeriodoAcademico;
    private String periodoAcademico;

    private Integer idAsignatura;
    private String asignatura;

    private Integer idUsuarioAyudante;
    private String nombresAyudante;
    private String apellidosAyudante;

    private LocalDate fechaInicio;
    private LocalDate fechaFin;

    private BigDecimal horasMaximas;
    private Integer horasCumplidas;

    private String estadoAyudantia;
}

