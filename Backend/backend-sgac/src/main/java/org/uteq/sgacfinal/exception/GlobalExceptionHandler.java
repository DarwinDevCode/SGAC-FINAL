package org.uteq.sgacfinal.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import jakarta.validation.ConstraintViolationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler({ResourceNotFoundException.class, RecursoNoEncontradoException.class})
    public ResponseEntity<Map<String, Object>> handleResourceNotFoundException(RuntimeException ex) {
        return buildResponse(HttpStatus.NOT_FOUND, "Not Found", ex.getMessage());
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<Map<String, Object>> handleBadRequestException(BadRequestException ex) {
        log.warn("[400] Bad Request: {}", ex.getMessage());
        return buildResponse(HttpStatus.BAD_REQUEST, "Bad Request", ex.getMessage());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> handleHttpMessageNotReadableException(HttpMessageNotReadableException ex) {
        log.warn("[400] Malformed JSON: {}", ex.getMessage());
        return buildResponse(HttpStatus.BAD_REQUEST, "Malformed JSON", "Error al procesar el JSON: " + ex.getMostSpecificCause().getMessage());
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Map<String, Object>> handleConstraintViolationException(ConstraintViolationException ex) {
        log.warn("[400] Constraint Violation: {}", ex.getMessage());
        return buildResponse(HttpStatus.BAD_REQUEST, "Validation Error", ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, Object>> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException ex) {
        log.warn("[400] Type Mismatch: {}", ex.getMessage());
        return buildResponse(HttpStatus.BAD_REQUEST, "Type Mismatch", String.format("El parámetro '%s' debe ser de tipo %s", ex.getName(), ex.getRequiredType().getSimpleName()));
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
        String mensajeLimpio = "Error de integridad de datos en el servidor.";
        String errorTipo = "Data Integrity Violation";

        Throwable rootCause = ex.getRootCause() != null ? ex.getRootCause() : ex;
        String errorCrudo = rootCause.getMessage();
        
        log.error("[DB-INTEGRITY] Error de integridad detallado: {}", errorCrudo);

        if (errorCrudo != null) {
            boolean esDuplicado = errorCrudo.toLowerCase().contains("unique") || errorCrudo.toLowerCase().contains("duplicate");
            boolean esNulo = errorCrudo.toLowerCase().contains("null") && errorCrudo.toLowerCase().contains("not-null");

            java.util.regex.Matcher matcher = java.util.regex.Pattern.compile("[\"«]([a-zA-Z0-9_]+)[\"»]").matcher(errorCrudo);

            if (matcher.find()) {
                String campoRaw = matcher.group(1);
                String campo = campoRaw
                        .replace("usuario_", "")
                        .replace("uk_", "")
                        .replace("_key", "")
                        .toUpperCase();

                if (esDuplicado) {
                    mensajeLimpio = "Error: El dato ingresado para el campo " + campo + " ya se encuentra registrado.";
                } else if (esNulo) {
                    mensajeLimpio = "Error: El campo " + campo + " es obligatorio y no puede estar vacío.";
                } else {
                    mensajeLimpio = "Error de integridad en el campo " + campo + ".";
                }
            }
        }

        return buildResponse(HttpStatus.BAD_REQUEST, errorTipo, mensajeLimpio);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(Exception ex) {
        // Log completo para diagnóstico
        log.error("[500] Excepción no controlada en el servidor:", ex);

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