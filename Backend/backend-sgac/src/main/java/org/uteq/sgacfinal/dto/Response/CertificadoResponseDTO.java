package org.uteq.sgacfinal.dto.Response;

import lombok.*;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CertificadoResponseDTO {
    private Integer idCertificado;
    private Integer idAyudantia;
    private Integer idUsuario;
    private String nombreUsuarioEmisor;
    private String codigoVerificacion;
    private LocalDate fechaEmision;
    private Integer totalHorasCertificadas;
    private String estado;
    private Boolean activo;
}