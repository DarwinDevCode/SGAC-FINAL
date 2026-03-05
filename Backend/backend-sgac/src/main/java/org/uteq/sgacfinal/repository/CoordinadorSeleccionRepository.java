package org.uteq.sgacfinal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.uteq.sgacfinal.dto.Response.AsignaturaSelectableDTO;
import org.uteq.sgacfinal.dto.Response.DocenteSelectableDTO;
import org.uteq.sgacfinal.entity.Docente;

import java.util.List;

@Repository
public interface CoordinadorSeleccionRepository extends JpaRepository<Docente, Integer> {

    /**
     * Docentes seleccionables para la carrera del coordinador:
     * - docente.activo = true
     * - usuario del docente en seguridad (para nombre completo)
     * - el docente debe tener al menos 1 relación docente_asignatura activa hacia una asignatura activa
     * - y esa asignatura debe pertenecer a la carrera del coordinador
     */
    @Query("""
        SELECT DISTINCT new org.uteq.sgacfinal.dto.Response.DocenteSelectableDTO(
            d.idDocente,
            CONCAT(u.nombres, ' ', u.apellidos)
        )
        FROM Docente d
        JOIN d.usuario u
        JOIN d.docenteAsignaturas da
        JOIN da.asignatura a
        WHERE d.activo = true
          AND da.activo = true
          AND a.activo = true
          AND a.carrera.idCarrera = :idCarrera
        ORDER BY CONCAT(u.nombres, ' ', u.apellidos), d.idDocente
        """)
    List<DocenteSelectableDTO> listarDocentesSeleccionables(@Param("idCarrera") Integer idCarrera);


    /**
     * Asignaturas seleccionables de un docente.
     * Filtros: asignatura.activo = true y docente_asignatura.activo = true.
     */
    @Query("""
        SELECT DISTINCT new org.uteq.sgacfinal.dto.Response.AsignaturaSelectableDTO(
            a.idAsignatura,
            a.nombreAsignatura
        )
        FROM DocenteAsignatura da
        JOIN da.asignatura a
        JOIN da.docente d
        WHERE d.idDocente = :idDocente
          AND da.activo = true
          AND a.activo = true
        ORDER BY a.nombreAsignatura
        """)
    List<AsignaturaSelectableDTO> listarAsignaturasPorDocente(@Param("idDocente") Integer idDocente);
}

