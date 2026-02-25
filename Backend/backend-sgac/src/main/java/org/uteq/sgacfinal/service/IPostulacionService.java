package org.uteq.sgacfinal.service;

import org.springframework.web.multipart.MultipartFile;
import org.uteq.sgacfinal.dto.Request.PostulacionRequestDTO;
import org.uteq.sgacfinal.dto.Response.PostulacionResponseDTO;
import java.util.List;

public interface IPostulacionService {

    PostulacionResponseDTO crear(PostulacionRequestDTO request);
    PostulacionResponseDTO actualizar(Integer id, PostulacionRequestDTO request);
    void desactivar(Integer id);
    PostulacionResponseDTO buscarPorId(Integer id);
    List<PostulacionResponseDTO> listarPorEstudiante(Integer idEstudiante);
    String registrarPostulacionCompleta(PostulacionRequestDTO request, List<MultipartFile> archivos, List<Integer> tiposRequisito);    List<PostulacionResponseDTO> listarPorConvocatoria(Integer idConvocatoria);
    void actualizarEstado(Integer idPostulacion, String nuevoEstado, String observacion);

}