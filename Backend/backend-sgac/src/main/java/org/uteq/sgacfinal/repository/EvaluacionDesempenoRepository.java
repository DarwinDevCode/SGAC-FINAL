package org.uteq.sgacfinal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.uteq.sgacfinal.entity.EvaluacionDesempeno;
import java.util.Optional;

@Repository
public interface EvaluacionDesempenoRepository extends JpaRepository<EvaluacionDesempeno, Integer> {
    Optional<EvaluacionDesempeno> findByRegistroActividadIdRegistroActividad(Integer idRegistroActividad);
}
