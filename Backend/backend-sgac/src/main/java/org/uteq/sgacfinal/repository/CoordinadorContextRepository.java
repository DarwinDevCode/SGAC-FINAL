package org.uteq.sgacfinal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.uteq.sgacfinal.entity.Coordinador;

import java.util.Optional;

@Repository
public interface CoordinadorContextRepository extends JpaRepository<Coordinador, Integer> {
    Optional<Coordinador> findByUsuario_IdUsuarioAndActivoTrue(Integer idUsuario);
}

