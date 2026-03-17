package org.uteq.sgacfinal.repository.documentos;

public interface ConvocatoriaActivaProjection {
    Integer getIdConvocatoria();
    String  getNombreAsignatura();
    String  getNombreDocente();
    String  getEstado();
    Integer getCuposDisponibles();
}