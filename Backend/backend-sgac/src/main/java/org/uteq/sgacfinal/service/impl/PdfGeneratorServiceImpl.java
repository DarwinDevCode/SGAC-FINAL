package org.uteq.sgacfinal.service.impl;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfWriter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.uteq.sgacfinal.entity.*;
import org.uteq.sgacfinal.repository.*;
import org.uteq.sgacfinal.service.IPdfGeneratorService;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PdfGeneratorServiceImpl implements IPdfGeneratorService {

    private final ResumenEvaluacionRepository resumenRepo;
    private final CalificacionOposicionIndividualRepository oposicionRepo;
    private final EvaluacionOposicionRepository evOposicionRepo;
    private final SorteoOposicionRepository sorteoRepo;
    private final ComisionSeleccionRepository comisionRepo;

    @Override
    public byte[] generarActaMeritos(Postulacion postulacion) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document document = new Document();
            PdfWriter.getInstance(document, baos);
            document.open();

            // Font definitions
            Font fontBold = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11);
            Font fontNormal = FontFactory.getFont(FontFactory.HELVETICA, 11);
            Font fontTitle = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);

            // TODO: Fetch Real Data
            String carrera = postulacion.getConvocatoria().getAsignatura().getCarrera() != null 
                ? postulacion.getConvocatoria().getAsignatura().getCarrera().getNombreCarrera() : "CARRERA NO ESPECIFICADA";
            String asignatura = postulacion.getConvocatoria().getAsignatura().getNombreAsignatura();
            String nombrePostulante = postulacion.getEstudiante().getUsuario().getNombres() + " " + postulacion.getEstudiante().getUsuario().getApellidos();
            String cedula = postulacion.getEstudiante().getUsuario().getCedula();
            String fechaActual = LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));

            // Retrieve summary and individual metrics
            ResumenEvaluacion resumen = resumenRepo.findByIdPostulacion(postulacion.getIdPostulacion()).orElse(null);
            BigDecimal totalMeritos = resumen != null ? resumen.getTotalMeritos() : BigDecimal.ZERO;
            
            // Evaluadores
            String decano = "N/A";
            String coordinador = "N/A";
            String docente = "N/A";
            List<ComisionSeleccion> comisiones = comisionRepo.findByConvocatoria_IdConvocatoria(postulacion.getConvocatoria().getIdConvocatoria());
            if (!comisiones.isEmpty() && !comisiones.get(0).getUsuariosComision().isEmpty()) {
                for (UsuarioComision uc : comisiones.get(0).getUsuariosComision()) {
                    String nombre = uc.getUsuario().getNombres() + " " + uc.getUsuario().getApellidos();
                    if ("DECANO".equalsIgnoreCase(uc.getRolIntegrante())) decano = nombre;
                    else if ("COORDINADOR".equalsIgnoreCase(uc.getRolIntegrante())) coordinador = nombre;
                    else if ("DOCENTE".equalsIgnoreCase(uc.getRolIntegrante())) docente = nombre;
                }
            }

            // HEADER
            document.add(new Paragraph("DE: COMISIÓN DE SELECCIÓN AYUDANTIAS DE CÁTEDRA CARRERA " + carrera.toUpperCase(), fontTitle));
            document.add(new Paragraph(" "));
            
            document.add(new Paragraph("Lugar: Plataforma Google.        Acta No: " + postulacion.getIdPostulacion(), fontNormal));
            document.add(new Paragraph("Fecha: " + fechaActual, fontNormal));
            document.add(new Paragraph(" "));

            document.add(new Paragraph("Asistentes:", fontBold));
            document.add(new Paragraph("1. " + decano + " (Subdecana/Decano)", fontNormal));
            document.add(new Paragraph("2. " + coordinador + " (Coordinador)", fontNormal));
            document.add(new Paragraph("3. " + docente + " (Docente y Secretario)", fontNormal));
            document.add(new Paragraph(" "));

            document.add(new Paragraph("Orden del día:", fontBold));
            document.add(new Paragraph("En la respectiva sede, el " + fechaActual + " se instala la Comisión de Selección de ayudante de cátedra, para realizar el proceso de calificación de la postulante " + nombrePostulante + " en la asignatura '" + asignatura + "' para la carrera de " + carrera + ".", fontNormal));
            
            document.add(new Paragraph("La valoración de méritos se realiza acorde a lo señalado por el Reglamento Vigente:", fontNormal));
            document.add(new Paragraph(" "));
            
            // PUNTUACIÓN DE MÉRITOS
            document.add(new Paragraph("PUNTUACIÓN DE MÉRITOS (Siempre que cumpla todos los requisitos)", fontBold));
            
            BigDecimal nAsignatura = BigDecimal.ZERO, nSemestres = BigDecimal.ZERO, nEventos = BigDecimal.ZERO, nExperiencia = BigDecimal.ZERO;
            if (postulacion.getEvaluacionesMeritos() != null && !postulacion.getEvaluacionesMeritos().isEmpty()) {
                EvaluacionMeritos em = postulacion.getEvaluacionesMeritos().get(0);
                nAsignatura = em.getNotaAsignatura() != null ? em.getNotaAsignatura() : BigDecimal.ZERO;
                nSemestres = em.getNotaSemestres() != null ? em.getNotaSemestres() : BigDecimal.ZERO;
                nEventos = em.getNotaEventos() != null ? em.getNotaEventos() : BigDecimal.ZERO;
                nExperiencia = em.getNotaExperiencia() != null ? em.getNotaExperiencia() : BigDecimal.ZERO;
            }

            document.add(new Paragraph("- Calificación final de la asignatura: " + nAsignatura, fontNormal));
            document.add(new Paragraph("- Puntajes obtenidos por cada semestre: " + nSemestres, fontNormal));
            document.add(new Paragraph("- Experiencia en actividades de colaboración: " + nExperiencia, fontNormal));
            document.add(new Paragraph("- Participación en eventos relacionados: " + nEventos, fontNormal));
            document.add(new Paragraph("TOTAL MÉRITOS: " + totalMeritos, fontBold));
            document.add(new Paragraph(" "));
            
            // TEMA SORTEADO
            String temaSorteado = sorteoRepo.findByIdPostulacion(postulacion.getIdPostulacion())
                .map(SorteoOposicion::getTemaSorteado)
                .orElseGet(() -> evOposicionRepo.findByPostulacion_IdPostulacion(postulacion.getIdPostulacion())
                        .map(org.uteq.sgacfinal.entity.EvaluacionOposicion::getTemaExposicion)
                        .orElse("Pendiente de sorteo"));

            document.add(new Paragraph("Concomitante a ello, mediante sorteo/asignación directa, se determina que debe presentar una exposición sobre: \"" + temaSorteado + "\"", fontNormal));

            document.add(new Paragraph(" "));
            document.add(new Paragraph("RESOLUCIONES:", fontBold));
            document.add(new Paragraph("Dejar constancia de lo actuado en el Acta de la Sesión y notificar." , fontNormal));

            document.close();
            return baos.toByteArray();
        } catch (DocumentException | java.io.IOException e) {
            throw new RuntimeException("Error generating Acta Meritos PDF", e);
        }
    }

    @Override
    public byte[] generarActaOposicion(Postulacion postulacion) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document document = new Document();
            PdfWriter.getInstance(document, baos);
            document.open();

            Font fontBold = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11);
            Font fontNormal = FontFactory.getFont(FontFactory.HELVETICA, 11);
            Font fontTitle = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);

            String carrera = postulacion.getConvocatoria().getAsignatura().getCarrera() != null 
                ? postulacion.getConvocatoria().getAsignatura().getCarrera().getNombreCarrera() : "CARRERA NO ESPECIFICADA";
            String asignatura = postulacion.getConvocatoria().getAsignatura().getNombreAsignatura();
            String nombrePostulante = postulacion.getEstudiante().getUsuario().getNombres() + " " + postulacion.getEstudiante().getUsuario().getApellidos();
            String cedula = postulacion.getEstudiante().getUsuario().getCedula();
            String fechaActual = LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));

            ResumenEvaluacion resumen = resumenRepo.findByIdPostulacion(postulacion.getIdPostulacion()).orElse(null);
            BigDecimal totalMeritos = resumen != null && resumen.getTotalMeritos() != null ? resumen.getTotalMeritos() : BigDecimal.ZERO;
            BigDecimal promedioOposicion = resumen != null && resumen.getPromedioOposicion() != null ? resumen.getPromedioOposicion() : BigDecimal.ZERO;
            BigDecimal totalFinal = resumen != null && resumen.getTotalFinal() != null ? resumen.getTotalFinal() : BigDecimal.ZERO;

            document.add(new Paragraph("UNIVERSIDAD TÉCNICA ESTATAL DE QUEVEDO", fontTitle));
            document.add(new Paragraph("ACTA DE REUNIÓN - OPOSICIÓN", fontTitle));
            document.add(new Paragraph(" "));
            
            document.add(new Paragraph("Lugar: Plataforma Google.        Fecha: " + fechaActual, fontNormal));
            document.add(new Paragraph(" "));

            document.add(new Paragraph("Desarrollo:", fontBold));
            document.add(new Paragraph("Se establece considerar a la/el postulante " + nombrePostulante + ", quien en la valoración de méritos alcanzó el puntaje de " + totalMeritos + " en la asignatura '" + asignatura + "'.", fontNormal));
            document.add(new Paragraph("Acto seguido se da paso a la exposición del estudiante.", fontNormal));
            document.add(new Paragraph(" "));

            document.add(new Paragraph("Promedio de la calificación del proceso de Oposición:", fontBold));
            
            List<CalificacionOposicionIndividual> calificaciones = oposicionRepo.findByIdPostulacion(postulacion.getIdPostulacion());
            BigDecimal matSum = BigDecimal.ZERO;
            BigDecimal calSum = BigDecimal.ZERO;
            BigDecimal pertSum = BigDecimal.ZERO;
            
            for(CalificacionOposicionIndividual c : calificaciones) {
                matSum = matSum.add(c.getCriterioMaterial() != null ? c.getCriterioMaterial() : BigDecimal.ZERO);
                calSum = calSum.add(c.getCriterioCalidad() != null ? c.getCriterioCalidad() : BigDecimal.ZERO);
                pertSum = pertSum.add(c.getCriterioPertinencia() != null ? c.getCriterioPertinencia() : BigDecimal.ZERO);
            }
            
            document.add(new Paragraph("- Desarrollo material (sobre 30): " + matSum, fontNormal));
            document.add(new Paragraph("- Calidad de exposición (sobre 12): " + calSum, fontNormal));
            document.add(new Paragraph("- Pertinencia respuestas (sobre 18): " + pertSum, fontNormal));
            document.add(new Paragraph("PROMEDIO OPOSICIÓN (sobre 20): " + promedioOposicion, fontBold));
            document.add(new Paragraph(" "));

            document.add(new Paragraph("RESOLUCIONES:", fontBold));
            document.add(new Paragraph("Calificación de Méritos:    " + totalMeritos, fontNormal));
            document.add(new Paragraph("Calificación de Oposición:  " + promedioOposicion, fontNormal));
            document.add(new Paragraph("Calificación Total:         " + totalFinal, fontBold));
            
            document.add(new Paragraph(" "));
            document.add(new Paragraph("Se resuelve designar/informar el puntaje del estudiante " + nombrePostulante + " con C.I. " + cedula + " con la calificación final de " + totalFinal + " puntos.", fontNormal));

            document.close();
            return baos.toByteArray();
        } catch (DocumentException | java.io.IOException e) {
            throw new RuntimeException("Error generating Acta Oposicion PDF", e);
        }
    }
}
