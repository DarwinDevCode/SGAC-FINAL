package org.uteq.sgacfinal.repository;

import org.uteq.sgacfinal.entity.Estudiante;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EstudianteRepository extends JpaRepository<Estudiante, Integer> {
    Optional<Estudiante> findByMatricula(String matricula);
    List<Estudiante> findByCarreraIdCarrera(Integer idCarrera);
    List<Estudiante> findByUsuarioIdUsuario(Integer idUsuario);
}
