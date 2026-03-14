package org.uteq.sgacfinal.service;

public interface IFirmaDigitalService {

    /**
     * Envía la solicitud al endpoint /appfirmardocumento de FirmaEC (Mobile API).
     *
     * @param pkcs12Base64 El archivo .p12 convertido a String en Base64.
     * @param password La contraseña proveída por el usuario.
     * @param pdfBase64 El documento PDF a firmar, convertido a String en Base64.
     * @return Respuesta en JSON del servidor enviada por FirmaDigital.
     */
    String firmarDocumentoApp(String pkcs12Base64, String password, String pdfBase64, String rolFirmante);
}
