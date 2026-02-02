package org.uteq.sgacfinal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.uteq.sgacfinal.entity.Postulacion;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface PostulacionRepository extends JpaRepository<Postulacion, Integer> {

    @Query(value = "SELECT public.sp_crear_postulacion(:idConv, :idEst, :idPlazo, :fecha, :estado, :obs)", nativeQuery = true)
    Integer registrarPostulacion(@Param("idConv") Integer idConvocatoria,
                                 @Param("idEst") Integer idEstudiante,
                                 @Param("idPlazo") Integer idPlazoActividad,
                                 @Param("fecha") LocalDate fechaPostulacion,
                                 @Param("estado") String estadoPostulacion,
                                 @Param("obs") String observaciones);

    @Query(value = "SELECT public.sp_actualizar_postulacion(:id, :estado, :obs)", nativeQuery = true)
    Integer actualizarPostulacion(@Param("id") Integer idPostulacion,
                                  @Param("estado") String estadoPostulacion,
                                  @Param("obs") String observaciones);

    @Query(value = "SELECT public.sp_desactivar_postulacion(:id)", nativeQuery = true)
    Integer desactivarPostulacion(@Param("id") Integer idPostulacion);

    @Query(value = "SELECT * FROM public.sp_listar_postulaciones_por_estudiante(:idEstudiante)", nativeQuery = true)
    List<Object[]> listarPostulacionesPorEstudianteSP(@Param("idEstudiante") Integer idEstudiante);

    List<Postulacion> findByConvocatoria_IdConvocatoria(Integer idConvocatoria);

    Postulacion findByIdPostulacion(Integer idPostulacion);
}