package org.uteq.sgacfinal.repository.catalogo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.uteq.sgacfinal.entity.TipoSancionAyudanteCatedra;

/**
 * Repositorio para operaciones CRUD sobre ayudantia.tipo_sancion_ayudante_catedra
 * utilizando funciones de PostgreSQL que retornan JSONB.
 */
@Repository
public interface TipoSancionCatalogoRepository extends JpaRepository<TipoSancionAyudanteCatedra, Integer> {

    @Query(value = "SELECT ayudantia.fn_listar_tipo_sancion_ayudante_catedra()", nativeQuery = true)
    String listar();

    @Query(value = "SELECT ayudantia.fn_crear_tipo_sancion_ayudante_catedra(:nombreTipoSancion, :codigo)", nativeQuery = true)
    String crear(@Param("nombreTipoSancion") String nombreTipoSancion,
                 @Param("codigo") String codigo);

    @Query(value = "SELECT ayudantia.fn_actualizar_tipo_sancion_ayudante_catedra(:id, :nombreTipoSancion, :codigo)", nativeQuery = true)
    String actualizar(@Param("id") Integer id,
                      @Param("nombreTipoSancion") String nombreTipoSancion,
                      @Param("codigo") String codigo);

    @Query(value = "SELECT ayudantia.fn_eliminar_tipo_sancion_ayudante_catedra(:id)", nativeQuery = true)
    String eliminar(@Param("id") Integer id);
}

