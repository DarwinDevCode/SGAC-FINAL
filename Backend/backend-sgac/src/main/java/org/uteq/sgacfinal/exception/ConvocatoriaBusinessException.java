package org.uteq.sgacfinal.exception;

public class ConvocatoriaBusinessException extends RuntimeException {

    private final String codigo;

    public ConvocatoriaBusinessException(String mensaje) {
        super(mensaje);
        this.codigo = "NEGOCIO";
    }

    public ConvocatoriaBusinessException(String mensaje, String codigo) {
        super(mensaje);
        this.codigo = codigo;
    }

    public String getCodigo() { return codigo; }
}
