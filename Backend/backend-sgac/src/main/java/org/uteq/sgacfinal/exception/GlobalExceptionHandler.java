package org.uteq.sgacfinal.exception;

import org.springframework.dao.DataIntegrityViolationException;
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
        return buildResponse(HttpStatus.NOT_FOUND, "Not Found", ex.getMessage());
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<Map<String, Object>> handleBadRequestException(BadRequestException ex) {
        return buildResponse(HttpStatus.BAD_REQUEST, "Bad Request", ex.getMessage());
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

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, Object>> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        String mensajeLimpio = "Error: Un dato único ya existe en el sistema.";

        Throwable rootCause = ex.getRootCause() != null ? ex.getRootCause() : ex;
        String errorCrudo = rootCause.getMessage();

        if (errorCrudo != null) {
            java.util.regex.Matcher matcher = java.util.regex.Pattern.compile("[\"«]([a-zA-Z0-9_]+)[\"»]").matcher(errorCrudo);

            if (matcher.find()) {
                String constraint = matcher.group(1);

                String campo = constraint
                        .replace("usuario_", "")
                        .replace("uk_", "")
                        .replace("_key", "")
                        .toUpperCase();

                mensajeLimpio = "Error: El dato ingresado para el campo " + campo + " ya se encuentra registrado.";
            }
        }

        return buildResponse(HttpStatus.BAD_REQUEST, "Data Integrity Violation", mensajeLimpio);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(Exception ex) {
        Throwable rootCause = ex;
        while (rootCause.getCause() != null && rootCause != rootCause.getCause()) {
            rootCause = rootCause.getCause();
        }

        String errorCrudo = rootCause.getMessage() != null ? rootCause.getMessage() : ex.getMessage();
        String mensajeLimpio = "Ocurrió un error inesperado en el servidor.";

        if (errorCrudo != null) {
            mensajeLimpio = errorCrudo.replaceAll("org.hibernate.exception.GenericJDBCException: ", "").split("\n")[0];
        }

        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error", mensajeLimpio);
    }

    private ResponseEntity<Map<String, Object>> buildResponse(HttpStatus status, String error, String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now());
        response.put("status", status.value());
        response.put("error", error);
        response.put("message", message);
        return new ResponseEntity<>(response, status);
    }
}