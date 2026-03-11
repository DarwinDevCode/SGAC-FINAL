package org.uteq.sgacfinal.service.impl.configuracion;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.uteq.sgacfinal.dto.Request.configuracion.AjusteCronogramaRequestDTO;
import org.uteq.sgacfinal.dto.Response.StandardResponseDTO;
import org.uteq.sgacfinal.dto.Response.configuracion.PeriodoFaseResponseDTO;
import org.uteq.sgacfinal.repository.configuracion.ICronogramaRepository;
import org.uteq.sgacfinal.service.configuracion.ICronogramaService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CronogramaServiceImpl implements ICronogramaService {

    private final ICronogramaRepository repository;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional(readOnly = true)
    public StandardResponseDTO<List<PeriodoFaseResponseDTO>> listarCronograma(Integer idPeriodo) {
        try {
            String jsonResult = repository.listarCronogramaPorPeriodo(idPeriodo);
            return objectMapper.readValue(jsonResult, new TypeReference<StandardResponseDTO<List<PeriodoFaseResponseDTO>>>() {});
        } catch (Exception e) {
            log.error("Error al listar cronograma: {}", e.getMessage());
            return StandardResponseDTO.<List<PeriodoFaseResponseDTO>>builder()
                    .exito(false)
                    .mensaje("Error al obtener el cronograma: " + e.getMessage())
                    .build();
        }
    }

    @Override
    @Transactional
    public StandardResponseDTO<Integer> guardarCronograma(AjusteCronogramaRequestDTO request) {
        try {
            String fasesJson = objectMapper.writeValueAsString(request.getFases());
            String jsonResult = repository.ajustarCronogramaLote(request.getIdPeriodo(), fasesJson);

            return objectMapper.readValue(jsonResult, new TypeReference<StandardResponseDTO<Integer>>() {});
        } catch (Exception e) {
            log.error("Error al guardar cronograma: {}", e.getMessage());
            return StandardResponseDTO.<Integer>builder()
                    .exito(false)
                    .mensaje("Error de validación: " + e.getMessage())
                    .build();
        }
    }
}
