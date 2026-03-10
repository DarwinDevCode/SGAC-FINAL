package org.uteq.sgacfinal.service;

import org.uteq.sgacfinal.entity.Postulacion;

public interface IPdfGeneratorService {
    
    /**
     * Generates the Acta de Méritos as a PDF byte array.
     */
    byte[] generarActaMeritos(Postulacion postulacion);

    /**
     * Generates the Acta de Oposición as a PDF byte array.
     */
    byte[] generarActaOposicion(Postulacion postulacion);

}
