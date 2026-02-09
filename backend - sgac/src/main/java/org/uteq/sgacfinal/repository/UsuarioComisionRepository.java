package org.uteq.sgacfinal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.uteq.sgacfinal.entity.UsuarioComision;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface UsuarioComisionRepository extends JpaRepository<UsuarioComision, Integer> {

    @Query(value = "SELECT public.sp_crear_usuario_comision(:idComision, :idUsuario, :idEval, :rol, :pMat, :pResp, :pExp, :fecha)", nativeQuery = true)
    Integer registrarUsuarioComision(@Param("idComision") Integer idComision,
                                     @Param("idUsuario") Integer idUsuario,
                                     @Param("idEval") Integer idEvaluacionOposicion,
                                     @Param("rol") String rolIntegrante,
                                     @Param("pMat") BigDecimal puntajeMaterial,
                                     @Param("pResp") BigDecimal puntajeRespuestas,
                                     @Param("pExp") BigDecimal puntajeExposicion,
                                     @Param("fecha") LocalDate fechaEvaluacion);

    @Query(value = "SELECT public.sp_actualizar_usuario_comision(:id, :pMat, :pResp, :pExp, :fecha)", nativeQuery = true)
    Integer actualizarPuntajes(@Param("id") Integer idUsuarioComision,
                               @Param("pMat") BigDecimal puntajeMaterial,
                               @Param("pResp") BigDecimal puntajeRespuestas,
                               @Param("pExp") BigDecimal puntajeExposicion,
                               @Param("fecha") LocalDate fechaEvaluacion);

    @Query(value = "SELECT public.sp_desactivar_usuario_comision(:id)", nativeQuery = true)
    Integer desactivarUsuarioComision(@Param("id") Integer idUsuarioComision);

    @Query(value = "SELECT * FROM public.sp_listar_evaluadores_comision(:idComision)", nativeQuery = true)
    List<UsuarioComision> listarEvaluadoresPorComisionSP(@Param("idComision") Integer idComision);
}