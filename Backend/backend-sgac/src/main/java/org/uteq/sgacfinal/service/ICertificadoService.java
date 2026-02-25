package org.uteq.sgacfinal.service;

import org.uteq.sgacfinal.dto.Request.CertificadoRequestDTO;
import org.uteq.sgacfinal.dto.Response.CertificadoResponseDTO;
import java.util.List;

public interface ICertificadoService {

    CertificadoResponseDTO crear(CertificadoRequestDTO request);

    CertificadoResponseDTO actualizar(Integer id, CertificadoRequestDTO request);

    void desactivar(Integer id);

    CertificadoResponseDTO buscarPorId(Integer id);

    List<CertificadoResponseDTO> listarPorUsuario(Integer idUsuario);

    List<CertificadoResponseDTO> listarTodosActivos();
}