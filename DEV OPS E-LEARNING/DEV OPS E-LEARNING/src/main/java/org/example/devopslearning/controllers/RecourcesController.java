package org.example.devopslearning.controllers;

import lombok.RequiredArgsConstructor;
import org.example.devopslearning.entities.CourseRessource;
import org.example.devopslearning.repositories.CourseResourceRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * ✅ CONTRÔLEUR API REST CORRIGÉ pour les ressources
 * Utilise CourseResourceRepository et CourseRessource (unifiés)
 */
@RestController
@RequestMapping("/api/v1/resources")
@RequiredArgsConstructor
public class RecourcesController {

    private final CourseResourceRepository repo;  // ✅ CHANGÉ

    /**
     * ✅ Liste toutes les ressources
     * GET /api/v1/resources
     */
    @GetMapping
    public List<CourseRessource> listAll() {  // ✅ CHANGÉ
        return repo.findAll();
    }

    /**
     * ✅ Liste les ressources d'un cours spécifique
     * GET /api/v1/resources/course/{courseId}
     */
    @GetMapping("/course/{courseId}")
    public List<CourseRessource> listByCourse(@PathVariable Long courseId) {  // ✅ CHANGÉ
        return repo.findByCourse_Id(courseId);  // ✅ Méthode du repository unifié
    }

    /**
     * ✅ Récupère une ressource par son ID
     * GET /api/v1/resources/{id}
     */
    @GetMapping("/{id}")
    public CourseRessource getById(@PathVariable Long id) {  // ✅ CHANGÉ
        return repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Ressource non trouvée avec l'ID: " + id));
    }

    /**
     * ✅ BONUS : Liste les ressources avec transcription
     * GET /api/v1/resources/course/{courseId}/transcripts
     */
    @GetMapping("/course/{courseId}/transcripts")
    public List<CourseRessource> getResourcesWithTranscript(@PathVariable Long courseId) {
        return repo.findResourcesWithTranscript(courseId);
    }

    /**
     * ✅ BONUS : Liste les ressources avec résumé
     * GET /api/v1/resources/course/{courseId}/summaries
     */
    @GetMapping("/course/{courseId}/summaries")
    public List<CourseRessource> getResourcesWithSummary(@PathVariable Long courseId) {
        return repo.findResourcesWithSummary(courseId);
    }

    /**
     * ✅ BONUS : Filtre par type de ressource
     * GET /api/v1/resources/course/{courseId}/type/{type}
     */
    @GetMapping("/course/{courseId}/type/{type}")
    public List<CourseRessource> getResourcesByType(
            @PathVariable Long courseId,
            @PathVariable String type) {
        return repo.findByCourse_IdAndType(courseId, type);
    }
}