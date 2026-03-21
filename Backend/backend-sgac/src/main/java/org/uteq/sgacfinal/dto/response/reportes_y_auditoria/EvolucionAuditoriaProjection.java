package org.uteq.sgacfinal.dto.response.reportes_y_auditoria;
public interface EvolucionAuditoriaProjection {
    String getFecha();
    Long getInserts();
    Long getUpdates();
    Long getDeletes();
}