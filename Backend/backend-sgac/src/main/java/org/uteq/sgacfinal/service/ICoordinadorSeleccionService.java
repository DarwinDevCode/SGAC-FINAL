package org.uteq.sgacfinal.service;

import org.uteq.sgacfinal.dto.response.AsignaturaSelectableDTO;
import org.uteq.sgacfinal.dto.response.DocenteSelectableDTO;

import java.util.List;

public interface ICoordinadorSeleccionService {

    List<DocenteSelectableDTO> listarDocentesSeleccionables();

    List<AsignaturaSelectableDTO> listarAsignaturasPorDocente(Integer idDocente);
}

