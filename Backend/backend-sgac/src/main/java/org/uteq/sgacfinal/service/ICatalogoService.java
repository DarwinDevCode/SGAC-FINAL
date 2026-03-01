package org.uteq.sgacfinal.service;

import org.uteq.sgacfinal.dto.Response.TipoEstadoAyudantiaResponse;
import org.uteq.sgacfinal.dto.Response.TipoEstadoEvidenciaResponse;
import org.uteq.sgacfinal.dto.Response.TipoEstadoRegistroResponse;
import org.uteq.sgacfinal.dto.Response.TipoEvidenciaResponse;

import java.util.List;

public interface ICatalogoService {
    List<TipoEstadoRegistroResponse> estadosRegistro();
    List<TipoEstadoEvidenciaResponse>  estadosEvidencia();
    List<TipoEvidenciaResponse>        tiposEvidencia();
    List<TipoEstadoAyudantiaResponse>  estadosAyudantia();
}
