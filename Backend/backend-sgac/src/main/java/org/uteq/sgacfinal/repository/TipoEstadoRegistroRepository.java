package org.uteq.sgacfinal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.uteq.sgacfinal.entity.TipoEstadoRegistro;

import java.util.List;
@Repository
public interface TipoEstadoRegistroRepository extends JpaRepository<TipoEstadoRegistro,Integer> {
    List<TipoEstadoRegistro> findByActivoTrue();
}
