package com.sgac.repository;

import com.sgac.entity.EvaluacionOposicion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface EvaluacionOposicionRepository extends JpaRepository<EvaluacionOposicion, Integer> {
    Optional<EvaluacionOposicion> findByPostulacionIdPostulacion(Integer idPostulacion);
}
