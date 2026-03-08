package org.uteq.sgacfinal.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;
import org.uteq.sgacfinal.dto.Request.EvaluacionMeritosRequestDTO;
import org.uteq.sgacfinal.dto.Request.EvaluacionOposicionRequestDTO;
import org.uteq.sgacfinal.service.IEvaluacionMeritosService;
import org.uteq.sgacfinal.service.IEvaluacionOposicionService;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/evaluaciones")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class EvaluacionController {

    private final IEvaluacionMeritosService evaluacionMeritosService;
    private final IEvaluacionOposicionService evaluacionOposicionService;
    private final JdbcTemplate jdbcTemplate;

    @PostMapping("/meritos")
    public ResponseEntity<?> registrarMeritos(@RequestBody EvaluacionMeritosRequestDTO request) {
        try {
            return ResponseEntity.ok(evaluacionMeritosService.crear(request));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al registrar evaluación de méritos: " + e.getMessage());
        }
    }

    @PostMapping("/oposicion")
    public ResponseEntity<?> registrarOposicion(@RequestBody EvaluacionOposicionRequestDTO request) {
        try {
            return ResponseEntity.ok(evaluacionOposicionService.crear(request));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al registrar evaluación de oposición: " + e.getMessage());
        }
    }

    @GetMapping("/meritos/postulacion/{idPostulacion}")
    public ResponseEntity<?> obtenerMeritosPorPostulacion(@PathVariable Integer idPostulacion) {
        try {
            return ResponseEntity.ok(evaluacionMeritosService.buscarPorPostulacion(idPostulacion));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @GetMapping("/oposicion/postulacion/{idPostulacion}")
    public ResponseEntity<?> obtenerOposicionPorPostulacion(@PathVariable Integer idPostulacion) {
        try {
            return ResponseEntity.ok(evaluacionOposicionService.buscarPorPostulacion(idPostulacion));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @DeleteMapping("/meritos/{id}")
    public ResponseEntity<?> eliminarMeritos(@PathVariable Integer id) {
        try {
            evaluacionMeritosService.eliminar(id);
            return ResponseEntity.ok().body("Evaluación de méritos eliminada correctamente.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al eliminar evaluación de méritos: " + e.getMessage());
        }
    }

    /**
     * P13 (Ítem 15): Ranking completo de una convocatoria.
     * GET /api/evaluaciones/ranking/convocatoria/{idConvocatoria}
     */
    @GetMapping("/ranking/convocatoria/{idConvocatoria}")
    public ResponseEntity<?> rankingPorConvocatoria(@PathVariable Integer idConvocatoria) {
        try {
            List<Map<String, Object>> resultado = jdbcTemplate.query(
                    "SELECT * FROM public.sp_ranking_convocatoria(?)",
                    (rs, rowNum) -> {
                        Map<String, Object> row = new LinkedHashMap<>();
                        row.put("idPostulacion",    rs.getInt("id_postulacion"));
                        row.put("nombreEstudiante", rs.getString("nombre_estudiante"));
                        row.put("matricula",        rs.getString("matricula"));
                        row.put("puntajeMeritos",   rs.getBigDecimal("puntaje_meritos"));
                        row.put("puntajeOposicion", rs.getBigDecimal("puntaje_oposicion"));
                        row.put("puntajeTotal",     rs.getBigDecimal("puntaje_total"));
                        row.put("posicion",         rs.getLong("posicion"));
                        return row;
                    },
                    idConvocatoria
            );
            return ResponseEntity.ok(resultado);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al calcular ranking: " + e.getMessage());
        }
    }

    /**
     * P13 (Ítem 15): Resultado final para una postulación.
     * GET /api/evaluaciones/resultado-final/{idPostulacion}
     */
    @GetMapping("/resultado-final/{idPostulacion}")
    public ResponseEntity<?> resultadoFinal(@PathVariable Integer idPostulacion) {
        try {
            List<Map<String, Object>> resultado = jdbcTemplate.query(
                    "SELECT * FROM public.sp_calcular_resultado_final(?)",
                    (rs, rowNum) -> {
                        Map<String, Object> row = new LinkedHashMap<>();
                        row.put("idPostulacion",    rs.getInt("id_postulacion"));
                        row.put("nombreEstudiante", rs.getString("nombre_estudiante"));
                        row.put("puntajeMeritos",   rs.getBigDecimal("puntaje_meritos"));
                        row.put("puntajeOposicion", rs.getBigDecimal("puntaje_oposicion"));
                        row.put("puntajeTotal",     rs.getBigDecimal("puntaje_total"));
                        row.put("posicion",         rs.getLong("posicion"));
                        return row;
                    },
                    idPostulacion
            );
            return ResponseEntity.ok(resultado.isEmpty() ? null : resultado.get(0));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al calcular resultado final: " + e.getMessage());
        }
    }

    @GetMapping("/debug/meritos")
    public ResponseEntity<?> debugMeritos() {
        try {
            List<Map<String, Object>> result = jdbcTemplate.queryForList("SELECT * FROM postulacion.evaluacion_meritos");
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(e.getMessage());
        }
    }
}
