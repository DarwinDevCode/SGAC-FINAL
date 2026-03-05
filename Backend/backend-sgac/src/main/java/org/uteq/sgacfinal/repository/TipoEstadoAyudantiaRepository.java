package org.uteq.sgacfinal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.uteq.sgacfinal.entity.TipoEstadoAyudantia;

import java.util.List;

public interface TipoEstadoAyudantiaRepository extends JpaRepository<TipoEstadoAyudantia,Integer> {
    List<TipoEstadoAyudantia> findByActivoTrue();
}
