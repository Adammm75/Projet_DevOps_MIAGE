package org.example.devopslearning.services;

import lombok.RequiredArgsConstructor;
import org.example.devopslearning.entities.*;
import org.example.devopslearning.repositories.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AbsenceJustificationService {

    private final AbsenceJustificationRepository justificationRepository;
    private final SessionAttendanceRepository    attendanceRepository;
    private final UserRepository                 userRepository;
    private final MessagingService               messagingService;

    // ================================================================
    // ÉTUDIANT : Soumettre un justificatif
    // ================================================================
    @Transactional
    public AbsenceJustification submit(Long attendanceId, User student, String reason, String fileUrl) {

        // Vérifier que l'absence appartient bien à cet étudiant
        SessionAttendance attendance = attendanceRepository.findById(attendanceId)
                .orElseThrow(() -> new RuntimeException("Absence introuvable"));

        if (!attendance.getStudent().getId().equals(student.getId())) {
            throw new RuntimeException("Accès non autorisé");
        }

        // Vérifier qu'un justificatif n'existe pas déjà
        if (justificationRepository.existsByAttendanceId(attendanceId)) {
            throw new RuntimeException("Un justificatif a déjà été soumis pour cette absence");
        }

        AbsenceJustification j = new AbsenceJustification();
        j.setAttendance(attendance);
        j.setStudent(student);
        j.setReason(reason);
        j.setFileUrl(fileUrl);
        j.setStatus(AbsenceJustification.JustificationStatus.PENDING);
        j.setSubmittedAt(Instant.now());

        AbsenceJustification saved = justificationRepository.save(j);

        // Notifier les admins par messagerie interne
        List<User> admins = userRepository.findByRoleName("ROLE_ADMIN");
        for (User admin : admins) {
            try {
                messagingService.sendMessage(
                        student,
                        admin.getEmail(),
                        null,
                        "📋 Justificatif d'absence — " + student.getFirstName() + " " + student.getLastName(),
                        buildNotificationBody(student, attendance, reason)
                );
            } catch (Exception ignored) {}
        }

        return saved;
    }

    // ================================================================
    // ADMIN : Approuver ou refuser
    // ================================================================
    @Transactional
    public AbsenceJustification review(Long justificationId, User admin,
                                       boolean approved, String adminComment) {

        AbsenceJustification j = justificationRepository.findById(justificationId)
                .orElseThrow(() -> new RuntimeException("Justificatif introuvable"));

        j.setStatus(approved
                ? AbsenceJustification.JustificationStatus.APPROVED
                : AbsenceJustification.JustificationStatus.REJECTED);
        j.setAdminComment(adminComment);
        j.setReviewedBy(admin);
        j.setReviewedAt(Instant.now());

        // Si approuvé → passer l'absence en PRESENT
        if (approved) {
            SessionAttendance attendance = j.getAttendance();
            attendance.setStatus(SessionAttendance.AttendanceStatus.PRESENT);
            attendanceRepository.save(attendance);
        }

        AbsenceJustification saved = justificationRepository.save(j);

        // Notifier l'étudiant
        try {
            String emoji = approved ? "✅" : "❌";
            String result = approved ? "approuvé" : "refusé";
            messagingService.sendMessage(
                    admin,
                    j.getStudent().getEmail(),
                    null,
                    emoji + " Votre justificatif a été " + result,
                    buildResponseBody(j, approved, adminComment, admin)
            );
        } catch (Exception ignored) {}

        return saved;
    }

    // ================================================================
    // GETTERS
    // ================================================================
    public List<AbsenceJustification> getAllForAdmin() {
        return justificationRepository.findAllByOrderBySubmittedAtDesc();
    }

    public List<AbsenceJustification> getPendingForAdmin() {
        return justificationRepository.findByStatusOrderBySubmittedAtDesc(
                AbsenceJustification.JustificationStatus.PENDING);
    }

    public List<AbsenceJustification> getForStudent(Long studentId) {
        return justificationRepository.findByStudentIdOrderBySubmittedAtDesc(studentId);
    }

    public AbsenceJustification getById(Long id) {
        return justificationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Justificatif introuvable"));
    }

    public long countPending() {
        return justificationRepository.countByStatus(AbsenceJustification.JustificationStatus.PENDING);
    }

    // ================================================================
    // Récupérer les absences/retards d'un étudiant (pour le formulaire)
    // ================================================================
    public List<SessionAttendance> getAbsencesForStudent(Long studentId) {
        return attendanceRepository.findByStudentIdAndStatusIn(
                studentId,
                List.of(SessionAttendance.AttendanceStatus.ABSENT,
                        SessionAttendance.AttendanceStatus.LATE)
        );
    }

    public boolean alreadySubmitted(Long attendanceId) {
        return justificationRepository.existsByAttendanceId(attendanceId);
    }

    // ================================================================
    // Messages
    // ================================================================
    private String buildNotificationBody(User student, SessionAttendance attendance, String reason) {
        return "Un étudiant a soumis un justificatif d'absence.\n\n" +
                "Étudiant  : " + student.getFirstName() + " " + student.getLastName() + "\n" +
                "Email     : " + student.getEmail() + "\n" +
                "Cours     : " + attendance.getSession().getCourse().getCode() + " — " +
                attendance.getSession().getCourse().getTitle() + "\n" +
                "Séance    : " + attendance.getSession().getTitle() + "\n" +
                "Statut    : " + attendance.getStatus() + "\n\n" +
                "Motif :\n" + reason + "\n\n" +
                "Rendez-vous dans Administration → Justificatifs pour traiter cette demande.";
    }

    private String buildResponseBody(AbsenceJustification j, boolean approved,
                                     String comment, User admin) {
        String result = approved ? "approuvé ✅" : "refusé ❌";
        return "Votre justificatif d'absence a été " + result + ".\n\n" +
                "Cours   : " + j.getAttendance().getSession().getCourse().getTitle() + "\n" +
                "Séance  : " + j.getAttendance().getSession().getTitle() + "\n\n" +
                (comment != null && !comment.isBlank() ? "Commentaire de l'administration :\n" + comment + "\n\n" : "") +
                (approved ? "Votre absence a été convertie en présence." :
                        "Votre absence reste enregistrée.") + "\n\n" +
                "— " + admin.getFirstName() + " " + admin.getLastName();
    }
}