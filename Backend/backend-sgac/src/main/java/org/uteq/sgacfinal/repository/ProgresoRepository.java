package org.uteq.sgacfinal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.uteq.sgacfinal.entity.Ayudantia;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProgresoRepository extends JpaRepository<Ayudantia, Integer> {
    @Query(value = """
        SELECT
            COALESCE(SUM(ra.horas_dedicadas) FILTER (WHERE ra.id_tipo_estado_registro = 2), 0) AS horasAprobadas,
            COALESCE(SUM(ra.horas_dedicadas) FILTER (WHERE ra.id_tipo_estado_registro = 1), 0) AS horasPendientes,
            COALESCE(SUM(ra.horas_dedicadas) FILTER (WHERE ra.id_tipo_estado_registro = 4), 0) AS horasObservadas,
            COALESCE(SUM(ra.horas_dedicadas), 0) AS horasTotalesRegistradas,
            ROUND(CEIL((a.fecha_fin - a.fecha_inicio) / 7.0) * 20, 2) AS horasMaximas,
            CASE
                WHEN CEIL((a.fecha_fin - a.fecha_inicio) / 7.0) = 0 THEN 0
                ELSE ROUND(
                    COALESCE(SUM(ra.horas_dedicadas) FILTER (WHERE ra.id_tipo_estado_registro = 2), 0)
                    / (CEIL((a.fecha_fin - a.fecha_inicio) / 7.0) * 20) * 100, 2
                )
            END AS porcentajeAvance,
            COUNT(ra.id_registro_actividad) AS totalSesiones,
            COUNT(ra.id_registro_actividad) FILTER (WHERE ra.id_tipo_estado_registro = 2) AS sesionesAprobadas,
            COUNT(ra.id_registro_actividad) FILTER (WHERE ra.id_tipo_estado_registro = 1) AS sesionesPendientes,
            COUNT(ra.id_registro_actividad) FILTER (WHERE ra.id_tipo_estado_registro = 4) AS sesionesObservadas
        FROM ayudantia.ayudantia a
        LEFT JOIN ayudantia.registro_actividad ra ON ra.id_ayudantia = a.id_ayudantia
        WHERE a.id_ayudantia = :idAyudantia
        GROUP BY a.id_ayudantia, a.fecha_inicio, a.fecha_fin
        """, nativeQuery = true)
    List<Object[]> progresoGeneral(@Param("idAyudantia") Integer idAyudantia);

    @Query(value = """
        SELECT
            DATE_TRUNC('week', CURRENT_DATE)::DATE AS semanaInicio,
            (DATE_TRUNC('week', CURRENT_DATE) + INTERVAL '6 days')::DATE AS semanaFin,
            COALESCE(SUM(ra.horas_dedicadas), 0) AS horasRegistradas,
            COALESCE(SUM(ra.horas_dedicadas) FILTER (WHERE ra.id_tipo_estado_registro = 2), 0) AS horasAprobadasSemana,
            COALESCE(SUM(ra.horas_dedicadas) FILTER (WHERE ra.id_tipo_estado_registro = 1), 0) AS horasPendientesSemana,
            20.0 AS limiteSemanal,
            GREATEST(20.0 - COALESCE(SUM(ra.horas_dedicadas), 0), 0) AS horasDisponibles,
            COALESCE(SUM(ra.horas_dedicadas), 0) > 20.0 AS superaLimite,
            COUNT(ra.id_registro_actividad) AS sesionesSemana
        FROM ayudantia.registro_actividad ra
        WHERE ra.id_ayudantia = :idAyudantia
          AND ra.fecha >= DATE_TRUNC('week', CURRENT_DATE)
          AND ra.fecha <= (DATE_TRUNC('week', CURRENT_DATE) + INTERVAL '6 days')
        """, nativeQuery = true)
    List<Object[]> controlSemanal(@Param("idAyudantia") Integer idAyudantia);
}
