package org.uteq.sgacfinal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.uteq.sgacfinal.entity.Decano;

import java.util.List;
import java.util.Optional;

@Repository
public interface DecanoNotificacionRepository extends JpaRepository<Decano, Integer> {

    // Puede haber múltiples decanos activos (datos existentes). Tomamos uno determinísticamente.
    @Query("SELECT d.usuario.idUsuario FROM Decano d WHERE d.activo = true AND d.facultad.idFacultad = :idFacultad ORDER BY d.idDecano DESC")
    List<Integer> findIdsUsuarioDecanoActivoByFacultad(@Param("idFacultad") Integer idFacultad);

    default Optional<Integer> findIdUsuarioDecanoActivoByFacultad(@Param("idFacultad") Integer idFacultad) {
        List<Integer> ids = findIdsUsuarioDecanoActivoByFacultad(idFacultad);
        if (ids == null || ids.isEmpty()) return Optional.empty();
        return Optional.ofNullable(ids.get(0));
    }
}
