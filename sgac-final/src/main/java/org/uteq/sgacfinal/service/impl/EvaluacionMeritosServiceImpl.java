package org.uteq.sgacfinal.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.uteq.sgacfinal.dto.Request.EvaluacionMeritosRequestDTO;
import org.uteq.sgacfinal.dto.Response.EvaluacionMeritosResponseDTO;
import org.uteq.sgacfinal.entity.EvaluacionMeritos;
import org.uteq.sgacfinal.repository.EvaluacionMeritosRepository;
import org.uteq.sgacfinal.service.IEvaluacionMeritosService;

@Service
@RequiredArgsConstructor
@Transactional
public class EvaluacionMeritosServiceImpl implements IEvaluacionMeritosService {

    private final EvaluacionMeritosRepository evaluacionMeritosRepository;

    @Override
    public EvaluacionMeritosResponseDTO crear(EvaluacionMeritosRequestDTO request) {
        Integer idGenerado = evaluacionMeritosRepository.registrarEvaluacionMeritos(
                request.getIdPostulacion(),
                request.getNotaAsignatura(),
                request.getNotaSemestres(),
                request.getNotaEventos(),
                request.getNotaExperiencia(),
                request.getFechaEvaluacion()
        );

        if (idGenerado == -1) {
            throw new RuntimeException("Error al registrar evaluación de méritos.");
        }

        return buscarPorId(idGenerado);
    }

    @Override
    public EvaluacionMeritosResponseDTO actualizar(Integer id, EvaluacionMeritosRequestDTO request) {
        Integer resultado = evaluacionMeritosRepository.actualizarEvaluacionMeritos(
                id,
                request.getNotaAsignatura(),
                request.getNotaSemestres(),
                request.getNotaEventos(),
                request.getNotaExperiencia()
        );

        if (resultado == -1) {
            throw new RuntimeException("Error al actualizar evaluación de méritos.");
        }

        return buscarPorId(id);
    }

    @Override
    @Transactional(readOnly = true)
    public EvaluacionMeritosResponseDTO buscarPorId(Integer id) {
        EvaluacionMeritos evaluacion = evaluacionMeritosRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Evaluación de méritos no encontrada con ID: " + id));
        return mapearADTO(evaluacion);
    }

    @Override
    @Transactional(readOnly = true)
    public EvaluacionMeritosResponseDTO buscarPorPostulacion(Integer idPostulacion) {
        EvaluacionMeritos evaluacion = evaluacionMeritosRepository.obtenerPorPostulacionSP(idPostulacion)
                .orElseThrow(() -> new RuntimeException("No existe evaluación de méritos para la postulación ID: " + idPostulacion));
        return mapearADTO(evaluacion);
    }

    private EvaluacionMeritosResponseDTO mapearADTO(EvaluacionMeritos entidad) {
        return EvaluacionMeritosResponseDTO.builder()
                .idEvaluacionMeritos(entidad.getIdEvaluacionMeritos())
                .idPostulacion(entidad.getPostulacion().getIdPostulacion())
                .notaAsignatura(entidad.getNotaAsignatura())
                .notaSemestres(entidad.getNotaSemestres())
                .notaEventos(entidad.getNotaEventos())
                .notaExperiencia(entidad.getNotaExperiencia())
                .fechaEvaluacion(entidad.getFechaEvaluacion())
                .build();
    }
}