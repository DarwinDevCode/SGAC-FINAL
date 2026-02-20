package org.uteq.sgacfinal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.uteq.sgacfinal.entity.Carrera;

import java.util.List;

@Repository
public interface CarreraRepository extends JpaRepository<Carrera, Integer> {

    @Query(value = "SELECT public.fn_crear_carrera(:idFacultad, :nombre)", nativeQuery = true)
    Integer registrarCarrera(@Param("idFacultad") Integer idFacultad,
                             @Param("nombre") String nombreCarrera);

    @Query(value = "SELECT public.fn_actualizar_carrera(:id, :idFacultad, :nombre)", nativeQuery = true)
    Integer actualizarCarrera(@Param("id") Integer idCarrera,
                              @Param("idFacultad") Integer idFacultad,
                              @Param("nombre") String nombreCarrera);

    @Query(value = "SELECT public.fn_desactivar_carrera(:id)", nativeQuery = true)
    Integer desactivarCarrera(@Param("id") Integer idCarrera);

    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.transaction.annotation.Transactional
    @Query(value = "UPDATE carrera SET activo = true WHERE id_carrera = :id", nativeQuery = true)
    int activarCarrera(@Param("id") Integer idCarrera);

    List<Carrera> findByFacultad_IdFacultad(Integer idFacultad);
}