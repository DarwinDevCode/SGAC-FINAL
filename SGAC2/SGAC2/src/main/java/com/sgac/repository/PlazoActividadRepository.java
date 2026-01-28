package com.sgac.repository;

import com.sgac.entity.PlazoActividad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PlazoActividadRepository extends JpaRepository<PlazoActividad, Integer> {
    List<PlazoActividad> findByActivo(Boolean activo);
    List<PlazoActividad> findByPeriodoAcademicoIdPeriodoAcademico(Integer idPeriodoAcademico);
}
