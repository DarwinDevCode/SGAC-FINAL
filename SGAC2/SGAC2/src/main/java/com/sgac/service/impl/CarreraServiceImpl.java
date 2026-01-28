package com.sgac.service.impl;

import com.sgac.dto.CarreraDTO;
import com.sgac.dto.CarreraRequest;
import com.sgac.entity.Carrera;
import com.sgac.entity.Facultad;
import com.sgac.exception.ResourceNotFoundException;
import com.sgac.repository.CarreraRepository;
import com.sgac.repository.FacultadRepository;
import com.sgac.service.CarreraService;
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
