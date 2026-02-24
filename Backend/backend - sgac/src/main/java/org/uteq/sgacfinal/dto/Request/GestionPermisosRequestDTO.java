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

    @NotBlank(message = "El nombre del rol de base de datos es obligatorio")
    private String nombreRolBd;

    @NotBlank(message = "El esquema es obligatorio")
    private String esquema;

    @NotBlank(message = "El nombre del elemento es obligatorio")
    private String elemento;

    @NotBlank(message = "La categoría es obligatoria")
    private String categoria;

    @NotNull(message = "La lista de permisos no puede ser nula")
    private List<PermisoDetalleRequestDTO> permisos;
}