package org.uteq.sgacfinal.repository;

import org.uteq.sgacfinal.entity.SancionAyudanteCatedra;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SancionAyudanteCatedraRepository extends JpaRepository<SancionAyudanteCatedra, Integer> {
    List<SancionAyudanteCatedra> findByAyudanteCatedraIdAyudanteCatedra(Integer idAyudanteCatedra);
    List<SancionAyudanteCatedra> findByActivo(Boolean activo);
}
