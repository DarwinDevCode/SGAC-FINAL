package org.uteq.sgacfinal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.uteq.sgacfinal.entity.TipoEstadoEvidenciaAyudantia;

import java.util.List;

@Repository
public interface ITipoEstadoEvidenciaAyudantiaRepository extends JpaRepository<TipoEstadoEvidenciaAyudantia, Integer> {
    List<TipoEstadoEvidenciaAyudantia> findAllByOrderByNombreEstadoAsc();

    @Query(value = "SELECT public.fn_crear_tipo_estado_evidencia_ayudantia(:nombreEstado, :descripcion)", nativeQuery = true)
    Integer crearTipoEstadoEvidenciaAyudantia(@Param("nombreEstado") String nombreEstado,
                                              @Param("descripcion") String descripcion);


    @Query(value = "SELECT public.fn_actualizar_tipo_estado_evidencia_ayudantia(:id, :nombreEstado, :descripcion)", nativeQuery = true)
    Integer actualizarTipoEstadoEvidenciaAyudantia(@Param("id") Integer id,
                                                   @Param("nombreEstado") String nombreEstado,
                                                   @Param("descripcion") String descripcion);

    @Query(value = "SELECT public.fn_desactivar_tipo_estado_evidencia_ayudantia(:id)", nativeQuery = true)
    Integer desactivarTipoEstadoEvidenciaAyudantia(@Param("id") Integer id);


}