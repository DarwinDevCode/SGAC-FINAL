package org.uteq.sgacfinal.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleResourceNotFoundException(ResourceNotFoundException ex) {
        return buildResponse(HttpStatus.NOT_FOUND, "Not Found", ex.getMessage());
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<Map<String, Object>> handleBadRequestException(BadRequestException ex) {
        return buildResponse(HttpStatus.BAD_REQUEST, "Bad Request", ex.getMessage());
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

    /** Errores de negocio devueltos por PL/pgSQL → 422 */
    @ExceptionHandler(ConvocatoriaBusinessException.class)
    public ResponseEntity<Map<String, Object>> handleBusiness(ConvocatoriaBusinessException ex) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(Map.of(
                "timestamp", Instant.now().toString(),
                "status",    422,
                "error",     "Validación de negocio",
                "message",   ex.getMessage(),
                "codigo",    ex.getCodigo()
        ));
    }

    /** Restricción de fase del cronograma → 409 */
    @ExceptionHandler(FaseRestriccionException.class)
    public ResponseEntity<Map<String, Object>> handleFase(FaseRestriccionException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                "timestamp", Instant.now().toString(),
                "status",    409,
                "error",     "Restricción de fase",
                "message",   ex.getMessage()
        ));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
        String errores = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .collect(Collectors.joining(", "));
        return ResponseEntity.badRequest().body(Map.of(
                "timestamp", Instant.now().toString(),
                "status",    400,
                "error",     "Datos inválidos",
                "message",   errores
        ));
    }
}

