package org.uteq.sgacfinal.dto.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TipoRequisitoPostulacionResponseDTO {
    private Integer idTipoRequisitoPostulacion;
    private String nombreRequisito;
    private String descripcion;
    private Boolean activo;
    private String tipoDocumentoPermitido; // Ej: 'PDF', 'PDF,DOCX'
}