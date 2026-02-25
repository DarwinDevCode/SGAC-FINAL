package org.uteq.sgacfinal.dto.Request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FacultadRequestDTO {
    @NotBlank
    private String nombreFacultad;
}