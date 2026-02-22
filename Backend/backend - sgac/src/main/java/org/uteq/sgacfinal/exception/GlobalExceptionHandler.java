package org.uteq.sgacfinal.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleResourceNotFoundException(ResourceNotFoundException ex) {
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now());
        response.put("status", HttpStatus.NOT_FOUND.value());
        response.put("error", "Not Found");
        response.put("message", ex.getMessage());
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<Map<String, Object>> handleBadRequestException(BadRequestException ex) {
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now());
        response.put("status", HttpStatus.BAD_REQUEST.value());
        response.put("error", "Bad Request");
        response.put("message", ex.getMessage());
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, Object> response = new HashMap<>();
        Map<String, String> errors = new HashMap<>();

        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        response.put("timestamp", LocalDateTime.now());
        response.put("status", HttpStatus.BAD_REQUEST.value());
        response.put("error", "Validation Failed");
        response.put("errors", errors);
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(Exception ex) {
        Map<String, Object> response = new HashMap<>();

        String errorCrudo = ex.getMessage();
        String mensajeLimpio = "Ocurrió un error inesperado en el servidor.";

        if (errorCrudo != null) {
            if (errorCrudo.contains("23505") || errorCrudo.contains("duplicate key")) {
                java.util.regex.Matcher matcher = java.util.regex.Pattern.compile("constraint \"(.*?)\"").matcher(errorCrudo);

                if (matcher.find()) {
                    String constraint = matcher.group(1);

                    String campo = constraint
                            .replace("usuario_", "")
                            .replace("uk_", "")
                            .replace("_key", "")
                            .toUpperCase();

                    mensajeLimpio = "Error: El dato ingresado para el campo " + campo + " ya se encuentra registrado.";
                } else {
                    mensajeLimpio = "Error: Un dato único ya existe en el sistema.";
                }
            } else {
                mensajeLimpio = errorCrudo.replaceAll("org.hibernate.exception.GenericJDBCException: ", "").split("\n")[0];
            }
        }

        response.put("timestamp", LocalDateTime.now());
        response.put("status", HttpStatus.BAD_REQUEST.value());
        response.put("error", "Database / Business Logic Error");
        response.put("message", mensajeLimpio);

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }
}