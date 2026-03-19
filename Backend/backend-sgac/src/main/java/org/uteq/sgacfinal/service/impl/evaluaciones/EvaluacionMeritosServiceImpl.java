package org.uteq.sgacfinal.service.impl.evaluaciones;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.uteq.sgacfinal.dto.request.evaluaciones.GuardarMeritosRequest;
import org.uteq.sgacfinal.exception.BadRequestException;
import org.uteq.sgacfinal.repository.evaluaciones.IEvaluacionMeritosRepository;
import org.uteq.sgacfinal.service.evaluaciones.IEvaluacionMeritosService;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class EvaluacionMeritosServiceImpl implements IEvaluacionMeritosService {

    private final IEvaluacionMeritosRepository repo;
    private final ObjectMapper                  objectMapper;

    @Override
    @Transactional(readOnly = true)
    public JsonNode listarPostulacionesParaMeritos(Integer idUsuario) {
        try {
            return parsear(repo.listarPostulacionesParaMeritos(idUsuario),
                    "listarPostulacionesParaMeritos");
        } catch (BadRequestException e) {
            throw e;
        } catch (Exception e) {
            log.error("[Meritos] listarPostulaciones idUsuario={}", idUsuario, e);
            throw new BadRequestException("Error al listar postulaciones: " + e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public JsonNode obtenerEvaluacionMeritos(Integer idPostulacion, Integer idUsuario) {
        try {
            return parsear(repo.obtenerEvaluacionMeritos(idPostulacion, idUsuario),
                    "obtenerEvaluacionMeritos");
        } catch (BadRequestException e) {
            throw e;
        } catch (Exception e) {
            log.error("[Meritos] obtener idPostulacion={}", idPostulacion, e);
            throw new BadRequestException("Error al obtener la evaluación: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public JsonNode guardarEvaluacionMeritos(GuardarMeritosRequest req, Integer idUsuario) {
        try {
            String semestresJson = buildSemestresJson(req.getSemestresNotas());

            String raw = repo.guardarEvaluacionMeritos(
                    req.getIdPostulacion(),
                    idUsuario,
                    req.getNotaAprobacionAsignatura().toPlainString(),
                    semestresJson,
                    req.getNotaExperiencia().toPlainString(),
                    req.getNotaEventos().toPlainString(),
                    req.isFinalizar()
            );
            return parsear(raw, "guardarEvaluacionMeritos");

        } catch (BadRequestException e) {
            throw e;
        } catch (Exception e) {
            log.error("[Meritos] guardar idPostulacion={} idUsuario={}", req.getIdPostulacion(), idUsuario, e);
            throw new BadRequestException("Error al guardar la evaluación: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public JsonNode reabrirEvaluacionMeritos(Integer idPostulacion, Integer idUsuario) {
        try {
            return parsear(repo.reabrirEvaluacionMeritos(idPostulacion, idUsuario),
                    "reabrirEvaluacionMeritos");
        } catch (BadRequestException e) {
            throw e;
        } catch (Exception e) {
            log.error("[Meritos] reabrir idPostulacion={} idUsuario={}", idPostulacion, idUsuario, e);
            throw new BadRequestException("Error al reabrir la evaluación: " + e.getMessage());
        }
    }

    private String buildSemestresJson(List<BigDecimal> notas) throws Exception {
        if (notas == null || notas.isEmpty()) return "[]";
        String valores = notas.stream()
                .map(BigDecimal::toPlainString)
                .collect(Collectors.joining(","));
        return "[" + valores + "]";
    }

    private JsonNode parsear(String raw, String contexto) {
        try {
            JsonNode node = objectMapper.readTree(raw);
            if (node.isArray() && !node.isEmpty()) {
                JsonNode first = node.get(0);
                if (first.isObject() && first.fields().hasNext()) {
                    node = first.fields().next().getValue();
                }
            }
            if (!node.path("exito").asBoolean(true)) {
                String msg = node.path("mensaje").asText("Error en " + contexto);
                throw new BadRequestException(msg);
            }
            return node;
        } catch (BadRequestException e) {
            throw e;
        } catch (Exception e) {
            log.error("[Meritos] Error parseando respuesta de {}: {}", contexto, raw);
            throw new BadRequestException("Respuesta inesperada del servidor.");
        }
    }
}