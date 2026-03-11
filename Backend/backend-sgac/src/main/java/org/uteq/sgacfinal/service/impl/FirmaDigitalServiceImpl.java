package org.uteq.sgacfinal.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import org.uteq.sgacfinal.service.IFirmaDigitalService;

@Service
@Slf4j
public class FirmaDigitalServiceImpl implements IFirmaDigitalService {

    private final RestClient restClient;

    @Value("${firmadigital.ws.mobile.url}")
    private String mobileApiUrl;

    public FirmaDigitalServiceImpl(RestClient.Builder restClientBuilder) {
        // Configuramos RestClient (disponible desde Spring Boot 3.2 / Spring Framework 6.1)
        this.restClient = restClientBuilder.build();
    }

    @Override
    public String firmarDocumentoApp(String pkcs12Base64, String password, String pdfBase64) {
        log.info("Iniciando firma digital de documento vía FirmaEC...");

        // Usamos MultiValueMap de Spring que equivale al "Form" de JAX-RS (x-www-form-urlencoded)
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("pkcs12", pkcs12Base64);
        formData.add("password", password);
        formData.add("documento", "archivo.pdf"); // El nombre reportado por la api
        formData.add("base64", pdfBase64);
        formData.add("json", ""); // Opcional, según la documentación
        // Nota: A veces piden un 'jwt'. Si es requerido por firmaEC se pasa vacío o el token correspondiente, pero la guía de tu amigo lo muestra como FormParam. Asumiremos que es opcional  si enviamos p12.

        String targetUrl = mobileApiUrl + "/appfirmardocumento";

        try {
            // Equivalent to Invocation.Builder en Java 17
            String respuestaJson = restClient.post()
                    .uri(targetUrl)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(formData)
                    .retrieve()
                    .body(String.class);

            log.info("FirmaEC procesó la petición exitosamente.");
            return respuestaJson;

        } catch (RestClientResponseException ex) {
            // Equivalente a WebApplicationException y ResteasyClientErrorException
            log.error("Error desde FirmaEC Code: {} - Body: {}", ex.getStatusCode(), ex.getResponseBodyAsString());
            throw new RuntimeException("Error en FirmaEC: " + ex.getResponseBodyAsString(), ex);
        } catch (Exception ex) {
            // Equivalente a NotFoundException u otros de red
            log.error("Fallo de red o servidor no disponible al conectar con FirmaEC", ex);
            throw new RuntimeException("Backend de FirmaEC no disponible.", ex);
        }
    }
}
