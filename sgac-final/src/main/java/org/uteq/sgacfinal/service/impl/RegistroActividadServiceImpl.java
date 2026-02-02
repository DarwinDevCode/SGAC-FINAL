package org.uteq.sgacfinal.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.uteq.sgacfinal.dto.Request.RegistroActividadRequestDTO;
import org.uteq.sgacfinal.dto.Response.RegistroActividadResponseDTO;
import org.uteq.sgacfinal.entity.RegistroActividad;
import org.uteq.sgacfinal.repository.RegistroActividadRepository;
import org.uteq.sgacfinal.service.IRegistroActividadService;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class RegistroActividadServiceImpl implements IRegistroActividadService {

    private final RegistroActividadRepository registroRepository;

    @Override
    public RegistroActividadResponseDTO crear(RegistroActividadRequestDTO request) {
        Integer idGenerado = registroRepository.registrarActividad(
                request.getIdAyudantia(),
                request.getDescripcionActividad(),
                request.getTemaTratado(),
                request.getFecha(),
                request.getNumeroAsistentes(),
                request.getHorasDedicadas(),
                request.getEstadoRevision()
        );

        if (idGenerado == -1) {
            throw new RuntimeException("Error al registrar la actividad.");
        }

        return buscarPorId(idGenerado);
    }

    @Override
    public RegistroActividadResponseDTO actualizar(Integer id, RegistroActividadRequestDTO request) {
        Integer resultado = registroRepository.actualizarActividad(
                id,
                request.getDescripcionActividad(),
                request.getTemaTratado(),
                request.getHorasDedicadas(),
                request.getEstadoRevision()
        );

        if (resultado == -1) {
            throw new RuntimeException("Error al actualizar la actividad.");
        }

        return buscarPorId(id);
    }

    @Override
    public void desactivar(Integer id) {
        RegistroActividad actividad = registroRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Actividad no encontrada."));

        registroRepository.actualizarActividad(
                id,
                actividad.getDescripcionActividad(),
                actividad.getTemaTratado(),
                actividad.getHorasDedicadas(),
                "ANULADO"
        );
    }

    @Override
    @Transactional(readOnly = true)
    public RegistroActividadResponseDTO buscarPorId(Integer id) {
        RegistroActividad registro = registroRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Registro de actividad no encontrado con ID: " + id));
        return mapearADTO(registro);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RegistroActividadResponseDTO> listarPorAyudantia(Integer idAyudantia) {
        return registroRepository.listarActividadesPorAyudantiaSP(idAyudantia).stream()
                .map(this::mapearADTO)
                .collect(Collectors.toList());
    }

    private RegistroActividadResponseDTO mapearADTO(RegistroActividad entidad) {
        return RegistroActividadResponseDTO.builder()
                .idRegistroActividad(entidad.getIdRegistroActividad())
                .idAyudantia(entidad.getAyudantia().getIdAyudantia())
                .descripcionActividad(entidad.getDescripcionActividad())
                .temaTratado(entidad.getTemaTratado())
                .fecha(entidad.getFecha())
                .numeroAsistentes(entidad.getNumeroAsistentes())
                .horasDedicadas(entidad.getHorasDedicadas())
                .estadoRevision(entidad.getEstadoRevision())
                .build();
    }
}