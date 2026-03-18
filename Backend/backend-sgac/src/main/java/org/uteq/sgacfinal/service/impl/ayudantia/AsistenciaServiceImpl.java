package org.uteq.sgacfinal.service.impl.ayudantia;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.uteq.sgacfinal.exception.BadRequestException;
import org.uteq.sgacfinal.repository.ayudantia.AsistenciaRepository;
import org.uteq.sgacfinal.service.ayudantia.IAsistenciaService;
import org.uteq.sgacfinal.service.security.SecurityContextService;

import java.io.ByteArrayOutputStream;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class AsistenciaServiceImpl implements IAsistenciaService {
    private final AsistenciaRepository   repo;
    private final ObjectMapper           objectMapper;
    private final SecurityContextService contextoSvc;

    private static final long     MAX_FILE_BYTES   = 2L * 1024 * 1024;
    private static final String[] EXPECTED_HEADERS = {"Nombre Completo", "Curso", "Paralelo"};

    @Override
    public Integer resolverIdAyudantia() {
        return contextoSvc.obtenerIdAyudantia();
    }

    @Override
    public Integer resolverIdRegistro() {
        return contextoSvc.obtenerIdRegistroActivo(resolverIdAyudantia());
    }

    @Override
    public Object consultarParticipantes(Integer idAyudantia) {
        try {
            return parsear(repo.consultarParticipantes(idAyudantia));
        } catch (Exception e) {
            log.error("[Asistencia] consultarParticipantes id={}", idAyudantia, e);
            throw new BadRequestException("Error al consultar participantes: " + e.getMessage());
        }
    }

    @Override
    public Object cargarParticipantesMasivo(Integer idAyudantia,
                                            List<Map<String, String>> participantes) {
        try {
            String json = objectMapper.writeValueAsString(participantes);
            return parsear(repo.cargarParticipantesMasivo(idAyudantia, json));
        } catch (BadRequestException e) {
            throw e;
        } catch (Exception e) {
            log.error("[Asistencia] cargarMasivo id={}", idAyudantia, e);
            throw new BadRequestException("Error al cargar participantes: " + e.getMessage());
        }
    }

    @Override
    public byte[] generarPlantillaExcel() {
        try (XSSFWorkbook wb  = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            XSSFSheet sheet = wb.createSheet("Participantes");

            XSSFCellStyle headerStyle = wb.createCellStyle();
            headerStyle.setFillForegroundColor(
                    new XSSFColor(new byte[]{0x1B, 0x5E, 0x20},
                            new org.apache.poi.xssf.usermodel.DefaultIndexedColorMap()));
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);
            XSSFFont font = wb.createFont();
            font.setColor(IndexedColors.WHITE.getIndex());
            font.setBold(true);
            font.setFontHeightInPoints((short) 11);
            headerStyle.setFont(font);

            Row headerRow = sheet.createRow(0);
            headerRow.setHeightInPoints(22);
            for (int i = 0; i < EXPECTED_HEADERS.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(EXPECTED_HEADERS[i]);
                cell.setCellStyle(headerStyle);
                sheet.setColumnWidth(i, i == 0 ? 9000 : 6000);
            }

            Row ejemplo = sheet.createRow(1);
            ejemplo.createCell(0).setCellValue("Juan Carlos Pérez López");
            ejemplo.createCell(1).setCellValue("Ingeniería en Software");
            ejemplo.createCell(2).setCellValue("A");

            sheet.setAutoFilter(new org.apache.poi.ss.util.CellRangeAddress(0, 0, 0, 2));

            wb.write(out);
            return out.toByteArray();

        } catch (Exception e) {
            log.error("[Asistencia] Error generando plantilla", e);
            throw new BadRequestException("No se pudo generar la plantilla Excel: " + e.getMessage());
        }
    }

    @Override
    public Object previewExcelImport(MultipartFile file) {

        if (file == null || file.isEmpty())
            return error("El archivo está vacío.");
        if (file.getSize() > MAX_FILE_BYTES)
            return error("El archivo supera el límite de 2 MB.");
        String filename = Optional.ofNullable(file.getOriginalFilename()).orElse("");
        if (!filename.toLowerCase().endsWith(".xlsx"))
            return error("Solo se permiten archivos .xlsx.");

        Set<String> existentes = cargarClavesExistentes();

        try (Workbook wb = new XSSFWorkbook(file.getInputStream())) {

            Sheet sheet = wb.getSheetAt(0);
            Row   headerRow = sheet.getRow(0);
            if (headerRow == null) {
                return error("El archivo no contiene fila de encabezados.");
            }

            // Validar cabeceras
            for (int i = 0; i < EXPECTED_HEADERS.length; i++) {
                Cell   cell   = headerRow.getCell(i);
                String actual = (cell != null) ? cell.getStringCellValue().trim() : "";
                if (!EXPECTED_HEADERS[i].equalsIgnoreCase(actual)) {
                    return error("Encabezado incorrecto en columna " + (i + 1)
                            + ". Se esperaba: \"" + EXPECTED_HEADERS[i] + "\".");
                }
            }

            List<Map<String, Object>> filas          = new ArrayList<>();
            Set<String>               seenEnExcel    = new HashSet<>();
            boolean                   tieneErrores   = false;
            int                       nuevos         = 0;
            int                       duplicadosBD   = 0;
            int                       duplicadosFile = 0;

            for (int r = 1; r <= sheet.getLastRowNum(); r++) {
                Row row = sheet.getRow(r);
                if (row == null) continue;

                String nombre   = cellStr(row.getCell(0));
                String curso    = cellStr(row.getCell(1));
                String paralelo = cellStr(row.getCell(2));

                String nombreLimpio   = nombre   != null ? nombre.trim()   : "";
                String cursoLimpio    = curso    != null ? curso.trim()    : "";
                String paraleloLimpio = paralelo != null ? paralelo.trim() : "";

                List<String> errores = new ArrayList<>();

                if (nombreLimpio.isBlank()) {
                    errores.add("El nombre completo es obligatorio.");
                } else if (nombreLimpio.matches(".*\\d.*")) {
                    errores.add("El nombre no debe contener números.");
                } else if (nombreLimpio.length() > 255) {
                    errores.add("El nombre supera los 255 caracteres.");
                }

                String claveExcel = claveNormalizada(nombreLimpio, cursoLimpio, paraleloLimpio);
                boolean duplicadoEnFile = !seenEnExcel.add(claveExcel);
                if (duplicadoEnFile) {
                    errores.add("Fila duplicada en el archivo.");
                    duplicadosFile++;
                }

                boolean yaExiste = existentes.contains(claveExcel);
                if (yaExiste)
                    duplicadosBD++;

                if (!errores.isEmpty()) tieneErrores = true;
                if (!yaExiste && !duplicadoEnFile && errores.isEmpty()) nuevos++;

                Map<String, Object> fila = new LinkedHashMap<>();
                fila.put("fila",           r + 1);
                fila.put("nombreCompleto", nombreLimpio);
                fila.put("curso",          cursoLimpio);
                fila.put("paralelo",       paraleloLimpio);
                fila.put("errores",        errores);
                fila.put("valida",         errores.isEmpty());
                fila.put("yaExiste",       yaExiste);     // ← FLAG CLAVE
                filas.add(fila);
            }

            if (filas.isEmpty()) {
                return error("El archivo no contiene datos.");
            }

            Map<String, Object> result = new LinkedHashMap<>();
            result.put("exito",         !tieneErrores);
            result.put("tieneErrores",  tieneErrores);
            result.put("totalFilas",    filas.size());
            result.put("nuevos",        nuevos);
            result.put("duplicadosBD",  duplicadosBD);
            result.put("duplicadosArchivo", duplicadosFile);
            result.put("filas", filas);
            result.put("mensaje", tieneErrores
                    ? "Se detectaron errores. Corrígelos antes de importar."
                    : String.format("Archivo válido. %d nuevo(s) · %d ya existe(n).", nuevos, duplicadosBD));

            return result;

        } catch (org.apache.poi.ooxml.POIXMLException e) {
            log.warn("[Asistencia] Archivo Excel dañado o formato inválido", e);
            return error("El archivo está dañado o no es un Excel válido (.xlsx).");
        } catch (Exception e) {
            log.error("[Asistencia] Error inesperado procesando Excel", e);
            return error("No se pudo leer el archivo. Verifique que sea un .xlsx válido.");
        }
    }

    private Set<String> cargarClavesExistentes() {
        try {
            Integer idAyudantia = resolverIdAyudantia();
            String  rawJson     = repo.consultarParticipantes(idAyudantia);
            if (rawJson == null || rawJson.isBlank() || rawJson.equals("null")) {
                return Collections.emptySet();
            }

            // Parsear el array JSON de participantes
            List<Map<String, Object>> participantes = objectMapper.readValue(
                    rawJson,
                    new TypeReference<>() {}
            );

            Set<String> claves = new HashSet<>();
            for (Map<String, Object> p : participantes) {
                String nombre   = Objects.toString(p.get("nombreCompleto"), "").trim();
                String curso    = Objects.toString(p.get("curso"),          "").trim();
                String paralelo = Objects.toString(p.get("paralelo"),       "").trim();
                claves.add(claveNormalizada(nombre, curso, paralelo));
            }
            return claves;

        } catch (Exception e) {
            // Si falla la consulta (p.ej. aún no hay ayudantía activa),
            // asumimos sin existentes para no bloquear la carga.
            log.warn("[Asistencia] No se pudo cargar participantes para comparación yaExiste: {}", e.getMessage());
            return Collections.emptySet();
        }
    }

    /**
     * Clave de comparación normalizada: lowercase + sin espacios extra.
     * Garantiza que "Juan Pérez" y "juan pérez" en la BD sean el mismo participante.
     */
    private String claveNormalizada(String nombre, String curso, String paralelo) {
        return (nombre.toLowerCase(java.util.Locale.ROOT)
                + "|" + curso.toLowerCase(java.util.Locale.ROOT)
                + "|" + paralelo.toLowerCase(java.util.Locale.ROOT));
    }

    // ══════════════════════════════════════════════════════════════════════
    // ASISTENCIA POR SESIÓN
    // ══════════════════════════════════════════════════════════════════════

    @Override
    public Object inicializarAsistencia(Integer idRegistro) {
        try {
            return parsear(repo.inicializarAsistencia(idRegistro));
        } catch (Exception e) {
            log.error("[Asistencia] inicializarAsistencia registro={}", idRegistro, e);
            throw new BadRequestException("Error al inicializar asistencia.");
        }
    }

    @Override
    public Object guardarAsistencias(Integer idRegistro, List<Map<String, Object>> asistencias) {
        try {
            String json = objectMapper.writeValueAsString(asistencias);
            return parsear(repo.guardarAsistencias(idRegistro, json));
        } catch (Exception e) {
            log.error("[Asistencia] guardarAsistencias registro={}", idRegistro, e);
            throw new BadRequestException("Error al guardar asistencias.");
        }
    }

    @Override
    public Object consultarAsistencia(Integer idRegistro) {
        try {
            return parsear(repo.consultarAsistencia(idRegistro));
        } catch (Exception e) {
            log.error("[Asistencia] consultarAsistencia registro={}", idRegistro, e);
            throw new BadRequestException("Error al consultar asistencia.");
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    // MATRIZ HISTÓRICA
    // ══════════════════════════════════════════════════════════════════════

    @Override
    public Object obtenerMatrizAsistencia() {
        try {
            Integer idAyudantia = resolverIdAyudantia();
            String  raw         = repo.obtenerMatrizAsistencia(idAyudantia);
            return parsear(raw);
        } catch (BadRequestException e) {
            throw e;
        } catch (Exception e) {
            log.error("[Asistencia] obtenerMatrizAsistencia", e);
            throw new BadRequestException("Error al generar la matriz de asistencia: " + e.getMessage());
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    // HELPERS PRIVADOS
    // ══════════════════════════════════════════════════════════════════════

    /**
     * Deserializa un String JSON hacia un tipo Java nativo (Map o List)
     * para que Jackson lo serialice limpiamente en la respuesta HTTP,
     * sin exponer propiedades internas de JsonNode.
     */
    private Object parsear(String raw) {
        try {
            if (raw == null || raw.isBlank()) return null;
            return objectMapper.readValue(raw, Object.class);
        } catch (JsonProcessingException e) {
            log.error("Error parseando JSON de BD: {}", raw, e);
            throw new BadRequestException("Respuesta del servidor con formato inválido.");
        }
    }

    private Map<String, Object> error(String mensaje) {
        Map<String, Object> err = new LinkedHashMap<>();
        err.put("exito",   false);
        err.put("mensaje", mensaje);
        return err;
    }

    private String cellStr(Cell cell) {
        if (cell == null) return null;
        return switch (cell.getCellType()) {
            case STRING  -> cell.getStringCellValue().trim();
            case NUMERIC -> String.valueOf((long) cell.getNumericCellValue());
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            case FORMULA -> {
                try { yield cell.getStringCellValue().trim(); }
                catch (Exception ex) { yield String.valueOf(cell.getNumericCellValue()); }
            }
            default -> null;
        };
    }
}