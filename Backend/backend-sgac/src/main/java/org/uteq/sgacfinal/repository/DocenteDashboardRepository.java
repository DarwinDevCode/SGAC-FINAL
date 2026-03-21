package org.uteq.sgacfinal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.uteq.sgacfinal.entity.Convocatoria;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface DocenteDashboardRepository extends JpaRepository<Convocatoria, Integer> {

    @Query(value = "SELECT COUNT(*) FROM convocatoria.convocatoria c WHERE c.id_docente = :idDocente AND c.activo = true AND c.estado = 'ABIERTA'", nativeQuery = true)
    Integer countConvocatoriasActivas(@Param("idDocente") Integer idDocente);

    // INTACTO (Ya lo habías corregido muy bien)
    @Query(value = """
            SELECT COUNT(*) FROM postulacion.postulacion p
            JOIN convocatoria.convocatoria c ON c.id_convocatoria = p.id_convocatoria
            JOIN postulacion.tipo_estado_postulacion tep ON tep.id_tipo_estado_postulacion = p.id_tipo_estado_postulacion
            WHERE c.id_docente = :idDocente
              AND p.activo = true
              AND tep.codigo = 'PENDIENTE'
            """, nativeQuery = true)
    Integer countPostulacionesPendientes(@Param("idDocente") Integer idDocente);

    // MEJORADO: Eliminado el ID '1'. Ahora busca por el código 'ACTIVO'
    @Query(value = """
            SELECT COUNT(DISTINCT a.id_ayudantia) FROM ayudantia.ayudantia a
            JOIN postulacion.postulacion p ON p.id_postulacion = a.id_postulacion
            JOIN convocatoria.convocatoria c ON c.id_convocatoria = p.id_convocatoria
            JOIN ayudantia.tipo_estado_ayudantia tea ON tea.id_tipo_estado_ayudantia = a.id_tipo_estado_ayudantia
            WHERE c.id_docente = :idDocente
              AND tea.codigo = 'ACTIVO'
            """, nativeQuery = true)
    Integer countAyudantesAsignados(@Param("idDocente") Integer idDocente);

    // MEJORADO: Eliminado el ID '17'. Ahora busca por el código 'PENDIENTE'
    @Query(value = """
            SELECT COUNT(*) FROM ayudantia.registro_actividad ra
            JOIN ayudantia.ayudantia a ON a.id_ayudantia = ra.id_ayudantia
            JOIN postulacion.postulacion p ON p.id_postulacion = a.id_postulacion
            JOIN convocatoria.convocatoria c ON c.id_convocatoria = p.id_convocatoria
            JOIN ayudantia.tipo_estado_registro ter ON ter.id_tipo_estado_registro = ra.id_tipo_estado_registro
            WHERE c.id_docente = :idDocente
              AND ter.codigo = 'PENDIENTE'
            """, nativeQuery = true)
    Integer countActividadesPorRevisar(@Param("idDocente") Integer idDocente);

    interface UltimaActividadProjection {
        LocalDate getFecha();
        String getNombreEstudiante();
        String getTema();
        Integer getIdRegistro();
    }

    // INTACTO
    @Query(value = """
            SELECT ra.fecha AS fecha, concat(u.nombres, ' ', u.apellidos) AS nombreEstudiante,
                   ra.tema_tratado AS tema, ra.id_registro_actividad AS idRegistro
            FROM ayudantia.registro_actividad ra
            JOIN ayudantia.ayudantia a ON a.id_ayudantia = ra.id_ayudantia
            JOIN postulacion.postulacion p ON p.id_postulacion = a.id_postulacion
            JOIN convocatoria.convocatoria c ON c.id_convocatoria = p.id_convocatoria
            JOIN academico.estudiante e ON e.id_estudiante = p.id_estudiante
            JOIN seguridad.usuario u ON u.id_usuario = e.id_usuario
            WHERE c.id_docente = :idDocente
            ORDER BY ra.fecha DESC, ra.id_registro_actividad DESC
            LIMIT 5
            """, nativeQuery = true)
    List<UltimaActividadProjection> findUltimasActividades(@Param("idDocente") Integer idDocente);
}