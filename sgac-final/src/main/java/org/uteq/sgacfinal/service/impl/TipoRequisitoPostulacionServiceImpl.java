package org.uteq.sgacfinal.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.uteq.sgacfinal.dto.Request.TipoRequisitoPostulacionRequestDTO;
import org.uteq.sgacfinal.dto.Response.TipoRequisitoPostulacionResponseDTO;
import org.uteq.sgacfinal.entity.TipoRequisitoPostulacion;
import org.uteq.sgacfinal.repository.TipoRequisitoPostulacionRepository;
import org.uteq.sgacfinal.service.ITipoRequisitoPostulacionService;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class TipoRequisitoPostulacionServiceImpl implements ITipoRequisitoPostulacionService {

    private final TipoRequisitoPostulacionRepository repository;

    @Override
    public TipoRequisitoPostulacionResponseDTO crear(TipoRequisitoPostulacionRequestDTO request) {
        TipoRequisitoPostulacion entidad = new TipoRequisitoPostulacion();
        entidad.setNombreRequisito(request.getNombreRequisito());
        entidad.setDescripcion(request.getDescripcion());
        entidad.setActivo(true);

        entidad = repository.save(entidad);
        return mapearADTO(entidad);
    }

    @Override
    public TipoRequisitoPostulacionResponseDTO actualizar(Integer id, TipoRequisitoPostulacionRequestDTO request) {
        TipoRequisitoPostulacion entidad = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Requisito no encontrado con ID: " + id));

        entidad.setNombreRequisito(request.getNombreRequisito());
        entidad.setDescripcion(request.getDescripcion());
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
    public TipoRequisitoPostulacionResponseDTO buscarPorId(Integer id) {
        TipoRequisitoPostulacion entidad = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Requisito no encontrado con ID: " + id));
        return mapearADTO(entidad);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TipoRequisitoPostulacionResponseDTO> listarTodos() {
        return repository.findAll().stream()
                .map(this::mapearADTO)
                .collect(Collectors.toList());
    }

    private TipoRequisitoPostulacionResponseDTO mapearADTO(TipoRequisitoPostulacion entidad) {
        return TipoRequisitoPostulacionResponseDTO.builder()
                .idTipoRequisitoPostulacion(entidad.getIdTipoRequisitoPostulacion())
                .nombreRequisito(entidad.getNombreRequisito())
                .descripcion(entidad.getDescripcion())
                .activo(entidad.getActivo())
                .build();
    }
}