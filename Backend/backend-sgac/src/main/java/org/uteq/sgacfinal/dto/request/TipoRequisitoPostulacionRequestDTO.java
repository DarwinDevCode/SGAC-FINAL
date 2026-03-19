package org.uteq.sgacfinal.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TipoRequisitoPostulacionRequestDTO {
    @NotBlank
    private String nombreRequisito;
    private String descripcion;
    private Boolean activo;
    private String tipoDocumentoPermitido;
}