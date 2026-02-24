package org.uteq.sgacfinal.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.uteq.sgacfinal.security.UserContext;
import org.uteq.sgacfinal.dto.request.*;
import org.uteq.sgacfinal.dto.response.TipoRolResponseDTO;
import org.uteq.sgacfinal.dto.response.UsuarioResponseDTO;
import org.uteq.sgacfinal.entity.Usuario;
import org.uteq.sgacfinal.entity.UsuarioTipoRol;
import org.uteq.sgacfinal.entity.UsuarioTipoRolId;
import org.uteq.sgacfinal.repository.IUsuarioTipoRolRepository;
import org.uteq.sgacfinal.repository.IUsuariosRepository;
import org.uteq.sgacfinal.security.UsuarioPrincipal;
import org.uteq.sgacfinal.service.EmailService;
import org.uteq.sgacfinal.service.IUsuariosService;
import org.uteq.sgacfinal.util.PasswordGenerator;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class UsuariosImpl implements IUsuariosService, UserDetailsService {

    private final IUsuariosRepository usuarioRepository;
    private final IUsuarioTipoRolRepository usuarioTipoRolRepository;
    private final EntityManager entityManager;
    private final PasswordEncoder passwordEncoder;
    private final PasswordGenerator passwordGenerator;
    private final EmailService emailService;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public void registrarUsuarioGlobal(RegistroUsuarioGlobalRequest dto) {
        applyCurrentDbRole();

        String plainPassword = passwordGenerator.generate();
        String hashedPassword = passwordEncoder.encode(plainPassword);

        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("nombres",       dto.getNombres());
            payload.put("apellidos",     dto.getApellidos());
            payload.put("cedula",        dto.getCedula());
            payload.put("correo",        dto.getCorreo());
            payload.put("username",      dto.getUsername());
            payload.put("passwordHash",  hashedPassword);
            payload.put("passwordPlain", plainPassword);
            payload.put("rolesIds",      dto.getRolesIds());

            // Campos opcionales (solo si están presentes)
            if (dto.getIdCarrera()     != null) payload.put("idCarrera",     dto.getIdCarrera());
            if (dto.getMatricula()     != null) payload.put("matricula",     dto.getMatricula());
            if (dto.getSemestre()      != null) payload.put("semestre",      dto.getSemestre());
            if (dto.getIdFacultad()    != null) payload.put("idFacultad",    dto.getIdFacultad());
            if (dto.getHorasAyudante() != null) payload.put("horasAyudante", dto.getHorasAyudante());

            String payloadJson = objectMapper.writeValueAsString(payload);

            String resultJson = usuarioRepository.registrarUsuarioGlobal(payloadJson);
            log.info("[UsuariosImpl] fn_registrar_usuario_global result: {}", resultJson);

            Map<String, Object> result = objectMapper.readValue(resultJson, new TypeReference<>() {});
            Boolean exito = (Boolean) result.get("exito");
            if (!Boolean.TRUE.equals(exito)) {
                throw new RuntimeException("La BD reportó error al registrar el usuario.");
            }

            List<String> nombresRoles = resolveRoleNames(dto.getRolesIds());

            String nombreCompleto = dto.getNombres() + " " + dto.getApellidos();
            emailService.enviarCredenciales(
                    dto.getCorreo(),
                    nombreCompleto,
                    dto.getUsername().toLowerCase(),
                    plainPassword,
                    nombresRoles
            );

        } catch (RuntimeException e) {
            throw e; // re-lanzar sin envolver
        } catch (Exception e) {
            throw new RuntimeException("Error al serializar payload de registro: " + e.getMessage(), e);
        }
    }

    private List<String> resolveRoleNames(List<Integer> rolesIds) {
        try {
            String json = usuarioRepository.listarRolesActivosJson();
            List<Map<String, Object>> roles = objectMapper.readValue(json, new TypeReference<>() {});
            return roles.stream()
                    .filter(r -> rolesIds.contains(((Number) r.get("idTipoRol")).intValue()))
                    .map(r -> (String) r.get("nombreTipoRol"))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.warn("[UsuariosImpl] No se pudieron resolver nombres de roles: {}", e.getMessage());
            return rolesIds.stream().map(Object::toString).collect(Collectors.toList());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<TipoRolResponseDTO> listarRolesActivos() {
        try {
            String json = usuarioRepository.listarRolesActivosJson();
            List<Map<String, Object>> raw = objectMapper.readValue(json, new TypeReference<>() {});
            return raw.stream()
                    .map(m -> TipoRolResponseDTO.builder()
                            .idTipoRol(((Number) m.get("idTipoRol")).intValue())
                            .nombreTipoRol((String) m.get("nombreTipoRol"))
                            .activo(true)
                            .build())
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("Error al listar roles activos: " + e.getMessage(), e);
        }
    }

    private void applyCurrentDbRole() {
        String username = UserContext.getUsername();
        if (username == null || username.isBlank()) {
            throw new IllegalStateException("No hay usuario autenticado para aplicar rol de BD");
        }
        String dbRole = resolveDbRole(username, UserContext.getAppRole());
        entityManager.createNativeQuery("SELECT set_config('role', ?1, true)")
                .setParameter(1, dbRole)
                .getSingleResult();
    }

    private String resolveDbRole(String username, String appRole) {
        if (appRole == null || appRole.isBlank()) return username.toLowerCase();
        String normalized = appRole.trim().toUpperCase().replaceFirst("^ROLE_", "");
        return switch (normalized) {
            case "ADMINISTRADOR"    -> "role_administrador";
            case "DECANO"           -> "role_decano";
            case "COORDINADOR"      -> "role_coordinador";
            case "DOCENTE"          -> "role_docente";
            case "ESTUDIANTE"       -> "role_estudiante";
            case "AYUDANTE_CATEDRA" -> "role_ayudante_catedra";
            default                 -> username.toLowerCase();
        };
    }

    @Override @Transactional
    public void registrarEstudiante(RegistroEstudianteRequestDTO dto) {
        applyCurrentDbRole();
        usuarioRepository.registrarEstudiante(
                dto.getNombres(), dto.getApellidos(), dto.getCedula(), dto.getCorreo(),
                dto.getUsername(), dto.getPassword(), dto.getIdCarrera(),
                dto.getMatricula(), dto.getSemestre());
    }

    @Override @Transactional
    public void registrarDocente(RegistroDocenteRequestDTO dto) {
        applyCurrentDbRole();
        usuarioRepository.registrarDocente(
                dto.getNombres(), dto.getApellidos(), dto.getCedula(),
                dto.getCorreo(), dto.getUsername(), dto.getPassword());
    }

    @Override @Transactional
    public void registrarDecano(RegistroDecanoRequestDTO dto) {
        applyCurrentDbRole();
        usuarioRepository.registrarDecano(
                dto.getNombres(), dto.getApellidos(), dto.getCedula(),
                dto.getCorreo(), dto.getUsername(), dto.getPassword(), dto.getIdFacultad());
    }

    @Override @Transactional
    public void registrarCoordinador(RegistroCoordinadorRequestDTO dto) {
        applyCurrentDbRole();
        usuarioRepository.registrarCoordinador(
                dto.getNombres(), dto.getApellidos(), dto.getCedula(),
                dto.getCorreo(), dto.getUsername(), dto.getPassword(), dto.getIdCarrera());
    }

    @Override @Transactional
    public void registrarAdministrador(RegistroAdministradorRequest dto) {
        applyCurrentDbRole();
        usuarioRepository.registrarAdministrador(
                dto.getNombres(), dto.getApellidos(), dto.getCedula(),
                dto.getCorreo(), dto.getUsername(), dto.getPassword());
    }

    @Override @Transactional
    public void registrarAyudanteDirecto(RegistroAyudanteCatedraRequestDTO dto) {
        applyCurrentDbRole();
        usuarioRepository.registrarAyudanteDirecto(
                dto.getNombres(), dto.getApellidos(), dto.getCedula(),
                dto.getCorreo(), dto.getUsername(), dto.getPassword(), dto.getHorasAyudante());
    }

    @Override @Transactional
    public void promoverEstudiante(PromoverEstudianteAyudanteRequest dto) {
        applyCurrentDbRole();
        usuarioRepository.promoverEstudianteAAyudante(dto.getUsername(), dto.getHorasAsignadas());
    }

    @Override
    public List<UsuarioResponseDTO> listarTodos() {
        applyCurrentDbRole();
        return usuarioRepository.findAllWithRolesAndTipoRol().stream()
                .map(u -> UsuarioResponseDTO.builder()
                        .idUsuario(u.getIdUsuario())
                        .nombres(u.getNombres())
                        .apellidos(u.getApellidos())
                        .correo(u.getCorreo())
                        .nombreUsuario(u.getNombreUsuario())
                        .cedula(u.getCedula())
                        .activo(u.getActivo())
                        .token(null)
                        .roles(u.getRoles().stream()
                                .map(utr -> TipoRolResponseDTO.builder()
                                        .idTipoRol(utr.getTipoRol().getIdTipoRol())
                                        .nombreTipoRol(utr.getTipoRol().getNombreTipoRol())
                                        .activo(utr.getActivo())
                                        .build())
                                .toList())
                        .build())
                .collect(Collectors.toList());
    }

    @Override @Transactional
    public void cambiarEstadoGlobal(Integer idUsuario) {
        applyCurrentDbRole();
        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado"));
        boolean nuevoEstado = !usuario.getActivo();
        usuario.setActivo(nuevoEstado);
        if (usuario.getRoles() != null) usuario.getRoles().forEach(r -> r.setActivo(nuevoEstado));
        usuarioRepository.save(usuario);
    }

    @Override @Transactional
    public void cambiarEstadoRol(Integer idUsuario, Integer idTipoRol) {
        applyCurrentDbRole();
        UsuarioTipoRolId id = new UsuarioTipoRolId(idUsuario, idTipoRol);
        UsuarioTipoRol relacion = usuarioTipoRolRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Rol no asignado a este usuario"));
        if (!Boolean.TRUE.equals(relacion.getUsuario().getActivo())) {
            throw new IllegalStateException("No se puede cambiar roles con el usuario inactivo globalmente");
        }
        relacion.setActivo(!relacion.getActivo());
        usuarioTipoRolRepository.save(relacion);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return usuarioRepository.findByNombreUsuarioWithRolesAndTipoRol(username)
                .map(UsuarioPrincipal::new)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + username));
    }
}