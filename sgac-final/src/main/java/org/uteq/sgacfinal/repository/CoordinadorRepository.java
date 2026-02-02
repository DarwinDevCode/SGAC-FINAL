package org.uteq.sgacfinal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.uteq.sgacfinal.entity.Coordinador;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface CoordinadorRepository extends JpaRepository<Coordinador, Integer> {

    @Query(value = "SELECT public.sp_crear_coordinador(:idUsuario, :idCarrera, :inicio, :fin)", nativeQuery = true)
    Integer registrarCoordinador(@Param("idUsuario") Integer idUsuario,
                                 @Param("idCarrera") Integer idCarrera,
                                 @Param("inicio") LocalDate fechaInicio,
                                 @Param("fin") LocalDate fechaFin);

    @Query(value = "SELECT public.sp_actualizar_coordinador(:id, :idCarrera, :inicio, :fin)", nativeQuery = true)
    Integer actualizarCoordinador(@Param("id") Integer idCoordinador,
                                  @Param("idCarrera") Integer idCarrera,
                                  @Param("inicio") LocalDate fechaInicio,
                                  @Param("fin") LocalDate fechaFin);

    @Query(value = "SELECT public.sp_desactivar_coordinador(:id)", nativeQuery = true)
    Integer desactivarCoordinador(@Param("id") Integer idCoordinador);

    @Query(value = "SELECT * FROM public.sp_listar_coordinadores()", nativeQuery = true)
    List<Object[]> listarCoordinadoresSP();

    Optional<Coordinador> findByUsuario_IdUsuarioAndActivoTrue(Integer idUsuario);
}