package org.uteq.sgacfinal.service.impl;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.uteq.sgacfinal.dto.Request.ConvocatoriaRequestDTO;
import org.uteq.sgacfinal.dto.Response.ConvocatoriaResponseDTO;
import org.uteq.sgacfinal.entity.Asignatura;
import org.uteq.sgacfinal.entity.Convocatoria;
import org.uteq.sgacfinal.entity.Docente;
import org.uteq.sgacfinal.entity.PeriodoAcademico;
import org.uteq.sgacfinal.mapper.ConvocatoriaMapper;
import org.uteq.sgacfinal.repository.DocenteRepository;
import org.uteq.sgacfinal.repository.IAsignaturaRepository;
import org.uteq.sgacfinal.repository.IConvocatoriaRepository;
import org.uteq.sgacfinal.repository.IPeriodoAcademicoRepository;
import org.uteq.sgacfinal.service.IConvocatoriaService;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ConvocatoriaServiceImpl implements IConvocatoriaService {
    private final IConvocatoriaRepository convocatoriaRepo;
    private final IPeriodoAcademicoRepository periodoRepo;
    private final IAsignaturaRepository asignaturaRepo;
    private final DocenteRepository docenteRepo;
    @Override
    @Transactional
    public ConvocatoriaResponseDTO create(ConvocatoriaRequestDTO dto) {
        Convocatoria convocatoria = new Convocatoria();
        mapDtoToEntity(dto, convocatoria);
        return ConvocatoriaMapper.toDTO(convocatoriaRepo.save(convocatoria));
    }

    @Override
    @Transactional
    public ConvocatoriaResponseDTO update(ConvocatoriaRequestDTO dto) {
        Convocatoria convocatoria = convocatoriaRepo.findById(dto.getIdConvocatoria())
                .orElseThrow(() -> new EntityNotFoundException("Convocatoria no encontrada"));
        mapDtoToEntity(dto, convocatoria);
        return ConvocatoriaMapper.toDTO(convocatoriaRepo.save(convocatoria));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ConvocatoriaResponseDTO> findAll() {
        return convocatoriaRepo.findAll().stream()
                .map(ConvocatoriaMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ConvocatoriaResponseDTO findById(Integer id) {
        return convocatoriaRepo.findById(id)
                .map(ConvocatoriaMapper::toDTO)
                .orElseThrow(() -> new EntityNotFoundException("Convocatoria no encontrada"));
    }

    @Override
    @Transactional
    public void delete(Integer id) {
        if (!convocatoriaRepo.existsById(id)) {
            throw new EntityNotFoundException("Convocatoria no encontrada");
        }
        convocatoriaRepo.deleteById(id);
    }

    private void mapDtoToEntity(ConvocatoriaRequestDTO dto, Convocatoria entity) {
        entity.setCuposDisponibles(dto.getCuposDisponibles());
        entity.setFechaPublicacion(dto.getFechaPublicacion());
        entity.setFechaCierre(dto.getFechaCierre());
        entity.setEstado(dto.getEstado());
        entity.setActivo(dto.getActivo());

        if(dto.getIdPeriodoAcademico() != null) {
            PeriodoAcademico periodo = periodoRepo.findById(dto.getIdPeriodoAcademico())
                    .orElseThrow(() -> new EntityNotFoundException("Periodo Académico no encontrado"));
            entity.setPeriodoAcademico(periodo);
        }

        if(dto.getIdAsignatura() != null) {
            Asignatura asignatura = asignaturaRepo.findById(dto.getIdAsignatura())
                    .orElseThrow(() -> new EntityNotFoundException("Asignatura no encontrada"));
            entity.setAsignatura(asignatura);
        }

        if(dto.getIdDocente() != null) {
            Docente docente = docenteRepo.findById(dto.getIdDocente())
                    .orElseThrow(() -> new EntityNotFoundException("Docente no encontrado"));
            entity.setDocente(docente);
        }
    }





//    private final IConvocatoriaRepository convocatoriaRepository;
//    private final PeriodoAcademicoRepository periodoRepository;
//    private final DocenteRepository docenteRepository;
//    private final AsignaturaRepository asignaturaRepository;
//
////    @Override
////    public ConvocatoriaResponseDTO crear(ConvocatoriaRequestDTO request) {
////        Integer idGenerado = convocatoriaRepository.registrarConvocatoria(
////                request.getIdPeriodoAcademico(),
////                request.getIdAsignatura(),
////                request.getIdDocente(),
////                request.getCuposDisponibles(),
////                request.getFechaPublicacion(),
////                request.getFechaCierre(),
////                request.getEstado()
////        );
////
////        if (idGenerado == -1) {
////            throw new RuntimeException("Error al crear la convocatoria.");
////        }
////
////        return buscarPorId(idGenerado);
////    }
//
//    @Override
//    public ConvocatoriaResponseDTO actualizar(Integer id, ConvocatoriaRequestDTO request) {
//        Integer resultado = convocatoriaRepository.actualizarConvocatoria(
//                id,
//                request.getCuposDisponibles(),
//                request.getFechaCierre(),
//                request.getEstado()
//        );
//
//        if (resultado == -1) {
//            throw new RuntimeException("Error al actualizar la convocatoria.");
//        }
//
//        return buscarPorId(id);
//    }
//
//    @Override
//    public void desactivar(Integer id) {
//        Integer resultado = convocatoriaRepository.desactivarConvocatoria(id);
//        if (resultado == -1) {
//            throw new RuntimeException("Error al desactivar la convocatoria.");
//        }
//    }
//
//    @Override
//    @Transactional(readOnly = true)
//    public ConvocatoriaResponseDTO buscarPorId(Integer id) {
//        Convocatoria convocatoria = convocatoriaRepository.findById(id)
//                .orElseThrow(() -> new RuntimeException("Convocatoria no encontrada con ID: " + id));
//        return mapearADTO(convocatoria);
//    }
//
//    @Override
//    @Transactional(readOnly = true)
//    public List<ConvocatoriaResponseDTO> listarPorPeriodo(Integer idPeriodo) {
//        return convocatoriaRepository.findByPeriodoAcademico_IdPeriodoAcademico(idPeriodo).stream()
//                .map(this::mapearADTO)
//                .collect(Collectors.toList());
//    }
//
//    private ConvocatoriaResponseDTO mapearADTO(Convocatoria entidad) {
////        String nombreDocente = "";
////        if (entidad.getDocente() != null && entidad.getDocente().getUsuario() != null) {
////            nombreDocente = entidad.getDocente().getUsuario().getNombres() + " " +
////                    entidad.getDocente().getUsuario().getApellidos();
////        }
////
////        return ConvocatoriaResponseDTO.builder()
////                .idConvocatoria(entidad.getIdConvocatoria())
////                .idPeriodoAcademico(entidad.getPeriodoAcademico().getIdPeriodoAcademico())
////                .nombrePeriodo(entidad.getPeriodoAcademico().getNombrePeriodo())
////                .idAsignatura(entidad.getAsignatura().getIdAsignatura())
////                .nombreAsignatura(entidad.getAsignatura().getNombreAsignatura())
////                .idDocente(entidad.getDocente().getIdDocente())
////                .nombreDocente(nombreDocente)
////                .cuposDisponibles(entidad.getCuposDisponibles())
////                .fechaPublicacion(entidad.getFechaPublicacion())
////                .fechaCierre(entidad.getFechaCierre())
////                .estado(entidad.getEstado())
////                .activo(entidad.getActivo())
////                .build();
//        return null;
//    }
//
//    @Override
//    public List<ConvocatoriaResponseDTO> obtenerTodasLasConvocatorias() {
//        List<Object[]> resultados = convocatoriaRepository.listarConvocatoriasVista();
//        List<ConvocatoriaResponseDTO> dtos = new ArrayList<>();
//        for (Object[] fila : resultados) {
//            ConvocatoriaResponseDTO dto = ConvocatoriaResponseDTO.builder()
//                    .idConvocatoria((Integer) fila[0])
//                    .cuposDisponibles((Integer) fila[1])
//                    .fechaCierre((LocalDate) fila[2])
//                    .estado((String) fila[3])
//                    .activo((Boolean) fila[4])
//                    .idAsignatura((Integer) fila[5])
//                    .nombreAsignatura((String) fila[6])
//                    .idCarrera((Integer) fila[7])
//                    .idDocente((Integer) fila[8])
//                    .nombres((String) fila[9])
//                    .apellidos((String) fila[10])
//                    .build();
//            dtos.add(dto);
//        }
//
//        return dtos;
//    }
//
//
//
//
//    @Transactional
//    public ConvocatoriaResponseDTO crear(ConvocatoriaRequestDTO request) {
//        PeriodoAcademico periodo = periodoRepository.findById(request.getIdPeriodoAcademico())
//                .orElseThrow(() -> new RuntimeException("Error: Periodo Académico no encontrado con ID: " + request.getIdPeriodoAcademico()));
//
//        Docente docente = docenteRepository.findById(request.getIdDocente())
//                .orElseThrow(() -> new RuntimeException("Error: Docente no encontrado con ID: " + request.getIdDocente()));
//
//        Asignatura asignatura = asignaturaRepository.findById(request.getIdAsignatura())
//                .orElseThrow(() -> new RuntimeException("Error: Asignatura no encontrada con ID: " + request.getIdAsignatura()));
//
//        Convocatoria convocatoria = Convocatoria.builder()
//                .periodoAcademico(periodo)
//                .docente(docente)
//                .asignatura(asignatura)
//                .cuposDisponibles(request.getCuposDisponibles())
//                .fechaPublicacion(request.getFechaPublicacion())
//                .fechaCierre(request.getFechaCierre())
//                .estado("ABIERTA")
//                .activo(true)
//                .build();
//
//        Convocatoria guardada = convocatoriaRepository.save(convocatoria);
//        return mapearADTO(guardada);
//    }
}