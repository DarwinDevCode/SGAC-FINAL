package org.uteq.sgacfinal.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.uteq.sgacfinal.dto.Request.CalificacionOposicionRequestDTO;
import org.uteq.sgacfinal.dto.Request.ConfirmarActaRequestDTO;
import org.uteq.sgacfinal.dto.Request.GenerarActaRequestDTO;
import org.uteq.sgacfinal.dto.Request.NotificationRequest;
import org.uteq.sgacfinal.dto.Response.RankingEvaluacionDTO;
import org.uteq.sgacfinal.entity.ResumenEvaluacion;
import org.uteq.sgacfinal.repository.PostulacionRepository;
import org.uteq.sgacfinal.repository.ResumenEvaluacionRepository;
import org.uteq.sgacfinal.service.IActaEvaluacionService;
import org.uteq.sgacfinal.service.ICalificacionOposicionService;
import org.uteq.sgacfinal.service.INotificacionService;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controlador del Módulo de Evaluación y Selección.
 *
 * Tabs del frontend:
 *   Tab 1 (Méritos)          → EvaluacionController existente  (/api/evaluaciones/meritos)
 *   Tab 2 (Oposición)        → /api/evaluaciones/oposicion/*
 *   Tab 3 (Resultados)       → /api/evaluaciones/ranking/convocatoria/v2/{id}
 *   Tab 4 (Actas/Confirmar)  → /api/evaluaciones/actas/*
 *
 *   Publicación:             → /api/evaluaciones/publicar-resultados/{idConvocatoria}
 *   Resultado del postulante → /api/evaluaciones/resultado-postulante/{idPostulacion}
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


    // ─────────────────────────────────────────────────────────────────────────
    // TAB 2: CALIFICACIÓN DE OPOSICIÓN INDIVIDUAL
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Guarda o actualiza la nota individual de un miembro del tribunal.
     * Cuando los 3 guardaron, calcula el promedio y notifica al postulante.
     * POST /api/evaluaciones/oposicion/individual
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
     * Eliminar la nota individual de un miembro del tribunal.
     * DELETE /api/evaluaciones/oposicion/individual/{idCalificacion}
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
     * Estado actual de la oposición para una postulación:
     * qué miembros han calificado y el promedio (solo si los 3 guardaron).
     * GET /api/evaluaciones/oposicion/estado/{idPostulacion}
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
     * Calificación individual de un evaluador específico (solo para que el frontend la cargue en su propio formulario).
     * GET /api/evaluaciones/oposicion/{idPostulacion}/evaluador/{idEvaluador}
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
     * Ranking completo de una convocatoria con estados (GANADOR/APTO/NO_APTO/DESIERTO)
     * y datos de desempate. Postulantes empatados tienen el flag empate=true.
     * GET /api/evaluaciones/ranking/convocatoria/{idConvocatoria}   (sobreescribe el existente)
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
     * Genera el registro del acta y su URL de descarga.
     * POST /api/evaluaciones/actas/generar
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
     * Lista todas las actas de una postulación (MERITOS y OPOSICION).
     * GET /api/evaluaciones/actas/{idPostulacion}
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
     * Confirma el acta por parte de un miembro del tribunal.
     * POST /api/evaluaciones/actas/confirmar
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
     * Eliminar un acta específica del sistema.
     * DELETE /api/evaluaciones/actas/{idActa}
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

    // ─────────────────────────────────────────────────────────────────────────
    // PUBLICAR RESULTADOS (solo COORDINADOR)
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Publica los resultados de una convocatoria y notifica a CADA postulante.
     * POST /api/evaluaciones/publicar-resultados/{idConvocatoria}
     *
     * Lógica:
     * - Recorre todos los resúmenes existentes de la convocatoria
     * - Para cada uno arma el mensaje según su estado (GANADOR / APTO / NO_APTO / DESIERTO)
     * - Llama a INotificacionService.enviarNotificacion por cada postulante
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
                    String estado   = r.getEstado() != null ? r.getEstado() : "PENDIENTE";

                    String titulo;
                    String mensaje;

                    switch (estado) {
                        case "GANADOR" -> {
                            titulo  = "¡Resultado de Concurso de Méritos y Oposición!";
                            mensaje = "¡Felicitaciones! Has obtenido " + total + " / 40 puntos y has sido seleccionado(a) como Ayudante de Cátedra.";
                        }
                        case "APTO" -> {
                            titulo  = "Resultado de Concurso de Méritos y Oposición";
                            mensaje = "Has aprobado el concurso con " + total + " / 40 puntos. Tu postulación quedó en la lista de elegibles.";
                        }
                        case "NO_APTO" -> {
                            titulo  = "Resultado de Concurso de Méritos y Oposición";
                            mensaje = "Has obtenido " + total + " / 40 puntos. Lamentablemente no alcanzaste el puntaje mínimo requerido de 25 puntos.";
                        }
                        case "DESIERTO" -> {
                            titulo  = "Concurso Declarado Desierto";
                            mensaje = "El concurso de méritos y oposición ha sido declarado desierto. Ningún postulante alcanzó el puntaje mínimo de 25 puntos.";
                        }
                        default -> {
                            titulo  = "Actualización de tu Postulación";
                            mensaje = "Los resultados de la evaluación han sido publicados. Tu puntaje total fue " + total + " / 40.";
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
                    // Log pero continúa con el siguiente
                    System.err.println("Error notificando al postulante " +
                            r.getPostulacion().getIdPostulacion() + ": " + ex.getMessage());
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
    // RESULTADO INDIVIDUAL (para el estudiante)
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * El estudiante consulta su propio resultado.
     * GET /api/evaluaciones/resultado-postulante/{idPostulacion}
     *
     * Devuelve: totalMeritos, promedioOposicion, totalFinal, estado, posicion
     */
    @GetMapping("/resultado-postulante/{idPostulacion}")
    public ResponseEntity<?> obtenerResultadoPostulante(@PathVariable Integer idPostulacion) {
        try {
            return resumenRepo.findByIdPostulacion(idPostulacion)
                    .map(r -> {
                        Map<String, Object> res = new HashMap<>();
                        res.put("idPostulacion", idPostulacion);
                        res.put("totalMeritos",     r.getTotalMeritos());
                        res.put("promedioOposicion", r.getPromedioOposicion());
                        res.put("totalFinal",        r.getTotalFinal());
                        res.put("estado",            r.getEstado());
                        res.put("posicion",          r.getPosicion());
                        res.put("aprobado",          r.getTotalFinal() != null &&
                                r.getTotalFinal().compareTo(BigDecimal.valueOf(25)) >= 0);
                        return ResponseEntity.ok(res);
                    })
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al obtener resultado: " + e.getMessage());
        }
    }
}
