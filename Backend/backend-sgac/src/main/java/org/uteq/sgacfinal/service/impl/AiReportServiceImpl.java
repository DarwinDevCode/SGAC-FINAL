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

    @Value("${groq.api.key:}")
    private String groqApiKey;

    @Value("${groq.api.url:https://api.groq.com/openai/v1/chat/completions}")
    private String groqApiUrl;

    @Value("${groq.model:llama-3.1-70b-versatile}")
    private String groqModel;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public AiReportServiceImpl(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public String generateBorradorInforme(String payloadSesiones) {
        log.info("Iniciando generación de borrador con Groq. Key presente: {}", (groqApiKey != null && !groqApiKey.isEmpty()));
        if (groqApiKey == null || groqApiKey.isEmpty()) {
            log.warn("GROQ_API_KEY no configurada. Retornando texto mock.");
            return generarMockInforme(payloadSesiones);
        }

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(groqApiKey);

            String prompt = "Actúa como un Rector o Decano universitario experto en GESTIÓN DE CALIDAD ACADÉMICA. " +
                    "Tu misión es redactar un INFORME DE GESTIÓN MENSUAL DE AYUDANTÍA DE CÁTEDRA de nivel superior.\n\n" +
                    "### DIRECTRICES DE ALTA CALIDAD:\n" +
                    "1. **Dominio Lingüístico**: Usa un léxico sofisticado y técnico (ej. 'Paradigma pedagógico', 'Sinergia educativa', 'Mitigación de brechas de aprendizaje').\n" +
                    "2. **Elaboración Narrativa**: NO resumas. EXPANDE. Si los datos dicen 'Tutoría de Matrices', redacta: 'Se lideró una sesión de fortalecimiento cognitivo enfocada en el álgebra matricial, permitiendo a los estudiantes internalizar estructuras de datos complejas aplicadas a la ingeniería'.\n" +
                    "3. **Análisis Crítico**: Incluye párrafos de reflexión sobre cómo estas actividades impactan en los indicadores de rendimiento estudiantil.\n" +
                    "4. **Estética Ejecutiva**: Entrega código HTML con estilos CSS inline sofisticados (bordes redondeados, tipografía elegante, espaciado generoso, colores institucionales #0d47a1).\n\n" +
                    "### ESTRUCTURA DEL DOCUMENTO:\n" +
                    "- **Título**: 'INFORME EJECUTIVO DE DESEMPEÑO DOCENTE Y ASISTENCIA ACADÉMICA'.\n" +
                    "- **Contextualización**: Un análisis profundo del entorno educativo del mes.\n" +
                    "- **Matriz de Intervenciones**: Una TABLA HTML impecable con columnas: 'Cronología', 'Eje Temático/Intervención', 'Carga Horaria', 'Resultado/Logro Alcanzado'.\n" +
                    "- **Evaluación de Competencias**: Un apartado detallando las competencias desarrolladas por los estudiantes.\n" +
                    "- **Recomendaciones Estratégicas**: Sugerencias para el próximo ciclo basadas en lo observado.\n\n" +
                    "### INSUMOS (SESIONES):\n" +
                    payloadSesiones + "\n\n" +
                    "### RESTRICCIONES TÉCNICAS:\n" +
                    "- Entrega SOLO el HTML del contenedor principal (sin <html>, <head> o <body>).\n" +
                    "- NADA de bloques de código markdown.\n" +
                    "- Idioma: ESPAÑOL FORMAL DE ESPAÑA/AMÉRICA LATINA (Nivel C2).";

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", groqModel);
            requestBody.put("messages", List.of(
                    Map.of("role", "system", "content", "Eres una IA de élite especializada en redacción administrativa y académica universitaria de alto nivel."),
                    Map.of("role", "user", "content", prompt)
            ));
            requestBody.put("temperature", 0.6);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            log.info("Enviando petición a Groq URL: {}", groqApiUrl);
            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.postForObject(groqApiUrl, entity, Map.class);
            log.info("Respuesta recibida de Groq: {}", (response != null));

            if (response != null && response.containsKey("choices")) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
                if (!choices.isEmpty()) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
                    if (message != null && message.containsKey("content")) {
                        String result = (String) message.get("content");
                        if (result != null && !result.isBlank()) {
                            return result;
                        }
                    }
                }
            }

            log.error("La API de Groq respondió pero no se pudo extraer el contenido.");
            return generarMockInforme(payloadSesiones);
        } catch (Exception e) {
            log.error("Error crítico al interactuar con Groq AI: {}", e.getMessage(), e);
            return generarMockInforme(payloadSesiones);
        }
    }

    private String generarMockInforme(String payload) {
        StringBuilder sb = new StringBuilder();
        sb.append("<div style='font-family: Arial, sans-serif; color: #333;'>");
        sb.append("<h1 style='color: #2c3e50;'>Informe de Ayudantía (respaldo sin API)</h1>");
        sb.append("<p>Este es un borrador generado automáticamente basado en las sesiones registradas.</p>");
        sb.append("<h2 style='color: #2980b9;'>1. Resumen de Actividades</h2>");
        sb.append("<table border='1' style='width:100%; border-collapse: collapse; margin-top: 10px;'>");
        sb.append("<tr style='background-color: #f2f2f2;'>");
        sb.append("<th style='padding: 8px; text-align: left;'>Fecha</th>");
        sb.append("<th style='padding: 8px; text-align: left;'>Tema</th>");
        sb.append("<th style='padding: 8px; text-align: left;'>Horas</th>");
        sb.append("</tr>");

        try {
            @SuppressWarnings("unchecked")
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
