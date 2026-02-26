package org.example.devopslearning.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.devopslearning.entities.*;
import org.example.devopslearning.entities.PointTransaction.PointReason;
import org.example.devopslearning.entities.StudentPoints.AcademicLevel;
import org.example.devopslearning.repositories.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class BadgeEngineService {

    private static final int POINTS_ATTENDANCE        = 10;
    private static final int POINTS_ASSIGNMENT_SUBMIT = 15;
    private static final int POINTS_ASSIGNMENT_BONUS  = 10;
    private static final int POINTS_QCM_SUCCESS       = 20;
    private static final int POINTS_RESOURCE          = 5;

    private static final int    SEUIL_COMPREHENSION       = 100;
    private static final int    SEUIL_MAITRISE            = 250;
    private static final int    SEUIL_BADGE_PARTICIPATION = 5;
    private static final double SEUIL_BADGE_REGULARITE    = 0.80;

    private final StudentPointsRepository    studentPointsRepository;
    private final PointTransactionRepository pointTransactionRepository;
    private final BadgeRepository            badgeRepository;
    private final StudentBadgeRepository     studentBadgeRepository;
    private final UserRepository             userRepository;
    private final CoursRepository            coursRepository;
    private final SessionAttendanceRepository sessionAttendanceRepository;

    @Transactional
    public void onAttendance(Long studentId, Long courseId, Long sessionId) {
        addPoints(studentId, courseId, POINTS_ATTENDANCE, PointReason.ATTENDANCE, sessionId);
        checkAndAwardBadges(studentId, courseId);
    }

    @Transactional
    public void onAssignmentSubmitted(Long studentId, Long courseId, Long assignmentId) {
        addPoints(studentId, courseId, POINTS_ASSIGNMENT_SUBMIT, PointReason.ASSIGNMENT_SUBMITTED, assignmentId);
        checkAndAwardBadges(studentId, courseId);
    }

    @Transactional
    public void onAssignmentGraded(Long studentId, Long courseId, Long submissionId, double grade) {
        if (grade >= 14.0) {
            addPoints(studentId, courseId, POINTS_ASSIGNMENT_BONUS, PointReason.ASSIGNMENT_GRADED, submissionId);
            checkAndAwardBadges(studentId, courseId);
        }
    }

    @Transactional
    public void onQcmCompleted(Long studentId, Long courseId, Long tentativeId, double scorePercent) {
        if (scorePercent >= 70.0) {
            addPoints(studentId, courseId, POINTS_QCM_SUCCESS, PointReason.QCM_SUCCESS, tentativeId);
            checkAndAwardBadges(studentId, courseId);
        }
    }

    @Transactional
    public void onResourceConsulted(Long studentId, Long courseId, Long resourceId) {
        addPoints(studentId, courseId, POINTS_RESOURCE, PointReason.RESOURCE_CONSULTED, resourceId);
        checkAndAwardBadges(studentId, courseId);
    }

    private void addPoints(Long studentId, Long courseId, int points, PointReason reason, Long referenceId) {
        if (referenceId != null &&
                pointTransactionRepository.existsByStudentIdAndCourseIdAndReasonAndReferenceId(
                        studentId, courseId, reason, referenceId)) {
            log.debug("Points déjà attribués : studentId={}, courseId={}, reason={}, ref={}", studentId, courseId, reason, referenceId);
            return;
        }

        User  student = userRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Étudiant introuvable : " + studentId));
        Cours course  = coursRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Cours introuvable : " + courseId));

        StudentPoints sp = studentPointsRepository
                .findByStudentIdAndCourseId(studentId, courseId)
                .orElseGet(() -> StudentPoints.builder()
                        .student(student)
                        .course(course)
                        .totalPoints(0)
                        .academicLevel(AcademicLevel.DECOUVERTE)
                        .build());

        sp.setTotalPoints(sp.getTotalPoints() + points);
        sp.recalculateLevel();
        studentPointsRepository.save(sp);

        PointTransaction tx = PointTransaction.builder()
                .student(student)
                .course(course)
                .points(points)
                .reason(reason)
                .referenceId(referenceId)
                .build();
        pointTransactionRepository.save(tx);

        log.info("+{} pts → studentId={}, courseId={}, raison={}, niveau={}", points, studentId, courseId, reason, sp.getAcademicLevel());
    }

    private void checkAndAwardBadges(Long studentId, Long courseId) {
        StudentPoints sp = studentPointsRepository
                .findByStudentIdAndCourseId(studentId, courseId)
                .orElse(null);
        if (sp == null) return;

        if (sp.getAcademicLevel() == AcademicLevel.MAITRISE) {
            awardBadgeIfNotExists(studentId, courseId, Badge.MAITRISE_COURS);
        }

        long nbPresences = pointTransactionRepository.countAttendanceTransactions(studentId, courseId);
        if (nbPresences >= SEUIL_BADGE_PARTICIPATION) {
            awardBadgeIfNotExists(studentId, courseId, Badge.PARTICIPATION_ACTIVE);
        }

        long totalSessions = sessionAttendanceRepository.countByCourseIdAndStudentId(courseId, studentId);
        if (totalSessions > 0) {
            double tauxPresence = (double) nbPresences / totalSessions;
            if (tauxPresence >= SEUIL_BADGE_REGULARITE) {
                awardBadgeIfNotExists(studentId, courseId, Badge.REGULARITE);
            }
        }
    }

    private void awardBadgeIfNotExists(Long studentId, Long courseId, String badgeCode) {
        Badge badge = badgeRepository.findByCode(badgeCode).orElse(null);
        if (badge == null) {
            log.warn("Badge introuvable en BDD : {}", badgeCode);
            return;
        }

        if (!studentBadgeRepository.existsByStudentIdAndBadgeIdAndCourseId(studentId, badge.getId(), courseId)) {
            User  student = userRepository.getReferenceById(studentId);
            Cours course  = coursRepository.getReferenceById(courseId);

            StudentBadge sb = StudentBadge.builder()
                    .student(student)
                    .badge(badge)
                    .course(course)
                    .build();
            studentBadgeRepository.save(sb);

            log.info("Badge attribué : {} → studentId={}, courseId={}", badgeCode, studentId, courseId);
        }
    }
}