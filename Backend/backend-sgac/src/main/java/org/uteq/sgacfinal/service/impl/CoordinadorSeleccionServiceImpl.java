package org.uteq.sgacfinal.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.uteq.sgacfinal.dto.response.AsignaturaSelectableDTO;
import org.uteq.sgacfinal.dto.response.DocenteSelectableDTO;
import org.uteq.sgacfinal.repository.CoordinadorSeleccionRepository;
import org.uteq.sgacfinal.service.ICoordinadorContextService;
import org.uteq.sgacfinal.service.ICoordinadorSeleccionService;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CoordinadorSeleccionServiceImpl implements ICoordinadorSeleccionService {

    private final ICoordinadorContextService coordinadorContextService;
    private final CoordinadorSeleccionRepository seleccionRepository;

    @Override
    public List<DocenteSelectableDTO> listarDocentesSeleccionables() {
        Integer idCarrera = coordinadorContextService.getIdCarreraCoordinadorAutenticado();
        return seleccionRepository.listarDocentesSeleccionables(idCarrera);
    }

    @Override
    public List<AsignaturaSelectableDTO> listarAsignaturasPorDocente(Integer idDocente) {
        // Si un docente no tiene asignaturas activas, devolverá lista vacía.
        return seleccionRepository.listarAsignaturasPorDocente(idDocente);
    }
}

