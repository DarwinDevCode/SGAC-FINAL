package org.uteq.sgacfinal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.uteq.sgacfinal.entity.PeriodoAcademicoRequisitoPostulacion;

import java.util.List;

@Repository
public interface PeriodoAcademicoRequisitoPostulacionRepository extends JpaRepository<PeriodoAcademicoRequisitoPostulacion, Integer> {

    @Query(value = "SELECT public.sp_crear_periodo_requisito(:idPeriodo, :idTipoReq, :obligatorio, :orden)", nativeQuery = true)
    Integer registrarConfiguracionRequisito(@Param("idPeriodo") Integer idPeriodo,
                                            @Param("idTipoReq") Integer idTipoRequisito,
                                            @Param("obligatorio") Boolean obligatorio,
                                            @Param("orden") Integer orden);

    @Query(value = "SELECT public.sp_actualizar_periodo_requisito(:id, :obligatorio, :orden)", nativeQuery = true)
    Integer actualizarConfiguracionRequisito(@Param("id") Integer idRelacion,
                                             @Param("obligatorio") Boolean obligatorio,
                                             @Param("orden") Integer orden);

    @Query(value = "SELECT public.sp_desactivar_periodo_requisito(:id)", nativeQuery = true)
    Integer desactivarConfiguracionRequisito(@Param("id") Integer idRelacion);

    @Query(value = "SELECT * FROM public.sp_listar_requisitos_periodo(:idPeriodo)", nativeQuery = true)
    List<Object[]> listarRequisitosPorPeriodoSP(@Param("idPeriodo") Integer idPeriodo);
}