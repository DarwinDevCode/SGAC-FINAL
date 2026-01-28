package com.sgac.repository;

import com.sgac.entity.LogAuditoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface LogAuditoriaRepository extends JpaRepository<LogAuditoria, Integer> {
    List<LogAuditoria> findByUsuarioIdUsuario(Integer idUsuario);
    List<LogAuditoria> findByTablaAfectada(String tablaAfectada);
    List<LogAuditoria> findByAccion(String accion);
}
