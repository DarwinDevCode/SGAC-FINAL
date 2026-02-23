package org.uteq.sgacfinal.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.uteq.sgacfinal.entity.TipoRol;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ITipoRolRepository extends JpaRepository<TipoRol, Integer> {
    @Query(value = "SELECT public.fn_actualizar_tipo_rol(:id, :nombre)", nativeQuery = true)
    Integer actualizarTipoRol(@Param("id") Integer id, @Param("nombre") String nombre);

    @Query(value = "SELECT public.fn_crear_tipo_rol(:nombre)", nativeQuery = true)
    Integer crearTipoRol(@Param("nombre") String nombre);

    @Query(value = "SELECT public.fn_desactivar_tipo_rol(:id)", nativeQuery = true)
    Integer desactivarTipoRol(@Param("id") Integer id);

    @Query(value = "SELECT seguridad.fn_gestionar_permiso_rol(:nombreRol, :esquema, :tabla, :privilegio, :otorgar)", nativeQuery = true)
    Boolean gestionarPermisoRol(
            @Param("nombreRol") String nombreRol,
            @Param("esquema") String esquema,
            @Param("tabla") String tabla,
            @Param("privilegio") String privilegio,
            @Param("otorgar") Boolean otorgar
    );

    Optional<TipoRol> findByNombreTipoRol(String nombreTipoRol);
    List<TipoRol> findByActivo(Boolean activo);
    List<TipoRol> findByActivoTrue();
    boolean existsByNombreTipoRol(String nombreTipoRol);
}
