package org.uteq.sgacfinal.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.uteq.sgacfinal.dto.Request.EvaluacionOposicionRequestDTO;
import org.uteq.sgacfinal.dto.Response.EvaluacionOposicionResponseDTO;
import org.uteq.sgacfinal.entity.EvaluacionOposicion;
import org.uteq.sgacfinal.repository.EvaluacionOposicionRepository;
import org.uteq.sgacfinal.service.IEvaluacionOposicionService;

import java.sql.Date;
import java.sql.Time;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class EvaluacionOposicionServiceImpl implements IEvaluacionOposicionService {

    private final EvaluacionOposicionRepository evaluacionOposicionRepository;

    @Override
    public EvaluacionOposicionResponseDTO crear(EvaluacionOposicionRequestDTO request) {
        Integer idGenerado = evaluacionOposicionRepository.registrarEvaluacionOposicion(
                request.getIdPostulacion(),
                request.getTemaExposicion(),
                request.getFechaEvaluacion(),
                request.getHoraInicio(),
                request.getHoraFin(),
                request.getLugar(),
                request.getEstado()
        );

        if (idGenerado == -1) {
            throw new RuntimeException("Error al registrar evaluación de oposición.");
        }

        return buscarPorId(idGenerado);
    }

    @Override
    public EvaluacionOposicionResponseDTO actualizar(Integer id, EvaluacionOposicionRequestDTO request) {
        Integer resultado = evaluacionOposicionRepository.actualizarEvaluacionOposicion(
                id,
                request.getTemaExposicion(),
                request.getFechaEvaluacion(),
                request.getHoraInicio(),
                request.getHoraFin(),
                request.getLugar(),
                request.getEstado()
        );
        if (resultado == -1) {
            throw new RuntimeException("Error al actualizar evaluación de oposición.");
        }

        return buscarPorId(id);
    }

    @Override
    @Transactional(readOnly = true)
    public EvaluacionOposicionResponseDTO buscarPorId(Integer id) {
        EvaluacionOposicion evaluacion = evaluacionOposicionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Evaluación de oposición no encontrada con ID: " + id));
        return mapearADTO(evaluacion);
    }

    @Override
    @Transactional(readOnly = true)
    public EvaluacionOposicionResponseDTO buscarPorPostulacion(Integer idPostulacion) {
        return evaluacionOposicionRepository.findAll().stream()
                .filter(ev -> ev.getPostulacion().getIdPostulacion().equals(idPostulacion))
                .findFirst()
                .map(this::mapearADTO)
                .orElseThrow(() -> new RuntimeException("No existe evaluación de oposición para la postulación ID: " + idPostulacion));
    }

    @Override
    @Transactional(readOnly = true)
    public List<EvaluacionOposicionResponseDTO> listarTodas() {
        List<Object[]> resultados = evaluacionOposicionRepository.listarEvaluacionesOposicionSP();
        return resultados.stream()
                .map(this::mapearDesdeObjectArray)
                .collect(Collectors.toList());
    }

    private EvaluacionOposicionResponseDTO mapearADTO(EvaluacionOposicion entidad) {
        return EvaluacionOposicionResponseDTO.builder()
                .idEvaluacionOposicion(entidad.getIdEvaluacionOposicion())
                .idPostulacion(entidad.getPostulacion().getIdPostulacion())
                .temaExposicion(entidad.getTemaExposicion())
                .fechaEvaluacion(entidad.getFechaEvaluacion())
                .horaInicio(entidad.getHoraInicio())
                .horaFin(entidad.getHoraFin())
                .lugar(entidad.getLugar())
                .estado(entidad.getEstado())
                .build();
    }

    private EvaluacionOposicionResponseDTO mapearDesdeObjectArray(Object[] obj) {
        return EvaluacionOposicionResponseDTO.builder()
                .idEvaluacionOposicion((Integer) obj[0])
                .temaExposicion((String) obj[1])
                .fechaEvaluacion(obj[2] != null ? ((Date) obj[2]).toLocalDate() : null)
                .estado((String) obj[3])
                .build();
    }
}