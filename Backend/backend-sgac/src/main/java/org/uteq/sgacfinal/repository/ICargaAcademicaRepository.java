package org.uteq.sgacfinal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.uteq.sgacfinal.entity.Docente;

@Repository
public interface ICargaAcademicaRepository extends JpaRepository<Docente, Integer> {
    @Query(value = "SELECT academico.fn_listar_docentes_activos()", nativeQuery = true)
    String listarDocentesActivos();

    @Query(value = "SELECT academico.fn_listar_jerarquia_asignaturas()", nativeQuery = true)
    String listarJerarquiaAsignaturas();

    @Query(value = """
            SELECT academico.fn_gestionar_carga_docente(
                :idDocente,
                cast(:asignaturasIds as integer[])
            )
            """, nativeQuery = true)
    String gestionarCargaDocente(
            @Param("idDocente") Integer idDocente,
            @Param("asignaturasIds") String asignaturasIds
    );
}