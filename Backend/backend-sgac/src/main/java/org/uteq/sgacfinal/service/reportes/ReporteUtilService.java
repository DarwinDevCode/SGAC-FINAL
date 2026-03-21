package org.uteq.sgacfinal.service.reportes;

import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.Image;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.*;
import org.apache.poi.xssf.usermodel.DefaultIndexedColorMap;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class ReporteUtilService {
    private static final Color COLOR_HEADER     = new Color(0x1B, 0x5E, 0x20);
    private static final Color COLOR_SUBHEADER  = new Color(0x2E, 0x7D, 0x32);
    private static final Color COLOR_SEL        = new Color(0xE8, 0xF5, 0xE9);
    private static final Color COLOR_ELEG       = new Color(0xE3, 0xF2, 0xFD);
    private static final Color COLOR_BORDE      = new Color(0x90, 0xA4, 0xAE);
    private static final Color COLOR_BLANCO     = Color.WHITE;
    private static final Color COLOR_FILA_PAR   = new Color(0xF5, 0xF5, 0xF5);
    private static final String CAMPO_ESTADO = "estado";

    private static final DateTimeFormatter FMT_FECHA =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter FMT_ARCHIVO =
            DateTimeFormatter.ofPattern("yyyyMMdd_HHmm");

    public enum Orientacion { PORTRAIT, LANDSCAPE }

    public byte[] exportarExcel(String titulo,
                                String[] cabeceras,
                                String[] campos,
                                List<Map<String, Object>> filas) {
        try (XSSFWorkbook wb = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            XSSFSheet sheet = wb.createSheet("Resultados");

            XSSFCellStyle estTitulo  = crearEstiloTitulo(wb);
            XSSFCellStyle estMeta    = crearEstiloMeta(wb);
            XSSFCellStyle estHeader  = crearEstiloHeader(wb);
            XSSFCellStyle estNormal  = crearEstiloNormal(wb, false, null);
            XSSFCellStyle estPar     = crearEstiloNormal(wb, false,
                    new XSSFColor(new byte[]{(byte)0xF5, (byte)0xF5, (byte)0xF5}, new DefaultIndexedColorMap()));
            XSSFCellStyle estSel     = crearEstiloNormal(wb, false,
                    new XSSFColor(new byte[]{(byte)0xE8, (byte)0xF5, (byte)0xE9}, new DefaultIndexedColorMap()));
            XSSFCellStyle estEleg    = crearEstiloNormal(wb, false,
                    new XSSFColor(new byte[]{(byte)0xE3, (byte)0xF2, (byte)0xFD}, new DefaultIndexedColorMap()));
            XSSFCellStyle estNumero  = crearEstiloNumero(wb, null);
            XSSFCellStyle estNumSel  = crearEstiloNumero(wb,
                    new XSSFColor(new byte[]{(byte)0xE8, (byte)0xF5, (byte)0xE9}, new DefaultIndexedColorMap()));
            XSSFCellStyle estNumEleg = crearEstiloNumero(wb,
                    new XSSFColor(new byte[]{(byte)0xE3, (byte)0xF2, (byte)0xFD}, new DefaultIndexedColorMap()));

            int fila = 0;

            /*
            try {
                ClassPathResource logoRes = new ClassPathResource("static/logo-uteq.png");
                if (logoRes.exists()) {
                    byte[] logoBytes = logoRes.getInputStream().readAllBytes();
                    int logoIdx = wb.addPicture(logoBytes, Workbook.PICTURE_TYPE_PNG);
                    Drawing<?> drawing = sheet.createDrawingPatriarch();
                    ClientAnchor anchor = wb.getCreationHelper().createClientAnchor();
                    anchor.setCol1(0); anchor.setRow1(0);
                    anchor.setCol2(2); anchor.setRow2(3);
                    drawing.createPicture(anchor, logoIdx);
                    fila = 3;
                }
            } catch (Exception e) {
                log.debug("[Reporte] Logo UTEQ no encontrado, se omite.");
            }

             */

            Row rowTitulo = sheet.createRow(fila++);
            rowTitulo.setHeightInPoints(32);
            Cell cTit = rowTitulo.createCell(0);
            cTit.setCellValue("SGAC — Universidad Técnica Estatal de Quevedo");
            cTit.setCellStyle(estTitulo);
            sheet.addMergedRegion(new CellRangeAddress(fila - 1, fila - 1, 0, cabeceras.length - 1));

            Row rowSub = sheet.createRow(fila++);
            rowSub.setHeightInPoints(22);
            Cell cSub = rowSub.createCell(0);
            cSub.setCellValue(titulo);
            cSub.setCellStyle(estMeta);
            sheet.addMergedRegion(new CellRangeAddress(fila - 1, fila - 1, 0, cabeceras.length - 1));

            Row rowFecha = sheet.createRow(fila++);
            Cell cFec = rowFecha.createCell(0);
            cFec.setCellValue("Generado: " + LocalDateTime.now().format(FMT_FECHA));
            cFec.setCellStyle(estMeta);
            sheet.addMergedRegion(new CellRangeAddress(fila - 1, fila - 1, 0, cabeceras.length - 1));
            fila++;

            Row rowHeader = sheet.createRow(fila++);
            rowHeader.setHeightInPoints(20);
            for (int c = 0; c < cabeceras.length; c++) {
                Cell cell = rowHeader.createCell(c);
                cell.setCellValue(cabeceras[c]);
                cell.setCellStyle(estHeader);
            }

            sheet.setAutoFilter(new CellRangeAddress(
                    fila - 1, fila - 1, 0, cabeceras.length - 1));

            int dataRow = 0;
            for (Map<String, Object> dato : filas) {
                Row row = sheet.createRow(fila++);
                String estado = dato.containsKey(CAMPO_ESTADO)
                        ? String.valueOf(dato.get(CAMPO_ESTADO)) : "";

                XSSFCellStyle estiloBase  = "SELECCIONADO".equals(estado) ? estSel
                        : "ELEGIBLE".equals(estado) ? estEleg
                        : (dataRow % 2 == 0 ? estNormal : estPar);
                XSSFCellStyle estiloNum   = "SELECCIONADO".equals(estado) ? estNumSel
                        : "ELEGIBLE".equals(estado) ? estNumEleg
                        : estNumero;

                for (int c = 0; c < campos.length; c++) {
                    Cell cell = row.createCell(c);
                    Object val = dato.get(campos[c]);
                    if (val == null) {
                        cell.setCellValue("");
                        cell.setCellStyle(estiloBase);
                    } else if (val instanceof Number num) {
                        cell.setCellValue(num.doubleValue());
                        cell.setCellStyle(estiloNum);
                    } else {
                        cell.setCellValue(String.valueOf(val));
                        cell.setCellStyle(estiloBase);
                    }
                }
                dataRow++;
            }

            for (int c = 0; c < cabeceras.length; c++) {
                sheet.autoSizeColumn(c);
                int ancho = sheet.getColumnWidth(c);
                sheet.setColumnWidth(c, Math.max(ancho, 3500));
            }

            wb.write(out);
            return out.toByteArray();

        } catch (Exception e) {
            log.error("[Reporte] Error generando Excel: {}", e.getMessage(), e);
            throw new RuntimeException("Error al generar el archivo Excel: " + e.getMessage());
        }
    }

    public byte[] exportarPdf(String titulo,
                              String[] cabeceras,
                              String[] campos,
                              List<Map<String, Object>> filas,
                              Orientacion orientacion,
                              String nombreUsuario) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Rectangle pageSize = orientacion == Orientacion.LANDSCAPE
                    ? PageSize.A4.rotate() : PageSize.A4;

            Document doc = new Document(pageSize, 36, 36, 72, 54);
            PdfWriter writer = PdfWriter.getInstance(doc, out);

            writer.setPageEvent(new FooterPdfEvent(nombreUsuario));

            doc.open();

            try {
                ClassPathResource logoRes = new ClassPathResource("static/logo-uteq.png");
                if (logoRes.exists()) {
                    Image logo = Image.getInstance(logoRes.getURL());
                    logo.scaleToFit(80, 40);
                    logo.setAlignment(Element.ALIGN_LEFT);
                    doc.add(logo);
                }
            } catch (Exception e) {
                log.debug("[Reporte] Logo UTEQ no encontrado en PDF.");
            }

            Font fuenteInstitucion = FontFactory.getFont(
                    FontFactory.HELVETICA_BOLD, 13, COLOR_HEADER);
            Font fuenteTitulo = FontFactory.getFont(
                    FontFactory.HELVETICA_BOLD, 11, COLOR_SUBHEADER);
            Font fuenteMeta = FontFactory.getFont(
                    FontFactory.HELVETICA, 9, Color.GRAY);

            Paragraph pInst = new Paragraph(
                    "SGAC — Universidad Técnica Estatal de Quevedo", fuenteInstitucion);
            pInst.setAlignment(Element.ALIGN_CENTER);
            pInst.setSpacingAfter(4);
            doc.add(pInst);

            Paragraph pTit = new Paragraph(titulo, fuenteTitulo);
            pTit.setAlignment(Element.ALIGN_CENTER);
            pTit.setSpacingAfter(4);
            doc.add(pTit);

            Paragraph pFecha = new Paragraph(
                    "Generado: " + LocalDateTime.now().format(FMT_FECHA),
                    fuenteMeta);
            pFecha.setAlignment(Element.ALIGN_CENTER);
            pFecha.setSpacingAfter(12);
            doc.add(pFecha);

            PdfPTable tabla = new PdfPTable(cabeceras.length);
            tabla.setWidthPercentage(100);
            tabla.setSpacingBefore(6);

            float[] anchos = calcularAnchoColumnas(cabeceras);
            tabla.setWidths(anchos);

            Font fCab = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, COLOR_BLANCO);
            for (String cab : cabeceras) {
                PdfPCell cell = new PdfPCell(new Phrase(cab, fCab));
                cell.setBackgroundColor(COLOR_HEADER);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                cell.setPadding(6);
                cell.setBorderColor(COLOR_BORDE);
                tabla.addCell(cell);
            }
            tabla.setHeaderRows(1);

            Font fNormal = FontFactory.getFont(FontFactory.HELVETICA, 8, Color.BLACK);
            Font fBold   = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8, Color.BLACK);
            int dataRow  = 0;

            for (Map<String, Object> dato : filas) {
                String estado = dato.containsKey(CAMPO_ESTADO)
                        ? String.valueOf(dato.get(CAMPO_ESTADO)) : "";

                Color bgFila = "SELECCIONADO".equals(estado) ? COLOR_SEL
                        : "ELEGIBLE".equals(estado)           ? COLOR_ELEG
                        : (dataRow % 2 == 0 ? Color.WHITE : COLOR_FILA_PAR);

                Font fFila = "SELECCIONADO".equals(estado) ? fBold : fNormal;

                for (int c = 0; c < campos.length; c++) {
                    Object val = dato.get(campos[c]);
                    String texto = val == null ? "" : formatearCelda(val);

                    PdfPCell cell = new PdfPCell(new Phrase(texto, fFila));
                    cell.setBackgroundColor(bgFila);
                    cell.setPadding(4);
                    cell.setBorderColor(COLOR_BORDE);
                    cell.setBorderWidth(0.3f);

                    // Alinear números a la derecha
                    if (val instanceof Number) {
                        cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                    } else {
                        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
                    }
                    tabla.addCell(cell);
                }
                dataRow++;
            }

            doc.add(tabla);

            // Resumen al pie
            Paragraph pResumen = new Paragraph(
                    "Total de registros: " + filas.size(), fuenteMeta);
            pResumen.setAlignment(Element.ALIGN_RIGHT);
            pResumen.setSpacingBefore(8);
            doc.add(pResumen);

            doc.close();
            return out.toByteArray();

        } catch (Exception e) {
            log.error("[Reporte] Error generando PDF: {}", e.getMessage(), e);
            throw new RuntimeException("Error al generar el archivo PDF: " + e.getMessage());
        }
    }

    private XSSFCellStyle crearEstiloTitulo(XSSFWorkbook wb) {
        XSSFCellStyle s = wb.createCellStyle();
        XSSFFont f = wb.createFont();
        f.setBold(true);
        f.setFontHeightInPoints((short) 14);
        f.setColor(new XSSFColor(new byte[]{(byte)0x1B, (byte)0x5E, (byte)0x20}, new DefaultIndexedColorMap()));
        s.setFont(f);
        s.setAlignment(HorizontalAlignment.CENTER);
        s.setVerticalAlignment(VerticalAlignment.CENTER);
        return s;
    }

    private XSSFCellStyle crearEstiloMeta(XSSFWorkbook wb) {
        XSSFCellStyle s = wb.createCellStyle();
        XSSFFont f = wb.createFont();
        f.setFontHeightInPoints((short) 10);
        f.setColor(IndexedColors.GREY_50_PERCENT.getIndex());
        s.setFont(f);
        s.setAlignment(HorizontalAlignment.CENTER);
        return s;
    }

    private XSSFCellStyle crearEstiloHeader(XSSFWorkbook wb) {
        XSSFCellStyle s = wb.createCellStyle();
        s.setFillForegroundColor(
                new XSSFColor(new byte[]{(byte)0x1B, (byte)0x5E, (byte)0x20}, new DefaultIndexedColorMap()));
        s.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        XSSFFont f = wb.createFont();
        f.setBold(true);
        f.setColor(IndexedColors.WHITE.getIndex());
        f.setFontHeightInPoints((short) 10);
        s.setFont(f);
        s.setAlignment(HorizontalAlignment.CENTER);
        s.setVerticalAlignment(VerticalAlignment.CENTER);
        s.setBorderBottom(BorderStyle.THIN);
        s.setBorderTop(BorderStyle.THIN);
        s.setBorderLeft(BorderStyle.THIN);
        s.setBorderRight(BorderStyle.THIN);
        return s;
    }

    private XSSFCellStyle crearEstiloNormal(XSSFWorkbook wb, boolean bold, XSSFColor bg) {
        XSSFCellStyle s = wb.createCellStyle();
        if (bg != null) {
            s.setFillForegroundColor(bg);
            s.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        }
        XSSFFont f = wb.createFont();
        f.setBold(bold);
        f.setFontHeightInPoints((short) 10);
        s.setFont(f);
        s.setBorderBottom(BorderStyle.THIN);
        s.setBorderTop(BorderStyle.THIN);
        s.setBorderLeft(BorderStyle.THIN);
        s.setBorderRight(BorderStyle.THIN);
        s.setBottomBorderColor(IndexedColors.GREY_25_PERCENT.getIndex());
        return s;
    }

    private XSSFCellStyle crearEstiloNumero(XSSFWorkbook wb, XSSFColor bg) {
        XSSFCellStyle s = crearEstiloNormal(wb, false, bg);
        s.setDataFormat(wb.createDataFormat().getFormat("0.00"));
        s.setAlignment(HorizontalAlignment.RIGHT);
        return s;
    }

    private float[] calcularAnchoColumnas(String[] cabeceras) {
        float[] anchos = new float[cabeceras.length];
        for (int i = 0; i < cabeceras.length; i++) {
            String cab = cabeceras[i].toLowerCase();
            if (cab.contains("estado"))               anchos[i] = 3.5f;
            else if (cab.contains("total"))           anchos[i] = 2.0f;
            else if (cab.contains("mérito")
                    || cab.contains("meritos")
                    || cab.contains("oposición")
                    || cab.contains("oposicion"))     anchos[i] = 2.5f;
            else if (cab.contains("#")
                    || cab.contains("pos")
                    || cab.contains("sem"))           anchos[i] = 1.2f;
            else if (cab.contains("postulante"))      anchos[i] = 5.0f;
            else if (cab.contains("asignatura"))      anchos[i] = 4.0f;
            else                                      anchos[i] = 3.0f;
        }
        return anchos;
    }

    private String formatearCelda(Object val) {
        if (val instanceof Double d || val instanceof Float) {
            return String.format("%.2f", ((Number) val).doubleValue());
        }
        return String.valueOf(val);
    }

    private static class FooterPdfEvent extends PdfPageEventHelper {
        private final String nombreUsuario;
        private PdfTemplate totalTemplate;
        private BaseFont baseFont;

        FooterPdfEvent(String nombreUsuario) {
            this.nombreUsuario = nombreUsuario != null ? nombreUsuario : "Sistema";
        }

        @Override
        public void onOpenDocument(PdfWriter writer, Document document) {
            totalTemplate = writer.getDirectContent().createTemplate(30, 16);
            try {
                baseFont = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.CP1252, false);
            } catch (Exception e) {
                log.warn("[FooterPdf] No se pudo cargar la fuente del footer.");
            }
        }

        @Override
        public void onEndPage(PdfWriter writer, Document document) {
            if (baseFont == null) return;
            PdfContentByte cb = writer.getDirectContent();
            float x = (document.left() + document.right()) / 2;
            float y = document.bottom() - 20;

            cb.beginText();
            cb.setFontAndSize(baseFont, 8);
            cb.setColorFill(Color.GRAY);

            String textLeft = "Generado por: " + nombreUsuario
                    + "   |   " + LocalDateTime.now().format(
                    DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
            cb.showTextAligned(PdfContentByte.ALIGN_LEFT,
                    textLeft, document.left(), y, 0);

            String pageText = "Página " + writer.getPageNumber() + " de ";
            float textWidth = baseFont.getWidthPoint(pageText, 8);
            cb.showTextAligned(PdfContentByte.ALIGN_RIGHT,
                    pageText, x + textWidth / 2, y, 0);
            cb.endText();

            cb.addTemplate(totalTemplate, x + textWidth / 2, y);
        }

        @Override
        public void onCloseDocument(PdfWriter writer, Document document) {
            if (baseFont == null || totalTemplate == null) return;
            totalTemplate.beginText();
            totalTemplate.setFontAndSize(baseFont, 8);
            totalTemplate.setColorFill(Color.GRAY);
            totalTemplate.showText(String.valueOf(writer.getPageNumber() - 1));
            totalTemplate.endText();
        }
    }
}