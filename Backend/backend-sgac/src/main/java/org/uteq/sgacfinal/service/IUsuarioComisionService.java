package org.uteq.sgacfinal.service;

import org.uteq.sgacfinal.dto.request.UsuarioComisionRequestDTO;
import org.uteq.sgacfinal.dto.response.UsuarioComisionResponseDTO;
import java.util.List;

public interface IUsuarioComisionService {

    UsuarioComisionResponseDTO asignarEvaluador(UsuarioComisionRequestDTO request);

    UsuarioComisionResponseDTO actualizarPuntajes(Integer id, UsuarioComisionRequestDTO request);

    void removerEvaluador(Integer id);

    UsuarioComisionResponseDTO buscarPorId(Integer id);

    List<UsuarioComisionResponseDTO> listarPorComision(Integer idComision);
}