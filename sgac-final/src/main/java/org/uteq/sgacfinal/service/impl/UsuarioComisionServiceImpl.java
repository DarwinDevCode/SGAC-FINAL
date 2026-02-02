package org.uteq.sgacfinal.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.uteq.sgacfinal.dto.Request.UsuarioComisionRequestDTO;
import org.uteq.sgacfinal.dto.Response.UsuarioComisionResponseDTO;
import org.uteq.sgacfinal.entity.UsuarioComision;
import org.uteq.sgacfinal.repository.UsuarioComisionRepository;
import org.uteq.sgacfinal.service.IUsuarioComisionService;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class UsuarioComisionServiceImpl implements IUsuarioComisionService {

    private final UsuarioComisionRepository usuarioComisionRepository;

    @Override
    public UsuarioComisionResponseDTO asignarEvaluador(UsuarioComisionRequestDTO request) {
        Integer idGenerado = usuarioComisionRepository.registrarUsuarioComision(
                request.getIdComisionSeleccion(),
                request.getIdUsuario(),
                request.getIdEvaluacionOposicion(),
                request.getRolIntegrante(),
                request.getPuntajeMaterial(),
                request.getPuntajeRespuestas(),
                request.getPuntajeExposicion(),
                request.getFechaEvaluacion()
        );

        if (idGenerado == -1) {
            throw new RuntimeException("Error al asignar usuario a la comisión.");
        }

        return buscarPorId(idGenerado);
    }

    @Override
    public UsuarioComisionResponseDTO actualizarPuntajes(Integer id, UsuarioComisionRequestDTO request) {
        Integer resultado = usuarioComisionRepository.actualizarPuntajes(
                id,
                request.getPuntajeMaterial(),
                request.getPuntajeRespuestas(),
                request.getPuntajeExposicion(),
                request.getFechaEvaluacion()
        );

        if (resultado == -1) {
            throw new RuntimeException("Error al actualizar puntajes.");
        }

        return buscarPorId(id);
    }

    @Override
    public void removerEvaluador(Integer id) {
        Integer resultado = usuarioComisionRepository.desactivarUsuarioComision(id);
        if (resultado == -1) {
            throw new RuntimeException("Error al remover evaluador de la comisión.");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public UsuarioComisionResponseDTO buscarPorId(Integer id) {
        UsuarioComision entidad = usuarioComisionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Registro no encontrado con ID: " + id));
        return mapearADTO(entidad);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UsuarioComisionResponseDTO> listarPorComision(Integer idComision) {
        return usuarioComisionRepository.listarEvaluadoresPorComisionSP(idComision).stream()
                .map(this::mapearADTO)
                .collect(Collectors.toList());
    }

    private UsuarioComisionResponseDTO mapearADTO(UsuarioComision entidad) {
        String nombreUsuario = entidad.getUsuario().getNombres() + " " + entidad.getUsuario().getApellidos();

        return UsuarioComisionResponseDTO.builder()
                .idUsuarioComision(entidad.getIdUsuarioComision())
                .idComisionSeleccion(entidad.getComisionSeleccion().getIdComisionSeleccion())
                .nombreComision(entidad.getComisionSeleccion().getNombreComision())
                .idUsuario(entidad.getUsuario().getIdUsuario())
                .nombreCompletoUsuario(nombreUsuario)
                .idEvaluacionOposicion(entidad.getEvaluacionOposicion() != null ? entidad.getEvaluacionOposicion().getIdEvaluacionOposicion() : null)
                .rolIntegrante(entidad.getRolIntegrante())
                .puntajeMaterial(entidad.getPuntajeMaterial())
                .puntajeRespuestas(entidad.getPuntajeRespuestas())
                .puntajeExposicion(entidad.getPuntajeExposicion())
                .fechaEvaluacion(entidad.getFechaEvaluacion())
                .build();
    }
}