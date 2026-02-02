package org.uteq.sgacfinal.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.uteq.sgacfinal.dto.Request.TipoEstadoRequisitoRequestDTO;
import org.uteq.sgacfinal.dto.Response.TipoEstadoRequisitoResponseDTO;
import org.uteq.sgacfinal.entity.TipoEstadoRequisito;
import org.uteq.sgacfinal.repository.TipoEstadoRequisitoRepository;
import org.uteq.sgacfinal.service.ITipoEstadoRequisitoService;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class TipoEstadoRequisitoServiceImpl implements ITipoEstadoRequisitoService {

    private final TipoEstadoRequisitoRepository repository;

    @Override
    public TipoEstadoRequisitoResponseDTO crear(TipoEstadoRequisitoRequestDTO request) {
        TipoEstadoRequisito entidad = new TipoEstadoRequisito();
        entidad.setNombreEstado(request.getNombreEstado());

        entidad = repository.save(entidad);
        return mapearADTO(entidad);
    }

    @Override
    public TipoEstadoRequisitoResponseDTO actualizar(Integer id, TipoEstadoRequisitoRequestDTO request) {
        TipoEstadoRequisito entidad = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Estado no encontrado"));

        entidad.setNombreEstado(request.getNombreEstado());

        entidad = repository.save(entidad);
        return mapearADTO(entidad);
    }

    @Override
    public void eliminar(Integer id) {
        repository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public TipoEstadoRequisitoResponseDTO buscarPorId(Integer id) {
        TipoEstadoRequisito entidad = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Estado no encontrado con ID: " + id));
        return mapearADTO(entidad);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TipoEstadoRequisitoResponseDTO> listarTodos() {
        return repository.findAll().stream()
                .map(this::mapearADTO)
                .collect(Collectors.toList());
    }

    private TipoEstadoRequisitoResponseDTO mapearADTO(TipoEstadoRequisito entidad) {
        return TipoEstadoRequisitoResponseDTO.builder()
                .idTipoEstadoRequisito(entidad.getIdTipoEstadoRequisito())
                .nombreEstado(entidad.getNombreEstado())
                .build();
    }
}