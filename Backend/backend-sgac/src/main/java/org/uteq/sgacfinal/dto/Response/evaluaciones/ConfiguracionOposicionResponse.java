package org.uteq.sgacfinal.dto.Response.evaluaciones;

public record ConfiguracionOposicionResponse(
        Double  maxPuntajeMaterial,
        Double  maxPuntajeExposicion,
        Double  maxPuntajeRespuestas,
        Integer minutosExposicion,
        Integer minutosPreguntas,
        Integer minutosTransicion
) {
    public int bloqueTotal() {
        return minutosExposicion + minutosPreguntas + minutosTransicion;
    }
}