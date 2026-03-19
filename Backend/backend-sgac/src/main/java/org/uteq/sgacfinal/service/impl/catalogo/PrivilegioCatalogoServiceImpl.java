package org.uteq.sgacfinal.service.impl.catalogo;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.uteq.sgacfinal.dto.request.PrivilegioRequestDTO;
import org.uteq.sgacfinal.dto.response.PrivilegioFuncionResponseDTO;
import org.uteq.sgacfinal.dto.response.StandardResponseDTO;
import org.uteq.sgacfinal.repository.catalogo.PrivilegioCatalogoRepository;
import org.uteq.sgacfinal.service.catalogo.IPrivilegioCatalogoService;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PrivilegioCatalogoServiceImpl implements IPrivilegioCatalogoService {

    private final PrivilegioCatalogoRepository repository;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional(readOnly = true)
    public StandardResponseDTO<List<PrivilegioFuncionResponseDTO>> listar() {
        try {
            String jsonResult = repository.listar();
            return objectMapper.readValue(jsonResult,
                new TypeReference<StandardResponseDTO<List<PrivilegioFuncionResponseDTO>>>() {});
        } catch (Exception e) {
            log.error("Error al listar privilegios: {}", e.getMessage());
            return StandardResponseDTO.<List<PrivilegioFuncionResponseDTO>>builder()
                    .exito(false)
                    .mensaje("Error al listar privilegios: " + e.getMessage())
                    .build();
        }
    }

    @Override
    @Transactional
    public StandardResponseDTO<Integer> crear(PrivilegioRequestDTO request) {
        try {
            String jsonResult = repository.crear(
                request.getNombrePrivilegio(),
                request.getCodigoInterno(),
                request.getDescripcion()
            );
            return objectMapper.readValue(jsonResult, new TypeReference<StandardResponseDTO<Integer>>() {});
        } catch (Exception e) {
            log.error("Error al crear privilegio: {}", e.getMessage());
            return StandardResponseDTO.<Integer>builder()
                    .exito(false)
                    .mensaje("Error al crear privilegio: " + e.getMessage())
                    .build();
        }
    }

    @Override
    @Transactional
    public StandardResponseDTO<Integer> actualizar(Integer id, PrivilegioRequestDTO request) {
        try {
            String jsonResult = repository.actualizar(
                id,
                request.getNombrePrivilegio(),
                request.getCodigoInterno(),
                request.getDescripcion()
            );
            return objectMapper.readValue(jsonResult, new TypeReference<StandardResponseDTO<Integer>>() {});
        } catch (Exception e) {
            log.error("Error al actualizar privilegio: {}", e.getMessage());
            return StandardResponseDTO.<Integer>builder()
                    .exito(false)
                    .mensaje("Error al actualizar privilegio: " + e.getMessage())
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
            log.error("Error al desactivar privilegio: {}", e.getMessage());
            return StandardResponseDTO.<Integer>builder()
                    .exito(false)
                    .mensaje("Error al desactivar privilegio: " + e.getMessage())
                    .build();
        }
    }
}

