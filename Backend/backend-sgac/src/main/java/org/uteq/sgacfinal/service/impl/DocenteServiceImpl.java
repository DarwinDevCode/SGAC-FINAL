package org.uteq.sgacfinal.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.uteq.sgacfinal.dto.Request.DocenteRequestDTO;
import org.uteq.sgacfinal.dto.Response.DocenteResponseDTO;
import org.uteq.sgacfinal.entity.Docente;
import org.uteq.sgacfinal.repository.DocenteRepository;
import org.uteq.sgacfinal.service.IDocenteService;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class DocenteServiceImpl implements IDocenteService {

    private final DocenteRepository docenteRepository;

    @Override
    public DocenteResponseDTO crear(DocenteRequestDTO request) {
        Integer idGenerado = docenteRepository.registrarDocente(
                request.getIdUsuario(),
                request.getFechaInicio()
        );

        if (idGenerado == -1) {
            throw new RuntimeException("Error al registrar docente.");
        }

        return buscarPorId(idGenerado);
    }

    @Override
    public DocenteResponseDTO actualizar(Integer id, DocenteRequestDTO request) {
        Integer resultado = docenteRepository.actualizarDocente(
                id,
                request.getFechaInicio(),
                request.getFechaFin()
        );

        if (resultado == -1) {
            throw new RuntimeException("Error al actualizar docente.");
        }

        return buscarPorId(id);
    }

    @Override
    public void desactivar(Integer id) {
        Integer resultado = docenteRepository.desactivarDocente(id);
        if (resultado == -1) {
            throw new RuntimeException("Error al desactivar docente.");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public DocenteResponseDTO buscarPorId(Integer id) {
        Docente docente = docenteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Docente no encontrado con ID: " + id));
        return mapearADTO(docente);
    }

    @Override
    @Transactional(readOnly = true)
    public DocenteResponseDTO buscarPorUsuario(Integer idUsuario) {
        Docente docente = docenteRepository.findByUsuario_IdUsuario(idUsuario)
                .orElseThrow(() -> new RuntimeException("No existe registro de docente para el usuario ID: " + idUsuario));
        return mapearADTO(docente);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DocenteResponseDTO> listarTodos() {
        return docenteRepository.findAll().stream()
                .map(this::mapearADTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<DocenteResponseDTO> listarDocentesActivos() {
        return docenteRepository.findAllActive().stream()
                .map(this::mapearADTO)
                .collect(Collectors.toList());
    }

    private DocenteResponseDTO mapearADTO(Docente entidad) {
        String nombreUsuario = entidad.getUsuario().getNombres() + " " + entidad.getUsuario().getApellidos();
        return DocenteResponseDTO.builder()
                .idDocente(entidad.getIdDocente())
                .idUsuario(entidad.getUsuario().getIdUsuario())
                .nombreCompletoUsuario(nombreUsuario)
                .correoUsuario(entidad.getUsuario().getCorreo())
                .cedulaUsuario(entidad.getUsuario().getCedula())
                .fechaInicio(entidad.getFechaInicio())
                .fechaFin(entidad.getFechaFin())
                .activo(entidad.getActivo())
                .build();
    }
}