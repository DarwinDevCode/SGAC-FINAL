package org.uteq.sgacfinal.service.ayudantia;

import org.springframework.web.multipart.MultipartFile;
import org.uteq.sgacfinal.dto.request.ayudantia.CargarEvidenciaRequestDTO;
import org.uteq.sgacfinal.dto.request.ayudantia.FinalizarSesionRequestDTO;
import org.uteq.sgacfinal.dto.response.RespuestaOperacionDTO;
import org.uteq.sgacfinal.dto.response.ayudantia.BorradorSesionResponseDTO;
import org.uteq.sgacfinal.dto.response.ayudantia.EvidenciaIdResponseDTO;
import org.uteq.sgacfinal.dto.response.ayudantia.FinalizarSesionResponseDTO;

public interface CierreSesionService {
    RespuestaOperacionDTO<BorradorSesionResponseDTO> obtenerBorrador(Integer idUsuario, Integer idRegistro);
    RespuestaOperacionDTO<Void> guardarProgreso(Integer idUsuario, Integer idRegistro, String descripcion);
    RespuestaOperacionDTO<EvidenciaIdResponseDTO> cargarEvidencia(Integer idUsuario, Integer idRegistro, Integer idTipoEvidencia, MultipartFile archivo);    RespuestaOperacionDTO<Void> eliminarEvidencia(Integer idUsuario, Integer idEvidencia);
    RespuestaOperacionDTO<FinalizarSesionResponseDTO> finalizarSesion(FinalizarSesionRequestDTO request);

}