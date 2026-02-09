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
import org.uteq.sgacfinal.repository.IRegistroUsuariosRepository;
import org.uteq.sgacfinal.security.JwtService;
import org.uteq.sgacfinal.service.IAuthService;
import org.uteq.sgacfinal.service.IRegistroUsuariosService;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final IAuthService authService;
    private final IRegistroUsuariosService registroUsuariosService;
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
        registroUsuariosService.registrarEstudiante(dto);
        return exito("Estudiante registrado correctamente.");
    }

    @PostMapping("/registro-docente")
    public ResponseEntity<MensajeResponseDTO> regDocente(@Valid @RequestBody RegistroDocenteRequestDTO dto) {
        registroUsuariosService.registrarDocente(dto);
        return exito("Docente registrado correctamente.");
    }

    @PostMapping("/registro-decano")
    public ResponseEntity<MensajeResponseDTO> regDecano(@Valid @RequestBody RegistroDecanoRequestDTO dto) {
        registroUsuariosService.registrarDecano(dto);
        return exito("Decano registrado correctamente.");
    }

    @PostMapping("/registro-coordinador")
    public ResponseEntity<MensajeResponseDTO> regCoordinador(@Valid @RequestBody RegistroCoordinadorRequestDTO dto) {
        registroUsuariosService.registrarCoordinador(dto);
        return exito("Coordinador registrado correctamente.");
    }

    @PostMapping("/registro-admin")
    public ResponseEntity<MensajeResponseDTO> regAdmin(@Valid @RequestBody RegistroAdministradorRequest dto) {
        registroUsuariosService.registrarAdministrador(dto);
        return exito("Administrador registrado correctamente.");
    }

    @PostMapping("/registro-ayudante-directo")
    public ResponseEntity<MensajeResponseDTO> regAyudante(@Valid @RequestBody RegistroAyudanteCatedraRequestDTO dto) {
        registroUsuariosService.registrarAyudanteDirecto(dto);
        return exito("Ayudante directo creado correctamente.");
    }

    @PostMapping("/promover-estudiante")
    public ResponseEntity<MensajeResponseDTO> promover(@Valid @RequestBody PromoverEstudianteAyudanteRequest dto) {
        registroUsuariosService.promoverEstudiante(dto);
        return exito("Estudiante promovido a ayudante correctamente.");
    }

    @PostMapping("/login")
    public ResponseEntity<UsuarioResponseDTO> login(@RequestBody LoginRequestDTO request) {
        UsuarioResponseDTO usuario = authService.loginUsuario(request);
        String token = jwtService.generateToken(usuario.getNombreUsuario(), usuario.getRolActual());
        usuario.setToken(token);
        return ResponseEntity.ok(usuario);
    }



}
