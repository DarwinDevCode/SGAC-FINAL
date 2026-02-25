package org.uteq.sgacfinal.dto.Request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GestionPermisosMasivoRequestDTO {
    @NotEmpty(message = "La lista de permisos no puede estar vacía")
    @Valid
    private List<GestionPermisosRequestDTO> permisos;
}
