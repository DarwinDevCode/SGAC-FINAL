package org.uteq.sgacfinal.repository;

import org.uteq.sgacfinal.entity.UsuarioComision;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UsuarioComisionRepository extends JpaRepository<UsuarioComision, Integer> {
    List<UsuarioComision> findByComisionSeleccionIdComisionSeleccion(Integer idComisionSeleccion);
    List<UsuarioComision> findByUsuarioIdUsuario(Integer idUsuario);
}
