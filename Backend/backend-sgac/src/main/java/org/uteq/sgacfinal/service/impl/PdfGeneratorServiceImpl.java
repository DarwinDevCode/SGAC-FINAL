package org.uteq.sgacfinal.service.impl;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;
import org.uteq.sgacfinal.dto.Response.LogAuditoriaResponseDTO;
import org.uteq.sgacfinal.service.IPdfGeneratorService;
import org.uteq.sgacfinal.dto.Response.UsuarioResponseDTO;
import org.uteq.sgacfinal.dto.Response.FacultadResponseDTO;
import org.uteq.sgacfinal.dto.Response.CarreraResponseDTO;
import org.uteq.sgacfinal.dto.Response.TipoRolResponseDTO;

import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class PdfGeneratorServiceImpl implements IPdfGeneratorService {

    @Override
    public byte[] generarReporteAuditoria(List<LogAuditoriaResponseDTO> logs, String filtros) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4.rotate());
            PdfWriter.getInstance(document, baos);
            
            document.open();
            
            // Titulo
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
            Paragraph title = new Paragraph("Reporte de Auditoría SGAC", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(10f);
            document.add(title);
            
            // Filtros aplicados
            Font filterFont = FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 10);
            Paragraph filters = new Paragraph("Filtros aplicados: " + (filtros != null && !filtros.isEmpty() ? filtros : "Ninguno"), filterFont);
            filters.setAlignment(Element.ALIGN_CENTER);
            filters.setSpacingAfter(20f);
            document.add(filters);
            
            // Tabla
            PdfPTable table = new PdfPTable(6);
            table.setWidthPercentage(100);
            float[] columnWidths = {2f, 2f, 1.5f, 2.5f, 1.5f, 2.5f};
            table.setWidths(columnWidths);

            String[] head = {"Fecha Hora", "Usuario", "Acción", "Tabla Afectada", "Reg. ID", "IP Origen"};
            Font headFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11);
            for (String h : head) {
                PdfPCell cell = new PdfPCell(new Phrase(h, headFont));
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell.setPadding(5f);
                table.addCell(cell);
            }

            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
            Font cellFont = FontFactory.getFont(FontFactory.HELVETICA, 10);

            for (LogAuditoriaResponseDTO log : logs) {
                table.addCell(new Phrase(log.getFechaHora() != null ? log.getFechaHora().format(dtf) : "N/A", cellFont));
                table.addCell(new Phrase(log.getNombreUsuario(), cellFont));
                table.addCell(new Phrase(log.getAccion(), cellFont));
                table.addCell(new Phrase(log.getTablaAfectada(), cellFont));
                table.addCell(new Phrase(log.getRegistroAfectado() != null ? log.getRegistroAfectado().toString() : "N/A", cellFont));
                table.addCell(new Phrase(log.getIpOrigen() != null ? log.getIpOrigen() : "N/A", cellFont));
            }

            document.add(table);
            document.close();
            
            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Error al generar PDF de auditoría", e);
        }
    }
    
    @Override
    public byte[] generarReporteUsuarios(List<UsuarioResponseDTO> usuarios) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4.rotate());
            PdfWriter.getInstance(document, baos);
            document.open();

            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
            Paragraph title = new Paragraph("Reporte General de Usuarios", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(20f);
            document.add(title);

            PdfPTable table = new PdfPTable(6);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{1f, 2f, 2.5f, 2.5f, 2f, 1f});

            String[] head = {"ID", "Cédula", "Nombres", "Apellidos", "Correo", "Estado"};
            Font headFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11);
            for (String h : head) {
                PdfPCell cell = new PdfPCell(new Phrase(h, headFont));
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell.setPadding(5f);
                table.addCell(cell);
            }

            Font cellFont = FontFactory.getFont(FontFactory.HELVETICA, 10);
            for (UsuarioResponseDTO user : usuarios) {
                table.addCell(new Phrase(user.getIdUsuario() != null ? user.getIdUsuario().toString() : "-", cellFont));
                table.addCell(new Phrase(user.getCedula() != null ? user.getCedula() : "-", cellFont));
                table.addCell(new Phrase(user.getNombres() != null ? user.getNombres() : "-", cellFont));
                table.addCell(new Phrase(user.getApellidos() != null ? user.getApellidos() : "-", cellFont));
                table.addCell(new Phrase(user.getCorreo() != null ? user.getCorreo() : "-", cellFont));
                
                String estado = (user.getActivo() != null && user.getActivo()) ? "Activo" : "Inactivo";
                table.addCell(new Phrase(estado, cellFont));
            }

            document.add(table);
            document.close();
            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Error al generar PDF de Usuarios", e);
        }
    }

    @Override
    public byte[] generarReporteCatalogos(List<FacultadResponseDTO> facultades, List<CarreraResponseDTO> carreras) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4);
            PdfWriter.getInstance(document, baos);
            document.open();

            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
            Paragraph title = new Paragraph("Reporte de Oferta Académica", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(20f);
            document.add(title);

            Font sectionFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14);
            Font cellFont = FontFactory.getFont(FontFactory.HELVETICA, 10);

            // Facultades
            if (facultades != null && !facultades.isEmpty()) {
                Paragraph facTitle = new Paragraph("Facultades Registradas", sectionFont);
                facTitle.setSpacingAfter(10f);
                document.add(facTitle);

                PdfPTable tableFac = new PdfPTable(3);
                tableFac.setWidthPercentage(100);
                tableFac.setWidths(new float[]{1f, 4f, 1f});
                tableFac.addCell(new Phrase("ID", sectionFont));
                tableFac.addCell(new Phrase("Nombre Facultad", sectionFont));
                tableFac.addCell(new Phrase("Estado", sectionFont));

                for (FacultadResponseDTO f : facultades) {
                    tableFac.addCell(new Phrase(f.getIdFacultad() != null ? f.getIdFacultad().toString() : "-", cellFont));
                    tableFac.addCell(new Phrase(f.getNombreFacultad() != null ? f.getNombreFacultad() : "-", cellFont));
                    String estado = (f.getActivo() != null && f.getActivo()) ? "Activo" : "Inactivo";
                    tableFac.addCell(new Phrase(estado, cellFont));
                }
                document.add(tableFac);
            }

            // Carreras
            if (carreras != null && !carreras.isEmpty()) {
                Paragraph carTitle = new Paragraph("Carreras Registradas", sectionFont);
                carTitle.setSpacingBefore(20f);
                carTitle.setSpacingAfter(10f);
                document.add(carTitle);

                PdfPTable tableCar = new PdfPTable(3);
                tableCar.setWidthPercentage(100);
                tableCar.setWidths(new float[]{1f, 4f, 1f});
                tableCar.addCell(new Phrase("ID", sectionFont));
                tableCar.addCell(new Phrase("Nombre Carrera", sectionFont));
                tableCar.addCell(new Phrase("Estado", sectionFont));

                for (CarreraResponseDTO c : carreras) {
                    tableCar.addCell(new Phrase(c.getIdCarrera() != null ? c.getIdCarrera().toString() : "-", cellFont));
                    tableCar.addCell(new Phrase(c.getNombreCarrera() != null ? c.getNombreCarrera() : "-", cellFont));
                    String estado = (c.getActivo() != null && c.getActivo()) ? "Activo" : "Inactivo";
                    tableCar.addCell(new Phrase(estado, cellFont));
                }
                document.add(tableCar);
            }

            document.close();
            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Error al generar PDF de Catálogos", e);
        }
    }

    @Override
    public byte[] generarMatrizPermisos(String permisosDataStr) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4.rotate());
            PdfWriter.getInstance(document, baos);
            document.open();

            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
            Paragraph title = new Paragraph("Matriz de Permisos de Roles", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(20f);
            document.add(title);
            
            Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 10);
            Paragraph expl = new Paragraph("Filtros aplicados o resumen: " + permisosDataStr, normalFont);
            expl.setSpacingAfter(10f);
            document.add(expl);
            
            Paragraph advice = new Paragraph("Esta matriz incluye un resumen del cruce de permisos a nivel sistema.", normalFont);
            document.add(advice);

            document.close();
            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Error al generar PDF de Permisos", e);
        }
    }

    @Override
    public byte[] generarReporteDashboard(String dashboardDataStr) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4);
            PdfWriter.getInstance(document, baos);
            document.open();

            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
            Paragraph title = new Paragraph("Resumen Estadístico y Dashboard", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(20f);
            document.add(title);
            
            Font headFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
            Font cellFont = FontFactory.getFont(FontFactory.HELVETICA, 10);
            
            ObjectMapper om = new ObjectMapper();
            Map<String, Object> data = om.readValue(dashboardDataStr, new TypeReference<Map<String,Object>>() {});
            
            PdfPTable table = new PdfPTable(2);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{3f, 2f});
            
            table.addCell(new Phrase("Métrica", headFont));
            table.addCell(new Phrase("Valor", headFont));
            
            for (Map.Entry<String, Object> entry : data.entrySet()) {
                table.addCell(new Phrase(entry.getKey(), cellFont));
                table.addCell(new Phrase(String.valueOf(entry.getValue()), cellFont));
            }
            
            document.add(table);

            document.close();
            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Error al generar PDF de Dashboard", e);
        }
    }
}
