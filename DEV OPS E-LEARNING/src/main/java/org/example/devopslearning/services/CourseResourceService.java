package org.example.devopslearning.services;

import lombok.RequiredArgsConstructor;
import org.example.devopslearning.entities.CourseRessource;
import org.example.devopslearning.repositories.CourseResourceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * ✅ Service Extension pour des fonctionnalités avancées de gestion des ressources
 * Ce service complète CoursService avec des méthodes spécifiques aux ressources
 */
@Service
@RequiredArgsConstructor
public class CourseResourceService {

    private final CourseResourceRepository resourceRepository;

    /**
     * ✅ Récupère les statistiques des ressources par type
     */
    public Map<String, Long> getResourceStatsByType(Long courseId) {
        List<CourseRessource> resources = resourceRepository.findByCourse_Id(courseId);

        Map<String, Long> stats = new HashMap<>();
        stats.put("PDF", resources.stream().filter(r -> "PDF".equals(r.getType())).count());
        stats.put("WORD", resources.stream().filter(r -> "WORD".equals(r.getType())).count());
        stats.put("PPT", resources.stream().filter(r -> "PPT".equals(r.getType())).count());
        stats.put("EXCEL", resources.stream().filter(r -> "EXCEL".equals(r.getType())).count());
        stats.put("AUDIO", resources.stream().filter(r -> "AUDIO".equals(r.getType())).count());
        stats.put("VIDEO", resources.stream().filter(r -> "VIDEO".equals(r.getType())).count());
        stats.put("IMAGE", resources.stream().filter(r -> "IMAGE".equals(r.getType())).count());
        stats.put("ARCHIVE", resources.stream().filter(r -> "ARCHIVE".equals(r.getType())).count());
        stats.put("OTHER", resources.stream().filter(r -> r.getType() == null || "FILE".equals(r.getType())).count());

        return stats;
    }

    /**
     * ✅ Récupère toutes les ressources d'un cours (méthode helper)
     */
    public List<CourseRessource> getResourcesByCourseId(Long courseId) {
        return resourceRepository.findByCourse_Id(courseId);
    }

    /**
     * ✅ Récupère les ressources par type
     */
    public List<CourseRessource> getResourcesByType(Long courseId, String type) {
        return resourceRepository.findByCourse_IdAndType(courseId, type);
    }

    /**
     * ✅ Recherche de ressources par mot-clé dans le titre
     */
    public List<CourseRessource> searchResourcesByTitle(Long courseId, String keyword) {
        List<CourseRessource> allResources = resourceRepository.findByCourse_Id(courseId);

        if (keyword == null || keyword.trim().isEmpty()) {
            return allResources;
        }

        String lowerKeyword = keyword.toLowerCase();
        return allResources.stream()
                .filter(r -> r.getTitle().toLowerCase().contains(lowerKeyword))
                .collect(Collectors.toList());
    }

    /**
     * ✅ Compte le nombre total de ressources d'un cours
     */
    public long countResourcesByCourse(Long courseId) {
        return resourceRepository.countByCourse_Id(courseId);
    }

    /**
     * ✅ Vérifie si une ressource appartient à un cours spécifique
     */
    public boolean resourceBelongsToCourse(Long resourceId, Long courseId) {
        return resourceRepository.findById(resourceId)
                .map(r -> r.getCourse() != null && r.getCourse().getId().equals(courseId))
                .orElse(false);
    }

    /**
     * ✅ Récupère les ressources récentes (dernières 10)
     */
    public List<CourseRessource> getRecentResources(Long courseId) {
        return resourceRepository.findByCourse_IdOrderByCreatedAtDesc(courseId)
                .stream()
                .limit(10)
                .collect(Collectors.toList());
    }

    /**
     * ✅ Supprime toutes les ressources d'un cours (pour suppression en cascade)
     */
    @Transactional
    public void deleteAllResourcesOfCourse(Long courseId) {
        List<CourseRessource> resources = resourceRepository.findByCourse_Id(courseId);
        resourceRepository.deleteAll(resources);
    }

    /**
     * ✅ Calcule la taille totale approximative (basé sur le nombre de fichiers)
     * Note: Pour avoir la vraie taille, il faudrait stocker file_size dans la BDD
     */
    public Map<String, Object> getResourcesSummary(Long courseId) {
        List<CourseRessource> resources = resourceRepository.findByCourse_Id(courseId);

        Map<String, Object> summary = new HashMap<>();
        summary.put("totalCount", resources.size());
        summary.put("types", getResourceStatsByType(courseId));
        summary.put("hasAudio", resources.stream().anyMatch(r -> "AUDIO".equals(r.getType())));
        summary.put("hasVideo", resources.stream().anyMatch(r -> "VIDEO".equals(r.getType())));
        summary.put("hasDocuments", resources.stream().anyMatch(r ->
                "PDF".equals(r.getType()) || "WORD".equals(r.getType()) || "PPT".equals(r.getType())
        ));

        return summary;
    }

    /**
     * ✅ Récupère l'icône Bootstrap Icon appropriée pour un type de fichier
     */
    public String getIconForType(String type) {
        if (type == null) return "bi-file-earmark";

        return switch (type.toUpperCase()) {
            case "PDF" -> "bi-file-earmark-pdf";
            case "WORD" -> "bi-file-earmark-word";
            case "PPT" -> "bi-file-earmark-slides";
            case "EXCEL" -> "bi-file-earmark-excel";
            case "IMAGE" -> "bi-file-earmark-image";
            case "VIDEO" -> "bi-camera-video";
            case "AUDIO" -> "bi-file-earmark-music";
            case "ARCHIVE" -> "bi-file-earmark-zip";
            default -> "bi-file-earmark";
        };
    }

    /**
     * ✅ Récupère la couleur de badge appropriée pour un type
     */
    public String getBadgeColorForType(String type) {
        if (type == null) return "secondary";

        return switch (type.toUpperCase()) {
            case "PDF" -> "danger";
            case "WORD" -> "primary";
            case "PPT" -> "warning";
            case "EXCEL" -> "success";
            case "IMAGE" -> "info";
            case "VIDEO" -> "danger";
            case "AUDIO" -> "purple";
            case "ARCHIVE" -> "dark";
            default -> "secondary";
        };
    }
}