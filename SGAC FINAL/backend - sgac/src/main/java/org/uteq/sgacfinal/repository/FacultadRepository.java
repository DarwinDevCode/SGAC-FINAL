package org.uteq.sgacfinal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.uteq.sgacfinal.entity.Facultad;

@Repository
public interface FacultadRepository extends JpaRepository<Facultad, Integer> {

    @Query(value = "SELECT public.fn_crear_facultad(:nombre)", nativeQuery = true)
    Integer registrarFacultad(@Param("nombre") String nombreFacultad);

    @Query(value = "SELECT public.fn_actualizar_facultad(:id, :nombre)", nativeQuery = true)
    Integer actualizarFacultad(@Param("id") Integer idFacultad,
                               @Param("nombre") String nombreFacultad);

    @Query(value = "SELECT public.fn_desactivar_facultad(:id)", nativeQuery = true)
    Integer desactivarFacultad(@Param("id") Integer idFacultad);
}