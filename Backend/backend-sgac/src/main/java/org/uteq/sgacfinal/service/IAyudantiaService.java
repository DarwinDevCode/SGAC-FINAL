package org.uteq.sgacfinal.service;

import org.uteq.sgacfinal.dto.Request.AyudantiaRequestDTO;
import org.uteq.sgacfinal.dto.Response.AyudantiaDetalleResponseDTO;
import org.uteq.sgacfinal.dto.Response.AyudantiaResponseDTO;
import org.uteq.sgacfinal.dto.Response.HistorialAyudantiaDTO;
import org.uteq.sgacfinal.dto.Response.RegistroActividadDetalleDTO;
import org.uteq.sgacfinal.dto.Response.SesionResponseDTO;

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
    Optional<Integer> buscarIdAyudantiaActivaPorUsuario(Integer idUsuario);
    List<HistorialAyudantiaDTO> listarHistorialEstudiante(Integer idUsuario);
}
