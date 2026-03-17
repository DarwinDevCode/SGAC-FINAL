package org.uteq.sgacfinal.dto.Request.documentos;

public record DocumentoInsertRequestDTO(
    String nombreMostrar,
    String rutaArchivo,
    String extension,
    Integer pesoBytes,
    Integer idTipoDoc,
    Integer idUsuario,
    Integer idFacultad,
    Integer idCarrera
) {}