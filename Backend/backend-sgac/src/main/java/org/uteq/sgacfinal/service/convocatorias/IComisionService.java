package org.uteq.sgacfinal.service.convocatorias;

import org.uteq.sgacfinal.dto.Response.convocatorias.ComisionDetalleResponseDTO;
import org.uteq.sgacfinal.dto.Response.convocatorias.GenerarComisionesResponseDTO;

public interface IComisionService {
    GenerarComisionesResponseDTO generarComisionesAutomaticas();
    ComisionDetalleResponseDTO consultarComision(Integer idUsuario, String rol);
}