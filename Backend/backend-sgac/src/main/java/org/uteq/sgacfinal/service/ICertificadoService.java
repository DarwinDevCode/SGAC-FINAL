package org.uteq.sgacfinal.service;

import org.uteq.sgacfinal.dto.request.CertificadoRequestDTO;
import org.uteq.sgacfinal.dto.response.CertificadoResponseDTO;
import java.util.List;

public interface ICertificadoService {

    CertificadoResponseDTO crear(CertificadoRequestDTO request);

    CertificadoResponseDTO actualizar(Integer id, CertificadoRequestDTO request);

    void desactivar(Integer id);

    CertificadoResponseDTO buscarPorId(Integer id);

    List<CertificadoResponseDTO> listarPorUsuario(Integer idUsuario);

    List<CertificadoResponseDTO> listarTodosActivos();
}