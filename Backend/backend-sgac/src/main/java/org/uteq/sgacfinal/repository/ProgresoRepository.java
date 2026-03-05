package org.uteq.sgacfinal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.uteq.sgacfinal.entity.Ayudantia;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProgresoRepository extends JpaRepository<Ayudantia, Integer> {
    @Query(value = """
        SELECT * FROM ayudantia.fn_progreso_general(:idUsuario)
        """, nativeQuery = true)
    List<Object[]> progresoGeneral(@Param("idUsuario") Integer idUsuario);

    @Query(value = """
        SELECT * FROM ayudantia.fn_control_semanal(:idUsuario)
        """, nativeQuery = true)
    List<Object[]> controlSemanal(@Param("idUsuario") Integer idUsuario);
}
