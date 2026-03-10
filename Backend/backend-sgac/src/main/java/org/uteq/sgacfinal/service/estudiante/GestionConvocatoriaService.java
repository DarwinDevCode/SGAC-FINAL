package org.uteq.sgacfinal.service.estudiante;

import org.uteq.sgacfinal.dto.Response.estudiante.ConvocatoriaEstudianteDTO;

import java.util.List;

public interface GestionConvocatoriaService {
    List<ConvocatoriaEstudianteDTO> listarConvocatoriasEstudiante(Integer idUsuario);
}
