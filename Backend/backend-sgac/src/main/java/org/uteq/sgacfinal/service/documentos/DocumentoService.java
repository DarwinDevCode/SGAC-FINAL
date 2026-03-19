package org.uteq.sgacfinal.service.documentos;

import org.springframework.web.multipart.MultipartFile;
import org.uteq.sgacfinal.dto.request.documentos.DocumentoUpdateRequestDTO;
import org.uteq.sgacfinal.dto.response.RespuestaOperacionDTO;
import org.uteq.sgacfinal.dto.response.documentos.*;

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