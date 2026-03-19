package org.uteq.sgacfinal.dto.response.ayudantia;
import com.fasterxml.jackson.annotation.JsonAlias;

public record EstudianteAsistenciaDTO(
        @JsonAlias("id_detalle") Integer idDetalle,
        @JsonAlias("nombre_completo") String nombreCompleto,
        String curso,
        String paralelo,
        Boolean asistio
) {}