package org.uteq.sgacfinal.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.uteq.sgacfinal.dto.Response.AdminConsultaDTO;
import org.uteq.sgacfinal.dto.Response.CarreraResponseDTO;
import org.uteq.sgacfinal.dto.Response.FacultadResponseDTO;
import org.uteq.sgacfinal.dto.Response.UsuarioResponseDTO;
import org.uteq.sgacfinal.dto.Response.LogAuditoriaResponseDTO;
import org.uteq.sgacfinal.service.IExcelGeneratorService;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExcelGeneratorServiceImpl implements IExcelGeneratorService {

    @Override
    public byte[] generarExcelUsuarios(List<UsuarioResponseDTO> usuarios) {
        log.info("Iniciando generación de Excel para {} usuarios", usuarios != null ? usuarios.size() : "null");
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Usuarios");

            CellStyle headerStyle = createHeaderStyle(workbook);

            Row headerRow = sheet.createRow(0);
            String[] headers = {"ID", "Cédula", "Nombres", "Apellidos", "Correo", "Usuario", "Estado"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowIdx = 1;
            if (usuarios != null) {
                for (UsuarioResponseDTO usuario : usuarios) {
                    if (usuario == null) continue;
                    Row row = sheet.createRow(rowIdx++);
                    row.createCell(0).setCellValue(usuario.getIdUsuario() != null ? usuario.getIdUsuario() : 0);
                    row.createCell(1).setCellValue(usuario.getCedula() != null ? usuario.getCedula() : "N/A");
                    row.createCell(2).setCellValue(usuario.getNombres() != null ? usuario.getNombres() : "");
                    row.createCell(3).setCellValue(usuario.getApellidos() != null ? usuario.getApellidos() : "");
                    row.createCell(4).setCellValue(usuario.getCorreo() != null ? usuario.getCorreo() : "");
                    row.createCell(5).setCellValue(usuario.getNombreUsuario() != null ? usuario.getNombreUsuario() : "");
                    row.createCell(6).setCellValue(usuario.getActivo() != null && usuario.getActivo() ? "Activo" : "Inactivo");
                }
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            log.info("Excel de usuarios generado exitosamente");
            return out.toByteArray();
        } catch (Exception e) {
            log.error("Error crítico al generar Excel de usuarios: {}", e.getMessage(), e);
            throw new RuntimeException("Error generating Excel file", e);
        }
    }

    @Override
    public byte[] generarExcelCatalogos(List<FacultadResponseDTO> facultades, List<CarreraResponseDTO> carreras) {
        log.info("Iniciando generación de Excel para catálogos. Facultades: {}, Carreras: {}", 
                facultades != null ? facultades.size() : "null", 
                carreras != null ? carreras.size() : "null");
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            
            CellStyle headerStyle = createHeaderStyle(workbook);

            // Sheet 1: Facultades
            Sheet sheetFacultades = workbook.createSheet("Facultades");
            Row headerRowFacultad = sheetFacultades.createRow(0);
            String[] facultadesHeaders = {"ID", "Nombre", "Estado"};
            for (int i = 0; i < facultadesHeaders.length; i++) {
                Cell cell = headerRowFacultad.createCell(i);
                cell.setCellValue(facultadesHeaders[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowIdx = 1;
            if (facultades != null) {
                for (FacultadResponseDTO f : facultades) {
                    if (f == null) continue;
                    Row row = sheetFacultades.createRow(rowIdx++);
                    row.createCell(0).setCellValue(f.getIdFacultad() != null ? f.getIdFacultad() : 0);
                    row.createCell(1).setCellValue(f.getNombreFacultad() != null ? f.getNombreFacultad() : "");
                    row.createCell(2).setCellValue(f.getActivo() != null && f.getActivo() ? "Activo" : "Inactivo");
                }
            }
            for (int i = 0; i < facultadesHeaders.length; i++) sheetFacultades.autoSizeColumn(i);

            // Sheet 2: Carreras
            Sheet sheetCarreras = workbook.createSheet("Carreras");
            Row headerRowCarrera = sheetCarreras.createRow(0);
            String[] carrerasHeaders = {"ID", "Nombre", "Facultad", "Estado"};
            for (int i = 0; i < carrerasHeaders.length; i++) {
                Cell cell = headerRowCarrera.createCell(i);
                cell.setCellValue(carrerasHeaders[i]);
                cell.setCellStyle(headerStyle);
            }
            
            rowIdx = 1;
            if (carreras != null) {
                for (CarreraResponseDTO c : carreras) {
                    if (c == null) continue;
                    Row row = sheetCarreras.createRow(rowIdx++);
                    row.createCell(0).setCellValue(c.getIdCarrera() != null ? c.getIdCarrera() : 0);
                    row.createCell(1).setCellValue(c.getNombreCarrera() != null ? c.getNombreCarrera() : "");
                    row.createCell(2).setCellValue(c.getNombreFacultad() != null ? c.getNombreFacultad() : "N/A");
                    row.createCell(3).setCellValue(c.getActivo() != null && c.getActivo() ? "Activo" : "Inactivo");
                }
            }
            for (int i = 0; i < carrerasHeaders.length; i++) sheetCarreras.autoSizeColumn(i);

            workbook.write(out);
            log.info("Excel de catálogos generado exitosamente");
            return out.toByteArray();
        } catch (Exception e) {
            log.error("Error crítico al generar Excel de catálogos: {}", e.getMessage(), e);
            throw new RuntimeException("Error generating Excel file", e);
        }
    }

    @Override
    public byte[] generarMatrizPermisos() {
        log.info("Iniciando generación de matriz de permisos");
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Matriz de Permisos");

            CellStyle headerStyle = createHeaderStyle(workbook);

            Row headerRow = sheet.createRow(0);
            Cell cell1 = headerRow.createCell(0);
            cell1.setCellValue("Matriz de Permisos");
            cell1.setCellStyle(headerStyle);

            Row dataRow = sheet.createRow(1);
            dataRow.createCell(0).setCellValue("Este reporte en Excel se desarrollará con la data de infraestructura en futuras versiones.");

            sheet.autoSizeColumn(0);

            workbook.write(out);
            log.info("Matriz de permisos generada exitosamente");
            return out.toByteArray();
        } catch (Exception e) {
            log.error("Error crítico al generar matriz de permisos: {}", e.getMessage(), e);
            throw new RuntimeException("Error generating Excel file", e);
        }
    }

    @Override
    public byte[] generarExcelDashboard(AdminConsultaDTO dashboardData) {
        log.info("Iniciando generación de Excel para Dashboard");
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Estadísticas del Sistema");

            CellStyle headerStyle = createHeaderStyle(workbook);

            Row titleRow = sheet.createRow(0);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("Indicadores del Sistema");
            titleCell.setCellStyle(headerStyle);

            if (dashboardData != null) {
                String[] metrics = {"Total Usuarios", "Total Postulaciones", "Total Convocatorias", "Período Activo"};
                Object[] values = {
                        dashboardData.getTotalUsuarios(),
                        dashboardData.getTotalPostulaciones(),
                        dashboardData.getTotalConvocatorias(),
                        dashboardData.getPeriodoActivo() != null ? dashboardData.getPeriodoActivo() : "N/A"
                };

                for (int i = 0; i < metrics.length; i++) {
                    Row row = sheet.createRow(i + 1);
                    row.createCell(0).setCellValue(metrics[i]);
                    row.createCell(1).setCellValue(values[i].toString());
                }
            }

            sheet.autoSizeColumn(0);
            sheet.autoSizeColumn(1);

            workbook.write(out);
            log.info("Excel de Dashboard generado exitosamente");
            return out.toByteArray();
        } catch (Exception e) {
            log.error("Error crítico al generar Excel de Dashboard: {}", e.getMessage(), e);
            throw new RuntimeException("Error generating Excel file", e);
        }
    }

    @Override
    public byte[] generarExcelAuditoria(List<LogAuditoriaResponseDTO> logs) {
        log.info("Iniciando generación de Excel para {} logs de auditoría", logs != null ? logs.size() : "null");
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Auditoría");

            CellStyle headerStyle = createHeaderStyle(workbook);

            Row headerRow = sheet.createRow(0);
            String[] headers = {"Fecha-Hora", "Usuario", "Acción", "Módulo", "ID Registro", "IP Origen", "Valor Anterior", "Valor Nuevo"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowIdx = 1;
            if (logs != null) {
                for (LogAuditoriaResponseDTO logItem : logs) {
                    if (logItem == null) continue;
                    Row row = sheet.createRow(rowIdx++);
                    row.createCell(0).setCellValue(logItem.getFechaHora() != null ? logItem.getFechaHora().toString() : "");
                    row.createCell(1).setCellValue(logItem.getNombreUsuario() != null ? logItem.getNombreUsuario() : "");
                    row.createCell(2).setCellValue(logItem.getAccion() != null ? logItem.getAccion() : "");
                    row.createCell(3).setCellValue(logItem.getTablaAfectada() != null ? logItem.getTablaAfectada() : "");
                    row.createCell(4).setCellValue(logItem.getRegistroAfectado() != null ? logItem.getRegistroAfectado().toString() : "");
                    row.createCell(5).setCellValue(logItem.getIpOrigen() != null ? logItem.getIpOrigen() : "");
                    row.createCell(6).setCellValue(logItem.getValorAnterior() != null ? logItem.getValorAnterior() : "");
                    row.createCell(7).setCellValue(logItem.getValorNuevo() != null ? logItem.getValorNuevo() : "");
                }
            }

            for (int i = 0; i < headers.length; i++) sheet.autoSizeColumn(i);

            workbook.write(out);
            log.info("Excel de auditoría generado exitosamente");
            return out.toByteArray();
        } catch (Exception e) {
            log.error("Error crítico al generar Excel de auditoría: {}", e.getMessage(), e);
            throw new RuntimeException("Error generating Excel file", e);
        }
    }

    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle headerStyle = workbook.createCellStyle();
        headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        Font font = workbook.createFont();
        font.setBold(true);
        headerStyle.setFont(font);
        return headerStyle;
    }
}
