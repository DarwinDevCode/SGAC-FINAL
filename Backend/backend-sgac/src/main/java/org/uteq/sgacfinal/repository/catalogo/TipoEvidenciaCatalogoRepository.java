package org.uteq.sgacfinal.repository.catalogo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.uteq.sgacfinal.entity.TipoEvidencia;

/**
 * Repositorio para operaciones CRUD sobre ayudantia.tipo_evidencia
 */
@Repository
public interface TipoEvidenciaCatalogoRepository extends JpaRepository<TipoEvidencia, Integer> {

    @Query(value = "SELECT ayudantia.fn_listar_tipo_evidencia()", nativeQuery = true)
    String listar();

    @Query(value = "SELECT ayudantia.fn_crear_tipo_evidencia(:nombre, :extensionPermitida, :codigo)", nativeQuery = true)
    String crear(@Param("nombre") String nombre,
                 @Param("extensionPermitida") String extensionPermitida,
                 @Param("codigo") String codigo);

    @Query(value = "SELECT ayudantia.fn_actualizar_tipo_evidencia(:id, :nombre, :extensionPermitida, :codigo)", nativeQuery = true)
    String actualizar(@Param("id") Integer id,
                      @Param("nombre") String nombre,
                      @Param("extensionPermitida") String extensionPermitida,
                      @Param("codigo") String codigo);

    @Query(value = "SELECT ayudantia.fn_eliminar_tipo_evidencia(:id)", nativeQuery = true)
    String eliminar(@Param("id") Integer id);
}

