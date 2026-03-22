package org.uteq.sgacfinal.service.impl.ayudantia;

import com.fasterxml.jackson.databind.JsonNode;
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

import java.io.ByteArrayOutputStream;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class AsistenciaServiceImpl implements IAsistenciaService {

    private final AsistenciaRepository repo;
    private final ObjectMapper         objectMapper;
    private static final long     MAX_FILE_BYTES     = 2L * 1024 * 1024;
    private static final String[] EXPECTED_HEADERS   = {"Nombre Completo", "Curso", "Paralelo"};

    @Override
    public JsonNode consultarParticipantes(Integer idAyudantia) {
        try {
            String raw = repo.consultarParticipantes(idAyudantia);
            return objectMapper.readTree(raw);
        } catch (Exception e) {
            log.error("[Asistencia] consultarParticipantes id={}", idAyudantia, e);
            throw new BadRequestException("Error al consultar participantes: " + e.getMessage());
        }
    }

    @Override
    public JsonNode cargarParticipantesMasivo(Integer idAyudantia,
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
    public JsonNode previewExcelImport(MultipartFile file) {
        if (file == null || file.isEmpty() || file.getSize() == 0) {
            return error("El archivo está vacío (0 bytes).");
        }
        if (file.getSize() > MAX_FILE_BYTES) {
            return error(String.format("El archivo supera el límite de 2 MB (tamaño recibido: %.1f MB).",
                    file.getSize() / (1024.0 * 1024.0)));
        }
        String filename = Optional.ofNullable(file.getOriginalFilename()).orElse("");
        if (!filename.toLowerCase().endsWith(".xlsx")) {
            return error("Solo se permiten archivos .xlsx. Para archivos CSV, conviértelos primero a Excel.");
        }

        try (Workbook wb = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = wb.getSheetAt(0);
            Row headerRow = sheet.getRow(0);
            if (headerRow == null) {
                return error("El archivo no contiene fila de encabezados.");
            }
            for (int i = 0; i < EXPECTED_HEADERS.length; i++) {
                Cell cell = headerRow.getCell(i);
                String actual = cell != null ? cell.getStringCellValue().trim() : "";
                if (!EXPECTED_HEADERS[i].equalsIgnoreCase(actual)) {
                    return error(String.format(
                            "Encabezado incorrecto en columna %d. Esperado: \"%s\", encontrado: \"%s\". " +
                                    "Descargue la plantilla oficial.",
                            i + 1, EXPECTED_HEADERS[i], actual));
                }
            }

            List<Map<String, Object>> filas = new ArrayList<>();
            Set<String> seenKeys    = new HashSet<>();
            boolean     tieneErrores = false;

            for (int r = 1; r <= sheet.getLastRowNum(); r++) {
                Row row = sheet.getRow(r);
                if (row == null) continue;

                String nombre   = cellStr(row.getCell(0));
                String curso    = cellStr(row.getCell(1));
                String paralelo = cellStr(row.getCell(2));

                List<String> errores = new ArrayList<>();

                if (nombre == null || nombre.isBlank())
                    errores.add("El nombre completo es obligatorio.");
                 else if (nombre.matches(".*\\d.*"))
                    errores.add("El nombre no debe contener números.");
                else if (nombre.length() > 255)
                    errores.add("El nombre supera los 255 caracteres.");

                String clave = (nombre + "|" + curso + "|" + paralelo).toLowerCase().strip();
                if (!seenKeys.add(clave)) {
                    errores.add("Fila duplicada dentro del archivo.");
                }

                boolean filaTieneError = !errores.isEmpty();
                if (filaTieneError) tieneErrores = true;

                Map<String, Object> fila = new LinkedHashMap<>();
                fila.put("fila",          r + 1);          // nro fila visible (1-based para el usuario)
                fila.put("nombreCompleto", nombre != null ? nombre : "");
                fila.put("curso",          curso   != null ? curso   : "");
                fila.put("paralelo",       paralelo != null ? paralelo : "");
                fila.put("errores",        errores);
                fila.put("valida",         !filaTieneError);
                filas.add(fila);
            }

            if (filas.isEmpty()) {
                return error("El archivo no contiene datos. Solo se detectaron los encabezados.");
            }

            Map<String, Object> result = new LinkedHashMap<>();
            result.put("exito",        !tieneErrores);
            result.put("tieneErrores",  tieneErrores);
            result.put("totalFilas",    filas.size());
            result.put("filas",         filas);
            result.put("mensaje",       tieneErrores
                    ? "El archivo contiene errores. Corrígelos antes de importar."
                    : String.format("Archivo válido. %d participante(s) listos para importar.", filas.size())
            );
            return objectMapper.valueToTree(result);

        } catch (BadRequestException e) {
            throw e;
        } catch (Exception e) {
            log.error("[Asistencia] Error procesando Excel", e);
            return error("No se pudo leer el archivo. Asegúrate de que sea un .xlsx válido.");
        }
    }

    @Override
    public JsonNode inicializarAsistencia(Integer idRegistro) {
        try {
            String raw = repo.inicializarAsistencia(idRegistro);
            return parsear(raw);
        } catch (Exception e) {
            log.error("[Asistencia] inicializarAsistencia registro={}", idRegistro, e);
            throw new BadRequestException("Error al inicializar asistencia: " + e.getMessage());
        }
    }

    @Override
    public JsonNode guardarAsistencias(Integer idRegistro,
                                       List<Map<String, Object>> asistencias) {
        try {
            String json = objectMapper.writeValueAsString(asistencias);
            String raw  = repo.guardarAsistencias(idRegistro, json);
            return parsear(raw);
        } catch (BadRequestException e) {
            throw e;
        } catch (Exception e) {
            log.error("[Asistencia] guardarAsistencias registro={}", idRegistro, e);
            throw new BadRequestException("Error al guardar asistencias: " + e.getMessage());
        }
    }

    @Override
    public JsonNode consultarAsistencia(Integer idRegistro) {
        try {
            String raw = repo.consultarAsistencia(idRegistro);
            return objectMapper.readTree(raw);
        } catch (Exception e) {
            log.error("[Asistencia] consultarAsistencia registro={}", idRegistro, e);
            throw new BadRequestException("Error al consultar asistencia: " + e.getMessage());
        }
    }

    private JsonNode parsear(String raw) {
        try {
            return objectMapper.readTree(raw);
        } catch (Exception e) {
            throw new BadRequestException("Respuesta inesperada del servidor.");
        }
    }

    private JsonNode error(String mensaje) {
        return objectMapper.valueToTree(Map.of("exito", false, "mensaje", mensaje));
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