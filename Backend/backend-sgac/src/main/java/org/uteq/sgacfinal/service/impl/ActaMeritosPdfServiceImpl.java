package org.uteq.sgacfinal.service.impl;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;
import org.uteq.sgacfinal.entity.Convocatoria;
import org.uteq.sgacfinal.entity.EvaluacionMeritos;
import org.uteq.sgacfinal.entity.Postulacion;
import org.uteq.sgacfinal.entity.Usuario;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class ActaMeritosPdfServiceImpl {

    public byte[] generarPdf(Postulacion postulacion, EvaluacionMeritos evaluacion) {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();

            Document document = new Document(PageSize.A4, 40, 40, 50, 40);
            PdfWriter.getInstance(document, out);
            document.open();

            Font titleFont = new Font(Font.HELVETICA, 16, Font.BOLD);
            Font subTitleFont = new Font(Font.HELVETICA, 12, Font.BOLD);
            Font normalFont = new Font(Font.HELVETICA, 11, Font.NORMAL);

            Convocatoria convocatoria = postulacion.getConvocatoria();
            Usuario usuario = postulacion.getEstudiante().getUsuario();

            String nombreCompleto = usuario.getNombres() + " " + usuario.getApellidos();
            String cedula = usuario.getCedula();
            String matricula = postulacion.getEstudiante().getMatricula();
            String carrera = postulacion.getEstudiante().getCarrera().getNombreCarrera();
            String asignatura = convocatoria.getAsignatura().getNombreAsignatura();
            String docente = convocatoria.getDocente().getUsuario().getNombres() + " "
                    + convocatoria.getDocente().getUsuario().getApellidos();

            BigDecimal notaAsignatura = nvl(evaluacion.getNotaAsignatura());
            BigDecimal notaSemestres = nvl(evaluacion.getNotaSemestres());
            BigDecimal notaEventos = nvl(evaluacion.getNotaEventos());
            BigDecimal notaExperiencia = nvl(evaluacion.getNotaExperiencia());
            BigDecimal total = notaAsignatura
                    .add(notaSemestres)
                    .add(notaEventos)
                    .add(notaExperiencia);

            Paragraph titulo = new Paragraph("ACTA DE MÉRITOS", titleFont);
            titulo.setAlignment(Element.ALIGN_CENTER);
            document.add(titulo);

            document.add(new Paragraph("Sistema de Gestión de Ayudantías de Cátedra", subTitleFont));
            document.add(new Paragraph("Universidad Técnica Estatal de Quevedo", normalFont));
            document.add(new Paragraph("Fecha de generación: " +
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")), normalFont));
            document.add(new Paragraph(" "));

            document.add(new Paragraph("DATOS DEL POSTULANTE", subTitleFont));
            document.add(new Paragraph("Nombre completo: " + nombreCompleto, normalFont));
            document.add(new Paragraph("Cédula: " + cedula, normalFont));
            document.add(new Paragraph("Matrícula: " + matricula, normalFont));
            document.add(new Paragraph("Carrera: " + carrera, normalFont));
            document.add(new Paragraph(" "));

            document.add(new Paragraph("DATOS DE LA CONVOCATORIA", subTitleFont));
            document.add(new Paragraph("Asignatura: " + asignatura, normalFont));
            document.add(new Paragraph("Docente responsable: " + docente, normalFont));
            document.add(new Paragraph("Fecha publicación: " + formatDate(convocatoria.getFechaPublicacion()), normalFont));
            document.add(new Paragraph("Fecha cierre: " + formatDate(convocatoria.getFechaCierre()), normalFont));
            document.add(new Paragraph(" "));

            document.add(new Paragraph("CALIFICACIÓN DE MÉRITOS", subTitleFont));
            document.add(new Paragraph(" "));

            PdfPTable table = new PdfPTable(2);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{4, 2});

            addHeaderCell(table, "Criterio");
            addHeaderCell(table, "Puntaje");

            addBodyCell(table, "Nota por asignatura");
            addBodyCell(table, notaAsignatura.toString());

            addBodyCell(table, "Nota por semestres");
            addBodyCell(table, notaSemestres.toString());

            addBodyCell(table, "Participación en eventos");
            addBodyCell(table, notaEventos.toString());

            addBodyCell(table, "Experiencia");
            addBodyCell(table, notaExperiencia.toString());

            addHeaderCell(table, "TOTAL MÉRITOS");
            addHeaderCell(table, total.toString());

            document.add(table);
            document.add(new Paragraph(" "));

            document.add(new Paragraph(
                    "Con base en la evaluación de méritos realizada, se deja constancia de la calificación obtenida por el postulante dentro del proceso de selección para ayudantía de cátedra.",
                    normalFont
            ));

            document.add(new Paragraph(" "));
            document.add(new Paragraph("______________________________", normalFont));
            document.add(new Paragraph("Coordinador / Responsable", normalFont));

            document.close();
            return out.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Error al generar PDF del acta de méritos", e);
        }
    }

    private BigDecimal nvl(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private String formatDate(LocalDate date) {
        return date == null ? "" : date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    }

    private void addHeaderCell(PdfPTable table, String text) {
        PdfPCell cell = new PdfPCell(new Phrase(text, new Font(Font.HELVETICA, 11, Font.BOLD)));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setPadding(8);
        table.addCell(cell);
    }

    private void addBodyCell(PdfPTable table, String text) {
        PdfPCell cell = new PdfPCell(new Phrase(text, new Font(Font.HELVETICA, 11, Font.NORMAL)));
        cell.setPadding(8);
        table.addCell(cell);
    }
}