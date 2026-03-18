package org.uteq.sgacfinal.dto.Request.ayudantia;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import org.uteq.sgacfinal.dto.EvidenciaRequest;

import java.util.List;

@Data
public class CompletarSesionRequest {

    @NotBlank(message = "Debe ingresar una descripción de las actividades realizadas.")
    private String descripcionActividad;

    @NotEmpty(message = "Debe enviar la lista de asistencia (incluso si todos faltaron).")
    @Valid
    private List<AsistenciaRequest> asistencias;

    @NotEmpty(message = "Debe enviar los metadatos de las evidencias subidas.")
    @Valid
    private List<EvidenciaRequest> metadatosEvidencias;
}