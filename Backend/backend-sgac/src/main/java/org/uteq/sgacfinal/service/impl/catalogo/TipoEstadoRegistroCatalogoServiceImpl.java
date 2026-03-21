package org.uteq.sgacfinal.service.impl.catalogo;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.uteq.sgacfinal.dto.request.TipoEstadoRegistroRequestDTO;
import org.uteq.sgacfinal.dto.response.StandardResponseDTO;
import org.uteq.sgacfinal.dto.response.TipoEstadoRegistroResponseDTO;
import org.uteq.sgacfinal.repository.catalogo.TipoEstadoRegistroCatalogoRepository;
import org.uteq.sgacfinal.service.catalogo.ITipoEstadoRegistroCatalogoService;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TipoEstadoRegistroCatalogoServiceImpl implements ITipoEstadoRegistroCatalogoService {

    private final TipoEstadoRegistroCatalogoRepository repository;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional(readOnly = true)
    public StandardResponseDTO<List<TipoEstadoRegistroResponseDTO>> listar() {
        try {
            String jsonResult = repository.listar();
            return objectMapper.readValue(jsonResult,
                new TypeReference<StandardResponseDTO<List<TipoEstadoRegistroResponseDTO>>>() {});
        } catch (Exception e) {
            log.error("Error al listar estados de registro: {}", e.getMessage());
            return StandardResponseDTO.<List<TipoEstadoRegistroResponseDTO>>builder()
                    .exito(false)
                    .mensaje("Error al listar estados de registro: " + e.getMessage())
                    .build();
        }
    }

    @Override
    @Transactional
    public StandardResponseDTO<Integer> crear(TipoEstadoRegistroRequestDTO request) {
        try {
            String jsonResult = repository.crear(
                request.getNombreEstado(),
                request.getDescripcion(),
                request.getCodigo()
            );
            return objectMapper.readValue(jsonResult, new TypeReference<StandardResponseDTO<Integer>>() {});
        } catch (Exception e) {
            log.error("Error al crear estado de registro: {}", e.getMessage());
            return StandardResponseDTO.<Integer>builder()
                    .exito(false)
                    .mensaje("Error al crear estado de registro: " + e.getMessage())
                    .build();
        }
    }

    @Override
    @Transactional
    public StandardResponseDTO<Integer> actualizar(Integer id, TipoEstadoRegistroRequestDTO request) {
        try {
            String jsonResult = repository.actualizar(
                id,
                request.getNombreEstado(),
                request.getDescripcion(),
                request.getCodigo()
            );
            return objectMapper.readValue(jsonResult, new TypeReference<StandardResponseDTO<Integer>>() {});
        } catch (Exception e) {
            log.error("Error al actualizar estado de registro: {}", e.getMessage());
            return StandardResponseDTO.<Integer>builder()
                    .exito(false)
                    .mensaje("Error al actualizar estado de registro: " + e.getMessage())
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
            log.error("Error al desactivar estado de registro: {}", e.getMessage());
            return StandardResponseDTO.<Integer>builder()
                    .exito(false)
                    .mensaje("Error al desactivar estado de registro: " + e.getMessage())
                    .build();
        }
    }
}

