package org.uteq.sgacfinal.dto.response.ayudantia;

import com.fasterxml.jackson.annotation.JsonAlias;

public record EvidenciaEliminadaResponseDTO(
    @JsonAlias("id_evidencia_eliminada") Integer idEvidenciaEliminada,
    @JsonAlias("ruta_archivo") String rutaArchivo
) {}