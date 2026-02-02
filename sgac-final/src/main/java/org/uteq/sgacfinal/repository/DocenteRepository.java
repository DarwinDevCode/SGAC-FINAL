package org.uteq.sgacfinal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.uteq.sgacfinal.entity.Docente;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface DocenteRepository extends JpaRepository<Docente, Integer> {

    @Query(value = "SELECT public.sp_crear_docente(:idUsuario, :inicio)", nativeQuery = true)
    Integer registrarDocente(@Param("idUsuario") Integer idUsuario,
                             @Param("inicio") LocalDate fechaInicio);

    @Query(value = "SELECT public.sp_actualizar_docente(:id, :inicio, :fin)", nativeQuery = true)
    Integer actualizarDocente(@Param("id") Integer idDocente,
                              @Param("inicio") LocalDate fechaInicio,
                              @Param("fin") LocalDate fechaFin);

    @Query(value = "SELECT public.sp_desactivar_docente(:id)", nativeQuery = true)
    Integer desactivarDocente(@Param("id") Integer idDocente);

    @Query(value = "SELECT * FROM public.sp_obtener_docente_por_id(:id)", nativeQuery = true)
    Optional<Docente> obtenerDocentePorIdSP(@Param("id") Integer id);

    Optional<Docente> findByUsuario_IdUsuario(Integer idUsuario);
}