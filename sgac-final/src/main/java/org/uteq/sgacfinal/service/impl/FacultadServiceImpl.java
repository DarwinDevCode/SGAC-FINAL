package org.uteq.sgacfinal.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.uteq.sgacfinal.dto.Request.FacultadRequestDTO;
import org.uteq.sgacfinal.dto.Response.FacultadResponseDTO;
import org.uteq.sgacfinal.entity.Facultad;
import org.uteq.sgacfinal.repository.FacultadRepository;
import org.uteq.sgacfinal.service.IFacultadService;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class FacultadServiceImpl implements IFacultadService {

    private final FacultadRepository facultadRepository;

    @Override
    public FacultadResponseDTO crear(FacultadRequestDTO request) {
        Integer idGenerado = facultadRepository.registrarFacultad(request.getNombreFacultad());

        if (idGenerado == -1) {
            throw new RuntimeException("Error al registrar la facultad.");
        }

        return buscarPorId(idGenerado);
    }

    @Override
    public FacultadResponseDTO actualizar(Integer id, FacultadRequestDTO request) {
        Integer resultado = facultadRepository.actualizarFacultad(id, request.getNombreFacultad());

        if (resultado == -1) {
            throw new RuntimeException("Error al actualizar la facultad.");
        }

        return buscarPorId(id);
    }

    @Override
    public void desactivar(Integer id) {
        Integer resultado = facultadRepository.desactivarFacultad(id);
        if (resultado == -1) {
            throw new RuntimeException("Error al eliminar la facultad.");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public FacultadResponseDTO buscarPorId(Integer id) {
        Facultad facultad = facultadRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Facultad no encontrada con ID: " + id));
        return mapearADTO(facultad);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FacultadResponseDTO> listarTodas() {
        return facultadRepository.findAll().stream()
                .map(this::mapearADTO)
                .collect(Collectors.toList());
    }

    private FacultadResponseDTO mapearADTO(Facultad entidad) {
        return FacultadResponseDTO.builder()
                .idFacultad(entidad.getIdFacultad())
                .nombreFacultad(entidad.getNombreFacultad())
                .build();
    }
}