package org.uteq.sgacfinal.repository.ayudantia;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.uteq.sgacfinal.entity.DetalleAsistenciaActividad;

@Repository
public interface DetalleAsistenciaActividadRepository extends JpaRepository<DetalleAsistenciaActividad, Integer> {
}