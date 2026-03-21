package org.uteq.sgacfinal.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ConvocatoriasEstudianteWrapperDTO {
    private Boolean exito;
    private String mensaje;
    private Integer totalConvocatorias;
    private List<ConvocatoriaEstudianteResponseDTO> convocatorias;
    public static ConvocatoriasEstudianteWrapperDTO exitoso(List<ConvocatoriaEstudianteResponseDTO> convocatorias) {
        return ConvocatoriasEstudianteWrapperDTO.builder()
                .exito(true)
                .mensaje("Convocatorias obtenidas exitosamente")
                .totalConvocatorias(convocatorias != null ? convocatorias.size() : 0)
                .convocatorias(convocatorias)
                .build();
    }
    public static ConvocatoriasEstudianteWrapperDTO error(String mensajeError) {
        return ConvocatoriasEstudianteWrapperDTO.builder()
                .exito(false)
                .mensaje(mensajeError)
                .totalConvocatorias(0)
                .convocatorias(List.of())
                .build();
    }
}

