package com.sgac.repository;

import com.sgac.entity.EvidenciaRegistroActividad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface EvidenciaRegistroActividadRepository extends JpaRepository<EvidenciaRegistroActividad, Integer> {
    List<EvidenciaRegistroActividad> findByRegistroActividadIdRegistroActividad(Integer idRegistroActividad);
    List<EvidenciaRegistroActividad> findByActivo(Boolean activo);
}
