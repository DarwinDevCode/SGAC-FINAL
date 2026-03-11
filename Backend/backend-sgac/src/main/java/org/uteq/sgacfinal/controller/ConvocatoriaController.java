package org.uteq.sgacfinal.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.uteq.sgacfinal.dto.Request.ConvocatoriaRequestDTO;
import org.uteq.sgacfinal.dto.Request.LogAuditoriaRequestDTO;
import org.uteq.sgacfinal.dto.Response.ConvocatoriaResponseDTO;
import org.uteq.sgacfinal.service.IConvocatoriaService;
import org.uteq.sgacfinal.service.ILogAuditoriaService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/convocatorias")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class ConvocatoriaController {

    private final IConvocatoriaService convocatoriaService;
    private final ILogAuditoriaService logAuditoriaService;

    @GetMapping("/listar-vista")
    public ResponseEntity<List<ConvocatoriaResponseDTO>> listarTodo() {
        return ResponseEntity.ok(convocatoriaService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ConvocatoriaResponseDTO> obtenerPorId(@PathVariable Integer id) {
        return ResponseEntity.ok(convocatoriaService.findById(id));
    }

    @PostMapping("/crear")
    public ResponseEntity<?> crear(@RequestBody ConvocatoriaRequestDTO dto,
                                   @RequestParam(required = false) Integer idCoordinador,
                                   HttpServletRequest request) {
        try {
            ConvocatoriaResponseDTO resultado = convocatoriaService.create(dto);
            registrarLog(idCoordinador, "CREAR_CONVOCATORIA",
                    "convocatoria", resultado.getIdConvocatoria(),
                    null, "Asignatura: " + resultado.getNombreAsignatura(), request);
            return new ResponseEntity<>(resultado, HttpStatus.CREATED);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error al crear convocatoria: " + e.getMessage()));
        }
    }

    @PutMapping("/actualizar")
    public ResponseEntity<?> actualizar(@RequestBody ConvocatoriaRequestDTO dto,
                                        @RequestParam(required = false) Integer idCoordinador,
                                        HttpServletRequest request) {
        try {
            ConvocatoriaResponseDTO resultado = convocatoriaService.update(dto);
            registrarLog(idCoordinador, "ACTUALIZAR_CONVOCATORIA",
                    "convocatoria", resultado.getIdConvocatoria(),
                    "Estado previo: " + resultado.getEstado(), "Actualización de datos", request);
            return ResponseEntity.ok(resultado);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error al actualizar convocatoria: " + e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(@PathVariable Integer id,
                                      @RequestParam(required = false) Integer idCoordinador,
                                      HttpServletRequest request) {
        try {
            convocatoriaService.delete(id);
            registrarLog(idCoordinador, "ELIMINAR_CONVOCATORIA",
                    "convocatoria", id, "Convocatoria ID: " + id, null, request);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error al eliminar convocatoria: " + e.getMessage()));
        }
    }

    private void registrarLog(Integer idUsuario, String accion, String tabla,
                               Integer idRegistro, String valorAnterior, String valorNuevo,
                               HttpServletRequest request) {
        try {
            if (idUsuario == null) return;
            logAuditoriaService.registrar(LogAuditoriaRequestDTO.builder()
                    .idUsuario(idUsuario)
                    .accion(accion)
                    .tablaAfectada(tabla)
                    .registroAfectado(idRegistro)
                    .ipOrigen(request.getRemoteAddr())
                    .valorAnterior(valorAnterior)
                    .valorNuevo(valorNuevo)
                    .build());
        } catch (Exception ignored) {
            // El log no debe bloquear la operación principal
        }
    }
}
