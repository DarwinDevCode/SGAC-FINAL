package org.uteq.sgacfinal.dto.Response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EsquemaCatalogoResponseDTO {
    private String esquema;
    private List<CategoriaCatalogoResponseDTO> categorias;
}