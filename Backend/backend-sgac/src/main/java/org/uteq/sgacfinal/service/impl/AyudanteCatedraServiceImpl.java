package org.uteq.sgacfinal.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.uteq.sgacfinal.dto.Request.AyudanteCatedraRequestDTO;
import org.uteq.sgacfinal.dto.Response.AyudanteCatedraResponseDTO;
import org.uteq.sgacfinal.entity.AyudanteCatedra;
import org.uteq.sgacfinal.repository.AyudanteCatedraRepository;
import org.uteq.sgacfinal.service.IAyudanteCatedraService;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class AyudanteCatedraServiceImpl implements IAyudanteCatedraService {

    private final AyudanteCatedraRepository ayudanteRepository;

    @Override
    public AyudanteCatedraResponseDTO crear(AyudanteCatedraRequestDTO request) {
        Integer idGenerado = ayudanteRepository.registrarAyudante(
                request.getIdUsuario(),
                request.getHorasAyudante()
        );

        if (idGenerado == -1) {
            throw new RuntimeException("Error al registrar el ayudante de cátedra.");
        }

        return buscarPorId(idGenerado);
    }

    @Override
    public AyudanteCatedraResponseDTO actualizar(Integer id, AyudanteCatedraRequestDTO request) {
        Integer resultado = ayudanteRepository.actualizarAyudante(
                id,
                request.getIdUsuario(),
                request.getHorasAyudante()
        );

        if (resultado == -1) {
            throw new RuntimeException("Error al actualizar el ayudante de cátedra.");
        }

        return buscarPorId(id);
    }

    @Override
    @Transactional(readOnly = true)
    public AyudanteCatedraResponseDTO buscarPorId(Integer id) {
        AyudanteCatedra ayudante = ayudanteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ayudante no encontrado con ID: " + id));
        return mapearADTO(ayudante);
    }

    @Override
    @Transactional(readOnly = true)
    public AyudanteCatedraResponseDTO buscarPorUsuario(Integer idUsuario) {
        AyudanteCatedra ayudante = ayudanteRepository.findByUsuario_IdUsuario(idUsuario)
                .orElseThrow(() -> new RuntimeException("No existe ayudante asociado al usuario ID: " + idUsuario));
        return mapearADTO(ayudante);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AyudanteCatedraResponseDTO> listarTodos() {
        return ayudanteRepository.findAll().stream()
                .map(this::mapearADTO)
                .collect(Collectors.toList());
    }

    private AyudanteCatedraResponseDTO mapearADTO(AyudanteCatedra entidad) {
        return AyudanteCatedraResponseDTO.builder()
                .idAyudanteCatedra(entidad.getIdAyudanteCatedra())
                .idUsuario(entidad.getUsuario().getIdUsuario())
                .nombreCompletoUsuario(entidad.getUsuario().getNombres() + " " + entidad.getUsuario().getApellidos())
                .cedulaUsuario(entidad.getUsuario().getCedula())
                .horasAyudante(entidad.getHorasAyudante())
                .build();
    }
}