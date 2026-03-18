package org.uteq.sgacfinal.dto.Response.ayudantia;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MatrizAsistenciaDTO {

    private List<SesionInfoDTO> sesiones;
    private List<EstudianteMatrizDTO> estudiantes;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SesionInfoDTO {
        private Integer id;
        private String fecha;
        private String tema;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EstudianteMatrizDTO {
        @JsonProperty("idParticipante")
        private Integer idParticipante;
        private String nombre;
        private String curso;
        private String paralelo;
        private Map<String, Boolean> asistencias;
    }
}