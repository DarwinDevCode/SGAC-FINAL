package org.uteq.sgacfinal.controller.resultados;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.uteq.sgacfinal.exception.AccesoDenegadoException;
import org.uteq.sgacfinal.security.UsuarioPrincipal;
import org.uteq.sgacfinal.service.reportes.ReporteUtilService;
import org.uteq.sgacfinal.service.resultados.IRankingService;
import org.uteq.sgacfinal.util.ExtraerAuth;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@RestController
@RequestMapping("/api/ranking")
@RequiredArgsConstructor
@Slf4j
public class RankingController {
    private final IRankingService svc;
    private final ObjectMapper    objectMapper;
    private final ReporteUtilService reporteSvc;
    private final ExtraerAuth extraerAuth;

    private static final Set<String> ROLES_REPORTE = Set.of("DECANO", "COORDINADOR", "ADMINISTRADOR");

    private static final String[] CABECERAS = {
            "#", "Postulante", "Asignatura", "Carrera", "Méritos", "Oposición", "Total", "Estado"
    };

    private static final String[] CAMPOS = {
            "posicion", "postulante", "asignatura", "carrera",
            "meritos", "oposicion", "total", "estado"
    };

    @GetMapping("/resultados")
    public ResponseEntity<String> obtenerResultados(
            Authentication authentication,
            @RequestHeader(value = "X-Active-Role", required = false) String rolSesion
    ) {
        ExtraerAuth.ExtraidoAuth ea = extraerAuth.extraer(authentication, rolSesion);
        log.info("Ejecutando Ranking - Usuario: {} | Rol Activo: {}", ea.idUsuario(), ea.rol());

        JsonNode resultado = svc.obtenerRankingResultados(ea.idUsuario(), ea.rol());
        return jsonResponse(resultado);
    }

    @GetMapping("/exportar/excel")
    public ResponseEntity<byte[]> exportarExcel(
            Authentication auth,
            @RequestHeader(value = "X-Active-Role", required = false) String rolSesion) {

        ExtraerAuth.ExtraidoAuth ea = extraerAuth.extraer(auth, rolSesion);
        verificarRolReporte(ea.rol(), ea.idUsuario());

        JsonNode datos = svc.obtenerRankingResultados(ea.idUsuario(), ea.rol());
        List<Map<String, Object>> filas = jsonNodeALista(datos);

        String titulo = "Ranking Final de Selección de Ayudantes — Período Activo";
        byte[] excel = reporteSvc.exportarExcel(titulo, CABECERAS, CAMPOS, filas);

        String nombreArchivo = "Ranking_Final_" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + ".xlsx";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + nombreArchivo + "\"")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(excel);
    }

    @GetMapping("/exportar/pdf")
    public ResponseEntity<byte[]> exportarPdf(
            Authentication auth,
            @RequestHeader(value = "X-Active-Role", required = false) String rolSesion) {

        ExtraerAuth.ExtraidoAuth ea = extraerAuth.extraer(auth, rolSesion);
        verificarRolReporte(ea.rol(), ea.idUsuario());

        JsonNode datos = svc.obtenerRankingResultados(ea.idUsuario(), ea.rol());
        List<Map<String, Object>> filas = jsonNodeALista(datos);

        String titulo = "Ranking Final de Selección de Ayudantes — Período Activo";
        String nombreUsuario = ea.nombreUsuario();
        byte[] pdf = reporteSvc.exportarPdf(titulo, CABECERAS, CAMPOS, filas,
                ReporteUtilService.Orientacion.LANDSCAPE, nombreUsuario);

        String nombreArchivo = "Ranking_Final_" +
                LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + ".pdf";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + nombreArchivo + "\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    private void verificarRolReporte(String rol, Integer idUsuario) {
        if (!ROLES_REPORTE.contains(rol.toUpperCase())) {
            log.warn("[Reporte] Acceso denegado — idUsuario={} rol={}", idUsuario, rol);
            throw new AccesoDenegadoException(
                    "Solo DECANO, COORDINADOR o ADMINISTRADOR pueden descargar reportes.");
        }
    }

    /*
    private ExtraidoAuth extraer(Authentication auth, String rolSesion) {
        if (auth == null || !(auth.getPrincipal() instanceof UsuarioPrincipal p)) {
            throw new AccesoDenegadoException("No autenticado.");
        }

        List<String> rolesPermitidos = auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        String rolFinal;
        if (rolSesion != null && rolesPermitidos.contains(rolSesion)) {
            rolFinal = rolSesion;
        } else {
            rolFinal = rolesPermitidos.get(0);
        }

        String nombre = p.getUsuario().getNombres() + " " + p.getUsuario().getApellidos();
        return new ExtraidoAuth(p.getIdUsuario(), rolFinal, nombre);
    }
     */

    private List<Map<String, Object>> jsonNodeALista(JsonNode nodo) {
        try {
            JsonNode resultados = nodo.path("resultados");
            if (resultados.isMissingNode() || !resultados.isArray()) {
                return Collections.emptyList();
            }
            List<Map<String, Object>> lista = new ArrayList<>();
            for (JsonNode item : resultados) {
                Map<String, Object> fila = objectMapper.convertValue(item, Map.class);
                lista.add(fila);
            }
            return lista;
        } catch (Exception e) {
            log.error("[Reporte] Error convirtiendo resultados a lista: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    /*
    private record ExtraidoAuth(Integer idUsuario, String rol, String nombreUsuario) {}
    */

    private ResponseEntity<String> jsonResponse(JsonNode node) {
        try {
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(objectMapper.writeValueAsString(node));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("{\"error\":\"Error al serializar\"}");
        }
    }
}
