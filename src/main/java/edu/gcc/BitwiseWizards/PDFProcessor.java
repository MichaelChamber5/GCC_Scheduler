package edu.gcc.BitwiseWizards;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class PDFProcessor {
    private static String catalogText = null;
    private static final String PDF_PATH = "src/main/resources/2024-25-Catalog.pdf";

    public static void initialize() throws IOException {
        try {
            System.out.println("Checking for PDF file at: " + PDF_PATH);
            // Check if PDF exists
            File pdfFile = new File(PDF_PATH);
            if (!pdfFile.exists()) {
                String errorMsg = "Course catalog PDF not found at " + PDF_PATH + 
                                "\nPlease ensure the file exists in the correct location.";
                System.err.println(errorMsg);
                throw new IOException(errorMsg);
            }

            System.out.println("PDF file found. Starting text extraction...");
            // Extract text from PDF
            catalogText = PDFTextExtractor.extractText(PDF_PATH);
            if (catalogText == null || catalogText.trim().isEmpty()) {
                throw new IOException("PDF text extraction returned empty content");
            }
            
            System.out.println("Successfully processed course catalog PDF");
            System.out.println("Extracted text length: " + catalogText.length() + " characters");
        } catch (IOException e) {
            System.err.println("Error processing course catalog PDF: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    public static String getCatalogText() {
        return catalogText;
    }
} 