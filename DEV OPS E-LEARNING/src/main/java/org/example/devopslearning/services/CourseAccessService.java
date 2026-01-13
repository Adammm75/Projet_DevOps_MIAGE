package org.example.devopslearning.services;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.devopslearning.entities.*;
import org.example.devopslearning.repositories.*;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CourseAccessService {

    private final InscriptionsClassRepository inscriptionsRepo;
    private final CoursClassRepository coursClassRepo;
    private final CoursFiliereRepository coursFiliereRepo;
    private final CoursRepository coursRepository;
    private final AssignmentRepository assignmentRepository;
    private final QcmRepository qcmRepository;

    /**
     * Récupère tous les cours accessibles par un étudiant
     */
    public List<Cours> getCoursAccessibles(Long etudiantId) {

        // Classes de l'étudiant
        List<InscriptionsClass> inscriptions =
                inscriptionsRepo.findByEtudiantId(etudiantId);

        Set<Long> classeIds = inscriptions.stream()
                .map(i -> i.getClasse().getId())
                .collect(Collectors.toSet());

        Set<Long> filiereIds = inscriptions.stream()
                .map(i -> i.getClasse().getParcours().getFiliere().getId())
                .collect(Collectors.toSet());

        Set<Long> coursIds = new HashSet<>();

        // Cours par classe
        for (Long classeId : classeIds) {
            coursClassRepo.findByClasseId(classeId)
                    .forEach(cc -> coursIds.add(cc.getCours().getId()));
        }

        // Cours par filière
        for (Long filiereId : filiereIds) {
            for (Cours c : coursRepository.findAll()) {
                if (coursFiliereRepo.existsByCoursIdAndFiliereId(c.getId(), filiereId)) {
                    coursIds.add(c.getId());
                }
            }
        }

        return coursRepository.findAllById(coursIds);
    }

    /**
     * Vérifie si un étudiant peut accéder à un cours
     */
    public boolean canAccess(Long etudiantId, Long coursId) {
        return getCoursAccessibles(etudiantId).stream()
                .anyMatch(c -> c.getId().equals(coursId));
    }

    /**
     * Vérifie si un utilisateur a le rôle ETUDIANT
     */
    public boolean isStudent(User user) {
        return user.hasRole("ETUDIANT");
    }

    /**
     * Vérifie si un étudiant peut accéder à un cours spécifique
     */
    public boolean canAccessCourse(Long etudiantId, Long coursId) {
        return getCoursAccessibles(etudiantId).stream()
                .anyMatch(c -> c.getId().equals(coursId));
    }

    /**
     * Récupère les détails d'un cours
     */
    public Cours getCourseDetails(Long courseId) {
        return coursRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Cours introuvable"));
    }

    /**
     * Récupère tous les devoirs d'un cours
     */
    public List<Assignment> getAssignments(Long courseId) {
        return assignmentRepository.findByCourseId(courseId);
    }

    /**
     * Récupère tous les QCMs d'un cours
     */
    public List<Qcm> getQcms(Long courseId) {
        return qcmRepository.findByCoursIdAndPublie(courseId, true);
    }
}