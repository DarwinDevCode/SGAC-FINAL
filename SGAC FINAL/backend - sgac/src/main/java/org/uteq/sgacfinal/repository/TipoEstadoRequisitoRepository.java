package org.uteq.sgacfinal.repository;

import org.uteq.sgacfinal.entity.TipoEstadoRequisito;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TipoEstadoRequisitoRepository extends JpaRepository<TipoEstadoRequisito, Integer> {
}
