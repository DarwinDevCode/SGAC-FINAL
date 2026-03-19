package org.uteq.sgacfinal.dto.response.ayudantia;

import com.fasterxml.jackson.annotation.JsonAlias;

public record EvidenciaResponseDTO(
    @JsonAlias("id_evidencia") Integer idEvidencia,
    @JsonAlias("nombre_archivo") String nombreArchivo,
    @JsonAlias("ruta_archivo") String rutaArchivo,
    @JsonAlias("mime_type") String mimeType,
    @JsonAlias("tamanio_bytes") Long tamanioBytes,
    @JsonAlias("fecha_subida") String fechaSubida
) {}