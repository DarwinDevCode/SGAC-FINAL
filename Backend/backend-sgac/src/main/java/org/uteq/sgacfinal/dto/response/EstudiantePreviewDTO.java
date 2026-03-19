package org.uteq.sgacfinal.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EstudiantePreviewDTO {
    private int fila;
    private String nombreCompleto;
    private String curso;
    private String paralelo;
    private boolean valida;
    private boolean yaExiste;
    private List<String> errores;
}