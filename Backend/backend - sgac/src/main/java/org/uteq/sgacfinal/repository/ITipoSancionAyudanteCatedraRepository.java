package org.uteq.sgacfinal.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.uteq.sgacfinal.entity.TipoSancionAyudanteCatedra;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ITipoSancionAyudanteCatedraRepository extends JpaRepository<TipoSancionAyudanteCatedra, Integer> {
    List<TipoSancionAyudanteCatedra> findByActivo(Boolean activo);
    List<TipoSancionAyudanteCatedra> findByActivoTrue();

    @Query(value = "SELECT public.fn_actualizar_tipo_sancion_ayudante_catedra(:id, :nombre)", nativeQuery = true)
    Integer actualizarTipoSancionAyudanteCatedra(@Param("id") Integer id, @Param("nombre") String nombre);

    @Query(value = "SELECT public.fn_crear_tipo_sancion_ayudante_catedra(:nombre)", nativeQuery = true)
    Integer crearTipoSancionAyudanteCatedra(@Param("nombre") String nombre);

    @Query(value = "SELECT public.fn_desactivar_tipo_sancion_ayudante_catedra(:id)", nativeQuery = true)
    Integer desactivarTipoSancionAyudanteCatedra(@Param("id") Integer id);
}
