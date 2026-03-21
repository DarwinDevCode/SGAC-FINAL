package org.uteq.sgacfinal.dto.request.ayudantia;

public record CargarEvidenciaRequestDTO(
    Integer idUsuario,
    Integer idRegistro,
    String nombreArchivo,
    String rutaArchivo,
    String mimeType,
    Integer tamanioBytes,
    Integer idTipoEvidencia
) {}