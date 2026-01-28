package com.sgac.repository;

import com.sgac.entity.RegistroActividad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface RegistroActividadRepository extends JpaRepository<RegistroActividad, Integer> {
    List<RegistroActividad> findByAyudantiaIdAyudantia(Integer idAyudantia);
    List<RegistroActividad> findByEstadoRevision(String estadoRevision);
}
