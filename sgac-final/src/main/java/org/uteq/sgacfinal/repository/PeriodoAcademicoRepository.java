package org.uteq.sgacfinal.repository;

import org.uteq.sgacfinal.entity.PeriodoAcademico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PeriodoAcademicoRepository extends JpaRepository<PeriodoAcademico, Integer> {
    List<PeriodoAcademico> findByEstado(String estado);
}
