package org.uteq.sgacfinal.service;

import org.uteq.sgacfinal.dto.Request.InformeMensualRequest;
import org.uteq.sgacfinal.dto.Response.InformeMensualResponse;
import java.util.List;

public interface InformeMensualService {
    InformeMensualResponse generarBorradorConIAUsuario(Integer idUsuario, Integer mes, Integer anio);
    InformeMensualResponse enviarARevision(Integer idInformeMensual, InformeMensualRequest request);
    InformeMensualResponse aprobarInforme(Integer idInformeMensual, String rolDeAprobacion);
    InformeMensualResponse observarInforme(Integer idInformeMensual, String observaciones);
    InformeMensualResponse rechazarInforme(Integer idInformeMensual, String motivo);
    List<InformeMensualResponse> listarMisInformes(Integer idUsuario);
    List<InformeMensualResponse> listarInformesPorAyudantia(Integer idAyudantia);
    InformeMensualResponse obtenerDetalle(Integer idInformeMensual);
    List<InformeMensualResponse> listarInformesPorDocenteYEstado(Integer idDocente, String codigoEstado);
    List<InformeMensualResponse> listarInformesPorEstado(String codigoEstado);
}
