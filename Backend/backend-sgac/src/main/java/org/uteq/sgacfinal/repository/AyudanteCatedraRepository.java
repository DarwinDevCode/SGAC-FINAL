package org.uteq.sgacfinal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.uteq.sgacfinal.entity.AyudanteCatedra;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface AyudanteCatedraRepository extends JpaRepository<AyudanteCatedra, Integer> {

    @Query(value = "SELECT public.sp_crear_ayudante_catedra(:idUsuario, :horas)", nativeQuery = true)
    Integer registrarAyudante(@Param("idUsuario") Integer idUsuario,
                              @Param("horas") BigDecimal horasAyudante);

    @Query(value = "SELECT public.sp_actualizar_ayudante_catedra(:id, :idUsuario, :horas)", nativeQuery = true)
    Integer actualizarAyudante(@Param("id") Integer idAyudanteCatedra,
                               @Param("idUsuario") Integer idUsuario,
                               @Param("horas") BigDecimal horasAyudante);

    @Query(value = "SELECT * FROM public.sp_obtener_ayudantes_catedra()", nativeQuery = true)
    List<AyudanteCatedra> obtenerTodosSP();

    Optional<AyudanteCatedra> findByUsuario_IdUsuario(Integer idUsuario);


}