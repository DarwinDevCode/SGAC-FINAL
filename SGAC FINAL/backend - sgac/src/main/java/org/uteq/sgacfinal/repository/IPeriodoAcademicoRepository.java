package org.uteq.sgacfinal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.uteq.sgacfinal.entity.PeriodoAcademico;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface IPeriodoAcademicoRepository extends JpaRepository<PeriodoAcademico, Integer> {

    @Query(value = "SELECT public.fn_crear_periodo_academico(:nombre, :inicio, :fin, :estado)", nativeQuery = true)
    Integer registrarPeriodo(@Param("nombre") String nombrePeriodo,
                             @Param("inicio") LocalDate fechaInicio,
                             @Param("fin") LocalDate fechaFin,
                             @Param("estado") String estado);

    @Query(value = "SELECT public.fn_actualizar_periodo_academico(:id, :nombre, :inicio, :fin, :estado)", nativeQuery = true)
    Integer actualizarPeriodo(@Param("id") Integer idPeriodo,
                              @Param("nombre") String nombrePeriodo,
                              @Param("inicio") LocalDate fechaInicio,
                              @Param("fin") LocalDate fechaFin,
                              @Param("estado") String estado);

    @Query(value = "SELECT public.fn_desactivar_periodo_academico(:id)", nativeQuery = true)
    Integer desactivarPeriodo(@Param("id") Integer idPeriodo);

    List<PeriodoAcademico> findByEstado(String estado);
}