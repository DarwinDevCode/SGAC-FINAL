package org.uteq.sgacfinal.repository.documentos;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.uteq.sgacfinal.entity.DocumentoAcademico;

import java.util.List;

@Repository
public interface DocumentoAcademicoRepository extends JpaRepository<DocumentoAcademico, Integer> {

    @Query("""
        SELECT d FROM DocumentoAcademico d 
        JOIN FETCH d.idTipoDocumento t
        WHERE d.activo = true 
          AND d.idPeriodo.idPeriodoAcademico = :idPeriodo
          AND (d.idConvocatoria IS NULL OR d.idConvocatoria.idConvocatoria = :idConvocatoria)
        ORDER BY d.fechaSubida DESC
    """)
    List<DocumentoAcademico> buscarDocumentosPorContexto(
            @Param("idPeriodo") Integer idPeriodo,
            @Param("idConvocatoria") Integer idConvocatoria
    );
}