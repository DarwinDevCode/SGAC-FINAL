package org.uteq.sgacfinal.dto.response.ayudantia;

import com.fasterxml.jackson.annotation.JsonAlias;

public record FinalizarSesionResponseDTO(
    @JsonAlias("id_registro") Integer idRegistro,
    @JsonAlias("resumen_asistencia") ResumenAsistenciaDTO resumenAsistencia,
    @JsonAlias("evidencias_adjuntas") Integer evidenciasAdjuntas
) {}