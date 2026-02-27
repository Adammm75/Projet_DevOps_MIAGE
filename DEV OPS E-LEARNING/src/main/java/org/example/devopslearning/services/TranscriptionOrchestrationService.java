package org.example.devopslearning.services;

import org.example.devopslearning.dto.GladiaWebhookDto;
import org.example.devopslearning.entities.Cours;
import org.example.devopslearning.entities.CourseRessource;
import org.example.devopslearning.repositories.CoursRepository;
import org.example.devopslearning.repositories.CourseResourceRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.UUID;

@Service
public class TranscriptionOrchestrationService {

    private final CoursRepository courseRepository;
    private final CourseResourceRepository resourceRepository;
    private final S3Service s3Service;
    private final OpenAISummarizationService summarizationService;
    private final ObjectMapper mapper = new ObjectMapper();

    public TranscriptionOrchestrationService(
            CoursRepository courseRepository,
            CourseResourceRepository resourceRepository,
            S3Service s3Service,
            OpenAISummarizationService summarizationService) {
        this.courseRepository = courseRepository;
        this.resourceRepository = resourceRepository;
        this.s3Service = s3Service;
        this.summarizationService = summarizationService;
    }

    @Transactional
    public void processGladiaPayload(GladiaWebhookDto p) {
        try {
            // 1. Récupérer ou créer le cours
            Cours course = courseRepository.findById(p.getCourseId()).orElseGet(() -> {
                Cours c = new Cours();
                c.setTitle("Cours auto-créé " + (p.getCourseId() == null ? UUID.randomUUID() : p.getCourseId()));
                return courseRepository.save(c);
            });

            CourseRessource r = new CourseRessource();
            r.setCourse(course);
            r.setType("AUDIO");
            r.setTitle(p.getTitle() != null ? p.getTitle()
                    : ("Séance " + (p.getSessionId() != null ? p.getSessionId() : UUID.randomUUID())));
            r.setCreatedAt(Instant.now());

            // Sauvegarder d'abord pour obtenir l'ID
            resourceRepository.save(r);

            // 3. Télécharger et uploader l'audio vers S3 si disponible
            if (p.getAudioUrl() != null && !p.getAudioUrl().isBlank()) {
                try (InputStream audioStream = downloadUrl(p.getAudioUrl())) {
                    String audioKey = "courses_" + course.getId() + "_" + UUID.randomUUID() + ".mp3";
                    String s3Audio = s3Service.upload(audioStream, audioKey);
                    r.setS3AudioUrl(s3Audio); // ✅ Utilise s3AudioUrl au lieu de s3audio_url
                } catch (Exception e) {
                    System.err.println("Erreur upload audio S3: " + e.getMessage());
                }
            }

            // 4. Récupérer la transcription
            String transcriptText = p.getTranscript();
            if ((transcriptText == null || transcriptText.isBlank()) && p.getTranscriptUrl() != null) {
                try {
                    transcriptText = downloadUrlAsString(p.getTranscriptUrl());
                } catch (Exception e) {
                    System.err.println("Erreur téléchargement transcription: " + e.getMessage());
                }
            }

            if (transcriptText != null && !transcriptText.isBlank()) {
                r.setTranscript(transcriptText);

                try {
                    String transcriptKey = "courses_" + course.getId() + "_" + UUID.randomUUID() + ".txt";
                    s3Service.upload(
                            new ByteArrayInputStream(transcriptText.getBytes(StandardCharsets.UTF_8)),
                            transcriptKey);
                } catch (Exception e) {
                    System.err.println("Erreur upload transcription S3: " + e.getMessage());
                }

                try {
                    String summary = summarizationService.summarize(transcriptText);
                    r.setSummary(summary);
                    r.setKeywords(null);
                    r.setStructureJson(null);
                } catch (Exception e) {
                    System.err.println("Erreur génération résumé OpenAI: " + e.getMessage());
                    r.setSummary("Erreur lors de la génération du résumé");
                }
            }

            // 7. Sauvegarder la ressource finale
            resourceRepository.save(r);

            System.out.println("✅ Ressource traitée avec succès - ID: " + r.getId());

        } catch (Exception ex) {
            System.err.println("❌ Erreur processing webhook: " + ex.getMessage());
            ex.printStackTrace();
            throw new RuntimeException("Erreur processing webhook", ex);
        }
    }

    private InputStream downloadUrl(String urlString) throws Exception {
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setConnectTimeout(10000);
        conn.setReadTimeout(20000);
        conn.setRequestProperty("User-Agent", "Mozilla/5.0");

        int responseCode = conn.getResponseCode();
        if (responseCode != 200) {
            throw new Exception("HTTP error code: " + responseCode);
        }

        return conn.getInputStream();
    }

    private String downloadUrlAsString(String urlString) throws Exception {
        try (InputStream is = downloadUrl(urlString)) {
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
