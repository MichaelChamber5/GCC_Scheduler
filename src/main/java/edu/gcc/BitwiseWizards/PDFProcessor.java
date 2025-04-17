package edu.gcc.BitwiseWizards;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class PDFProcessor {
    private static String catalogText = null;
    private static final String PDF_PATH = "src/main/resources/2024-25-Catalog.pdf";

    public static void initialize() {
        try {
            // Check if PDF exists
            File pdfFile = new File(PDF_PATH);
            if (!pdfFile.exists()) {
                System.err.println("Warning: Course catalog PDF not found at " + PDF_PATH);
                return;
            }

            // Extract text from PDF
            catalogText = PDFTextExtractor.extractText(PDF_PATH);
            System.out.println("Successfully processed course catalog PDF");
        } catch (IOException e) {
            System.err.println("Error processing course catalog PDF: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static String getCatalogText() {
        return catalogText;
    }
} 