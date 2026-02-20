package org.uteq.sgacfinal.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.uteq.sgacfinal.dto.Request.EstudianteRequestDTO;
import org.uteq.sgacfinal.dto.Response.EstudianteResponseDTO;
import org.uteq.sgacfinal.entity.Estudiante;
import org.uteq.sgacfinal.repository.EstudianteRepository;
import org.uteq.sgacfinal.service.IEstudianteService;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class EstudianteServiceImpl implements IEstudianteService {

    private final EstudianteRepository estudianteRepository;

    @Override
    public EstudianteResponseDTO crear(EstudianteRequestDTO request) {
        Integer idGenerado = estudianteRepository.registrarEstudiante(
                request.getIdUsuario(),
                request.getIdCarrera(),
                request.getMatricula(),
                request.getSemestre(),
                request.getEstadoAcademico()
        );

        if (idGenerado == -1) {
            throw new RuntimeException("Error al registrar estudiante. Verifique matrícula duplicada.");
        }

        return buscarPorId(idGenerado);
    }

    @Override
    public EstudianteResponseDTO actualizar(Integer id, EstudianteRequestDTO request) {
        Integer resultado = estudianteRepository.actualizarEstudiante(
                id,
                request.getIdCarrera(),
                request.getSemestre(),
                request.getEstadoAcademico()
        );

        if (resultado == -1) {
            throw new RuntimeException("Error al actualizar información del estudiante.");
        }

        return buscarPorId(id);
    }

    @Override
    @Transactional(readOnly = true)
    public EstudianteResponseDTO buscarPorId(Integer id) {
        Estudiante estudiante = estudianteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Estudiante no encontrado con ID: " + id));
        return mapearADTO(estudiante);
    }

    @Override
    @Transactional(readOnly = true)
    public EstudianteResponseDTO buscarPorMatricula(String matricula) {
        Estudiante estudiante = estudianteRepository.buscarPorMatriculaSP(matricula)
                .orElseThrow(() -> new RuntimeException("No existe estudiante con matrícula: " + matricula));
        return mapearADTO(estudiante);
    }

    @Override
    @Transactional(readOnly = true)
    public EstudianteResponseDTO buscarPorUsuario(Integer idUsuario) {
        Estudiante estudiante = estudianteRepository.findByUsuario_IdUsuario(idUsuario)
                .orElseThrow(() -> new RuntimeException("No existe registro de estudiante para el usuario ID: " + idUsuario));
        return mapearADTO(estudiante);
    }

//    @Override
//    @Transactional(readOnly = true)
//    public List<EstudianteResponseDTO> listarPorCarrera(Integer idCarrera) {
//        return estudianteRepository.findByCarrera_IdCarrera(idCarrera).stream()
//                .map(this::mapearADTO)
//                .collect(Collectors.toList());
//    }

    private EstudianteResponseDTO mapearADTO(Estudiante entidad) {
        String nombreUsuario = entidad.getUsuario().getNombres() + " " + entidad.getUsuario().getApellidos();
        return EstudianteResponseDTO.builder()
                .idEstudiante(entidad.getIdEstudiante())
                .idUsuario(entidad.getUsuario().getIdUsuario())
                .nombreCompletoUsuario(nombreUsuario)
                .cedula(entidad.getUsuario().getCedula())
                .correo(entidad.getUsuario().getCorreo())
                .idCarrera(entidad.getCarrera().getIdCarrera())
                .matricula(entidad.getMatricula())
                .semestre(entidad.getSemestre())
                .estadoAcademico(entidad.getEstadoAcademico())
                .build();
    }
}