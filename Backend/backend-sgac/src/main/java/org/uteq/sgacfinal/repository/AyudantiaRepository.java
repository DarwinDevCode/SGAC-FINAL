package org.uteq.sgacfinal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.uteq.sgacfinal.entity.Ayudantia;

import java.time.LocalDate;
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

    @Query(value = """
        SELECT a.id_ayudantia
        FROM ayudantia.ayudantia a
        JOIN postulacion.postulacion pp ON pp.id_postulacion = a.id_postulacion
        JOIN academico.estudiante est ON est.id_estudiante = pp.id_estudiante
        JOIN ayudantia.tipo_estado_ayudantia tea 
            ON tea.id_tipo_estado_ayudantia = a.id_tipo_estado_ayudantia
        WHERE est.id_usuario = :idUsuario
          AND tea.nombre_estado = 'EN_PROGRESO'
        LIMIT 1
        """, nativeQuery = true)
    Optional<Integer> findIdAyudantiaActivaByUsuario(@Param("idUsuario") Integer idUsuario);
}