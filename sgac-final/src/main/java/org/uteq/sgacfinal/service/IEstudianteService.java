package org.uteq.sgacfinal.service;

import org.uteq.sgacfinal.dto.Request.EstudianteRequestDTO;
import org.uteq.sgacfinal.dto.Response.EstudianteResponseDTO;
import java.util.List;

public interface IEstudianteService {

    EstudianteResponseDTO crear(EstudianteRequestDTO request);

    EstudianteResponseDTO actualizar(Integer id, EstudianteRequestDTO request);

    EstudianteResponseDTO buscarPorId(Integer id);

    EstudianteResponseDTO buscarPorMatricula(String matricula);

    EstudianteResponseDTO buscarPorUsuario(Integer idUsuario);

    //List<EstudianteResponseDTO> listarPorCarrera(Integer idCarrera);
}