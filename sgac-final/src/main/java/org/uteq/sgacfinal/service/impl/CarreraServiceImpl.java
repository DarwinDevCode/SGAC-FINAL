package org.uteq.sgacfinal.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.uteq.sgacfinal.dto.Request.CarreraRequestDTO;
import org.uteq.sgacfinal.dto.Response.CarreraResponseDTO;
import org.uteq.sgacfinal.entity.Carrera;
import org.uteq.sgacfinal.repository.CarreraRepository;
import org.uteq.sgacfinal.service.ICarreraService;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class CarreraServiceImpl implements ICarreraService {

    private final CarreraRepository carreraRepository;

    @Override
    public CarreraResponseDTO crear(CarreraRequestDTO request) {
        Integer idGenerado = carreraRepository.registrarCarrera(
                request.getIdFacultad(),
                request.getNombreCarrera()
        );

        if (idGenerado == -1) {
            throw new RuntimeException("Error al crear la carrera. Verifique la facultad.");
        }

        return buscarPorId(idGenerado);
    }

    @Override
    public CarreraResponseDTO actualizar(Integer id, CarreraRequestDTO request) {
        Integer resultado = carreraRepository.actualizarCarrera(
                id,
                request.getIdFacultad(),
                request.getNombreCarrera()
        );

        if (resultado == -1) {
            throw new RuntimeException("Error al actualizar la carrera.");
        }

        return buscarPorId(id);
    }

    @Override
    public void desactivar(Integer id) {
        Integer resultado = carreraRepository.desactivarCarrera(id);
        if (resultado == -1) {
            throw new RuntimeException("Error al desactivar la carrera.");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public CarreraResponseDTO buscarPorId(Integer id) {
        Carrera carrera = carreraRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Carrera no encontrada con ID: " + id));
        return mapearADTO(carrera);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CarreraResponseDTO> listarTodas() {
        return carreraRepository.findAll().stream()
                .map(this::mapearADTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CarreraResponseDTO> listarPorFacultad(Integer idFacultad) {
        return carreraRepository.findByFacultad_IdFacultad(idFacultad).stream()
                .map(this::mapearADTO)
                .collect(Collectors.toList());
    }

    private CarreraResponseDTO mapearADTO(Carrera entidad) {
        return CarreraResponseDTO.builder()
                .idCarrera(entidad.getIdCarrera())
                .idFacultad(entidad.getFacultad().getIdFacultad())
                .nombreFacultad(entidad.getFacultad().getNombreFacultad())
                .nombreCarrera(entidad.getNombreCarrera())
                .build();
    }
}