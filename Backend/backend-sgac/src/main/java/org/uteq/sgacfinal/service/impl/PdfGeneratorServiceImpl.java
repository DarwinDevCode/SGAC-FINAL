package org.uteq.sgacfinal.service.impl;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.uteq.sgacfinal.entity.*;
import org.uteq.sgacfinal.repository.*;
import org.uteq.sgacfinal.service.IPdfGeneratorService;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

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

            // Fonts
            java.awt.Color darkGreen = new java.awt.Color(55, 96, 28);
            Font fontHeader = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, darkGreen);
            Font fontBold = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);
            Font fontNormal = FontFactory.getFont(FontFactory.HELVETICA, 10);
            Font fontSmall = FontFactory.getFont(FontFactory.HELVETICA, 9);
            Font fontSmallBold = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9);

            // Data Extraction
            String carrera = postulacion.getConvocatoria().getAsignatura().getCarrera() != null 
                ? postulacion.getConvocatoria().getAsignatura().getCarrera().getNombreCarrera() : "CARRERA NO ESPECIFICADA";
            String asignatura = postulacion.getConvocatoria().getAsignatura().getNombreAsignatura();
            String nombrePostulante = postulacion.getEstudiante().getUsuario().getNombres() + " " + postulacion.getEstudiante().getUsuario().getApellidos();
            String cedula = postulacion.getEstudiante().getUsuario().getCedula();
            
            LocalDate now = LocalDate.now();
            String fechaActualStr = now.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            String mesTexto = now.getMonth().getDisplayName(java.time.format.TextStyle.FULL, new Locale("es", "ES"));
            String dia = String.valueOf(now.getDayOfMonth());
            String anio = String.valueOf(now.getYear());
            String horaInicio = "09H30"; 
            String horaFin = "10H30";
            String ciudad = "Quevedo";

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

            ResumenEvaluacion resumen = resumenRepo.findByIdPostulacion(postulacion.getIdPostulacion()).orElse(null);
            BigDecimal totalMeritos = resumen != null && resumen.getTotalMeritos() != null ? resumen.getTotalMeritos() : BigDecimal.ZERO;
            
            BigDecimal nAsignatura = BigDecimal.ZERO, nSemestres = BigDecimal.ZERO, nEventos = BigDecimal.ZERO, nExperiencia = BigDecimal.ZERO;
            if (postulacion.getEvaluacionesMeritos() != null && !postulacion.getEvaluacionesMeritos().isEmpty()) {
                EvaluacionMeritos em = postulacion.getEvaluacionesMeritos().get(0);
                nAsignatura = em.getNotaAsignatura() != null ? em.getNotaAsignatura() : BigDecimal.ZERO;
                nSemestres = em.getNotaSemestres() != null ? em.getNotaSemestres() : BigDecimal.ZERO;
                nEventos = em.getNotaEventos() != null ? em.getNotaEventos() : BigDecimal.ZERO;
                nExperiencia = em.getNotaExperiencia() != null ? em.getNotaExperiencia() : BigDecimal.ZERO;
            }

            String temaSorteado = sorteoRepo.findByIdPostulacion(postulacion.getIdPostulacion())
                .map(SorteoOposicion::getTemaSorteado)
                .orElseGet(() -> evOposicionRepo.findByPostulacion_IdPostulacion(postulacion.getIdPostulacion())
                        .map(org.uteq.sgacfinal.entity.EvaluacionOposicion::getTemaExposicion)
                        .orElse("Pendiente de sorteo"));

            // --- PAGE 1 ---
            document.add(createHeaderTable("1 de 5", fontHeader));
            document.add(new Paragraph(" "));

            PdfPTable titleTable = new PdfPTable(1);
            titleTable.setWidthPercentage(100);
            PdfPCell titleCell = new PdfPCell(new Phrase("DE: COMISIÓN DE SELECCIÓN AYUDANTÍAS DE CÁTEDRA CARRERA " + carrera.toUpperCase(), fontBold));
            titleCell.setPadding(5);
            titleTable.addCell(titleCell);
            document.add(titleTable);
            document.add(new Paragraph(" "));

            PdfPTable infoTable = new PdfPTable(new float[]{25, 45, 15, 15});
            infoTable.setWidthPercentage(100);
            PdfPCell cLugar = new PdfPCell(new Phrase("Lugar:", fontBold)); cLugar.setPadding(5); infoTable.addCell(cLugar);
            PdfPCell cPlat = new PdfPCell(new Phrase("Plataforma Google.", fontBold)); cPlat.setPadding(5); infoTable.addCell(cPlat);
            PdfPCell cActaNo = new PdfPCell(new Phrase("Acta Nº:", fontBold)); cActaNo.setPadding(5); cActaNo.setRowspan(3); cActaNo.setVerticalAlignment(Element.ALIGN_MIDDLE); infoTable.addCell(cActaNo);
            PdfPCell cActaNum = new PdfPCell(new Phrase(String.format("%02d", postulacion.getIdPostulacion()), fontNormal)); cActaNum.setPadding(5); cActaNum.setRowspan(3); cActaNum.setVerticalAlignment(Element.ALIGN_MIDDLE); infoTable.addCell(cActaNum);
            PdfPCell cFechaL = new PdfPCell(new Phrase("Fecha:", fontBold)); cFechaL.setPadding(5); infoTable.addCell(cFechaL);
            PdfPCell cFechaV = new PdfPCell(new Phrase(fechaActualStr, fontBold)); cFechaV.setPadding(5); infoTable.addCell(cFechaV);
            PdfPCell cHoraL = new PdfPCell(new Phrase("Hora inicio/ fin:", fontBold)); cHoraL.setPadding(5); infoTable.addCell(cHoraL);
            PdfPCell cHoraV = new PdfPCell(new Phrase(horaInicio + " / " + horaFin, fontBold)); cHoraV.setPadding(5); infoTable.addCell(cHoraV);
            document.add(infoTable);
            document.add(new Paragraph(" "));

            PdfPTable asisTable = new PdfPTable(new float[]{35, 45, 20});
            asisTable.setWidthPercentage(100);
            PdfPCell cAsisH = new PdfPCell(new Phrase("Asistentes:", fontBold)); cAsisH.setColspan(3); cAsisH.setPadding(5); cAsisH.setBorder(Rectangle.NO_BORDER); asisTable.addCell(cAsisH);
            PdfPCell cAsisN = new PdfPCell(new Phrase("Nombre", fontBold)); cAsisN.setHorizontalAlignment(Element.ALIGN_CENTER); cAsisN.setPadding(5); asisTable.addCell(cAsisN);
            PdfPCell cAsisC = new PdfPCell(new Phrase("Cargo", fontBold)); cAsisC.setHorizontalAlignment(Element.ALIGN_CENTER); cAsisC.setPadding(5); asisTable.addCell(cAsisC);
            PdfPCell cAsisF = new PdfPCell(new Phrase("Firmas", fontBold)); cAsisF.setHorizontalAlignment(Element.ALIGN_CENTER); cAsisF.setPadding(5); asisTable.addCell(cAsisF);
            PdfPCell pDecano = new PdfPCell(new Phrase("Ing. " + decano, fontNormal)); pDecano.setPadding(5); asisTable.addCell(pDecano);
            PdfPCell pDecanoC = new PdfPCell(new Phrase("Subdecana/Decano de la Facultad de Ciencias de la Ingeniería", fontNormal)); pDecanoC.setPadding(5); asisTable.addCell(pDecanoC);
            asisTable.addCell(new PdfPCell(new Phrase(" ")));
            PdfPCell pCoord = new PdfPCell(new Phrase("Ing. " + coordinador, fontNormal)); pCoord.setPadding(5); asisTable.addCell(pCoord);
            PdfPCell pCoordC = new PdfPCell(new Phrase("Coordinador de la Carrera de " + carrera, fontNormal)); pCoordC.setPadding(5); asisTable.addCell(pCoordC);
            asisTable.addCell(new PdfPCell(new Phrase(" ")));
            PdfPCell pDoc = new PdfPCell(new Phrase("Ing. " + docente, fontNormal)); pDoc.setPadding(5); asisTable.addCell(pDoc);
            PdfPCell pDocC = new PdfPCell(new Phrase("Docente y secretario comisión.", fontNormal)); pDocC.setPadding(5); asisTable.addCell(pDocC);
            asisTable.addCell(new PdfPCell(new Phrase(" ")));
            document.add(asisTable);
            document.add(new Paragraph(" "));

            PdfPTable ordenTable = new PdfPTable(1);
            ordenTable.setWidthPercentage(100);
            PdfPCell cOrdenH = new PdfPCell(new Phrase("Orden del día:", fontBold)); cOrdenH.setPadding(5); ordenTable.addCell(cOrdenH);
            
            String ordenText = "En la Ciudad de " + ciudad + ", el " + dia + " de " + mesTexto + " del " + anio + ", siendo las " + horaInicio + " se instala la Comisión de Selección de ayudante de cátedra, para realizar el proceso de calificación de la postulante " + nombrePostulante + " en la asignatura \"" + asignatura + "\" para la carrera de " + carrera + ". La sesión se realiza por medio de videoconferencia, la misma es grabada como evidencia del proceso de selección.\n\n" +
                               "Preside la sesión la Ing. " + decano + ", Subdecana/Decano de la Facultad de Ciencias de la Ingeniería, quien da inicio a la sesión conformada además por el Ing. " + coordinador + ", Coordinador de la Carrera de " + carrera + " y el Ing. " + docente + ", profesor de la Carrera quien cumplirá la función de secretaria de la Comisión.\n\n" +
                               "La Subdecana pone a consideración el siguiente orden del día:\n\n" +
                               "1. Lectura de Normativa del Concurso.\n" +
                               "2.-Calificación de postulantes.\n" +
                               "3. Resoluciones.\n" +
                               "4.- Clausura.\n" +
                               "El orden del día propuesto es aprobado por los integrantes de la Comisión.";
            PdfPCell cOrdenB = new PdfPCell(new Phrase(ordenText, fontNormal)); cOrdenB.setPadding(8); ordenTable.addCell(cOrdenB);
            document.add(ordenTable);

            // --- PAGE 2 ---
            document.newPage();
            document.add(createHeaderTable("2 de 5", fontHeader));
            document.add(new Paragraph(" "));

            PdfPTable p2Table = new PdfPTable(1);
            p2Table.setWidthPercentage(100);
            PdfPCell p2Cell = new PdfPCell();
            p2Cell.setPadding(8);
            p2Cell.addElement(new Paragraph("Desarrollo:", fontBold));
            
            Paragraph p2P1 = new Paragraph();
            p2P1.add(new Chunk("PRIMERA.- LECTURA DE NORMATIVA DEL CONCURSO.- ", fontBold));
            p2P1.add(new Chunk("La Subdecana procede a dar lectura a los artículos de la normativa que hacen referencia a la facultad que se otorga a la Comisión designada para el Concurso de Ayudantes de Cátedra y las bases del concurso por constituirse en el fundamento del proceso de selección.\n", fontNormal));
            p2P1.setAlignment(Element.ALIGN_JUSTIFIED);
            p2Cell.addElement(p2P1);
            
            Paragraph p2P2 = new Paragraph("CAPÍTULO VIII\nDE LA COMISIÓN DE SELECCIÓN Y PROCESO DE APROBACIÓN", fontBold);
            p2P2.setAlignment(Element.ALIGN_CENTER);
            p2Cell.addElement(p2P2);
            p2Cell.addElement(new Paragraph(" "));

            String norm1 = "Artículo 1.- La Comisión de Selección será designada por el Consejo Académico de la respectiva Unidad Académica al día siguiente de concluidas las publicaciones de la convocatoria. Cuando se trate de ayudantía de Cátedra, la Comisión de Selección estará integrada por el/la Decano/a o su Delegado/a quien lo presidirá, el Coordinador de la Carrera y un/a profesor/a titular de la materia que actuará como Secretario. Cuando se trate de Ayudante de Investigación, la Comisión de Selección estará integrada por el/la Decano/a o su Delegado/a quien lo presidirá, el Director de Investigación y el Director del proyecto de Investigación en que participará el estudiante, quien actuará como Secretario.\n\n" +
                           "Artículo 2.- La Comisión de Selección, al siguiente día de finalizado el plazo que Secretaría de la Unidad Académica tiene para la difusión de la información a los participantes, se reunirá para verificar que los aspirantes cumplan los requisitos exigidos en la convocatoria y procederá a calificar los aspectos establecidos en las bases del concurso respecto a los méritos estudiantiles en el lapso de 2 días posteriores a su designación. Posteriormente, de acuerdo al calendario establecido procederá a la recepción de la prueba de oposición, presentando el informe de lo actuado en el lapso de 2 días posteriores a la prueba de oposición.\n\n" +
                           "Artículo 3.- De lo actuado por la Comisión de Selección, se dejará constancia en el acta respectiva, indicando los resultados de la evaluación de méritos y de la prueba de oposición, que será entregada al Decano/a para ser tratado por el Consejo Directivo de la Unidad Académica.\n\n" +
                           "Artículo 4.- El Consejo Académico de la Unidad Académica acogerá y aprobará el informe presentado por la Comisión de Selección; remitiendo lo resuelto al Consejo Académico General, el cual verificará el cumplimiento de todas las actividades indicadas en el presente Reglamento y la evaluación realizada. En caso de que el Consejo Académico General acoja el informe de la Comisión de Selección, trasladará lo resuelto al Consejo Universitario.\n\n";
            Paragraph pNorm1 = new Paragraph(norm1, fontSmall);
            pNorm1.setAlignment(Element.ALIGN_JUSTIFIED);
            p2Cell.addElement(pNorm1);

            Paragraph pBases = new Paragraph();
            pBases.add(new Chunk("Con respecto a las ", fontSmall));
            pBases.add(new Chunk("bases de concurso ", fontSmallBold));
            pBases.add(new Chunk("se informa la siguiente normativa:\n\n", fontSmall));
            pBases.add(new Chunk("Artículo 14. Bases del concurso. - Se calificarán dos aspectos: Méritos estudiantiles y una prueba de oposición.\n", fontSmall));
            pBases.add(new Chunk("1. Los méritos estudiantiles medirán los siguientes aspectos:\n", fontSmall));
            pBases.add(new Chunk("a. Calificación final de la asignatura por la cual postula;\n", fontSmall));
            pBases.add(new Chunk("b. Puntajes obtenidos en cada semestre de estudio;\n", fontSmall));
            pBases.add(new Chunk("c. Experiencia en actividades de colaboración académica;\n", fontSmall));
            pBases.add(new Chunk("d. Participación en eventos relacionados con la materia;\n", fontSmall));
            pBases.add(new Chunk("2. La prueba de oposición medirá la capacidad pedagógica o investigativa del estudiante.\n\n", fontSmall));
            
            pBases.add(new Chunk("Artículo 15. ", fontSmallBold));
            pBases.add(new Chunk("Para concursar como Ayudante de Cátedra e Investigación, los participantes presentarán en la Secretaría de la Unidad Académica los siguientes requisitos:\n", fontSmall));
            pBases.setAlignment(Element.ALIGN_JUSTIFIED);
            p2Cell.addElement(pBases);

            p2Table.addCell(p2Cell);
            document.add(p2Table);

            // --- PAGE 3 ---
            document.newPage();
            document.add(createHeaderTable("3 de 5", fontHeader));
            document.add(new Paragraph(" "));
            
            PdfPTable p3Table = new PdfPTable(1);
            p3Table.setWidthPercentage(100);
            PdfPCell p3Cell = new PdfPCell();
            p3Cell.setPadding(8);

            String reqs = "a. Ser estudiante regular al momento de la convocatoria al concurso y cumplir con la normativa para realización de prácticas pre-profesionales o pasantías.\n\n" +
                          "b. No haber sido sancionado por faltas disciplinarias durante su permanencia en la Universidad, que se incluiría en el certificado que se emitirá desde coordinación de carrera.\n\n" +
                          "c. Poseer calificación sobre el promedio de notas de la asignatura en período de aprobación, siempre superior o al quintil más alto.\n\n" +
                          "d. Poseer un promedio general superior al promedio de la carrera en el periodo anterior al de la fecha del concurso.\n\n" +
                          "e. Demostrar disponibilidad de tiempo para ejercer la ayudantía, mediante la entrega del horario de clases con la inclusión de las 20 horas de ayudantía de cátedra que cumplirá;\n\n" +
                          "f. Contar con el criterio favorable del docente de la cátedra y/o del director del proyecto de investigación;\n\n" +
                          "g. Contar con la certificación del Coordinador (a) de la carrera en función de los literales anteriores.\n\n";
            p3Cell.addElement(new Paragraph(reqs, fontSmall));

            Paragraph pVal = new Paragraph();
            pVal.add(new Chunk("La valoración de méritos se realizará acorde a lo señalado por el Reglamento Vigente, que indica:\n\n", fontSmall));
            pVal.add(new Chunk("Artículo 16. ", fontSmallBold));
            pVal.add(new Chunk("Los méritos serán calificados sobre 20 puntos, obtenidos de la siguiente manera:\n\n", fontSmall));
            pVal.add(new Chunk("a. Calificación final de la asignatura por la cual postula (máximo 10 puntos] calculados de la siguiente manera:\n\n", fontSmall));
            
            pVal.add(new Chunk(">     10.00 puntos, si la calificación de aprobación es de 9.50 a 10 puntos.\n\n", fontSmall));
            pVal.add(new Chunk(">     9.00 puntos, si la calificación de aprobación es de 9.00 a 9.49 puntos\n\n", fontSmall));
            pVal.add(new Chunk(">     8.00 puntos, si la calificación de aprobación es de 8.50 a 8.99 puntos\n\n", fontSmall));
            pVal.add(new Chunk(">     7.00 puntos, si la calificación de aprobación es de 8.00 a 8.49 puntos\n\n", fontSmall));
            
            pVal.add(new Chunk("b. Por puntajes obtenidos en cada semestre de estudio (máximo cuatro puntos) calculados de la siguiente manera:\n\n", fontSmall));
            pVal.add(new Chunk(">     1.00 punto por semestre, si el promedio de aprobación es de 9.50 a 10 puntos.\n\n", fontSmall));
            pVal.add(new Chunk(">     0.70 puntos por semestre, si el promedio de aprobación es de 9.00 a 9.49 puntos\n\n", fontSmall));
            pVal.add(new Chunk(">     0.50 puntos por semestre, si el promedio de aprobación es de 8.50 a 8.99 puntos\n", fontSmall));
            
            p3Cell.addElement(pVal);
            p3Table.addCell(p3Cell);
            document.add(p3Table);

            // --- PAGE 4 ---
            document.newPage();
            document.add(createHeaderTable("4 de 5", fontHeader));
            document.add(new Paragraph(" "));

            PdfPTable p4Table = new PdfPTable(1);
            p4Table.setWidthPercentage(100);
            PdfPCell p4Cell = new PdfPCell();
            p4Cell.setPadding(8);
            
            String reqs4 = ">     0.25 puntos por semestre, si el promedio de aprobación es de 8.00 a 8.49 puntos\n\n" +
                           "c. Por experiencia en actividades de colaboración académica en los últimos dos años, en cátedras relacionadas con la asignatura de postulación (máximo cuatro puntos): dos puntos por cada semestre.\n\n" +
                           "d. Por participación en eventos académicos y/o científicos relacionados con las áreas profesionalizantes de la carrera (máximo dos puntos), calculados de la siguiente manera:\n\n" +
                           ">     1.00 punto por seminarios o cursos de capacitación recibidos en Universidades del Ecuador o del extranjero, dentro de los dos años anteriores a la convocatoria.\n\n" +
                           ">     1.00 punto por participar en alguna representación de la materia (Casa Abierta, póster y ponencias], dentro de los dos años anteriores a la convocatoria.\n\n";
            p4Cell.addElement(new Paragraph(reqs4, fontSmall));

            p4Cell.addElement(new Paragraph("SEGUNDA.- CALIFICACIÓN DE LOS POSTULANTES.- Se procede a dar lectura de las solicitudes con los requisitos presentados por los postulantes para realizar la valoración respectiva acorde a lo establecido en los artículos 15 y 16 de la normativa.\n", fontNormal));
            p4Cell.addElement(new Paragraph("La Comisión en uso de las facultades que le otorga el Reglamento analiza la documentación presentada por el estudiante " + nombrePostulante + " con C.I. " + cedula + " que postula por la asignatura \"" + asignatura + "\" para la carrera de " + carrera + " y procede a registrar el cumplimiento correspondiente en la matriz siguiente:", fontNormal));
            p4Cell.addElement(new Paragraph(" "));

            PdfPTable reqCheckTable = new PdfPTable(new float[]{85, 15});
            reqCheckTable.setWidthPercentage(100);
            String[] reqList = {
                "a) Ser estudiante regular al momento de la convocatoria al concurso y acreditar la aprobación de más del 50% de las asignaturas de la malla curricular.",
                "b) Constar entre las 10 mejores calificaciones obtenidas por el estudiante en la asignatura objeto de concurso",
                "c) Demostrar disponibilidad de tiempo para ejercer la ayudantía;",
                "d) Contar con el criterio favorable del docente de la cátedra y/o del proyecto de investigación;",
                "e) No haber sido sancionado por faltas disciplinarias durante su permanencia en la Universidad"
            };
            for(String reqL : reqList) {
                PdfPCell cRL = new PdfPCell(new Phrase(reqL, fontSmall)); cRL.setPadding(4); reqCheckTable.addCell(cRL);
                PdfPCell cRC = new PdfPCell(new Phrase("Cumple", fontSmall)); cRC.setPadding(4); reqCheckTable.addCell(cRC);
            }
            p4Cell.addElement(reqCheckTable);
            p4Cell.addElement(new Paragraph(" "));

            p4Cell.addElement(new Paragraph("PUNTUACIÓN DE MÉRITOS (Siempre que cumpla todos los requisitos)", fontNormal));
            p4Cell.addElement(new Paragraph(" "));

            PdfPTable scoresTable = new PdfPTable(new float[]{85, 15});
            scoresTable.setWidthPercentage(100);
            
            PdfPCell s1 = new PdfPCell(new Phrase("Calificación final de la asignatura por la cual postula", fontNormal)); s1.setPadding(3); scoresTable.addCell(s1);
            PdfPCell v1 = createRightCell(nAsignatura.toString(), fontNormal); v1.setPadding(3); scoresTable.addCell(v1);

            PdfPCell s2 = new PdfPCell(new Phrase("Puntajes obtenidos por cada semestre de estudio.", fontNormal)); s2.setPadding(3); scoresTable.addCell(s2);
            PdfPCell v2 = createRightCell(nSemestres.toString(), fontNormal); v2.setPadding(3); scoresTable.addCell(v2);

            PdfPCell s3 = new PdfPCell(new Phrase("Experiencia en actividades de colaboración académica en los últimos dos años", fontNormal)); s3.setPadding(3); scoresTable.addCell(s3);
            PdfPCell v3 = createRightCell(nExperiencia.toString(), fontNormal); v3.setPadding(3); scoresTable.addCell(v3);

            PdfPCell s4 = new PdfPCell(new Phrase("Participación en eventos relacionados con la asignatura.", fontNormal)); s4.setPadding(3); scoresTable.addCell(s4);
            PdfPCell v4 = createRightCell(nEventos.toString(), fontNormal); v4.setPadding(3); scoresTable.addCell(v4);

            PdfPCell sTotal = new PdfPCell(new Phrase("TOTAL", fontNormal)); sTotal.setPadding(3); sTotal.setHorizontalAlignment(Element.ALIGN_CENTER); scoresTable.addCell(sTotal);
            PdfPCell vTotal = createRightCell(totalMeritos.toString(), fontNormal); vTotal.setPadding(3); scoresTable.addCell(vTotal);
            
            p4Cell.addElement(scoresTable);
            p4Cell.addElement(new Paragraph(" "));

            p4Cell.addElement(new Paragraph("Las calificaciones en la calificación de méritos del estudiante " + nombrePostulante + " con C.I. " + cedula + " alcanza un total de " + totalMeritos + " puntos.\n", fontNormal));
            p4Cell.addElement(new Paragraph("Concomitante a ello se procede con el sorteo de temas para rendir la Prueba de Oposición.\nMediante el sorteo se determina que debe presentar una exposición sobre: \"" + temaSorteado + "\"", fontNormal));

            p4Table.addCell(p4Cell);
            document.add(p4Table);

            // --- PAGE 5 ---
            document.newPage();
            document.add(createHeaderTable("5 de 5", fontHeader));
            document.add(new Paragraph(" "));

            PdfPTable p5Table = new PdfPTable(1);
            p5Table.setWidthPercentage(100);
            PdfPCell p5Cell = new PdfPCell();
            p5Cell.setPadding(8);

            Paragraph pResH = new Paragraph("TERCERA.- RESOLUCIONES. LA COMISIÓN RESUELVE:\n\n", fontBold);
            p5Cell.addElement(pResH);

            Paragraph pResFirst = new Paragraph();
            pResFirst.add(new Chunk("Primera: ", fontBold));
            pResFirst.add(new Chunk("Dejar constancia de lo actuado en el Acta de la Sesión y remitirla a la Señora Subdecana/Decano y por su intermedio al Consejo Directivo de la Facultad de Ciencias de la Ingeniería para su conocimiento y aprobación.\n\n", fontNormal));
            p5Cell.addElement(pResFirst);

            Paragraph pResSec = new Paragraph();
            pResSec.add(new Chunk("Segunda: ", fontBold));
            pResSec.add(new Chunk("Notificar mediante el correo electrónico al postulante el resultado de su valoración de méritos, con la finalidad que se preparen para la prueba de Oposición programada, la misma se realizará de manera virtual. Se les recuerda que en base al Artículo 23.-La prueba de oposición será pública y se calificará sobre un máximo de 20 puntos. Cada participante realizará su exposición en un tiempo máximo de 20 minutos. Una vez concluida la disertación, los miembros de la Comisión de Selección realizará preguntas sobre el tema, por un tiempo de 10 minutos.\n\n", fontNormal));
            pResSec.add(new Chunk("Artículo 24.-La prueba de oposición será evaluada por cada integrante de la Comisión de Selección de la siguiente manera:\n", fontNormal));
            pResSec.add(new Chunk("a.    Por el desarrollo del material impreso, digital y didáctico, 10 puntos;\n", fontNormal));
            pResSec.add(new Chunk("b.    Calidad de la exposición, 4 puntos;\n", fontNormal));
            pResSec.add(new Chunk("c.    Pertinencia de respuestas a las preguntas de la Comisión, 6 puntos.\n\n", fontNormal));
            pResSec.add(new Chunk("Una vez concluida la evaluación individual de cada miembro de la Comisión de Selección, se procederá a obtener el promedio general de la oposición.\n", fontNormal));
            p5Cell.addElement(pResSec);

            p5Table.addCell(p5Cell);
            document.add(p5Table);

            PdfPTable clausuraTable2 = new PdfPTable(1);
            clausuraTable2.setWidthPercentage(100);
            Phrase pClaus2 = new Phrase();
            pClaus2.add(new Chunk("CUARTO CLAUSURA.- ", fontBold));
            pClaus2.add(new Chunk("Sin tener otro punto que tratar la Comisión de Selección concluye la sesión siendo las " + horaFin + ".", fontNormal));
            PdfPCell cClaus2 = new PdfPCell(pClaus2);
            cClaus2.setPadding(10);
            clausuraTable2.addCell(cClaus2);            
            document.add(clausuraTable2);

            document.close();
            return baos.toByteArray();
        } catch (DocumentException | java.io.IOException e) {
            throw new RuntimeException("Error generating Acta Meritos PDF", e);
        }
    }

    private PdfPTable createHeaderTable(String hojasText, Font fontHeader) throws DocumentException {
        PdfPTable headerTable = new PdfPTable(new float[]{80, 20});
        headerTable.setWidthPercentage(100);
        PdfPCell cellUniv = new PdfPCell(new Phrase("UNIVERSIDAD TÉCNICA ESTATAL DE QUEVEDO\nFACULTAD DE CIENCIAS DE LA INGENIERÍA\nACTA DE REUNIÓN", fontHeader));
        cellUniv.setHorizontalAlignment(Element.ALIGN_CENTER);
        cellUniv.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cellUniv.setPadding(10);
        headerTable.addCell(cellUniv);
        PdfPCell cellHojas = new PdfPCell(new Phrase("Hojas:\n" + hojasText, fontHeader));
        cellHojas.setHorizontalAlignment(Element.ALIGN_CENTER);
        cellHojas.setVerticalAlignment(Element.ALIGN_MIDDLE);
        headerTable.addCell(cellHojas);
        return headerTable;
    }

    @Override
    public byte[] generarActaOposicion(Postulacion postulacion) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document document = new Document();
            PdfWriter writer = PdfWriter.getInstance(document, baos);
            document.open();

            // Fonts
            java.awt.Color darkGreen = new java.awt.Color(55, 96, 28);
            Font fontHeader = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, darkGreen);
            Font fontBold = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);
            Font fontNormal = FontFactory.getFont(FontFactory.HELVETICA, 10);

            // Data Extraction
            String carrera = postulacion.getConvocatoria().getAsignatura().getCarrera() != null 
                ? postulacion.getConvocatoria().getAsignatura().getCarrera().getNombreCarrera() : "CARRERA NO ESPECIFICADA";
            String asignatura = postulacion.getConvocatoria().getAsignatura().getNombreAsignatura();
            String nombrePostulante = postulacion.getEstudiante().getUsuario().getNombres() + " " + postulacion.getEstudiante().getUsuario().getApellidos();
            String cedula = postulacion.getEstudiante().getUsuario().getCedula();
            
            LocalDate now = LocalDate.now();
            String fechaActualStr = now.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            String mesTexto = now.getMonth().getDisplayName(java.time.format.TextStyle.FULL, new Locale("es", "ES"));
            String dia = String.valueOf(now.getDayOfMonth());
            String anio = String.valueOf(now.getYear());
            String horaInicio = "08H00"; // Default static as per template
            String horaFin = "09H30";
            String ciudad = "Quevedo";

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

            ResumenEvaluacion resumen = resumenRepo.findByIdPostulacion(postulacion.getIdPostulacion()).orElse(null);
            BigDecimal totalMeritos = resumen != null && resumen.getTotalMeritos() != null ? resumen.getTotalMeritos() : BigDecimal.ZERO;
            BigDecimal promedioOposicion = resumen != null && resumen.getPromedioOposicion() != null ? resumen.getPromedioOposicion() : BigDecimal.ZERO;
            BigDecimal totalFinal = resumen != null && resumen.getTotalFinal() != null ? resumen.getTotalFinal() : BigDecimal.ZERO;

            String temaSorteado = sorteoRepo.findByIdPostulacion(postulacion.getIdPostulacion())
                .map(SorteoOposicion::getTemaSorteado)
                .orElseGet(() -> evOposicionRepo.findByPostulacion_IdPostulacion(postulacion.getIdPostulacion())
                        .map(org.uteq.sgacfinal.entity.EvaluacionOposicion::getTemaExposicion)
                        .orElse("Pendiente de sorteo"));

            // --- PAGE 1 ---
            // 1. Header Table
            PdfPTable headerTable = new PdfPTable(new float[]{80, 20});
            headerTable.setWidthPercentage(100);
            
            PdfPCell cellUniv = new PdfPCell(new Phrase("UNIVERSIDAD TÉCNICA ESTATAL DE QUEVEDO\nFACULTAD DE CIENCIAS DE LA INGENIERÍA\nACTA DE REUNIÓN", fontHeader));
            cellUniv.setHorizontalAlignment(Element.ALIGN_CENTER);
            cellUniv.setVerticalAlignment(Element.ALIGN_MIDDLE);
            cellUniv.setPadding(10);
            headerTable.addCell(cellUniv);
            
            PdfPCell cellHojas = new PdfPCell(new Phrase("Hojas:\n1 de 3", fontHeader));
            cellHojas.setHorizontalAlignment(Element.ALIGN_CENTER);
            cellHojas.setVerticalAlignment(Element.ALIGN_MIDDLE);
            headerTable.addCell(cellHojas);
            
            document.add(headerTable);
            document.add(new Paragraph(" "));

            // 2. Title Box
            PdfPTable titleTable = new PdfPTable(1);
            titleTable.setWidthPercentage(100);
            PdfPCell titleCell = new PdfPCell(new Phrase("DE: COMISIÓN DE SELECCIÓN AYUDANTÍAS DE CÁTEDRA CARRERA " + carrera.toUpperCase() + ".", fontBold));
            titleCell.setPadding(8);
            titleTable.addCell(titleCell);
            document.add(titleTable);
            document.add(new Paragraph(" "));

            // 3. Info Table
            PdfPTable infoTable = new PdfPTable(new float[]{25, 45, 15, 15});
            infoTable.setWidthPercentage(100);
            
            PdfPCell cLugar = new PdfPCell(new Phrase("Lugar:", fontBold)); cLugar.setPadding(5); infoTable.addCell(cLugar);
            PdfPCell cPlat = new PdfPCell(new Phrase("Plataforma Google.", fontBold)); cPlat.setPadding(5); infoTable.addCell(cPlat);
            PdfPCell cActaNo = new PdfPCell(new Phrase("Acta Nº:", fontBold)); cActaNo.setPadding(5); cActaNo.setRowspan(3); cActaNo.setVerticalAlignment(Element.ALIGN_MIDDLE); infoTable.addCell(cActaNo);
            PdfPCell cActaNum = new PdfPCell(new Phrase(String.format("%02d", postulacion.getIdPostulacion()), fontNormal)); cActaNum.setPadding(5); cActaNum.setRowspan(3); cActaNum.setVerticalAlignment(Element.ALIGN_MIDDLE); infoTable.addCell(cActaNum);

            PdfPCell cFechaL = new PdfPCell(new Phrase("Fecha :", fontBold)); cFechaL.setPadding(5); infoTable.addCell(cFechaL);
            PdfPCell cFechaV = new PdfPCell(new Phrase(fechaActualStr, fontBold)); cFechaV.setPadding(5); infoTable.addCell(cFechaV);

            PdfPCell cHoraL = new PdfPCell(new Phrase("Hora inicio/ fin:", fontBold)); cHoraL.setPadding(5); infoTable.addCell(cHoraL);
            PdfPCell cHoraV = new PdfPCell(new Phrase(horaInicio + " / " + horaFin, fontBold)); cHoraV.setPadding(5); infoTable.addCell(cHoraV);

            document.add(infoTable);
            document.add(new Paragraph(" "));

            // 4. Asistentes Table
            PdfPTable asisTable = new PdfPTable(new float[]{35, 45, 20});
            asisTable.setWidthPercentage(100);
            
            PdfPCell cAsisH = new PdfPCell(new Phrase("Asistentes:", fontBold)); cAsisH.setColspan(3); cAsisH.setPadding(5); asisTable.addCell(cAsisH);
            PdfPCell cAsisN = new PdfPCell(new Phrase("Nombre", fontBold)); cAsisN.setHorizontalAlignment(Element.ALIGN_CENTER); cAsisN.setPadding(5); asisTable.addCell(cAsisN);
            PdfPCell cAsisC = new PdfPCell(new Phrase("Cargo", fontBold)); cAsisC.setHorizontalAlignment(Element.ALIGN_CENTER); cAsisC.setPadding(5); asisTable.addCell(cAsisC);
            PdfPCell cAsisF = new PdfPCell(new Phrase("Firmas", fontBold)); cAsisF.setHorizontalAlignment(Element.ALIGN_CENTER); cAsisF.setPadding(5); asisTable.addCell(cAsisF);

            PdfPCell pDecano = new PdfPCell(new Phrase("Ing. " + decano, fontNormal)); pDecano.setPadding(5); asisTable.addCell(pDecano);
            PdfPCell pDecanoC = new PdfPCell(new Phrase("Subdecana/Decano de la Facultad", fontNormal)); pDecanoC.setPadding(5); asisTable.addCell(pDecanoC);
            asisTable.addCell(new PdfPCell(new Phrase(" ")));

            PdfPCell pCoord = new PdfPCell(new Phrase("Ing. " + coordinador, fontNormal)); pCoord.setPadding(5); asisTable.addCell(pCoord);
            PdfPCell pCoordC = new PdfPCell(new Phrase("Coordinador de Carrera", fontNormal)); pCoordC.setPadding(5); asisTable.addCell(pCoordC);
            asisTable.addCell(new PdfPCell(new Phrase(" ")));

            PdfPCell pDoc = new PdfPCell(new Phrase("Ing. " + docente, fontNormal)); pDoc.setPadding(5); asisTable.addCell(pDoc);
            PdfPCell pDocC = new PdfPCell(new Phrase("Profesor – Secretario Comisión en la asignatura \"" + asignatura + "\"", fontNormal)); pDocC.setPadding(5); asisTable.addCell(pDocC);
            asisTable.addCell(new PdfPCell(new Phrase(" ")));

            document.add(asisTable);
            document.add(new Paragraph(" "));

            // 5. Orden del dia Table
            PdfPTable ordenTable = new PdfPTable(1);
            ordenTable.setWidthPercentage(100);
            PdfPCell cOrdenH = new PdfPCell(new Phrase("Orden del día:", fontBold)); cOrdenH.setPadding(5); ordenTable.addCell(cOrdenH);
            
            String ordenText = "En la Ciudad de " + ciudad + ", el " + dia + " de " + mesTexto + " del " + anio + ", siendo las " + horaInicio + " se instala la Comisión de Selección de ayudante de cátedra, para realizar el proceso de Oposición de postulantes en la asignatura \"" + asignatura + "\". La sesión se realiza por medio de videoconferencia, la misma es grabada como evidencia del proceso de selección.\n\n" +
                               "Preside la sesión la Ing. " + decano + ", Subdecana/Decano de la Facultad de Ciencias de la Ingeniería, quien da inicio a la sesión conformada además por el Ing. " + coordinador + ", Coordinador de la Carrera; y el Ing. " + docente + " profesor de la Carrera quien cumplirá la función de Secretario de la Comisión.\n\n" +
                               "La Subdecana pone a consideración el siguiente orden del día:\n" +
                               "1. Lectura de Normativa del Concurso.\n" +
                               "2.-Calificación de postulantes.\n" +
                               "3. Resoluciones.\n" +
                               "4.- Clausura.\n" +
                               "El orden del día propuesto es aprobado por los integrantes de la Comisión.";

            PdfPCell cOrdenB = new PdfPCell(new Phrase(ordenText, fontNormal)); cOrdenB.setPadding(8); ordenTable.addCell(cOrdenB);
            document.add(ordenTable);
            
            // --- PAGE 2 ---
            document.newPage();

            PdfPTable headerTable2 = new PdfPTable(new float[]{80, 20});
            headerTable2.setWidthPercentage(100);
            PdfPCell cellUniv2 = new PdfPCell(new Phrase("UNIVERSIDAD TÉCNICA ESTATAL DE QUEVEDO\nACTA DE REUNIONES", fontBold));
            cellUniv2.setHorizontalAlignment(Element.ALIGN_CENTER);
            cellUniv2.setVerticalAlignment(Element.ALIGN_MIDDLE);
            cellUniv2.setPadding(10);
            headerTable2.addCell(cellUniv2);
            PdfPCell cellHojas2 = new PdfPCell(new Phrase("Hojas:\n2 de 3", fontBold));
            cellHojas2.setHorizontalAlignment(Element.ALIGN_CENTER);
            cellHojas2.setVerticalAlignment(Element.ALIGN_MIDDLE);
            headerTable2.addCell(cellHojas2);
            document.add(headerTable2);
            document.add(new Paragraph(" "));

            // Desarrollo Table
            PdfPTable desarrolloTable = new PdfPTable(1);
            desarrolloTable.setWidthPercentage(100);
            PdfPCell cDesH = new PdfPCell(new Phrase("Desarrollo:", fontBold)); cDesH.setPadding(5); desarrolloTable.addCell(cDesH);
            
            Phrase pDesarrollo = new Phrase();
            pDesarrollo.add(new Chunk("PRIMERA. - LECTURA DE NORMATIVA DEL CONCURSO. - ", fontBold));
            pDesarrollo.add(new Chunk("Una vez confirmada la asistencia de los integrantes de la Comisión y del postulante, la Subdecana procede a dar lectura a los artículos de la normativa que hacen referencia a la facultad que se otorga a la Comisión designada para el Concurso de Ayudantes de Cátedra, así como también a la base del concurso por constituirse en el fundamento del proceso de selección en la segunda fase del mismo.\n", fontNormal));
            pDesarrollo.add(new Chunk("En base al Artículo 18, ", fontBold));
            pDesarrollo.add(new Chunk("La prueba de oposición será pública y se calificará sobre un máximo de 20 puntos. El orden de participación de los estudiantes será según el orden de inscripción. Cada participante realizará su exposición en un tiempo máximo de 20 minutos. Una vez concluida la disertación, los miembros de la Comisión de Selección realizarán preguntas sobre el tema, por un tiempo de 10 minutos.\n\n", fontNormal));
            pDesarrollo.add(new Chunk("Artículo 19.-", fontBold));
            pDesarrollo.add(new Chunk("La prueba de oposición será evaluada por cada integrante de la Comisión de Selección de la siguiente manera:\n", fontNormal));
            pDesarrollo.add(new Chunk("a. Por el desarrollo del material impreso, digital y didáctico, 10 puntos (Word-power point)\n", fontNormal));
            pDesarrollo.add(new Chunk("b. Calidad de la exposición, 4 puntos;\n", fontNormal));
            pDesarrollo.add(new Chunk("c. Pertinencia de respuestas a las preguntas de la Comisión, 6 puntos.\n\n", fontNormal));
            pDesarrollo.add(new Chunk("Una vez concluida la evaluación individual de cada miembro de la Comisión de Selección, se procederá a obtener el promedio general de la oposición.\n\n", fontNormal));
            
            pDesarrollo.add(new Chunk("SEGUNDA. - Calificación de postulantes.\n\n", fontBold));
            pDesarrollo.add(new Chunk("La Subdecana expone a los integrantes de la Comisión que en la sesión anterior se estableció considerar a los postulantes de Ayudante de Cátedra de la asignatura \"" + asignatura + "\" entre los cuáles consta la estudiante " + nombrePostulante.toUpperCase() + ", quien en la valoración de méritos alcanzó el puntaje de " + totalMeritos + ". Además, se estableció el Tema a exponer sea: \"" + temaSorteado + "\"\n\n", fontNormal));
            pDesarrollo.add(new Chunk("Acto seguido se da paso a la exposición del estudiante, el cual se acogió a una exposición de 20 minutos. Luego de lo cual se inició la ronda de preguntas sobre la exposición, las cuales fueron respondidas por el postulante.\n\n", fontNormal));
            pDesarrollo.add(new Chunk("Acorde al procedimiento establecido, se calculó el promedio de la calificación del proceso de selección de Ayudante de Cátedra según el siguiente cuadro:\n", fontNormal));

            PdfPCell cDesBody = new PdfPCell(pDesarrollo); cDesBody.setPadding(8); desarrolloTable.addCell(cDesBody);
            document.add(desarrolloTable);

            // Scores Fetch
            List<CalificacionOposicionIndividual> calificaciones = oposicionRepo.findByIdPostulacion(postulacion.getIdPostulacion());
            BigDecimal decanoMat = BigDecimal.ZERO, decanoCal = BigDecimal.ZERO, decanoPert = BigDecimal.ZERO;
            BigDecimal coordMat = BigDecimal.ZERO, coordCal = BigDecimal.ZERO, coordPert = BigDecimal.ZERO;
            BigDecimal docenteMat = BigDecimal.ZERO, docenteCal = BigDecimal.ZERO, docentePert = BigDecimal.ZERO;
            
            for(CalificacionOposicionIndividual c : calificaciones) {
                if ("DECANO".equals(c.getRolEvaluador())) {
                    decanoMat = c.getCriterioMaterial() != null ? c.getCriterioMaterial() : BigDecimal.ZERO;
                    decanoCal = c.getCriterioCalidad() != null ? c.getCriterioCalidad() : BigDecimal.ZERO;
                    decanoPert = c.getCriterioPertinencia() != null ? c.getCriterioPertinencia() : BigDecimal.ZERO;
                } else if ("COORDINADOR".equals(c.getRolEvaluador())) {
                    coordMat = c.getCriterioMaterial() != null ? c.getCriterioMaterial() : BigDecimal.ZERO;
                    coordCal = c.getCriterioCalidad() != null ? c.getCriterioCalidad() : BigDecimal.ZERO;
                    coordPert = c.getCriterioPertinencia() != null ? c.getCriterioPertinencia() : BigDecimal.ZERO;
                } else if ("DOCENTE".equals(c.getRolEvaluador())) {
                    docenteMat = c.getCriterioMaterial() != null ? c.getCriterioMaterial() : BigDecimal.ZERO;
                    docenteCal = c.getCriterioCalidad() != null ? c.getCriterioCalidad() : BigDecimal.ZERO;
                    docentePert = c.getCriterioPertinencia() != null ? c.getCriterioPertinencia() : BigDecimal.ZERO;
                }
            }

            BigDecimal sumaMat = decanoMat.add(coordMat).add(docenteMat);
            BigDecimal sumaCal = decanoCal.add(coordCal).add(docenteCal);
            BigDecimal sumaPert = decanoPert.add(coordPert).add(docentePert);

            // Notas Table (nested inside the same border if we want, or separate)
            PdfPTable notasTable = new PdfPTable(new float[]{32, 17, 17, 17, 17});
            notasTable.setWidthPercentage(100);
            // Header
            notasTable.addCell(new PdfPCell(new Phrase("Ítem a Evaluar", fontNormal)));
            notasTable.addCell(new PdfPCell(new Phrase("Ing. " + decano.split(" ")[0], fontNormal)));
            notasTable.addCell(new PdfPCell(new Phrase("Ing. " + coordinador.split(" ")[0], fontNormal)));
            notasTable.addCell(new PdfPCell(new Phrase("Ing. " + docente.split(" ")[0], fontNormal)));
            notasTable.addCell(new PdfPCell(new Phrase("SUMA", fontNormal)));

            // Row a
            notasTable.addCell(new PdfPCell(new Phrase("a. Por el desarrollo del material impreso, digital y didáctico, 10 puntos", fontNormal)));
            notasTable.addCell(createRightCell(decanoMat.toString(), fontNormal));
            notasTable.addCell(createRightCell(coordMat.toString(), fontNormal));
            notasTable.addCell(createRightCell(docenteMat.toString(), fontNormal));
            notasTable.addCell(createRightCell(sumaMat.toString(), fontNormal));

            // Row b
            notasTable.addCell(new PdfPCell(new Phrase("b. Calidad de la exposición, 4 puntos;", fontNormal)));
            notasTable.addCell(createRightCell(decanoCal.toString(), fontNormal));
            notasTable.addCell(createRightCell(coordCal.toString(), fontNormal));
            notasTable.addCell(createRightCell(docenteCal.toString(), fontNormal));
            notasTable.addCell(createRightCell(sumaCal.toString(), fontNormal));

            // Row c
            notasTable.addCell(new PdfPCell(new Phrase("c. Pertinencia de respuestas a las preguntas de la Comisión, 6 puntos", fontNormal)));
            notasTable.addCell(createRightCell(decanoPert.toString(), fontNormal));
            notasTable.addCell(createRightCell(coordPert.toString(), fontNormal));
            notasTable.addCell(createRightCell(docentePert.toString(), fontNormal));
            notasTable.addCell(createRightCell(sumaPert.toString(), fontNormal));

            // Footer
            PdfPCell cPromLbl = new PdfPCell(new Phrase("PROMEDIO", fontBold));
            cPromLbl.setColspan(4);
            cPromLbl.setHorizontalAlignment(Element.ALIGN_RIGHT);
            notasTable.addCell(cPromLbl);
            PdfPCell cPromVal = new PdfPCell(new Phrase(promedioOposicion.toString(), fontBold));
            cPromVal.setHorizontalAlignment(Element.ALIGN_RIGHT);
            notasTable.addCell(cPromVal);

            document.add(notasTable);
            
            // --- PAGE 3 ---
            document.newPage();
            
            PdfPTable resolucionOuterTable = new PdfPTable(1);
            resolucionOuterTable.setWidthPercentage(100);

            Phrase pRes = new Phrase();
            pRes.add(new Chunk("TERCERA. -RESOLUCIONES. LA COMISIÓN RESUELVE:\n\n", fontBold));
            pRes.add(new Chunk("Primera: ", fontBold));
            pRes.add(new Chunk("Acorde al procedimiento establecido, considerando las calificaciones de méritos y oposición, se establece como promedio general del postulante según se detalla a continuación:\n\n", fontNormal));
            
            PdfPCell cResTop = new PdfPCell(pRes);
            cResTop.setBorder(Rectangle.LEFT | Rectangle.RIGHT | Rectangle.TOP);
            cResTop.setPadding(8);
            resolucionOuterTable.addCell(cResTop);

            // Inner mini table for final scores
            PdfPTable finalScoresTable = new PdfPTable(new float[]{60, 40});
            finalScoresTable.setWidthPercentage(40);
            finalScoresTable.setHorizontalAlignment(Element.ALIGN_CENTER);
            
            finalScoresTable.addCell(new PdfPCell(new Phrase("Calificación de Méritos", fontNormal)));
            finalScoresTable.addCell(createCenterCell(totalMeritos.toString(), fontNormal));
            finalScoresTable.addCell(new PdfPCell(new Phrase("Calificación de Oposición", fontNormal)));
            finalScoresTable.addCell(createCenterCell(promedioOposicion.toString(), fontNormal));
            finalScoresTable.addCell(new PdfPCell(new Phrase("Calificación Total", fontBold)));
            finalScoresTable.addCell(createCenterCell(totalFinal.toString(), fontBold));

            PdfPCell cResMid = new PdfPCell(finalScoresTable);
            cResMid.setBorder(Rectangle.LEFT | Rectangle.RIGHT);
            cResMid.setPaddingBottom(15);
            resolucionOuterTable.addCell(cResMid);

            Phrase pRes2 = new Phrase();
            pRes2.add(new Chunk("Concomitante a lo anterior, se resuelve designar al estudiante, " + nombrePostulante.toUpperCase() + " con Cédula de Ciudadanía " + cedula + ", como ganador/participante del concurso de Ayudante de Cátedra de la Carrera de " + carrera + " en la Asignatura \"" + asignatura + "\" con una calificación final de " + totalFinal + " puntos.\n\n", fontNormal));
            pRes2.add(new Chunk("Segunda: ", fontBold));
            pRes2.add(new Chunk("Dejar constancia de lo actuado en el Acta de la Sesión y remitir al Consejo Directivo de la Facultad de Ciencias de la Ingeniería para su conocimiento y aprobación.\n\n", fontNormal));

            PdfPCell cResBot = new PdfPCell(pRes2);
            cResBot.setBorder(Rectangle.LEFT | Rectangle.RIGHT | Rectangle.BOTTOM);
            cResBot.setPadding(8);
            resolucionOuterTable.addCell(cResBot);

            document.add(resolucionOuterTable);
            document.add(new Paragraph(" "));

            // Clausura inside a box
            PdfPTable clausuraTable = new PdfPTable(1);
            clausuraTable.setWidthPercentage(100);
            Phrase pClaus = new Phrase();
            pClaus.add(new Chunk("CUARTA CLAUSURA.- ", fontBold));
            pClaus.add(new Chunk("Sin tener otro punto que tratar la Comisión de Selección concluye la sesión a las " + horaFin + ".", fontNormal));
            PdfPCell cClaus = new PdfPCell(pClaus);
            cClaus.setPadding(10);
            clausuraTable.addCell(cClaus);
            
            document.add(clausuraTable);

            document.close();
            return baos.toByteArray();
        } catch (DocumentException | java.io.IOException e) {
            throw new RuntimeException("Error generating Acta Oposicion PDF", e);
        }
    }

    private PdfPCell createRightCell(String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        return cell;
    }

    private PdfPCell createCenterCell(String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        return cell;
    }
}
