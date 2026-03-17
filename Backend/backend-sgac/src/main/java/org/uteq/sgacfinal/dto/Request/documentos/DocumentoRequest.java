package org.uteq.sgacfinal.dto.Request.documentos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record DocumentoRequest(
        @NotBlank(message = "El nombre es obligatorio")
        String nombreMostrar,

        @NotNull(message = "El tipo de documento es obligatorio")
        Integer idTipoDocumento,

        @NotNull(message = "El periodo académico es obligatorio")
        Integer idPeriodo,

        Integer idConvocatoria
) {}