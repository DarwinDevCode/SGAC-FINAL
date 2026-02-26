package org.uteq.sgacfinal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.uteq.sgacfinal.entity.RequisitoAdjunto;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface RequisitoAdjuntoRepository extends JpaRepository<RequisitoAdjunto, Integer> {

    @Query(value = "SELECT public.sp_crear_requisito_adjunto(:idPostulacion, :idTipoReq, :idEstado, :archivo, :nombre, :fecha, :obs)", nativeQuery = true)
    Integer registrarRequisito(@Param("idPostulacion") Integer idPostulacion,
                               @Param("idTipoReq") Integer idTipoRequisito,
                               @Param("idEstado") Integer idTipoEstado,
                               @Param("archivo") byte[] archivo,
                               @Param("nombre") String nombreArchivo,
                               @Param("fecha") LocalDate fechaSubida,
                               @Param("obs") String observacion);

    @Query(value = "SELECT public.sp_actualizar_requisito_adjunto(:id, :idEstado, :obs)", nativeQuery = true)
    Integer actualizarRequisito(@Param("id") Integer idRequisito,
                                @Param("idEstado") Integer idTipoEstado,
                                @Param("obs") String observacion);

    @Query(value = "SELECT * FROM public.sp_obtener_requisitos_postulacion(:idPostulacion)", nativeQuery = true)
    List<Object[]> obtenerRequisitosPorPostulacionSP(@Param("idPostulacion") Integer idPostulacion);

    List<RequisitoAdjunto> findByPostulacion_IdPostulacion(Integer idPostulacion);
}