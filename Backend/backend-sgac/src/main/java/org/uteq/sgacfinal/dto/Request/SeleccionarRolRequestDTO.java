package org.uteq.sgacfinal.dto.Request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SeleccionarRolRequestDTO {
    private String preAuthToken;
    private String rolSeleccionado;
}