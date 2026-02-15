package org.uteq.sgacfinal.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.uteq.sgacfinal.dto.Request.PeriodoAcademicoRequisitoPostulacionRequestDTO;
import org.uteq.sgacfinal.dto.Response.PeriodoAcademicoRequisitoPostulacionResponseDTO;
import org.uteq.sgacfinal.entity.PeriodoAcademicoRequisitoPostulacion;
import org.uteq.sgacfinal.repository.PeriodoAcademicoRequisitoPostulacionRepository;
import org.uteq.sgacfinal.service.IPeriodoAcademicoRequisitoPostulacionService;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class PeriodoAcademicoRequisitoPostulacionServiceImpl implements IPeriodoAcademicoRequisitoPostulacionService {

    private final PeriodoAcademicoRequisitoPostulacionRepository repository;

    @Override
    public PeriodoAcademicoRequisitoPostulacionResponseDTO crear(PeriodoAcademicoRequisitoPostulacionRequestDTO request) {
        Integer idGenerado = repository.registrarConfiguracionRequisito(
                request.getIdPeriodoAcademico(),
                request.getIdTipoRequisitoPostulacion(),
                request.getObligatorio(),
                request.getOrden()
        );

        if (idGenerado == -1) {
            throw new RuntimeException("Error al configurar requisito para el periodo.");
        }

        return buscarPorId(idGenerado);
    }

    @Override
    public PeriodoAcademicoRequisitoPostulacionResponseDTO actualizar(Integer id, PeriodoAcademicoRequisitoPostulacionRequestDTO request) {
        Integer resultado = repository.actualizarConfiguracionRequisito(
                id,
                request.getObligatorio(),
                request.getOrden()
        );

        if (resultado == -1) {
            throw new RuntimeException("Error al actualizar configuración de requisito.");
        }

        return buscarPorId(id);
    }

    @Override
    public void desactivar(Integer id) {
        Integer resultado = repository.desactivarConfiguracionRequisito(id);
        if (resultado == -1) {
            throw new RuntimeException("Error al desactivar configuración de requisito.");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public PeriodoAcademicoRequisitoPostulacionResponseDTO buscarPorId(Integer id) {
        PeriodoAcademicoRequisitoPostulacion entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Configuración no encontrada con ID: " + id));
        return mapearADTO(entity);
    }

//    @Override
//    @Transactional(readOnly = true)
//    public List<PeriodoAcademicoRequisitoPostulacionResponseDTO> listarPorPeriodo(Integer idPeriodo) {
//        return repository.findByPeriodoAcademico_IdPeriodoAcademicoAndActivoTrueOrderByOrdenAsc(idPeriodo).stream()
//                .map(this::mapearADTO)
//                .collect(Collectors.toList());
//
//    }

    private PeriodoAcademicoRequisitoPostulacionResponseDTO mapearADTO(PeriodoAcademicoRequisitoPostulacion entidad) {
        return PeriodoAcademicoRequisitoPostulacionResponseDTO.builder()
                .idPeriodoAcademicoRequisitoPostulacion(entidad.getIdPeriodoAcademicoRequisitoPostulacion())
                .idPeriodoAcademico(entidad.getPeriodoAcademico().getIdPeriodoAcademico())
                .nombrePeriodo(entidad.getPeriodoAcademico().getNombrePeriodo())
                .idTipoRequisitoPostulacion(entidad.getTipoRequisitoPostulacion().getIdTipoRequisitoPostulacion())
                .nombreRequisito(entidad.getTipoRequisitoPostulacion().getNombreRequisito())
                .obligatorio(entidad.getObligatorio())
                .orden(entidad.getOrden())
                .activo(entidad.getActivo())
                .build();
    }
}