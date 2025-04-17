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

public class GeminiKeywordExtractor {
    private final OkHttpClient client = new OkHttpClient();
    private final String apiKey;

    public GeminiKeywordExtractor() {
        this.apiKey = loadApiKeyFromConfig();
    }

    // Load the API key from config.properties
    private String loadApiKeyFromConfig() {
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("config.properties")) {
            Properties prop = new Properties();
            prop.load(input);
            return prop.getProperty("GEMINI_API_KEY");
        } catch (IOException e) {
            throw new RuntimeException("Failed to load API key from config.properties", e);
        }
    }

    // Read query from a file and extract keywords
    public List<String> extractFromFile(String filePath) throws IOException {
        String content = new String(Files.readAllBytes(Paths.get(filePath)));
        return extractKeywords(content);
    }

    // Send user input to Gemini and get keywords back
    public List<String> extractKeywords(String inputText) throws IOException {
        String prompt = """
        Extract relevant keywords from the following course search query. Keywords should include:
        - Professor names
        - Course titles or topics
        - Department codes
        - Days of the week
        - Time of day (like morning/evening)
        - Semester info
        
        Respond ONLY with a comma-separated list of keywords, no full sentences.
        
        Query:
        """ + inputText;

        JSONObject requestBody = new JSONObject()
                .put("contents", new JSONArray().put(new JSONObject()
                        .put("parts", new JSONArray().put(new JSONObject().put("text", prompt)))))
                .put("generationConfig", new JSONObject()
                        .put("temperature", 0.7)
                        .put("maxOutputTokens", 100));

        Request request = new Request.Builder()
                .url("https://generativelanguage.googleapis.com/v1beta/models/gemini-pro:generateContent")
                .addHeader("Content-Type", "application/json")
                .addHeader("Authorization", "Bearer " + apiKey)
                .post(RequestBody.create(requestBody.toString(), MediaType.parse("application/json")))
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Gemini API failed: " + response.body().string());
            }

            JSONObject json = new JSONObject(response.body().string());
            String text = json
                    .getJSONArray("candidates")
                    .getJSONObject(0)
                    .getJSONObject("content")
                    .getJSONArray("parts")
                    .getJSONObject(0)
                    .getString("text");

            return Arrays.stream(text.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.toList());
        }
    }
}
