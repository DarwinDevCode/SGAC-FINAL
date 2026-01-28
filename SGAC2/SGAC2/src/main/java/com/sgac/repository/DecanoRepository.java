package com.sgac.repository;

import com.sgac.entity.Decano;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface DecanoRepository extends JpaRepository<Decano, Integer> {
    List<Decano> findByActivo(Boolean activo);
    List<Decano> findByFacultadIdFacultad(Integer idFacultad);
}
