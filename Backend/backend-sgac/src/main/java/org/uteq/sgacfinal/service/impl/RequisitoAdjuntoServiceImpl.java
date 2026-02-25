package org.uteq.sgacfinal.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.uteq.sgacfinal.dto.Request.RequisitoAdjuntoRequestDTO;
import org.uteq.sgacfinal.dto.Response.RequisitoAdjuntoResponseDTO;
import org.uteq.sgacfinal.entity.RequisitoAdjunto;
import org.uteq.sgacfinal.repository.RequisitoAdjuntoRepository;
import org.uteq.sgacfinal.service.IRequisitoAdjuntoService;

import java.sql.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class RequisitoAdjuntoServiceImpl implements IRequisitoAdjuntoService {

    private final RequisitoAdjuntoRepository requisitoRepository;

    @Override
    public RequisitoAdjuntoResponseDTO crear(RequisitoAdjuntoRequestDTO request) {
        Integer idGenerado = requisitoRepository.registrarRequisito(
                request.getIdPostulacion(),
                request.getIdTipoRequisitoPostulacion(),
                request.getIdTipoEstadoRequisito(),
                request.getArchivo(),
                request.getNombreArchivo(),
                request.getFechaSubida(),
                request.getObservacion()
        );

        if (idGenerado == -1) {
            throw new RuntimeException("Error al adjuntar requisito.");
        }

        return buscarPorId(idGenerado);
    }

    @Override
    public RequisitoAdjuntoResponseDTO actualizar(Integer id, RequisitoAdjuntoRequestDTO request) {
        Integer resultado = requisitoRepository.actualizarRequisito(
                id,
                request.getIdTipoEstadoRequisito(),
                request.getObservacion()
        );

        if (resultado == -1) {
            throw new RuntimeException("Error al actualizar el estado del requisito.");
        }

        return buscarPorId(id);
    }

    @Override
    @Transactional(readOnly = true)
    public RequisitoAdjuntoResponseDTO buscarPorId(Integer id) {
        RequisitoAdjunto requisito = requisitoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Requisito adjunto no encontrado con ID: " + id));
        return mapearADTO(requisito);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RequisitoAdjuntoResponseDTO> listarPorPostulacion(Integer idPostulacion) {
        List<Object[]> resultados = requisitoRepository.obtenerRequisitosPorPostulacionSP(idPostulacion);

        return resultados.stream()
                .map(this::mapearDesdeObjectArray)
                .collect(Collectors.toList());
    }

    private RequisitoAdjuntoResponseDTO mapearADTO(RequisitoAdjunto entidad) {
        return RequisitoAdjuntoResponseDTO.builder()
                .idRequisitoAdjunto(entidad.getIdRequisitoAdjunto())
                .idPostulacion(entidad.getPostulacion().getIdPostulacion())
                .nombreEstado(entidad.getTipoEstadoRequisito().getNombreEstado())
                .nombreArchivo(entidad.getNombreArchivo())
                .fechaSubida(entidad.getFechaSubida())
                .observacion(entidad.getObservacion())
                .build();
    }

    private RequisitoAdjuntoResponseDTO mapearDesdeObjectArray(Object[] obj) {
        return RequisitoAdjuntoResponseDTO.builder()
                .idRequisitoAdjunto((Integer) obj[0])
                .nombreArchivo((String) obj[1])
                .fechaSubida(obj[2] != null ? ((Date) obj[2]).toLocalDate() : null)
                .build();
    }
}