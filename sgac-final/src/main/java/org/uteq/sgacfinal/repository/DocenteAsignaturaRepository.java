package org.uteq.sgacfinal.repository;

import org.uteq.sgacfinal.entity.DocenteAsignatura;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DocenteAsignaturaRepository extends JpaRepository<DocenteAsignatura, Integer> {
    List<DocenteAsignatura> findByDocenteIdDocente(Integer idDocente);
    List<DocenteAsignatura> findByAsignaturaIdAsignatura(Integer idAsignatura);
    List<DocenteAsignatura> findByActivo(Boolean activo);
}
