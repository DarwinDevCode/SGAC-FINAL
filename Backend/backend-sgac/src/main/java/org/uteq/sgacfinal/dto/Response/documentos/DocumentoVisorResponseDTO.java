package org.uteq.sgacfinal.dto.Response.documentos;

import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonAlias;

public record DocumentoVisorResponseDTO(
    @JsonAlias("id_documento") Integer idDocumento,
    @JsonAlias("nombre_mostrar") String nombreMostrar,
    @JsonAlias("ruta_archivo") String rutaArchivo,
    String extension,
    @JsonAlias("peso_bytes") Integer pesoBytes,
    @JsonAlias("fecha_subida") LocalDateTime fechaSubida,
    @JsonAlias("tipo_documento") String tipoDocumento,
    @JsonAlias("nombre_facultad") String nombreFacultad,
    @JsonAlias("nombre_carrera") String nombreCarrera
) {}