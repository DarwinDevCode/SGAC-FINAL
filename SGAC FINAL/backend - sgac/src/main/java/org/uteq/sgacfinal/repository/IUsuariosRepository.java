package org.uteq.sgacfinal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.uteq.sgacfinal.entity.Usuario;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface IUsuariosRepository extends JpaRepository<Usuario, Integer> {

    @Modifying
    @Query(value = "CALL public.sp_registrar_estudiante(:nombres, :apellidos, :cedula, :correo, :username, :password, :idCarrera, :matricula, :semestre)", nativeQuery = true)
    void registrarEstudiante(
            @Param("nombres") String nombres,
            @Param("apellidos") String apellidos,
            @Param("cedula") String cedula,
            @Param("correo") String correo,
            @Param("username") String username,
            @Param("password") String password,
            @Param("idCarrera") Integer idCarrera,
            @Param("matricula") String matricula,
            @Param("semestre") Integer semestre
    );

    @Modifying
    @Query(value = "CALL public.sp_registrar_docente(:nombres, :apellidos, :cedula, :correo, :username, :password)", nativeQuery = true)
    void registrarDocente(
           @Param("nombres") String nombres,
           @Param("apellidos") String apellidos,
           @Param("cedula") String cedula,
           @Param("correo") String correo,
           @Param("username") String username,
           @Param("password") String password
    );

    @Modifying
    @Query(value = "CALL public.sp_registrar_decano(:nombres, :apellidos, :cedula, :correo, :username, :password, :idFacultad)", nativeQuery = true)
    void registrarDecano(
            @Param("nombres") String nombres,
            @Param("apellidos") String apellidos,
            @Param("cedula") String cedula,
            @Param("correo") String correo,
            @Param("username") String username,
            @Param("password") String password,
            @Param("idFacultad") Integer idFacultad
    );

    @Modifying
    @Query(value = "CALL public.sp_registrar_coordinador(:nombres, :apellidos, :cedula, :correo, :username, :password, :idCarrera)", nativeQuery = true)
    void registrarCoordinador(
            @Param("nombres") String nombres,
            @Param("apellidos") String apellidos,
            @Param("cedula") String cedula,
            @Param("correo") String correo,
            @Param("username") String username,
            @Param("password") String password,
            @Param("idCarrera") Integer idCarrera
    );

    @Modifying
    @Query(value = "CALL public.sp_registrar_ayudante_directo(:nombres, :apellidos, :cedula, :correo, :username, :password, :horasAsignadas)", nativeQuery = true)
    void registrarAyudanteDirecto(
            @Param("nombres") String nombres,
            @Param("apellidos") String apellidos,
            @Param("cedula") String cedula,
            @Param("correo") String correo,
            @Param("username") String username,
            @Param("password") String password,
            @Param("horasAsignadas") BigDecimal horasAsignadas
    );

    @Modifying
    @Query(value = "CALL public.sp_registrar_administrador(:nombres, :apellidos, :cedula, :correo, :username, :password)", nativeQuery = true)
    void registrarAdministrador(
            @Param("nombres") String nombres,
            @Param("apellidos") String apellidos,
            @Param("cedula") String cedula,
            @Param("correo") String correo,
            @Param("username") String username,
            @Param("password") String password
    );

    @Modifying
    @Query(value = "CALL public.sp_promover_estudiante_a_ayudante(:username, :horasAsignadas)", nativeQuery = true)
    void promoverEstudianteAAyudante(
            @Param("username") String username,
            @Param("horasAsignadas") BigDecimal horasAsignadas
    );


    Optional<Usuario> findByNombreUsuario(String nombreUsuario);

    @Query("""
            SELECT DISTINCT u
            FROM Usuario u
            LEFT JOIN FETCH u.roles r
            LEFT JOIN FETCH r.tipoRol
            WHERE u.nombreUsuario = :nombreUsuario
            """)
    Optional<Usuario> findByNombreUsuarioWithRolesAndTipoRol(@Param("nombreUsuario") String nombreUsuario);

}
