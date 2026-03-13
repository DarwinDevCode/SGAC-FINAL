package org.uteq.sgacfinal.service.impl.convocatorias;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.uteq.sgacfinal.dto.Response.convocatorias.ComisionDetalleResponseDTO;
import org.uteq.sgacfinal.dto.Response.convocatorias.GenerarComisionesResponseDTO;
import org.uteq.sgacfinal.exception.ComisionException;
import org.uteq.sgacfinal.repository.convocatorias.IComisionRepository;
import org.uteq.sgacfinal.service.convocatorias.IComisionService;

@Slf4j
@Service
@RequiredArgsConstructor
public class ComisionServiceImpl implements IComisionService {

    private final IComisionRepository comisionRepository;
    private final ObjectMapper        objectMapper;

    @Override
    @Transactional
    public GenerarComisionesResponseDTO generarComisionesAutomaticas() {
        try {
            String json = comisionRepository.generarComisionesAutomaticas();
            GenerarComisionesResponseDTO dto =
                    objectMapper.readValue(json, GenerarComisionesResponseDTO.class);

            if (!dto.isExito()) {
                throw new ComisionException(dto.getMensaje());
            }
            return dto;

        } catch (ComisionException e) {
            throw e;
        } catch (Exception e) {
            log.error("[ComisionService] Error en generarComisionesAutomaticas", e);
            throw new ComisionException(
                    "Error interno al generar las comisiones: " + e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ComisionDetalleResponseDTO consultarComision(Integer idUsuario, String rol) {
        validarParametros(idUsuario, rol);
        try {
            String json = comisionRepository.consultarComisionDetalle(idUsuario,
                    rol.toUpperCase());
            ComisionDetalleResponseDTO dto =
                    objectMapper.readValue(json, ComisionDetalleResponseDTO.class);

            if (!dto.isExito()) {
                throw new ComisionException(dto.getMensaje());
            }
            return dto;

        } catch (ComisionException e) {
            throw e;
        } catch (Exception e) {
            log.error("[ComisionService] Error en consultarComision — usuario={}, rol={}",
                    idUsuario, rol, e);
            throw new ComisionException(
                    "Error interno al consultar la comisión: " + e.getMessage());
        }
    }

    private void validarParametros(Integer idUsuario, String rol) {
        if (idUsuario == null || idUsuario <= 0) {
            throw new ComisionException("El id de usuario es inválido.");
        }
        if (rol == null || rol.isBlank()) {
            throw new ComisionException("El rol es requerido.");
        }
        String rolUp = rol.toUpperCase();
        if (!rolUp.equals("ESTUDIANTE") && !rolUp.equals("DECANO")
                && !rolUp.equals("COORDINADOR") && !rolUp.equals("DOCENTE")) {
            throw new ComisionException(
                    "Rol no reconocido: " + rol
                            + ". Valores aceptados: ESTUDIANTE, DECANO, COORDINADOR, DOCENTE.");
        }
    }
}