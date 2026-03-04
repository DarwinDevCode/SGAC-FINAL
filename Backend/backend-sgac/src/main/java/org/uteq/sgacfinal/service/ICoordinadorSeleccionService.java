package org.uteq.sgacfinal.service;

import org.uteq.sgacfinal.dto.Response.AsignaturaSelectableDTO;
import org.uteq.sgacfinal.dto.Response.DocenteSelectableDTO;

import java.util.List;

public interface ICoordinadorSeleccionService {

    List<DocenteSelectableDTO> listarDocentesSeleccionables();

    List<AsignaturaSelectableDTO> listarAsignaturasPorDocente(Integer idDocente);
}

