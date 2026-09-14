package org.uteq.sgacfinal.service;

public interface IFirmaElectronicaService {
    String generarHashDocumento(byte[] contenidoPdf);
    boolean verificarFirma(String hashDocumento, String firma);
    void firmarDocumento(Integer idUsuario, Integer idInforme, Integer idCertificado, byte[] contenidoPdf, String certificadoBase64);
}
