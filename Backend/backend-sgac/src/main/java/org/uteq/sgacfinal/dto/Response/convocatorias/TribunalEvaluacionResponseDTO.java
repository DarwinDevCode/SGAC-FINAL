package org.uteq.sgacfinal.dto.Response.convocatorias;

import java.util.List;

public record TribunalEvaluacionResponseDTO(
        String comision,
        EvaluacionOposicionResponseDTO evaluacion,
        List<MiembroTribunalResponseDTO> miembros
) {}