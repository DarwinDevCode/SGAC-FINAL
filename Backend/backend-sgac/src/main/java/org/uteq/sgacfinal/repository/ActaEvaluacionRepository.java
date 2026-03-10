package org.uteq.sgacfinal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.uteq.sgacfinal.entity.ActaEvaluacion;

import java.util.List;
import java.util.Optional;

@Repository
public interface ActaEvaluacionRepository extends JpaRepository<ActaEvaluacion, Integer> {

    @Query("SELECT a FROM ActaEvaluacion a WHERE a.postulacion.idPostulacion = :idPostulacion")
    List<ActaEvaluacion> findByIdPostulacion(@Param("idPostulacion") Integer idPostulacion);

    @Query("SELECT a FROM ActaEvaluacion a WHERE a.postulacion.idPostulacion = :idPostulacion AND a.tipoActa = :tipoActa")
    Optional<ActaEvaluacion> findByIdPostulacionAndTipoActa(
            @Param("idPostulacion") Integer idPostulacion,
            @Param("tipoActa") String tipoActa);

    @Query(value = "SELECT CAST(public.sp_crear_acta(:idPostulacion, :tipoActa, :estado, :urlDocumento) AS INTEGER)", nativeQuery = true)
    Integer crearActa(@Param("idPostulacion") Integer idPostulacion,
                      @Param("tipoActa") String tipoActa,
                      @Param("estado") String estado,
                      @Param("urlDocumento") String urlDocumento);

    @Query(value = "SELECT CAST(public.sp_actualizar_acta(:idActa, :estado, :urlDocumento) AS INTEGER)", nativeQuery = true)
    Integer actualizarActa(@Param("idActa") Integer idActa,
                           @Param("estado") String estado,
                           @Param("urlDocumento") String urlDocumento);

    @Query(value = "SELECT CAST(public.sp_eliminar_acta(:idActa) AS INTEGER)", nativeQuery = true)
    Integer eliminarActa(@Param("idActa") Integer idActa);

    @Query(value = "SELECT CAST(public.sp_confirmar_acta(:idActa, :idEvaluador, :rol) AS INTEGER)", nativeQuery = true)
    Integer confirmarActa(
            @Param("idActa") Integer idActa,
            @Param("idEvaluador") Integer idEvaluador,
            @Param("rol") String rol);
}
