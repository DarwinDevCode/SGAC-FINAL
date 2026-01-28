package org.uteq.sgacfinal.repository;

import org.uteq.sgacfinal.entity.PeriodoAcademicoRequisitoPostulacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PeriodoAcademicoRequisitoPostulacionRepository extends JpaRepository<PeriodoAcademicoRequisitoPostulacion, Integer> {
    List<PeriodoAcademicoRequisitoPostulacion> findByPeriodoAcademicoIdPeriodoAcademico(Integer idPeriodoAcademico);
    List<PeriodoAcademicoRequisitoPostulacion> findByActivo(Boolean activo);
}
