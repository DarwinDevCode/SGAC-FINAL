package org.uteq.sgacfinal.security;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.uteq.sgacfinal.exception.AccesoDenegadoException;
import org.uteq.sgacfinal.exception.OposicionBusinessException;
import org.uteq.sgacfinal.repository.ayudantia.AsistenciaRepository;
@Service
@RequiredArgsConstructor
public class SecurityContextService {
    private final AsistenciaRepository asistenciaRepository;

    public UsuarioPrincipal obtenerPrincipal() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof UsuarioPrincipal principal)) {
            throw new AccesoDenegadoException("No hay sesión autenticada.");
        }
        return principal;
    }

    public Integer obtenerIdUsuario() {
        return obtenerPrincipal().getIdUsuario();
    }

    public String obtenerNombreUsuario() {
        return obtenerPrincipal().getUsername();
    }

    public Integer obtenerIdAyudantia() {
        Integer idUsuario   = obtenerIdUsuario();
        Integer idAyudantia = asistenciaRepository.obtenerIdAyudantiaPorUsuario(idUsuario);

        if (idAyudantia == null) {
            throw new OposicionBusinessException(
                    "No se encontró una ayudantía activa para su usuario.");
        }
        return idAyudantia;
    }

    public Integer obtenerIdRegistroActivo(Integer idAyudantia) {
        Integer idRegistro = asistenciaRepository.obtenerIdRegistroActivoPorAyudantia(idAyudantia);

        if (idRegistro == null) {
            throw new OposicionBusinessException(
                    "No se encontró un registro de actividad activo para la ayudantía.");
        }
        return idRegistro;
    }

    public Integer[] obtenerContextoAsistencia() {
        Integer idAyudantia = obtenerIdAyudantia();
        Integer idRegistro  = obtenerIdRegistroActivo(idAyudantia);
        return new Integer[]{ idAyudantia, idRegistro };
    }
}