package org.uteq.sgacfinal.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.uteq.sgacfinal.dto.Request.UsuarioRequestDTO;
import org.uteq.sgacfinal.dto.Response.UsuarioResponseDTO;
import org.uteq.sgacfinal.entity.Usuario;
import org.uteq.sgacfinal.repository.UsuarioRepository;
import org.uteq.sgacfinal.service.IUsuarioService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class UsuarioServiceImpl implements IUsuarioService {

    private final UsuarioRepository usuarioRepository;

    @Override
    public UsuarioResponseDTO crear(UsuarioRequestDTO request) {
        if (usuarioRepository.existsByCedula(request.getCedula())) {
            throw new RuntimeException("La cédula ya está registrada.");
        }

        Integer idGenerado = usuarioRepository.registrarUsuario(
                request.getNombres(),
                request.getApellidos(),
                request.getCedula(),
                request.getCorreo(),
                request.getNombreUsuario(),
                request.getContraseniaUsuario()
        );

        if (idGenerado == -1) {
            throw new RuntimeException("Error al registrar el usuario.");
        }

        return buscarPorId(idGenerado);
    }

    @Override
    public UsuarioResponseDTO actualizar(Integer id, UsuarioRequestDTO request) {
        Integer resultado = usuarioRepository.actualizarUsuario(
                id,
                request.getNombres(),
                request.getApellidos(),
                request.getCorreo()
        );

        if (resultado == -1) {
            throw new RuntimeException("Error al actualizar usuario.");
        }

        return buscarPorId(id);
    }

    @Override
    public void desactivar(Integer id) {
        Integer resultado = usuarioRepository.desactivarUsuario(id);
        if (resultado == -1) {
            throw new RuntimeException("Error al desactivar usuario.");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public UsuarioResponseDTO buscarPorId(Integer id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + id));
        return mapearADTO(usuario);
    }

    @Override
    @Transactional(readOnly = true)
    public UsuarioResponseDTO buscarPorNombreUsuario(String nombreUsuario) {
        Usuario usuario = usuarioRepository.findByNombreUsuario(nombreUsuario)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado: " + nombreUsuario));
        return mapearADTO(usuario);
    }

    @Override
    @Transactional(readOnly = true)
    public Object login(String usuario, String contrasenia) {
        List<Object[]> resultados = usuarioRepository.loginSgac(usuario, contrasenia);

        if (resultados.isEmpty()) {
            throw new RuntimeException("Credenciales incorrectas o usuario inactivo.");
        }

        Object[] row = resultados.get(0);
        Map<String, Object> loginResponse = new HashMap<>();
        loginResponse.put("idUsuario", row[0]);
        loginResponse.put("nombres", row[1]);
        loginResponse.put("apellidos", row[2]);
        loginResponse.put("correo", row[3]);
        loginResponse.put("nombreUsuario", row[4]);
        loginResponse.put("roles", row[5]);
        return loginResponse;
    }

    @Override
    @Transactional(readOnly = true)
    public List<UsuarioResponseDTO> listarTodos() {
        return usuarioRepository.findAll().stream()
                .map(this::mapearADTO)
                .collect(Collectors.toList());
    }

    private UsuarioResponseDTO mapearADTO(Usuario entidad) {
        return UsuarioResponseDTO.builder()
                .idUsuario(entidad.getIdUsuario())
                .nombres(entidad.getNombres())
                .apellidos(entidad.getApellidos())
                .cedula(entidad.getCedula())
                .correo(entidad.getCorreo())
                .nombreUsuario(entidad.getNombreUsuario())
                .fechaCreacion(entidad.getFechaCreacion())
                .activo(entidad.getActivo())
                .build();
    }
}