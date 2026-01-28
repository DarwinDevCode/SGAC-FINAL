package com.sgac.repository;

import com.sgac.entity.TipoPlazoActividad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TipoPlazoActividadRepository extends JpaRepository<TipoPlazoActividad, Integer> {
}
