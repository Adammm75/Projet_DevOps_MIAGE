package org.example.devopslearning.controllers;

import lombok.RequiredArgsConstructor;
import org.example.devopslearning.entities.CourseRessource;
import org.example.devopslearning.repositories.CourseResourceRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * ✅ CONTRÔLEUR POUR LES RÉSUMÉS DE COURS
 * Utilise CourseResourceRepository (unifié)
 */
@RestController
@RequestMapping("/api/v1/courses")
@RequiredArgsConstructor
public class CourseSummaryController {

    private final CourseResourceRepository resourceRepository;

    /**
     * ✅ Récupère le résumé d'un cours (dernière ressource audio avec résumé)
     * Endpoint : GET /api/v1/courses/{courseId}/summary
     */
    @GetMapping("/{courseId}/summary")
    public ResponseEntity<Map<String, String>> getCourseSummary(@PathVariable Long courseId) {
        try {
            // Chercher la dernière ressource audio avec résumé
            Optional<CourseRessource> resource = resourceRepository
                    .findLatestAudioWithSummary(courseId);

            if (resource.isPresent()) {
                CourseRessource r = resource.get();

                Map<String, String> response = new HashMap<>();
                response.put("summary", r.getSummary() != null ? r.getSummary() : "");
                response.put("title", r.getTitle() != null ? r.getTitle() : "");

                // Optionnel : ajouter la transcription si disponible
                if (r.hasTranscript()) {
                    response.put("transcript", r.getTranscript());
                }

                return ResponseEntity.ok(response);
            } else {
                // Aucun résumé disponible
                return ResponseEntity.ok(Map.of(
                        "summary", "",
                        "message", "Aucun résumé disponible pour ce cours"
                ));
            }
        } catch (Exception e) {
            // En cas d'erreur, retourner un résumé vide
            return ResponseEntity.ok(Map.of(
                    "summary", "",
                    "error", "Erreur lors de la récupération du résumé"
            ));
        }
    }

    /**
     * ✅ BONUS : Récupère toutes les transcriptions d'un cours
     * Endpoint : GET /api/v1/courses/{courseId}/transcripts
     */
    @GetMapping("/{courseId}/transcripts")
    public ResponseEntity<Map<String, Object>> getCourseTranscripts(@PathVariable Long courseId) {
        try {
            List<CourseRessource> resources = resourceRepository
                    .findResourcesWithTranscript(courseId);

            Map<String, Object> response = new HashMap<>();
            response.put("count", resources.size());
            response.put("transcripts", resources);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(
                    Map.of("error", e.getMessage())
            );
        }
    }

    /**
     * ✅ BONUS : Récupère toutes les ressources avec résumé d'un cours
     * Endpoint : GET /api/v1/courses/{courseId}/summaries
     */
    @GetMapping("/{courseId}/summaries")
    public ResponseEntity<Map<String, Object>> getCourseSummaries(@PathVariable Long courseId) {
        try {
            List<CourseRessource> resources = resourceRepository
                    .findResourcesWithSummary(courseId);

            Map<String, Object> response = new HashMap<>();
            response.put("count", resources.size());
            response.put("resources", resources);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(
                    Map.of("error", e.getMessage())
            );
        }
    }
}
