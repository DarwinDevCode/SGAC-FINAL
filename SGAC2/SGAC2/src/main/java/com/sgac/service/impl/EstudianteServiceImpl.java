package com.sgac.service.impl;

import com.sgac.dto.EstudianteDTO;
import com.sgac.dto.EstudianteRequest;
import com.sgac.entity.Carrera;
import com.sgac.entity.Estudiante;
import com.sgac.entity.Usuario;
import com.sgac.exception.ResourceNotFoundException;
import com.sgac.repository.CarreraRepository;
import com.sgac.repository.EstudianteRepository;
import com.sgac.repository.UsuarioRepository;
import com.sgac.service.EstudianteService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class EstudianteServiceImpl implements EstudianteService {

    private final EstudianteRepository estudianteRepository;
    private final UsuarioRepository usuarioRepository;
    private final CarreraRepository carreraRepository;

    @Override
    @Transactional(readOnly = true)
    public List<EstudianteDTO> findAll() {
        return estudianteRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<EstudianteDTO> findByCarrera(Integer idCarrera) {
        return estudianteRepository.findByCarreraIdCarrera(idCarrera).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public EstudianteDTO findById(Integer id) {
        Estudiante estudiante = estudianteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Estudiante", "id", id));
        return convertToDTO(estudiante);
    }

    @Override
    public EstudianteDTO create(EstudianteRequest request) {
        Usuario usuario = usuarioRepository.findById(request.getIdUsuario())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "id", request.getIdUsuario()));
        Carrera carrera = carreraRepository.findById(request.getIdCarrera())
                .orElseThrow(() -> new ResourceNotFoundException("Carrera", "id", request.getIdCarrera()));

        Estudiante estudiante = Estudiante.builder()
                .usuario(usuario)
                .carrera(carrera)
                .matricula(request.getMatricula())
                .semestre(request.getSemestre())
                .estadoAcademico(request.getEstadoAcademico())
                .build();
        return convertToDTO(estudianteRepository.save(estudiante));
    }

    @Override
    public EstudianteDTO update(Integer id, EstudianteRequest request) {
        Estudiante estudiante = estudianteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Estudiante", "id", id));
        Usuario usuario = usuarioRepository.findById(request.getIdUsuario())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "id", request.getIdUsuario()));
        Carrera carrera = carreraRepository.findById(request.getIdCarrera())
                .orElseThrow(() -> new ResourceNotFoundException("Carrera", "id", request.getIdCarrera()));

        estudiante.setUsuario(usuario);
        estudiante.setCarrera(carrera);
        estudiante.setMatricula(request.getMatricula());
        estudiante.setSemestre(request.getSemestre());
        estudiante.setEstadoAcademico(request.getEstadoAcademico());
        return convertToDTO(estudianteRepository.save(estudiante));
    }

    @Override
    public void delete(Integer id) {
        if (!estudianteRepository.existsById(id)) {
            throw new ResourceNotFoundException("Estudiante", "id", id);
        }
        estudianteRepository.deleteById(id);
    }

    private EstudianteDTO convertToDTO(Estudiante estudiante) {
        return EstudianteDTO.builder()
                .idEstudiante(estudiante.getIdEstudiante())
                .idUsuario(estudiante.getUsuario().getIdUsuario())
                .nombreCompleto(estudiante.getUsuario().getNombres() + " " + estudiante.getUsuario().getApellidos())
                .correo(estudiante.getUsuario().getCorreo())
                .idCarrera(estudiante.getCarrera().getIdCarrera())
                .nombreCarrera(estudiante.getCarrera().getNombreCarrera())
                .matricula(estudiante.getMatricula())
                .semestre(estudiante.getSemestre())
                .estadoAcademico(estudiante.getEstadoAcademico())
                .build();
    }
}
