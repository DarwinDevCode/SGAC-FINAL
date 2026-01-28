package org.uteq.sgacfinal.security.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.uteq.sgacfinal.entity.Usuario;
import org.uteq.sgacfinal.repository.UsuarioRepository;

import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        Usuario usuario = usuarioRepository
                .findByNombreUsuarioAndActivoTrue(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));

        return new User(
                usuario.getNombreUsuario(),
                usuario.getContraseniaUsuario(),
                usuario.getUsuarioTipoRoles()
                        .stream()
                        .filter(r -> r.getActivo())
                        .map(r -> new SimpleGrantedAuthority(r.getTipoRol().getNombreTipoRol()))
                        .collect(Collectors.toList())
        );
    }
}
