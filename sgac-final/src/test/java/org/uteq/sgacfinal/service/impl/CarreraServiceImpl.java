package org.uteq.sgacfinal.service.impl;

import org.uteq.sgacfinal.dto.CarreraDTO;
import org.uteq.sgacfinal.dto.CarreraRequest;
import org.uteq.sgacfinal.entity.Carrera;
import org.uteq.sgacfinal.entity.Facultad;
import org.uteq.sgacfinal.exception.ResourceNotFoundException;
import org.uteq.sgacfinal.repository.CarreraRepository;
import org.uteq.sgacfinal.repository.FacultadRepository;
import org.uteq.sgacfinal.service.CarreraService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class CarreraServiceImpl implements CarreraService {

    private final CarreraRepository carreraRepository;
    private final FacultadRepository facultadRepository;

    @Override
    @Transactional(readOnly = true)
    public List<CarreraDTO> findAll() {
        return carreraRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CarreraDTO> findByFacultad(Integer idFacultad) {
        return carreraRepository.findByFacultadIdFacultad(idFacultad).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public CarreraDTO findById(Integer id) {
        Carrera carrera = carreraRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Carrera", "id", id));
        return convertToDTO(carrera);
    }

    @Override
    public CarreraDTO create(CarreraRequest request) {
        Facultad facultad = facultadRepository.findById(request.getIdFacultad())
                .orElseThrow(() -> new ResourceNotFoundException("Facultad", "id", request.getIdFacultad()));
        
        Carrera carrera = Carrera.builder()
                .facultad(facultad)
                .nombreCarrera(request.getNombreCarrera())
                .build();
        return convertToDTO(carreraRepository.save(carrera));
    }

    @Override
    public CarreraDTO update(Integer id, CarreraRequest request) {
        Carrera carrera = carreraRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Carrera", "id", id));
        
        Facultad facultad = facultadRepository.findById(request.getIdFacultad())
                .orElseThrow(() -> new ResourceNotFoundException("Facultad", "id", request.getIdFacultad()));
        
        carrera.setFacultad(facultad);
        carrera.setNombreCarrera(request.getNombreCarrera());
        return convertToDTO(carreraRepository.save(carrera));
    }

    @Override
    public void delete(Integer id) {
        if (!carreraRepository.existsById(id)) {
            throw new ResourceNotFoundException("Carrera", "id", id);
        }
        carreraRepository.deleteById(id);
    }

    private CarreraDTO convertToDTO(Carrera carrera) {
        return CarreraDTO.builder()
                .idCarrera(carrera.getIdCarrera())
                .idFacultad(carrera.getFacultad().getIdFacultad())
                .nombreFacultad(carrera.getFacultad().getNombreFacultad())
                .nombreCarrera(carrera.getNombreCarrera())
                .build();
    }
}
