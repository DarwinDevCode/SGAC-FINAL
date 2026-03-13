package org.uteq.sgacfinal.dto.Response.convocatorias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;
import java.util.List;


@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class ComisionDetalleResponseDTO {
    private boolean exito;
    private String  mensaje;
    private String  rol;
    private List<ComisionEstudianteDTO>   comisiones;
    private List<ConvocatoriaComisionDTO> convocatorias;
}
