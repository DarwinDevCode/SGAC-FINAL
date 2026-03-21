package org.uteq.sgacfinal.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PrivilegioResponseDTO {
    private Integer idPrivilegio;
    private String nombrePrivilegio;
    private String codigoInterno;
}