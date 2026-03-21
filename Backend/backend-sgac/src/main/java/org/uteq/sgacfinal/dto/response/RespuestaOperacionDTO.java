package org.uteq.sgacfinal.dto.response;

public record RespuestaOperacionDTO<T>(
    boolean valido,
    String mensaje,
    T datos
) {}