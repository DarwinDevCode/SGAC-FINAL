package org.uteq.sgacfinal.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.uteq.sgacfinal.dto.Request.TipoEstadoEvidenciaAyudantiaRequestDTO;
import org.uteq.sgacfinal.dto.Response.TipoEstadoEvidenciaAyudantiaResponseDTO;
import org.uteq.sgacfinal.entity.TipoEstadoEvidenciaAyudantia;
import org.uteq.sgacfinal.repository.ITipoEstadoEvidenciaAyudantiaRepository;
import org.uteq.sgacfinal.service.ITipoEstadoEvidenciaAyudantiaService;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class TipoEstadoEvidenciaAyudantiaServiceImpl implements ITipoEstadoEvidenciaAyudantiaService {
    private final ITipoEstadoEvidenciaAyudantiaRepository repository;

    @Override
    @Transactional(readOnly = true)
    public List<TipoEstadoEvidenciaAyudantiaResponseDTO> listarTodos() {
        return repository.findAllByOrderByNombreEstadoAsc().stream().map(this::mapToDTO).toList();
    }

    @Override
    @Transactional
    public TipoEstadoEvidenciaAyudantiaResponseDTO crear(TipoEstadoEvidenciaAyudantiaRequestDTO request) {
        Integer id = repository.crearTipoEstadoEvidenciaAyudantia(request.getNombreEstado(), request.getDescripcion());
        return repository.findById(id).map(this::mapToDTO).orElseThrow();
    }

    @Override
    @Transactional
    public TipoEstadoEvidenciaAyudantiaResponseDTO actualizar(Integer id, TipoEstadoEvidenciaAyudantiaRequestDTO request) {
        repository.actualizarTipoEstadoEvidenciaAyudantia(id, request.getNombreEstado(), request.getDescripcion());
        return repository.findById(id).map(this::mapToDTO).orElseThrow();
    }

    @Override
    @Transactional
    public void desactivar(Integer id) {
        repository.desactivarTipoEstadoEvidenciaAyudantia(id);
    }

    private TipoEstadoEvidenciaAyudantiaResponseDTO mapToDTO(TipoEstadoEvidenciaAyudantia entity) {
        return TipoEstadoEvidenciaAyudantiaResponseDTO.builder()
                .idTipoEstadoEvidenciaAyudantia(entity.getIdTipoEstadoEvidenciaAyudantia())
                .nombreEstado(entity.getNombreEstado())
                .descripcion(entity.getDescripcion())
                .activo(entity.getActivo())
                .build();
    }



//    private final ITipoEstadoEvidenciaAyudantiaRepository repository;
//
//    @Override
//    public TipoEstadoEvidenciaAyudantiaResponseDTO crear(TipoEstadoEvidenciaAyudantiaRequestDTO request) {
//        TipoEstadoEvidenciaAyudantia entidad = new TipoEstadoEvidenciaAyudantia();
//        entidad.setNombreEstado(request.getNombreEstado());
//
//        entidad = repository.save(entidad);
//        return mapearADTO(entidad);
//    }
//
//    @Override
//    public TipoEstadoEvidenciaAyudantiaResponseDTO actualizar(Integer id, TipoEstadoEvidenciaAyudantiaRequestDTO request) {
//        TipoEstadoEvidenciaAyudantia entidad = repository.findById(id)
//                .orElseThrow(() -> new RuntimeException("Estado no encontrado"));
//
//        entidad.setNombreEstado(request.getNombreEstado());
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
//    public TipoEstadoEvidenciaAyudantiaResponseDTO buscarPorId(Integer id) {
//        TipoEstadoEvidenciaAyudantia entidad = repository.findById(id)
//                .orElseThrow(() -> new RuntimeException("Estado no encontrado con ID: " + id));
//        return mapearADTO(entidad);
//    }
//
//    @Override
//    @Transactional(readOnly = true)
//    public List<TipoEstadoEvidenciaAyudantiaResponseDTO> listarTodos() {
//        return repository.findAll().stream()
//                .map(this::mapearADTO)
//                .collect(Collectors.toList());
//    }
//
//    private TipoEstadoEvidenciaAyudantiaResponseDTO mapearADTO(TipoEstadoEvidenciaAyudantia entidad) {
//        return TipoEstadoEvidenciaAyudantiaResponseDTO.builder()
//                .idTipoEstadoEvidenciaAyudantia(entidad.getIdTipoEstadoEvidenciaAyudantia())
//                .nombreEstado(entidad.getNombreEstado())
//                .build();
//    }
}