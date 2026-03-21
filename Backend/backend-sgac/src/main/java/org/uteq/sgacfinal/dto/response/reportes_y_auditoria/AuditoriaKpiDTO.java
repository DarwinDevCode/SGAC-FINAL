package org.uteq.sgacfinal.dto.response.reportes_y_auditoria;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class AuditoriaKpiDTO {

    private Long totalRegistros;
    private Long actividadHoy;
    private Long totalInserts;
    private Long totalUpdates;
    private Long totalDeletes;

    public AuditoriaKpiDTO(Number totalRegistros, Number actividadHoy, Number totalInserts, Number totalUpdates, Number totalDeletes) {
        this.totalRegistros = totalRegistros != null ? totalRegistros.longValue() : 0L;
        this.actividadHoy = actividadHoy != null ? actividadHoy.longValue() : 0L;
        this.totalInserts = totalInserts != null ? totalInserts.longValue() : 0L;
        this.totalUpdates = totalUpdates != null ? totalUpdates.longValue() : 0L;
        this.totalDeletes = totalDeletes != null ? totalDeletes.longValue() : 0L;
    }
}