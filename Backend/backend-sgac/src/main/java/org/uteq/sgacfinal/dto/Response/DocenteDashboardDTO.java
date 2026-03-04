package org.uteq.sgacfinal.dto.Response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DocenteDashboardDTO {
    private long totalAyudantes;
    private long actividadesPendientes;
    private long actividadesAceptadas;
    private long actividadesRechazadas;
    private long actividadesObservadas;
    private long totalActividades;
}
