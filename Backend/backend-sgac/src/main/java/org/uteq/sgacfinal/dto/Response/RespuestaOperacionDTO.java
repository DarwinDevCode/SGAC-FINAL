package org.uteq.sgacfinal.dto.Response;

public record RespuestaOperacionDTO<T>(
    boolean valido,
    String mensaje,
    T datos
) {}