package org.uteq.sgacfinal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.uteq.sgacfinal.entity.Usuario;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {
    @Query(value = "SELECT public.sp_crear_usuario(:nombres, :apellidos, :cedula, :correo, :user, :pass)", nativeQuery = true)
    Integer registrarUsuario(@Param("nombres") String nombres,
                             @Param("apellidos") String apellidos,
                             @Param("cedula") String cedula,
                             @Param("correo") String correo,
                             @Param("user") String nombreUsuario,
                             @Param("pass") String contrasenia);

    @Query(value = "SELECT public.sp_actualizar_usuario(:id, :nombres, :apellidos, :correo)", nativeQuery = true)
    Integer actualizarUsuario(@Param("id") Integer idUsuario,
                              @Param("nombres") String nombres,
                              @Param("apellidos") String apellidos,
                              @Param("correo") String correo);

    @Query(value = "SELECT public.sp_desactivar_usuario(:id)", nativeQuery = true)
    Integer desactivarUsuario(@Param("id") Integer idUsuario);

    @Query(value = "SELECT * FROM public.fn_login_sgac(:user, :pass)", nativeQuery = true)
    List<Object[]> loginSgac(@Param("user") String usuario,
                             @Param("pass") String contrasenia);

    @Query(value = "SELECT * FROM public.sp_validar_usuario(:user)", nativeQuery = true)
    List<Object[]> validarUsuarioSP(@Param("user") String nombreUsuario);

    Optional<Usuario> findByNombreUsuario(String nombreUsuario);
    boolean existsByCedula(String cedula);
    boolean existsByCorreo(String correo);
}