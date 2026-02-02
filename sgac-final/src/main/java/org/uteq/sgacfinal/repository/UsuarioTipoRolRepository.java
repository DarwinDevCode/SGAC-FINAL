package org.uteq.sgacfinal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.uteq.sgacfinal.entity.UsuarioTipoRol;
import org.uteq.sgacfinal.entity.UsuarioTipoRolId;

import java.util.List;

@Repository
public interface UsuarioTipoRolRepository extends JpaRepository<UsuarioTipoRol, UsuarioTipoRolId> {

    @Query(value = "SELECT public.sp_asignar_rol_usuario(:idUsuario, :idRol)", nativeQuery = true)
    Integer asignarRolUsuario(@Param("idUsuario") Integer idUsuario,
                              @Param("idRol") Integer idTipoRol);

    @Query(value = "SELECT public.sp_actualizar_estado_rol_usuario(:idUsuario, :idRol, :activo)", nativeQuery = true)
    Integer actualizarEstadoRol(@Param("idUsuario") Integer idUsuario,
                                @Param("idRol") Integer idTipoRol,
                                @Param("activo") Boolean activo);

    @Query(value = "SELECT public.sp_desactivar_rol_usuario(:idUsuario, :idRol)", nativeQuery = true)
    Integer desactivarRolUsuario(@Param("idUsuario") Integer idUsuario,
                                 @Param("idRol") Integer idTipoRol);

    @Query(value = "SELECT * FROM public.sp_obtener_roles_usuario(:idUsuario)", nativeQuery = true)
    List<Object[]> obtenerRolesPorUsuarioSP(@Param("idUsuario") Integer idUsuario);
}