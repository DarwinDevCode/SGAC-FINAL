package com.sgac.repository;

import com.sgac.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {

    Optional<Usuario> findByNombreUsuario(String nombreUsuario);

    Optional<Usuario> findByCedula(String cedula);

    Optional<Usuario> findByCorreo(String correo);

    List<Usuario> findByActivo(Boolean activo);

    boolean existsByNombreUsuario(String nombreUsuario);

    boolean existsByCedula(String cedula);

    boolean existsByCorreo(String correo);

    @Query("SELECT u FROM Usuario u LEFT JOIN FETCH u.usuarioTipoRoles utr LEFT JOIN FETCH utr.tipoRol WHERE u.idUsuario = :id")
    Optional<Usuario> findByIdWithRoles(Integer id);

    @Query("SELECT DISTINCT u FROM Usuario u LEFT JOIN FETCH u.usuarioTipoRoles utr LEFT JOIN FETCH utr.tipoRol WHERE u.activo = true")
    List<Usuario> findAllActiveWithRoles();
}
