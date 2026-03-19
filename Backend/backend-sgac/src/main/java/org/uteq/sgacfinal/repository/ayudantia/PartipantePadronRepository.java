package org.uteq.sgacfinal.repository.ayudantia;

import jakarta.persistence.criteria.CriteriaBuilder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.uteq.sgacfinal.entity.ParticipanteAyudantia;

import java.util.List;

@Repository
public interface PartipantePadronRepository extends JpaRepository<ParticipanteAyudantia, Integer> {
    @Query(value = "SELECT * FROM ayudantia.fn_listar_padron_ayudante(:idUsuario)", nativeQuery = true)
    List<Object[]> listarPadronAyudante(@Param("idUsuario") Integer idUsuario);
}
