package org.uteq.sgacfinal.dto.Response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de respuesta para la operación de subsanación de documento observado.
 * Mapea el resultado de fn_subsanar_documento_estudiante.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubsanacionDocumentoResponseDTO {

    /**
     * Indica si la operación fue exitosa
     */
    private Boolean exito;

    /**
     * Código de estado/error (ej: "DOC_NO_OBSERVADO", "FUERA_PERIODO", etc.)
     */
    private String codigo;

    /**
     * Mensaje descriptivo del resultado
     */
    private String mensaje;

    /**
     * ID del requisito adjunto actualizado
     */
    @JsonProperty("id_requisito_adjunto")
    private Integer idRequisitoAdjunto;

    /**
     * Nuevo estado del documento (CORREGIDO)
     */
    @JsonProperty("nuevo_estado")
    private String nuevoEstado;

    /**
     * Indica si se envió notificación al coordinador
     */
    @JsonProperty("notificacion_enviada")
    private Boolean notificacionEnviada;

    /**
     * Método de fábrica para crear respuesta de error
     */
    public static SubsanacionDocumentoResponseDTO error(String codigo, String mensaje) {
        return SubsanacionDocumentoResponseDTO.builder()
                .exito(false)
                .codigo(codigo)
                .mensaje(mensaje)
                .build();
    }

    /**
     * Método de fábrica para crear respuesta exitosa
     */
    public static SubsanacionDocumentoResponseDTO exitoso(Integer idRequisito, String mensaje) {
        return SubsanacionDocumentoResponseDTO.builder()
                .exito(true)
                .codigo("OK")
                .mensaje(mensaje)
                .idRequisitoAdjunto(idRequisito)
                .nuevoEstado("CORREGIDO")
                .notificacionEnviada(true)
                .build();
    }
}

