package org.uteq.sgacfinal.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.uteq.sgacfinal.entity.NotificacionW;

import java.util.List;

@Repository
public interface NotificacionWRepository extends JpaRepository<NotificacionW, Integer> {

    List<NotificacionW> findByIdUsuario_IdUsuarioAndLeidoFalseOrderByFechaCreacionDesc(Integer idUsuario);

    List<NotificacionW> findByIdUsuario_IdUsuarioOrderByFechaCreacionDesc(Integer idUsuario, Pageable pageable);
}

