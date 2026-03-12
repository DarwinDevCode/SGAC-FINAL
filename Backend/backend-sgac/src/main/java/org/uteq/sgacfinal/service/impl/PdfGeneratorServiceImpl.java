package org.uteq.sgacfinal.service.impl;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;
import org.uteq.sgacfinal.dto.Response.LogAuditoriaResponseDTO;
import org.uteq.sgacfinal.service.IPdfGeneratorService;

import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.List;

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
}
