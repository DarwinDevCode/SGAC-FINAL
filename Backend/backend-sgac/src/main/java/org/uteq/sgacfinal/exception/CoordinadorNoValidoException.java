package org.uteq.sgacfinal.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.FORBIDDEN)
public class CoordinadorNoValidoException extends RuntimeException {
    public CoordinadorNoValidoException(String message) {
        super(message);
    }
}

