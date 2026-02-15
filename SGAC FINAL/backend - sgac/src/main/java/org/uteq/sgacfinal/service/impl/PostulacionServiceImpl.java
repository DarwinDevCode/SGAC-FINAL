package org.uteq.sgacfinal.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.uteq.sgacfinal.dto.Request.PostulacionRequestDTO;
import org.uteq.sgacfinal.dto.Response.PostulacionResponseDTO;
import org.uteq.sgacfinal.entity.Postulacion;
import org.uteq.sgacfinal.repository.PostulacionRepository;
import org.uteq.sgacfinal.service.IPostulacionService;


import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class PostulacionServiceImpl implements IPostulacionService {

    private final PostulacionRepository postulacionRepository;

    @Override
    public PostulacionResponseDTO crear(PostulacionRequestDTO request) {
//        Integer idGenerado = postulacionRepository.registrarPostulacion(
//                request.getIdConvocatoria(),
//                request.getIdEstudiante(),
//                request.getIdPlazoActividad(),
//                request.getFechaPostulacion(),
//                request.getEstadoPostulacion(),
//                request.getObservaciones()
//        );
//
//        if (idGenerado == -1) {
//            throw new RuntimeException("Error al registrar postulación.");
//        }
//
//        return buscarPorId(idGenerado);
        return null;
    }

    @Override
    public PostulacionResponseDTO actualizar(Integer id, PostulacionRequestDTO request) {
//        Integer resultado = postulacionRepository.actualizarPostulacion(
//                id,
//                request.getEstadoPostulacion(),
//                request.getObservaciones()
//        );
//
//        if (resultado == -1) {
//            throw new RuntimeException("Error al actualizar postulación.");
//        }

//        return buscarPorId(id);

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
    public List<PostulacionResponseDTO> listarPorEstudiante(Integer idEstudiante) {
        List<Object[]> resultados = postulacionRepository.listarPostulacionesPorEstudianteSP(idEstudiante);

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
                // Resto de campos nulos porque el SP no los devuelve
                .build();
    }



    @Override
    @Transactional
    public String registrarPostulacionCompleta(PostulacionRequestDTO request, List<MultipartFile> archivos, List<Integer> tiposRequisito) {
        try {
            Integer idPostulacion = postulacionRepository.crearPostulacion(
                    request.getIdConvocatoria(),
                    request.getIdEstudiante(),
                    new java.sql.Date(System.currentTimeMillis()),
                    "PENDIENTE",
                    request.getObservaciones()
            );
            if (idPostulacion == -1) throw new RuntimeException("Error al crear la postulación");

            for (int i = 0; i < archivos.size(); i++) {
                MultipartFile archivo = archivos.get(i);
                Integer idTipoReq = tiposRequisito.get(i);

                if (!archivo.isEmpty()) {
                    Integer idRequisito = postulacionRepository.crearRequisitoAdjunto(
                            idPostulacion,
                            idTipoReq,
                            1,
                            archivo.getBytes(),
                            archivo.getOriginalFilename(),
                            new java.sql.Date(System.currentTimeMillis()));

                    if (idRequisito == -1) throw new RuntimeException("Error al guardar el archivo: " + archivo.getOriginalFilename());
                }
            }

            return "Postulación registrada con éxito. ID: " + idPostulacion;

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error en el proceso: " + e.getMessage());
        }
    }
}