package org.uteq.sgacfinal.service.impl.configuracion;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.uteq.sgacfinal.dto.response.RespuestaOperacionDTO;
import org.uteq.sgacfinal.dto.response.convocatorias.TribunalEvaluacionResponseDTO;
import org.uteq.sgacfinal.repository.convocatorias.PostulacionTypeRepository;
import org.uteq.sgacfinal.service.convocatorias.IPostulacionTypeService;

@Slf4j
@Service
@RequiredArgsConstructor
public class PostulacionTypeServiceImpl implements IPostulacionTypeService {
    private final PostulacionTypeRepository postulacionRepository;

    @Override
    @Transactional
    public RespuestaOperacionDTO<TribunalEvaluacionResponseDTO> obtenerTribunalEvaluacion(Integer idUsuario) {
        log.info("Obteniendo información del tribunal para el usuario ID: {}", idUsuario);
        return postulacionRepository.obtenerTribunalEvaluacion(idUsuario);
    }
}