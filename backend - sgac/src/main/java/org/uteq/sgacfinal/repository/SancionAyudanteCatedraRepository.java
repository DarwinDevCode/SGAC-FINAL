package org.uteq.sgacfinal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.uteq.sgacfinal.entity.SancionAyudanteCatedra;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface SancionAyudanteCatedraRepository extends JpaRepository<SancionAyudanteCatedra, Integer> {

    @Query(value = "SELECT public.sp_crear_sancion(:idTipo, :idAyudante, :fecha, :motivo)", nativeQuery = true)
    Integer registrarSancion(@Param("idTipo") Integer idTipoSancion,
                             @Param("idAyudante") Integer idAyudante,
                             @Param("fecha") LocalDate fechaSancion,
                             @Param("motivo") String motivo);

    @Query(value = "SELECT public.sp_actualizar_sancion(:id, :motivo, :fecha)", nativeQuery = true)
    Integer actualizarSancion(@Param("id") Integer idSancion,
                              @Param("motivo") String motivo,
                              @Param("fecha") LocalDate fechaSancion);

    @Query(value = "SELECT public.sp_desactivar_sancion(:id)", nativeQuery = true)
    Integer desactivarSancion(@Param("id") Integer idSancion);

    @Query(value = "SELECT * FROM public.sp_listar_sanciones_ayudante(:idAyudante)", nativeQuery = true)
    List<SancionAyudanteCatedra> listarSancionesPorAyudanteSP(@Param("idAyudante") Integer idAyudante);
}