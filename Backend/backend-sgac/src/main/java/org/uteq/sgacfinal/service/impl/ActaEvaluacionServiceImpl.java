package org.uteq.sgacfinal.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.uteq.sgacfinal.dto.Request.ConfirmarActaRequestDTO;
import org.uteq.sgacfinal.dto.Request.GenerarActaRequestDTO;
import org.uteq.sgacfinal.dto.Response.ActaEvaluacionResponseDTO;
import org.uteq.sgacfinal.entity.ActaEvaluacion;
import org.uteq.sgacfinal.entity.Postulacion;
import org.uteq.sgacfinal.repository.ActaEvaluacionRepository;
import org.uteq.sgacfinal.repository.PostulacionRepository;
import org.uteq.sgacfinal.service.IActaEvaluacionService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ActaEvaluacionServiceImpl implements IActaEvaluacionService {

    private final ActaEvaluacionRepository actaRepo;
    private final PostulacionRepository postulacionRepo;

    @Override
    public ActaEvaluacionResponseDTO generarActa(GenerarActaRequestDTO request) {
        Postulacion postulacion = postulacionRepo.findById(request.getIdPostulacion())
                .orElseThrow(() -> new RuntimeException("Postulación no encontrada: " + request.getIdPostulacion()));

        // Generar URL real despues 
        String urlDocumento = "/api/actas/download/" + request.getTipoActa().toLowerCase()
                + "/" + request.getIdPostulacion() + "/" + System.currentTimeMillis() + ".pdf";

        Integer resultado;
        var existente = actaRepo.findByIdPostulacionAndTipoActa(request.getIdPostulacion(), request.getTipoActa());
        if (existente.isPresent()) {
            resultado = actaRepo.actualizarActa(existente.get().getIdActa(), "PENDIENTE", urlDocumento);
        } else {
            resultado = actaRepo.crearActa(request.getIdPostulacion(), request.getTipoActa(), "PENDIENTE", urlDocumento);
        }

        if (resultado == null || resultado == -1) {
            throw new RuntimeException("Error al guardar o actualizar el acta de evaluación.");
        }

        ActaEvaluacion saved = actaRepo.findById(resultado)
                .orElseThrow(() -> new RuntimeException("No se encontró el acta creada/actualizada"));
        return mapToDTO(saved);
    }

    @Override
    public void eliminar(Integer idActa) {
        Integer res = actaRepo.eliminarActa(idActa);
        if (res == -1 || res == 0) {
            throw new RuntimeException("Error al eliminar el acta de evaluación o no existe.");
        }
    }

    @Override
    public ActaEvaluacionResponseDTO confirmarActa(ConfirmarActaRequestDTO request) {
        actaRepo.findById(request.getIdActa())
                .orElseThrow(() -> new RuntimeException("Acta no encontrada: " + request.getIdActa()));

        // El SP actualiza la confirmación y cambia estado a CONFIRMADO si los 3 confirmaron
        Integer resultado = actaRepo.confirmarActa(request.getIdActa(), request.getIdEvaluador(), request.getRolEvaluador());

        if (resultado == null) {
            throw new RuntimeException("Error al confirmar el acta.");
        }

        ActaEvaluacion updated = actaRepo.findById(request.getIdActa())
                .orElseThrow(() -> new RuntimeException("Acta no encontrada después de confirmar."));

        return mapToDTO(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ActaEvaluacionResponseDTO> listarPorPostulacion(Integer idPostulacion) {
        return actaRepo.findByIdPostulacion(idPostulacion)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    private ActaEvaluacionResponseDTO mapToDTO(ActaEvaluacion e) {
        return ActaEvaluacionResponseDTO.builder()
                .idActa(e.getIdActa())
                .idPostulacion(e.getPostulacion().getIdPostulacion())
                .tipoActa(e.getTipoActa())
                .urlDocumento(e.getUrlDocumento())
                .fechaGeneracion(e.getFechaGeneracion())
                .confirmadoDecano(e.getConfirmadoDecano())
                .confirmadoCoordinador(e.getConfirmadoCoordinador())
                .confirmadoDocente(e.getConfirmadoDocente())
                .estado(e.getEstado())
                .build();
    }
}
