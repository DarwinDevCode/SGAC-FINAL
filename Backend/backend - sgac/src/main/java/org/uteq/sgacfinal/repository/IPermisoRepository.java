package org.uteq.sgacfinal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.uteq.sgacfinal.entity.Usuario;

import java.util.List;

@Repository
public interface IPermisoRepository extends JpaRepository<Usuario, Integer> {

    @Query(value = "SELECT * FROM fn_permisos_actuales()", nativeQuery = true)
    List<Object[]> obtenerPermisosActuales();


    @Query(value = "SELECT esquema, elemento, categoria, privilegio " +
            "FROM seguridad.fn_consultar_permisos_rol(:rolBd, :esquema, :categoria, :privilegio)",
            nativeQuery = true)
    List<Object[]> consultarPermisosRolRaw(
            @Param("rolBd") String rolBd,
            @Param("esquema") String esquema,
            @Param("categoria") String categoria,
            @Param("privilegio") String privilegio
    );


    @Query(value = "SELECT seguridad.fn_gestionar_permisos_elemento(" +
            ":rolBd, :esquema, :elemento, :categoria, :privilegio, :otorgar)",
            nativeQuery = true)
    Boolean gestionarPermisoElementoRaw(
            @Param("rolBd") String rolBd,
            @Param("esquema") String esquema,
            @Param("elemento") String elemento,
            @Param("categoria") String categoria,
            @Param("privilegio") String privilegio,
            @Param("otorgar") Boolean otorgar
    );
}
