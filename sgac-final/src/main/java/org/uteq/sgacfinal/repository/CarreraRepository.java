package org.uteq.sgacfinal.repository;

import org.uteq.sgacfinal.entity.Carrera;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CarreraRepository extends JpaRepository<Carrera, Integer> {
    List<Carrera> findByFacultadIdFacultad(Integer idFacultad);
}
