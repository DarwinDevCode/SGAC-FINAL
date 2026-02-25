package org.uteq.sgacfinal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.uteq.sgacfinal.dto.Response.EsquemaResponseDTO;
import org.uteq.sgacfinal.entity.Usuario;

import java.util.List;

@Repository
public interface IPermisoRepository extends JpaRepository<Usuario, Integer> {

    @Query(value = "SELECT * FROM fn_permisos_actuales()", nativeQuery = true)
    List<Object[]> obtenerPermisosActuales();


    @Query(value = "SELECT esquema, elemento, categoria, privilegio " +
            "FROM seguridad.fn_consultar_permisos_rol(:rolBd, :esquema, :categoria, :privilegio)",
            nativeQuery = true)
    List<Object[]> consultarPermisosRolRaw(
            @Param("rolBd") String rolBd,
            @Param("esquema") String esquema,
            @Param("categoria") String categoria,
            @Param("privilegio") String privilegio
    );


    @Query(value = "SELECT seguridad.fn_gestionar_permisos_elemento2(" +
            ":rolBd, :esquema, :elemento, :categoria, :privilegio, :otorgar)",
            nativeQuery = true)
    Boolean gestionarPermisoElementoRaw(
            @Param("rolBd") String rolBd,
            @Param("esquema") String esquema,
            @Param("elemento") String elemento,
            @Param("categoria") String categoria,
            @Param("privilegio") String privilegio,
            @Param("otorgar") Boolean otorgar
    );




//    @Query(value = "SELECT nombre_elemento FROM seguridad.fn_listar_elementos_por_filtro(:esquema, :categoria)",
//            nativeQuery = true)
//    List<String> listarElementosPorFiltroRaw(
//            @Param("esquema") String esquema,
//            @Param("categoria") String categoria
//    );


//    @Query(value = "SELECT * FROM seguridad.fn_listar_esquemas()", nativeQuery = true)
//    List<EsquemaResponseDTO> listarEsquemas();


    @Query(value = "SELECT * FROM seguridad.fn_listar_esquemas()", nativeQuery = true)
    List<String> listarEsquemas();

    @Query(value = "SELECT id_tipo_objeto_seguridad, nombre_tipo_objeto FROM seguridad.fn_listar_tipos_objeto_seguridad()", nativeQuery = true)
    List<Object[]> listarTiposObjetoSeguridad();

    @Query(value = "SELECT * FROM seguridad.fn_listar_elementos_por_tipo_de_objeto(:esquema, :tipoObjeto)", nativeQuery = true)
    List<String> listarElementosPorTipo(@Param("esquema") String esquema, @Param("tipoObjeto") String tipoObjeto);

    @Query(value = "SELECT id_privilegio, nombre_privilegio, codigo_interno FROM seguridad.fn_listar_privilegios_por_tipo_objeto(:idTipoObjeto)", nativeQuery = true)
    List<Object[]> listarPrivilegiosPorTipoObjeto(@Param("idTipoObjeto") Integer idTipoObjeto);
}
