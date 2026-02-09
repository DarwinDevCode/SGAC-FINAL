package org.uteq.sgacfinal.dto.Response;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDate;

@Getter
@Setter
@Builder
public class ConvocatoriaResponseDTO {
    private Integer idConvocatoria;
    private String nombrePeriodo;
    private String nombreAsignatura;
    private Integer idPeriodoAcademico;
    private Integer idAsignatura;
    private Integer idDocente;
    private Integer cuposDisponibles;
    private LocalDate fechaPublicacion;
    private LocalDate fechaCierre;
    private String estado;
    private Boolean activo;
}
