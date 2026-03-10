package org.uteq.sgacfinal.repository.catalogo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.uteq.sgacfinal.entity.TipoEstadoEvidencia;

/**
 * Repositorio para operaciones CRUD sobre ayudantia.tipo_estado_evidencia
 */
@Repository
public interface TipoEstadoEvidenciaCatalogoRepository extends JpaRepository<TipoEstadoEvidencia, Integer> {

    @Query(value = "SELECT ayudantia.fn_listar_tipo_estado_evidencia()", nativeQuery = true)
    String listar();

    @Query(value = "SELECT ayudantia.fn_crear_tipo_estado_evidencia(:nombreEstado, :descripcion, :codigo)", nativeQuery = true)
    String crear(@Param("nombreEstado") String nombreEstado,
                 @Param("descripcion") String descripcion,
                 @Param("codigo") String codigo);

    @Query(value = "SELECT ayudantia.fn_actualizar_tipo_estado_evidencia(:id, :nombreEstado, :descripcion, :codigo)", nativeQuery = true)
    String actualizar(@Param("id") Integer id,
                      @Param("nombreEstado") String nombreEstado,
                      @Param("descripcion") String descripcion,
                      @Param("codigo") String codigo);

    @Query(value = "SELECT ayudantia.fn_eliminar_tipo_estado_evidencia(:id)", nativeQuery = true)
    String eliminar(@Param("id") Integer id);
}

