package org.uteq.sgacfinal.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.uteq.sgacfinal.entity.Notificacion;

import java.util.List;

@Repository
public interface NotificacionRepository extends JpaRepository<Notificacion, Integer> {
    List<Notificacion> findByUsuario_IdUsuarioAndLeidoFalseOrderByFechaCreacionDesc(Integer idUsuario);
    List<Notificacion> findByUsuario_IdUsuarioOrderByFechaCreacionDesc(Integer idUsuario, Pageable pageable);
    List<Notificacion> findByUsuario_IdUsuarioOrderByFechaCreacionDesc(Integer idUsuario);
    List<Notificacion> findByUsuario_IdUsuarioAndLeidoFalse(Integer idUsuario);
}

