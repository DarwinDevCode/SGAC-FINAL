package org.uteq.sgacfinal.dto.Response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DetallePostulacionResponseDTO {

    private Boolean exito;
    private String codigo;
    private String mensaje;

    private PostulacionInfoDTO postulacion;
    private ConvocatoriaPostulacionDTO convocatoria;

    private List<EtapaCronogramaDTO> cronograma;
    private List<DocumentoPostulacionDTO> documentos;

    @JsonProperty("total_documentos")
    private Integer totalDocumentos;

    @JsonProperty("resumen_documentos")
    private ResumenDocumentosDTO resumenDocumentos;
}