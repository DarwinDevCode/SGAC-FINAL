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
public class PreviewPadronDTO {
    private boolean exito;
    private boolean tieneErrores;
    private int totalFilas;
    private int nuevos;
    private int existentes;
    private String nombreArchivo;
    private List<EstudiantePreviewDTO> filas;
    private String mensaje;
}