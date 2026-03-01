package org.uteq.sgacfinal.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.uteq.sgacfinal.dto.Request.EvidenciaRegistroActividadRequestDTO;
import org.uteq.sgacfinal.dto.Response.EvidenciaRegistroActividadResponseDTO;
import org.uteq.sgacfinal.entity.EvidenciaRegistroActividad;
import org.uteq.sgacfinal.repository.EvidenciaRegistroActividadRepository;
import org.uteq.sgacfinal.service.IEvidenciaRegistroActividadService;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class EvidenciaRegistroActividadServiceImpl implements IEvidenciaRegistroActividadService {

    private final EvidenciaRegistroActividadRepository evidenciaRepository;

    @Override
    public EvidenciaRegistroActividadResponseDTO crear(EvidenciaRegistroActividadRequestDTO request) {
        Integer idGenerado = evidenciaRepository.registrarEvidencia(
                request.getIdRegistroActividad(),
                request.getTipoEvidencia(),
                request.getArchivo(),
                request.getNombreArchivo(),
                request.getFechaSubida()
        );

        if (idGenerado == -1) {
            throw new RuntimeException("Error al subir la evidencia.");
        }

        return buscarPorId(idGenerado);
    }

    @Override
    public EvidenciaRegistroActividadResponseDTO actualizar(Integer id, EvidenciaRegistroActividadRequestDTO request) {
        Integer resultado = evidenciaRepository.actualizarEvidencia(
                id,
                request.getTipoEvidencia(),
                request.getNombreArchivo(),
                request.getArchivo()
        );

        if (resultado == -1) {
            throw new RuntimeException("Error al actualizar la evidencia.");
        }
        return buscarPorId(id);
    }

    @Override
    public void desactivar(Integer id) {
        Integer resultado = evidenciaRepository.desactivarEvidencia(id);
        if (resultado == -1) {
            throw new RuntimeException("Error al eliminar (desactivar) la evidencia.");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public EvidenciaRegistroActividadResponseDTO buscarPorId(Integer id) {
        EvidenciaRegistroActividad evidencia = evidenciaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Evidencia no encontrada con ID: " + id));
        return mapearADTO(evidencia);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EvidenciaRegistroActividadResponseDTO> listarPorRegistroActividad(Integer idRegistroActividad) {
        return evidenciaRepository.obtenerEvidenciasPorActividadSP(idRegistroActividad).stream()
                .map(this::mapearADTO)
                .collect(Collectors.toList());
    }

    private EvidenciaRegistroActividadResponseDTO mapearADTO(EvidenciaRegistroActividad entidad) {
        return EvidenciaRegistroActividadResponseDTO.builder()
                .idEvidenciaRegistroActividad(entidad.getIdEvidenciaRegistroActividad())
                .idRegistroActividad(entidad.getRegistroActividad().getIdRegistroActividad())
                .descripcionActividad(entidad.getRegistroActividad().getDescripcionActividad()) // Dato útil del padre
                .tipoEvidencia(entidad.getIdTipoEvidencia().getExtensionPermitida())
                .nombreArchivo(entidad.getNombreArchivo())
                .fechaSubida(entidad.getFechaSubida())
                .activo(entidad.getActivo())
                .build();
    }
}