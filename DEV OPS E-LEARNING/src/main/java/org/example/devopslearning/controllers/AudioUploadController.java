package org.example.devopslearning.controllers;

import org.example.devopslearning.entities.Cours;
import org.example.devopslearning.entities.CourseRessource;
import org.example.devopslearning.repositories.CoursRepository;
import org.example.devopslearning.repositories.CourseResourceRepository;
import org.example.devopslearning.services.GladiaTranscriptionService;
import org.example.devopslearning.services.OpenAISummarizationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Files;
import java.time.Instant;
import java.util.Map;

/**
 * ✅ CONTRÔLEUR CORRIGÉ : Utilise CourseRessource (entité unifiée)
 */
@RestController
@RequestMapping("/api/v1/audio")
public class AudioUploadController {

    private final CoursRepository courseRepository;
    private final CourseResourceRepository resourceRepository;
    private final GladiaTranscriptionService gladiaService;
    private final OpenAISummarizationService summarizationService;

    public AudioUploadController(
            CoursRepository courseRepository,
            CourseResourceRepository resourceRepository,
            GladiaTranscriptionService gladiaService,
            OpenAISummarizationService summarizationService) {
        this.courseRepository = courseRepository;
        this.resourceRepository = resourceRepository;
        this.gladiaService = gladiaService;
        this.summarizationService = summarizationService;
    }

    @PostMapping("/upload")
    public ResponseEntity<?> uploadAudio(
            @RequestParam Long courseId,
            @RequestParam String title,
            @RequestParam("file") MultipartFile file) {
        try {
            // 📹 1. Récupération du cours
            Cours course = courseRepository.findById(courseId).orElseGet(() -> {
                Cours c = new Cours();
                c.setTitle("Cours auto-créé " + courseId);
                return courseRepository.save(c);
            });

            // 📹 2. Sauvegarde temporaire du fichier
            File tempFile = File.createTempFile("audio_", ".mp3");
            file.transferTo(tempFile);

            // 📹 3. Envoi à Gladia pour transcription
            var gladiaResponse = gladiaService.sendAudioForTranscription(tempFile);
            String transcript = gladiaService.extractTranscript(gladiaResponse);

            // 📹 4. Résumé via OpenAI
            String summary = summarizationService.summarize(transcript);

            // 📹 5. INSERTION EN BASE avec CourseRessource
            CourseRessource resource = new CourseRessource();
            resource.setCourse(course);
            resource.setType("AUDIO");
            resource.setTitle(title);
            resource.setTranscript(transcript);
            resource.setSummary(summary);
            resource.setCreatedAt(Instant.now());

            // Note: s3AudioUrl sera défini par S3StorageService si nécessaire
            // Pour l'instant on stocke juste la transcription et le résumé

            resourceRepository.save(resource);

            // 📹 LOG DE TEST
            System.out.println(">>> COURSE_RESOURCE SAVED ID = " + resource.getId());
            System.out.println(">>> HAS TRANSCRIPT: " + resource.hasTranscript());
            System.out.println(">>> HAS SUMMARY: " + resource.hasSummary());

            // 📹 Nettoyage du fichier temporaire
            Files.deleteIfExists(tempFile.toPath());

            return ResponseEntity.ok(Map.of(
                    "message", "Audio traité et sauvegardé",
                    "resourceId", resource.getId(),
                    "summary", summary,
                    "transcript", transcript,
                    "hasTranscript", resource.hasTranscript(),
                    "hasSummary", resource.hasSummary()
            ));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(
                    Map.of("error", e.getMessage())
            );
        }
    }

    /**
     * ✅ NOUVEAU : Endpoint pour récupérer le résumé d'un cours
     */
    @GetMapping("/courses/{courseId}/summary")
    public ResponseEntity<?> getCourseSummary(@PathVariable Long courseId) {
        try {
            // Récupérer la ressource audio la plus récente avec un résumé
            var resource = resourceRepository.findByCourse_Id(courseId).stream()
                    .filter(r -> "AUDIO".equals(r.getType()) && r.hasSummary())
                    .findFirst();

            if (resource.isPresent()) {
                return ResponseEntity.ok(Map.of(
                        "summary", resource.get().getSummary(),
                        "transcript", resource.get().getTranscript() != null ? resource.get().getTranscript() : "",
                        "title", resource.get().getTitle()
                ));
            } else {
                return ResponseEntity.ok(Map.of(
                        "summary", "",
                        "message", "Aucun résumé disponible"
                ));
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(
                    Map.of("error", e.getMessage())
            );
        }
    }
}
