package org.uteq.sgacfinal.repository.catalogo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.uteq.sgacfinal.entity.TipoEstadoPostulacion;

/**
 * Repositorio para operaciones CRUD sobre postulacion.tipo_estado_postulacion
 */
@Repository
public interface TipoEstadoPostulacionCatalogoRepository extends JpaRepository<TipoEstadoPostulacion, Integer> {

    @Query(value = "SELECT postulacion.fn_listar_tipo_estado_postulacion()", nativeQuery = true)
    String listar();

    @Query(value = "SELECT postulacion.fn_crear_tipo_estado_postulacion(:codigo, :nombre, :descripcion)", nativeQuery = true)
    String crear(@Param("codigo") String codigo,
                 @Param("nombre") String nombre,
                 @Param("descripcion") String descripcion);

    @Query(value = "SELECT postulacion.fn_actualizar_tipo_estado_postulacion(:id, :codigo, :nombre, :descripcion)", nativeQuery = true)
    String actualizar(@Param("id") Integer id,
                      @Param("codigo") String codigo,
                      @Param("nombre") String nombre,
                      @Param("descripcion") String descripcion);

    @Query(value = "SELECT postulacion.fn_eliminar_tipo_estado_postulacion(:id)", nativeQuery = true)
    String eliminar(@Param("id") Integer id);
}

