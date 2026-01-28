package org.uteq.sgacfinal.repository;

import org.uteq.sgacfinal.entity.Asignatura;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AsignaturaRepository extends JpaRepository<Asignatura, Integer> {
    List<Asignatura> findByCarreraIdCarrera(Integer idCarrera);
    List<Asignatura> findBySemestre(Integer semestre);
}
