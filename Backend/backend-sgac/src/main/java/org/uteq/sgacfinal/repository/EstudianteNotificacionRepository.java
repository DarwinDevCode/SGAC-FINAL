package org.uteq.sgacfinal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.uteq.sgacfinal.entity.Estudiante;

import java.util.List;

@Repository
public interface EstudianteNotificacionRepository extends JpaRepository<Estudiante, Integer> {
    @Query(value = "SELECT DISTINCT e.id_usuario " +
            "FROM academico.estudiante e " +
            "JOIN academico.carrera c ON e.id_carrera = c.id_carrera " +
            "WHERE c.id_carrera = :idCarrera " +
            "AND e.id_usuario IS NOT NULL " +
            "AND (e.estado_academico IS NULL OR UPPER(e.estado_academico) <> 'INACTIVO')",
            nativeQuery = true)
    List<Integer> findIdsUsuarioActivosByCarrera(@Param("idCarrera") Integer idCarrera);
}
