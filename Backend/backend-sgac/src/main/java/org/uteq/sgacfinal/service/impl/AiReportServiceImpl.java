package org.uteq.sgacfinal.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.uteq.sgacfinal.service.AiReportService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class AiReportServiceImpl implements AiReportService {

    @Value("${gemini.api.key:}")
    private String geminiApiKey;

    @Value("${gemini.api.url:https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent}")
    private String geminiApiUrl;

    private final RestTemplate restTemplate;

    @Override
    public String generateBorradorInforme(String payloadSesiones) {
        if (geminiApiKey == null || geminiApiKey.isEmpty()) {
            log.warn("GEMINI_API_KEY no configurada. Retornando texto mock.");
            return generarMockInforme(payloadSesiones);
        }

        try {
            String url = geminiApiUrl + "?key=" + geminiApiKey;

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            String prompt = "Genera un informe formal mensual de ayudantía de cátedra estructurado en HTML básico (solo h1, h2, p, ul, li) " +
                    "con las siguientes secciones: Introducción, Resumen de Actividades, Evidencias y Conclusiones. " +
                    "La información en la que debes basarte es la siguiente (en formato JSON anónimo): \n" + payloadSesiones +
                    "\n\nRecuerda: Escribe en tono formal y académico. Solo general el HTML del contenido, sin las etiquetas html/body envolventes.";

            Map<String, Object> requestBody = new HashMap<>();
            Map<String, Object> contentList = new HashMap<>();
            Map<String, Object> partsList = new HashMap<>();
            
            partsList.put("text", prompt);
            contentList.put("parts", List.of(partsList));
            requestBody.put("contents", List.of(contentList));

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            Map response = restTemplate.postForObject(url, entity, Map.class);
            
            if (response != null && response.containsKey("candidates")) {
                List<Map<String, Object>> candidates = (List<Map<String, Object>>) response.get("candidates");
                if (!candidates.isEmpty()) {
                    Map<String, Object> contentMap = (Map<String, Object>) candidates.get(0).get("content");
                    if (contentMap != null && contentMap.containsKey("parts")) {
                        List<Map<String, Object>> textParts = (List<Map<String, Object>>) contentMap.get("parts");
                        if (!textParts.isEmpty()) {
                            return (String) textParts.get(0).get("text");
                        }
                    }
                }
            }

            return generarMockInforme(payloadSesiones);
        } catch (Exception e) {
            log.error("Error al generar borrador con Gemini AI: {}", e.getMessage(), e);
            return generarMockInforme(payloadSesiones);
        }
    }

    private String generarMockInforme(String payload) {
        return "<h1>Informe de Ayudantía (Borrador Autogenerado)</h1>" +
               "<h2>Introducción</h2><p>El presente informe detalla las actividades...</p>" +
               "<h2>Resumen de Actividades</h2><p>La información recopilada fue: " + payload + "</p>" +
               "<h2>Conclusiones</h2><p>Las actividades se cumplieron a cabalidad.</p>";
    }
}
