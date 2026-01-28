package org.uteq.sgacfinal.service.impl;

import org.uteq.sgacfinal.dto.AsignaturaDTO;
import org.uteq.sgacfinal.dto.AsignaturaRequest;
import org.uteq.sgacfinal.entity.Asignatura;
import org.uteq.sgacfinal.entity.Carrera;
import org.uteq.sgacfinal.exception.ResourceNotFoundException;
import org.uteq.sgacfinal.repository.AsignaturaRepository;
import org.uteq.sgacfinal.repository.CarreraRepository;
import org.uteq.sgacfinal.service.AsignaturaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class AsignaturaServiceImpl implements AsignaturaService {

    private final AsignaturaRepository asignaturaRepository;
    private final CarreraRepository carreraRepository;

    @Override
    @Transactional(readOnly = true)
    public List<AsignaturaDTO> findAll() {
        return asignaturaRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AsignaturaDTO> findByCarrera(Integer idCarrera) {
        return asignaturaRepository.findByCarreraIdCarrera(idCarrera).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public AsignaturaDTO findById(Integer id) {
        Asignatura asignatura = asignaturaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Asignatura", "id", id));
        return convertToDTO(asignatura);
    }

    @Override
    public AsignaturaDTO create(AsignaturaRequest request) {
        Carrera carrera = carreraRepository.findById(request.getIdCarrera())
                .orElseThrow(() -> new ResourceNotFoundException("Carrera", "id", request.getIdCarrera()));
        
        Asignatura asignatura = Asignatura.builder()
                .carrera(carrera)
                .nombreAsignatura(request.getNombreAsignatura())
                .semestre(request.getSemestre())
                .build();
        return convertToDTO(asignaturaRepository.save(asignatura));
    }

    @Override
    public AsignaturaDTO update(Integer id, AsignaturaRequest request) {
        Asignatura asignatura = asignaturaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Asignatura", "id", id));
        
        Carrera carrera = carreraRepository.findById(request.getIdCarrera())
                .orElseThrow(() -> new ResourceNotFoundException("Carrera", "id", request.getIdCarrera()));
        
        asignatura.setCarrera(carrera);
        asignatura.setNombreAsignatura(request.getNombreAsignatura());
        asignatura.setSemestre(request.getSemestre());
        return convertToDTO(asignaturaRepository.save(asignatura));
    }

    @Override
    public void delete(Integer id) {
        if (!asignaturaRepository.existsById(id)) {
            throw new ResourceNotFoundException("Asignatura", "id", id);
        }
        asignaturaRepository.deleteById(id);
    }

    private AsignaturaDTO convertToDTO(Asignatura asignatura) {
        return AsignaturaDTO.builder()
                .idAsignatura(asignatura.getIdAsignatura())
                .idCarrera(asignatura.getCarrera().getIdCarrera())
                .nombreCarrera(asignatura.getCarrera().getNombreCarrera())
                .nombreAsignatura(asignatura.getNombreAsignatura())
                .semestre(asignatura.getSemestre())
                .build();
    }
}
