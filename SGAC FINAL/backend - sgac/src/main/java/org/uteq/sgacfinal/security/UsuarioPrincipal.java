package org.uteq.sgacfinal.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.uteq.sgacfinal.entity.Usuario;
import org.uteq.sgacfinal.entity.UsuarioTipoRol;

import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;

public class UsuarioPrincipal implements UserDetails {

    private final Usuario usuario;

    public UsuarioPrincipal(Usuario usuario) {
        this.usuario = usuario;
    }

    @Override
    public String getPassword() {
        return usuario.getContraseniaUsuario();
    }

    @Override
    public String getUsername() {
        return usuario.getNombreUsuario();
    }

    @Override
    public boolean isAccountNonExpired() { return true; }
    @Override
    public boolean isAccountNonLocked() { return true; }
    @Override
    public boolean isCredentialsNonExpired() { return true; }
    @Override
    public boolean isEnabled() { return usuario.getActivo(); }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if (usuario.getRoles() == null) return Collections.emptyList();

        return usuario.getRoles().stream()
                .filter(UsuarioTipoRol::getActivo)
                .map(utr -> new SimpleGrantedAuthority(utr.getTipoRol().getNombreTipoRol()))
                .collect(Collectors.toList());
    }
}