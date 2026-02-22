package org.uteq.sgacfinal.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.uteq.sgacfinal.dto.Request.SancionAyudanteCatedraRequestDTO;
import org.uteq.sgacfinal.dto.Response.SancionAyudanteCatedraResponseDTO;
import org.uteq.sgacfinal.entity.SancionAyudanteCatedra;
import org.uteq.sgacfinal.repository.ISancionAyudanteCatedraRepository;
import org.uteq.sgacfinal.service.ISancionAyudanteCatedraService;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class SancionAyudanteCatedraServiceImpl implements ISancionAyudanteCatedraService {

    private final ISancionAyudanteCatedraRepository sancionRepository;

    @Override
    public SancionAyudanteCatedraResponseDTO crear(SancionAyudanteCatedraRequestDTO request) {
        Integer idGenerado = sancionRepository.registrarSancion(
                request.getIdTipoSancionAyudanteCatedra(),
                request.getIdAyudanteCatedra(),
                request.getFechaSancion(),
                request.getMotivo()
        );

        if (idGenerado == -1) {
            throw new RuntimeException("Error al registrar la sanción.");
        }

        return buscarPorId(idGenerado);
    }

    @Override
    public SancionAyudanteCatedraResponseDTO actualizar(Integer id, SancionAyudanteCatedraRequestDTO request) {
        Integer resultado = sancionRepository.actualizarSancion(
                id,
                request.getMotivo(),
                request.getFechaSancion()
        );

        if (resultado == -1) {
            throw new RuntimeException("Error al actualizar la sanción.");
        }

        return buscarPorId(id);
    }

    @Override
    public void desactivar(Integer id) {
        Integer resultado = sancionRepository.desactivarSancion(id);
        if (resultado == -1) {
            throw new RuntimeException("Error al desactivar la sanción.");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public SancionAyudanteCatedraResponseDTO buscarPorId(Integer id) {
        SancionAyudanteCatedra sancion = sancionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Sanción no encontrada con ID: " + id));
        return mapearADTO(sancion);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SancionAyudanteCatedraResponseDTO> listarPorAyudante(Integer idAyudante) {
        return sancionRepository.listarSancionesPorAyudanteSP(idAyudante).stream()
                .map(this::mapearADTO)
                .collect(Collectors.toList());
    }

    private SancionAyudanteCatedraResponseDTO mapearADTO(SancionAyudanteCatedra entidad) {
        return SancionAyudanteCatedraResponseDTO.builder()
                .idSancionAyudanteCatedra(entidad.getIdSancionAyudanteCatedra())
                .idAyudanteCatedra(entidad.getAyudanteCatedra().getIdAyudanteCatedra())
                .nombreTipoSancion(entidad.getTipoSancionAyudanteCatedra().getNombreTipoSancion())
                .fechaSancion(entidad.getFechaSancion())
                .motivo(entidad.getMotivo())
                .activo(entidad.getActivo())
                .build();
    }
}