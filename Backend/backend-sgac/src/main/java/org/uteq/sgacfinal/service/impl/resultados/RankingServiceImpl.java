package org.uteq.sgacfinal.service.impl.resultados;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.uteq.sgacfinal.exception.BadRequestException;
import org.uteq.sgacfinal.repository.resultados.IRankingRepository;
import org.uteq.sgacfinal.service.resultados.IRankingService;

@Slf4j
@Service
@RequiredArgsConstructor
public class RankingServiceImpl implements IRankingService {

    private final IRankingRepository repo;
    private final ObjectMapper        objectMapper;

    @Override
    @Transactional(readOnly = true)
    public JsonNode obtenerRankingResultados(Integer idUsuario, String rol) {
        try {
            String raw = repo.obtenerRankingResultados(idUsuario, rol);
            return parsear(raw);
        } catch (BadRequestException e) {
            throw e;
        } catch (Exception e) {
            log.error("[Ranking] obtenerRankingResultados idUsuario={} rol={}", idUsuario, rol, e);
            throw new BadRequestException("Error al obtener el ranking de resultados: " + e.getMessage());
        }
    }


    private JsonNode parsear(String raw) {
        try {
            JsonNode node = objectMapper.readTree(raw);
            if (node.isArray() && !node.isEmpty()) {
                JsonNode first = node.get(0);
                if (first.isObject() && first.fields().hasNext()) {
                    node = first.fields().next().getValue();
                }
            }
            return node;
        } catch (Exception e) {
            log.error("[Ranking] Error parseando respuesta PL/pgSQL: {}", raw);
            throw new BadRequestException("Respuesta inesperada del servidor al obtener el ranking.");
        }
    }
}