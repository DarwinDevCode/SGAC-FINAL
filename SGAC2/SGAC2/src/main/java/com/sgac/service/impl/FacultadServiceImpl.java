package com.sgac.service.impl;

import com.sgac.dto.FacultadDTO;
import com.sgac.dto.FacultadRequest;
import com.sgac.entity.Facultad;
import com.sgac.exception.ResourceNotFoundException;
import com.sgac.repository.FacultadRepository;
import com.sgac.service.FacultadService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class FacultadServiceImpl implements FacultadService {

    private final FacultadRepository facultadRepository;

    @Override
    @Transactional(readOnly = true)
    public List<FacultadDTO> findAll() {
        return facultadRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public FacultadDTO findById(Integer id) {
        Facultad facultad = facultadRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Facultad", "id", id));
        return convertToDTO(facultad);
    }

    @Override
    public FacultadDTO create(FacultadRequest request) {
        Facultad facultad = Facultad.builder()
                .nombreFacultad(request.getNombreFacultad())
                .build();
        return convertToDTO(facultadRepository.save(facultad));
    }

    @Override
    public FacultadDTO update(Integer id, FacultadRequest request) {
        Facultad facultad = facultadRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Facultad", "id", id));
        facultad.setNombreFacultad(request.getNombreFacultad());
        return convertToDTO(facultadRepository.save(facultad));
    }

    @Override
    public void delete(Integer id) {
        if (!facultadRepository.existsById(id)) {
            throw new ResourceNotFoundException("Facultad", "id", id);
        }
        facultadRepository.deleteById(id);
    }

    private FacultadDTO convertToDTO(Facultad facultad) {
        return FacultadDTO.builder()
                .idFacultad(facultad.getIdFacultad())
                .nombreFacultad(facultad.getNombreFacultad())
                .build();
    }
}
