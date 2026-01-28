package com.sgac.repository;

import com.sgac.entity.TipoEstadoEvidenciaAyudantia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TipoEstadoEvidenciaAyudantiaRepository extends JpaRepository<TipoEstadoEvidenciaAyudantia, Integer> {
}
