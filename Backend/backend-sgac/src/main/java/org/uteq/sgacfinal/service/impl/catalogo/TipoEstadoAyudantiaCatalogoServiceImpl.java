package org.uteq.sgacfinal.service.impl.catalogo;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.uteq.sgacfinal.dto.request.TipoEstadoAyudantiaRequestDTO;
import org.uteq.sgacfinal.dto.response.StandardResponseDTO;
import org.uteq.sgacfinal.dto.response.TipoEstadoAyudantiaResponseDTO;
import org.uteq.sgacfinal.repository.catalogo.TipoEstadoAyudantiaCatalogoRepository;
import org.uteq.sgacfinal.service.catalogo.ITipoEstadoAyudantiaCatalogoService;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TipoEstadoAyudantiaCatalogoServiceImpl implements ITipoEstadoAyudantiaCatalogoService {

    private final TipoEstadoAyudantiaCatalogoRepository repository;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional(readOnly = true)
    public StandardResponseDTO<List<TipoEstadoAyudantiaResponseDTO>> listar() {
        try {
            String jsonResult = repository.listar();
            return objectMapper.readValue(jsonResult,
                new TypeReference<StandardResponseDTO<List<TipoEstadoAyudantiaResponseDTO>>>() {});
        } catch (Exception e) {
            log.error("Error al listar estados de ayudantía: {}", e.getMessage());
            return StandardResponseDTO.<List<TipoEstadoAyudantiaResponseDTO>>builder()
                    .exito(false)
                    .mensaje("Error al listar estados de ayudantía: " + e.getMessage())
                    .build();
        }
    }

    @Override
    @Transactional
    public StandardResponseDTO<Integer> crear(TipoEstadoAyudantiaRequestDTO request) {
        try {
            String jsonResult = repository.crear(
                request.getNombreEstado(),
                request.getDescripcion(),
                request.getCodigo()
            );
            return objectMapper.readValue(jsonResult, new TypeReference<StandardResponseDTO<Integer>>() {});
        } catch (Exception e) {
            log.error("Error al crear estado de ayudantía: {}", e.getMessage());
            return StandardResponseDTO.<Integer>builder()
                    .exito(false)
                    .mensaje("Error al crear estado de ayudantía: " + e.getMessage())
                    .build();
        }
    }

    @Override
    @Transactional
    public StandardResponseDTO<Integer> actualizar(Integer id, TipoEstadoAyudantiaRequestDTO request) {
        try {
            String jsonResult = repository.actualizar(
                id,
                request.getNombreEstado(),
                request.getDescripcion(),
                request.getCodigo()
            );
            return objectMapper.readValue(jsonResult, new TypeReference<StandardResponseDTO<Integer>>() {});
        } catch (Exception e) {
            log.error("Error al actualizar estado de ayudantía: {}", e.getMessage());
            return StandardResponseDTO.<Integer>builder()
                    .exito(false)
                    .mensaje("Error al actualizar estado de ayudantía: " + e.getMessage())
                    .build();
        }
    }

    @Override
    @Transactional
    public StandardResponseDTO<Integer> desactivar(Integer id) {
        try {
            String jsonResult = repository.eliminar(id);
            return objectMapper.readValue(jsonResult, new TypeReference<StandardResponseDTO<Integer>>() {});
        } catch (Exception e) {
            log.error("Error al desactivar estado de ayudantía: {}", e.getMessage());
            return StandardResponseDTO.<Integer>builder()
                    .exito(false)
                    .mensaje("Error al desactivar estado de ayudantía: " + e.getMessage())
                    .build();
        }
    }
}

