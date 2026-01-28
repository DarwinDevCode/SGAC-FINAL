package org.uteq.sgacfinal.repository;

import org.uteq.sgacfinal.entity.TipoSancionAyudanteCatedra;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TipoSancionAyudanteCatedraRepository extends JpaRepository<TipoSancionAyudanteCatedra, Integer> {
    List<TipoSancionAyudanteCatedra> findByActivo(Boolean activo);
}
