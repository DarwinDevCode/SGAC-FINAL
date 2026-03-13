package org.uteq.sgacfinal.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
public class ComisionException extends RuntimeException {
    public ComisionException(String mensaje) {
        super(mensaje);
    }
}
