package org.uteq.sgacfinal.service.impl;

import org.uteq.sgacfinal.dto.DecanoDTO;
import org.uteq.sgacfinal.dto.DecanoRequest;
import org.uteq.sgacfinal.entity.Decano;
import org.uteq.sgacfinal.entity.Facultad;
import org.uteq.sgacfinal.entity.Usuario;
import org.uteq.sgacfinal.exception.ResourceNotFoundException;
import org.uteq.sgacfinal.repository.DecanoRepository;
import org.uteq.sgacfinal.repository.FacultadRepository;
import org.uteq.sgacfinal.repository.UsuarioRepository;
import org.uteq.sgacfinal.service.DecanoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class DecanoServiceImpl implements DecanoService {

    private final DecanoRepository decanoRepository;
    private final UsuarioRepository usuarioRepository;
    private final FacultadRepository facultadRepository;

    @Override
    @Transactional(readOnly = true)
    public List<DecanoDTO> findAll() {
        return decanoRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<DecanoDTO> findByActivo(Boolean activo) {
        return decanoRepository.findByActivo(activo).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public DecanoDTO findById(Integer id) {
        Decano decano = decanoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Decano", "id", id));
        return convertToDTO(decano);
    }

    @Override
    public DecanoDTO create(DecanoRequest request) {
        Usuario usuario = usuarioRepository.findById(request.getIdUsuario())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "id", request.getIdUsuario()));
        Facultad facultad = facultadRepository.findById(request.getIdFacultad())
                .orElseThrow(() -> new ResourceNotFoundException("Facultad", "id", request.getIdFacultad()));

        Decano decano = Decano.builder()
                .usuario(usuario)
                .facultad(facultad)
                .fechaInicioGestion(request.getFechaInicioGestion())
                .fechaFinGestion(request.getFechaFinGestion())
                .activo(request.getActivo())
                .build();
        return convertToDTO(decanoRepository.save(decano));
    }

    @Override
    public DecanoDTO update(Integer id, DecanoRequest request) {
        Decano decano = decanoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Decano", "id", id));
        Usuario usuario = usuarioRepository.findById(request.getIdUsuario())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "id", request.getIdUsuario()));
        Facultad facultad = facultadRepository.findById(request.getIdFacultad())
                .orElseThrow(() -> new ResourceNotFoundException("Facultad", "id", request.getIdFacultad()));

        decano.setUsuario(usuario);
        decano.setFacultad(facultad);
        decano.setFechaInicioGestion(request.getFechaInicioGestion());
        decano.setFechaFinGestion(request.getFechaFinGestion());
        decano.setActivo(request.getActivo());
        return convertToDTO(decanoRepository.save(decano));
    }

    @Override
    public void delete(Integer id) {
        if (!decanoRepository.existsById(id)) {
            throw new ResourceNotFoundException("Decano", "id", id);
        }
        decanoRepository.deleteById(id);
    }

    private DecanoDTO convertToDTO(Decano decano) {
        return DecanoDTO.builder()
                .idDecano(decano.getIdDecano())
                .idUsuario(decano.getUsuario().getIdUsuario())
                .nombreCompleto(decano.getUsuario().getNombres() + " " + decano.getUsuario().getApellidos())
                .idFacultad(decano.getFacultad().getIdFacultad())
                .nombreFacultad(decano.getFacultad().getNombreFacultad())
                .fechaInicioGestion(decano.getFechaInicioGestion())
                .fechaFinGestion(decano.getFechaFinGestion())
                .activo(decano.getActivo())
                .build();
    }
}
