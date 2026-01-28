package org.uteq.sgacfinal.service.impl;

import org.uteq.sgacfinal.dto.ConvocatoriaDTO;
import org.uteq.sgacfinal.dto.ConvocatoriaRequest;
import org.uteq.sgacfinal.entity.*;
import org.uteq.sgacfinal.exception.ResourceNotFoundException;
import org.uteq.sgacfinal.repository.*;
import org.uteq.sgacfinal.service.ConvocatoriaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ConvocatoriaServiceImpl implements ConvocatoriaService {

    private final ConvocatoriaRepository convocatoriaRepository;
    private final PeriodoAcademicoRepository periodoAcademicoRepository;
    private final AsignaturaRepository asignaturaRepository;
    private final DocenteRepository docenteRepository;
    private final PlazoActividadRepository plazoActividadRepository;

    @Override
    @Transactional(readOnly = true)
    public List<ConvocatoriaDTO> findAll() {
        return convocatoriaRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ConvocatoriaDTO> findByActivo(Boolean activo) {
        return convocatoriaRepository.findByActivo(activo).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ConvocatoriaDTO> findByPeriodo(Integer idPeriodoAcademico) {
        return convocatoriaRepository.findByPeriodoAcademicoIdPeriodoAcademico(idPeriodoAcademico).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ConvocatoriaDTO findById(Integer id) {
        Convocatoria convocatoria = convocatoriaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Convocatoria", "id", id));
        return convertToDTO(convocatoria);
    }

    @Override
    public ConvocatoriaDTO create(ConvocatoriaRequest request) {
        PeriodoAcademico periodo = periodoAcademicoRepository.findById(request.getIdPeriodoAcademico())
                .orElseThrow(() -> new ResourceNotFoundException("PeriodoAcademico", "id", request.getIdPeriodoAcademico()));
        Asignatura asignatura = asignaturaRepository.findById(request.getIdAsignatura())
                .orElseThrow(() -> new ResourceNotFoundException("Asignatura", "id", request.getIdAsignatura()));
        Docente docente = docenteRepository.findById(request.getIdDocente())
                .orElseThrow(() -> new ResourceNotFoundException("Docente", "id", request.getIdDocente()));

        Convocatoria convocatoria = Convocatoria.builder()
                .periodoAcademico(periodo)
                .asignatura(asignatura)
                .docente(docente)
                .cuposDisponibles(request.getCuposDisponibles())
                .fechaPublicacion(request.getFechaPublicacion())
                .fechaCierre(request.getFechaCierre())
                .estado(request.getEstado())
                .activo(request.getActivo())
                .build();

        if (request.getIdPlazoActividad() != null) {
            PlazoActividad plazoActividad = plazoActividadRepository.findById(request.getIdPlazoActividad())
                    .orElseThrow(() -> new ResourceNotFoundException("PlazoActividad", "id", request.getIdPlazoActividad()));
            convocatoria.setPlazoActividad(plazoActividad);
        }

        return convertToDTO(convocatoriaRepository.save(convocatoria));
    }

    @Override
    public ConvocatoriaDTO update(Integer id, ConvocatoriaRequest request) {
        Convocatoria convocatoria = convocatoriaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Convocatoria", "id", id));
        PeriodoAcademico periodo = periodoAcademicoRepository.findById(request.getIdPeriodoAcademico())
                .orElseThrow(() -> new ResourceNotFoundException("PeriodoAcademico", "id", request.getIdPeriodoAcademico()));
        Asignatura asignatura = asignaturaRepository.findById(request.getIdAsignatura())
                .orElseThrow(() -> new ResourceNotFoundException("Asignatura", "id", request.getIdAsignatura()));
        Docente docente = docenteRepository.findById(request.getIdDocente())
                .orElseThrow(() -> new ResourceNotFoundException("Docente", "id", request.getIdDocente()));

        convocatoria.setPeriodoAcademico(periodo);
        convocatoria.setAsignatura(asignatura);
        convocatoria.setDocente(docente);
        convocatoria.setCuposDisponibles(request.getCuposDisponibles());
        convocatoria.setFechaPublicacion(request.getFechaPublicacion());
        convocatoria.setFechaCierre(request.getFechaCierre());
        convocatoria.setEstado(request.getEstado());
        convocatoria.setActivo(request.getActivo());

        if (request.getIdPlazoActividad() != null) {
            PlazoActividad plazoActividad = plazoActividadRepository.findById(request.getIdPlazoActividad())
                    .orElseThrow(() -> new ResourceNotFoundException("PlazoActividad", "id", request.getIdPlazoActividad()));
            convocatoria.setPlazoActividad(plazoActividad);
        } else {
            convocatoria.setPlazoActividad(null);
        }

        return convertToDTO(convocatoriaRepository.save(convocatoria));
    }

    @Override
    public void delete(Integer id) {
        if (!convocatoriaRepository.existsById(id)) {
            throw new ResourceNotFoundException("Convocatoria", "id", id);
        }
        convocatoriaRepository.deleteById(id);
    }

    private ConvocatoriaDTO convertToDTO(Convocatoria convocatoria) {
        return ConvocatoriaDTO.builder()
                .idConvocatoria(convocatoria.getIdConvocatoria())
                .idPeriodoAcademico(convocatoria.getPeriodoAcademico().getIdPeriodoAcademico())
                .nombrePeriodo(convocatoria.getPeriodoAcademico().getNombrePeriodo())
                .idAsignatura(convocatoria.getAsignatura().getIdAsignatura())
                .nombreAsignatura(convocatoria.getAsignatura().getNombreAsignatura())
                .idDocente(convocatoria.getDocente().getIdDocente())
                .nombreDocente(convocatoria.getDocente().getUsuario().getNombres() + " " + convocatoria.getDocente().getUsuario().getApellidos())
                .cuposDisponibles(convocatoria.getCuposDisponibles())
                .fechaPublicacion(convocatoria.getFechaPublicacion())
                .fechaCierre(convocatoria.getFechaCierre())
                .estado(convocatoria.getEstado())
                .activo(convocatoria.getActivo())
                .build();
    }
}
