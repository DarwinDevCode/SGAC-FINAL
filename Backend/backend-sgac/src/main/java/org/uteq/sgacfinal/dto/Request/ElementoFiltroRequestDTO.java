package org.uteq.sgacfinal.dto.Request;

import lombok.Data;

@Data
public class ElementoFiltroRequestDTO {
    private String esquema;
    private String tipoObjeto; // Ej: 'TABLA', 'FUNCION'
}
