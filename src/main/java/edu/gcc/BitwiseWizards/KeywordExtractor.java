import java.net.http.*;
import java.net.URI;
import java.util.*;
import com.google.gson.*;

public class KeywordExtractor {

    private static final String API_URL = "https://generativelanguage.googleapis.com/v1beta3/models/gemini-pro:generateContent";
    private final String apiKey;

    public KeywordExtractor(String apiKey) {
        this.apiKey = apiKey;
    }

    public List<String> extract(String query) throws Exception {
        HttpClient client = HttpClient.newHttpClient();

        JsonObject prompt = new JsonObject();
        prompt.addProperty("text", "Extract academic subject keywords (e.g., math, writing, psychology) from: \"" + query + "\"");

        JsonObject part = new JsonObject();
        part.add("parts", new JsonArray());
        part.getAsJsonArray("parts").add(prompt);

        JsonObject contents = new JsonObject();
        contents.add("contents", new JsonArray());
        contents.getAsJsonArray("contents").add(part);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI(API_URL + "?key=" + apiKey))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(
                        "{\"contents\":[{\"parts\":[{\"text\":\"Extract academic keywords from: \\\"" + query + "\\\"\"}]}]}")
                ).build();

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
