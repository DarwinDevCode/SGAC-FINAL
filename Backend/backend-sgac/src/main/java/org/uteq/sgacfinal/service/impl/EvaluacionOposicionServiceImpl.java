package org.uteq.sgacfinal.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.uteq.sgacfinal.dto.Request.AsignarComisionRequestDTO;
import org.uteq.sgacfinal.dto.Request.EvaluacionOposicionRequestDTO;
import org.uteq.sgacfinal.dto.Response.EvaluacionOposicionResponseDTO;
import org.uteq.sgacfinal.entity.EvaluacionOposicion;
import org.uteq.sgacfinal.entity.UsuarioComision;
import org.uteq.sgacfinal.repository.ComisionSeleccionRepository;
import org.uteq.sgacfinal.repository.EvaluacionOposicionRepository;
import org.uteq.sgacfinal.repository.UsuarioComisionRepository;
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
    private final UsuarioComisionRepository usuarioComisionRepository;
    private final ComisionSeleccionRepository comisionSeleccionRepository;

    @Override
    public EvaluacionOposicionResponseDTO crear(EvaluacionOposicionRequestDTO request) {
        Integer idGenerado = evaluacionOposicionRepository.registrarEvaluacionOposicion(
                request.getIdPostulacion(),
                request.getTemaExposicion(),
                request.getFechaEvaluacion(),
                request.getHoraInicio(),
                request.getHoraFin(),
                request.getLugar(),
                request.getEstado());

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
                request.getEstado());
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
                .orElseThrow(() -> new RuntimeException(
                        "No existe evaluación de oposición para la postulación ID: " + idPostulacion));
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

    @Override
    public EvaluacionOposicionResponseDTO asignarComisionAPostulacion(AsignarComisionRequestDTO request) {
        // 1. Verify commission exists
        comisionSeleccionRepository.findById(request.getIdComisionSeleccion())
                .orElseThrow(() -> new RuntimeException(
                        "Comisión no encontrada con ID: " + request.getIdComisionSeleccion()));

        // 2. Create or Update the EvaluacionOposicion record
        Integer idEvaluacion = null;
        var evaluacionExistente = evaluacionOposicionRepository.findAll().stream()
                .filter(ev -> ev.getPostulacion().getIdPostulacion().equals(request.getIdPostulacion()))
                .findFirst();

        if (evaluacionExistente.isPresent()) {
            idEvaluacion = evaluacionExistente.get().getIdEvaluacionOposicion();
            Integer resUpdate = evaluacionOposicionRepository.actualizarEvaluacionOposicion(
                    idEvaluacion,
                    request.getTemaExposicion(),
                    request.getFechaEvaluacion(),
                    request.getHoraInicio(),
                    request.getHoraFin(),
                    request.getLugar(),
                    "PROGRAMADA");
            if (resUpdate == null || resUpdate == -1) {
                throw new RuntimeException("Error al actualizar la evaluación de oposición existente.");
            }

            // Delete old commission members since we are re-assigning
            final Integer finalIdEvaluacion = idEvaluacion;
            List<UsuarioComision> oldMembers = usuarioComisionRepository.findAll().stream()
                    .filter(uc -> uc.getEvaluacionOposicion() != null
                            && uc.getEvaluacionOposicion().getIdEvaluacionOposicion().equals(finalIdEvaluacion))
                    .collect(Collectors.toList());
            for (UsuarioComision oldMember : oldMembers) {
                usuarioComisionRepository.desactivarUsuarioComision(oldMember.getIdUsuarioComision());
            }

        } else {
            idEvaluacion = evaluacionOposicionRepository.registrarEvaluacionOposicion(
                    request.getIdPostulacion(),
                    request.getTemaExposicion(),
                    request.getFechaEvaluacion(),
                    request.getHoraInicio(),
                    request.getHoraFin(),
                    request.getLugar(),
                    "PROGRAMADA");
        }

        if (idEvaluacion == null || idEvaluacion == -1) {
            throw new RuntimeException("Error al crear la evaluación de oposición.");
        }

        // 3. Fetch new commission members (Decano, Coordinador, Docente)
        List<UsuarioComision> miembros = usuarioComisionRepository
                .findByComisionSeleccion_IdComisionSeleccion(request.getIdComisionSeleccion());

        // 4. Link each member to this evaluation
        for (UsuarioComision miembro : miembros) {
            Integer resultado = usuarioComisionRepository.registrarUsuarioComision(
                    request.getIdComisionSeleccion(),
                    miembro.getUsuario().getIdUsuario(),
                    idEvaluacion,
                    miembro.getRolIntegrante(),
                    null, null, null,
                    request.getFechaEvaluacion());
            if (resultado == null || resultado == -1) {
                throw new RuntimeException(
                        "Error al asignar miembro " + miembro.getUsuario().getIdUsuario() + " a la evaluación.");
            }
        }

        return buscarPorId(idEvaluacion);
    }
}