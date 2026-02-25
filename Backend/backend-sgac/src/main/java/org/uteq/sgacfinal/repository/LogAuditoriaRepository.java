package org.uteq.sgacfinal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.uteq.sgacfinal.entity.LogAuditoria;

import java.util.List;

@Repository
public interface LogAuditoriaRepository extends JpaRepository<LogAuditoria, Integer> {

    @Query(value = "SELECT public.sp_crear_log_auditoria(:idUsuario, :accion, :tabla, :registro, :ip, :anterior, :nuevo)", nativeQuery = true)
    Integer registrarLog(@Param("idUsuario") Integer idUsuario,
                         @Param("accion") String accion,
                         @Param("tabla") String tablaAfectada,
                         @Param("registro") Integer registroAfectado,
                         @Param("ip") String ipOrigen,
                         @Param("anterior") String valorAnterior,
                         @Param("nuevo") String valorNuevo);

    @Query(value = "SELECT public.sp_actualizar_log_auditoria(:id, :accion, :nuevo)", nativeQuery = true)
    Integer actualizarLog(@Param("id") Integer idLog,
                          @Param("accion") String accion,
                          @Param("nuevo") String valorNuevo);

    @Query(value = "SELECT * FROM public.sp_listar_logs_auditoria()", nativeQuery = true)
    List<Object[]> listarLogsSP();

    List<LogAuditoria> findByUsuario_IdUsuario(Integer idUsuario);
}