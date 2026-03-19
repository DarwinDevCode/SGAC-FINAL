package org.uteq.sgacfinal.service;

import org.uteq.sgacfinal.dto.request.EstudianteRequestDTO;
import org.uteq.sgacfinal.dto.response.EstudianteResponseDTO;

public interface IEstudianteService {

    EstudianteResponseDTO crear(EstudianteRequestDTO request);

    EstudianteResponseDTO actualizar(Integer id, EstudianteRequestDTO request);

    EstudianteResponseDTO buscarPorId(Integer id);

    EstudianteResponseDTO buscarPorMatricula(String matricula);

    EstudianteResponseDTO buscarPorUsuario(Integer idUsuario);

    //List<EstudianteResponseDTO> listarPorCarrera(Integer idCarrera);
}