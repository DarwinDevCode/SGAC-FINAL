package org.uteq.sgacfinal.service;

import org.uteq.sgacfinal.dto.response.TipoEstadoAyudantiaResponse;
import org.uteq.sgacfinal.dto.response.TipoEstadoEvidenciaResponse;
import org.uteq.sgacfinal.dto.response.TipoEstadoRegistroResponse;
import org.uteq.sgacfinal.dto.response.TipoEvidenciaResponse;

import java.util.List;

public interface ICatalogoService {
    List<TipoEstadoRegistroResponse> estadosRegistro();
    List<TipoEstadoEvidenciaResponse>  estadosEvidencia();
    List<TipoEvidenciaResponse>        tiposEvidencia();
    List<TipoEstadoAyudantiaResponse>  estadosAyudantia();
}
