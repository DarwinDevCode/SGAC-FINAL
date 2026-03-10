package org.uteq.sgacfinal.repository.catalogo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.uteq.sgacfinal.entity.TipoEstadoRequisito;

/**
 * Repositorio para operaciones CRUD sobre convocatoria.tipo_estado_requisito
 */
@Repository
public interface TipoEstadoRequisitoCatalogoRepository extends JpaRepository<TipoEstadoRequisito, Integer> {

    @Query(value = "SELECT convocatoria.fn_listar_tipo_estado_requisito()", nativeQuery = true)
    String listar();

    @Query(value = "SELECT convocatoria.fn_crear_tipo_estado_requisito(:nombreEstado, :descripcion, :codigo)", nativeQuery = true)
    String crear(@Param("nombreEstado") String nombreEstado,
                 @Param("descripcion") String descripcion,
                 @Param("codigo") String codigo);

    @Query(value = "SELECT convocatoria.fn_actualizar_tipo_estado_requisito(:id, :nombreEstado, :descripcion, :codigo)", nativeQuery = true)
    String actualizar(@Param("id") Integer id,
                      @Param("nombreEstado") String nombreEstado,
                      @Param("descripcion") String descripcion,
                      @Param("codigo") String codigo);

    @Query(value = "SELECT convocatoria.fn_eliminar_tipo_estado_requisito(:id)", nativeQuery = true)
    String eliminar(@Param("id") Integer id);
}

