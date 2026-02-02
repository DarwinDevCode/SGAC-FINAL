package org.uteq.sgacfinal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.uteq.sgacfinal.entity.ComisionSeleccion;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ComisionSeleccionRepository extends JpaRepository<ComisionSeleccion, Integer> {

    @Query(value = "SELECT public.sp_crear_comision(:idConvocatoria, :nombre, :fecha)", nativeQuery = true)
    Integer registrarComision(@Param("idConvocatoria") Integer idConvocatoria,
                              @Param("nombre") String nombreComision,
                              @Param("fecha") LocalDate fechaConformacion);

    @Query(value = "SELECT public.sp_actualizar_comision(:id, :nombre, :fecha)", nativeQuery = true)
    Integer actualizarComision(@Param("id") Integer idComision,
                               @Param("nombre") String nombreComision,
                               @Param("fecha") LocalDate fechaConformacion);

    @Query(value = "SELECT public.sp_desactivar_comision(:id)", nativeQuery = true)
    Integer desactivarComision(@Param("id") Integer idComision);

    @Query(value = "SELECT * FROM public.sp_obtener_comision(:id)", nativeQuery = true)
    Optional<ComisionSeleccion> obtenerComisionPorIdSP(@Param("id") Integer id);

    List<ComisionSeleccion> findByConvocatoria_IdConvocatoria(Integer idConvocatoria);
}