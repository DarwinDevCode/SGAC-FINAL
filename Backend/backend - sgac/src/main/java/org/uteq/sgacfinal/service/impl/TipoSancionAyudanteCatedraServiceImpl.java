package org.uteq.sgacfinal.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.uteq.sgacfinal.dto.Request.TipoSancionAyudanteCatedraRequestDTO;
import org.uteq.sgacfinal.dto.Response.TipoSancionAyudanteCatedraResponseDTO;
import org.uteq.sgacfinal.entity.TipoSancionAyudanteCatedra;
import org.uteq.sgacfinal.repository.TipoSancionAyudanteCatedraRepository;
import org.uteq.sgacfinal.service.ITipoSancionAyudanteCatedraService;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class TipoSancionAyudanteCatedraServiceImpl implements ITipoSancionAyudanteCatedraService {

    private final TipoSancionAyudanteCatedraRepository repository;

    @Override
    public TipoSancionAyudanteCatedraResponseDTO crear(TipoSancionAyudanteCatedraRequestDTO request) {
        TipoSancionAyudanteCatedra entidad = new TipoSancionAyudanteCatedra();
        entidad.setNombreTipoSancion(request.getNombreTipoSancion());
        entidad.setActivo(true);

        entidad = repository.save(entidad);
        return mapearADTO(entidad);
    }

    @Override
    public TipoSancionAyudanteCatedraResponseDTO actualizar(Integer id, TipoSancionAyudanteCatedraRequestDTO request) {
        TipoSancionAyudanteCatedra entidad = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tipo de sanción no encontrado."));

        entidad.setNombreTipoSancion(request.getNombreTipoSancion());
        if(request.getActivo() != null) entidad.setActivo(request.getActivo());

        entidad = repository.save(entidad);
        return mapearADTO(entidad);
    }

    @Override
    public void eliminar(Integer id) {
        repository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public TipoSancionAyudanteCatedraResponseDTO buscarPorId(Integer id) {
        TipoSancionAyudanteCatedra entidad = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tipo de sanción no encontrado con ID: " + id));
        return mapearADTO(entidad);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TipoSancionAyudanteCatedraResponseDTO> listarTodos() {
        return repository.findAll().stream()
                .map(this::mapearADTO)
                .collect(Collectors.toList());
    }

    private TipoSancionAyudanteCatedraResponseDTO mapearADTO(TipoSancionAyudanteCatedra entidad) {
        return TipoSancionAyudanteCatedraResponseDTO.builder()
                .idTipoSancionAyudanteCatedra(entidad.getIdTipoSancionAyudanteCatedra())
                .nombreTipoSancion(entidad.getNombreTipoSancion())
                .activo(entidad.getActivo())
                .build();
    }
}