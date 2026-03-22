package org.uteq.sgacfinal.service.impl.convocatorias;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.uteq.sgacfinal.dto.Response.convocatorias.FinalizarSeleccionResponseDTO;
import org.uteq.sgacfinal.exception.OposicionBusinessException;
import org.uteq.sgacfinal.repository.convocatorias.IFinalizarSeleccionRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class FinalizarSeleccionService {

    private final IFinalizarSeleccionRepository repo;
    private final ObjectMapper objectMapper;

    @Transactional
    public FinalizarSeleccionResponseDTO finalizar(Integer idConvocatoria) {
        try {
            String raw = repo.finalizarProcesoSeleccion(idConvocatoria);
            return parsear(raw, idConvocatoria);
        } catch (OposicionBusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("[FinalizarSeleccion] Error al finalizar convocatoria id={}", idConvocatoria, e);
            throw new OposicionBusinessException(
                    "Error al ejecutar el proceso de cierre: " + e.getMessage());
        }
    }

    private FinalizarSeleccionResponseDTO parsear(String raw, Integer idConvocatoria) {
        try {
            JsonNode node = objectMapper.readTree(raw);
            if (node.isArray() && !node.isEmpty()) {
                JsonNode first = node.get(0);
                if (first.isObject() && first.fields().hasNext())
                    node = first.fields().next().getValue();
            }

            boolean exito = node.path("exito").asBoolean(true);
            if (!exito) {
                String msg = node.path("mensaje").asText("Error en el proceso de cierre.");
                throw new OposicionBusinessException(msg);
            }

            return FinalizarSeleccionResponseDTO.builder()
                    .exito(true)
                    .mensaje(node.path("mensaje").asText())
                    .seleccionados(node.path("seleccionados").asInt(0))
                    .elegibles(node.path("elegibles").asInt(0))
                    .noSeleccionados(node.path("noSeleccionados").asInt(0))
                    .build();

        } catch (OposicionBusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("[FinalizarSeleccion] Error al parsear respuesta para convocatoria {}: {}",
                    idConvocatoria, raw, e);
            throw new OposicionBusinessException("Respuesta inesperada del servidor al finalizar la selección.");
        }
    }
}