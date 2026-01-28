package com.sgac.service;

import com.sgac.dto.PostulacionDTO;
import com.sgac.dto.PostulacionRequest;
import java.util.List;

public interface PostulacionService {
    List<PostulacionDTO> findAll();
    List<PostulacionDTO> findByActivo(Boolean activo);
    List<PostulacionDTO> findByConvocatoria(Integer idConvocatoria);
    List<PostulacionDTO> findByEstudiante(Integer idEstudiante);
    PostulacionDTO findById(Integer id);
    PostulacionDTO create(PostulacionRequest request);
    PostulacionDTO update(Integer id, PostulacionRequest request);
    void delete(Integer id);
}
