package org.uteq.sgacfinal.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class EvidenciaRequest {

    @NotNull(message = "El tipo de evidencia es obligatorio")
    private Integer idTipoEvidencia;

    @NotBlank(message = "El nombre del archivo es obligatorio")
    @Size(max = 150)
    private String nombreArchivo;

    @NotBlank(message = "La ruta del archivo es obligatoria")
    @Size(max = 500)
    private String rutaArchivo;

    @Size(max = 100)
    private String mimeType;

    @Positive(message = "El tamaño debe ser mayor a 0")
    private Integer tamanioBytes;
}