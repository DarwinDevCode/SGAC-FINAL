package org.uteq.sgacfinal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.uteq.sgacfinal.entity.TipoEstadoEvidencia;

import java.util.List;
@Repository
public interface TipoEstadoEvidenciaRepository extends JpaRepository<TipoEstadoEvidencia,Integer> {
    List<TipoEstadoEvidencia> findByActivoTrue();
}
