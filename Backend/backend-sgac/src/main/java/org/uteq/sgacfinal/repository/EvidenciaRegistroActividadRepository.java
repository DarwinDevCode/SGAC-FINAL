package org.uteq.sgacfinal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.uteq.sgacfinal.entity.EvidenciaRegistroActividad;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface EvidenciaRegistroActividadRepository extends JpaRepository<EvidenciaRegistroActividad, Integer> {

    @Query(value = "SELECT public.sp_crear_evidencia_actividad(:idRegistro, :tipo, :archivo, :nombre, :fecha)", nativeQuery = true)
    Integer registrarEvidencia(@Param("idRegistro") Integer idRegistroActividad,
                               @Param("tipo") String tipoEvidencia,
                               @Param("archivo") byte[] archivo,
                               @Param("nombre") String nombreArchivo,
                               @Param("fecha") LocalDate fechaSubida);

    @Query(value = "SELECT public.sp_actualizar_evidencia_actividad(:id, :tipo, :nombre, :archivo)", nativeQuery = true)
    Integer actualizarEvidencia(@Param("id") Integer idEvidencia,
                                @Param("tipo") String tipoEvidencia,
                                @Param("nombre") String nombreArchivo,
                                @Param("archivo") byte[] archivo);

    @Modifying
    @Query(value = "SELECT public.sp_desactivar_evidencia_actividad(:id)", nativeQuery = true)
    Integer desactivarEvidencia(@Param("id") Integer idEvidencia);

    @Query(value = "SELECT * FROM public.sp_obtener_evidencias_actividad(:idActividad)", nativeQuery = true)
    List<EvidenciaRegistroActividad> obtenerEvidenciasPorActividadSP(@Param("idActividad") Integer idRegistroActividad);


    @Query(value = """
        SELECT * FROM ayudantia.fn_evidencias_sesion(:idUsuario, :idRegistro)
        """, nativeQuery = true)
    List<Object[]> evidenciasSesion(
            @Param("idUsuario")  Integer idUsuario,
            @Param("idRegistro") Integer idRegistro
    );

//    @Modifying
//    @Query(value = """
//        UPDATE ayudantia.evidencia_registro_actividad
//        SET activo = false
//        WHERE id_evidencia_registro_actividad = :idEvidencia
//        """, nativeQuery = true)
//    void desactivarEvidencia(@Param("idEvidencia") Integer idEvidencia);
}