package org.uteq.sgacfinal.service;

import org.springframework.web.multipart.MultipartFile;
import org.uteq.sgacfinal.dto.Request.RequisitoAdjuntoRequestDTO;
import org.uteq.sgacfinal.dto.Response.RequisitoAdjuntoResponseDTO;
import java.util.List;

public interface IRequisitoAdjuntoService {

    RequisitoAdjuntoResponseDTO crear(RequisitoAdjuntoRequestDTO request);

    RequisitoAdjuntoResponseDTO actualizar(Integer id, RequisitoAdjuntoRequestDTO request);

    RequisitoAdjuntoResponseDTO buscarPorId(Integer id);

    List<RequisitoAdjuntoResponseDTO> listarPorPostulacion(Integer idPostulacion);

    /** Ítem 8: el postulante reemplaza un documento observado */
    RequisitoAdjuntoResponseDTO reemplazar(Integer idAdjunto, MultipartFile archivo);
}