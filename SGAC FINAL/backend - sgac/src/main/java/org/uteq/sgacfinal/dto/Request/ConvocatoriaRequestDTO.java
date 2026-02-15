package org.uteq.sgacfinal.dto.Request;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDate;

@Getter
@Setter
public class ConvocatoriaRequestDTO {
    private Integer idConvocatoria;
    private Integer idPeriodoAcademico;
    private Integer idAsignatura;
    private Integer idDocente;
    private Integer cuposDisponibles;
    private LocalDate fechaPublicacion;
    private LocalDate fechaCierre;
    private String estado;
    private Boolean activo;
}