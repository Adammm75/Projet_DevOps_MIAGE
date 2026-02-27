package org.example.devopslearning.services;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;
import java.util.HashMap;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;

@Service
public class OpenAISummarizationService {

    @Value("${openai.api-key}")
    private String apiKey;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public String summarize(String text) {
        try {
            if (apiKey == null || apiKey.isBlank()) {
                return "Erreur : clé OpenAI manquante.";
            }

            HttpClient client = HttpClient.newHttpClient();

            Map<String, Object> requestMap = new HashMap<>();
            requestMap.put("model", "gpt-3.5-turbo");
            requestMap.put("messages", new Object[] {
                    Map.of(
                            "role", "user",
                            "content",
                            "Résume le texte suivant.\n" +
                                    "Retourne STRICTEMENT en JSON avec ce format :\n" +
                                    "{\n" +
                                    "  \"title\": \"...\",\n" +
                                    "  \"summary\": \"...\",\n" +
                                    "  \"keywords\": [\"mot1\", \"mot2\", \"mot3\"]\n" +
                                    "}\n\nTexte :\n" + text)
            });

            String requestBody = objectMapper.writeValueAsString(requestMap);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.openai.com/v1/chat/completions"))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + apiKey)
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            JsonNode json = objectMapper.readTree(response.body());

            if (json.has("error")) {
                return "Erreur OpenAI : " + json.get("error").get("message").asText();
            }

            String content = json.get("choices")
                    .get(0)
                    .get("message")
                    .get("content")
                    .asText();

            return content; // JSON string

        } catch (Exception e) {
            e.printStackTrace();
            return "ERREUR_INTERNE: " + e.getMessage();
        }
    }
}