package org.uteq.sgacfinal.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.uteq.sgacfinal.entity.Usuario;
import org.uteq.sgacfinal.repository.IUsuariosRepository;
import org.uteq.sgacfinal.service.IUsuarioSesionService;
import org.uteq.sgacfinal.security.UsuarioPrincipal;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UsuarioSesionServiceImpl implements IUsuarioSesionService {

    private final IUsuariosRepository usuariosRepository;

    @Override
    public Integer getIdUsuarioAutenticado() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            throw new RuntimeException("No hay usuario autenticado");
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof UsuarioPrincipal usuarioPrincipal) {
            Integer id = usuarioPrincipal.getIdUsuario();
            if (id != null) {
                return id;
            }
        }

        if (principal instanceof UserDetails userDetails) {
            return usuariosRepository.findByNombreUsuarioWithRolesAndTipoRol(userDetails.getUsername())
                    .map(Usuario::getIdUsuario)
                    .orElseThrow(() -> new RuntimeException("Usuario autenticado no encontrado"));
        }

        throw new RuntimeException("No hay usuario autenticado");
    }
}
