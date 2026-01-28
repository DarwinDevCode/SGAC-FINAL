package com.sgac.repository;

import com.sgac.entity.UsuarioTipoRol;
import com.sgac.entity.UsuarioTipoRolId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UsuarioTipoRolRepository extends JpaRepository<UsuarioTipoRol, UsuarioTipoRolId> {

    @Query("SELECT utr FROM UsuarioTipoRol utr WHERE utr.usuario.idUsuario = :idUsuario")
    List<UsuarioTipoRol> findByUsuarioId(Integer idUsuario);

    @Query("SELECT utr FROM UsuarioTipoRol utr WHERE utr.tipoRol.idTipoRol = :idTipoRol")
    List<UsuarioTipoRol> findByTipoRolId(Integer idTipoRol);

    @Query("SELECT utr FROM UsuarioTipoRol utr WHERE utr.usuario.idUsuario = :idUsuario AND utr.activo = true")
    List<UsuarioTipoRol> findActiveByUsuarioId(Integer idUsuario);

    void deleteByUsuarioIdUsuario(Integer idUsuario);
}
