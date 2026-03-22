package org.uteq.sgacfinal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.uteq.sgacfinal.entity.InformeMensual;

import java.util.List;
import java.util.Optional;

@Repository
public interface InformeMensualRepository extends JpaRepository<InformeMensual, Integer> {
    
    Optional<InformeMensual> findByAyudantia_IdAyudantiaAndMesAndAnio(Integer idAyudantia, Integer mes, Integer anio);
    
    List<InformeMensual> findByAyudantia_IdAyudantia(Integer idAyudantia);
}
