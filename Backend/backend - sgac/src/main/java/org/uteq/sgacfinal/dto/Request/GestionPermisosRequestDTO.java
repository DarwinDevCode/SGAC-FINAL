package org.uteq.sgacfinal.dto.Request;

import lombok.Data;

import java.util.List;

@Data
public class GestionPermisosRequestDTO {
    private String nombreRol;
    private String esquema;
    private String tabla;
    private List<PermisoDetalleDTO> permisos;

    @Data
    public static class PermisoDetalleDTO {
        private String privilegio;
        private boolean otorgar;
    }
}
