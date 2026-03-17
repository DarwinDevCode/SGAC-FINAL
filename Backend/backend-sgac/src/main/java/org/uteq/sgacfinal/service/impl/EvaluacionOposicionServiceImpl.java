package org.uteq.sgacfinal.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.uteq.sgacfinal.dto.Request.AsignarComisionRequestDTO;
import org.uteq.sgacfinal.dto.Request.EvaluacionOposicionRequestDTO;
import org.uteq.sgacfinal.dto.Response.EvaluacionOposicionResponseDTO;
import org.uteq.sgacfinal.entity.EvaluacionOposicion;
import org.uteq.sgacfinal.entity.UsuarioComision;
import org.uteq.sgacfinal.entity.Postulacion;
import org.uteq.sgacfinal.repository.ComisionSeleccionRepository;
import org.uteq.sgacfinal.repository.EvaluacionOposicionRepository;
import org.uteq.sgacfinal.repository.UsuarioComisionRepository;
import org.uteq.sgacfinal.repository.PostulacionRepository;
import org.uteq.sgacfinal.service.IEvaluacionOposicionService;
import org.uteq.sgacfinal.service.INotificacionService;
import org.uteq.sgacfinal.dto.Request.NotificationRequest;
import org.uteq.sgacfinal.entity.BancoTema;
import org.uteq.sgacfinal.repository.BancoTemaRepository;

import java.sql.Date;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class EvaluacionOposicionServiceImpl implements IEvaluacionOposicionService {

    private final EvaluacionOposicionRepository evaluacionOposicionRepository;
    private final UsuarioComisionRepository usuarioComisionRepository;
    private final ComisionSeleccionRepository comisionSeleccionRepository;
    private final PostulacionRepository postulacionRepository;
    private final INotificacionService notificacionService;
    private final BancoTemaRepository bancoTemaRepository;

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
                .orElseThrow(() -> new RuntimeException("Comisión no encontrada con ID: " + request.getIdComisionSeleccion()));

        // Validar citación con mínimo 7 días de anticipación
        if (request.getFechaEvaluacion().isBefore(java.time.LocalDate.now().plusDays(7))) {
            throw new RuntimeException("La citación debe realizarse con un mínimo de siete días de anticipación.");
        }

        // 2. Create the EvaluacionOposicion record via the stored procedure
        Integer idEvaluacion = evaluacionOposicionRepository.registrarEvaluacionOposicion(
                request.getIdPostulacion(),
                request.getTemaExposicion(),
                request.getFechaEvaluacion(),
                request.getHoraInicio(),
                request.getHoraFin(),
                request.getLugar(),
                "PROGRAMADA"
        );

        if (idEvaluacion == null || idEvaluacion == -1) {
            throw new RuntimeException("Error al crear la evaluación de oposición.");
        }

        // 3. Fetch commission members (Decano, Coordinador, Docente)
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
                    request.getFechaEvaluacion()
            );
            if (resultado == null || resultado == -1) {
                throw new RuntimeException("Error al asignar miembro " + miembro.getUsuario().getIdUsuario() + " a la evaluación.");
            }
            
            // Notificar al evaluador
            try {
                String rol = miembro.getRolIntegrante();
                notificacionService.enviarNotificacion(miembro.getUsuario().getIdUsuario(), NotificationRequest.builder()
                        .titulo("Asignación de Comisión Evaluadora")
                        .mensaje("Usted ha sido asignado como " + rol + " para la evaluación de oposición con tema: " 
                                + request.getTemaExposicion() + ". Fecha: " + request.getFechaEvaluacion() 
                                + ", Hora: " + request.getHoraInicio() + " - " + request.getHoraFin() + " en " + request.getLugar() + ".")
                        .tipo("SISTEMA").build());
            } catch (Exception e) {
                System.err.println("Error al notificar al miembro de comisión: " + e.getMessage());
            }
        }

        // 5. Notificar al estudiante
        try {
            Postulacion postulacion = postulacionRepository.findById(request.getIdPostulacion())
                    .orElseThrow(() -> new RuntimeException("Postulación no encontrada"));
            
            Integer idEstudiante = postulacion.getEstudiante().getUsuario().getIdUsuario();
            
            notificacionService.enviarNotificacion(idEstudiante, NotificationRequest.builder()
                    .titulo("Comisión de Evaluación Asignada")
                    .mensaje("Su postulación ha sido programada para evaluación de oposición y méritos. " +
                            "Tema: " + request.getTemaExposicion() + ". Fecha: " + request.getFechaEvaluacion() + 
                            ", Horario: " + request.getHoraInicio() + " - " + request.getHoraFin() + 
                            ", Lugar: " + request.getLugar() + ".")
                    .tipo("SISTEMA").build());
        } catch (Exception e) {
            System.err.println("Error al notificar al estudiante sobre la asignación de comisión: " + e.getMessage());
        }

        return buscarPorId(idEvaluacion);
    }

    @Override
    public EvaluacionOposicionResponseDTO sortearTema(Integer idEvaluacionOposicion) {
        EvaluacionOposicion evaluacion = evaluacionOposicionRepository.findById(idEvaluacionOposicion)
                .orElseThrow(() -> new RuntimeException("Evaluación de oposición no encontrada con ID: " + idEvaluacionOposicion));

        // Obtener temas disponibles para la convocatoria
        List<BancoTema> temas = bancoTemaRepository.findByIdConvocatoriaIdConvocatoriaAndActivoTrue(
                evaluacion.getPostulacion().getConvocatoria().getIdConvocatoria());
        
        if (temas.isEmpty()) {
            throw new RuntimeException("No hay temas disponibles en el banco para esta convocatoria.");
        }

        // Sortear tema aleatorio
        Collections.shuffle(temas);
        BancoTema temaSorteado = temas.get(0);

        // Actualizar evaluación
        evaluacion.setTemaSorteado(temaSorteado.getDescripcionTema());
        evaluacion.setTimestampSorteo(LocalDateTime.now());
        evaluacion.setTemaExposicion(temaSorteado.getDescripcionTema());

        EvaluacionOposicion guardada = evaluacionOposicionRepository.save(evaluacion);
        return mapearADTO(guardada);
    }
}