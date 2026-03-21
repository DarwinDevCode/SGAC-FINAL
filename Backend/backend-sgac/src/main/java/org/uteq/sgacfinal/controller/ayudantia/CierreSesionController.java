package org.uteq.sgacfinal.controller.ayudantia;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.uteq.sgacfinal.dto.request.ayudantia.FinalizarSesionRequestDTO;
import org.uteq.sgacfinal.dto.response.RespuestaOperacionDTO;
import org.uteq.sgacfinal.dto.response.ayudantia.BorradorSesionResponseDTO;
import org.uteq.sgacfinal.dto.response.ayudantia.EvidenciaIdResponseDTO;
import org.uteq.sgacfinal.dto.response.ayudantia.FinalizarSesionResponseDTO;
import org.uteq.sgacfinal.service.IUsuarioSesionService;
import org.uteq.sgacfinal.service.ayudantia.CierreSesionService;

@RestController
@RequestMapping("/api/ayudantias/sesiones/cierre")
@RequiredArgsConstructor
public class CierreSesionController {

    private final CierreSesionService cierreService;
    private final IUsuarioSesionService sesionService;

    @GetMapping("/{idRegistro}/borrador")
    public ResponseEntity<RespuestaOperacionDTO<BorradorSesionResponseDTO>> obtenerBorrador(
            @PathVariable Integer idRegistro) {
        Integer idUsuario = sesionService.getIdUsuarioAutenticado();
        return ResponseEntity.ok(cierreService.obtenerBorrador(idUsuario, idRegistro));
    }

    @PutMapping("/{idRegistro}/progreso")
    public ResponseEntity<RespuestaOperacionDTO<Void>> guardarProgreso(
            @PathVariable Integer idRegistro,
            @RequestBody String descripcion) {
        Integer idUsuario = sesionService.getIdUsuarioAutenticado();
        return ResponseEntity.ok(cierreService.guardarProgreso(idUsuario, idRegistro, descripcion));
    }

    @PostMapping(value = "/{idRegistro}/evidencia", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<RespuestaOperacionDTO<EvidenciaIdResponseDTO>> cargarEvidencia(
            @PathVariable Integer idRegistro,
            @RequestParam("idTipoEvidencia") Integer idTipoEvidencia,
            @RequestPart("archivo") MultipartFile archivo) {

        Integer idUsuario = sesionService.getIdUsuarioAutenticado();
        return ResponseEntity.ok(cierreService.cargarEvidencia(idUsuario, idRegistro, idTipoEvidencia, archivo));
    }

    @DeleteMapping("/evidencia/{idEvidencia}")
    public ResponseEntity<RespuestaOperacionDTO<Void>> eliminarEvidencia(
            @PathVariable Integer idEvidencia) {
        Integer idUsuario = sesionService.getIdUsuarioAutenticado();
        return ResponseEntity.ok(cierreService.eliminarEvidencia(idUsuario, idEvidencia));
    }

    @PostMapping("/finalizar")
    public ResponseEntity<RespuestaOperacionDTO<FinalizarSesionResponseDTO>> finalizarSesion(
            @RequestBody FinalizarSesionRequestDTO request) {

        Integer idUsuario = sesionService.getIdUsuarioAutenticado();

        FinalizarSesionRequestDTO secureRequest = new FinalizarSesionRequestDTO(
                idUsuario,
                request.idRegistro(),
                request.descripcion()
        );

        return ResponseEntity.ok(cierreService.finalizarSesion(secureRequest));
    }
}