package org.uteq.sgacfinal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.uteq.sgacfinal.entity.Ayudantia;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface AyudantiaRepository extends JpaRepository<Ayudantia, Integer> {

    @Query(value = "SELECT public.sp_crear_ayudantia(:idEstado, :idPostulacion, :inicio, :fin, :horas)", nativeQuery = true)
    Integer registrarAyudantia(@Param("idEstado") Integer idTipoEstado,
                               @Param("idPostulacion") Integer idPostulacion,
                               @Param("inicio") LocalDate fechaInicio,
                               @Param("fin") LocalDate fechaFin,
                               @Param("horas") Integer horasCumplidas);

    @Query(value = "SELECT public.sp_actualizar_ayudantia(:id, :idEstado, :inicio, :fin, :horas)", nativeQuery = true)
    Integer actualizarAyudantia(@Param("id") Integer idAyudantia,
                                @Param("idEstado") Integer idTipoEstado,
                                @Param("inicio") LocalDate fechaInicio,
                                @Param("fin") LocalDate fechaFin,
                                @Param("horas") Integer horasCumplidas);

    @Query(value = "SELECT * FROM public.sp_obtener_ayudantia_por_id(:id)", nativeQuery = true)
    Optional<Ayudantia> buscarPorIdSP(@Param("id") Integer id);
}