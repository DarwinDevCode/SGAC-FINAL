package org.uteq.sgacfinal.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.uteq.sgacfinal.dto.Request.UsuarioTipoRolRequestDTO;
import org.uteq.sgacfinal.dto.Response.UsuarioTipoRolResponseDTO;
import org.uteq.sgacfinal.entity.Usuario;
import org.uteq.sgacfinal.repository.IUsuariosRepository;
import org.uteq.sgacfinal.repository.ITipoRolRepository;
import org.uteq.sgacfinal.repository.IUsuarioTipoRolRepository;
import org.uteq.sgacfinal.service.IUsuarioTipoRolService;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class UsuarioTipoRolServiceImpl implements IUsuarioTipoRolService {

    private final IUsuarioTipoRolRepository IUsuarioTipoRolRepository;
    private final IUsuariosRepository usuarioRepository;
    private final ITipoRolRepository ITipoRolRepository;

    @Override
    public UsuarioTipoRolResponseDTO asignarRol(UsuarioTipoRolRequestDTO request) {
        Integer idUsuario = IUsuarioTipoRolRepository.asignarRolUsuario(
                request.getIdUsuario(),
                request.getIdTipoRol()
        );

        if (idUsuario == -1) {
            throw new RuntimeException("Error al asignar rol al usuario.");
        }

        return construirDTO(request.getIdUsuario(), request.getIdTipoRol(), true);
    }

    @Override
    public UsuarioTipoRolResponseDTO cambiarEstado(Integer idUsuario, Integer idRol, Boolean activo) {
        Integer resultado = IUsuarioTipoRolRepository.actualizarEstadoRol(idUsuario, idRol, activo);
        if (resultado == -1) {
            throw new RuntimeException("Error al cambiar estado del rol.");
        }
        return construirDTO(idUsuario, idRol, activo);
    }

    @Override
    public void revocarRol(Integer idUsuario, Integer idRol) {
        Integer resultado = IUsuarioTipoRolRepository.desactivarRolUsuario(idUsuario, idRol);
        if (resultado == -1) {
            throw new RuntimeException("Error al revocar rol.");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<UsuarioTipoRolResponseDTO> listarRolesPorUsuario(Integer idUsuario) {
        List<Object[]> resultados = IUsuarioTipoRolRepository.obtenerRolesPorUsuarioSP(idUsuario);
        List<UsuarioTipoRolResponseDTO> listaDTO = new ArrayList<>();

        String nombreUsuario = usuarioRepository.findById(idUsuario)
                .map(Usuario::getNombreUsuario)
                .orElse("Desconocido");

        for (Object[] fila : resultados) {
            Integer idRol = (Integer) fila[0];
            Boolean activo = (Boolean) fila[1];

            String nombreRol = ITipoRolRepository.findById(idRol)
                    .map(r -> r.getNombreTipoRol())
                    .orElse("Rol ID " + idRol);

            listaDTO.add(UsuarioTipoRolResponseDTO.builder()
                    .idUsuario(idUsuario)
                    .nombreUsuario(nombreUsuario)
                    .idTipoRol(idRol)
                    .nombreRol(nombreRol)
                    .activo(activo)
                    .build());
        }
        return listaDTO;
    }

    private UsuarioTipoRolResponseDTO construirDTO(Integer idUsuario, Integer idRol, Boolean activo) {
        String nombreUsuario = usuarioRepository.findById(idUsuario)
                .map(Usuario::getNombreUsuario)
                .orElse("N/A");

        String nombreRol = ITipoRolRepository.findById(idRol)
                .map(r -> r.getNombreTipoRol())
                .orElse("N/A");

        return UsuarioTipoRolResponseDTO.builder()
                .idUsuario(idUsuario)
                .nombreUsuario(nombreUsuario)
                .idTipoRol(idRol)
                .nombreRol(nombreRol)
                .activo(activo)
                .build();
    }
}