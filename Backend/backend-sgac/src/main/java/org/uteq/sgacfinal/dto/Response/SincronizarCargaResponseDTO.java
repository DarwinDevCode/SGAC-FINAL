package org.uteq.sgacfinal.dto.Response;

import lombok.*;
import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class SincronizarCargaResponseDTO {
    private Boolean exito;
    private Integer idDocente;
    private String  nombreDocente;
    private String  correoDocente;
    private Integer revocadas;
    private Integer asignadas;
    private Integer sinCambio;
    private List<String> asignaturasActuales;
    private List<String> asignaturasRevocadas;
    private String  mensaje;
}