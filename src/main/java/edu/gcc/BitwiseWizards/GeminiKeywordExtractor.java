package edu.gcc.BitwiseWizards;

import java.net.http.*;
import java.net.URI;
import java.util.*;
import com.google.gson.*;

public class GeminiKeywordExtractor {

    private static final String API_URL = "https://generativelanguage.googleapis.com/v1beta3/models/gemini-pro:generateContent";
    private final String apiKey;

    public GeminiKeywordExtractor(String apiKey) {
        this.apiKey = apiKey;
    }

    public List<String> extractKeywords(String query) throws Exception {
        HttpClient client = HttpClient.newHttpClient();

        String prompt = "Extract relevant academic subject keywords (e.g. math, writing, psychology, accounting) from the student query: \"" + query + "\". Return only keywords as a comma-separated list.";

        String bodyJson = """
        {
          "contents": [
            {
              "parts": [
                {
                  "text": "%s"
                }
              ]
            }
          ]
        }
        """.formatted(prompt);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL + "?key=" + apiKey))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(bodyJson))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        JsonObject json = JsonParser.parseString(response.body()).getAsJsonObject();
        String keywordString = json.getAsJsonArray("candidates")
                .get(0).getAsJsonObject()
                .getAsJsonObject("content")
                .getAsJsonArray("parts")
                .get(0).getAsJsonObject()
                .get("text").getAsString();

        return Arrays.stream(keywordString.split(","))
                .map(String::trim)
                .map(String::toLowerCase)
                .toList();
    }
}
