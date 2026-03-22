package org.uteq.sgacfinal.repository.convocatorias;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.uteq.sgacfinal.entity.Convocatoria;

@Repository
public interface IFinalizarSeleccionRepository
        extends JpaRepository<Convocatoria, Integer> {

    @Query(value = """
        SELECT CAST(
            postulacion.fn_finalizar_proceso_seleccion(:pIdConvocatoria)
        AS text)
        """, nativeQuery = true)
    String finalizarProcesoSeleccion(
            @Param("pIdConvocatoria") Integer idConvocatoria
    );
}