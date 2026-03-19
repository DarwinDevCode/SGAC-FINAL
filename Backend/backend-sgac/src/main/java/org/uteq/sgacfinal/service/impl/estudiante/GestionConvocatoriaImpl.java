package org.uteq.sgacfinal.service.impl.estudiante;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.uteq.sgacfinal.dto.response.estudiante.ConvocatoriaEstudianteDTO;
import org.uteq.sgacfinal.repository.estudiante.IGestionConvocatoria;
import org.uteq.sgacfinal.service.estudiante.GestionConvocatoriaService;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GestionConvocatoriaImpl implements GestionConvocatoriaService {
    private final IGestionConvocatoria gestionConvocatoria;

    @Override
    public List<ConvocatoriaEstudianteDTO> listarConvocatoriasEstudiante(Integer idUsuario) {
        return gestionConvocatoria.listarConvocatoriasEstudiante(idUsuario);
    }
}
