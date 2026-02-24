package org.uteq.sgacfinal.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

public class AdminReporteGlobalDTO {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UsuarioDTO {
        private String usuario;
        private String email;
        private String roles;
        private String estado;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PersonalDTO {
        private String nombre;
        private String cargoContexto;
        private String estado;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PostulanteDTO {
        private String estudiante;
        private String cedula;
        private String asignatura;
        private String periodo;
        private String estado;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AyudanteDTO {
        private String estudiante;
        private String asignatura;
        private String docente;
        private Double horas;
        private String estado;
    }
}
