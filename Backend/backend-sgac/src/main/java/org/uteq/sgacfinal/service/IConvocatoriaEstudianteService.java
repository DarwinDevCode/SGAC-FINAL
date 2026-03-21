package org.uteq.sgacfinal.service;

import org.uteq.sgacfinal.dto.response.ConvocatoriaEstudianteResponseDTO;
import org.uteq.sgacfinal.dto.response.ConvocatoriasEstudianteWrapperDTO;
import org.uteq.sgacfinal.dto.response.ValidacionContextoEstudianteDTO;
import org.uteq.sgacfinal.dto.response.ValidacionElegibilidadAcademicaDTO;

import java.util.List;

/**
 * Servicio para la gestion de convocatorias desde la perspectiva del estudiante.
 * Proporciona funcionalidades de consulta de convocatorias elegibles basadas
 * en los criterios academicos del estudiante autenticado.
 */
public interface IConvocatoriaEstudianteService {

    /**
     * Valida el contexto del estudiante: transforma id_usuario en id_estudiante.
     * @param idUsuario ID del usuario autenticado
     * @return DTO con el resultado de la validacion (id_estudiante, es_valido, mensaje)
     */
    ValidacionContextoEstudianteDTO validarContextoEstudiante(Integer idUsuario);

    /**
     * Verifica si el estudiante cumple los requisitos academicos para postular.
     * Requisito: semestre >= 6
     * @param idEstudiante ID del estudiante
     * @return DTO con el resultado de la validacion (es_elegible, mensaje)
     */
    ValidacionElegibilidadAcademicaDTO verificarElegibilidadAcademica(Integer idEstudiante);

    /**
     * Lista las convocatorias elegibles para el estudiante.
     * Aplica filtros de carrera, nivel de asignatura, estado y fecha.
     * @param idUsuario ID del usuario autenticado
     * @return Lista de convocatorias elegibles
     */
    List<ConvocatoriaEstudianteResponseDTO> listarConvocatoriasElegibles(Integer idUsuario);

    /**
     * Lista las convocatorias elegibles para el estudiante autenticado actualmente en sesion.
     * Obtiene el idUsuario del contexto de seguridad.
     * @return Wrapper con la lista de convocatorias y metadatos
     */
    ConvocatoriasEstudianteWrapperDTO listarMisConvocatoriasElegibles();
}
