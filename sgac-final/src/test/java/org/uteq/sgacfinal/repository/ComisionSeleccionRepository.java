package org.uteq.sgacfinal.repository;
import org.uteq.sgacfinal.entity.ComisionSeleccion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ComisionSeleccionRepository extends JpaRepository<ComisionSeleccion, Integer> {
    List<ComisionSeleccion> findByActivo(Boolean activo);
    List<ComisionSeleccion> findByConvocatoriaIdConvocatoria(Integer idConvocatoria);
}
