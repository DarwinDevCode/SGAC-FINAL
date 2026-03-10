package org.uteq.sgacfinal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.uteq.sgacfinal.entity.Convocatoria;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ConvocatoriaEstudianteRepository extends JpaRepository<Convocatoria, Integer> {






    interface ValidacionContextoProjection {
        Integer getIdEstudiante();
        Boolean getEsValido();
        String getMensaje();
    }

    @Query(value = "SELECT p_id_estudiante AS idEstudiante, p_es_valido AS esValido, p_mensaje AS mensaje " +
                   "FROM seguridad.fn_validar_contexto_estudiante(:idUsuario)", nativeQuery = true)
    ValidacionContextoProjection validarContextoEstudiante(@Param("idUsuario") Integer idUsuario);
    interface ElegibilidadAcademicaProjection {
        Boolean getEsElegible();
        String getMensaje();
    }

    @Query(value = "SELECT p_es_elegible AS esElegible, p_mensaje AS mensaje " +
                   "FROM academico.fn_verificar_elegibilidad_academica(:idEstudiante)", nativeQuery = true)
    ElegibilidadAcademicaProjection verificarElegibilidadAcademica(@Param("idEstudiante") Integer idEstudiante);
    interface ConvocatoriaEstudianteProjection {
        Integer getIdConvocatoria();
        String getNombreAsignatura();
        Integer getSemestreAsignatura();
        String getNombreCarrera();
        String getNombreDocente();
        Integer getCuposDisponibles();
        LocalDate getFechaPublicacion();
        LocalDate getFechaCierre();
        String getEstado();
    }


    @Query(value = "SELECT id_convocatoria AS idConvocatoria, " +
                   "nombre_asignatura AS nombreAsignatura, " +
                   "semestre_asignatura AS semestreAsignatura, " +
                   "nombre_carrera AS nombreCarrera, " +
                   "nombre_docente AS nombreDocente, " +
                   "cupos_disponibles AS cuposDisponibles, " +
                   "fecha_publicacion AS fechaPublicacion, " +
                   "fecha_cierre AS fechaCierre, " +
                   "estado AS estado " +
                   "FROM convocatoria.fn_listar_convocatorias_estudiante(:idUsuario)", nativeQuery = true)
    List<ConvocatoriaEstudianteProjection> listarConvocatoriasEstudiante(@Param("idUsuario") Integer idUsuario);
}
