package org.uteq.sgacfinal.repository;

import org.uteq.sgacfinal.entity.Coordinador;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CoordinadorRepository extends JpaRepository<Coordinador, Integer> {
    List<Coordinador> findByActivo(Boolean activo);
    List<Coordinador> findByCarreraIdCarrera(Integer idCarrera);
}
