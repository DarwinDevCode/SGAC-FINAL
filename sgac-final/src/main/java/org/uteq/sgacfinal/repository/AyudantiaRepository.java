package org.uteq.sgacfinal.repository;

import org.uteq.sgacfinal.entity.Ayudantia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AyudantiaRepository extends JpaRepository<Ayudantia, Integer> {
    List<Ayudantia> findByPostulacionIdPostulacion(Integer idPostulacion);
}
