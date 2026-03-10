package org.uteq.sgacfinal.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.uteq.sgacfinal.entity.Coordinador;
import org.uteq.sgacfinal.exception.CoordinadorNoValidoException;
import org.uteq.sgacfinal.repository.CoordinadorContextRepository;
import org.uteq.sgacfinal.service.ICoordinadorContextService;
import org.uteq.sgacfinal.service.IUsuarioSesionService;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CoordinadorContextServiceImpl implements ICoordinadorContextService {

    private final IUsuarioSesionService usuarioSesionService;
    private final CoordinadorContextRepository coordinadorRepository;

    @Override
    public Integer getIdCarreraCoordinadorAutenticado() {
        Integer idUsuario = usuarioSesionService.getIdUsuarioAutenticado();

        Coordinador coordinador = coordinadorRepository.findByUsuario_IdUsuarioAndActivoTrue(idUsuario)
                .orElseThrow(() -> new CoordinadorNoValidoException("El usuario autenticado no es un coordinador válido"));

        if (coordinador.getCarrera() == null || coordinador.getCarrera().getIdCarrera() == null) {
            throw new CoordinadorNoValidoException("El coordinador no tiene carrera asignada");
        }

        return coordinador.getCarrera().getIdCarrera();
    }
}

