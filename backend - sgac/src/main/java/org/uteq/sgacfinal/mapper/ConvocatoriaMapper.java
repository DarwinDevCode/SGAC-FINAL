package org.uteq.sgacfinal.mapper;

import org.uteq.sgacfinal.dto.Response.ConvocatoriaResponseDTO;
import org.uteq.sgacfinal.entity.Convocatoria;

public class ConvocatoriaMapper {

    public static ConvocatoriaResponseDTO toDTO(Convocatoria entity) {
        if (entity == null) return null;

        return ConvocatoriaResponseDTO.builder()
                .idConvocatoria(entity.getIdConvocatoria())
                .nombrePeriodo(entity.getPeriodoAcademico() != null ? entity.getPeriodoAcademico().getIdPeriodoAcademico().toString() : "N/A")
                .nombreAsignatura(entity.getAsignatura() != null ? entity.getAsignatura().getNombreAsignatura() : "N/A")
                .cuposDisponibles(entity.getCuposDisponibles())
                .fechaPublicacion(entity.getFechaPublicacion())
                .idPeriodoAcademico(entity.getPeriodoAcademico().getIdPeriodoAcademico())
                .idAsignatura(entity.getAsignatura().getIdAsignatura())
                .idDocente(entity.getDocente().getIdDocente())
                .fechaCierre(entity.getFechaCierre())
                .estado(entity.getEstado())
                .activo(entity.getActivo())
                .build();
    }
}