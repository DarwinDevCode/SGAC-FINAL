package org.uteq.sgacfinal.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.uteq.sgacfinal.dto.Request.DecanoRequestDTO;
import org.uteq.sgacfinal.dto.Response.DecanoResponseDTO;
import org.uteq.sgacfinal.entity.Decano;
import org.uteq.sgacfinal.repository.DecanoRepository;
import org.uteq.sgacfinal.service.IDecanoService;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class DecanoServiceImpl implements IDecanoService {

    private final DecanoRepository decanoRepository;

    @Override
    public DecanoResponseDTO crear(DecanoRequestDTO request) {
        Integer idGenerado = decanoRepository.registrarDecano(
                request.getIdUsuario(),
                request.getIdFacultad(),
                request.getFechaInicioGestion(),
                request.getFechaFinGestion()
        );

        if (idGenerado == -1) {
            throw new RuntimeException("Error al registrar decano.");
        }

        return buscarPorId(idGenerado);
    }

    @Override
    public DecanoResponseDTO actualizar(Integer id, DecanoRequestDTO request) {
        Integer resultado = decanoRepository.actualizarDecano(
                id,
                request.getIdFacultad(),
                request.getFechaInicioGestion(),
                request.getFechaFinGestion()
        );

        if (resultado == -1) {
            throw new RuntimeException("Error al actualizar decano.");
        }

        return buscarPorId(id);
    }

    @Override
    public void desactivar(Integer id) {
        Integer resultado = decanoRepository.desactivarDecano(id);
        if (resultado == -1) {
            throw new RuntimeException("Error al desactivar decano.");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public DecanoResponseDTO buscarPorId(Integer id) {
        Decano decano = decanoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Decano no encontrado con ID: " + id));
        return mapearADTO(decano);
    }

    @Override
    @Transactional(readOnly = true)
    public DecanoResponseDTO buscarPorUsuario(Integer idUsuario) {
        Decano decano = decanoRepository.findByUsuario_IdUsuarioAndActivoTrue(idUsuario)
                .orElseThrow(() -> new RuntimeException("No existe decano activo para el usuario ID: " + idUsuario));
        return mapearADTO(decano);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DecanoResponseDTO> listarActivos() {
        return decanoRepository.obtenerDecanosActivosSP().stream()
                .map(this::mapearADTO)
                .collect(Collectors.toList());
    }

    private DecanoResponseDTO mapearADTO(Decano entidad) {
        String nombreUsuario = entidad.getUsuario().getNombres() + " " + entidad.getUsuario().getApellidos();
        return DecanoResponseDTO.builder()
                .idDecano(entidad.getIdDecano())
                .idUsuario(entidad.getUsuario().getIdUsuario())
                .nombreCompletoUsuario(nombreUsuario)
                .idFacultad(entidad.getFacultad().getIdFacultad())
                .nombreFacultad(entidad.getFacultad().getNombreFacultad())
                .fechaInicioGestion(entidad.getFechaInicioGestion())
                .fechaFinGestion(entidad.getFechaFinGestion())
                .activo(entidad.getActivo())
                .build();
    }
}