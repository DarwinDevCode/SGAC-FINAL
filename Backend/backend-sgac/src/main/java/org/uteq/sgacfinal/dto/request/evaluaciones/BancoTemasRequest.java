package org.uteq.sgacfinal.dto.request.evaluaciones;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class BancoTemasRequest {

    @NotNull(message = "El id de convocatoria es requerido")
    private Integer idConvocatoria;

    @NotNull(message = "La acción es requerida")
    private String accion;

    @Valid
    private List<TemaItem> temas;

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor
    public static class TemaItem {
        @NotNull(message = "La descripción del tema no puede ser nula")
        private String descripcionTema;
    }
}