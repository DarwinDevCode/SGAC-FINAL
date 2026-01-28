package org.uteq.sgacfinal.repository;

import org.uteq.sgacfinal.entity.TipoRol;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TipoRolRepository extends JpaRepository<TipoRol, Integer> {

    Optional<TipoRol> findByNombreTipoRol(String nombreTipoRol);

    List<TipoRol> findByActivo(Boolean activo);

    boolean existsByNombreTipoRol(String nombreTipoRol);
}
