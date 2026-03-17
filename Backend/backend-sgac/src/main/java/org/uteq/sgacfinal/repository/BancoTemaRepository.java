package org.uteq.sgacfinal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.uteq.sgacfinal.entity.BancoTema;
import java.util.List;

@Repository
public interface BancoTemaRepository extends JpaRepository<BancoTema, Integer> {
    List<BancoTema> findByIdConvocatoriaIdConvocatoriaAndActivoTrue(Integer idConvocatoria);
}
