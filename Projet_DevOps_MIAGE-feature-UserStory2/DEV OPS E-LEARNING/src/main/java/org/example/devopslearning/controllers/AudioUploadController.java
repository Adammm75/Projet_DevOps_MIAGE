package org.example.devopslearning.controllers;

import org.example.devopslearning.entities.Cours;
import org.example.devopslearning.entities.CourseResource;
import org.example.devopslearning.repositories.CoursRepository;
import org.example.devopslearning.repositories.CourseResourceRepositoryy;
import org.example.devopslearning.services.GladiaTranscriptionService;
import org.example.devopslearning.services.OpenAISummarizationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Files;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/audio")
public class AudioUploadController {

    private final CoursRepository courseRepository;
    private final CourseResourceRepositoryy resourceRepository;
    private final GladiaTranscriptionService gladiaService;
    private final OpenAISummarizationService summarizationService;

    public AudioUploadController(CoursRepository courseRepository,
            CourseResourceRepositoryy resourceRepository,
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
            // 🔹 1. Récupération du cours
            Cours course = courseRepository.findById(courseId).orElseGet(() -> {
                Cours c = new Cours();
                c.setTitle("Cours auto-créé " + courseId);
                return courseRepository.save(c);
            });

            // 🔹 2. Sauvegarde temporaire du fichier
            File tempFile = File.createTempFile("audio_", ".mp3");
            file.transferTo(tempFile);

            // 🔹 3. Envoi à Gladia
            var gladiaResponse = gladiaService.sendAudioForTranscription(tempFile);
            String transcript = gladiaService.extractTranscript(gladiaResponse);

            // 🔹 4. Résumé OpenAI
            String summary = summarizationService.summarize(transcript);

            // 🔹 5. INSERTION EN BASE
            CourseResource r = new CourseResource();
            r.setCourse(course);
            r.setType("AUDIO");
            r.setTitle(title);
            r.setTranscript(transcript);
            r.setSummary(summary);

            resourceRepository.save(r);

            // 🔹 LOG DE TEST
            System.out.println(">>> COURSE_RESOURCE SAVED ID = " + r.getId());

            // 🔹 Nettoyage
            Files.deleteIfExists(tempFile.toPath());

            return ResponseEntity.ok(Map.of(
                    "message", "Audio traité et sauvegardé",
                    "resourceId", r.getId(),
                    "summary", summary,
                    "transcript", transcript));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(
                    Map.of("error", e.getMessage()));
        }
    }
}
