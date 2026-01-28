package com.sgac.service.impl;

import com.sgac.dto.DocenteDTO;
import com.sgac.dto.DocenteRequest;
import com.sgac.entity.Docente;
import com.sgac.entity.Usuario;
import com.sgac.exception.ResourceNotFoundException;
import com.sgac.repository.DocenteRepository;
import com.sgac.repository.UsuarioRepository;
import com.sgac.service.DocenteService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class DocenteServiceImpl implements DocenteService {

    private final DocenteRepository docenteRepository;
    private final UsuarioRepository usuarioRepository;

    @Override
    @Transactional(readOnly = true)
    public List<DocenteDTO> findAll() {
        return docenteRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<DocenteDTO> findByActivo(Boolean activo) {
        return docenteRepository.findByActivo(activo).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public DocenteDTO findById(Integer id) {
        Docente docente = docenteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Docente", "id", id));
        return convertToDTO(docente);
    }

    @Override
    public DocenteDTO create(DocenteRequest request) {
        Usuario usuario = usuarioRepository.findById(request.getIdUsuario())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "id", request.getIdUsuario()));

        Docente docente = Docente.builder()
                .usuario(usuario)
                .fechaInicio(request.getFechaInicio())
                .fechaFin(request.getFechaFin())
                .activo(request.getActivo())
                .build();
        return convertToDTO(docenteRepository.save(docente));
    }

    @Override
    public DocenteDTO update(Integer id, DocenteRequest request) {
        Docente docente = docenteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Docente", "id", id));
        Usuario usuario = usuarioRepository.findById(request.getIdUsuario())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "id", request.getIdUsuario()));

        docente.setUsuario(usuario);
        docente.setFechaInicio(request.getFechaInicio());
        docente.setFechaFin(request.getFechaFin());
        docente.setActivo(request.getActivo());
        return convertToDTO(docenteRepository.save(docente));
    }

    @Override
    public void delete(Integer id) {
        if (!docenteRepository.existsById(id)) {
            throw new ResourceNotFoundException("Docente", "id", id);
        }
        docenteRepository.deleteById(id);
    }

    private DocenteDTO convertToDTO(Docente docente) {
        return DocenteDTO.builder()
                .idDocente(docente.getIdDocente())
                .idUsuario(docente.getUsuario().getIdUsuario())
                .nombreCompleto(docente.getUsuario().getNombres() + " " + docente.getUsuario().getApellidos())
                .correo(docente.getUsuario().getCorreo())
                .fechaInicio(docente.getFechaInicio())
                .fechaFin(docente.getFechaFin())
                .activo(docente.getActivo())
                .build();
    }
}
