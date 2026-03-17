package org.uteq.sgacfinal.repository.documentos;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.uteq.sgacfinal.entity.DocumentoAcademico;

import java.util.List;
import java.util.Optional;

@Repository
public interface DocumentoAcademicoRepository
        extends JpaRepository<DocumentoAcademico, Integer> {

    @Query(nativeQuery = true, value = """
        SELECT * FROM ayudantia.fn_listar_documentos_visor(:idConvocatoria)
    """)
    List<DocumentoVisorProjection> listarParaVisor(
            @Param("idConvocatoria") Integer idConvocatoria
    );

    @Query(nativeQuery = true, value = """
        SELECT * FROM convocatoria.fn_listar_convocatorias_activas_periodo()
    """)
    List<ConvocatoriaActivaProjection> listarConvocatoriasActivasPeriodo();

    @Query(nativeQuery = true, value = """
        SELECT id_tipo_documento, nombre, codigo
        FROM   ayudantia.fn_listar_tipo_documentos_activos()
    """)
    List<TipoDocumentoProjection> listarTiposActivos();

    @Query(nativeQuery = true, value = """
        SELECT da.id_documento,
               da.nombre_mostrar,
               da.ruta_archivo,
               da.extension,
               da.peso_bytes,
               da.fecha_subida,
               da.id_tipo_documento,
               td.nombre  AS tipo_nombre,
               td.codigo  AS tipo_codigo,
               da.id_periodo,
               da.id_convocatoria,
               (da.id_convocatoria IS NULL) AS es_global
        FROM   ayudantia.documento_academico da
        JOIN   ayudantia.tipo_documento      td
               ON td.id_tipo_documento = da.id_tipo_documento
        WHERE  da.id_documento = :id
          AND  da.activo       = TRUE
    """)
    Optional<DocumentoVisorProjection> findDetalleById(@Param("id") Integer id);
}