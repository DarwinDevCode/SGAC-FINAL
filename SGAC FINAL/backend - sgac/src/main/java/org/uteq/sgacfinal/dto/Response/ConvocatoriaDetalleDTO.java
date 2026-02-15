package org.uteq.sgacfinal.dto.Response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Date;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ConvocatoriaDetalleDTO {
    private Integer idConvocatoria;
    private Integer cuposDisponibles;
    private Date fechaPublicacion;
    private Date fechaCierre;
    private String estado;
    private Boolean activo;
    private Integer idPeriodoAcademico;
    private Integer idAsignatura;
    private String nombreAsignatura;
    private Integer semestre;
    private Integer idCarrera;
    private Integer idDocente;
}