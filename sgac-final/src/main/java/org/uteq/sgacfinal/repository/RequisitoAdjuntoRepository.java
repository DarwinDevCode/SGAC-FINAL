package org.uteq.sgacfinal.repository;

import org.uteq.sgacfinal.entity.RequisitoAdjunto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RequisitoAdjuntoRepository extends JpaRepository<RequisitoAdjunto, Integer> {
    List<RequisitoAdjunto> findByPostulacionIdPostulacion(Integer idPostulacion);
}
