package org.uteq.sgacfinal.util;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;
import org.uteq.sgacfinal.exception.AccesoDenegadoException;
import org.uteq.sgacfinal.security.UsuarioPrincipal;

import java.util.List;

@Component
public class ExtraerAuth {
    public record ExtraidoAuth(Integer idUsuario, String rol, String nombreUsuario) {}

    public ExtraidoAuth extraer(Authentication auth, String rolSesion) {
        if (auth == null || !(auth.getPrincipal() instanceof UsuarioPrincipal p)) {
            throw new AccesoDenegadoException("No autenticado.");
        }

        List<String> rolesPermitidos = auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        String rolFinal;
        if (rolSesion != null && rolesPermitidos.contains(rolSesion)) {
            rolFinal = rolSesion;
        } else {
            rolFinal = rolesPermitidos.get(0);
        }

        String nombre = p.getUsuario().getNombres() + " " + p.getUsuario().getApellidos();
        return new ExtraidoAuth(p.getIdUsuario(), rolFinal, nombre);
    }
}
