package org.uteq.sgacfinal.service;

import org.uteq.sgacfinal.dto.Request.ConfirmarActaRequestDTO;
import org.uteq.sgacfinal.dto.Request.GenerarActaRequestDTO;
import org.uteq.sgacfinal.dto.Response.ActaEvaluacionResponseDTO;

import java.util.List;

public interface IActaEvaluacionService {

    /**
     * Genera el PDF del acta (JasperReports), lo guarda y retorna la URL.
     */
    ActaEvaluacionResponseDTO generarActa(GenerarActaRequestDTO request);

    /**
     * Registra la confirmación de un miembro del tribunal.
     * Cuando los 3 confirman, cambia el estado del acta a CONFIRMADO.
     */
    ActaEvaluacionResponseDTO confirmarActa(ConfirmarActaRequestDTO request);

    /**
     * Lista todas las actas de una postulación.
     */
    List<ActaEvaluacionResponseDTO> listarPorPostulacion(Integer idPostulacion);

    void eliminar(Integer idActa);

    /**
     * Coordina la firma electrónica de un acta enviando el PDF físico a FirmaEC.
     * Retorna el nuevo DTO del acta con sus confirmaciones actualizadas.
     */
    ActaEvaluacionResponseDTO firmarActa(Integer idActa, org.springframework.web.multipart.MultipartFile archivoFirma, String password, String rolFirmante);
}
