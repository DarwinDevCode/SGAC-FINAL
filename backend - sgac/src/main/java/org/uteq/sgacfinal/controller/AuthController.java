package org.uteq.sgacfinal.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.uteq.sgacfinal.dto.Request.LoginRequestDTO;
import org.uteq.sgacfinal.dto.Request.*;
import org.uteq.sgacfinal.dto.Response.MensajeResponseDTO;
import org.uteq.sgacfinal.dto.Response.UsuarioResponseDTO;
import org.uteq.sgacfinal.security.JwtService;
import org.uteq.sgacfinal.service.IAuthService;
import org.uteq.sgacfinal.service.IUsuariosService;

import java.util.List;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final IAuthService authService;
    private final IUsuariosService usuarioService;
    private final JwtService jwtService;

    private ResponseEntity<MensajeResponseDTO> exito(String mensaje) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new MensajeResponseDTO(mensaje, true));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<MensajeResponseDTO> handleException(Exception e) {
        String errorLimpio = e.getMessage().replaceAll("org.hibernate.exception.GenericJDBCException: ", "");
        return ResponseEntity.badRequest().body(new MensajeResponseDTO(errorLimpio, false));
    }

    @PostMapping("/registro-estudiante")
    public ResponseEntity<MensajeResponseDTO> regEstudiante(@Valid @RequestBody RegistroEstudianteRequestDTO dto) {
        usuarioService.registrarEstudiante(dto);
        return exito("Estudiante registrado correctamente.");
    }

    @PostMapping("/registro-docente")
    public ResponseEntity<MensajeResponseDTO> regDocente(@Valid @RequestBody RegistroDocenteRequestDTO dto) {
        usuarioService.registrarDocente(dto);
        return exito("Docente registrado correctamente.");
    }

    @PostMapping("/registro-decano")
    public ResponseEntity<MensajeResponseDTO> regDecano(@Valid @RequestBody RegistroDecanoRequestDTO dto) {
        usuarioService.registrarDecano(dto);
        return exito("Decano registrado correctamente.");
    }

    @PostMapping("/registro-coordinador")
    public ResponseEntity<MensajeResponseDTO> regCoordinador(@Valid @RequestBody RegistroCoordinadorRequestDTO dto) {
        usuarioService.registrarCoordinador(dto);
        return exito("Coordinador registrado correctamente.");
    }

    @PostMapping("/registro-admin")
    public ResponseEntity<MensajeResponseDTO> regAdmin(@Valid @RequestBody RegistroAdministradorRequest dto) {
        usuarioService.registrarAdministrador(dto);
        return exito("Administrador registrado correctamente.");
    }

    @PostMapping("/registro-ayudante-directo")
    public ResponseEntity<MensajeResponseDTO> regAyudante(@Valid @RequestBody RegistroAyudanteCatedraRequestDTO dto) {
        usuarioService.registrarAyudanteDirecto(dto);
        return exito("Ayudante directo creado correctamente.");
    }

    @PostMapping("/promover-estudiante")
    public ResponseEntity<MensajeResponseDTO> promover(@Valid @RequestBody PromoverEstudianteAyudanteRequest dto) {
        usuarioService.promoverEstudiante(dto);
        return exito("Estudiante promovido a ayudante correctamente.");
    }

    @PostMapping("/login")
    public ResponseEntity<UsuarioResponseDTO> login(@RequestBody LoginRequestDTO request) {
        UsuarioResponseDTO usuario = authService.loginUsuario(request);
        String token = jwtService.generateToken(usuario.getNombreUsuario(), usuario.getRolActual());
        usuario.setToken(token);
        return ResponseEntity.ok(usuario);
    }

    @GetMapping
    public ResponseEntity<List<UsuarioResponseDTO>> listar() {
        return ResponseEntity.ok(usuarioService.listarTodos());
    }

    @PatchMapping("/{id}/estado")
    public ResponseEntity<Void> cambiarEstadoGlobal(@PathVariable Integer id) {
        usuarioService.cambiarEstadoGlobal(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{idUsuario}/roles/{idRol}/estado")
    public ResponseEntity<Void> cambiarEstadoRol(
            @PathVariable Integer idUsuario,
            @PathVariable Integer idRol) {
        usuarioService.cambiarEstadoRol(idUsuario, idRol);
        return ResponseEntity.noContent().build();
    }
}
