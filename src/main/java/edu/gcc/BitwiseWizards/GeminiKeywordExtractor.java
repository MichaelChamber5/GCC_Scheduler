package edu.gcc.BitwiseWizards;

import okhttp3.*;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.Properties;

/**
 * GeminiKeywordExtractor provides functionality to extract keywords from user input
 * based on the contents of a college course catalog using the Gemini LLM API.
 * <p>
 * This class reads user queries, composes prompts including the course catalog content,
 * and sends the prompt to Gemini via an HTTP request using OkHttp. The API's response
 * is then parsed to return a list of relevant keywords.
 * </p>
 */
public class GeminiKeywordExtractor {

    /** HTTP client for making API requests */
    private final OkHttpClient client = new OkHttpClient();

    /** API key loaded from config.properties */
    private final String apiKey;

    /**
     * Constructor initializes the extractor by loading the API key from configuration.
     */
    public GeminiKeywordExtractor() {
        this.apiKey = loadApiKeyFromConfig();
    }

    /**
     * Loads the Gemini API key from a configuration file located in the classpath.
     *
     * @return the API key as a String
     * @throws RuntimeException if the file cannot be loaded or the key is missing
     */
    private String loadApiKeyFromConfig() {
        // First try environment variable
        String envApiKey = System.getenv("GEMINI_API_KEY");
        if (envApiKey != null && !envApiKey.trim().isEmpty()) {
            return envApiKey;
        }

        // Fallback to config file
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("config.properties")) {
            Properties prop = new Properties();
            prop.load(input);
            String apiKey = prop.getProperty("GEMINI_API_KEY");
            if (apiKey == null || apiKey.trim().isEmpty()) {
                throw new RuntimeException("GEMINI_API_KEY not found in config.properties or environment variables");
            }
            return apiKey;
        } catch (IOException e) {
            throw new RuntimeException("Failed to load API key from config.properties", e);
        }
    }

    /**
     * Reads the content of a file and extracts keywords from it using the Gemini API.
     *
     * @param filePath the path to the file containing the user's query
     * @return a list of keywords extracted from the input text
     * @throws IOException if the file cannot be read or the API call fails
     */
    public List<String> extractFromFile(String filePath) throws IOException {
        String content = new String(Files.readAllBytes(Paths.get(filePath)));
        return extractKeywords(content);
    }

    /**
     * Sends the input text along with the course catalog to the Gemini API to extract relevant keywords.
     * <p>
     * The prompt includes both the catalog contents and the user query. If the catalog
     * text is not already processed, it falls back to direct extraction from a known PDF file.
     * </p>
     *
     * @param inputText the user's input or query text
     * @return a list of keywords suggested by the Gemini API
     * @throws IOException if the API request fails or the response is malformed
     */
    public List<String> extractKeywords(String inputText) throws IOException {
        String catalogText = PDFProcessor.getCatalogText();
        if (catalogText == null) {
            System.err.println("Warning: Course catalog not processed. Falling back to direct PDF extraction.");
            catalogText = PDFTextExtractor.extractText("src/main/resources/2024-25-Catalog.pdf");
        }

        // First, get the list of available models
        Request listModelsRequest = new Request.Builder()
                .url("https://generativelanguage.googleapis.com/v1/models?key=" + apiKey)
                .get()
                .build();

        try (Response listModelsResponse = client.newCall(listModelsRequest).execute()) {
            if (!listModelsResponse.isSuccessful()) {
                String errorBody = listModelsResponse.body().string();
                System.err.println("Error listing models: " + errorBody);
                throw new IOException("Failed to list models: " + errorBody);
            }

            String modelsResponse = listModelsResponse.body().string();
            System.out.println("Available models: " + modelsResponse);

            // Use the standard gemini-pro model
            String modelName = "gemini-pro";
        }

        String prompt = """
        You are given a college course catalog and a student query.
        Extract a comma-separated list of **relevant keywords** based only on what exists in the catalog.
        Examples: course titles, professor names, department names, class days/times, semester terms.
        
        Catalog:
        """ + catalogText + """
        
        Query:
        """ + inputText + """
        """;

        JSONObject requestBody = new JSONObject()
                .put("contents", new JSONArray().put(new JSONObject()
                        .put("parts", new JSONArray().put(new JSONObject().put("text", prompt)))))
                .put("generationConfig", new JSONObject()
                        .put("temperature", 0.7)
                        .put("maxOutputTokens", 100));

        Request request = new Request.Builder()
                .url("https://generativelanguage.googleapis.com/v1/models/gemini-pro:generateContent?key=" + apiKey)
                .addHeader("Content-Type", "application/json")
                .post(RequestBody.create(requestBody.toString(), MediaType.parse("application/json")))
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                String errorBody = response.body().string();
                System.err.println("Gemini API Error Response: " + errorBody);
                throw new IOException("Gemini API failed with status " + response.code() + ": " + errorBody);
            }

            String responseBody = response.body().string();
            System.out.println("Gemini API Response: " + responseBody);

            JSONObject json = new JSONObject(responseBody);
            String text = json
                    .getJSONArray("candidates")
                    .getJSONObject(0)
                    .getJSONObject("content")
                    .getJSONArray("parts")
                    .getJSONObject(0)
                    .getString("text");

            System.out.println("Extracted keywords: " + text);

            return Arrays.stream(text.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.toList());
        } catch (Exception e) {
            System.err.println("Error in Gemini API call: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
}
