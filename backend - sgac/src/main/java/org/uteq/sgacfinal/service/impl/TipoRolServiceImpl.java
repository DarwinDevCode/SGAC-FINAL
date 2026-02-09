package org.uteq.sgacfinal.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.uteq.sgacfinal.dto.Request.TipoRolRequestDTO;
import org.uteq.sgacfinal.dto.Response.TipoRolResponseDTO;
import org.uteq.sgacfinal.entity.TipoRol;
import org.uteq.sgacfinal.repository.TipoRolRepository;
import org.uteq.sgacfinal.service.ITipoRolService;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class TipoRolServiceImpl implements ITipoRolService {

    private final TipoRolRepository tipoRolRepository;

    @Override
    public TipoRolResponseDTO crear(TipoRolRequestDTO request) {
        TipoRol entidad = new TipoRol();
        entidad.setNombreTipoRol(request.getNombreTipoRol());
        entidad.setActivo(true);

        entidad = tipoRolRepository.save(entidad);
        return mapearADTO(entidad);
    }

    @Override
    public TipoRolResponseDTO actualizar(Integer id, TipoRolRequestDTO request) {
        TipoRol entidad = tipoRolRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Rol no encontrado con ID: " + id));

        entidad.setNombreTipoRol(request.getNombreTipoRol());
        if(request.getActivo() != null) entidad.setActivo(request.getActivo());

        entidad = tipoRolRepository.save(entidad);
        return mapearADTO(entidad);
    }

    @Override
    @Transactional(readOnly = true)
    public TipoRolResponseDTO buscarPorId(Integer id) {
        TipoRol entidad = tipoRolRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Rol no encontrado con ID: " + id));
        return mapearADTO(entidad);
    }

    @Override
    @Transactional(readOnly = true)
    public TipoRolResponseDTO buscarPorNombre(String nombre) {
        TipoRol entidad = tipoRolRepository.findByNombreTipoRol(nombre)
                .orElseThrow(() -> new RuntimeException("Rol no encontrado con nombre: " + nombre));
        return mapearADTO(entidad);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TipoRolResponseDTO> listarTodos() {
        return tipoRolRepository.findAll().stream()
                .map(this::mapearADTO)
                .collect(Collectors.toList());
    }

    private TipoRolResponseDTO mapearADTO(TipoRol entidad) {
        return TipoRolResponseDTO.builder()
                .idTipoRol(entidad.getIdTipoRol())
                .nombreTipoRol(entidad.getNombreTipoRol())
                .activo(entidad.getActivo())
                .build();
    }
}