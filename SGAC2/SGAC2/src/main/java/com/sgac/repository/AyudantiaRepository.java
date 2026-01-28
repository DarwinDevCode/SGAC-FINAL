package com.sgac.repository;

import com.sgac.entity.Ayudantia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AyudantiaRepository extends JpaRepository<Ayudantia, Integer> {
    List<Ayudantia> findByPostulacionIdPostulacion(Integer idPostulacion);
}
