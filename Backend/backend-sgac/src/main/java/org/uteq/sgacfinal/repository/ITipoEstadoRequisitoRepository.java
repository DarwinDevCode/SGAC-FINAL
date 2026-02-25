package org.uteq.sgacfinal.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.uteq.sgacfinal.entity.TipoEstadoRequisito;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ITipoEstadoRequisitoRepository extends JpaRepository<TipoEstadoRequisito, Integer> {
    @Query(value = "SELECT public.fn_actualizar_tipo_estado_requisito(:id, :nombre, :descripcion)", nativeQuery = true)
    Integer actualizarTipoEstadoRequisito(@Param("id") Integer id,
                                          @Param("nombre") String nombre,
                                          @Param("descripcion") String descripcion);

    @Query(value = "SELECT public.fn_crear_tipo_estado_requisito(:nombre, :descripcion)", nativeQuery = true)
    Integer crearTipoEstadoRequisito(@Param("nombre") String nombre,
                                     @Param("descripcion")  String descripcion);

    @Query(value = "SELECT public.fn_desactivar_tipo_estado_requisito(:id)", nativeQuery = true)
    Integer desactivarTipoEstadoRequisito(@Param("id") Integer id);

    List<TipoEstadoRequisito> findAllByOrderByNombreEstadoAsc();
}
