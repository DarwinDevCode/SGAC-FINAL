package com.sgac.service.impl;

import com.sgac.dto.CoordinadorDTO;
import com.sgac.dto.CoordinadorRequest;
import com.sgac.entity.Carrera;
import com.sgac.entity.Coordinador;
import com.sgac.entity.Usuario;
import com.sgac.exception.ResourceNotFoundException;
import com.sgac.repository.CarreraRepository;
import com.sgac.repository.CoordinadorRepository;
import com.sgac.repository.UsuarioRepository;
import com.sgac.service.CoordinadorService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class CoordinadorServiceImpl implements CoordinadorService {

    private final CoordinadorRepository coordinadorRepository;
    private final UsuarioRepository usuarioRepository;
    private final CarreraRepository carreraRepository;

    @Override
    @Transactional(readOnly = true)
    public List<CoordinadorDTO> findAll() {
        return coordinadorRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CoordinadorDTO> findByActivo(Boolean activo) {
        return coordinadorRepository.findByActivo(activo).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public CoordinadorDTO findById(Integer id) {
        Coordinador coordinador = coordinadorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Coordinador", "id", id));
        return convertToDTO(coordinador);
    }

    @Override
    public CoordinadorDTO create(CoordinadorRequest request) {
        Usuario usuario = usuarioRepository.findById(request.getIdUsuario())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "id", request.getIdUsuario()));
        Carrera carrera = carreraRepository.findById(request.getIdCarrera())
                .orElseThrow(() -> new ResourceNotFoundException("Carrera", "id", request.getIdCarrera()));

        Coordinador coordinador = Coordinador.builder()
                .usuario(usuario)
                .carrera(carrera)
                .fechaInicio(request.getFechaInicio())
                .fechaFin(request.getFechaFin())
                .activo(request.getActivo())
                .build();
        return convertToDTO(coordinadorRepository.save(coordinador));
    }

    @Override
    public CoordinadorDTO update(Integer id, CoordinadorRequest request) {
        Coordinador coordinador = coordinadorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Coordinador", "id", id));
        Usuario usuario = usuarioRepository.findById(request.getIdUsuario())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "id", request.getIdUsuario()));
        Carrera carrera = carreraRepository.findById(request.getIdCarrera())
                .orElseThrow(() -> new ResourceNotFoundException("Carrera", "id", request.getIdCarrera()));

        coordinador.setUsuario(usuario);
        coordinador.setCarrera(carrera);
        coordinador.setFechaInicio(request.getFechaInicio());
        coordinador.setFechaFin(request.getFechaFin());
        coordinador.setActivo(request.getActivo());
        return convertToDTO(coordinadorRepository.save(coordinador));
    }

    @Override
    public void delete(Integer id) {
        if (!coordinadorRepository.existsById(id)) {
            throw new ResourceNotFoundException("Coordinador", "id", id);
        }
        coordinadorRepository.deleteById(id);
    }

    private CoordinadorDTO convertToDTO(Coordinador coordinador) {
        return CoordinadorDTO.builder()
                .idCoordinador(coordinador.getIdCoordinador())
                .idUsuario(coordinador.getUsuario().getIdUsuario())
                .nombreCompleto(coordinador.getUsuario().getNombres() + " " + coordinador.getUsuario().getApellidos())
                .idCarrera(coordinador.getCarrera().getIdCarrera())
                .nombreCarrera(coordinador.getCarrera().getNombreCarrera())
                .fechaInicio(coordinador.getFechaInicio())
                .fechaFin(coordinador.getFechaFin())
                .activo(coordinador.getActivo())
                .build();
    }
}
