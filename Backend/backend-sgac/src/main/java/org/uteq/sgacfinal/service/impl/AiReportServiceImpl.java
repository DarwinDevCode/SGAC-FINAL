package org.uteq.sgacfinal.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
public class AiReportServiceImpl implements AiReportService {

    private static final Logger log = LoggerFactory.getLogger(AiReportServiceImpl.class);

    @Value("${gemini.api.key:}")
    private String geminiApiKey;

    @Value("${gemini.api.url:https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent}")
    private String geminiApiUrl;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public AiReportServiceImpl(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

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

            String prompt = "Actúa como un asistente académico profesional. Genera un INFORME MENSUAL DE ACTIVIDADES DE AYUDANTÍA DE CÁTEDRA. " +
                    "Usa un lenguaje formal, técnico y detallado. " +
                    "Estructura el informe en HTML (usando h1, h2, p, ul, li, strong, table, tr, td). " +
                    "El informe debe incluir: \n" +
                    "1. Un título profesional.\n" +
                    "2. Introducción descriptiva del periodo.\n" +
                    "3. Un Cuadro Resumen de actividades basado en estos datos (formatea los datos como una tabla HTML): \n" + payloadSesiones + "\n" +
                    "4. Conclusiones y recomendaciones basadas en las horas y temas tratados.\n\n" +
                    "IMPORTANTE: No incluyas etiquetas <html>, <head> ni <body>. Solo el contenido interno. " +
                    "No uses bloques de código (```html). Entrega directamente el código HTML.";

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
        StringBuilder sb = new StringBuilder();
        sb.append("<div style='font-family: Arial, sans-serif; color: #333;'>");
        sb.append("<h1 style='color: #2c3e50;'>Informe de Ayudantía (Borrador de Respaldo)</h1>");
        sb.append("<p>Este es un borrador generado automáticamente basado en las sesiones registradas.</p>");
        sb.append("<h2 style='color: #2980b9;'>1. Resumen de Actividades</h2>");
        sb.append("<table border='1' style='width:100%; border-collapse: collapse; margin-top: 10px;'>");
        sb.append("<tr style='background-color: #f2f2f2;'>");
        sb.append("<th style='padding: 8px; text-align: left;'>Fecha</th>");
        sb.append("<th style='padding: 8px; text-align: left;'>Tema</th>");
        sb.append("<th style='padding: 8px; text-align: left;'>Horas</th>");
        sb.append("</tr>");

        try {
            List<Map<String, Object>> sesiones = objectMapper.readValue(payload, List.class);
            for (Map<String, Object> s : sesiones) {
                sb.append("<tr>");
                sb.append("<td style='padding: 8px;'>").append(s.getOrDefault("fecha", "N/A")).append("</td>");
                sb.append("<td style='padding: 8px;'>").append(s.getOrDefault("temaTratado", "Sin tema")).append("</td>");
                sb.append("<td style='padding: 8px;'>").append(s.getOrDefault("horasObtenidas", "0")).append("</td>");
                sb.append("</tr>");
            }
        } catch (Exception e) {
            sb.append("<tr><td colspan='3' style='padding: 8px;'>Detalle de sesiones disponible en formato crudo: ").append(payload).append("</td></tr>");
        }

        sb.append("</table>");
        sb.append("<h2 style='color: #2980b9;'>2. Conclusiones</h2>");
        sb.append("<p>Las actividades se cumplieron satisfactoriamente según el registro adjunto.</p>");
        sb.append("</div>");
        return sb.toString();
    }
}
