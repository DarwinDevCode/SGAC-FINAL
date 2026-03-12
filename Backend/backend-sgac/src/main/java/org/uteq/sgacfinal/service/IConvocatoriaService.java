package org.uteq.sgacfinal.service;

import org.uteq.sgacfinal.dto.Request.ConvocatoriaRequestDTO;
import org.uteq.sgacfinal.dto.Request.configuracion.ConvocatoriaActualizarRequestDTO;
import org.uteq.sgacfinal.dto.Request.configuracion.ConvocatoriaCrearRequestDTO;
import org.uteq.sgacfinal.dto.Response.ConvocatoriaDetalleDTO;
import org.uteq.sgacfinal.dto.Response.ConvocatoriaResponseDTO;
import org.uteq.sgacfinal.dto.Response.configuracion.ConvocatoriaNativaResponseDTO;
import org.uteq.sgacfinal.dto.Response.configuracion.VerificarFaseResponseDTO;
import org.uteq.sgacfinal.dto.Response.configuracion.VerificarPostulantesResponseDTO;

import java.util.List;

public interface IConvocatoriaService {
    ConvocatoriaResponseDTO create(ConvocatoriaRequestDTO dto);
    ConvocatoriaResponseDTO update(ConvocatoriaRequestDTO dto);
    List<ConvocatoriaResponseDTO> findAll();
    ConvocatoriaResponseDTO findById(Integer id);
    void delete(Integer id);

    VerificarFaseResponseDTO verificarFase();
    ConvocatoriaNativaResponseDTO crear(ConvocatoriaCrearRequestDTO request);
    ConvocatoriaNativaResponseDTO actualizar(ConvocatoriaActualizarRequestDTO request);
    VerificarPostulantesResponseDTO checkPostulantes(Integer idConvocatoria);
    ConvocatoriaNativaResponseDTO desactivar(Integer idConvocatoria);
}