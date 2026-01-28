package org.uteq.sgacfinal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.uteq.sgacfinal.entity.Usuario;

import java.util.List;

@Repository
public interface IPermisoRepository extends JpaRepository<Usuario, Integer> {

    @Query(value = "SELECT * FROM fn_permisos_actuales()", nativeQuery = true)
    List<Object[]> obtenerPermisosActuales();
}
