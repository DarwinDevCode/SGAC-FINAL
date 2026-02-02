package org.uteq.sgacfinal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.uteq.sgacfinal.entity.Decano;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface DecanoRepository extends JpaRepository<Decano, Integer> {

    @Query(value = "SELECT public.sp_crear_decano(:idUsuario, :idFacultad, :inicio, :fin)", nativeQuery = true)
    Integer registrarDecano(@Param("idUsuario") Integer idUsuario,
                            @Param("idFacultad") Integer idFacultad,
                            @Param("inicio") LocalDate fechaInicio,
                            @Param("fin") LocalDate fechaFin);

    @Query(value = "SELECT public.sp_actualizar_decano(:id, :idFacultad, :inicio, :fin)", nativeQuery = true)
    Integer actualizarDecano(@Param("id") Integer idDecano,
                             @Param("idFacultad") Integer idFacultad,
                             @Param("inicio") LocalDate fechaInicio,
                             @Param("fin") LocalDate fechaFin);

    @Query(value = "SELECT public.sp_desactivar_decano(:id)", nativeQuery = true)
    Integer desactivarDecano(@Param("id") Integer idDecano);

    @Query(value = "SELECT * FROM public.sp_obtener_decanos_activos()", nativeQuery = true)
    List<Decano> obtenerDecanosActivosSP();

    Optional<Decano> findByUsuario_IdUsuarioAndActivoTrue(Integer idUsuario);
}