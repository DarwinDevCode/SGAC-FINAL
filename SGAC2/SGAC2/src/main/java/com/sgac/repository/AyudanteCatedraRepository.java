package com.sgac.repository;

import com.sgac.entity.AyudanteCatedra;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AyudanteCatedraRepository extends JpaRepository<AyudanteCatedra, Integer> {
    List<AyudanteCatedra> findByUsuarioIdUsuario(Integer idUsuario);
}
