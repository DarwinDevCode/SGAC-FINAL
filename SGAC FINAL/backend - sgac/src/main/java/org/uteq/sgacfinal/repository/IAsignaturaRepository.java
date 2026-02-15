package org.uteq.sgacfinal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.uteq.sgacfinal.entity.Asignatura;

import java.util.List;

@Repository
public interface IAsignaturaRepository extends JpaRepository<Asignatura, Integer> {

    @Query(value = "SELECT public.sp_crear_asignatura(:idCarrera, :nombre, :semestre)", nativeQuery = true)
    Integer registrarAsignatura(@Param("idCarrera") Integer idCarrera,
                                @Param("nombre") String nombreAsignatura,
                                @Param("semestre") Integer semestre);

    @Query(value = "SELECT public.sp_actualizar_asignatura(:id, :idCarrera, :nombre, :semestre)", nativeQuery = true)
    Integer actualizarAsignatura(@Param("id") Integer idAsignatura,
                                 @Param("idCarrera") Integer idCarrera,
                                 @Param("nombre") String nombreAsignatura,
                                 @Param("semestre") Integer semestre);

    @Query(value = "SELECT public.sp_desactivar_asignatura(:id)", nativeQuery = true)
    Integer desactivarAsignatura(@Param("id") Integer idAsignatura);

    List<Asignatura> findByCarrera_IdCarrera(Integer idCarrera);

    @Query("SELECT a FROM Asignatura a JOIN FETCH a.carrera c")
    List<Asignatura> listarAsignaturasConCarrera();

}