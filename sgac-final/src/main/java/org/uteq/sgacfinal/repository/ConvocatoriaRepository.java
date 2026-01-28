package org.uteq.sgacfinal.repository;

import org.uteq.sgacfinal.entity.Convocatoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ConvocatoriaRepository extends JpaRepository<Convocatoria, Integer> {
    List<Convocatoria> findByActivo(Boolean activo);
    List<Convocatoria> findByEstado(String estado);
    List<Convocatoria> findByPeriodoAcademicoIdPeriodoAcademico(Integer idPeriodoAcademico);
    List<Convocatoria> findByDocenteIdDocente(Integer idDocente);
    List<Convocatoria> findByAsignaturaIdAsignatura(Integer idAsignatura);
}
