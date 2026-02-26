package org.example.devopslearning.services;

import lombok.RequiredArgsConstructor;
import org.example.devopslearning.entities.Cours;
import org.example.devopslearning.entities.User;
import org.example.devopslearning.entities.UserCourseCompletion;
import org.example.devopslearning.repositories.CoursRepository;
import org.example.devopslearning.repositories.UserCourseCompletionRepository;
import org.example.devopslearning.repositories.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class CourseCompletionService {

    private final UserCourseCompletionRepository completionRepository;
    private final UserRepository userRepository;
    private final CoursRepository coursRepository;

    /**
     * Bascule le statut terminé/non terminé (toggle).
     * Retourne true si le cours est maintenant marqué terminé.
     */
    @Transactional
    public boolean toggleCompletion(Long userId, Long courseId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));
        Cours course = coursRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Cours introuvable"));

        UserCourseCompletion completion = completionRepository
                .findByUserAndCourse(user, course)
                .orElseGet(() -> {
                    UserCourseCompletion c = new UserCourseCompletion();
                    c.setUser(user);
                    c.setCourse(course);
                    return c;
                });

        boolean newStatus = !completion.isCompleted();
        completion.setCompleted(newStatus);
        completion.setCompletedAt(newStatus ? Instant.now() : null);
        completionRepository.save(completion);

        return newStatus;
    }

    /**
     * L'étudiant a-t-il marqué ce cours comme terminé ?
     */
    public boolean isCompleted(Long userId, Long courseId) {
        return completionRepository.isCompletedByStudent(userId, courseId);
    }

    /**
     * Nombre d'étudiants ayant marqué le cours comme terminé.
     * Utilisé côté enseignant.
     */
    public long countCompleted(Long courseId) {
        return completionRepository.countCompletedByCourse(courseId);
    }
}