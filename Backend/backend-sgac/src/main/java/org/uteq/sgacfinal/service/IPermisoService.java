package org.uteq.sgacfinal.service;

import org.uteq.sgacfinal.dto.PermisoDTO;
import org.uteq.sgacfinal.dto.request.FiltroPermisosRequestDTO;
import org.uteq.sgacfinal.dto.request.GestionPermisosRequestDTO;
import org.uteq.sgacfinal.dto.response.*;

import java.util.List;

public interface IPermisoService {
    List<PermisoDTO> obtenerPermisos();
    List<PermisoRolResponseDTO> consultarPermisos(FiltroPermisosRequestDTO filtro);
    Boolean gestionarPermiso(GestionPermisosRequestDTO request);
//    List<ElementoBdResponseDTO> listarElementos(String esquema, String categoria);
    //List<EsquemaResponseDTO> listarEsquemas();

    List<String> listarEsquemas();
    List<TipoObjetoResponseDTO> listarTiposObjeto();
    List<String> listarElementos(String esquema, String tipoObjeto);
    List<PrivilegioResponseDTO> listarPrivilegios(Integer idTipoObjeto);

    ResultadoMasivoResponseDTO gestionarPermisosMasivo(List<GestionPermisosRequestDTO> permisos);
}
