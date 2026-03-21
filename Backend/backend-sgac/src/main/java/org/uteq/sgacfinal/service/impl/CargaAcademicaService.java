package org.uteq.sgacfinal.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.uteq.sgacfinal.dto.request.SincronizarCargaRequest;
import org.uteq.sgacfinal.dto.response.AsignaturaJerarquiaDTO;
import org.uteq.sgacfinal.dto.response.DocenteActivoDTO;
import org.uteq.sgacfinal.dto.response.SincronizarCargaResponseDTO;
import org.uteq.sgacfinal.repository.ICargaAcademicaRepository;
import org.uteq.sgacfinal.service.EmailService;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CargaAcademicaService {

    private final ICargaAcademicaRepository repo;
    private final ObjectMapper objectMapper;
    private final EmailService emailService;

    @Transactional(readOnly = true)
    public List<DocenteActivoDTO> listarDocentes() {
        try {
            String json = repo.listarDocentesActivos();
            List<Map<String, Object>> raw = objectMapper.readValue(json, new TypeReference<>() {});
            return raw.stream().map(m -> DocenteActivoDTO.builder()
                            .idDocente       (num(m, "idDocente"))
                            .nombres         ((String) m.get("nombres"))
                            .apellidos       ((String) m.get("apellidos"))
                            .cedula          ((String) m.get("cedula"))
                            .correo          ((String) m.get("correo"))
                            .totalAsignaturas(((Number) m.get("totalAsignaturas")).longValue())
                            .build())
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("Error al listar docentes: " + e.getMessage(), e);
        }
    }

    @Transactional(readOnly = true)
    public List<AsignaturaJerarquiaDTO> listarAsignaturasDocente(Integer idDocente) {
        try {
            String json = repo.listarAsignaturasDocente(idDocente);
            List<Map<String, Object>> raw = objectMapper.readValue(json, new TypeReference<>() {});
            return raw.stream().map(m -> AsignaturaJerarquiaDTO.builder()
                            .idAsignatura    (num(m, "idAsignatura"))
                            .nombreAsignatura((String) m.get("nombreAsignatura"))
                            .semestre        (num(m, "semestre"))
                            .idCarrera       (num(m, "idCarrera"))
                            .nombreCarrera   ((String) m.get("nombreCarrera"))
                            .idFacultad      (num(m, "idFacultad"))
                            .nombreFacultad  ((String) m.get("nombreFacultad"))
                            .etiqueta        ((String) m.get("etiqueta"))
                            .build())
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("Error al listar asignaturas del docente: " + e.getMessage(), e);
        }
    }

    @Transactional(readOnly = true)
    public List<AsignaturaJerarquiaDTO> listarAsignaturas() {
        try {
            String json = repo.listarJerarquiaAsignaturas();
            List<Map<String, Object>> raw = objectMapper.readValue(json, new TypeReference<>() {});
            return raw.stream().map(m -> AsignaturaJerarquiaDTO.builder()
                            .idAsignatura    (num(m, "idAsignatura"))
                            .nombreAsignatura((String) m.get("nombreAsignatura"))
                            .semestre        (num(m, "semestre"))
                            .idCarrera       (num(m, "idCarrera"))
                            .nombreCarrera   ((String) m.get("nombreCarrera"))
                            .idFacultad      (num(m, "idFacultad"))
                            .nombreFacultad  ((String) m.get("nombreFacultad"))
                            .etiqueta        ((String) m.get("etiqueta"))
                            .build())
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("Error al listar asignaturas: " + e.getMessage(), e);
        }
    }

    // ── Sincronización atómica ──────────────────────────────────────
    @Transactional
    public SincronizarCargaResponseDTO sincronizar(SincronizarCargaRequest req) {
        try {
            // Convertir List<Integer> → formato PostgreSQL array: {1,2,3}
            String pgArray = req.getAsignaturasIds() == null || req.getAsignaturasIds().isEmpty()
                    ? "{}"
                    : "{" + req.getAsignaturasIds().stream()
                    .map(Object::toString)
                    .collect(Collectors.joining(",")) + "}";

            String resultJson = repo.gestionarCargaDocente(req.getIdDocente(), pgArray);
            Map<String, Object> result = objectMapper.readValue(resultJson, new TypeReference<>() {});

            SincronizarCargaResponseDTO dto = SincronizarCargaResponseDTO.builder()
                    .exito          ((Boolean) result.get("exito"))
                    .idDocente      (num(result, "idDocente"))
                    .nombreDocente  ((String) result.get("nombreDocente"))
                    .correoDocente  ((String) result.get("correoDocente"))
                    .revocadas      (num(result, "revocadas"))
                    .asignadas      (num(result, "asignadas"))
                    .sinCambio      (num(result, "sinCambio"))
                    .asignaturasActuales (asList(result, "asignaturasActuales"))
                    .asignaturasRevocadas(asList(result, "asignaturasRevocadas"))
                    .mensaje(buildMensaje(result))
                    .build();

            // Notificar al docente por correo (asíncrono)
            emailService.enviarActualizacionCarga(
                    dto.getCorreoDocente(),
                    dto.getNombreDocente(),
                    dto.getAsignaturasActuales(),
                    dto.getAsignaturasRevocadas()
            );

            log.info("[CargaAcademica] Sincronización exitosa — Docente {}: {} asignadas, {} revocadas",
                    dto.getNombreDocente(), dto.getAsignadas(), dto.getRevocadas());

            return dto;

        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Error en sincronización de carga: " + e.getMessage(), e);
        }
    }

    private Integer num(Map<String, Object> m, String key) {
        Object v = m.get(key);
        return v == null ? null : ((Number) v).intValue();
    }

    @SuppressWarnings("unchecked")
    private List<String> asList(Map<String, Object> m, String key) {
        Object v = m.get(key);
        if (v instanceof List) return (List<String>) v;
        return Collections.emptyList();
    }

    private String buildMensaje(Map<String, Object> r) {
        int asig = ((Number) r.get("asignadas")).intValue();
        int rev  = ((Number) r.get("revocadas")).intValue();
        if (asig == 0 && rev == 0) return "Sin cambios en la carga académica.";
        StringBuilder sb = new StringBuilder("Carga actualizada:");
        if (asig > 0) sb.append(" ").append(asig).append(" asignatura(s) añadida(s).");
        if (rev  > 0) sb.append(" ").append(rev).append(" asignatura(s) revocada(s).");
        return sb.toString();
    }
}