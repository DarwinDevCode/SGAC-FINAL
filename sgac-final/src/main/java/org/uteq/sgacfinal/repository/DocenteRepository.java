package org.uteq.sgacfinal.repository;

import org.uteq.sgacfinal.entity.Docente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DocenteRepository extends JpaRepository<Docente, Integer> {
    List<Docente> findByActivo(Boolean activo);
    List<Docente> findByUsuarioIdUsuario(Integer idUsuario);
}
