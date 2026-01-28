package com.sgac.service.impl;

import com.sgac.dto.TipoRolDTO;
import com.sgac.dto.UsuarioDTO;
import com.sgac.dto.UsuarioRequest;
import com.sgac.entity.TipoRol;
import com.sgac.entity.Usuario;
import com.sgac.entity.UsuarioTipoRol;
import com.sgac.entity.UsuarioTipoRolId;
import com.sgac.exception.BadRequestException;
import com.sgac.exception.ResourceNotFoundException;
import com.sgac.repository.TipoRolRepository;
import com.sgac.repository.UsuarioRepository;
import com.sgac.repository.UsuarioTipoRolRepository;
import com.sgac.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class UsuarioServiceImpl implements UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final TipoRolRepository tipoRolRepository;
    private final UsuarioTipoRolRepository usuarioTipoRolRepository;

    @Override
    @Transactional(readOnly = true)
    public List<UsuarioDTO> findAll() {
        return usuarioRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<UsuarioDTO> findAllActive() {
        return usuarioRepository.findAllActiveWithRoles().stream()
                .map(this::convertToDTOWithRoles)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public UsuarioDTO findById(Integer id) {
        Usuario usuario = usuarioRepository.findByIdWithRoles(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "id", id));
        return convertToDTOWithRoles(usuario);
    }

    @Override
    @Transactional(readOnly = true)
    public UsuarioDTO findByNombreUsuario(String nombreUsuario) {
        Usuario usuario = usuarioRepository.findByNombreUsuario(nombreUsuario)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "nombreUsuario", nombreUsuario));
        return convertToDTO(usuario);
    }

    @Override
    public UsuarioDTO create(UsuarioRequest request) {
        validateUniqueFields(request, null);

        Usuario usuario = Usuario.builder()
                .nombres(request.getNombres())
                .apellidos(request.getApellidos())
                .cedula(request.getCedula())
                .correo(request.getCorreo())
                .nombreUsuario(request.getNombreUsuario())
                .contraseniaUsuario(request.getContraseniaUsuario())
                .fechaCreacion(LocalDate.now())
                .activo(request.getActivo())
                .build();

        Usuario saved = usuarioRepository.save(usuario);

        // Assign roles if provided
        if (request.getRolesIds() != null && !request.getRolesIds().isEmpty()) {
            for (Integer rolId : request.getRolesIds()) {
                assignRoleInternal(saved, rolId);
            }
        }

        return convertToDTOWithRoles(usuarioRepository.findByIdWithRoles(saved.getIdUsuario()).orElse(saved));
    }

    @Override
    public UsuarioDTO update(Integer id, UsuarioRequest request) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "id", id));

        validateUniqueFields(request, id);

        usuario.setNombres(request.getNombres());
        usuario.setApellidos(request.getApellidos());
        usuario.setCedula(request.getCedula());
        usuario.setCorreo(request.getCorreo());
        usuario.setNombreUsuario(request.getNombreUsuario());
        if (request.getContraseniaUsuario() != null && !request.getContraseniaUsuario().isEmpty()) {
            usuario.setContraseniaUsuario(request.getContraseniaUsuario());
        }
        usuario.setActivo(request.getActivo());

        Usuario updated = usuarioRepository.save(usuario);

        // Update roles if provided
        if (request.getRolesIds() != null) {
            usuarioTipoRolRepository.deleteByUsuarioIdUsuario(id);
            for (Integer rolId : request.getRolesIds()) {
                assignRoleInternal(updated, rolId);
            }
        }

        return convertToDTOWithRoles(usuarioRepository.findByIdWithRoles(updated.getIdUsuario()).orElse(updated));
    }

    @Override
    public void delete(Integer id) {
        if (!usuarioRepository.existsById(id)) {
            throw new ResourceNotFoundException("Usuario", "id", id);
        }
        usuarioRepository.deleteById(id);
    }

    @Override
    public void toggleActive(Integer id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "id", id));
        usuario.setActivo(!usuario.getActivo());
        usuarioRepository.save(usuario);
    }

    @Override
    public void assignRole(Integer usuarioId, Integer tipoRolId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "id", usuarioId));
        assignRoleInternal(usuario, tipoRolId);
    }

    @Override
    public void removeRole(Integer usuarioId, Integer tipoRolId) {
        UsuarioTipoRolId id = new UsuarioTipoRolId(usuarioId, tipoRolId);
        if (!usuarioTipoRolRepository.existsById(id)) {
            throw new ResourceNotFoundException("Relación Usuario-Rol no encontrada");
        }
        usuarioTipoRolRepository.deleteById(id);
    }

    private void assignRoleInternal(Usuario usuario, Integer tipoRolId) {
        TipoRol tipoRol = tipoRolRepository.findById(tipoRolId)
                .orElseThrow(() -> new ResourceNotFoundException("TipoRol", "id", tipoRolId));

        UsuarioTipoRolId id = new UsuarioTipoRolId(usuario.getIdUsuario(), tipoRolId);

        if (usuarioTipoRolRepository.existsById(id)) {
            return; // Role already assigned
        }

        UsuarioTipoRol usuarioTipoRol = UsuarioTipoRol.builder()
                .id(id)
                .usuario(usuario)
                .tipoRol(tipoRol)
                .activo(true)
                .build();

        usuarioTipoRolRepository.save(usuarioTipoRol);
    }

    private void validateUniqueFields(UsuarioRequest request, Integer excludeId) {
        usuarioRepository.findByNombreUsuario(request.getNombreUsuario())
                .ifPresent(existing -> {
                    if (excludeId == null || !existing.getIdUsuario().equals(excludeId)) {
                        throw new BadRequestException("Ya existe un usuario con el nombre de usuario: " + request.getNombreUsuario());
                    }
                });

        usuarioRepository.findByCedula(request.getCedula())
                .ifPresent(existing -> {
                    if (excludeId == null || !existing.getIdUsuario().equals(excludeId)) {
                        throw new BadRequestException("Ya existe un usuario con la cédula: " + request.getCedula());
                    }
                });

        usuarioRepository.findByCorreo(request.getCorreo())
                .ifPresent(existing -> {
                    if (excludeId == null || !existing.getIdUsuario().equals(excludeId)) {
                        throw new BadRequestException("Ya existe un usuario con el correo: " + request.getCorreo());
                    }
                });
    }

    private UsuarioDTO convertToDTO(Usuario usuario) {
        return UsuarioDTO.builder()
                .idUsuario(usuario.getIdUsuario())
                .nombres(usuario.getNombres())
                .apellidos(usuario.getApellidos())
                .cedula(usuario.getCedula())
                .correo(usuario.getCorreo())
                .nombreUsuario(usuario.getNombreUsuario())
                .fechaCreacion(usuario.getFechaCreacion())
                .activo(usuario.getActivo())
                .roles(new ArrayList<>())
                .build();
    }

    private UsuarioDTO convertToDTOWithRoles(Usuario usuario) {
        List<TipoRolDTO> roles = new ArrayList<>();
        if (usuario.getUsuarioTipoRoles() != null) {
            roles = usuario.getUsuarioTipoRoles().stream()
                    .filter(utr -> utr.getActivo() != null && utr.getActivo())
                    .map(utr -> TipoRolDTO.builder()
                            .idTipoRol(utr.getTipoRol().getIdTipoRol())
                            .nombreTipoRol(utr.getTipoRol().getNombreTipoRol())
                            .activo(utr.getTipoRol().getActivo())
                            .build())
                    .collect(Collectors.toList());
        }

        return UsuarioDTO.builder()
                .idUsuario(usuario.getIdUsuario())
                .nombres(usuario.getNombres())
                .apellidos(usuario.getApellidos())
                .cedula(usuario.getCedula())
                .correo(usuario.getCorreo())
                .nombreUsuario(usuario.getNombreUsuario())
                .fechaCreacion(usuario.getFechaCreacion())
                .activo(usuario.getActivo())
                .roles(roles)
                .build();
    }
}
