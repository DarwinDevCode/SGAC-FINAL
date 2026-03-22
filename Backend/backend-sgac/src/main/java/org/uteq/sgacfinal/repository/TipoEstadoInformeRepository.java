package org.uteq.sgacfinal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.uteq.sgacfinal.entity.TipoEstadoInforme;

import java.util.Optional;

@Repository
public interface TipoEstadoInformeRepository extends JpaRepository<TipoEstadoInforme, Integer> {
    Optional<TipoEstadoInforme> findByCodigo(String codigo);
}
