package org.example.devopslearning.services;

import lombok.RequiredArgsConstructor;
import org.example.devopslearning.entities.*;
import org.example.devopslearning.repositories.*;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CourseProgressService {

    private final CourseProgressRepository progressRepository;
    private final CourseResourceRepository resourceRepository;
    private final AssignmentRepository assignmentRepository;
    private final QcmRepository qcmRepository;
    private final QcmTentativeRepository qcmTentativeRepository;

    private static final double RESOURCE_WEIGHT = 0.3;
    private static final double ASSIGNMENT_WEIGHT = 0.4;
    private static final double QCM_WEIGHT = 0.3;

    public CourseProgress updateProgress(User user, Cours course) {
        

        double resourceScore = calculateResourceScore(course);
        double assignmentScore = calculateAssignmentScore(course);
        double qcmScore = calculateQcmScore(user, course);

        double finalScore =
                (RESOURCE_WEIGHT * resourceScore) +
                (ASSIGNMENT_WEIGHT * assignmentScore) +
                (QCM_WEIGHT * qcmScore);

        CourseProgress progress = progressRepository
                .findByUserIdAndCourseId(user.getId(), course.getId())
                .orElse(new CourseProgress());

        progress.setUser(user);
        progress.setCourse(course);
        progress.setProgressPercentage(Math.min(finalScore, 100));
        progress.setQcmAverage(qcmScore);
        progress.setCompleted(finalScore >= 80);
        progress.setLastUpdated(Instant.now());

        return progressRepository.save(progress);
    }

    private double calculateResourceScore(Cours course) {
        long total = resourceRepository.countByCourse(course);
        if (total == 0) return 0;

        // Pour l’instant on considère tout vu
        return 100;
    }

    private double calculateAssignmentScore(Cours course) {
        long total = assignmentRepository.countByCourse(course);
        if (total == 0) return 0;

        long graded = assignmentRepository.countGradedAssignmentsByCourse(course.getId());

        return ((double) graded / total) * 100;
    }

    private double calculateQcmScore(User user, Cours course) {
        List<Qcm> qcms = qcmRepository.findByCoursId(course.getId());

        if (qcms.isEmpty()) return 0;

        BigDecimal totalScore = BigDecimal.ZERO;  // ✅ totalScore en BigDecimal
        int count = 0;

        for (Qcm qcm : qcms) {
            List<QcmTentative> tentatives =
                    qcmTentativeRepository
                            .findByQcmIdAndEtudiantIdOrderByDateDebutDesc(
                                    qcm.getId(),
                                    user.getId()
                            );

            if (!tentatives.isEmpty()) {
                // ✅ Ajouter le score de la dernière tentative
                BigDecimal score = tentatives.get(0).getScore(); // suppose que getScore() retourne BigDecimal
                totalScore = totalScore.add(score);             // 🔹 .add() au lieu de +=
                count++;
            }
        }

        return count == 0 ? 0 : totalScore.divide(new BigDecimal(count)).doubleValue();
    }
}