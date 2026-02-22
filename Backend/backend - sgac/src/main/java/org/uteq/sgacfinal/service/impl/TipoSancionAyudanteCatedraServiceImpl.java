package org.uteq.sgacfinal.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.uteq.sgacfinal.dto.Request.TipoSancionAyudanteCatedraRequestDTO;
import org.uteq.sgacfinal.dto.Response.TipoSancionAyudanteCatedraResponseDTO;
import org.uteq.sgacfinal.entity.TipoSancionAyudanteCatedra;
import org.uteq.sgacfinal.repository.ITipoSancionAyudanteCatedraRepository;
import org.uteq.sgacfinal.service.ITipoSancionAyudanteCatedraService;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class TipoSancionAyudanteCatedraServiceImpl implements ITipoSancionAyudanteCatedraService {
    private final ITipoSancionAyudanteCatedraRepository repository;

    @Override
    @Transactional(readOnly = true)
    public List<TipoSancionAyudanteCatedraResponseDTO> listarTodos() {
        return repository.findAll().stream().map(this::mapToDTO).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<TipoSancionAyudanteCatedraResponseDTO> listarActivos() {
        return repository.findByActivoTrue().stream().map(this::mapToDTO).toList();
    }

    @Override
    @Transactional
    public TipoSancionAyudanteCatedraResponseDTO crear(TipoSancionAyudanteCatedraRequestDTO request) {
        Integer id = repository.crearTipoSancionAyudanteCatedra(request.getNombreTipoSancion());
        return repository.findById(id).map(this::mapToDTO).orElseThrow();
    }

    @Override
    @Transactional
    public TipoSancionAyudanteCatedraResponseDTO actualizar(Integer id, TipoSancionAyudanteCatedraRequestDTO request) {
        repository.actualizarTipoSancionAyudanteCatedra(id, request.getNombreTipoSancion());
        return repository.findById(id).map(this::mapToDTO).orElseThrow();
    }

    @Override
    @Transactional
    public void desactivar(Integer id) {
        repository.desactivarTipoSancionAyudanteCatedra(id);
    }

    private TipoSancionAyudanteCatedraResponseDTO mapToDTO(TipoSancionAyudanteCatedra entity) {
        return TipoSancionAyudanteCatedraResponseDTO.builder()
                .idTipoSancionAyudanteCatedra(entity.getIdTipoSancionAyudanteCatedra())
                .nombreTipoSancion(entity.getNombreTipoSancion())
                .activo(entity.getActivo())
                .build();
    }


//    private final ITipoSancionAyudanteCatedraRepository repository;
//
//    @Override
//    public TipoSancionAyudanteCatedraResponseDTO crear(TipoSancionAyudanteCatedraRequestDTO request) {
//        TipoSancionAyudanteCatedra entidad = new TipoSancionAyudanteCatedra();
//        entidad.setNombreTipoSancion(request.getNombreTipoSancion());
//        entidad.setActivo(true);
//
//        entidad = repository.save(entidad);
//        return mapearADTO(entidad);
//    }
//
//    @Override
//    public TipoSancionAyudanteCatedraResponseDTO actualizar(Integer id, TipoSancionAyudanteCatedraRequestDTO request) {
//        TipoSancionAyudanteCatedra entidad = repository.findById(id)
//                .orElseThrow(() -> new RuntimeException("Tipo de sanción no encontrado."));
//
//        entidad.setNombreTipoSancion(request.getNombreTipoSancion());
//        if(request.getActivo() != null) entidad.setActivo(request.getActivo());
//
//        entidad = repository.save(entidad);
//        return mapearADTO(entidad);
//    }
//
//    @Override
//    public void eliminar(Integer id) {
//        repository.deleteById(id);
//    }
//
//    @Override
//    @Transactional(readOnly = true)
//    public TipoSancionAyudanteCatedraResponseDTO buscarPorId(Integer id) {
//        TipoSancionAyudanteCatedra entidad = repository.findById(id)
//                .orElseThrow(() -> new RuntimeException("Tipo de sanción no encontrado con ID: " + id));
//        return mapearADTO(entidad);
//    }
//
//    @Override
//    @Transactional(readOnly = true)
//    public List<TipoSancionAyudanteCatedraResponseDTO> listarTodos() {
//        return repository.findAll().stream()
//                .map(this::mapearADTO)
//                .collect(Collectors.toList());
//    }
//
//    private TipoSancionAyudanteCatedraResponseDTO mapearADTO(TipoSancionAyudanteCatedra entidad) {
//        return TipoSancionAyudanteCatedraResponseDTO.builder()
//                .idTipoSancionAyudanteCatedra(entidad.getIdTipoSancionAyudanteCatedra())
//                .nombreTipoSancion(entidad.getNombreTipoSancion())
//                .activo(entidad.getActivo())
//                .build();
//    }
}