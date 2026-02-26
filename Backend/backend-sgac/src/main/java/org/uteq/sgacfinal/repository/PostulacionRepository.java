package org.uteq.sgacfinal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.uteq.sgacfinal.entity.Postulacion;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

@Repository
public interface PostulacionRepository extends JpaRepository<Postulacion, Integer> {
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

    // Lista postulaciones por estado filtradas por carrera de la convocatoria
    @Query("SELECT p FROM Postulacion p WHERE p.estadoPostulacion = :estado " +
           "AND p.convocatoria.asignatura.carrera.idCarrera = :idCarrera")
    List<Postulacion> findByEstadoAndCarrera(@Param("estado") String estado,
                                             @Param("idCarrera") Integer idCarrera);

    // Lista todas las postulaciones de una carrera
    @Query("SELECT p FROM Postulacion p WHERE p.convocatoria.asignatura.carrera.idCarrera = :idCarrera")
    List<Postulacion> findByCarrera(@Param("idCarrera") Integer idCarrera);


    @Query(value = "SELECT public.sp_crear_postulacion(:idConv, :idEst, :fecha, :estado, :obs)", nativeQuery = true)
    Integer crearPostulacion(
            @Param("idConv") Integer idConv,
            @Param("idEst") Integer idEst,
            @Param("fecha") Date fecha,
            @Param("estado") String estado,
            @Param("obs") String obs
    );

    @Query(value = "SELECT public.sp_crear_requisito_adjunto(:idPost, :idTipoReq, :idTipoEst, :archivo, :nombre, :fecha)", nativeQuery = true)
    Integer crearRequisitoAdjunto(
            @Param("idPost") Integer idPost,
            @Param("idTipoReq") Integer idTipoReq,
            @Param("idTipoEst") Integer idTipoEst,
            @Param("archivo") byte[] archivo,
            @Param("nombre") String nombre,
            @Param("fecha") Date fecha);
}