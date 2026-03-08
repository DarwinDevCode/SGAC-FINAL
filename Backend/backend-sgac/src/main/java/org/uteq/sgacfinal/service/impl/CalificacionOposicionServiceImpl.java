package org.uteq.sgacfinal.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.uteq.sgacfinal.dto.Request.CalificacionOposicionRequestDTO;
import org.uteq.sgacfinal.dto.Request.NotificationRequest;
import org.uteq.sgacfinal.dto.Response.CalificacionOposicionResponseDTO;
import org.uteq.sgacfinal.dto.Response.OposicionEstadoResponseDTO;
import org.uteq.sgacfinal.dto.Response.RankingEvaluacionDTO;
import org.uteq.sgacfinal.entity.CalificacionOposicionIndividual;
import org.uteq.sgacfinal.entity.Postulacion;
import org.uteq.sgacfinal.entity.SorteoOposicion;
import org.uteq.sgacfinal.repository.CalificacionOposicionIndividualRepository;
import org.uteq.sgacfinal.repository.PostulacionRepository;
import org.uteq.sgacfinal.repository.ResumenEvaluacionRepository;
import org.uteq.sgacfinal.repository.SorteoOposicionRepository;
import org.uteq.sgacfinal.service.ICalificacionOposicionService;
import org.uteq.sgacfinal.service.INotificacionService;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CalificacionOposicionServiceImpl implements ICalificacionOposicionService {

    private final CalificacionOposicionIndividualRepository oposicionRepo;
    private final ResumenEvaluacionRepository resumenRepo;
    private final SorteoOposicionRepository sorteoRepo;
    private final PostulacionRepository postulacionRepo;
    private final INotificacionService notificacionService;

    @Override
    public CalificacionOposicionResponseDTO guardarNota(CalificacionOposicionRequestDTO req) {
        // Validar subtotal ≤ 20
        BigDecimal subtotal = req.getCriterioMaterial()
                .add(req.getCriterioCalidad())
                .add(req.getCriterioPertinencia());
        if (subtotal.compareTo(BigDecimal.valueOf(20)) > 0) {
            throw new IllegalArgumentException("El subtotal de oposición no puede superar 20 puntos. Total calculado: " + subtotal);
        }

        Integer resultado;
        var existente = oposicionRepo.findByIdPostulacionAndIdEvaluador(req.getIdPostulacion(), req.getIdEvaluador());

        if (existente.isPresent()) {
            resultado = oposicionRepo.actualizarOposicionIndividual(
                    existente.get().getIdCalificacion(),
                    req.getCriterioMaterial(),
                    req.getCriterioCalidad(),
                    req.getCriterioPertinencia()
            );
        } else {
            resultado = oposicionRepo.guardarOposicionIndividual(
                    req.getIdPostulacion(),
                    req.getIdEvaluador(),
                    req.getRolEvaluador(),
                    req.getCriterioMaterial(),
                    req.getCriterioCalidad(),
                    req.getCriterioPertinencia()
            );
        }

        if (resultado == null || resultado == -1) {
            throw new RuntimeException("Error al guardar o actualizar la calificación de oposición.");
        }

        // Si los 3 ya calificaron, notificar al postulante
        Long count = oposicionRepo.countByIdPostulacion(req.getIdPostulacion());
        if (count >= 3) {
            notificarResumenOposicion(req.getIdPostulacion());
        }

        // Retornar la calificación guardada
        CalificacionOposicionIndividual entidad = oposicionRepo
                .findByIdPostulacionAndIdEvaluador(req.getIdPostulacion(), req.getIdEvaluador())
                .orElseThrow(() -> new RuntimeException("Calificación no encontrada después de guardar."));
        return mapToDTO(entidad);
    }

    @Override
    public void eliminar(Integer id) {
        Integer res = oposicionRepo.eliminarOposicionIndividual(id);
        if (res == -1 || res == 0) {
            throw new RuntimeException("Error al eliminar la calificación de oposición individual o no existe.");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public OposicionEstadoResponseDTO obtenerEstado(Integer idPostulacion) {
        List<CalificacionOposicionIndividual> calificaciones = oposicionRepo.findByIdPostulacion(idPostulacion);

        boolean decanoCalif      = calificaciones.stream().anyMatch(c -> "DECANO".equals(c.getRolEvaluador()));
        boolean coordinadorCalif = calificaciones.stream().anyMatch(c -> "COORDINADOR".equals(c.getRolEvaluador()));
        boolean docenteCalif     = calificaciones.stream().anyMatch(c -> "DOCENTE".equals(c.getRolEvaluador()));
        boolean todosCalif       = decanoCalif && coordinadorCalif && docenteCalif;

        BigDecimal promedio = null;
        if (todosCalif) {
            promedio = calificaciones.stream()
                    .map(c -> c.getSubtotal() != null ? c.getSubtotal() :
                            c.getCriterioMaterial().add(c.getCriterioCalidad()).add(c.getCriterioPertinencia()))
                    .reduce(BigDecimal.ZERO, BigDecimal::add)
                    .divide(BigDecimal.valueOf(3), 2, RoundingMode.HALF_UP);
        }

        String temaSorteado = sorteoRepo.findByIdPostulacion(idPostulacion)
                .map(SorteoOposicion::getTemaSorteado)
                .orElse("Pendiente de sorteo");

        return OposicionEstadoResponseDTO.builder()
                .idPostulacion(idPostulacion)
                .temaSorteado(temaSorteado)
                .decanoCalificado(decanoCalif)
                .coordinadorCalificado(coordinadorCalif)
                .docenteCalificado(docenteCalif)
                .todosCalificados(todosCalif)
                .promedioOposicion(todosCalif ? promedio : null)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public CalificacionOposicionResponseDTO obtenerCalificacion(Integer idPostulacion, Integer idEvaluador) {
        CalificacionOposicionIndividual entidad = oposicionRepo
                .findByIdPostulacionAndIdEvaluador(idPostulacion, idEvaluador)
                .orElseThrow(() -> new RuntimeException("No existe calificación para este evaluador y postulación."));
        return mapToDTO(entidad);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RankingEvaluacionDTO> obtenerRankingConvocatoria(Integer idConvocatoria) {
        List<Object[]> rows = resumenRepo.calcularRankingConvocatoria(idConvocatoria);
        List<RankingEvaluacionDTO> result = new ArrayList<>();

        for (Object[] row : rows) {
            result.add(RankingEvaluacionDTO.builder()
                    .idPostulacion(row[0] != null ? ((Number) row[0]).intValue() : null)
                    .nombreEstudiante(row[1] != null ? row[1].toString() : "")
                    .matricula(row[2] != null ? row[2].toString() : "")
                    .totalMeritos(row[3] != null ? new BigDecimal(row[3].toString()) : BigDecimal.ZERO)
                    .promedioOposicion(row[4] != null ? new BigDecimal(row[4].toString()) : BigDecimal.ZERO)
                    .totalFinal(row[5] != null ? new BigDecimal(row[5].toString()) : BigDecimal.ZERO)
                    .matSum(row[6] != null ? new BigDecimal(row[6].toString()) : BigDecimal.ZERO)
                    .pertinenciaSum(row[7] != null ? new BigDecimal(row[7].toString()) : BigDecimal.ZERO)
                    .estado(row[8] != null ? row[8].toString() : "PENDIENTE")
                    .posicion(row[9] != null ? ((Number) row[9]).longValue() : null)
                    .empate(false) // Se calcula abajo
                    .build());
        }

        // Marcar empates: postulantes con el mismo totalFinal
        for (int i = 0; i < result.size() - 1; i++) {
            BigDecimal current = result.get(i).getTotalFinal();
            BigDecimal next = result.get(i + 1).getTotalFinal();
            if (current != null && current.compareTo(next) == 0) {
                result.get(i).setEmpate(true);
                result.get(i + 1).setEmpate(true);
            }
        }

        return result;
    }

    private void notificarResumenOposicion(Integer idPostulacion) {
        try {
            Postulacion postulacion = postulacionRepo.findById(idPostulacion).orElse(null);
            if (postulacion == null) return;

            Integer idUsuario = postulacion.getEstudiante().getUsuario().getIdUsuario();
            BigDecimal totalFinal = resumenRepo.findByIdPostulacion(idPostulacion)
                    .map(re -> re.getTotalFinal())
                    .orElse(null);

            String mensaje;
            if (totalFinal != null) {
                boolean aprobado = totalFinal.compareTo(BigDecimal.valueOf(25)) >= 0;
                mensaje = aprobado
                        ? "Tu evaluación final es de " + totalFinal + " puntos. ¡Felicitaciones, has alcanzado el puntaje mínimo requerido!"
                        : "Tu evaluación final es de " + totalFinal + " puntos. Lamentablemente no alcanzaste el puntaje mínimo de 25 puntos.";
            } else {
                mensaje = "La evaluación de oposición ha sido completada por todos los miembros del tribunal.";
            }

            notificacionService.enviarNotificacion(idUsuario, NotificationRequest.builder()
                    .titulo("Resultado de Evaluación de Oposición")
                    .mensaje(mensaje)
                    .tipo("EVALUACION")
                    .idReferencia(idPostulacion)
                    .build());
        } catch (Exception e) {
            // No fallar el flujo principal por un error de notificación
            System.err.println("Error enviando notificación de oposición: " + e.getMessage());
        }
    }

    private CalificacionOposicionResponseDTO mapToDTO(CalificacionOposicionIndividual e) {
        return CalificacionOposicionResponseDTO.builder()
                .idCalificacion(e.getIdCalificacion())
                .idPostulacion(e.getPostulacion().getIdPostulacion())
                .idEvaluador(e.getIdEvaluador())
                .rolEvaluador(e.getRolEvaluador())
                .criterioMaterial(e.getCriterioMaterial())
                .criterioCalidad(e.getCriterioCalidad())
                .criterioPertinencia(e.getCriterioPertinencia())
                .subtotal(e.getSubtotal() != null ? e.getSubtotal() :
                        e.getCriterioMaterial().add(e.getCriterioCalidad()).add(e.getCriterioPertinencia()))
                .fechaRegistro(e.getFechaRegistro())
                .build();
    }
}
