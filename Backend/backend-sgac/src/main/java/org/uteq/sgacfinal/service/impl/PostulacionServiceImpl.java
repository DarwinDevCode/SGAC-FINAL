package org.uteq.sgacfinal.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.uteq.sgacfinal.dto.Request.PostulacionRequestDTO;
import org.uteq.sgacfinal.dto.Response.PostulacionResponseDTO;
import org.uteq.sgacfinal.entity.Estudiante;
import org.uteq.sgacfinal.entity.Postulacion;
import org.uteq.sgacfinal.repository.EstudianteRepository;
import org.uteq.sgacfinal.repository.PostulacionRepository;
import org.uteq.sgacfinal.service.INotificacionService;
import org.uteq.sgacfinal.service.IPostulacionService;


import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class PostulacionServiceImpl implements IPostulacionService {

    private final PostulacionRepository postulacionRepository;
    private final EstudianteRepository estudianteRepository;
    private final INotificacionService notificacionService;

    @Override
    public PostulacionResponseDTO crear(PostulacionRequestDTO request) {

        return null;
    }

    @Override
    public PostulacionResponseDTO actualizar(Integer id, PostulacionRequestDTO request) {

        return null;
    }

    @Override
    public void desactivar(Integer id) {
        Integer resultado = postulacionRepository.desactivarPostulacion(id);
        if (resultado == -1) {
            throw new RuntimeException("Error al anular la postulación.");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public PostulacionResponseDTO buscarPorId(Integer id) {
        Postulacion postulacion = postulacionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Postulación no encontrada con ID: " + id));
        return mapearADTO(postulacion);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PostulacionResponseDTO> listarPorEstudiante(Integer idUsuario) {
        Optional<Estudiante> estudianteOpt = estudianteRepository.findByUsuario_IdUsuario(idUsuario);
        if (estudianteOpt.isEmpty()) {
            return List.of();
        }

        List<Object[]> resultados = postulacionRepository.listarPostulacionesPorEstudianteSP(estudianteOpt.get().getIdEstudiante());

        return resultados.stream()
                .map(this::mapearDesdeObjectArray)
                .collect(Collectors.toList());
    }



    @Override
    @Transactional(readOnly = true)
    public List<PostulacionResponseDTO> listarPorConvocatoria(Integer idConvocatoria) {
        return postulacionRepository.findByConvocatoria_IdConvocatoria(idConvocatoria).stream()
                .map(this::mapearADTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PostulacionResponseDTO> listarPorCarrera(Integer idCarrera) {
        return postulacionRepository.findByCarrera(idCarrera).stream()
                .map(this::mapearADTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PostulacionResponseDTO> listarPendientesPorCarrera(Integer idCarrera) {
        return postulacionRepository.findByEstadoAndCarrera("PENDIENTE", idCarrera).stream()
                .map(this::mapearADTO)
                .collect(Collectors.toList());
    }

    @Override
    public void actualizarEstado(Integer idPostulacion, String nuevoEstado, String observacion) {
        Postulacion postulacion = postulacionRepository.findById(idPostulacion)
                .orElseThrow(() -> new RuntimeException("Postulación no encontrada"));

        postulacion.setEstadoPostulacion(nuevoEstado);
        postulacion.setObservaciones(observacion);
        
        try {
            System.out.println("Intentando actualizar postulación ID: " + idPostulacion + " a estado: " + nuevoEstado);
            Integer resultado = postulacionRepository.actualizarPostulacion(idPostulacion, nuevoEstado, observacion);
            System.out.println("Resultado de sp_actualizar_postulacion: " + resultado);
            
            if (resultado == null || resultado == -1) {
                throw new RuntimeException("El SP actualizarPostulacion devolvió " + resultado);
            }
        } catch (Exception ex) {
            System.err.println("Error al ejecutar actualizarPostulacion: " + ex.getMessage());
            ex.printStackTrace();
            throw new RuntimeException("Error al actualizar el estado de la postulación en la base de datos: " + ex.getMessage(), ex);
        }

        if (postulacion.getEstudiante() != null && postulacion.getEstudiante().getUsuario() != null) {
            String mensaje = "Tu postulación ha cambiado de estado a: " + nuevoEstado;
            notificacionService.enviarNotificacion(
                    postulacion.getEstudiante().getUsuario().getIdUsuario(),
                    mensaje,
                    "ESTADO_POSTULACION"
            );
        }
    }

    private PostulacionResponseDTO mapearADTO(Postulacion entidad) {
        String nombreEstudiante = "";
        String matricula = "";
        if (entidad.getEstudiante() != null && entidad.getEstudiante().getUsuario() != null) {
            nombreEstudiante = entidad.getEstudiante().getUsuario().getNombres() + " " +
                    entidad.getEstudiante().getUsuario().getApellidos();
            matricula = entidad.getEstudiante().getMatricula();
        }

        return PostulacionResponseDTO.builder()
                .idPostulacion(entidad.getIdPostulacion())
                .idConvocatoria(entidad.getConvocatoria().getIdConvocatoria())
                .asignaturaConvocatoria(entidad.getConvocatoria().getAsignatura().getNombreAsignatura())
                .idEstudiante(entidad.getEstudiante().getIdEstudiante())
                .nombreCompletoEstudiante(nombreEstudiante)
                .matriculaEstudiante(matricula)
                .fechaPostulacion(entidad.getFechaPostulacion())
                .estadoPostulacion(entidad.getEstadoPostulacion())
                .observaciones(entidad.getObservaciones())
                .activo(entidad.getActivo())
                .build();
    }

    private PostulacionResponseDTO mapearDesdeObjectArray(Object[] obj) {
        return PostulacionResponseDTO.builder()
                .idPostulacion((Integer) obj[0])
                .idConvocatoria((Integer) obj[1])
                .estadoPostulacion((String) obj[3])
                .build();
    }



    @Override
    @Transactional
    public String registrarPostulacionCompleta(PostulacionRequestDTO request, List<MultipartFile> archivos, List<Integer> tiposRequisito) {
        try {
            Estudiante estudiante = estudianteRepository.findByUsuario_IdUsuario(request.getIdEstudiante())
                    .orElseThrow(() -> new RuntimeException("Estudiante no encontrado para el usuario con ID: " + request.getIdEstudiante()));
            long postulacionesActivas = postulacionRepository.contarPostulacionesActivasPorEstudiante(estudiante.getIdEstudiante());
            if (postulacionesActivas > 0) {
                throw new RuntimeException("Ya tienes una postulación activa. No puedes postularte a más de una convocatoria a la vez.");
            }

            Integer idPostulacion = postulacionRepository.crearPostulacion(
                    request.getIdConvocatoria(),
                    estudiante.getIdEstudiante(),
                    new java.sql.Date(System.currentTimeMillis()),
                    "PENDIENTE",
                    request.getObservaciones()
            );
            if (idPostulacion == null || idPostulacion == -1)
                throw new RuntimeException("Error al crear la postulación — el SP devolvió -1");

            for (int i = 0; i < archivos.size(); i++) {
                MultipartFile archivo = archivos.get(i);
                Integer idTipoReq = tiposRequisito.get(i);

                if (!archivo.isEmpty()) {
                    try {
                        System.out.println("Intentando guardar archivo: " + archivo.getOriginalFilename() + " para req ID: " + idTipoReq);
                        Integer idRequisito = postulacionRepository.crearRequisitoAdjunto(
                                idPostulacion,
                                idTipoReq,
                                1, // idTipoEstadoRequisito = 1 (PENDIENTE/ENTREGADO)
                                archivo.getBytes(),
                                archivo.getOriginalFilename(),
                                new java.sql.Date(System.currentTimeMillis()));

                        System.out.println("Resultado sp_crear_requisito_adjunto: " + idRequisito);
                        if (idRequisito == null || idRequisito == -1) {
                            throw new RuntimeException("El SP sp_crear_requisito_adjunto devolvió " + idRequisito + " para archivo: " + archivo.getOriginalFilename());
                        }
                    } catch (Exception ex) {
                        System.err.println("Error interno al guardar archivo " + archivo.getOriginalFilename() + ": " + ex.getMessage());
                        ex.printStackTrace();
                        throw new RuntimeException("Error al guardar el archivo: " + archivo.getOriginalFilename(), ex);
                    }
                }
            }

            return "Postulación registrada con éxito. ID: " + idPostulacion;

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error en el proceso: " + e.getMessage());
        }
    }
}