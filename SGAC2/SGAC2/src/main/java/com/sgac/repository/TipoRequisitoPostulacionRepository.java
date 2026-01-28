package com.sgac.repository;

import com.sgac.entity.TipoRequisitoPostulacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TipoRequisitoPostulacionRepository extends JpaRepository<TipoRequisitoPostulacion, Integer> {
    List<TipoRequisitoPostulacion> findByActivo(Boolean activo);
}
