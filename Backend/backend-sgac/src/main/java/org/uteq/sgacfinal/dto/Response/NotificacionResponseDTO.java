package org.uteq.sgacfinal.dto.Response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificacionResponseDTO {
    private Integer idNotificacion;
    private Integer idUsuario;
    private String  mensaje;
    private LocalDateTime fechaEnvio;
    private Boolean leida;   // mapeado desde entidad.leido
    private String  tipo;
    private String  tipoNotificacion;
    private Integer idConvocatoria;
}
