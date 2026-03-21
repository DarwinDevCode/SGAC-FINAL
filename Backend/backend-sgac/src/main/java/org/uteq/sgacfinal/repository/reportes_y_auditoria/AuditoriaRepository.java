package org.uteq.sgacfinal.repository.reportes_y_auditoria;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.uteq.sgacfinal.dto.response.reportes_y_auditoria.AuditoriaKpiDTO;
import org.uteq.sgacfinal.dto.response.reportes_y_auditoria.EvolucionAuditoriaProjection;
import org.uteq.sgacfinal.entity.VistaAuditoria;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AuditoriaRepository extends JpaRepository<VistaAuditoria, Integer> {

    @Query("SELECT a FROM VistaAuditoria a WHERE " +
            "(:tabla IS NULL OR a.tablaAfectada = :tabla) AND " +
            "(:accion IS NULL OR a.accion = :accion) AND " +
            "(:idUsuario IS NULL OR a.idUsuario = :idUsuario) AND " +
            "(cast(:fechaInicio as date) IS NULL OR a.fechaHora >= :fechaInicio) AND " +
            "(cast(:fechaFin as date) IS NULL OR a.fechaHora <= :fechaFin)")
    Page<VistaAuditoria> buscarAuditoriasFiltradas(
            @Param("tabla") String tabla,
            @Param("accion") String accion,
            @Param("idUsuario") Integer idUsuario,
            @Param("fechaInicio") LocalDateTime fechaInicio,
            @Param("fechaFin") LocalDateTime fechaFin,
            Pageable pageable);

    @Query(value = "SELECT TO_CHAR(fecha_hora, 'YYYY-MM-DD') AS fecha, " +
            "SUM(CASE WHEN accion = 'INSERT' THEN 1 ELSE 0 END) AS inserts, " +
            "SUM(CASE WHEN accion = 'UPDATE' THEN 1 ELSE 0 END) AS updates, " +
            "SUM(CASE WHEN accion = 'DELETE' THEN 1 ELSE 0 END) AS deletes " +
            "FROM seguridad.log_auditoria " +
            "WHERE fecha_hora >= CURRENT_DATE - INTERVAL '14 days' " +
            "GROUP BY TO_CHAR(fecha_hora, 'YYYY-MM-DD') " +
            "ORDER BY fecha ASC", nativeQuery = true)
    List<EvolucionAuditoriaProjection> obtenerEvolucionUltimosDias();

    @Query("SELECT new org.uteq.sgacfinal.dto.response.reportes_y_auditoria.AuditoriaKpiDTO(" +
            "COUNT(a), " +
            "COALESCE(SUM(CASE WHEN CAST(a.fechaHora AS date) = CURRENT_DATE THEN 1L ELSE 0L END), 0L), " +
            "COALESCE(SUM(CASE WHEN a.accion = 'INSERT' THEN 1L ELSE 0L END), 0L), " +
            "COALESCE(SUM(CASE WHEN a.accion = 'UPDATE' THEN 1L ELSE 0L END), 0L), " +
            "COALESCE(SUM(CASE WHEN a.accion = 'DELETE' THEN 1L ELSE 0L END), 0L)) " +
            "FROM VistaAuditoria a")
    AuditoriaKpiDTO obtenerKpisGlobales();
}