package org.uteq.sgacfinal.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.uteq.sgacfinal.dto.Request.ComisionSeleccionRequestDTO;
import org.uteq.sgacfinal.dto.Response.ComisionSeleccionResponseDTO;
import org.uteq.sgacfinal.entity.ComisionSeleccion;
import org.uteq.sgacfinal.repository.ComisionSeleccionRepository;
import org.uteq.sgacfinal.service.IComisionSeleccionService;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ComisionSeleccionServiceImpl implements IComisionSeleccionService {

    private final ComisionSeleccionRepository comisionRepository;

    @Override
    public ComisionSeleccionResponseDTO crear(ComisionSeleccionRequestDTO request) {
        Integer idGenerado = comisionRepository.registrarComision(
                request.getIdConvocatoria(),
                request.getNombreComision(),
                request.getFechaConformacion()
        );

        if (idGenerado == -1) {
            throw new RuntimeException("Error al crear la comisión de selección.");
        }

        return buscarPorId(idGenerado);
    }

    @Override
    public ComisionSeleccionResponseDTO actualizar(Integer id, ComisionSeleccionRequestDTO request) {
        Integer resultado = comisionRepository.actualizarComision(
                id,
                request.getNombreComision(),
                request.getFechaConformacion()
        );

        if (resultado == -1) {
            throw new RuntimeException("Error al actualizar la comisión.");
        }

        return buscarPorId(id);
    }

    @Override
    public void desactivar(Integer id) {
        Integer resultado = comisionRepository.desactivarComision(id);
        if (resultado == -1) {
            throw new RuntimeException("Error al desactivar la comisión.");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ComisionSeleccionResponseDTO buscarPorId(Integer id) {
        ComisionSeleccion comision = comisionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Comisión no encontrada con ID: " + id));
        return mapearADTO(comision);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ComisionSeleccionResponseDTO> listarPorConvocatoria(Integer idConvocatoria) {
        return comisionRepository.findByConvocatoria_IdConvocatoria(idConvocatoria).stream()
                .map(this::mapearADTO)
                .collect(Collectors.toList());
    }

    private ComisionSeleccionResponseDTO mapearADTO(ComisionSeleccion entidad) {
        return ComisionSeleccionResponseDTO.builder()
                .idComisionSeleccion(entidad.getIdComisionSeleccion())
                .idConvocatoria(entidad.getConvocatoria().getIdConvocatoria())
                .nombreComision(entidad.getNombreComision())
                .fechaConformacion(entidad.getFechaConformacion())
                .activo(entidad.getActivo())
                .build();
    }
}