package org.uteq.sgacfinal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.uteq.sgacfinal.entity.Notificacion;

import java.util.List;

@Repository
public interface NotificacionRepository extends JpaRepository<Notificacion, Integer> {
    List<Notificacion> findByUsuarioDestino_IdUsuarioOrderByFechaEnvioDesc(Integer idUsuario);
    List<Notificacion> findByUsuarioDestino_IdUsuarioAndLeidoFalse(Integer idUsuario);
}
