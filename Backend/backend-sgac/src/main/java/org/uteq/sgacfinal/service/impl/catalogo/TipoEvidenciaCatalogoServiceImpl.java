package org.uteq.sgacfinal.service.impl.catalogo;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.uteq.sgacfinal.dto.Request.TipoEvidenciaRequestDTO;
import org.uteq.sgacfinal.dto.Response.StandardResponseDTO;
import org.uteq.sgacfinal.dto.Response.TipoEvidenciaResponseDTO;
import org.uteq.sgacfinal.repository.catalogo.TipoEvidenciaCatalogoRepository;
import org.uteq.sgacfinal.service.catalogo.ITipoEvidenciaCatalogoService;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TipoEvidenciaCatalogoServiceImpl implements ITipoEvidenciaCatalogoService {

    private final TipoEvidenciaCatalogoRepository repository;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional(readOnly = true)
    public StandardResponseDTO<List<TipoEvidenciaResponseDTO>> listar() {
        try {
            String jsonResult = repository.listar();
            return objectMapper.readValue(jsonResult,
                new TypeReference<StandardResponseDTO<List<TipoEvidenciaResponseDTO>>>() {});
        } catch (Exception e) {
            log.error("Error al listar tipos de evidencia: {}", e.getMessage());
            return StandardResponseDTO.<List<TipoEvidenciaResponseDTO>>builder()
                    .exito(false)
                    .mensaje("Error al listar tipos de evidencia: " + e.getMessage())
                    .build();
        }
    }

    @Override
    @Transactional
    public StandardResponseDTO<Integer> crear(TipoEvidenciaRequestDTO request) {
        try {
            String jsonResult = repository.crear(
                request.getNombre(),
                request.getExtensionPermitida(),
                request.getCodigo()
            );
            return objectMapper.readValue(jsonResult, new TypeReference<StandardResponseDTO<Integer>>() {});
        } catch (Exception e) {
            log.error("Error al crear tipo de evidencia: {}", e.getMessage());
            return StandardResponseDTO.<Integer>builder()
                    .exito(false)
                    .mensaje("Error al crear tipo de evidencia: " + e.getMessage())
                    .build();
        }
    }

    @Override
    @Transactional
    public StandardResponseDTO<Integer> actualizar(Integer id, TipoEvidenciaRequestDTO request) {
        try {
            String jsonResult = repository.actualizar(
                id,
                request.getNombre(),
                request.getExtensionPermitida(),
                request.getCodigo()
            );
            return objectMapper.readValue(jsonResult, new TypeReference<StandardResponseDTO<Integer>>() {});
        } catch (Exception e) {
            log.error("Error al actualizar tipo de evidencia: {}", e.getMessage());
            return StandardResponseDTO.<Integer>builder()
                    .exito(false)
                    .mensaje("Error al actualizar tipo de evidencia: " + e.getMessage())
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
            log.error("Error al desactivar tipo de evidencia: {}", e.getMessage());
            return StandardResponseDTO.<Integer>builder()
                    .exito(false)
                    .mensaje("Error al desactivar tipo de evidencia: " + e.getMessage())
                    .build();
        }
    }
}

