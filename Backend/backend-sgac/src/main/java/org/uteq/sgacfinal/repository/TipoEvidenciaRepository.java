package org.uteq.sgacfinal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.uteq.sgacfinal.entity.TipoEvidencia;

import java.util.List;
@Repository
public interface TipoEvidenciaRepository extends JpaRepository<TipoEvidencia,Integer> {
    List<TipoEvidencia> findByActivoTrue();
}
