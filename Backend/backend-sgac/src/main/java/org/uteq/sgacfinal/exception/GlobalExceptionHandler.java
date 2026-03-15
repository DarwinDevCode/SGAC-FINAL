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

    // ── Prefijos inyectados por los RAISE EXCEPTION de PL/pgSQL ──────────
    private static final String PG_VALIDACION = "VALIDACION:";
    private static final String PG_ACCESO     = "ACCESO:";

    // ════════════════════════════════════════════════════════════════════
    // ESCENARIO 3 — RAISE EXCEPTION de PL/pgSQL capturado aquí
    //
    // PL/pgSQL lanza una excepción con mensaje que empieza en:
    //   "VALIDACION: ..."  → 400 Bad Request
    //   "ACCESO: ..."      → 403 Forbidden
    //
    // Spring lo envuelve en uno de estos:
    //   - org.springframework.dao.DataIntegrityViolationException
    //   - org.hibernate.exception.GenericJDBCException
    //   - java.sql.SQLException
    //
    // El handler genérico (Exception.class) ya lo captura pero devolvía
    // un mensaje crudo de Hibernate. Ahora detectamos el prefijo VALIDACION
    // y devolvemos un 400 limpio y legible para el frontend.
    // ════════════════════════════════════════════════════════════════════

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, Object>> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        Throwable root = unwrap(ex);
        String raw = root.getMessage() != null ? root.getMessage() : "";

        // ── RAISE EXCEPTION con prefijo PL/pgSQL ──────────────────────
        String pgMsg = extractPgMessage(raw);
        if (pgMsg != null) {
            HttpStatus status = pgMsg.startsWith(PG_ACCESO) ? HttpStatus.FORBIDDEN : HttpStatus.BAD_REQUEST;
            String limpio = pgMsg
                    .replaceFirst("^" + PG_VALIDACION, "")
                    .replaceFirst("^" + PG_ACCESO, "")
                    .trim();
            return buildResponse(status, status == HttpStatus.FORBIDDEN ? "Acceso denegado" : "Validación fallida", limpio);
        }

        // ── Violación de UNIQUE constraint (comportamiento previo) ────
        String mensajeLimpio = "Error: Un dato único ya existe en el sistema.";
        java.util.regex.Matcher matcher = java.util.regex.Pattern
                .compile("[\"«]([a-zA-Z0-9_]+)[\"»]").matcher(raw);
        if (matcher.find()) {
            String campo = matcher.group(1)
                    .replace("usuario_", "").replace("uk_", "").replace("_key", "").toUpperCase();
            mensajeLimpio = "Error: El dato ingresado para el campo " + campo + " ya se encuentra registrado.";
        }
        return buildResponse(HttpStatus.BAD_REQUEST, "Data Integrity Violation", mensajeLimpio);
    }

    // ════════════════════════════════════════════════════════════════════

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleResourceNotFoundException(ResourceNotFoundException ex) {
        return buildResponse(HttpStatus.NOT_FOUND, "Not Found", ex.getMessage());
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<Map<String, Object>> handleBadRequestException(BadRequestException ex) {
        return buildResponse(HttpStatus.BAD_REQUEST, "Bad Request", ex.getMessage());
    }

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

    @ExceptionHandler(FaseRestriccionException.class)
    public ResponseEntity<Map<String, Object>> handleFase(FaseRestriccionException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                "timestamp", Instant.now().toString(),
                "status",    409,
                "error",     "Restricción de fase",
                "message",   ex.getMessage()
        ));
    }

    @ExceptionHandler(OposicionBusinessException.class)
    public ResponseEntity<Map<String, Object>> handleOposicion(OposicionBusinessException ex) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(Map.of(
                "timestamp", Instant.now().toString(),
                "status",    422,
                "error",     "Validación de oposición",
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

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(Exception ex) {
        Throwable root = unwrap(ex);
        String raw = root.getMessage() != null ? root.getMessage() : ex.getMessage();

        // Segundo intento: el RAISE puede llegar envuelto en una excepción genérica
        String pgMsg = extractPgMessage(raw != null ? raw : "");
        if (pgMsg != null) {
            HttpStatus status = pgMsg.startsWith(PG_ACCESO) ? HttpStatus.FORBIDDEN : HttpStatus.BAD_REQUEST;
            String limpio = pgMsg
                    .replaceFirst("^" + PG_VALIDACION, "")
                    .replaceFirst("^" + PG_ACCESO, "")
                    .trim();
            return buildResponse(status, "Validación fallida", limpio);
        }

        log.error("[500] Excepción no controlada en el servidor:", ex);
        String mensajeLimpio = "Ocurrió un error inesperado en el servidor.";
        if (raw != null) {
            mensajeLimpio = raw.replaceAll("org.hibernate.exception.GenericJDBCException: ", "").split("\n")[0];
        }
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error", mensajeLimpio);
    }

    // ── Helpers ───────────────────────────────────────────────────────

    /**
     * Busca el prefijo VALIDACION: o ACCESO: dentro del mensaje de error.
     * PostgreSQL a veces antepone "ERROR: " o "DETAIL: " al mensaje del RAISE.
     */
    private String extractPgMessage(String raw) {
        if (raw == null) return null;
        for (String prefix : new String[]{ PG_VALIDACION, PG_ACCESO }) {
            int idx = raw.indexOf(prefix);
            if (idx >= 0) {
                // Tomar desde el prefijo hasta el primer salto de línea
                String found = raw.substring(idx);
                int nl = found.indexOf('\n');
                return (nl > 0 ? found.substring(0, nl) : found).trim();
            }
        }
        return null;
    }

    private Throwable unwrap(Throwable t) {
        while (t.getCause() != null && t != t.getCause()) t = t.getCause();
        return t;
    }

    private ResponseEntity<Map<String, Object>> buildResponse(HttpStatus status, String error, String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now());
        response.put("status",    status.value());
        response.put("error",     error);
        response.put("message",   message);
        return new ResponseEntity<>(response, status);
    }
}