package org.uteq.sgacfinal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.uteq.sgacfinal.entity.TipoEstadoEvidenciaAyudantia;

@Repository
public interface TipoEstadoEvidenciaAyudantiaRepository extends JpaRepository<TipoEstadoEvidenciaAyudantia, Integer> {

}