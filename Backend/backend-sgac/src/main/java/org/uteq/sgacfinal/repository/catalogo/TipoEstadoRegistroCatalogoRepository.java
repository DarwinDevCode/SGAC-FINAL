package org.uteq.sgacfinal.repository.catalogo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.uteq.sgacfinal.entity.TipoEstadoRegistro;

/**
 * Repositorio para operaciones CRUD sobre ayudantia.tipo_estado_registro
 */
@Repository
public interface TipoEstadoRegistroCatalogoRepository extends JpaRepository<TipoEstadoRegistro, Integer> {

    @Query(value = "SELECT ayudantia.fn_listar_tipo_estado_registro()", nativeQuery = true)
    String listar();

    @Query(value = "SELECT ayudantia.fn_crear_tipo_estado_registro(:nombreEstado, :descripcion, :codigo)", nativeQuery = true)
    String crear(@Param("nombreEstado") String nombreEstado,
                 @Param("descripcion") String descripcion,
                 @Param("codigo") String codigo);

    @Query(value = "SELECT ayudantia.fn_actualizar_tipo_estado_registro(:id, :nombreEstado, :descripcion, :codigo)", nativeQuery = true)
    String actualizar(@Param("id") Integer id,
                      @Param("nombreEstado") String nombreEstado,
                      @Param("descripcion") String descripcion,
                      @Param("codigo") String codigo);

    @Query(value = "SELECT ayudantia.fn_eliminar_tipo_estado_registro(:id)", nativeQuery = true)
    String eliminar(@Param("id") Integer id);
}

