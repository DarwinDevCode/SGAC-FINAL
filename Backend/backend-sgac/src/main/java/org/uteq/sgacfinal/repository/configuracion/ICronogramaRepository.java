package org.uteq.sgacfinal.repository.configuracion;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.uteq.sgacfinal.entity.PeriodoFase;

@Repository
public interface ICronogramaRepository extends JpaRepository<PeriodoFase, Integer> {
    @Query(value = "SELECT planificacion.fn_ajustar_cronograma_lote(:idPeriodo, CAST(:fasesJson AS jsonb))", nativeQuery = true)
    String ajustarCronogramaLote(
            @Param("idPeriodo") Integer idPeriodo,
            @Param("fasesJson") String fasesJson
    );

    @Query(value = "SELECT jsonb_build_object(" +
            "'exito', true, " +
            "'datos', COALESCE(jsonb_agg(" +
            "    jsonb_build_object(" +
            "        'idPeriodoFase', pf.id_periodo_fase, " +
            "        'idPeriodoAcademico', pf.id_periodo_academico, " +
            "        'idTipoFase', pf.id_tipo_fase, " +
            "        'nombreFase', tf.nombre, " +
            "        'orden', tf.orden, " +
            "        'fechaInicio', pf.fecha_inicio, " +
            "        'fechaFin', pf.fecha_fin" +
            "    ) ORDER BY tf.orden ASC" +
            "), '[]'::jsonb)) " +
            "FROM planificacion.periodo_fase pf " +
            "JOIN planificacion.tipo_fase tf ON tf.id_tipo_fase = pf.id_tipo_fase " +
            "WHERE pf.id_periodo_academico = :idPeriodo", nativeQuery = true)
    String listarCronogramaPorPeriodo(@Param("idPeriodo") Integer idPeriodo);

    @Query(value = "SELECT planificacion.fn_obtener_cronograma_activo()", nativeQuery = true)
    String obtenerCronogramaActivo();
}
