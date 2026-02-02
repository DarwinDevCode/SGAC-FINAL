package org.uteq.sgacfinal.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.uteq.sgacfinal.entity.Usuario;
import org.uteq.sgacfinal.entity.UsuarioTipoRol;
import org.uteq.sgacfinal.repository.UsuarioRepository;
import lombok.*;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    @Override
    public UserDetails loadUserByUsername(String username)
            throws UsernameNotFoundException {

//        Usuario usuario = usuarioRepository
//                .orElseThrow(() ->
//                        new UsernameNotFoundException("Usuario no encontrado"));
//
//        List<GrantedAuthority> authorities =
//                usuario.getRoles().stream()
//                        .filter(UsuarioTipoRol::getActivo)
//                        .filter(ur -> ur.getTipoRol().getActivo())
//                        .map(ur -> (GrantedAuthority) new SimpleGrantedAuthority(
//                                "ROLE_" + ur.getTipoRol().getNombreTipoRol()))
//                        .toList();
//
//        return new org.springframework.security.core.userdetails.User(
//                usuario.getNombreUsuario(),
//                usuario.getContraseniaUsuario(),
//                authorities
//        );
        return null;
    }
}
