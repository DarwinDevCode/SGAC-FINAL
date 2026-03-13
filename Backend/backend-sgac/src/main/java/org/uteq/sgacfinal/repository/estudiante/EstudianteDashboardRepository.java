package org.uteq.sgacfinal.repository.estudiante;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.uteq.sgacfinal.entity.Convocatoria;

@Repository
public interface EstudianteDashboardRepository extends JpaRepository<Convocatoria, Integer> {

    /*
     * Usa la MISMA función del módulo de convocatorias del estudiante
     * para que el número de convocatorias abiertas sea consistente.
     */
    @Query(value = """
            SELECT COUNT(*)
            FROM convocatoria.fn_listar_convocatorias_estudiante(:idUsuario) c
            WHERE UPPER(COALESCE(c.estado_convocatoria, '')) = 'ABIERTA'
            """, nativeQuery = true)
    Integer countConvocatoriasAbiertasReales(@Param("idUsuario") Integer idUsuario);

    /*
     * Cuenta real de postulaciones activas del estudiante.
     */
    @Query(value = """
            SELECT COUNT(*)
            FROM postulacion.postulacion p
            WHERE p.id_estudiante = :idEstudiante
              AND p.activo = true
            """, nativeQuery = true)
    Integer countMisPostulaciones(@Param("idEstudiante") Integer idEstudiante);
}