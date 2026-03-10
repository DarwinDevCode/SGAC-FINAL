package org.uteq.sgacfinal.repository.catalogo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.uteq.sgacfinal.entity.TipoEstadoAyudantia;

/**
 * Repositorio para operaciones CRUD sobre ayudantia.tipo_estado_ayudantia
 */
@Repository
public interface TipoEstadoAyudantiaCatalogoRepository extends JpaRepository<TipoEstadoAyudantia, Integer> {

    @Query(value = "SELECT ayudantia.fn_listar_tipo_estado_ayudantia()", nativeQuery = true)
    String listar();

    @Query(value = "SELECT ayudantia.fn_crear_tipo_estado_ayudantia(:nombreEstado, :descripcion, :codigo)", nativeQuery = true)
    String crear(@Param("nombreEstado") String nombreEstado,
                 @Param("descripcion") String descripcion,
                 @Param("codigo") String codigo);

    @Query(value = "SELECT ayudantia.fn_actualizar_tipo_estado_ayudantia(:id, :nombreEstado, :descripcion, :codigo)", nativeQuery = true)
    String actualizar(@Param("id") Integer id,
                      @Param("nombreEstado") String nombreEstado,
                      @Param("descripcion") String descripcion,
                      @Param("codigo") String codigo);

    @Query(value = "SELECT ayudantia.fn_eliminar_tipo_estado_ayudantia(:id)", nativeQuery = true)
    String eliminar(@Param("id") Integer id);
}

