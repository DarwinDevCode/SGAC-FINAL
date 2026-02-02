package org.uteq.sgacfinal.dto.Request;

import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CertificadoRequestDTO {

    @NotNull(message = "La ayudantía es obligatoria")
    private Integer idAyudantia;

    private Integer idUsuario;

    private String codigoVerificacion;
    private LocalDate fechaEmision;
    private Integer totalHorasCertificadas;

    private byte[] archivo;

    private String estado;
    private Boolean activo;
}