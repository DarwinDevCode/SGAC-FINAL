package org.uteq.sgacfinal.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.uteq.sgacfinal.dto.Request.ConvocatoriaRequestDTO;
import org.uteq.sgacfinal.dto.Response.ConvocatoriaResponseDTO;
import org.uteq.sgacfinal.entity.Convocatoria;
import org.uteq.sgacfinal.repository.ConvocatoriaRepository;
import org.uteq.sgacfinal.service.IConvocatoriaService;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ConvocatoriaServiceImpl implements IConvocatoriaService {

    private final ConvocatoriaRepository convocatoriaRepository;

    @Override
    public ConvocatoriaResponseDTO crear(ConvocatoriaRequestDTO request) {
        Integer idGenerado = convocatoriaRepository.registrarConvocatoria(
                request.getIdPeriodoAcademico(),
                request.getIdAsignatura(),
                request.getIdDocente(),
                request.getCuposDisponibles(),
                request.getFechaPublicacion(),
                request.getFechaCierre(),
                request.getEstado()
        );

        if (idGenerado == -1) {
            throw new RuntimeException("Error al crear la convocatoria.");
        }

        return buscarPorId(idGenerado);
    }

    @Override
    public ConvocatoriaResponseDTO actualizar(Integer id, ConvocatoriaRequestDTO request) {
        Integer resultado = convocatoriaRepository.actualizarConvocatoria(
                id,
                request.getCuposDisponibles(),
                request.getFechaCierre(),
                request.getEstado()
        );

        if (resultado == -1) {
            throw new RuntimeException("Error al actualizar la convocatoria.");
        }

        return buscarPorId(id);
    }

    @Override
    public void desactivar(Integer id) {
        Integer resultado = convocatoriaRepository.desactivarConvocatoria(id);
        if (resultado == -1) {
            throw new RuntimeException("Error al desactivar la convocatoria.");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ConvocatoriaResponseDTO buscarPorId(Integer id) {
        Convocatoria convocatoria = convocatoriaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Convocatoria no encontrada con ID: " + id));
        return mapearADTO(convocatoria);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ConvocatoriaResponseDTO> listarPorPeriodo(Integer idPeriodo) {
        return convocatoriaRepository.findByPeriodoAcademico_IdPeriodoAcademico(idPeriodo).stream()
                .map(this::mapearADTO)
                .collect(Collectors.toList());
    }

    private ConvocatoriaResponseDTO mapearADTO(Convocatoria entidad) {
        String nombreDocente = "";
        if (entidad.getDocente() != null && entidad.getDocente().getUsuario() != null) {
            nombreDocente = entidad.getDocente().getUsuario().getNombres() + " " +
                    entidad.getDocente().getUsuario().getApellidos();
        }

        return ConvocatoriaResponseDTO.builder()
                .idConvocatoria(entidad.getIdConvocatoria())
                .idPeriodoAcademico(entidad.getPeriodoAcademico().getIdPeriodoAcademico())
                .nombrePeriodo(entidad.getPeriodoAcademico().getNombrePeriodo())
                .idAsignatura(entidad.getAsignatura().getIdAsignatura())
                .nombreAsignatura(entidad.getAsignatura().getNombreAsignatura())
                .idDocente(entidad.getDocente().getIdDocente())
                .nombreDocente(nombreDocente)
                .cuposDisponibles(entidad.getCuposDisponibles())
                .fechaPublicacion(entidad.getFechaPublicacion())
                .fechaCierre(entidad.getFechaCierre())
                .estado(entidad.getEstado())
                .activo(entidad.getActivo())
                .build();
    }
}