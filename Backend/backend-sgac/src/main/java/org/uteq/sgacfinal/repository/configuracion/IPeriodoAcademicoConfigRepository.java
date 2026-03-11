package org.uteq.sgacfinal.repository.configuracion;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.uteq.sgacfinal.entity.PeriodoAcademico;

import java.time.LocalDate;

@Repository
public interface IPeriodoAcademicoConfigRepository extends JpaRepository<PeriodoAcademico, Integer> {

    @Query(value = "SELECT academico.fn_abrir_periodo_academico(:nombre, :inicio, :fin)",
            nativeQuery = true)
    String abrirPeriodo(
            @Param("nombre") String nombre,
            @Param("inicio") LocalDate inicio,
            @Param("fin")    LocalDate fin
    );

    @Query(value = "SELECT academico.fn_iniciar_periodo_academico(:idPeriodo)",
            nativeQuery = true)
    String iniciarPeriodo(@Param("idPeriodo") Integer idPeriodo);

    @Query(value = "SELECT jsonb_build_object(" +
            "'id',     id_periodo_academico, " +
            "'nombre', nombre_periodo, " +
            "'estado', estado, " +
            "'activo', activo) " +
            "FROM academico.periodo_academico WHERE id_periodo_academico = :id",
            nativeQuery = true)
    String obtenerEstadoPeriodo(@Param("id") Integer id);
}