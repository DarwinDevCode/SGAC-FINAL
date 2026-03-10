package org.uteq.sgacfinal.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.uteq.sgacfinal.dto.Request.CalificacionOposicionRequestDTO;
import org.uteq.sgacfinal.dto.Request.ConfirmarActaRequestDTO;
import org.uteq.sgacfinal.dto.Request.GenerarActaRequestDTO;
import org.uteq.sgacfinal.dto.Request.NotificationRequest;
import org.uteq.sgacfinal.entity.ResumenEvaluacion;
import org.uteq.sgacfinal.repository.PostulacionRepository;
import org.uteq.sgacfinal.repository.ResumenEvaluacionRepository;
import org.uteq.sgacfinal.service.IActaEvaluacionService;
import org.uteq.sgacfinal.service.ICalificacionOposicionService;
import org.uteq.sgacfinal.service.INotificacionService;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controlador del Módulo de Evaluación y Selección.
 *
 * Tabs del frontend:
 *   Tab 1 (Méritos)          → EvaluacionController existente
 *   Tab 2 (Oposición)        → /api/evaluacion-seleccion/oposicion/*
 *   Tab 3 (Resultados)       → /api/evaluacion-seleccion/ranking/convocatoria/v2/{id}
 *   Tab 4 (Actas/Confirmar)  → /api/evaluacion-seleccion/actas/*
 */
@RestController
@RequestMapping("/api/evaluacion-seleccion")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class EvaluacionSeleccionController {

    private final ICalificacionOposicionService oposicionService;
    private final IActaEvaluacionService actaService;
    private final ResumenEvaluacionRepository resumenRepo;
    private final PostulacionRepository postulacionRepo;
    private final INotificacionService notificacionService;

    @Value("${app.actas.ruta}")
    private String rutaActas;

    // ─────────────────────────────────────────────────────────────────────────
    // TAB 2: CALIFICACIÓN DE OPOSICIÓN INDIVIDUAL
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Guarda o actualiza la nota individual de un miembro del tribunal.
     * Cuando los 3 guardaron, calcula el promedio y notifica al postulante.
     */
    @PostMapping("/oposicion/individual")
    public ResponseEntity<?> guardarOposicionIndividual(@RequestBody CalificacionOposicionRequestDTO request) {
        try {
            return ResponseEntity.ok(oposicionService.guardarNota(request));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al guardar calificación de oposición: " + e.getMessage());
        }
    }

    /**
     * Elimina la nota individual de un miembro del tribunal.
     */
    @DeleteMapping("/oposicion/individual/{idCalificacion}")
    public ResponseEntity<?> eliminarOposicionIndividual(@PathVariable Integer idCalificacion) {
        try {
            oposicionService.eliminar(idCalificacion);
            return ResponseEntity.ok().body("Calificación individual eliminada con éxito.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al eliminar calificación individual: " + e.getMessage());
        }
    }

    /**
     * Estado actual de la oposición para una postulación.
     */
    @GetMapping("/oposicion/estado/{idPostulacion}")
    public ResponseEntity<?> obtenerEstadoOposicion(@PathVariable Integer idPostulacion) {
        try {
            return ResponseEntity.ok(oposicionService.obtenerEstado(idPostulacion));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    /**
     * Obtiene la calificación individual de un evaluador específico.
     */
    @GetMapping("/oposicion/{idPostulacion}/evaluador/{idEvaluador}")
    public ResponseEntity<?> obtenerCalificacionEvaluador(
            @PathVariable Integer idPostulacion,
            @PathVariable Integer idEvaluador) {
        try {
            return ResponseEntity.ok(oposicionService.obtenerCalificacion(idPostulacion, idEvaluador));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // TAB 3: RANKING / RESULTADOS
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Ranking completo de una convocatoria.
     */
    @GetMapping("/ranking/convocatoria/v2/{idConvocatoria}")
    public ResponseEntity<?> rankingConvocatoria(@PathVariable Integer idConvocatoria) {
        try {
            return ResponseEntity.ok(oposicionService.obtenerRankingConvocatoria(idConvocatoria));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al calcular ranking: " + e.getMessage());
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // TAB 4: ACTAS / CONFIRMAR
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Genera el acta (por ahora, méritos).
     */
    @PostMapping("/actas/generar")
    public ResponseEntity<?> generarActa(@RequestBody GenerarActaRequestDTO request) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(actaService.generarActa(request));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al generar acta: " + e.getMessage());
        }
    }

    /**
     * Lista todas las actas de una postulación.
     */
    @GetMapping("/actas/{idPostulacion}")
    public ResponseEntity<?> listarActas(@PathVariable Integer idPostulacion) {
        try {
            return ResponseEntity.ok(actaService.listarPorPostulacion(idPostulacion));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al listar actas: " + e.getMessage());
        }
    }

    /**
     * Confirma el acta por parte de un evaluador.
     */
    @PostMapping("/actas/confirmar")
    public ResponseEntity<?> confirmarActa(@RequestBody ConfirmarActaRequestDTO request) {
        try {
            return ResponseEntity.ok(actaService.confirmarActa(request));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al confirmar acta: " + e.getMessage());
        }
    }

    /**
     * Elimina un acta específica.
     */
    @DeleteMapping("/actas/{idActa}")
    public ResponseEntity<?> eliminarActa(@PathVariable Integer idActa) {
        try {
            actaService.eliminar(idActa);
            return ResponseEntity.ok().body("Acta de evaluación eliminada correctamente.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al eliminar acta: " + e.getMessage());
        }
    }

    /**
     * Abre el archivo PDF del acta generado.
     * Ejemplo:
     * GET /api/evaluacion-seleccion/actas/archivo/meritos_1_Adrian_Garcia.pdf
     */
    @GetMapping("/actas/archivo/{nombreArchivo:.+}")
    public ResponseEntity<?> verArchivoActa(@PathVariable String nombreArchivo) {
        try {
            Path ruta = Paths.get(rutaActas)
                    .toAbsolutePath()
                    .normalize()
                    .resolve(nombreArchivo)
                    .normalize();

            Resource recurso = new UrlResource(ruta.toUri());

            if (!recurso.exists() || !recurso.isReadable()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("No se encontró el archivo del acta.");
            }

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_TYPE, "application/pdf")
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + recurso.getFilename() + "\"")
                    .body(recurso);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al abrir el archivo del acta: " + e.getMessage());
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // PUBLICAR RESULTADOS
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Publica los resultados de una convocatoria y notifica a cada postulante.
     */
    @PostMapping("/publicar-resultados/{idConvocatoria}")
    public ResponseEntity<?> publicarResultados(@PathVariable Integer idConvocatoria) {
        try {
            List<ResumenEvaluacion> resumenes = resumenRepo.findAllByConvocatoria(idConvocatoria);

            if (resumenes.isEmpty()) {
                return ResponseEntity.badRequest().body("No hay evaluaciones completadas para esta convocatoria.");
            }

            int notificados = 0;
            for (ResumenEvaluacion r : resumenes) {
                try {
                    Integer idUsuario = r.getPostulacion().getEstudiante().getUsuario().getIdUsuario();
                    BigDecimal total = r.getTotalFinal() != null ? r.getTotalFinal() : BigDecimal.ZERO;
                    String estado = r.getEstado() != null ? r.getEstado() : "PENDIENTE";

                    String titulo;
                    String mensaje;

                    switch (estado) {
                        case "GANADOR" -> {
                            titulo = "¡Resultado de Concurso de Méritos y Oposición!";
                            mensaje = "¡Felicitaciones! Has obtenido " + total
                                    + " / 40 puntos y has sido seleccionado(a) como Ayudante de Cátedra.";
                        }
                        case "APTO" -> {
                            titulo = "Resultado de Concurso de Méritos y Oposición";
                            mensaje = "Has aprobado el concurso con " + total
                                    + " / 40 puntos. Tu postulación quedó en la lista de elegibles.";
                        }
                        case "NO_APTO" -> {
                            titulo = "Resultado de Concurso de Méritos y Oposición";
                            mensaje = "Has obtenido " + total
                                    + " / 40 puntos. Lamentablemente no alcanzaste el puntaje mínimo requerido de 25 puntos.";
                        }
                        case "DESIERTO" -> {
                            titulo = "Concurso Declarado Desierto";
                            mensaje = "El concurso de méritos y oposición ha sido declarado desierto. "
                                    + "Ningún postulante alcanzó el puntaje mínimo de 25 puntos.";
                        }
                        default -> {
                            titulo = "Actualización de tu Postulación";
                            mensaje = "Los resultados de la evaluación han sido publicados. "
                                    + "Tu puntaje total fue " + total + " / 40.";
                        }
                    }

                    notificacionService.enviarNotificacion(idUsuario, NotificationRequest.builder()
                            .titulo(titulo)
                            .mensaje(mensaje)
                            .tipo("RESULTADO_EVALUACION")
                            .idReferencia(r.getPostulacion().getIdPostulacion())
                            .build());

                    notificados++;
                } catch (Exception ex) {
                    System.err.println("Error notificando al postulante "
                            + r.getPostulacion().getIdPostulacion() + ": " + ex.getMessage());
                }
            }

            Map<String, Object> resp = new HashMap<>();
            resp.put("mensaje", "Resultados publicados");
            resp.put("totalPostulantes", resumenes.size());
            resp.put("notificados", notificados);

            return ResponseEntity.ok(resp);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al publicar resultados: " + e.getMessage());
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // RESULTADO INDIVIDUAL
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * El estudiante consulta su propio resultado.
     */
    @GetMapping("/resultado-postulante/{idPostulacion}")
    public ResponseEntity<?> obtenerResultadoPostulante(@PathVariable Integer idPostulacion) {
        try {
            return resumenRepo.findByIdPostulacion(idPostulacion)
                    .map(r -> {
                        Map<String, Object> res = new HashMap<>();
                        res.put("idPostulacion", idPostulacion);
                        res.put("totalMeritos", r.getTotalMeritos());
                        res.put("promedioOposicion", r.getPromedioOposicion());
                        res.put("totalFinal", r.getTotalFinal());
                        res.put("estado", r.getEstado());
                        res.put("posicion", r.getPosicion());
                        res.put("aprobado", r.getTotalFinal() != null
                                && r.getTotalFinal().compareTo(BigDecimal.valueOf(25)) >= 0);
                        return ResponseEntity.ok(res);
                    })
                    .orElse(ResponseEntity.notFound().build());

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al obtener resultado: " + e.getMessage());
        }
    }
}