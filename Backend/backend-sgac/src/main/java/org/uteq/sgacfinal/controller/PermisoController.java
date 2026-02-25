package org.uteq.sgacfinal.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.uteq.sgacfinal.dto.PermisoDTO;
import org.uteq.sgacfinal.dto.Request.FiltroPermisosRequestDTO;
import org.uteq.sgacfinal.dto.Request.GestionPermisosRequestDTO;
import org.uteq.sgacfinal.dto.Response.MensajeResponseDTO;
import org.uteq.sgacfinal.dto.Response.PermisoRolResponseDTO;
import org.uteq.sgacfinal.service.IPermisoService;

import java.util.List;

@RestController
@RequestMapping("/api/permisos")
@RequiredArgsConstructor
public class PermisoController {

    private final IPermisoService permisoService;

    @GetMapping
    public ResponseEntity<List<PermisoDTO>> permisosActuales() {
        return ResponseEntity.ok(permisoService.obtenerPermisos());
    }

    @GetMapping("/consultar")
    public ResponseEntity<List<PermisoRolResponseDTO>> consultarPermisosRol(@Valid FiltroPermisosRequestDTO filtro){
        List<PermisoRolResponseDTO> respuesta = permisoService.consultarPermisos(filtro);
        return ResponseEntity.ok(respuesta);
    }

    @PostMapping("/gestionar")
    public ResponseEntity<MensajeResponseDTO> gestionarPermiso(@Valid @RequestBody GestionPermisosRequestDTO request) {

        Boolean exito = permisoService.gestionarPermiso(request);

        if (exito) {
            String accion = request.getOtorgar() ? "otorgado" : "revocado";
            return ResponseEntity.ok(
                    new MensajeResponseDTO("Permiso " + accion + " exitosamente.", true)
            );
        } else
            return ResponseEntity.badRequest().body(
                    new MensajeResponseDTO("Ocurrió un error al intentar gestionar el permiso.", false)
            );
    }
}
