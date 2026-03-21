package org.uteq.sgacfinal.service.impl;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.uteq.sgacfinal.dto.request.ConvocatoriaRequestDTO;
import org.uteq.sgacfinal.dto.request.configuracion.ConvocatoriaActualizarRequestDTO;
import org.uteq.sgacfinal.dto.request.configuracion.ConvocatoriaCrearRequestDTO;
import org.uteq.sgacfinal.dto.response.ConvocatoriaResponseDTO;
import org.uteq.sgacfinal.dto.response.configuracion.ConvocatoriaNativaResponseDTO;
import org.uteq.sgacfinal.dto.response.configuracion.VerificarFaseResponseDTO;
import org.uteq.sgacfinal.dto.response.configuracion.VerificarPostulantesResponseDTO;
import org.uteq.sgacfinal.entity.Asignatura;
import org.uteq.sgacfinal.entity.Convocatoria;
import org.uteq.sgacfinal.entity.Docente;
import org.uteq.sgacfinal.entity.PeriodoAcademico;
import org.uteq.sgacfinal.event.ConvocatoriaCreadaEvent;
import org.uteq.sgacfinal.exception.ConvocatoriaBusinessException;
import org.uteq.sgacfinal.exception.FaseRestriccionException;
import org.uteq.sgacfinal.mapper.ConvocatoriaMapper;
import org.uteq.sgacfinal.repository.DocenteRepository;
import org.uteq.sgacfinal.repository.IAsignaturaRepository;
import org.uteq.sgacfinal.repository.IConvocatoriaRepository;
import org.uteq.sgacfinal.repository.IPeriodoAcademicoRepository;
import org.uteq.sgacfinal.service.IConvocatoriaService;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class ConvocatoriaServiceImpl implements IConvocatoriaService {
    private final IConvocatoriaRepository convocatoriaRepo;
    private final IPeriodoAcademicoRepository periodoRepo;
    private final IAsignaturaRepository asignaturaRepo;
    private final DocenteRepository docenteRepo;
    private final ObjectMapper mapper;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public ConvocatoriaResponseDTO create(ConvocatoriaRequestDTO dto) {
        Convocatoria convocatoria = new Convocatoria();
        mapDtoToEntity(dto, convocatoria);

        Convocatoria saved = convocatoriaRepo.save(convocatoria);
        eventPublisher.publishEvent(new ConvocatoriaCreadaEvent(saved));

        return ConvocatoriaMapper.toDTO(saved);
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
        PeriodoAcademico periodoActivo = periodoRepo.findFirstByEstadoAndActivoTrueOrderByFechaInicioDesc("EN PROCESO")
                .orElse(null);

        if (periodoActivo == null) {
            return List.of(); // Si no hay periodo activo, no hay convocatorias que mostrar
        }

        return convocatoriaRepo.findByPeriodoAcademico_IdPeriodoAcademico(periodoActivo.getIdPeriodoAcademico())
                .stream()
                .filter(Convocatoria::getActivo) // Solo convocatorias activas
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

    private <T> T parse(String json, Class<T> clazz) {
        try {
            return mapper.readValue(json, clazz);
        } catch (Exception e) {
            log.error("Error al deserializar respuesta PL/pgSQL: {}", e.getMessage());
            throw new ConvocatoriaBusinessException("Error técnico al procesar la respuesta del servidor.");
        }
    }

    /** Serializa el DTO para enviarlo como JSON string a PL/pgSQL */
    private String toJson(Object obj) {
        try {
            return mapper.writeValueAsString(obj);
        } catch (Exception e) {
            throw new ConvocatoriaBusinessException("Error técnico al preparar la solicitud.");
        }
    }

    private ConvocatoriaNativaResponseDTO evaluar(String json) {
        ConvocatoriaNativaResponseDTO res = parse(json, ConvocatoriaNativaResponseDTO.class);
        if (!res.isExito()) {
            String msg = res.getMensaje() != null ? res.getMensaje() : "Error desconocido.";
            if (msg.startsWith("[FASE]") || msg.contains("fase") || msg.contains("cronograma")) {
                throw new FaseRestriccionException(msg);
            }
            if (msg.startsWith("[BLOQUEO]")) {
                throw new ConvocatoriaBusinessException(msg, "BLOQUEO_POSTULANTES");
            }
            throw new ConvocatoriaBusinessException(msg);
        }
        return res;
    }


    @Override
    @Transactional(readOnly = true)
    public VerificarFaseResponseDTO verificarFase() {
        return parse(convocatoriaRepo.verificarRestriccionesFase(), VerificarFaseResponseDTO.class);
    }

    @Override
    @Transactional
    public ConvocatoriaNativaResponseDTO crear(ConvocatoriaCrearRequestDTO request) {
        return evaluar(convocatoriaRepo.crearConvocatoria(toJson(request)));
    }

    @Override
    @Transactional
    public ConvocatoriaNativaResponseDTO actualizar(ConvocatoriaActualizarRequestDTO request) {
        return evaluar(
                convocatoriaRepo.actualizarConvocatoria(toJson(request), request.getTipoEdicion())
        );
    }

    @Override
    @Transactional(readOnly = true)
    public VerificarPostulantesResponseDTO checkPostulantes(Integer idConvocatoria) {
        VerificarPostulantesResponseDTO res =
                parse(convocatoriaRepo.verificarPostulantes(idConvocatoria), VerificarPostulantesResponseDTO.class);
        if (!res.isExito()) {
            throw new ConvocatoriaBusinessException(res.getMensaje());
        }
        return res;
    }

    @Override
    @Transactional
    public ConvocatoriaNativaResponseDTO desactivar(Integer idConvocatoria) {
        return evaluar(convocatoriaRepo.desactivarConvocatoria(idConvocatoria));
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
