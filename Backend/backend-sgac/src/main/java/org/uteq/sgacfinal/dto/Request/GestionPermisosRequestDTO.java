package org.uteq.sgacfinal.dto.Request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GestionPermisosRequestDTO {
    @NotBlank(message = "El rol es obligatorio")
    private String rolBd;

    @NotBlank(message = "El esquema es obligatorio")
    private String esquema;

    @NotBlank(message = "El elemento es obligatorio")
    private String elemento;

    @NotBlank(message = "La categoría es obligatoria")
    private String categoria;

    @NotBlank(message = "El privilegio es obligatorio")
    private String privilegio;

    @NotNull(message = "Debe especificar si va a otorgar o revocar")
    private Boolean otorgar;
}