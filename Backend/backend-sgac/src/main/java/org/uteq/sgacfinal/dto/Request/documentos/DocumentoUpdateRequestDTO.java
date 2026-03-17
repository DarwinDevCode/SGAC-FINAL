package org.uteq.sgacfinal.dto.Request.documentos;

public record DocumentoUpdateRequestDTO(
    Integer idDocumento,
    String nombreMostrar,
    Integer idTipoDoc,
    Integer idFacultad,
    Integer idCarrera,
    Integer idUsuario
) {}
