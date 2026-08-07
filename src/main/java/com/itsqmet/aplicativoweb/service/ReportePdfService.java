package com.itsqmet.aplicativoweb.service;

import com.itsqmet.aplicativoweb.dto.BoletaDtos;
import com.itsqmet.aplicativoweb.model.Alumno;
import com.itsqmet.aplicativoweb.model.PeriodoAcademico;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.Image;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

/**
 * Convierte los DTOs que ya arma BoletaService (informe de progreso por
 * trimestre e informe final anual) a un PDF. Se usa OpenPDF (fork libre de
 * iText) porque es la opción más directa para tablas + texto sin agregar
 * plantillas HTML ni un motor de render aparte.
 */
@Service
public class ReportePdfService {

    // OpenPDF NO detecta la codificación del texto que le pasas: "new
    // Font(Font.HELVETICA, ...)" sin más produce un BaseFont con encoding
    // "Cp1252" por defecto, pero según cómo llegue el String (aquí, valores
    // ya en UTF-16 desde JPA) el resultado salía en el PDF como "Ã¡" en vez
    // de "á" -- clásico mojibake de bytes UTF-8 reinterpretados como
    // Cp1252/Latin-1. Se construye el BaseFont explícitamente con
    // BaseFont.CP1252 (cubre á/é/í/ó/ú/ñ/¿/¡, todo lo que necesita este
    // reporte en español) para que la codificación quede inequívoca.
    private static final BaseFont BASE_FONT = crearBaseFont();

    private static BaseFont crearBaseFont() {
        try {
            return BaseFont.createFont(BaseFont.HELVETICA, BaseFont.CP1252, BaseFont.NOT_EMBEDDED);
        } catch (Exception e) {
            throw new RuntimeException("No se pudo cargar la fuente del PDF", e);
        }
    }

    private static final Font TITULO = new Font(BASE_FONT, 16, Font.BOLD);
    private static final Font SUBTITULO = new Font(BASE_FONT, 11, Font.BOLD);
    private static final Font NORMAL = new Font(BASE_FONT, 10, Font.NORMAL);
    private static final Font ENCABEZADO_TABLA = new Font(BASE_FONT, 9, Font.BOLD);

    // Se lee una sola vez a memoria (logo de la escuela, src/main/resources) y
    // se crea una Image nueva por PDF a partir de esos bytes -- una misma
    // instancia de Image no se reutiliza de forma segura entre documentos.
    private static final byte[] LOGO_BYTES = cargarLogo();

    private static byte[] cargarLogo() {
        try {
            return new ClassPathResource("logoescuela.png").getInputStream().readAllBytes();
        } catch (Exception e) {
            throw new RuntimeException("No se pudo cargar el logo del reporte", e);
        }
    }

    public byte[] generarPdfProgreso(BoletaDtos.InformeProgresoDto dto, Alumno alumno, PeriodoAcademico periodo) {
        boolean tieneDetalleMaterias = !dto.materiasDetalle().isEmpty();
        Document documento = new Document(
                tieneDetalleMaterias ? PageSize.A4.rotate() : PageSize.A4, 40, 40, 50, 40);
        ByteArrayOutputStream salida = new ByteArrayOutputStream();
        try {
            PdfWriter.getInstance(documento, salida);
            documento.open();

            agregarEncabezado(documento, alumno, "Trimestre " + periodo.getNumero());

            if (tieneDetalleMaterias) {
                // Elemental/Media: una tabla por materia con CADA nota del
                // trimestre (no solo el promedio), en horizontal para que
                // entren todas las columnas de actividades.
                for (BoletaDtos.LineaMateriaDetalleDto materia : dto.materiasDetalle()) {
                    agregarMateriaDetalle(documento, materia);
                }
                if (dto.promedioFinal() != null) {
                    Paragraph promedioFinal = new Paragraph(
                            "Promedio final del trimestre: " + dto.promedioFinal(), SUBTITULO);
                    promedioFinal.setSpacingBefore(14);
                    documento.add(promedioFinal);
                }
            } else if (!dto.destrezas().isEmpty()) {
                // Inicial/Preparatoria: 100% cualitativo por destrezas, no
                // aplica un "promedio final" numérico (Art. 6-7 del Acuerdo).
                documento.add(tablaDestrezas(dto.destrezas()));
            } else {
                documento.add(new Paragraph("Todavía no hay calificaciones registradas para este trimestre.", NORMAL));
            }

            if (dto.evaluacionComportamental() != null) {
                Paragraph comportamiento = new Paragraph(
                        "Comportamiento: " + dto.evaluacionComportamental(), NORMAL);
                comportamiento.setSpacingBefore(14);
                documento.add(comportamiento);
            }

            documento.close();
        } catch (Exception e) {
            throw new RuntimeException("No se pudo generar el PDF del reporte", e);
        }
        return salida.toByteArray();
    }

    public byte[] generarPdfAnual(BoletaDtos.InformeFinalAnualDto dto, Alumno alumno) {
        Document documento = new Document(PageSize.A4, 40, 40, 50, 40);
        ByteArrayOutputStream salida = new ByteArrayOutputStream();
        try {
            PdfWriter.getInstance(documento, salida);
            documento.open();

            agregarEncabezado(documento, alumno, "Reporte general");

            // Simplificado a propósito: Materia + los 3 trimestres + promedio
            // final (se quita la columna de supletoria, que en la gran
            // mayoría de los casos queda en "—" y solo agrega ruido aquí).
            PdfPTable tabla = new PdfPTable(new float[]{3, 1, 1, 1, 1.3f});
            tabla.setWidthPercentage(100);
            tabla.setSpacingBefore(10);
            for (String columna : new String[]{"Materia", "T1", "T2", "T3", "Promedio final"}) {
                tabla.addCell(celdaEncabezado(columna));
            }
            for (BoletaDtos.LineaMateriaAnualDto materia : dto.materias()) {
                tabla.addCell(celda(materia.nombreMateria()));
                List<BigDecimal> promedios = materia.promediosPorPeriodo();
                for (int i = 0; i < 3; i++) {
                    BigDecimal promedio = i < promedios.size() ? promedios.get(i) : null;
                    tabla.addCell(celda(promedio != null ? promedio.toPlainString() : "—"));
                }
                tabla.addCell(celda(materia.promedioFinal() != null ? materia.promedioFinal().toPlainString() : "—"));
            }
            documento.add(tabla);

            Paragraph resumen = new Paragraph(
                    "Promedio final (anual): " + dto.promedioAnualGeneral()
                            + "\nResultado de promoción: " + dto.resultadoPromocion(),
                    SUBTITULO);
            resumen.setSpacingBefore(14);
            documento.add(resumen);

            documento.close();
        } catch (Exception e) {
            throw new RuntimeException("No se pudo generar el PDF del reporte", e);
        }
        return salida.toByteArray();
    }

    private void agregarEncabezado(Document documento, Alumno alumno, String subtitulo) throws Exception {
        PdfPTable encabezado = new PdfPTable(new float[]{1, 4});
        encabezado.setWidthPercentage(100);

        Image logo = Image.getInstance(LOGO_BYTES);
        logo.scaleToFit(65, 65);
        PdfPCell celdaLogo = new PdfPCell(logo, false);
        celdaLogo.setBorder(0);
        celdaLogo.setHorizontalAlignment(Element.ALIGN_CENTER);
        celdaLogo.setVerticalAlignment(Element.ALIGN_MIDDLE);
        encabezado.addCell(celdaLogo);

        PdfPCell celdaTitulo = new PdfPCell();
        celdaTitulo.setBorder(0);
        celdaTitulo.setVerticalAlignment(Element.ALIGN_MIDDLE);
        Paragraph titulo = new Paragraph("Escuela de Educación Básica República de Venezuela", TITULO);
        titulo.setAlignment(Element.ALIGN_CENTER);
        Paragraph sub = new Paragraph(subtitulo, SUBTITULO);
        sub.setAlignment(Element.ALIGN_CENTER);
        celdaTitulo.addElement(titulo);
        celdaTitulo.addElement(sub);
        encabezado.addCell(celdaTitulo);

        documento.add(encabezado);

        Paragraph anioLectivo = new Paragraph(
                "Año lectivo: " + alumno.getCurso().getAnioLectivo().getNombre(), NORMAL);
        anioLectivo.setAlignment(Element.ALIGN_CENTER);
        anioLectivo.setSpacingBefore(2);
        anioLectivo.setSpacingAfter(14);
        documento.add(anioLectivo);

        documento.add(new Paragraph("Estudiante: " + alumno.getNombres() + " " + alumno.getApellidos(), NORMAL));
        documento.add(new Paragraph("Cédula: " + alumno.getCedula(), NORMAL));
        documento.add(new Paragraph(
                "Curso: " + alumno.getCurso().getGrado() + " \"" + alumno.getCurso().getParalelo() + "\"", NORMAL));
    }

    private void agregarMateriaDetalle(Document documento, BoletaDtos.LineaMateriaDetalleDto materia) throws Exception {
        Paragraph nombreMateria = new Paragraph(materia.nombreMateria(), SUBTITULO);
        nombreMateria.setSpacingBefore(14);
        nombreMateria.setSpacingAfter(4);
        documento.add(nombreMateria);

        List<BoletaDtos.LineaActividadDto> actividades = materia.actividades();
        int columnas = actividades.size() + 2; // + Promedio + Equivalencia
        float[] anchos = new float[columnas];
        Arrays.fill(anchos, 1f);
        anchos[columnas - 2] = 1.3f;
        anchos[columnas - 1] = 1.3f;

        PdfPTable tabla = new PdfPTable(anchos);
        tabla.setWidthPercentage(100);
        for (BoletaDtos.LineaActividadDto actividad : actividades) {
            tabla.addCell(celdaEncabezado(actividad.nombre()));
        }
        tabla.addCell(celdaEncabezado("Promedio"));
        tabla.addCell(celdaEncabezado("Equivalencia"));

        for (BoletaDtos.LineaActividadDto actividad : actividades) {
            tabla.addCell(celda(actividad.calificacion() != null ? actividad.calificacion().toPlainString() : "—"));
        }
        tabla.addCell(celda(materia.promedio() != null ? materia.promedio().toPlainString() : "—"));
        tabla.addCell(celda(materia.equivalenciaCualitativa() != null ? materia.equivalenciaCualitativa() : "—"));

        documento.add(tabla);
    }

    private PdfPTable tablaDestrezas(List<BoletaDtos.LineaDestrezaDto> destrezas) {
        PdfPTable tabla = new PdfPTable(new float[]{2, 3, 1});
        tabla.setWidthPercentage(100);
        tabla.setSpacingBefore(10);
        for (String columna : new String[]{"Ámbito de aprendizaje", "Destreza", "Escala"}) {
            tabla.addCell(celdaEncabezado(columna));
        }
        for (BoletaDtos.LineaDestrezaDto destreza : destrezas) {
            tabla.addCell(celda(destreza.ambitoAprendizaje()));
            tabla.addCell(celda(destreza.destreza()));
            tabla.addCell(celda(destreza.escala()));
        }
        return tabla;
    }

    private PdfPCell celdaEncabezado(String texto) {
        PdfPCell celda = new PdfPCell(new Paragraph(texto, ENCABEZADO_TABLA));
        celda.setPadding(6);
        celda.setBackgroundColor(new java.awt.Color(230, 230, 230));
        return celda;
    }

    private PdfPCell celda(String texto) {
        PdfPCell celda = new PdfPCell(new Paragraph(texto, NORMAL));
        celda.setPadding(6);
        return celda;
    }
}
