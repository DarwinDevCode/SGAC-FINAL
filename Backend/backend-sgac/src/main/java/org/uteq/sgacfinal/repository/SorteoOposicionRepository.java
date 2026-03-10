package org.uteq.sgacfinal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.uteq.sgacfinal.entity.SorteoOposicion;

import java.util.Optional;

@Repository
public interface SorteoOposicionRepository extends JpaRepository<SorteoOposicion, Integer> {

    @Query("SELECT s FROM SorteoOposicion s WHERE s.postulacion.idPostulacion = :idPostulacion")
    Optional<SorteoOposicion> findByIdPostulacion(@Param("idPostulacion") Integer idPostulacion);
}
