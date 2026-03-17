package org.uteq.sgacfinal.service.impl.ayudantia;

import com.fasterxml.jackson.core.JsonProcessingException;
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

    private final AsistenciaRepository repo;
    private final ObjectMapper         objectMapper;
    private final SecurityContextService contextoSvc;

    private static final long     MAX_FILE_BYTES     = 2L * 1024 * 1024;
    private static final String[] EXPECTED_HEADERS   = {"Nombre Completo", "Curso", "Paralelo"};

    @Override
    public Integer resolverIdAyudantia() {
        return contextoSvc.obtenerIdAyudantia();
    }

    @Override
    public Integer resolverIdRegistro() {
        Integer idAyudantia = resolverIdAyudantia();
        return contextoSvc.obtenerIdRegistroActivo(idAyudantia);
    }

    @Override
    public Object consultarParticipantes(Integer idAyudantia) {
        try {
            String raw = repo.consultarParticipantes(idAyudantia);
            return parsear(raw);
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
            String raw  = repo.cargarParticipantesMasivo(idAyudantia, json);
            return parsear(raw);
        } catch (BadRequestException e) {
            throw e;
        } catch (Exception e) {
            log.error("[Asistencia] cargarMasivo id={}", idAyudantia, e);
            throw new BadRequestException("Error al cargar participantes: " + e.getMessage());
        }
    }

    @Override
    public byte[] generarPlantillaExcel() {
        try (XSSFWorkbook wb = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            XSSFSheet sheet = wb.createSheet("Participantes");

            XSSFCellStyle headerStyle = wb.createCellStyle();
            headerStyle.setFillForegroundColor(
                    new XSSFColor(new byte[]{0x1B, 0x5E, 0x20}, new org.apache.poi.xssf.usermodel.DefaultIndexedColorMap())
            );
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
        if (file == null || file.isEmpty() || file.getSize() == 0) {
            return error("El archivo está vacío.");
        }
        if (file.getSize() > MAX_FILE_BYTES) {
            return error(String.format("El archivo supera el límite de 2 MB."));
        }

        String filename = Optional.ofNullable(file.getOriginalFilename()).orElse("");
        if (!filename.toLowerCase().endsWith(".xlsx")) {
            return error("Solo se permiten archivos .xlsx.");
        }

        try (Workbook wb = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = wb.getSheetAt(0);
            Row headerRow = sheet.getRow(0);
            if (headerRow == null) return error("El archivo no contiene fila de encabezados.");

            for (int i = 0; i < EXPECTED_HEADERS.length; i++) {
                Cell cell = headerRow.getCell(i);
                String actual = (cell != null) ? cell.getStringCellValue().trim() : "";
                if (!EXPECTED_HEADERS[i].equalsIgnoreCase(actual)) {
                    return error("Encabezado incorrecto en columna " + (i+1));
                }
            }

            List<Map<String, Object>> filas = new ArrayList<>();
            Set<String> seenKeys = new HashSet<>();
            boolean tieneErrores = false;

            for (int r = 1; r <= sheet.getLastRowNum(); r++) {
                Row row = sheet.getRow(r);
                if (row == null) continue;

                String nombre   = cellStr(row.getCell(0));
                String curso    = cellStr(row.getCell(1));
                String paralelo = cellStr(row.getCell(2));

                List<String> errores = new ArrayList<>();
                if (nombre == null || nombre.isBlank()) {
                    errores.add("El nombre es obligatorio.");
                } else if (nombre.matches(".*\\d.*")) {
                    errores.add("El nombre no debe contener números.");
                }

                String clave = (nombre + "|" + curso + "|" + paralelo).toLowerCase().strip();
                if (!seenKeys.add(clave)) errores.add("Fila duplicada.");

                if (!errores.isEmpty()) tieneErrores = true;

                Map<String, Object> fila = new LinkedHashMap<>();
                fila.put("fila", r + 1);
                fila.put("nombreCompleto", nombre != null ? nombre : "");
                fila.put("curso", curso != null ? curso : "");
                fila.put("paralelo", paralelo != null ? paralelo : "");
                fila.put("errores", errores);
                fila.put("valida", errores.isEmpty());
                filas.add(fila);
            }

            if (filas.isEmpty()) return error("El archivo no contiene datos.");

            Map<String, Object> result = new LinkedHashMap<>();
            result.put("exito", !tieneErrores);
            result.put("tieneErrores", tieneErrores);
            result.put("totalFilas", filas.size());
            result.put("filas", filas);
            result.put("mensaje", tieneErrores ? "Errores detectados." : "Archivo válido.");

            return result; // Retornamos el Map directamente para que Jackson lo serialice bien

        } catch (Exception e) {
            log.error("[Asistencia] Error procesando Excel", e);
            return error("No se pudo leer el archivo Excel.");
        }
    }

    @Override
    public Object inicializarAsistencia(Integer idRegistro) {
        try {
            String raw = repo.inicializarAsistencia(idRegistro);
            return parsear(raw);
        } catch (Exception e) {
            log.error("[Asistencia] inicializarAsistencia registro={}", idRegistro, e);
            throw new BadRequestException("Error al inicializar asistencia.");
        }
    }

    @Override
    public Object guardarAsistencias(Integer idRegistro, List<Map<String, Object>> asistencias) {
        try {
            String json = objectMapper.writeValueAsString(asistencias);
            String raw  = repo.guardarAsistencias(idRegistro, json);
            return parsear(raw);
        } catch (Exception e) {
            log.error("[Asistencia] guardarAsistencias registro={}", idRegistro, e);
            throw new BadRequestException("Error al guardar asistencias.");
        }
    }

    @Override
    public Object consultarAsistencia(Integer idRegistro) {
        try {
            String raw = repo.consultarAsistencia(idRegistro);
            return parsear(raw);
        } catch (Exception e) {
            log.error("[Asistencia] consultarAsistencia registro={}", idRegistro, e);
            throw new BadRequestException("Error al consultar asistencia.");
        }
    }

    /**
     * Mapea un String JSON a un Objeto de Java (Map o List) para evitar
     * que Jackson exponga las propiedades internas de JsonNode.
     */
    private Object parsear(String raw) {
        try {
            if (raw == null || raw.isBlank()) return null;
            return objectMapper.readValue(raw, Object.class);
        } catch (JsonProcessingException e) {
            log.error("Error parseando respuesta JSON: {}", raw, e);
            throw new BadRequestException("Respuesta del servidor con formato inválido.");
        }
    }

    /**
     * Retorna un mapa de error estándar.
     */
    private Map<String, Object> error(String mensaje) {
        Map<String, Object> err = new HashMap<>();
        err.put("exito", false);
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