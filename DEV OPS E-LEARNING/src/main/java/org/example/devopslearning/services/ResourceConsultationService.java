package org.example.devopslearning.services;

import lombok.RequiredArgsConstructor;
import org.example.devopslearning.entities.CourseRessource;
import org.example.devopslearning.entities.ResourceConsultation;
import org.example.devopslearning.entities.User;
import org.example.devopslearning.repositories.CourseResourceRepository;
import org.example.devopslearning.repositories.ResourceConsultationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ResourceConsultationService {

    private final ResourceConsultationRepository consultationRepository;
    private final CourseResourceRepository resourceRepository;

    /**
     * Enregistre (ou met à jour) la consultation d'une ressource par un étudiant.
     * Appelé dès que l'étudiant ouvre/télécharge la ressource.
     */
    @Transactional
    public void markAsConsulted(Long resourceId, User student) {
        CourseRessource resource = resourceRepository.findById(resourceId)
                .orElseThrow(() -> new RuntimeException("Ressource introuvable"));

        consultationRepository.findByResourceAndStudent(resource, student)
                .ifPresentOrElse(
                        existing -> {
                            // Déjà consultée : on incrémente le compteur
                            existing.setLastSeenAt(Instant.now());
                            existing.setViewCount(existing.getViewCount() + 1);
                            consultationRepository.save(existing);
                        },
                        () -> {
                            // Première consultation
                            ResourceConsultation c = new ResourceConsultation();
                            c.setResource(resource);
                            c.setStudent(student);
                            consultationRepository.save(c);
                        }
                );
    }

    /**
     * Retourne les IDs des ressources déjà consultées par l'étudiant dans un cours.
     * Utilisé côté étudiant pour afficher les indicateurs visuels.
     */
    public Set<Long> getConsultedResourceIds(Long studentId, Long courseId) {
        return consultationRepository.findConsultedResourceIdsByStudentAndCourse(studentId, courseId);
    }

    /**
     * Nombre de ressources consultées par l'étudiant dans un cours.
     */
    public long countConsulted(Long studentId, Long courseId) {
        return consultationRepository.countConsultedByStudentInCourse(studentId, courseId);
    }

    /**
     * Retourne une map resourceId → nombre d'étudiants distincts ayant consulté.
     * Utilisé côté enseignant pour voir le taux de consultation par ressource.
     */
    public Map<Long, Long> getConsultationCountPerResource(Long courseId) {
        List<Object[]> rows = consultationRepository.countStudentsPerResourceInCourse(courseId);
        Map<Long, Long> result = new HashMap<>();
        for (Object[] row : rows) {
            result.put((Long) row[0], (Long) row[1]);
        }
        return result;
    }
}