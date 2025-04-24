package edu.gcc.BitwiseWizards;

import java.net.http.*;
import java.net.URI;
import java.util.*;
import com.google.gson.*;

/**
 * A lightweight keyword extractor that uses Google's Gemini API to identify academic subject keywords
 * from natural language queries.
 *
 * This class is useful for quick parsing of general subject terms like "math", "biology", or "philosophy"
 * from user input without referencing a course catalog.
 */
public class KeywordExtractor {

    /** The base URL for Google's Gemini API */
    private static final String API_URL = "https://generativelanguage.googleapis.com/v1beta3/models/gemini-pro:generateContent";

    /** API key used to authenticate with Gemini */
    private final String apiKey;

    /**
     * Constructs a KeywordExtractor using the provided Gemini API key.
     *
     * @param apiKey the API key for Gemini
     */
    public KeywordExtractor(String apiKey) {
        this.apiKey = apiKey;
    }

    /**
     * Extracts academic subject keywords from the provided query.
     *
     * Sends a prompt to Gemini to identify subject-specific terms based on the user input,
     * such as course topics or areas of study.
     *
     * Example input: "I'm interested in psychology and computer science classes"
     * Example output: ["psychology", "computer science"]
     *
     * @param query a natural language query representing a student's academic interests
     * @return a list of lowercase academic keywords extracted from the query
     * @throws Exception if the HTTP request fails or the API response is invalid
     */
    public List<String> extract(String query) throws Exception {
        HttpClient client = HttpClient.newHttpClient();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI(API_URL + "?key=" + apiKey))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(
                        "{\"contents\":[{\"parts\":[{\"text\":\"Extract academic keywords from: \\\"" + query + "\\\"\"}]}]}"))
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
