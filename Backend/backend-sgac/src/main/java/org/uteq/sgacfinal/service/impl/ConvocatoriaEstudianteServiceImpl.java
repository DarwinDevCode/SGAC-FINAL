package org.uteq.sgacfinal.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.uteq.sgacfinal.dto.Response.ConvocatoriaEstudianteResponseDTO;
import org.uteq.sgacfinal.dto.Response.ConvocatoriasEstudianteWrapperDTO;
import org.uteq.sgacfinal.dto.Response.ValidacionContextoEstudianteDTO;
import org.uteq.sgacfinal.dto.Response.ValidacionElegibilidadAcademicaDTO;
import org.uteq.sgacfinal.repository.ConvocatoriaEstudianteRepository;
import org.uteq.sgacfinal.repository.ConvocatoriaEstudianteRepository.ConvocatoriaEstudianteProjection;
import org.uteq.sgacfinal.repository.ConvocatoriaEstudianteRepository.ValidacionContextoProjection;
import org.uteq.sgacfinal.repository.ConvocatoriaEstudianteRepository.ElegibilidadAcademicaProjection;
import org.uteq.sgacfinal.service.IConvocatoriaEstudianteService;
import org.uteq.sgacfinal.service.IUsuarioSesionService;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ConvocatoriaEstudianteServiceImpl implements IConvocatoriaEstudianteService {

    private final ConvocatoriaEstudianteRepository convocatoriaEstudianteRepository;
    private final IUsuarioSesionService usuarioSesionService;

    @Override
    public ValidacionContextoEstudianteDTO validarContextoEstudiante(Integer idUsuario) {
        log.debug("Validando contexto de estudiante para usuario ID: {}", idUsuario);

        ValidacionContextoProjection projection = convocatoriaEstudianteRepository
                .validarContextoEstudiante(idUsuario);

        return ValidacionContextoEstudianteDTO.builder()
                .idEstudiante(projection.getIdEstudiante())
                .esValido(projection.getEsValido())
                .mensaje(projection.getMensaje())
                .build();
    }

    @Override
    public ValidacionElegibilidadAcademicaDTO verificarElegibilidadAcademica(Integer idEstudiante) {
        log.debug("Verificando elegibilidad academica para estudiante ID: {}", idEstudiante);

        ElegibilidadAcademicaProjection projection = convocatoriaEstudianteRepository
                .verificarElegibilidadAcademica(idEstudiante);

        return ValidacionElegibilidadAcademicaDTO.builder()
                .esElegible(projection.getEsElegible())
                .mensaje(projection.getMensaje())
                .build();
    }

    @Override
    public List<ConvocatoriaEstudianteResponseDTO> listarConvocatoriasElegibles(Integer idUsuario) {
        log.debug("Listando convocatorias elegibles para usuario ID: {}", idUsuario);

        List<ConvocatoriaEstudianteProjection> projections = convocatoriaEstudianteRepository
                .listarConvocatoriasEstudiante(idUsuario);

        return projections.stream()
                .map(this::mapProjectionToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public ConvocatoriasEstudianteWrapperDTO listarMisConvocatoriasElegibles() {
        Integer idUsuario = usuarioSesionService.getIdUsuarioAutenticado();
        log.info("Obteniendo convocatorias elegibles para usuario autenticado ID: {}", idUsuario);

        try {
            List<ConvocatoriaEstudianteResponseDTO> convocatorias = listarConvocatoriasElegibles(idUsuario);

            log.info("Se encontraron {} convocatorias elegibles para el usuario ID: {}",
                    convocatorias.size(), idUsuario);

            return ConvocatoriasEstudianteWrapperDTO.exitoso(convocatorias);

        } catch (DataAccessException ex) {
            String mensajeError = extraerMensajeError(ex);
            log.warn("Error de validacion para usuario ID {}: {}", idUsuario, mensajeError);

            return ConvocatoriasEstudianteWrapperDTO.error(mensajeError);
        }
    }


    private ConvocatoriaEstudianteResponseDTO mapProjectionToDTO(ConvocatoriaEstudianteProjection projection) {
        return ConvocatoriaEstudianteResponseDTO.builder()
                .idConvocatoria(projection.getIdConvocatoria())
                .nombreAsignatura(projection.getNombreAsignatura())
                .semestreAsignatura(projection.getSemestreAsignatura())
                .nombreCarrera(projection.getNombreCarrera())
                .nombreDocente(projection.getNombreDocente())
                .cuposDisponibles(projection.getCuposDisponibles())
                .fechaPublicacion(projection.getFechaPublicacion())
                .fechaCierre(projection.getFechaCierre())
                .estado(projection.getEstado())
                .build();
    }


    private String extraerMensajeError(DataAccessException ex) {
        String mensaje = ex.getMostSpecificCause().getMessage();

        if (mensaje != null) {
            if (mensaje.startsWith("ERROR: ")) {
                mensaje = mensaje.substring(7);
            }
            int indexNewLine = mensaje.indexOf('\n');
            if (indexNewLine > 0) {
                mensaje = mensaje.substring(0, indexNewLine);
            }
        }

        return mensaje != null ? mensaje : "Error desconocido al consultar convocatorias";
    }
}

