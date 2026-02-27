package org.example.devopslearning.services;

import lombok.RequiredArgsConstructor;
import org.example.devopslearning.entities.*;
import org.example.devopslearning.repositories.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AttendanceService {

    private final CourseSessionRepository      sessionRepository;
    private final SessionAttendanceRepository  attendanceRepository;
    private final AttendanceReportRepository   reportRepository;
    private final UserRepository               userRepository;
    private final CoursClassRepository         coursClassRepository;
    private final InscriptionsClassRepository  inscriptionsRepository;

    // DTO
    public record AttendanceRowDTO(
            User   student,
            String currentStatus,
            long   absencesInCourse,
            String absenceAlert
    ) {}

    // ============================================================
    // Récupérer une séance
    // ============================================================
    @Transactional
    public CourseSession getSession(Long sessionId) {
        CourseSession session = sessionRepository.findByIdWithCourse(sessionId)
                .orElseThrow(() -> new RuntimeException("Séance introuvable : " + sessionId));
        // Force initialization of lazy associations within transaction
        if (session.getCourse() != null) {
            session.getCourse().getCode(); // trigger proxy init
            if (session.getCourse().getCreatedBy() != null) {
                session.getCourse().getCreatedBy().getEmail(); // trigger createdBy proxy init
            }
        }
        return session;
    }

    // ============================================================
    // Construire la liste étudiants avec statuts + alertes absences
    // ============================================================
    @Transactional
    public List<AttendanceRowDTO> buildAttendanceRows(Long sessionId) {
        CourseSession session = getSession(sessionId);
        Long courseId = session.getCourse().getId();

        // Récupérer étudiants via cours_classes → inscriptions_classes
        List<Long> classIds = coursClassRepository.findByCoursId(courseId).stream()
                .map(cc -> cc.getClasse().getId())
                .collect(Collectors.toList());

        Set<Long> studentIds = new LinkedHashSet<>();
        for (Long classId : classIds) {
            inscriptionsRepository.findByClasseId(classId).stream()
                    .map(i -> i.getEtudiant().getId())
                    .forEach(studentIds::add);
        }

        if (studentIds.isEmpty()) return List.of();

        // Présences déjà enregistrées pour cette séance
        Map<Long, String> existingStatus = getAttendanceMap(sessionId);

        return studentIds.stream()
                .map(sid -> userRepository.findById(sid).orElse(null))
                .filter(Objects::nonNull)
                .map(student -> {
                    String status = existingStatus.get(student.getId());
                    long absences = attendanceRepository
                            .countAbsencesByStudentAndCourse(student.getId(), courseId);
                    String alert = absences >= 4 ? "CRITICAL"
                            : absences >= 2 ? "WARNING" : "NORMAL";
                    return new AttendanceRowDTO(student, status, absences, alert);
                })
                .sorted(Comparator.comparing(r -> r.student().getLastName()))
                .collect(Collectors.toList());
    }

    // ============================================================
    // Map sessionId → statuts existants
    // ============================================================
    public Map<Long, String> getAttendanceMap(Long sessionId) {
        return attendanceRepository.findBySessionId(sessionId).stream()
                .collect(Collectors.toMap(
                        a -> a.getStudent().getId(),
                        a -> a.getStatus().name()
                ));
    }

    // ============================================================
    // Vérifier si appel déjà fait
    // ============================================================
    @Transactional(readOnly = true)
    public boolean isAttendanceDone(Long sessionId) {
        return attendanceRepository.existsBySessionId(sessionId);
    }

    // ============================================================
    // Récupérer rapport existant
    // ============================================================
    @Transactional(readOnly = true)
    public Optional<AttendanceReport> getReport(Long sessionId) {
        return reportRepository.findBySessionId(sessionId);
    }

    // ============================================================
    // Soumettre l'appel → sauvegarder + générer rapport
    // ============================================================
    @Transactional
    public AttendanceReport submitAttendance(Long sessionId, User teacher,
                                             Map<Long, String> statuses,
                                             Map<Long, String> notes) {
        CourseSession session = getSession(sessionId);

        // Supprimer l'ancien appel si re-soumission
        attendanceRepository.deleteBySessionId(sessionId);

        int present = 0, absent = 0, late = 0;

        for (Map.Entry<Long, String> e : statuses.entrySet()) {
            User student = userRepository.findById(e.getKey()).orElse(null);
            if (student == null) continue;

            SessionAttendance.AttendanceStatus status;
            try {
                status = SessionAttendance.AttendanceStatus.valueOf(e.getValue());
            } catch (Exception ex) {
                status = SessionAttendance.AttendanceStatus.ABSENT;
            }

            SessionAttendance sa = new SessionAttendance();
            sa.setSession(session);
            sa.setStudent(student);
            sa.setStatus(status);
            sa.setNote(notes.getOrDefault(e.getKey(), null));
            sa.setMarkedAt(Instant.now());
            sa.setMarkedBy(teacher);
            attendanceRepository.save(sa);

            switch (status) {
                case PRESENT -> present++;
                case ABSENT  -> absent++;
                case LATE    -> late++;
            }
        }

        // Supprimer l'ancien rapport
        reportRepository.findBySessionId(sessionId)
                .ifPresent(r -> reportRepository.delete(r));

        // Calculer et sauvegarder le rapport
        int total = present + absent + late;
        double rate = total > 0 ? Math.round((present + late) * 10000.0 / total) / 100.0 : 0.0;
        String alertLevel = rate < 50 ? "CRITICAL" : rate < 75 ? "WARNING" : "NORMAL";

        AttendanceReport report = new AttendanceReport();
        report.setSession(session);
        report.setTeacher(teacher);
        report.setTotalStudents(total);
        report.setPresentCount(present);
        report.setAbsentCount(absent);
        report.setLateCount(late);
        report.setPresenceRate(java.math.BigDecimal.valueOf(rate));
        report.setAlertLevel(alertLevel);
        report.setSentToAdmin(false);
        report.setGeneratedAt(Instant.now());

        return reportRepository.save(report);
    }

    // ============================================================
    // Rapports pour un enseignant
    // ============================================================
    public List<AttendanceReport> getReportsForTeacher(Long teacherId) {
        return reportRepository.findByTeacherIdOrderByGeneratedAtDesc(teacherId);
    }

    // ============================================================
    // Tous les rapports (admin)
    // ============================================================
    public List<AttendanceReport> getAllReports() {
        return reportRepository.findAllByOrderByGeneratedAtDesc();
    }

    // ============================================================
    // Rapports WARNING/CRITICAL (admin)
    // ============================================================
    public List<AttendanceReport> getAlertReports() {
        return reportRepository.findAlertsOrdered();
    }
}