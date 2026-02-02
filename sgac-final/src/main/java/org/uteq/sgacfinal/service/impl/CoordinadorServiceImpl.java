package org.uteq.sgacfinal.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.uteq.sgacfinal.dto.Request.CoordinadorRequestDTO;
import org.uteq.sgacfinal.dto.Response.CoordinadorResponseDTO;
import org.uteq.sgacfinal.entity.Coordinador;
import org.uteq.sgacfinal.repository.CoordinadorRepository;
import org.uteq.sgacfinal.service.ICoordinadorService;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class CoordinadorServiceImpl implements ICoordinadorService {

    private final CoordinadorRepository coordinadorRepository;

    @Override
    public CoordinadorResponseDTO crear(CoordinadorRequestDTO request) {
        Integer idGenerado = coordinadorRepository.registrarCoordinador(
                request.getIdUsuario(),
                request.getIdCarrera(),
                request.getFechaInicio(),
                request.getFechaFin()
        );

        if (idGenerado == -1) {
            throw new RuntimeException("Error al registrar coordinador.");
        }

        return buscarPorId(idGenerado);
    }

    @Override
    public CoordinadorResponseDTO actualizar(Integer id, CoordinadorRequestDTO request) {
        Integer resultado = coordinadorRepository.actualizarCoordinador(
                id,
                request.getIdCarrera(),
                request.getFechaInicio(),
                request.getFechaFin()
        );

        if (resultado == -1) {
            throw new RuntimeException("Error al actualizar coordinador.");
        }

        return buscarPorId(id);
    }

    @Override
    public void desactivar(Integer id) {
        Integer resultado = coordinadorRepository.desactivarCoordinador(id);
        if (resultado == -1) {
            throw new RuntimeException("Error al desactivar coordinador.");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public CoordinadorResponseDTO buscarPorId(Integer id) {
        Coordinador coordinador = coordinadorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Coordinador no encontrado con ID: " + id));
        return mapearADTO(coordinador);
    }

    @Override
    @Transactional(readOnly = true)
    public CoordinadorResponseDTO buscarPorUsuario(Integer idUsuario) {
        Coordinador coordinador = coordinadorRepository.findByUsuario_IdUsuarioAndActivoTrue(idUsuario)
                .orElseThrow(() -> new RuntimeException("No existe coordinador activo para el usuario ID: " + idUsuario));
        return mapearADTO(coordinador);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CoordinadorResponseDTO> listarTodos() {
        return coordinadorRepository.findAll().stream()
                .map(this::mapearADTO)
                .collect(Collectors.toList());
    }

//    @Override
//    @Transactional(readOnly = true)
//    public List<CoordinadorResponseDTO> listarActivosPorCarrera(Integer idCarrera) {
//        return coordinadorRepository.findByCarrera_IdCarreraAndActivoTrue(idCarrera).stream()
//                .map(this::mapearADTO)
//                .collect(Collectors.toList());
//    }

    private CoordinadorResponseDTO mapearADTO(Coordinador entidad) {
        String nombreUsuario = entidad.getUsuario().getNombres() + " " + entidad.getUsuario().getApellidos();
        return CoordinadorResponseDTO.builder()
                .idCoordinador(entidad.getIdCoordinador())
                .idUsuario(entidad.getUsuario().getIdUsuario())
                .nombreCompletoUsuario(nombreUsuario)
                .cedula(entidad.getUsuario().getCedula())
                .idCarrera(entidad.getCarrera().getIdCarrera())
                .nombreCarrera(entidad.getCarrera().getNombreCarrera())
                .fechaInicio(entidad.getFechaInicio())
                .fechaFin(entidad.getFechaFin())
                .activo(entidad.getActivo())
                .build();
    }
}