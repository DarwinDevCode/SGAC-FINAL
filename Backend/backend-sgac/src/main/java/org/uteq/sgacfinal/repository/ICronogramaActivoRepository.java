package org.uteq.sgacfinal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.uteq.sgacfinal.entity.PeriodoFase;

@Repository
public interface ICronogramaActivoRepository extends JpaRepository<PeriodoFase, Integer> {
    @Query(value = "SELECT planificacion.fn_obtener_cronograma_activo()", nativeQuery = true)
    String obtenerCronogramaActivo();
}
