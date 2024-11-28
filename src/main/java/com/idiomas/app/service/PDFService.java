package com.idiomas.app.service;

import org.springframework.stereotype.Service;

import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;

import java.io.IOException;

@Service
public class PDFService {

	public void generateHeader(Document document, String studentName, String id, String program, String date) throws DocumentException {
        // Crear una tabla para el encabezado con tres columnas
        PdfPTable headerTable = new PdfPTable(3);
        headerTable.setWidthPercentage(100); // Ancho al 100%
        headerTable.setWidths(new float[]{2, 6, 2}); // Proporción de las columnas

        try {
            // Logo
            Image logo = Image.getInstance("src/main/resources/static/img/Logo.png");
            logo.scaleToFit(80, 50); // Escalar logo
            PdfPCell logoCell = new PdfPCell(logo);
            logoCell.setBorder(Rectangle.NO_BORDER); // Sin bordes
            logoCell.setHorizontalAlignment(Element.ALIGN_LEFT);
            headerTable.addCell(logoCell);
        } catch (IOException e) {
            e.printStackTrace();
            PdfPCell placeholderCell = new PdfPCell(new Phrase("UTS Logo"));
            placeholderCell.setBorder(Rectangle.NO_BORDER);
            placeholderCell.setHorizontalAlignment(Element.ALIGN_LEFT);
            headerTable.addCell(placeholderCell);
        }

        // Texto central
        PdfPCell titleCell = new PdfPCell();
        titleCell.setBorder(Rectangle.NO_BORDER); // Sin bordes
        titleCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        titleCell.setVerticalAlignment(Element.ALIGN_MIDDLE);

        Paragraph title = new Paragraph("DOCENCIA\nMANIFIESTO PARA GRADUACIÓN",
                new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD));
        title.setAlignment(Element.ALIGN_CENTER);
        titleCell.addElement(title);
        headerTable.addCell(titleCell);

        // Información a la derecha
        PdfPCell infoCell = new PdfPCell();
        infoCell.setBorder(Rectangle.NO_BORDER); // Sin bordes
        infoCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        infoCell.setVerticalAlignment(Element.ALIGN_TOP);

        Paragraph info = new Paragraph("F - DC - 104\nPÁGINA: 1 DE 1\nVERSIÓN: 2.0",
                new Font(Font.FontFamily.HELVETICA, 10, Font.NORMAL));
        info.setAlignment(Element.ALIGN_RIGHT);
        infoCell.addElement(info);
        headerTable.addCell(infoCell);

        // Agregar tabla al documento
        document.add(headerTable);

        // Agregar un espaciado después del encabezado
        document.add(Chunk.NEWLINE);
        // Crear una tabla con una columna (1x1)
        PdfPTable shadedTable = new PdfPTable(1);
        shadedTable.setWidthPercentage(100); // Ancho al 100%
        shadedTable.setSpacingBefore(10f); // Espaciado antes de la tabla
        shadedTable.setSpacingAfter(10f); // Espaciado después de la tabla

        // Celda sombreada
        PdfPCell shadedCell = new PdfPCell(new Phrase("170524", 
                new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD, BaseColor.WHITE)));
        shadedCell.setBackgroundColor(BaseColor.GRAY); // Color de fondo gris
        shadedCell.setHorizontalAlignment(Element.ALIGN_CENTER); // Texto centrado
        shadedCell.setVerticalAlignment(Element.ALIGN_MIDDLE); // Verticalmente centrado
        shadedCell.setPadding(10f); // Espaciado interno de la celda
        shadedTable.addCell(shadedCell);

        // Agregar la tabla al documento
        document.add(shadedTable);
        
        // Primera tabla: Responsable del Manifiesto
        PdfPTable table1 = new PdfPTable(2);
        table1.setWidthPercentage(100); // Ancho al 100%
        table1.setWidths(new float[]{1, 3}); // Ancho relativo de columnas
        table1.setSpacingBefore(10f); // Espaciado antes de la tabla
        table1.setSpacingAfter(10f); // Espaciado después de la tabla

        // Celdas de la primera tabla
        PdfPCell cell1_1 = new PdfPCell(new Phrase("Responsable del Manifiesto:", 
                new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD)));
        PdfPCell cell1_2 = new PdfPCell(new Phrase("JESUS VARGAS DÍAZ", 
                new Font(Font.FontFamily.HELVETICA, 12)));
        cell1_1.setPadding(8f);
        cell1_2.setPadding(8f);

        table1.addCell(cell1_1);
        table1.addCell(cell1_2);

        // Agregar la primera tabla al documento
        document.add(table1);

        // Segunda tabla: Área/Proceso/Dependencia
        PdfPTable table2 = new PdfPTable(2);
        table2.setWidthPercentage(100); // Ancho al 100%
        table2.setWidths(new float[]{1, 3}); // Ancho relativo de columnas
        table2.setSpacingBefore(10f); // Espaciado antes de la tabla
        table2.setSpacingAfter(10f); // Espaciado después de la tabla

        // Celdas de la segunda tabla
        PdfPCell cell2_1 = new PdfPCell(new Phrase("Área/Proceso/Dependencia:", 
                new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD)));
        PdfPCell cell2_2 = new PdfPCell(new Phrase("DEPARTAMENTO DE IDIOMAS", 
                new Font(Font.FontFamily.HELVETICA, 12)));
        cell2_1.setPadding(8f);
        cell2_2.setPadding(8f);

        table2.addCell(cell2_1);
        table2.addCell(cell2_2);

        // Agregar la segunda tabla al documento
        document.add(table2);
        
     // Crear tabla 1x1 para el contenido principal
        PdfPTable table = new PdfPTable(1);
        table.setWidthPercentage(100); // Ocupa todo el ancho de la página
        table.setSpacingBefore(30f); // Espaciado antes de la tabla
        table.setSpacingAfter(30f); // Espaciado después de la tabla

        // Crear celda para todo el contenido
        PdfPCell combinedCell = new PdfPCell();
        combinedCell.setBackgroundColor(new BaseColor(240, 240, 240)); // Sombreado claro
        combinedCell.setPadding(20f); // Espaciado interno de la celda

        // Crear título "MANIFIESTA QUE:"
        Paragraph title1 = new Paragraph("MANIFIESTA QUE:", new Font(Font.FontFamily.HELVETICA, 14, Font.BOLD));
        title1.setAlignment(Element.ALIGN_CENTER);
        combinedCell.addElement(title1); // Agregar título

        // Contenido del manifiesto
        String content = String.format(
            "(El/La) estudiante %s identificado/a con la cédula de ciudadanía No. %s del programa académico %s " +
            "cumplió satisfactoriamente con el requisito de grado exigido por la institución en materia de segunda lengua extranjera, " +
            "para obtener el título de TECNÓLOGO/A según Acuerdo No. 03-044 de julio 05 de 2016 y para que surta efectos oportunos, " +
            "expido el presente documento a petición del interesado en Bucaramanga a los %s.",
            studentName, id, program, date
        );

        // Agregar contenido a la celda
        Paragraph paragraph = new Paragraph(content, new Font(Font.FontFamily.HELVETICA, 12, Font.NORMAL));
        paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
        combinedCell.addElement(paragraph);

        // Espaciado antes de la firma
        combinedCell.addElement(Chunk.NEWLINE);

        // Agregar imagen de la firma
        try {
            Image signatureImage = Image.getInstance("src/main/resources/static/img/Logo.png"); // Cambiar por firma 
            signatureImage.scaleToFit(100, 50); // Tamaño de la firma
            signatureImage.setAlignment(Element.ALIGN_LEFT); // Alineación de la firma
            combinedCell.addElement(signatureImage); // Agregar firma
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Agregar línea adicional para firma
        Paragraph signatureLine = new Paragraph("________________________", new Font(Font.FontFamily.HELVETICA, 12, Font.NORMAL));
        signatureLine.setAlignment(Element.ALIGN_LEFT); // Alinear a la izquierda
        combinedCell.addElement(signatureLine);

        // Agregar texto para "Elaborado y revisado por:"
        Paragraph reviewedBy = new Paragraph("Elaborado y revisado por:", 
            new Font(Font.FontFamily.HELVETICA, 12, Font.NORMAL));
        reviewedBy.setAlignment(Element.ALIGN_LEFT);
        combinedCell.addElement(reviewedBy);

        // Espaciado adicional
        combinedCell.addElement(Chunk.NEWLINE);

        // Crear tabla para "Válido únicamente para trámite interno"
        PdfPTable footerTable = new PdfPTable(2); // Tabla de 2 columnas
        footerTable.setWidthPercentage(100); // Ancho completo de la página
        footerTable.setSpacingBefore(15f); // Espaciado antes de la tabla

        // Celda de texto "Válido únicamente para trámite interno"
        PdfPCell leftCell = new PdfPCell(new Phrase("Válido únicamente para trámite interno\n(Graduación)", 
            new Font(Font.FontFamily.HELVETICA, 10, Font.ITALIC)));
        leftCell.setHorizontalAlignment(Element.ALIGN_LEFT);
        leftCell.setBorder(Rectangle.NO_BORDER); // Sin bordes

     // Crear celda para "Firma y sello"
        PdfPCell rightCell = new PdfPCell();
        rightCell.setBorder(Rectangle.NO_BORDER); // Sin bordes

        // Agregar la imagen de la firma
        try {
            Image signatureImage = Image.getInstance("src/main/resources/static/img/Logo.png"); // Cambiar por firma Coodrinador 
            signatureImage.scaleToFit(80, 40); // Ajustar tamaño de la firma
            signatureImage.setAlignment(Element.ALIGN_CENTER); // Centrar la imagen
            rightCell.addElement(signatureImage);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Agregar texto debajo de la imagen
        Paragraph firmaText = new Paragraph("________________________\nFirma",
                new Font(Font.FontFamily.HELVETICA, 10, Font.NORMAL));
        firmaText.setAlignment(Element.ALIGN_CENTER); // Centrar el texto
        rightCell.addElement(firmaText);
        
        
        
        // Añadir celdas a la tabla
        footerTable.addCell(leftCell);
        footerTable.addCell(rightCell);

        // Agregar la tabla del footer a la celda combinada
        combinedCell.addElement(footerTable);

        // Añadir la celda combinada a la tabla principal
        table.addCell(combinedCell);

        // Añadir la tabla al documento
        document.add(table);
        
        
     // Crear tabla de 3 columnas para la parte inferior
        PdfPTable footerDetailsTable = new PdfPTable(3);
        footerDetailsTable.setWidthPercentage(100); // Ocupa todo el ancho de la página
        footerDetailsTable.setSpacingBefore(20f); // Espaciado antes de esta tabla

        // Crear estilos para las celdas
        Font headerFont = new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD);
        Font contentFont = new Font(Font.FontFamily.HELVETICA, 10, Font.NORMAL);

        // Celda "ELABORADO POR"
        PdfPCell elaboradoCell = new PdfPCell();
        elaboradoCell.setBorder(Rectangle.NO_BORDER);
        elaboradoCell.addElement(new Phrase("ELABORADO POR:", headerFont));
        elaboradoCell.addElement(new Phrase("Departamento de Humanidades", contentFont));
        footerDetailsTable.addCell(elaboradoCell);

        // Celda "REVISADO POR"
        PdfPCell revisadoCell = new PdfPCell();
        revisadoCell.setBorder(Rectangle.NO_BORDER);
        revisadoCell.addElement(new Phrase("REVISADO POR:", headerFont));
        revisadoCell.addElement(new Phrase("Soporte al Sistema Integrado de Gestión SIG", contentFont));
        footerDetailsTable.addCell(revisadoCell);

        // Celda "APROBADO POR"
        PdfPCell aprobadoCell = new PdfPCell();
        aprobadoCell.setBorder(Rectangle.NO_BORDER);
        aprobadoCell.addElement(new Phrase("APROBADO POR:", headerFont));
        aprobadoCell.addElement(new Phrase("Representante de la Dirección\nFecha de Aprobación: Septiembre de 2020", contentFont));
        footerDetailsTable.addCell(aprobadoCell);

        // Añadir la tabla al documento
        document.add(footerDetailsTable);


}
}
