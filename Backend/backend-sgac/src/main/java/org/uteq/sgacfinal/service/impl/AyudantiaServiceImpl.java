package org.uteq.sgacfinal.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.uteq.sgacfinal.dto.Request.AyudantiaRequestDTO;
import org.uteq.sgacfinal.dto.Response.AyudantiaResponseDTO;
import org.uteq.sgacfinal.entity.Ayudantia;
import org.uteq.sgacfinal.repository.AyudantiaRepository;
import org.uteq.sgacfinal.service.IAyudantiaService;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class AyudantiaServiceImpl implements IAyudantiaService {

    private final AyudantiaRepository ayudantiaRepository;

    @Override
    public AyudantiaResponseDTO crear(AyudantiaRequestDTO request) {
        Integer idGenerado = ayudantiaRepository.registrarAyudantia(
                request.getIdTipoEstadoEvidenciaAyudantia(),
                request.getIdPostulacion(),
                request.getFechaInicio(),
                request.getFechaFin(),
                request.getHorasCumplidas()
        );

        if (idGenerado == -1) {
            throw new RuntimeException("Error al crear la ayudantía.");
        }

        return buscarPorId(idGenerado);
    }

    @Override
    public AyudantiaResponseDTO actualizar(Integer id, AyudantiaRequestDTO request) {
        Integer resultado = ayudantiaRepository.actualizarAyudantia(
                id,
                request.getIdTipoEstadoEvidenciaAyudantia(),
                request.getFechaInicio(),
                request.getFechaFin(),
                request.getHorasCumplidas()
        );

        if (resultado == -1) {
            throw new RuntimeException("Error al actualizar la ayudantía.");
        }

        return buscarPorId(id);
    }

    @Override
    @Transactional(readOnly = true)
    public AyudantiaResponseDTO buscarPorId(Integer id) {
        Ayudantia ayudantia = ayudantiaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ayudantía no encontrada con ID: " + id));
        return mapearADTO(ayudantia);
    }

    @Override
    @Transactional(readOnly = true)
    public AyudantiaResponseDTO buscarPorPostulacion(Integer idPostulacion) {
        Ayudantia ayudantia = ayudantiaRepository.findById(idPostulacion)
                .orElseThrow(() -> new RuntimeException("No existe ayudantía para la postulación: " + idPostulacion));
        return mapearADTO(ayudantia);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AyudantiaResponseDTO> listarTodas() {
        return ayudantiaRepository.findAll().stream()
                .map(this::mapearADTO)
                .collect(Collectors.toList());
    }

    private AyudantiaResponseDTO mapearADTO(Ayudantia entidad) {
        String nombreEstudiante = "";
        if(entidad.getPostulacion() != null && entidad.getPostulacion().getEstudiante() != null) {
            var usuario = entidad.getPostulacion().getEstudiante().getUsuario();
            nombreEstudiante = usuario.getNombres() + " " + usuario.getApellidos();
        }

        return AyudantiaResponseDTO.builder()
                .idAyudantia(entidad.getIdAyudantia())
                .nombreEstadoEvidencia(entidad.getIdTipoEstadoAyudantia().getNombreEstado())
                .idPostulacion(entidad.getPostulacion().getIdPostulacion())
                .nombreEstudiante(nombreEstudiante)
                .fechaInicio(entidad.getFechaInicio())
                .fechaFin(entidad.getFechaFin())
                .horasCumplidas(entidad.getHorasCumplidas())
                .build();
    }
}