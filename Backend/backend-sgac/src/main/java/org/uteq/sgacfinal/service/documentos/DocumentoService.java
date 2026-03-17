package org.uteq.sgacfinal.service.documentos;

import org.springframework.web.multipart.MultipartFile;
import org.uteq.sgacfinal.dto.Request.documentos.DocumentoUpdateRequestDTO;
import org.uteq.sgacfinal.dto.Response.RespuestaOperacionDTO;
import org.uteq.sgacfinal.dto.Response.documentos.*;

import java.util.List;

public interface DocumentoService {
    RespuestaOperacionDTO<List<FacultadResponseDTO>> getFacultades();
    RespuestaOperacionDTO<List<CarreraResponseDTO>> getCarreras(Integer idFacultad);
    RespuestaOperacionDTO<List<TipoDocumentoResponseDTO>> getTiposDocumento();
    RespuestaOperacionDTO<DocumentoIdResponseDTO> guardarDocumento(MultipartFile archivo, String nombre, Integer idTipo, Integer idUser, Integer idFac, Integer idCar);
    RespuestaOperacionDTO<Void> actualizarDocumento(DocumentoUpdateRequestDTO req);
    RespuestaOperacionDTO<Void> eliminarDocumento(Integer idDocumento);
    RespuestaOperacionDTO<List<DocumentoVisorResponseDTO>> listarVisor(Integer idUsuario, String rol);
}