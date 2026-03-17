package org.uteq.sgacfinal.dto.Response.documentos;

import java.time.Instant;
import java.time.LocalDateTime;

public record DocumentoResponse(
        Integer id,
        String nombreMostrar,
        String rutaArchivo,
        String extension,
        Integer pesoBytes,
        Instant fechaSubida,
        String tipoNombre,
        String tipoCodigo,
        Integer idPeriodo,
        Integer idConvocatoria,
        boolean esGlobal
) {}