package org.uteq.sgacfinal.service.impl.catalogo;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.uteq.sgacfinal.dto.Request.TipoFaseRequestDTO;
import org.uteq.sgacfinal.dto.Response.StandardResponseDTO;
import org.uteq.sgacfinal.dto.Response.TipoFaseResponseDTO;
import org.uteq.sgacfinal.repository.catalogo.TipoFaseCatalogoRepository;
import org.uteq.sgacfinal.service.catalogo.ITipoFaseCatalogoService;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TipoFaseCatalogoServiceImpl implements ITipoFaseCatalogoService {

    private final TipoFaseCatalogoRepository repository;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional(readOnly = true)
    public StandardResponseDTO<List<TipoFaseResponseDTO>> listar() {
        try {
            String jsonResult = repository.listar();
            return objectMapper.readValue(jsonResult,
                new TypeReference<StandardResponseDTO<List<TipoFaseResponseDTO>>>() {});
        } catch (Exception e) {
            log.error("Error al listar tipos de fase: {}", e.getMessage());
            return StandardResponseDTO.<List<TipoFaseResponseDTO>>builder()
                    .exito(false)
                    .mensaje("Error al listar tipos de fase: " + e.getMessage())
                    .build();
        }
    }

    @Override
    @Transactional
    public StandardResponseDTO<Integer> crear(TipoFaseRequestDTO request) {
        try {
            String jsonResult = repository.crear(
                request.getCodigo(),
                request.getNombre(),
                request.getDescripcion(),
                request.getOrden()
            );
            return objectMapper.readValue(jsonResult, new TypeReference<StandardResponseDTO<Integer>>() {});
        } catch (Exception e) {
            log.error("Error al crear tipo de fase: {}", e.getMessage());
            return StandardResponseDTO.<Integer>builder()
                    .exito(false)
                    .mensaje("Error al crear tipo de fase: " + e.getMessage())
                    .build();
        }
    }

    @Override
    @Transactional
    public StandardResponseDTO<Integer> actualizar(Integer id, TipoFaseRequestDTO request) {
        try {
            String jsonResult = repository.actualizar(
                id,
                request.getCodigo(),
                request.getNombre(),
                request.getDescripcion(),
                request.getOrden()
            );
            return objectMapper.readValue(jsonResult, new TypeReference<StandardResponseDTO<Integer>>() {});
        } catch (Exception e) {
            log.error("Error al actualizar tipo de fase: {}", e.getMessage());
            return StandardResponseDTO.<Integer>builder()
                    .exito(false)
                    .mensaje("Error al actualizar tipo de fase: " + e.getMessage())
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
            log.error("Error al desactivar tipo de fase: {}", e.getMessage());
            return StandardResponseDTO.<Integer>builder()
                    .exito(false)
                    .mensaje("Error al desactivar tipo de fase: " + e.getMessage())
                    .build();
        }
    }
}

