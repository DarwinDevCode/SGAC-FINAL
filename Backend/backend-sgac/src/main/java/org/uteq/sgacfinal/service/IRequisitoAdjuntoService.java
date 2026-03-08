package org.uteq.sgacfinal.service;

import org.springframework.web.multipart.MultipartFile;
import org.uteq.sgacfinal.dto.Request.RequisitoAdjuntoRequestDTO;
import org.uteq.sgacfinal.dto.Response.RequisitoAdjuntoResponseDTO;
import org.uteq.sgacfinal.dto.Response.SubsanacionDocumentoResponseDTO;

import java.util.List;

public interface IRequisitoAdjuntoService {

    RequisitoAdjuntoResponseDTO crear(RequisitoAdjuntoRequestDTO request);

    RequisitoAdjuntoResponseDTO actualizar(Integer id, RequisitoAdjuntoRequestDTO request);

    RequisitoAdjuntoResponseDTO buscarPorId(Integer id);

    List<RequisitoAdjuntoResponseDTO> listarPorPostulacion(Integer idPostulacion);

    /** Ítem 8: el postulante reemplaza un documento observado */
    RequisitoAdjuntoResponseDTO reemplazar(Integer idAdjunto, MultipartFile archivo);

    /**
     * Subsana un documento observado con validación de fechas y notificación al coordinador.
     * Solo permite reemplazo si:
     * 1. El documento está en estado OBSERVADO
     * 2. Estamos dentro del periodo de subsanación (etapa de revisión)
     *
     * @param idUsuario ID del usuario estudiante
     * @param idRequisitoAdjunto ID del documento a subsanar
     * @param archivo Nuevo archivo
     * @return DTO con resultado de la operación
     */
    SubsanacionDocumentoResponseDTO subsanarDocumentoObservado(Integer idUsuario, Integer idRequisitoAdjunto, MultipartFile archivo);
}