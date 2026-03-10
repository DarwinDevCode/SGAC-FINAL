package org.uteq.sgacfinal.mapper;

import org.uteq.sgacfinal.dto.Response.ConvocatoriaResponseDTO;
import org.uteq.sgacfinal.entity.Convocatoria;

public class ConvocatoriaMapper {

    public static ConvocatoriaResponseDTO toDTO(Convocatoria entity) {
        if (entity == null) return null;

        Integer idUsuarioDoc = (entity.getDocente() != null && entity.getDocente().getUsuario() != null)
                ? entity.getDocente().getUsuario().getIdUsuario() : null;

        Integer idUsuarioCoord = null;
        if (entity.getAsignatura() != null && entity.getAsignatura().getCarrera() != null) {
            idUsuarioCoord = entity.getAsignatura().getCarrera().getCoordinadores().stream()
                    .filter(c -> c.getActivo() != null && c.getActivo())
                    .findFirst()
                    .map(c -> c.getUsuario() != null ? c.getUsuario().getIdUsuario() : null)
                    .orElse(null);
        }

        return ConvocatoriaResponseDTO.builder()
                .idConvocatoria(entity.getIdConvocatoria())
                .nombrePeriodo(entity.getPeriodoAcademico() != null ? entity.getPeriodoAcademico().getNombrePeriodo() : "N/A")
                .nombreAsignatura(entity.getAsignatura() != null ? entity.getAsignatura().getNombreAsignatura() : "N/A")
                .cuposDisponibles(entity.getCuposDisponibles())
                .fechaPublicacion(entity.getFechaPublicacion())
                .idPeriodoAcademico(entity.getPeriodoAcademico().getIdPeriodoAcademico())
                .idAsignatura(entity.getAsignatura().getIdAsignatura())
                .idDocente(entity.getDocente().getIdDocente())
                .nombreDocente(entity.getDocente().getUsuario().getNombres() + " " + entity.getDocente().getUsuario().getApellidos())
                .idConvocatoria(entity.getIdConvocatoria())
                .idUsuarioDocente(idUsuarioDoc)
                .idUsuarioCoordinador(idUsuarioCoord)
                .fechaCierre(entity.getFechaCierre())
                .estado(entity.getEstado())
                .activo(entity.getActivo())
                .build();
    }
}