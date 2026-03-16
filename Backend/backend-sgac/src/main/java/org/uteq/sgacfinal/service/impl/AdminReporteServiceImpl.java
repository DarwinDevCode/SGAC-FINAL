package org.uteq.sgacfinal.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.uteq.sgacfinal.dto.Response.*;
import org.uteq.sgacfinal.entity.*;
import org.uteq.sgacfinal.repository.*;
import org.uteq.sgacfinal.service.IAdminReporteService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminReporteServiceImpl implements IAdminReporteService {

    private final LogAuditoriaRepository logAuditoriaRepository;
    private final FacultadRepository facultadRepository;
    private final CarreraRepository carreraRepository;
    private final IAsignaturaRepository asignaturaRepository;
    private final IConvocatoriaRepository convocatoriaRepository;
    private final DecanoRepository decanoRepository;
    private final CoordinadorRepository coordinadorRepository;
    private final PostulacionRepository postulacionRepository;
    private final IUsuariosRepository usuariosRepository;

    @Override
    public List<LogAuditoriaResponseDTO> reporteAuditoriaCompleto(Integer idUsuario, String modulo, String fechaDesde, String fechaHasta) {
        List<LogAuditoria> logs = logAuditoriaRepository.findAll();
        
        return logs.stream()
                .filter(l -> idUsuario == null || (l.getUsuario() != null && l.getUsuario().getIdUsuario().equals(idUsuario)))
                .filter(l -> modulo == null || modulo.isEmpty() || (l.getTablaAfectada() != null && l.getTablaAfectada().equalsIgnoreCase(modulo)))
                .sorted(Comparator.comparing(LogAuditoria::getFechaHora, Comparator.nullsLast(Comparator.reverseOrder())))
                .map(l -> LogAuditoriaResponseDTO.builder()
                        .idLogAuditoria(l.getIdLogAuditoria())
                        .idUsuario(l.getUsuario() != null ? l.getUsuario().getIdUsuario() : null)
                        .nombreUsuario(l.getUsuario() != null ? l.getUsuario().getNombres() + " " + l.getUsuario().getApellidos() : "N/A")
                        .accion(l.getAccion())
                        .tablaAfectada(l.getTablaAfectada())
                        .fechaHora(l.getFechaHora())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public List<AdminReporteGeneralDTO> reporteFacultades() {
        return facultadRepository.findAll().stream()
                .map(f -> AdminReporteGeneralDTO.builder()
                        .id(f.getIdFacultad())
                        .nombre(f.getNombreFacultad())
                        .totalRelacionado(carreraRepository.countByFacultad_IdFacultad(f.getIdFacultad()))
                        .estado(f.getActivo() ? "Activo" : "Inactivo")
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public List<AdminReporteGeneralDTO> reporteCarreras(Integer idFacultad) {
        List<Carrera> carreras = idFacultad != null ? 
                carreraRepository.findByFacultad_IdFacultad(idFacultad) : 
                carreraRepository.findAll();
                
        return carreras.stream()
                .map(c -> AdminReporteGeneralDTO.builder()
                        .id(c.getIdCarrera())
                        .nombre(c.getNombreCarrera())
                        .descripcion(c.getFacultad() != null ? c.getFacultad().getNombreFacultad() : "N/A")
                        .totalRelacionado(asignaturaRepository.countByCarrera_IdCarrera(c.getIdCarrera()))
                        .estado(c.getActivo() ? "Activo" : "Inactivo")
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public List<AdminReporteGeneralDTO> reporteAsignaturas(Integer idCarrera) {
        List<Asignatura> asignaturas = idCarrera != null ?
                asignaturaRepository.findByCarrera_IdCarrera(idCarrera) :
                asignaturaRepository.findAll();
                
        return asignaturas.stream()
                .map(a -> AdminReporteGeneralDTO.builder()
                        .id(a.getIdAsignatura())
                        .nombre(a.getNombreAsignatura())
                        .descripcion(a.getCarrera() != null ? a.getCarrera().getNombreCarrera() : "N/A")
                        .totalRelacionado(convocatoriaRepository.countByAsignatura_IdAsignatura(a.getIdAsignatura()))
                        .estado(a.getActivo() ? "Activo" : "Inactivo")
                        .extraInfo("Semestre: " + a.getSemestre())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public List<AdminReporteGeneralDTO> reporteConvocatorias(Integer idAsignatura, Integer idPeriodo) {
        List<Convocatoria> convocatorias = convocatoriaRepository.findAll(); // Aplicar filtros manuales por simplicidad si no hay repo específico
        
        return convocatorias.stream()
                .filter(c -> idAsignatura == null || (c.getAsignatura() != null && c.getAsignatura().getIdAsignatura().equals(idAsignatura)))
                .filter(c -> idPeriodo == null || (c.getPeriodoAcademico() != null && c.getPeriodoAcademico().getIdPeriodoAcademico().equals(idPeriodo)))
                .map(c -> AdminReporteGeneralDTO.builder()
                        .id(c.getIdConvocatoria())
                        .nombre(c.getAsignatura() != null ? c.getAsignatura().getNombreAsignatura() : "N/A")
                        .descripcion(c.getPeriodoAcademico() != null ? c.getPeriodoAcademico().getNombrePeriodo() : "N/A")
                        .totalRelacionado(postulacionRepository.countByConvocatoria_IdConvocatoria(c.getIdConvocatoria()))
                        .estado(c.getEstado())
                        .extraInfo("Cupos: " + c.getCuposDisponibles())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public List<AdminReporteGeneralDTO> reporteDecanosCoordinadores(Integer idFacultad, Integer idCarrera, String tipo) {
        List<AdminReporteGeneralDTO> result = new ArrayList<>();
        
        if (tipo == null || tipo.equalsIgnoreCase("DECANO")) {
            List<Decano> decanos = idFacultad != null ? 
                    decanoRepository.findByFacultad_IdFacultad(idFacultad) : 
                    decanoRepository.findAll();
            result.addAll(decanos.stream()
                    .map(d -> AdminReporteGeneralDTO.builder()
                            .id(d.getIdDecano())
                            .nombre(d.getUsuario().getNombres() + " " + d.getUsuario().getApellidos())
                            .descripcion("Decano - " + (d.getFacultad() != null ? d.getFacultad().getNombreFacultad() : "N/A"))
                            .estado(d.getActivo() ? "Activo" : "Inactivo")
                            .build())
                    .collect(Collectors.toList()));
        }
        
        if (tipo == null || tipo.equalsIgnoreCase("COORDINADOR")) {
            List<Coordinador> coords = idCarrera != null ?
                    coordinadorRepository.findByCarrera_IdCarrera(idCarrera) :
                    coordinadorRepository.findAll();
            result.addAll(coords.stream()
                    .map(c -> AdminReporteGeneralDTO.builder()
                            .id(c.getIdCoordinador())
                            .nombre(c.getUsuario().getNombres() + " " + c.getUsuario().getApellidos())
                            .descripcion("Coordinador - " + (c.getCarrera() != null ? c.getCarrera().getNombreCarrera() : "N/A"))
                            .estado(c.getActivo() ? "Activo" : "Inactivo")
                            .build())
                    .collect(Collectors.toList()));
        }
        
        return result;
    }

    @Override
    public List<CoordinadorPostulanteReporteDTO> reportePostulantesGlobal(Integer idAsignatura, Integer idPeriodo, String estado) {
        List<Postulacion> posts = postulacionRepository.findAll();
        
        return posts.stream()
                .filter(p -> idAsignatura == null || (p.getConvocatoria() != null && p.getConvocatoria().getAsignatura() != null && p.getConvocatoria().getAsignatura().getIdAsignatura().equals(idAsignatura)))
                .filter(p -> idPeriodo == null || (p.getConvocatoria() != null && p.getConvocatoria().getPeriodoAcademico() != null && p.getConvocatoria().getPeriodoAcademico().getIdPeriodoAcademico().equals(idPeriodo)))
                .filter(p -> estado == null || estado.isEmpty() || (p.getEstadoPostulacion() != null && p.getEstadoPostulacion().equalsIgnoreCase(estado)))
                .map(p -> CoordinadorPostulanteReporteDTO.builder()
                        .idPostulacion(p.getIdPostulacion())
                        .nombreEstudiante(p.getEstudiante() != null && p.getEstudiante().getUsuario() != null ? p.getEstudiante().getUsuario().getNombres() + " " + p.getEstudiante().getUsuario().getApellidos() : "N/A")
                        .cedula(p.getEstudiante() != null && p.getEstudiante().getUsuario() != null ? p.getEstudiante().getUsuario().getCedula() : "N/A")
                        .nombreAsignatura(p.getConvocatoria() != null && p.getConvocatoria().getAsignatura() != null ? p.getConvocatoria().getAsignatura().getNombreAsignatura() : "N/A")
                        .nombrePeriodo(p.getConvocatoria() != null && p.getConvocatoria().getPeriodoAcademico() != null ? p.getConvocatoria().getPeriodoAcademico().getNombrePeriodo() : "N/A")
                        .fechaPostulacion(p.getFechaPostulacion())
                        .estadoEvaluacion(p.getEstadoPostulacion())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public List<UsuarioReporteDTO> reporteUsuarios(String rol, Boolean activo) {
        List<Usuario> usuarios = usuariosRepository.findAllWithRolesAndTipoRol();
        
        return usuarios.stream()
                .filter(u -> activo == null || u.getActivo() == activo)
                .filter(u -> {
                    if (rol == null || rol.isEmpty()) return true;
                    return u.getRoles() != null && u.getRoles().stream()
                            .anyMatch(r -> r.getTipoRol() != null && r.getTipoRol().getNombreTipoRol().contains(rol));
                })
                .map(u -> UsuarioReporteDTO.builder()
                        .idUsuario(u.getIdUsuario())
                        .nombreUsuario(u.getNombreUsuario())
                        .nombres(u.getNombres())
                        .apellidos(u.getApellidos())
                        .cedula(u.getCedula())
                        .correo(u.getCorreo())
                        .activo(u.getActivo())
                        .roles(u.getRoles() != null ? u.getRoles().stream()
                                .map(r -> r.getTipoRol().getNombreTipoRol().replace("ROLE_", ""))
                                .collect(Collectors.joining(", ")) : "")
                        .build())
                .collect(Collectors.toList());
    }
}
