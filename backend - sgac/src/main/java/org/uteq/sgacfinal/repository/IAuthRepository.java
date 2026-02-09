package org.uteq.sgacfinal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.query.Procedure;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.uteq.sgacfinal.dto.Response.UsuarioResponseDTO;
import org.uteq.sgacfinal.entity.Usuario;
import java.util.List;

@Repository
public interface IAuthRepository extends JpaRepository<Usuario, Integer> {

    @Query(value = "SELECT * FROM public.fn_login_sgac(:usuario, :contrasenia)", nativeQuery = true)
    List<Object[]> login(@Param("usuario") String usuario,
                         @Param("contrasenia") String contrasenia);

}

