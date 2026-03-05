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

    /** Ítem 5: importa los requisitos activos de un periodo de origen al destino */
    @Query(value = "SELECT public.sp_importar_requisitos_periodo(:idOrigen, :idDestino)", nativeQuery = true)
    Integer importarRequisitosDeOtroPeriodo(@Param("idOrigen") Integer idPeriodoOrigen,
                                             @Param("idDestino") Integer idPeriodoDestino);

    /** Lista los periodos disponibles junto con la cantidad de requisitos activos */
    @Query(value = "SELECT pa.id_periodo_academico, pa.nombre_periodo, COUNT(parp.id_periodo_academico_requisito_postulacion) AS total_requisitos " +
                   "FROM academico.periodo_academico pa " +
                   "LEFT JOIN convocatoria.periodo_academico_requisito_postulacion parp ON pa.id_periodo_academico = parp.id_periodo_academico AND parp.activo = TRUE " +
                   "GROUP BY pa.id_periodo_academico, pa.nombre_periodo " +
                   "ORDER BY pa.id_periodo_academico DESC", nativeQuery = true)
    List<Object[]> listarPeriodosConTotalRequisitos();
}