package org.example.devopslearning.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.io.File;
import java.nio.file.Files;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;

@Service
public class GladiaTranscriptionService {

    private final String GLADIA_KEY = "b4ab522e-9a56-441a-8578-b7bc9bdae46b"; // Ta clé Gladia
    private final ObjectMapper mapper = new ObjectMapper();
    private final OpenAISummarizationService summarizationService;

    public GladiaTranscriptionService(OpenAISummarizationService summarizationService) {
        this.summarizationService = summarizationService;
    }

    public OpenAISummarizationService getSummarizationService() {
        return summarizationService;
    }

    public JsonNode sendAudioForTranscription(File audio) throws Exception {
        String url = "https://api.gladia.io/audio/text/audio-transcription";
        String boundary = "----Boundary" + System.currentTimeMillis();

        HttpRequest.BodyPublisher body = buildMultipartBody(audio, boundary);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("x-gladia-key", GLADIA_KEY)
                .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                .POST(body)
                .build();

        HttpClient client = HttpClient.newHttpClient();
        HttpResponse<String> resp = client.send(request, HttpResponse.BodyHandlers.ofString());

        System.out.println("=== GLADIA DEBUG ===");
        System.out.println("STATUS: " + resp.statusCode());
        System.out.println("BODY: " + resp.body());
        System.out.println("====================");

        if (resp.statusCode() >= 400) {
            throw new RuntimeException("Erreur Gladia: " + resp.body());
        }

        return mapper.readTree(resp.body());
    }

    public String extractTranscript(JsonNode node) {
        if (node.has("prediction") && node.get("prediction").isArray()) {
            StringBuilder sb = new StringBuilder();
            for (JsonNode segment : node.get("prediction")) {
                if (segment.has("transcription") && !segment.get("transcription").asText().isBlank()) {
                    sb.append(segment.get("transcription").asText()).append(" ");
                }
            }
            return sb.toString().trim();
        }
        return null;
    }

    private HttpRequest.BodyPublisher buildMultipartBody(File audio, String boundary) throws Exception {
        var byteArrays = new ArrayList<byte[]>();

        String part1 = "--" + boundary + "\r\n" +
                "Content-Disposition: form-data; name=\"audio\"; filename=\"" + audio.getName() + "\"\r\n" +
                "Content-Type: audio/mpeg\r\n\r\n";

        byteArrays.add(part1.getBytes());
        byteArrays.add(Files.readAllBytes(audio.toPath()));
        byteArrays.add("\r\n".getBytes());

        String part2 = "--" + boundary + "\r\n" +
                "Content-Disposition: form-data; name=\"timestamps\"\r\n\r\n" +
                "true\r\n";

        byteArrays.add(part2.getBytes());

        String end = "--" + boundary + "--";
        byteArrays.add(end.getBytes());

        return HttpRequest.BodyPublishers.ofByteArrays(byteArrays);
    }
}
