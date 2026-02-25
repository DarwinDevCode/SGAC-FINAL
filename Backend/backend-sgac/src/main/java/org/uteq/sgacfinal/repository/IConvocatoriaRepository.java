package org.uteq.sgacfinal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.uteq.sgacfinal.entity.Convocatoria;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface IConvocatoriaRepository extends JpaRepository<Convocatoria, Integer> {

    @Query(value = "SELECT public.sp_crear_convocatoria(:periodo, :asignatura, :docente, :cupos, :publicacion, :cierre, :estado)", nativeQuery = true)
    Integer registrarConvocatoria(
            @Param("periodo") Integer idPeriodo,
            @Param("asignatura") Integer idAsignatura,
            @Param("docente") Integer idDocente,
            @Param("cupos") Integer cupos,
            @Param("publicacion") LocalDate fechaPublicacion,
            @Param("cierre") LocalDate fechaCierre,
            @Param("estado") String estado
    );

    @Query(value = "SELECT public.sp_actualizar_convocatoria(:id, :cupos, :cierre, :estado)", nativeQuery = true)
    Integer actualizarConvocatoria(
            @Param("id") Integer idConvocatoria,
            @Param("cupos") Integer cupos,
            @Param("cierre") LocalDate fechaCierre,
            @Param("estado") String estado
    );

    @Query(value = "SELECT public.sp_desactivar_convocatoria(:id)", nativeQuery = true)
    Integer desactivarConvocatoria(@Param("id") Integer idConvocatoria);

    @Query(value = "SELECT * FROM public.sp_listar_convocatorias_activas()", nativeQuery = true)
    List<Object[]> listarConvocatoriasActivasSP();

    List<Convocatoria> findByPeriodoAcademico_IdPeriodoAcademico(Integer idPeriodo);

    @Query(value = "SELECT * FROM public.fn_listar_convocatorias_vista()", nativeQuery = true)
    List<Object[]> listarConvocatoriasVista();

    List<Convocatoria> findByActivoTrue();
    List<Convocatoria> findByActivoFalse();
}