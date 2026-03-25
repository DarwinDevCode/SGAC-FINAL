package org.uteq.sgacfinal.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TemaDTO {
    private Integer idTema;
    private String nombreTema;
    private Integer idMateria;
    private String nombreMateria;
}