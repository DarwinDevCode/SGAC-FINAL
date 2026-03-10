package org.uteq.sgacfinal.service.impl.catalogo;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.uteq.sgacfinal.dto.Request.TipoEstadoPostulacionRequestDTO;
import org.uteq.sgacfinal.dto.Response.StandardResponseDTO;
import org.uteq.sgacfinal.dto.Response.TipoEstadoPostulacionResponseDTO;
import org.uteq.sgacfinal.repository.catalogo.TipoEstadoPostulacionCatalogoRepository;
import org.uteq.sgacfinal.service.catalogo.ITipoEstadoPostulacionCatalogoService;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TipoEstadoPostulacionCatalogoServiceImpl implements ITipoEstadoPostulacionCatalogoService {

    private final TipoEstadoPostulacionCatalogoRepository repository;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional(readOnly = true)
    public StandardResponseDTO<List<TipoEstadoPostulacionResponseDTO>> listar() {
        try {
            String jsonResult = repository.listar();
            return objectMapper.readValue(jsonResult,
                new TypeReference<StandardResponseDTO<List<TipoEstadoPostulacionResponseDTO>>>() {});
        } catch (Exception e) {
            log.error("Error al listar estados de postulación: {}", e.getMessage());
            return StandardResponseDTO.<List<TipoEstadoPostulacionResponseDTO>>builder()
                    .exito(false)
                    .mensaje("Error al listar estados de postulación: " + e.getMessage())
                    .build();
        }
    }

    @Override
    @Transactional
    public StandardResponseDTO<Integer> crear(TipoEstadoPostulacionRequestDTO request) {
        try {
            String jsonResult = repository.crear(
                request.getCodigo(),
                request.getNombre(),
                request.getDescripcion()
            );
            return objectMapper.readValue(jsonResult, new TypeReference<StandardResponseDTO<Integer>>() {});
        } catch (Exception e) {
            log.error("Error al crear estado de postulación: {}", e.getMessage());
            return StandardResponseDTO.<Integer>builder()
                    .exito(false)
                    .mensaje("Error al crear estado de postulación: " + e.getMessage())
                    .build();
        }
    }

    @Override
    @Transactional
    public StandardResponseDTO<Integer> actualizar(Integer id, TipoEstadoPostulacionRequestDTO request) {
        try {
            String jsonResult = repository.actualizar(
                id,
                request.getCodigo(),
                request.getNombre(),
                request.getDescripcion()
            );
            return objectMapper.readValue(jsonResult, new TypeReference<StandardResponseDTO<Integer>>() {});
        } catch (Exception e) {
            log.error("Error al actualizar estado de postulación: {}", e.getMessage());
            return StandardResponseDTO.<Integer>builder()
                    .exito(false)
                    .mensaje("Error al actualizar estado de postulación: " + e.getMessage())
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
            log.error("Error al desactivar estado de postulación: {}", e.getMessage());
            return StandardResponseDTO.<Integer>builder()
                    .exito(false)
                    .mensaje("Error al desactivar estado de postulación: " + e.getMessage())
                    .build();
        }
    }
}

