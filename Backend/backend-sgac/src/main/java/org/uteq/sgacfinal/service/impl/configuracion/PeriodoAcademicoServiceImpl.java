package org.uteq.sgacfinal.service.impl.configuracion;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.uteq.sgacfinal.dto.Request.configuracion.PeriodoAcademicoRequestDTO;
import org.uteq.sgacfinal.dto.Response.StandardResponseDTO;
import org.uteq.sgacfinal.repository.configuracion.IPeriodoAcademicoRepository;
import org.uteq.sgacfinal.service.configuracion.IPeriodoAcademicoService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
@RequiredArgsConstructor
@Slf4j
public class PeriodoAcademicoServiceImpl implements IPeriodoAcademicoService {

    private final IPeriodoAcademicoRepository repository;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public StandardResponseDTO<Integer> abrirPeriodo(PeriodoAcademicoRequestDTO request) {
        try {
            String jsonResult = repository.abrirPeriodo(
                    request.getNombrePeriodo(),
                    request.getFechaInicio(),
                    request.getFechaFin()
            );
            return objectMapper.readValue(jsonResult, new TypeReference<StandardResponseDTO<Integer>>() {});
        } catch (Exception e) {
            log.error("Error al abrir periodo académico: {}", e.getMessage());
            return StandardResponseDTO.<Integer>builder()
                    .exito(false)
                    .mensaje("Error técnico al abrir periodo: " + e.getMessage())
                    .build();
        }
    }
}