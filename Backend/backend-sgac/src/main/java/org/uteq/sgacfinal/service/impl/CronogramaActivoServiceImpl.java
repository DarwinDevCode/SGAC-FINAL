package org.uteq.sgacfinal.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.uteq.sgacfinal.dto.Response.configuracion.CronogramaActivoResponseDTO;
import org.uteq.sgacfinal.repository.ICronogramaActivoRepository;
import org.uteq.sgacfinal.service.ICronogramaActivoService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.transaction.annotation.Transactional;
import com.fasterxml.jackson.core.type.TypeReference;

@Service
@RequiredArgsConstructor
@Slf4j
public class CronogramaActivoServiceImpl implements ICronogramaActivoService {

    private final ICronogramaActivoRepository repository;
    private final ObjectMapper                objectMapper;

    @Override
    @Transactional(readOnly = true)
    public CronogramaActivoResponseDTO obtenerCronogramaActivo() {
        try {
            String json = repository.obtenerCronogramaActivo();
            return objectMapper.readValue(json, new TypeReference<>() {});
        } catch (Exception e) {
            log.error("Error al obtener cronograma activo: {}", e.getMessage());
            return CronogramaActivoResponseDTO.builder()
                    .exito(false)
                    .mensaje("Error técnico al obtener el cronograma: " + e.getMessage())
                    .build();
        }
    }
}