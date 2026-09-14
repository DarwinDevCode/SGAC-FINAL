package org.uteq.sgacfinal.dto.request;

import lombok.Data;

@Data
public class IaAsistenteRequestDTO {
    private String tipoConsulta; // REGLAMENTO, REVISION_TEXTO, SUGERENCIA_RUBRICA
    private String prompt;
}
