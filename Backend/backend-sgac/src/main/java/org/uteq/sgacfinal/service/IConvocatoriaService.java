package org.uteq.sgacfinal.service;

import org.uteq.sgacfinal.dto.request.ConvocatoriaRequestDTO;
import org.uteq.sgacfinal.dto.request.configuracion.ConvocatoriaActualizarRequestDTO;
import org.uteq.sgacfinal.dto.request.configuracion.ConvocatoriaCrearRequestDTO;
import org.uteq.sgacfinal.dto.response.ConvocatoriaResponseDTO;
import org.uteq.sgacfinal.dto.response.configuracion.ConvocatoriaNativaResponseDTO;
import org.uteq.sgacfinal.dto.response.configuracion.VerificarFaseResponseDTO;
import org.uteq.sgacfinal.dto.response.configuracion.VerificarPostulantesResponseDTO;

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