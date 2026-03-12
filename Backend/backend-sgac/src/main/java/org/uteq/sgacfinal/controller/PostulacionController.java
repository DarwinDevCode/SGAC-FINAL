package org.uteq.sgacfinal.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.MediaType;
import org.springframework.web.multipart.MultipartFile;
import org.uteq.sgacfinal.dto.Request.LogAuditoriaRequestDTO;
import org.uteq.sgacfinal.dto.Request.PostulacionRequestDTO;
import org.uteq.sgacfinal.dto.Response.TipoRequisitoPostulacionResponseDTO;
import org.uteq.sgacfinal.service.ILogAuditoriaService;
import org.uteq.sgacfinal.service.IPostulacionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.uteq.sgacfinal.service.ITipoRequisitoPostulacionService;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;

@RestController
@RequestMapping("/api/postulaciones")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class PostulacionController {
    private final IPostulacionService postulacionService;
    private final ITipoRequisitoPostulacionService requisitoService;
    private final ILogAuditoriaService logAuditoriaService;

    @PostMapping(value = "/registrar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> registrar(
            @RequestPart("datos") String datosJson,
            @RequestPart("archivos") List<MultipartFile> archivos,
            @RequestParam("tiposRequisito") List<Integer> tiposRequisito
    ) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            PostulacionRequestDTO request = mapper.readValue(datosJson, PostulacionRequestDTO.class);
            if (archivos.size() != tiposRequisito.size())
                return ResponseEntity.badRequest().body("La cantidad de archivos no coincide con los requisitos enviados.");
            return ResponseEntity.ok(postulacionService.registrarPostulacionCompleta(request, archivos, tiposRequisito));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error interno: " + e.getMessage());
        }
    }




    @GetMapping("/listar-activos")
    public ResponseEntity<List<TipoRequisitoPostulacionResponseDTO>> listar() {
        return ResponseEntity.ok(requisitoService.listarRequisitosActivos());
    }

    @PutMapping("/cambiar-estado/{id}")
    public ResponseEntity<?> cambiarEstado(@PathVariable Integer id,
                                           @RequestParam String estado,
                                           @RequestParam String observacion,
                                           @RequestParam(required = false) Integer idCoordinador,
                                           HttpServletRequest request) {
        try {
            postulacionService.actualizarEstado(id, estado, observacion);
            // Audit log
            try {
                if (idCoordinador != null) {
                    logAuditoriaService.registrar(LogAuditoriaRequestDTO.builder()
                            .idUsuario(idCoordinador)
                            .accion("VALIDAR_POSTULACION_" + estado.toUpperCase())
                            .tablaAfectada("postulacion")
                            .registroAfectado(id)
                            .ipOrigen(request.getRemoteAddr())
                            .valorAnterior("Postulación ID: " + id)
                            .valorNuevo("Estado: " + estado + " | Obs: " + observacion)
                            .build());
                }
            } catch (Exception ignored) {}
            return ResponseEntity.ok("Estado actualizado correctamente.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: " + e.getMessage());
        }
    }

    @GetMapping("/mis-postulaciones/{idEstudiante}")
    public ResponseEntity<?> listarPorEstudiante(@PathVariable Integer idEstudiante) {
        try {
            return ResponseEntity.ok(postulacionService.listarPorEstudiante(idEstudiante));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al listar postulaciones: " + e.getMessage());
        }
    }

    @GetMapping("/convocatoria/{idConvocatoria}")
    public ResponseEntity<?> listarPorConvocatoria(@PathVariable Integer idConvocatoria) {
        try {
            return ResponseEntity.ok(postulacionService.listarPorConvocatoria(idConvocatoria));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al listar postulaciones: " + e.getMessage());
        }
    }

    @GetMapping("/carrera/{idCarrera}")
    public ResponseEntity<?> listarPorCarrera(@PathVariable Integer idCarrera) {
        try {
            return ResponseEntity.ok(postulacionService.listarPorCarrera(idCarrera));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al listar: " + e.getMessage());
        }
    }

    @GetMapping("/pendientes/carrera/{idCarrera}")
    public ResponseEntity<?> listarPendientesPorCarrera(@PathVariable Integer idCarrera) {
        try {
            return ResponseEntity.ok(postulacionService.listarPendientesPorCarrera(idCarrera));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al listar pendientes: " + e.getMessage());
        }
    }

    @GetMapping("/en-evaluacion/carrera/{idCarrera}")
    public ResponseEntity<?> listarEnEvaluacionPorCarrera(@PathVariable Integer idCarrera) {
        try {
            return ResponseEntity.ok(postulacionService.listarEnEvaluacionPorCarrera(idCarrera));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al listar en evaluación: " + e.getMessage());
        }
    }

    /**
     * Verifica si un estudiante (por su idUsuario) ya se postuló a una convocatoria específica.
     * GET /api/postulaciones/existe?idEstudiante={idUsuario}&idConvocatoria={id}
     */
    @GetMapping("/existe")
    public ResponseEntity<?> existe(
            @RequestParam Integer idEstudiante,
            @RequestParam Integer idConvocatoria) {
        try {
            boolean resultado = postulacionService.existePostulacion(idEstudiante, idConvocatoria);
            return ResponseEntity.ok(resultado);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al verificar postulación: " + e.getMessage());
        }
    }

    /**
     * Obtiene el detalle completo de la postulación activa del estudiante.
     * Incluye: información de postulación, convocatoria, cronograma de etapas,
     * documentos adjuntos y resumen de estados.
     * GET /api/postulaciones/mi-postulacion/{idUsuario}
     */
    @GetMapping("/mi-postulacion/{idUsuario}")
    public ResponseEntity<?> obtenerMiPostulacion(@PathVariable Integer idUsuario) {
        try {
            return ResponseEntity.ok(postulacionService.obtenerMiPostulacionActiva(idUsuario));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al obtener postulación: " + e.getMessage());
        }
    }
}
