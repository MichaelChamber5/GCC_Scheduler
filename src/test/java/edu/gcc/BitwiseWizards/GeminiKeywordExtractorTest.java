package edu.gcc.BitwiseWizards;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.util.List;

public class GeminiKeywordExtractorTest {
    private static GeminiKeywordExtractor extractor;

    @BeforeAll
    public static void setUp() throws IOException {
        // Initialize PDF processor first
        PDFProcessor.initialize();
        // Create the extractor
        extractor = new GeminiKeywordExtractor();
    }

    @Test
    public void testExtractKeywords() {
        try {
            // Test with a simple query
            String query = "I want to take a computer science class in the morning";
            List<String> keywords = extractor.extractKeywords(query);
            
            // Print the results for debugging
            System.out.println("Query: " + query);
            System.out.println("Extracted keywords: " + keywords);
            
            // Basic assertions
            assertNotNull(keywords, "Keywords list should not be null");
            assertFalse(keywords.isEmpty(), "Keywords list should not be empty");
            
            // Check if common expected keywords are present
            boolean hasComputerScience = keywords.stream()
                    .anyMatch(k -> k.toLowerCase().contains("computer") || 
                                 k.toLowerCase().contains("cs") ||
                                 k.toLowerCase().contains("comp"));
            
            assertTrue(hasComputerScience, "Should extract computer science related keywords");
            
        } catch (IOException e) {
            fail("Test failed with IOException: " + e.getMessage());
        }
    }

    @Test
    public void testExtractKeywordsWithProfessor() {
        try {
            // Test with a query including a professor name
            String query = "I want to take a class with Professor Smith";
            List<String> keywords = extractor.extractKeywords(query);
            
            // Print the results for debugging
            System.out.println("Query: " + query);
            System.out.println("Extracted keywords: " + keywords);
            
            // Basic assertions
            assertNotNull(keywords, "Keywords list should not be null");
            assertFalse(keywords.isEmpty(), "Keywords list should not be empty");
            
            // Check if professor name is extracted
            boolean hasProfessor = keywords.stream()
                    .anyMatch(k -> k.toLowerCase().contains("smith"));
            
            assertTrue(hasProfessor, "Should extract professor name from query");
            
        } catch (IOException e) {
            fail("Test failed with IOException: " + e.getMessage());
        }
    }

    @Test
    public void testExtractKeywordsWithTime() {
        try {
            // Test with a query including time information
            String query = "I need a class that meets on Monday morning";
            List<String> keywords = extractor.extractKeywords(query);
            
            // Print the results for debugging
            System.out.println("Query: " + query);
            System.out.println("Extracted keywords: " + keywords);
            
            // Basic assertions
            assertNotNull(keywords, "Keywords list should not be null");
            assertFalse(keywords.isEmpty(), "Keywords list should not be empty");
            
            // Check if time-related keywords are extracted
            boolean hasTimeInfo = keywords.stream()
                    .anyMatch(k -> k.toLowerCase().contains("monday") || 
                                 k.toLowerCase().contains("morning"));
            
            assertTrue(hasTimeInfo, "Should extract time-related keywords");
            
        } catch (IOException e) {
            fail("Test failed with IOException: " + e.getMessage());
        }
    }
} 