package org.uteq.sgacfinal.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AyudanteResumenDTO {
    private Integer idAyudantia;
    private Integer idUsuario;
    private String nombreCompleto;
    private String correo;
    private String nombreAsignatura;
    private String estadoAyudantia;
    private Integer horasCumplidas;
    private long actividadesTotal;
    private long actividadesPendientes;
}
