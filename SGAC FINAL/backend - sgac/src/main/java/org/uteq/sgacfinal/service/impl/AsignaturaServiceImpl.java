package org.uteq.sgacfinal.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.uteq.sgacfinal.dto.Request.AsignaturaRequestDTO;
import org.uteq.sgacfinal.dto.Response.AsignaturaResponseDTO;
import org.uteq.sgacfinal.entity.Asignatura;
import org.uteq.sgacfinal.repository.IAsignaturaRepository;
import org.uteq.sgacfinal.repository.CarreraRepository;
import org.uteq.sgacfinal.service.IAsignaturaService;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class AsignaturaServiceImpl implements IAsignaturaService {

    private final IAsignaturaRepository asignaturaRepository;
    private final CarreraRepository carreraRepository;

    @Override
    public AsignaturaResponseDTO crear(AsignaturaRequestDTO request) {
        Integer idGenerado = asignaturaRepository.registrarAsignatura(
                request.getIdCarrera(),
                request.getNombreAsignatura(),
                request.getSemestre()
        );

        if (idGenerado == -1) {
            throw new RuntimeException("Error al crear la asignatura. Verifique que la carrera exista.");
        }

        return buscarPorId(idGenerado);
    }

    @Override
    public AsignaturaResponseDTO actualizar(Integer id, AsignaturaRequestDTO request) {
        Integer resultado = asignaturaRepository.actualizarAsignatura(
                id,
                request.getIdCarrera(),
                request.getNombreAsignatura(),
                request.getSemestre()
        );

        if (resultado == -1) {
            throw new RuntimeException("Error al actualizar la asignatura.");
        }

        return buscarPorId(id);
    }

    @Override
    public void desactivar(Integer id) {
        Asignatura asignatura = asignaturaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Asignatura no encontrada con ID: " + id));

        if (Boolean.TRUE.equals(asignatura.getActivo())) {
            Integer resultado = asignaturaRepository.desactivarAsignatura(id);
            if (resultado == -1) {
                throw new RuntimeException("Error al desactivar la asignatura.");
            }
            return;
        }

        int actualizados = asignaturaRepository.activarAsignatura(id);
        if (actualizados == 0) {
            throw new RuntimeException("Error al activar la asignatura.");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public AsignaturaResponseDTO buscarPorId(Integer id) {
        Asignatura asignatura = asignaturaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Asignatura no encontrada con ID: " + id));
        return mapearADTO(asignatura);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AsignaturaResponseDTO> listarTodas() {
        return asignaturaRepository.findAll().stream()
                .map(this::mapearADTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AsignaturaResponseDTO> listarPorCarrera(Integer idCarrera) {
        return asignaturaRepository.findByCarrera_IdCarrera(idCarrera).stream()
                .map(this::mapearADTO)
                .collect(Collectors.toList());
    }

    private AsignaturaResponseDTO mapearADTO(Asignatura entidad) {
        return AsignaturaResponseDTO.builder()
                .idAsignatura(entidad.getIdAsignatura())
                .idCarrera(entidad.getCarrera().getIdCarrera())
                .nombreCarrera(entidad.getCarrera().getNombreCarrera())
                .nombreAsignatura(entidad.getNombreAsignatura())
                .semestre(entidad.getSemestre())
                .activo(entidad.getActivo())
                .build();
    }
}