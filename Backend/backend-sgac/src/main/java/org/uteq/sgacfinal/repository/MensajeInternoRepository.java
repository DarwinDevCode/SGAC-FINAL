package org.uteq.sgacfinal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.uteq.sgacfinal.entity.MensajeInterno;
import java.util.List;

@Repository
public interface MensajeInternoRepository extends JpaRepository<MensajeInterno, Integer> {
    List<MensajeInterno> findByAyudantiaIdAyudantiaOrderByFechaEnvioAsc(Integer idAyudantia);

    /**
     * Búsqueda eficiente en BD usando ILIKE (PostgreSQL) para insensibilidad de mayúsculas.
     * Evita cargar todos los mensajes en memoria para luego filtrar en Java.
     */
    @Query(value = """
        SELECT m.* FROM comunicacion.mensaje_interno m
        WHERE m.id_ayudantia = :idAyudantia
          AND m.mensaje ILIKE CONCAT('%', :criterio, '%')
        ORDER BY m.fecha_envio ASC
        """, nativeQuery = true)
    List<MensajeInterno> buscarPorCriterio(
            @Param("idAyudantia") Integer idAyudantia,
            @Param("criterio") String criterio
    );
}
