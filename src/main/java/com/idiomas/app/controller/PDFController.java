package com.idiomas.app.controller;


import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.MediaType;

import com.idiomas.app.service.PDFService;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Image;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfWriter;


@RestController
@RequestMapping("/PDF")
public class PDFController {

	
    @Autowired
    private final PDFService pdfService;



    public PDFController(PDFService pdfService) {
        this.pdfService = pdfService;
    }

    @GetMapping("/download")
    public ResponseEntity<InputStreamResource> downloadCertificate(
            @RequestParam String studentName,
            @RequestParam String id,
            @RequestParam String program,
            @RequestParam String date) {

        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            Document document = new Document();
            PdfWriter writer = PdfWriter.getInstance(document, out);

            document.open(); // Abrir el documento antes de cualquier operación
            pdfService.generateHeader(document, studentName, id, program, date);

            // Agregar la imagen de firma
            PdfContentByte canvas = writer.getDirectContentUnder();
            try {
                Image watermarkImage = Image.getInstance("src/main/resources/static/img/sello_opaco.png");
                watermarkImage.scaleToFit(100, 100); // Tamaño del sello
                watermarkImage.setAbsolutePosition(400, 100); // Posición del sello
                canvas.addImage(watermarkImage);
            } catch (IOException e) {
                e.printStackTrace();
            }

            document.close(); // Cerrar el documento después de finalizar las operaciones

            ByteArrayInputStream bis = new ByteArrayInputStream(out.toByteArray());
            HttpHeaders headers = new HttpHeaders();
            // Usa el parámetro `id` (que corresponde a la cédula del alumno) como el nombre del archivo
            headers.add("Content-Disposition", "attachment; filename=" + id + ".pdf");

            return ResponseEntity.ok()
                    .headers(headers)
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(new InputStreamResource(bis));
        } catch (DocumentException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }


	
}
