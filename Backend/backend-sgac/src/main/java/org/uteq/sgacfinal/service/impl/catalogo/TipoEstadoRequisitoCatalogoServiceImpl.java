package org.uteq.sgacfinal.service.impl.catalogo;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.uteq.sgacfinal.dto.Request.TipoEstadoRequisitoRequestDTO;
import org.uteq.sgacfinal.dto.Response.StandardResponseDTO;
import org.uteq.sgacfinal.dto.Response.TipoEstadoRequisitoResponseDTO;
import org.uteq.sgacfinal.repository.catalogo.TipoEstadoRequisitoCatalogoRepository;
import org.uteq.sgacfinal.service.catalogo.ITipoEstadoRequisitoCatalogoService;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TipoEstadoRequisitoCatalogoServiceImpl implements ITipoEstadoRequisitoCatalogoService {

    private final TipoEstadoRequisitoCatalogoRepository repository;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional(readOnly = true)
    public StandardResponseDTO<List<TipoEstadoRequisitoResponseDTO>> listar() {
        try {
            String jsonResult = repository.listar();
            return objectMapper.readValue(jsonResult,
                new TypeReference<StandardResponseDTO<List<TipoEstadoRequisitoResponseDTO>>>() {});
        } catch (Exception e) {
            log.error("Error al listar estados de requisito: {}", e.getMessage());
            return StandardResponseDTO.<List<TipoEstadoRequisitoResponseDTO>>builder()
                    .exito(false)
                    .mensaje("Error al listar estados de requisito: " + e.getMessage())
                    .build();
        }
    }

    @Override
    @Transactional
    public StandardResponseDTO<Integer> crear(TipoEstadoRequisitoRequestDTO request) {
        try {
            String jsonResult = repository.crear(
                request.getNombreEstado(),
                request.getDescripcion(),
                request.getCodigo()
            );
            return objectMapper.readValue(jsonResult, new TypeReference<StandardResponseDTO<Integer>>() {});
        } catch (Exception e) {
            log.error("Error al crear estado de requisito: {}", e.getMessage());
            return StandardResponseDTO.<Integer>builder()
                    .exito(false)
                    .mensaje("Error al crear estado de requisito: " + e.getMessage())
                    .build();
        }
    }

    @Override
    @Transactional
    public StandardResponseDTO<Integer> actualizar(Integer id, TipoEstadoRequisitoRequestDTO request) {
        try {
            String jsonResult = repository.actualizar(
                id,
                request.getNombreEstado(),
                request.getDescripcion(),
                request.getCodigo()
            );
            return objectMapper.readValue(jsonResult, new TypeReference<StandardResponseDTO<Integer>>() {});
        } catch (Exception e) {
            log.error("Error al actualizar estado de requisito: {}", e.getMessage());
            return StandardResponseDTO.<Integer>builder()
                    .exito(false)
                    .mensaje("Error al actualizar estado de requisito: " + e.getMessage())
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
            log.error("Error al desactivar estado de requisito: {}", e.getMessage());
            return StandardResponseDTO.<Integer>builder()
                    .exito(false)
                    .mensaje("Error al desactivar estado de requisito: " + e.getMessage())
                    .build();
        }
    }
}

