package org.uteq.sgacfinal.repository.documentos;

import java.time.LocalDateTime;

public interface DocumentoVisorProjection {
    Integer       getIdDocumento();
    String        getNombreMostrar();
    String        getRutaArchivo();
    String        getExtension();
    Integer       getPesoBytes();
    LocalDateTime getFechaSubida();
    Integer       getIdTipoDocumento();
    String        getTipoNombre();
    String        getTipoCodigo();
    Integer       getIdPeriodo();
    Integer       getIdConvocatoria();
    Boolean       getEsGlobal();
}