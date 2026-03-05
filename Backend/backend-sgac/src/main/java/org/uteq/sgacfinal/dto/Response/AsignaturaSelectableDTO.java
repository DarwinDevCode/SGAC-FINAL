package org.uteq.sgacfinal.dto.Response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** DTO ligero para combos de selección de asignaturas. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AsignaturaSelectableDTO {
    private Integer idAsignatura;
    private String nombreAsignatura;
}

