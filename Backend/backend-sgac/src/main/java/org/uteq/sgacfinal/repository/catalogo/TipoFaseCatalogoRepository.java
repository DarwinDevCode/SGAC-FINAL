package org.uteq.sgacfinal.repository.catalogo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.uteq.sgacfinal.entity.TipoFase;

/**
 * Repositorio para operaciones CRUD sobre planificacion.tipo_fase
 */
@Repository
public interface TipoFaseCatalogoRepository extends JpaRepository<TipoFase, Integer> {

    @Query(value = "SELECT planificacion.fn_listar_tipo_fase()", nativeQuery = true)
    String listar();

    @Query(value = "SELECT planificacion.fn_crear_tipo_fase(:codigo, :nombre, :descripcion, :orden)", nativeQuery = true)
    String crear(@Param("codigo") String codigo,
                 @Param("nombre") String nombre,
                 @Param("descripcion") String descripcion,
                 @Param("orden") Integer orden);

    @Query(value = "SELECT planificacion.fn_actualizar_tipo_fase(:id, :codigo, :nombre, :descripcion, :orden)", nativeQuery = true)
    String actualizar(@Param("id") Integer id,
                      @Param("codigo") String codigo,
                      @Param("nombre") String nombre,
                      @Param("descripcion") String descripcion,
                      @Param("orden") Integer orden);

    @Query(value = "SELECT planificacion.fn_eliminar_tipo_fase(:id)", nativeQuery = true)
    String eliminar(@Param("id") Integer id);
}

