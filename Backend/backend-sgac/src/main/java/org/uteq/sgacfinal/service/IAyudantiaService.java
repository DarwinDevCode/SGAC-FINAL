package org.uteq.sgacfinal.service;

import org.uteq.sgacfinal.dto.request.AyudantiaRequestDTO;
import org.uteq.sgacfinal.dto.response.AyudantiaDetalleResponseDTO;
import org.uteq.sgacfinal.dto.response.AyudantiaResponseDTO;
import org.uteq.sgacfinal.dto.response.RegistroActividadDetalleDTO;
import org.uteq.sgacfinal.dto.response.SesionResponseDTO;

import java.util.List;
import java.util.Optional;

public interface IAyudantiaService {
    AyudantiaResponseDTO crear(AyudantiaRequestDTO request);
    AyudantiaResponseDTO actualizar(Integer id, AyudantiaRequestDTO request);
    AyudantiaResponseDTO buscarPorId(Integer id);
    AyudantiaResponseDTO buscarPorPostulacion(Integer idPostulacion);
    List<AyudantiaResponseDTO> listarTodas();
    AyudantiaDetalleResponseDTO obtenerDetallescompletos(Integer idAyudantia);
    List<RegistroActividadDetalleDTO> listarActividadesPorUsuario(Integer idUsuario);
    List<SesionResponseDTO> listarSesionesPorAyudante(Integer idAyudante);
    Optional<SesionResponseDTO> obtenerDetalleSesionConEvidencias(Integer idAyudante, Integer idRegistroActividad);
}