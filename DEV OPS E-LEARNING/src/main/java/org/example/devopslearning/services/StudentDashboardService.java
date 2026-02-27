package org.example.devopslearning.services;

import lombok.RequiredArgsConstructor;
import org.example.devopslearning.entities.*;
import org.example.devopslearning.repositories.*;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.stream.Collectors;
import java.util.List;
import java.util.Map;
import java.util.Comparator;

@Service
@RequiredArgsConstructor
public class StudentDashboardService {

    private final CourseAccessService courseAccessService;
    private final AssignmentRepository assignmentRepository;
    private final AssignmentSubmissionRepository submissionRepository;
    private final NotesCourRepository notesCourRepository;
    private final MessageRepository messageRepository;
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final TeacherCommentRepository teacherCommentRepository;
    private final SessionAttendanceRepository sessionAttendanceRepository;

    public Map<String, Object> buildDashboard(Long studentId) {

        Map<String, Object> dashboard = new HashMap<>();

        User student = userRepository.findById(studentId)
                .orElseThrow();

        List<Cours> cours = courseAccessService.getCoursAccessibles(studentId);

        // Devoirs à rendre = devoirs non encore soumis par l'étudiant
        List<Assignment> devoirs = cours.stream()
                .flatMap(c -> assignmentRepository.findByCourseId(c.getId()).stream())
                .filter(a -> !submissionRepository.existsByStudentIdAndAssignmentId(studentId, a.getId()))
                .toList();

        List<NotesCour> notes = notesCourRepository.findByEtudiantId(studentId);
        List<Message> messages =
                messageRepository.findTop5ByRecipientOrderBySentAtDesc(student);

        List<Notification> notifications =
                notificationRepository.findByUserIdAndIsReadFalse(studentId);

        // Commentaires pédagogiques visibles par l'étudiant (TEACHER_ADMIN uniquement)
        List<org.example.devopslearning.entities.TeacherComment> comments =
                teacherCommentRepository.findVisibleByStudentId(studentId);
        long newCommentsCount = comments.stream()
                .filter(c -> c.getCreatedAt() != null &&
                        c.getCreatedAt().isAfter(java.time.Instant.now().minus(7, java.time.temporal.ChronoUnit.DAYS)))
                .count();

        // Moyenne générale
        Double averageGrade = notes.isEmpty() ? null :
                notes.stream()
                        .filter(n -> n.getNoteFinale() != null)
                        .mapToDouble(n -> n.getNoteFinale().doubleValue())
                        .average()
                        .stream().boxed().findFirst().orElse(null);

        dashboard.put("cours", cours);
        dashboard.put("devoirs", devoirs);
        dashboard.put("notes", notes);
        dashboard.put("messages", messages);
        dashboard.put("notifications", notifications);
        dashboard.put("pedagogicComments", comments);
        dashboard.put("newCommentsCount", newCommentsCount);
        dashboard.put("averageGrade", averageGrade);

        // Absences et retards récents (30 derniers jours)
        java.time.Instant since = java.time.Instant.now().minus(30, java.time.temporal.ChronoUnit.DAYS);
        List<SessionAttendance> recentAttendances = sessionAttendanceRepository
                .findRecentByStudentId(studentId, since);

        // Garder seulement ABSENT et LATE, triés par date décroissante
        List<SessionAttendance> absencesRetards = recentAttendances.stream()
                .filter(a -> a.getStatus() == SessionAttendance.AttendanceStatus.ABSENT
                        || a.getStatus() == SessionAttendance.AttendanceStatus.LATE)
                .sorted(Comparator.comparing(SessionAttendance::getMarkedAt).reversed())
                .limit(5)
                .collect(Collectors.toList());

        dashboard.put("absencesRetards", absencesRetards);
        dashboard.put("absencesCount", absencesRetards.stream()
                .filter(a -> a.getStatus() == SessionAttendance.AttendanceStatus.ABSENT).count());
        dashboard.put("retardsCount", absencesRetards.stream()
                .filter(a -> a.getStatus() == SessionAttendance.AttendanceStatus.LATE).count());

        return dashboard;
    }
}
