package com.sgac.repository;

import com.sgac.entity.EvaluacionMeritos;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface EvaluacionMeritosRepository extends JpaRepository<EvaluacionMeritos, Integer> {
    Optional<EvaluacionMeritos> findByPostulacionIdPostulacion(Integer idPostulacion);
}
