package org.uteq.sgacfinal.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.uteq.sgacfinal.dto.request.IaAsistenteRequestDTO;
import org.uteq.sgacfinal.dto.response.IaAsistenteResponseDTO;
import org.uteq.sgacfinal.service.IIaAsistenteService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class IaAsistenteServiceImpl implements IIaAsistenteService {

    @Value("${ia.api.url}")
    private String apiUrl;

    @Value("${ia.api.key}")
    private String apiKey;

    @Value("${ia.api.model}")
    private String apiModel;

    @Override
    public IaAsistenteResponseDTO consultar(IaAsistenteRequestDTO request, Integer idUsuario) {
        if (apiKey == null || apiKey.trim().isEmpty()) {
            return IaAsistenteResponseDTO.builder()
                    .respuesta("El servicio de IA no está configurado (Falta API Key).")
                    .tokensUsados(0)
                    .build();
        }

        String systemPrompt = getSystemPrompt(request.getTipoConsulta());

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        Map<String, Object> messageSystem = new HashMap<>();
        messageSystem.put("role", "system");
        messageSystem.put("content", systemPrompt);

        Map<String, Object> messageUser = new HashMap<>();
        messageUser.put("role", "user");
        messageUser.put("content", request.getPrompt());

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", apiModel);
        requestBody.put("messages", List.of(messageSystem, messageUser));
        requestBody.put("temperature", 0.7);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(apiUrl, entity, Map.class);
            Map<String, Object> body = response.getBody();
            if (body != null && body.containsKey("choices")) {
                List<Map<String, Object>> choices = (List<Map<String, Object>>) body.get("choices");
                Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
                String respuestaTexto = (String) message.get("content");
                
                // TODO: Registrar auditoria en BD usando JdbcTemplate o repositorio de log_asistente_ia
                
                return IaAsistenteResponseDTO.builder()
                        .respuesta(respuestaTexto)
                        .tokensUsados(0) // Extraer si es necesario de response.usage
                        .build();
            }
        } catch (Exception e) {
            log.error("Error al consultar la IA", e);
            return IaAsistenteResponseDTO.builder()
                    .respuesta("Ocurrió un error al consultar al asistente IA: " + e.getMessage())
                    .tokensUsados(0)
                    .build();
        }
        
        return IaAsistenteResponseDTO.builder().respuesta("Sin respuesta").tokensUsados(0).build();
    }

    private String getSystemPrompt(String tipo) {
        if ("REGLAMENTO".equalsIgnoreCase(tipo)) {
            return "Eres un asistente legal experto en el Reglamento de Ayudantías de Cátedra de la UTEQ. Responde preguntas basándote en que los estudiantes deben tener buen promedio, haber cursado la materia y cumplir máximo 20 horas semanales. Limita tu respuesta a las normativas universitarias.";
        } else if ("REVISION_TEXTO".equalsIgnoreCase(tipo)) {
            return "Eres un asistente de redacción. Revisa el borrador o documento enviado por el usuario (docente o coordinador) y corrige ortografía, mejora la redacción y dale un tono formal y académico. No cambies el sentido del mensaje.";
        } else if ("SUGERENCIA_RUBRICA".equalsIgnoreCase(tipo)) {
            return "Eres un experto en evaluación académica universitaria. Sugiere una rúbrica de calificación sobre 100 puntos basada en los requerimientos que te pida el usuario. Entrega la rúbrica en formato JSON estructurado o tabla.";
        }
        return "Eres un asistente académico de la UTEQ.";
    }
}
