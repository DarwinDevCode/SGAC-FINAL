package org.uteq.sgacfinal.service.impl;

import org.uteq.sgacfinal.dto.PostulacionDTO;
import org.uteq.sgacfinal.dto.PostulacionRequest;
import org.uteq.sgacfinal.entity.*;
import org.uteq.sgacfinal.exception.ResourceNotFoundException;
import org.uteq.sgacfinal.repository.*;
import org.uteq.sgacfinal.service.PostulacionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class PostulacionServiceImpl implements PostulacionService {

    private final PostulacionRepository postulacionRepository;
    private final ConvocatoriaRepository convocatoriaRepository;
    private final EstudianteRepository estudianteRepository;
    private final PlazoActividadRepository plazoActividadRepository;

    @Override
    @Transactional(readOnly = true)
    public List<PostulacionDTO> findAll() {
        return postulacionRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PostulacionDTO> findByActivo(Boolean activo) {
        return postulacionRepository.findByActivo(activo).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PostulacionDTO> findByConvocatoria(Integer idConvocatoria) {
        return postulacionRepository.findByConvocatoriaIdConvocatoria(idConvocatoria).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PostulacionDTO> findByEstudiante(Integer idEstudiante) {
        return postulacionRepository.findByEstudianteIdEstudiante(idEstudiante).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public PostulacionDTO findById(Integer id) {
        Postulacion postulacion = postulacionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Postulacion", "id", id));
        return convertToDTO(postulacion);
    }

    @Override
    public PostulacionDTO create(PostulacionRequest request) {
        Convocatoria convocatoria = convocatoriaRepository.findById(request.getIdConvocatoria())
                .orElseThrow(() -> new ResourceNotFoundException("Convocatoria", "id", request.getIdConvocatoria()));
        Estudiante estudiante = estudianteRepository.findById(request.getIdEstudiante())
                .orElseThrow(() -> new ResourceNotFoundException("Estudiante", "id", request.getIdEstudiante()));

        Postulacion postulacion = Postulacion.builder()
                .convocatoria(convocatoria)
                .estudiante(estudiante)
                .fechaPostulacion(request.getFechaPostulacion())
                .estadoPostulacion(request.getEstadoPostulacion())
                .observaciones(request.getObservaciones())
                .activo(request.getActivo())
                .build();

        if (request.getIdPlazoActividad() != null) {
            PlazoActividad plazoActividad = plazoActividadRepository.findById(request.getIdPlazoActividad())
                    .orElseThrow(() -> new ResourceNotFoundException("PlazoActividad", "id", request.getIdPlazoActividad()));
            postulacion.setPlazoActividad(plazoActividad);
        }

        return convertToDTO(postulacionRepository.save(postulacion));
    }

    @Override
    public PostulacionDTO update(Integer id, PostulacionRequest request) {
        Postulacion postulacion = postulacionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Postulacion", "id", id));
        Convocatoria convocatoria = convocatoriaRepository.findById(request.getIdConvocatoria())
                .orElseThrow(() -> new ResourceNotFoundException("Convocatoria", "id", request.getIdConvocatoria()));
        Estudiante estudiante = estudianteRepository.findById(request.getIdEstudiante())
                .orElseThrow(() -> new ResourceNotFoundException("Estudiante", "id", request.getIdEstudiante()));

        postulacion.setConvocatoria(convocatoria);
        postulacion.setEstudiante(estudiante);
        postulacion.setFechaPostulacion(request.getFechaPostulacion());
        postulacion.setEstadoPostulacion(request.getEstadoPostulacion());
        postulacion.setObservaciones(request.getObservaciones());
        postulacion.setActivo(request.getActivo());

        if (request.getIdPlazoActividad() != null) {
            PlazoActividad plazoActividad = plazoActividadRepository.findById(request.getIdPlazoActividad())
                    .orElseThrow(() -> new ResourceNotFoundException("PlazoActividad", "id", request.getIdPlazoActividad()));
            postulacion.setPlazoActividad(plazoActividad);
        } else {
            postulacion.setPlazoActividad(null);
        }

        return convertToDTO(postulacionRepository.save(postulacion));
    }

    @Override
    public void delete(Integer id) {
        if (!postulacionRepository.existsById(id)) {
            throw new ResourceNotFoundException("Postulacion", "id", id);
        }
        postulacionRepository.deleteById(id);
    }

    private PostulacionDTO convertToDTO(Postulacion postulacion) {
        return PostulacionDTO.builder()
                .idPostulacion(postulacion.getIdPostulacion())
                .idConvocatoria(postulacion.getConvocatoria().getIdConvocatoria())
                .nombreAsignatura(postulacion.getConvocatoria().getAsignatura().getNombreAsignatura())
                .idEstudiante(postulacion.getEstudiante().getIdEstudiante())
                .nombreEstudiante(postulacion.getEstudiante().getUsuario().getNombres() + " " + postulacion.getEstudiante().getUsuario().getApellidos())
                .fechaPostulacion(postulacion.getFechaPostulacion())
                .estadoPostulacion(postulacion.getEstadoPostulacion())
                .observaciones(postulacion.getObservaciones())
                .activo(postulacion.getActivo())
                .build();
    }
}
