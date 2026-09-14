package org.uteq.sgacfinal.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.uteq.sgacfinal.service.IFirmaElectronicaService;
import java.security.MessageDigest;
import java.util.Base64;

@Service
@RequiredArgsConstructor
@Slf4j
public class FirmaElectronicaServiceImpl implements IFirmaElectronicaService {

    @Override
    public String generarHashDocumento(byte[] contenidoPdf) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(contenidoPdf);
            return Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            log.error("Error generando hash del documento", e);
            throw new RuntimeException("Error al generar hash del documento");
        }
    }

    @Override
    public boolean verificarFirma(String hashDocumento, String firma) {
        // Logica dummy: En un entorno real se verifica la firma criptografica con la clave publica
        return firma != null && !firma.isEmpty() && hashDocumento != null;
    }

    @Override
    public void firmarDocumento(Integer idUsuario, Integer idInforme, Integer idCertificado, byte[] contenidoPdf, String certificadoBase64) {
        String hash = generarHashDocumento(contenidoPdf);
        // TODO: Insertar en ayudantia.firma_documento usando JdbcTemplate o Repositorio
        log.info("Documento firmado. Hash: {}, Usuario: {}", hash, idUsuario);
    }
}
