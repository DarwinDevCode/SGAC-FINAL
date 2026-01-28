package org.uteq.sgacfinal.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

import java.util.List;

public interface IPermisoRepository extends Repository<Object, Long> {

    @Query(value = "SELECT * FROM fn_permisos_actuales()", nativeQuery = true)
    List<Object[]> obtenerPermisosActuales();
}
