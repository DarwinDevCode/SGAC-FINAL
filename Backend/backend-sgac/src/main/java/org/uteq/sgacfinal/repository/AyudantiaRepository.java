package org.uteq.sgacfinal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.uteq.sgacfinal.entity.Ayudantia;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AyudantiaRepository extends JpaRepository<Ayudantia, Integer> {

    @Query(value = "SELECT public.sp_crear_ayudantia(:idEstado, :idPostulacion, :inicio, :fin, :horas)", nativeQuery = true)
    Integer registrarAyudantia(@Param("idEstado") Integer idTipoEstado,
                               @Param("idPostulacion") Integer idPostulacion,
                               @Param("inicio") LocalDate fechaInicio,
                               @Param("fin") LocalDate fechaFin,
                               @Param("horas") Integer horasCumplidas);

    @Query(value = "SELECT public.sp_actualizar_ayudantia(:id, :idEstado, :inicio, :fin, :horas)", nativeQuery = true)
    Integer actualizarAyudantia(@Param("id") Integer idAyudantia,
                                @Param("idEstado") Integer idTipoEstado,
                                @Param("inicio") LocalDate fechaInicio,
                                @Param("fin") LocalDate fechaFin,
                                @Param("horas") Integer horasCumplidas);

    @Query(value = "SELECT * FROM public.sp_obtener_ayudantia_por_id(:id)", nativeQuery = true)
    Optional<Ayudantia> buscarPorIdSP(@Param("id") Integer id);

    // Eliminamos findAyudantiaConDetalles (JPQL) y lo reemplazamos por funciones específicas en el service

    @Query(value = "SELECT * FROM ayudantia.fn_listar_sesiones(:idUsuario, NULL, NULL, NULL, NULL)", nativeQuery = true)
    List<Object[]> findActividadesRawByUsuario(@Param("idUsuario") Integer idUsuario);

    @Query(value = "SELECT * FROM ayudantia.fn_listar_sesiones(:idAyudante, NULL, NULL, NULL, NULL)", nativeQuery = true)
    List<Object[]> findAllByAyudanteId(@Param("idAyudante") Integer idAyudante);

    @Query(value = "SELECT * FROM ayudantia.fn_evidencias_sesion(:idAyudante, :idRegistroActividad)", nativeQuery = true)
    List<Object[]> findDetalleConEvidenciasById(
            @Param("idAyudante") Integer idAyudante,
            @Param("idRegistroActividad") Integer idRegistroActividad
    );

    @Query(value = "SELECT * FROM ayudantia.fn_info_general_ayudantia(:idAyudantia)", nativeQuery = true)
    Optional<Object[]> findInfoGeneralAyudantia(@Param("idAyudantia") Integer idAyudantia);

    @Query(value = "SELECT ayudantia.fn_obtener_id_ayudantia(:idUsuario)", nativeQuery = true)
    Optional<Integer> findIdAyudantiaActivaByUsuario(@Param("idUsuario") Integer idUsuario);
}