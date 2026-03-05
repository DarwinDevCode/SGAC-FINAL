package org.uteq.sgacfinal.dto.Response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** DTO ligero para combos de selección de docentes. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocenteSelectableDTO {
    private Integer idDocente;
    private String nombreCompleto;
}

