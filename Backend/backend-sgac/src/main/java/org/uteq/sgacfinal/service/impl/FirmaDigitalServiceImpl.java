package org.uteq.sgacfinal.service.impl;

import com.lowagie.text.Element;
import com.lowagie.text.pdf.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import org.uteq.sgacfinal.service.IFirmaDigitalService;

import java.io.ByteArrayOutputStream;
import java.util.Base64;

@Service
@Slf4j
@RequiredArgsConstructor
public class FirmaDigitalServiceImpl implements IFirmaDigitalService {

    private final RestClient restClient;

    @Value("${firmadigital.ws.mobile.url}")
    private String mobileApiUrl;

    @Value("${firmadigital.mode:PROD}")
    private String signatureMode;

    @Override
    public String firmarDocumentoApp(String pkcs12Base64, String password, String pdfBase64, String rolFirmante) {
        log.info("Iniciando firma electrónica para rol: {} (Modo: {})", rolFirmante, signatureMode);

        if ("DEV".equalsIgnoreCase(signatureMode)) {
            return firmarSimulado(pdfBase64, rolFirmante);
        }

        try {
            MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
            formData.add("pkcs12", pkcs12Base64);
            formData.add("password", password);
            formData.add("base64", pdfBase64);

            return restClient.post()
                    .uri(mobileApiUrl)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(formData)
                    .retrieve()
                    .body(String.class);

        } catch (RestClientResponseException ex) {
            log.error("FirmaEC retornó un error: {}", ex.getResponseBodyAsString());
            throw new RuntimeException("FirmaEC: " + ex.getResponseBodyAsString());
        } catch (Exception ex) {
            log.error("Error al conectar con el servicio de firma", ex);
            throw new RuntimeException("Servicio de firma no disponible.");
        }
    }

    private String firmarSimulado(String pdfBase64, String rolFirmante) {
        try {
            byte[] bytes = Base64.getDecoder().decode(pdfBase64);
            PdfReader reader = new PdfReader(bytes);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            PdfStamper stamper = new PdfStamper(reader, out);

            int totalPages = reader.getNumberOfPages();
            BaseFont bf = BaseFont.createFont(BaseFont.HELVETICA_BOLD, BaseFont.CP1252, BaseFont.NOT_EMBEDDED);
            java.awt.Color blueColor = new java.awt.Color(30, 64, 175);
            
            // 1. Marca de agua lateral en todas las páginas
            for (int i = 1; i <= totalPages; i++) {
                PdfContentByte over = stamper.getOverContent(i);
                over.beginText();
                over.setFontAndSize(bf, 8);
                over.setColorFill(blueColor);
                over.showTextAligned(Element.ALIGN_CENTER, "SGAC - DOCUMENTO FIRMADO EN MODO DESARROLLO - SIN VALOR LEGAL", 20, 300, 90);
                over.endText();
            }

            // 2. Firma visual en el cuadro respectivo (SOLO HOJA 1)
            float posX = 505; 
            float posY = 560; // Centrado en fila DECANO
            
            if ("COORDINADOR".equalsIgnoreCase(rolFirmante)) {
                posY = 505; // Centrado en fila COORDINADOR
            } else if ("DOCENTE".equalsIgnoreCase(rolFirmante)) {
                posY = 450; // Centrado en fila DOCENTE
            }

            PdfContentByte canvas = stamper.getOverContent(1);
            
            // Estampado
            canvas.setLineWidth(0.5f);
            canvas.setColorStroke(blueColor);
            canvas.roundRectangle(posX - 45, posY - 15, 90, 30, 3);
            canvas.stroke();

            canvas.beginText();
            canvas.setFontAndSize(bf, 6);
            canvas.setColorFill(blueColor);
            canvas.showTextAligned(Element.ALIGN_CENTER, "FIRMADO POR:", posX, posY + 8, 0);
            canvas.setFontAndSize(bf, 7);
            canvas.showTextAligned(Element.ALIGN_CENTER, rolFirmante, posX, posY - 2, 0);
            canvas.setFontAndSize(bf, 5);
            String fecha = java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
            canvas.showTextAligned(Element.ALIGN_CENTER, "MODO PRUEBA - " + fecha, posX, posY - 10, 0);
            canvas.endText();

            stamper.close();
            reader.close();

            String signedBase64 = Base64.getEncoder().encodeToString(out.toByteArray());
            return "{\"documento\": \"" + signedBase64 + "\"}";
        } catch (Exception e) {
            log.error("Error en firma simulada", e);
            throw new RuntimeException("Error simulando firma local.");
        }
    }
}
