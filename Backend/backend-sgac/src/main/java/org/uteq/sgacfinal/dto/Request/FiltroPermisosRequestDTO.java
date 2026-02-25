package org.uteq.sgacfinal.dto.Request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FiltroPermisosRequestDTO {
    @NotBlank(message = "El rol de base de datos es requerido")
    private String rolBd;

    @Builder.Default
    private String esquema = "todo";

    @Builder.Default
    private String categoria = "todo";

    @Builder.Default
    private String privilegio = "todo";
}