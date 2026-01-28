package org.uteq.sgacfinal.service.impl;

import org.uteq.sgacfinal.dto.TipoRolDTO;
import org.uteq.sgacfinal.dto.TipoRolRequest;
import org.uteq.sgacfinal.entity.TipoRol;
import org.uteq.sgacfinal.exception.BadRequestException;
import org.uteq.sgacfinal.exception.ResourceNotFoundException;
import org.uteq.sgacfinal.repository.TipoRolRepository;
import org.uteq.sgacfinal.service.TipoRolService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class TipoRolServiceImpl implements TipoRolService {

    private final TipoRolRepository tipoRolRepository;

    @Override
    @Transactional(readOnly = true)
    public List<TipoRolDTO> findAll() {
        return tipoRolRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TipoRolDTO> findAllActive() {
        return tipoRolRepository.findByActivo(true).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public TipoRolDTO findById(Integer id) {
        TipoRol tipoRol = tipoRolRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("TipoRol", "id", id));
        return convertToDTO(tipoRol);
    }

    @Override
    public TipoRolDTO create(TipoRolRequest request) {
        if (tipoRolRepository.existsByNombreTipoRol(request.getNombreTipoRol())) {
            throw new BadRequestException("Ya existe un rol con el nombre: " + request.getNombreTipoRol());
        }

        TipoRol tipoRol = TipoRol.builder()
                .nombreTipoRol(request.getNombreTipoRol())
                .activo(request.getActivo())
                .build();

        TipoRol saved = tipoRolRepository.save(tipoRol);
        return convertToDTO(saved);
    }

    @Override
    public TipoRolDTO update(Integer id, TipoRolRequest request) {
        TipoRol tipoRol = tipoRolRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("TipoRol", "id", id));

        // Check if name already exists for another role
        tipoRolRepository.findByNombreTipoRol(request.getNombreTipoRol())
                .ifPresent(existing -> {
                    if (!existing.getIdTipoRol().equals(id)) {
                        throw new BadRequestException("Ya existe un rol con el nombre: " + request.getNombreTipoRol());
                    }
                });

        tipoRol.setNombreTipoRol(request.getNombreTipoRol());
        tipoRol.setActivo(request.getActivo());

        TipoRol updated = tipoRolRepository.save(tipoRol);
        return convertToDTO(updated);
    }

    @Override
    public void delete(Integer id) {
        if (!tipoRolRepository.existsById(id)) {
            throw new ResourceNotFoundException("TipoRol", "id", id);
        }
        tipoRolRepository.deleteById(id);
    }

    @Override
    public void toggleActive(Integer id) {
        TipoRol tipoRol = tipoRolRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("TipoRol", "id", id));
        tipoRol.setActivo(!tipoRol.getActivo());
        tipoRolRepository.save(tipoRol);
    }

    private TipoRolDTO convertToDTO(TipoRol tipoRol) {
        return TipoRolDTO.builder()
                .idTipoRol(tipoRol.getIdTipoRol())
                .nombreTipoRol(tipoRol.getNombreTipoRol())
                .activo(tipoRol.getActivo())
                .build();
    }
}
