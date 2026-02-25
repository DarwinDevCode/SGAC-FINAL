package org.uteq.sgacfinal.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.uteq.sgacfinal.entity.TipoRequisitoPostulacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ITipoRequisitoPostulacionRepository extends JpaRepository<TipoRequisitoPostulacion, Integer> {
    List<TipoRequisitoPostulacion> findByActivo(Boolean activo);
    List<TipoRequisitoPostulacion> findByActivoTrue();

    @Query(value = "SELECT public.fn_actualizar_tipo_requisito_postulacion(:id, :nombre, :descripcion)", nativeQuery = true)
    Integer actualizarTipoRequisitoPostulacion(@Param("id") Integer id,
                                               @Param("nombre") String nombre,
                                               @Param("descripcion") String descripcion);

    @Query(value = "SELECT public.fn_crear_tipo_requisito_postulacion(:nombre, :descripcion)", nativeQuery = true)
    Integer crearTipoRequisitoPostulacion(@Param("nombre") String nombre, @Param("descripcion") String descripcion);

    @Query(value = "SELECT public.fn_desactivar_tipo_requisito_postulacion(:id)", nativeQuery = true)
    Integer desactivarTipoRequisitoPostulacion(@Param("id") Integer id);
}
