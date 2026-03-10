package org.uteq.sgacfinal.repository.catalogo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.uteq.sgacfinal.entity.Privilegio;

/**
 * Repositorio para operaciones CRUD sobre seguridad.privilegio
 */
@Repository
public interface PrivilegioCatalogoRepository extends JpaRepository<Privilegio, Integer> {

    @Query(value = "SELECT seguridad.fn_listar_privilegio()", nativeQuery = true)
    String listar();

    @Query(value = "SELECT seguridad.fn_crear_privilegio(:nombrePrivilegio, :codigoInterno, :descripcion)", nativeQuery = true)
    String crear(@Param("nombrePrivilegio") String nombrePrivilegio,
                 @Param("codigoInterno") String codigoInterno,
                 @Param("descripcion") String descripcion);

    @Query(value = "SELECT seguridad.fn_actualizar_privilegio(:id, :nombrePrivilegio, :codigoInterno, :descripcion)", nativeQuery = true)
    String actualizar(@Param("id") Integer id,
                      @Param("nombrePrivilegio") String nombrePrivilegio,
                      @Param("codigoInterno") String codigoInterno,
                      @Param("descripcion") String descripcion);

    @Query(value = "SELECT seguridad.fn_eliminar_privilegio(:id)", nativeQuery = true)
    String eliminar(@Param("id") Integer id);
}

