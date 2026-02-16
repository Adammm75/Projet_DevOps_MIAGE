package org.example.devopslearning.services;

import org.example.devopslearning.dto.GladiaWebhookDto;
import org.example.devopslearning.entities.Cours;
import org.example.devopslearning.entities.CourseResource;
import org.example.devopslearning.repositories.CourseResourceRepositoryy;
import org.example.devopslearning.repositories.CoursRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Service
public class TranscriptionOrchestrationService {

    private final CoursRepository courseRepository;
    private final CourseResourceRepositoryy resourceRepository;
    private final S3Service s3Service;
    private final OpenAISummarizationService summarizationService;
    private final ObjectMapper mapper = new ObjectMapper();

    public TranscriptionOrchestrationService(CoursRepository courseRepository,
            CourseResourceRepositoryy resourceRepository,
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
            Cours course = courseRepository.findById(p.getCourseId()).orElseGet(() -> {
                Cours c = new Cours();
                c.setTitle("Cours auto-créé " + (p.getCourseId() == null ? UUID.randomUUID() : p.getCourseId()));
                return courseRepository.save(c);
            });

            CourseResource r = new CourseResource();
            r.setCourse(course);
            r.setType("AUDIO");
            r.setTitle(p.getTitle() != null ? p.getTitle()
                    : ("Séance " + (p.getSessionId() != null ? p.getSessionId() : UUID.randomUUID())));
            resourceRepository.save(r);

            if (p.getAudioUrl() != null && !p.getAudioUrl().isBlank()) {
                try (InputStream audioStream = downloadUrl(p.getAudioUrl())) {
                    String audioKey = "courses_" + course.getId() + "_" + UUID.randomUUID() + ".mp3";
                    String s3Audio = s3Service.upload(audioStream, audioKey);
                    r.setS3AudioUrl(s3Audio);
                }
            }

            String transcriptText = p.getTranscript();
            if ((transcriptText == null || transcriptText.isBlank()) && p.getTranscriptUrl() != null) {
                transcriptText = downloadUrlAsString(p.getTranscriptUrl());
            }

            if (transcriptText != null) {
                r.setTranscript(transcriptText);
                String transcriptKey = "courses_" + course.getId() + "_" + UUID.randomUUID() + ".txt";
                s3Service.upload(new ByteArrayInputStream(transcriptText.getBytes(StandardCharsets.UTF_8)),
                        transcriptKey);

                // résumé avec OpenAI
                String summary = summarizationService.summarize(transcriptText);
                r.setSummary(summary);
                r.setKeywords(null);
                r.setStructureJson(null);
            }

            resourceRepository.save(r);
        } catch (Exception ex) {
            throw new RuntimeException("Erreur processing webhook", ex);
        }
    }

    private InputStream downloadUrl(String urlString) throws Exception {
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setConnectTimeout(10000);
        conn.setReadTimeout(20000);
        return conn.getInputStream();
    }

    private String downloadUrlAsString(String urlString) throws Exception {
        try (InputStream is = downloadUrl(urlString)) {
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
