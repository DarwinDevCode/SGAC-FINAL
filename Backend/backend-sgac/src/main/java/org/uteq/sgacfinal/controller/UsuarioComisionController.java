package org.uteq.sgacfinal.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.uteq.sgacfinal.dto.Request.UsuarioComisionRequestDTO;
import org.uteq.sgacfinal.dto.Response.UsuarioComisionResponseDTO;
import org.uteq.sgacfinal.service.IUsuarioComisionService;

import java.util.List;

@RestController
@RequestMapping("/api/comisiones/integrantes")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class UsuarioComisionController {

    private final IUsuarioComisionService usuarioComisionService;

    /** POST /api/comisiones/integrantes — asignar un integrante a la comisión */
    @PostMapping
    public ResponseEntity<?> asignar(@RequestBody UsuarioComisionRequestDTO request) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(usuarioComisionService.asignarEvaluador(request));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al asignar integrante: " + e.getMessage());
        }
    }

    /** GET /api/comisiones/integrantes/comision/{id} — listar integrantes de una comisión */
    @GetMapping("/comision/{id}")
    public ResponseEntity<?> listar(@PathVariable Integer id) {
        try {
            List<UsuarioComisionResponseDTO> lista = usuarioComisionService.listarPorComision(id);
            return ResponseEntity.ok(lista);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al listar integrantes: " + e.getMessage());
        }
    }

    /** DELETE /api/comisiones/integrantes/{id} — remover integrante */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> remover(@PathVariable Integer id) {
        try {
            usuarioComisionService.removerEvaluador(id);
            return ResponseEntity.ok("Integrante removido correctamente.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al remover integrante: " + e.getMessage());
        }
    }
}
