package org.uteq.sgacfinal.service.impl.catalogo;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.uteq.sgacfinal.dto.Request.TipoSancionAyudanteCatedraRequestDTO;
import org.uteq.sgacfinal.dto.Response.StandardResponseDTO;
import org.uteq.sgacfinal.dto.Response.TipoSancionAyudanteCatedraResponseDTO;
import org.uteq.sgacfinal.repository.catalogo.TipoSancionCatalogoRepository;
import org.uteq.sgacfinal.service.catalogo.ITipoSancionCatalogoService;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TipoSancionCatalogoServiceImpl implements ITipoSancionCatalogoService {

    private final TipoSancionCatalogoRepository repository;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional(readOnly = true)
    public StandardResponseDTO<List<TipoSancionAyudanteCatedraResponseDTO>> listar() {
        try {
            String jsonResult = repository.listar();
            return objectMapper.readValue(jsonResult,
                new TypeReference<StandardResponseDTO<List<TipoSancionAyudanteCatedraResponseDTO>>>() {});
        } catch (Exception e) {
            log.error("Error al listar tipos de sanción: {}", e.getMessage());
            return StandardResponseDTO.<List<TipoSancionAyudanteCatedraResponseDTO>>builder()
                    .exito(false)
                    .mensaje("Error al listar tipos de sanción: " + e.getMessage())
                    .build();
        }
    }

    @Override
    @Transactional
    public StandardResponseDTO<Integer> crear(TipoSancionAyudanteCatedraRequestDTO request) {
        try {
            String jsonResult = repository.crear(request.getNombreTipoSancion(), request.getCodigo());
            return objectMapper.readValue(jsonResult, new TypeReference<StandardResponseDTO<Integer>>() {});
        } catch (Exception e) {
            log.error("Error al crear tipo de sanción: {}", e.getMessage());
            return StandardResponseDTO.<Integer>builder()
                    .exito(false)
                    .mensaje("Error al crear tipo de sanción: " + e.getMessage())
                    .build();
        }
    }

    @Override
    @Transactional
    public StandardResponseDTO<Integer> actualizar(Integer id, TipoSancionAyudanteCatedraRequestDTO request) {
        try {
            String jsonResult = repository.actualizar(id, request.getNombreTipoSancion(), request.getCodigo());
            return objectMapper.readValue(jsonResult, new TypeReference<StandardResponseDTO<Integer>>() {});
        } catch (Exception e) {
            log.error("Error al actualizar tipo de sanción: {}", e.getMessage());
            return StandardResponseDTO.<Integer>builder()
                    .exito(false)
                    .mensaje("Error al actualizar tipo de sanción: " + e.getMessage())
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
            log.error("Error al desactivar tipo de sanción: {}", e.getMessage());
            return StandardResponseDTO.<Integer>builder()
                    .exito(false)
                    .mensaje("Error al desactivar tipo de sanción: " + e.getMessage())
                    .build();
        }
    }
}

