package org.uteq.sgacfinal.service;

import org.springframework.transaction.annotation.Transactional;
import org.uteq.sgacfinal.dto.PermisoDTO;
import org.uteq.sgacfinal.dto.Request.FiltroPermisosRequestDTO;
import org.uteq.sgacfinal.dto.Request.GestionPermisosRequestDTO;
import org.uteq.sgacfinal.dto.Response.*;

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
