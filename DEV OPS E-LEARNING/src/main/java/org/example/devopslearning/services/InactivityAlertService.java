package org.example.devopslearning.services;

import lombok.RequiredArgsConstructor;
import org.example.devopslearning.entities.Cours;
import org.example.devopslearning.entities.InactivityAlert;
import org.example.devopslearning.entities.User;
import org.example.devopslearning.repositories.CoursRepository;
import org.example.devopslearning.repositories.InactivityAlertRepository;
import org.example.devopslearning.repositories.StudentActivityRepository;
import org.example.devopslearning.repositories.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class InactivityAlertService {

    private final UserRepository userRepository;
    private final CoursRepository coursRepository;
    private final StudentActivityRepository activityRepository;
    private final InactivityAlertRepository alertRepository;
    private final NotificationService notificationService;
    // private final EmailService emailService;

    private final int DEFAULT_DAYS = 7;

    /**
     * Méthode principale appelée par le scheduler
     */
    public void runInactivityCheck(int daysThreshold) {

        // 1️⃣ Récupérer tous les étudiants
        List<User> students = userRepository.findByRoleName("ROLE_STUDENT");
        Instant now = Instant.now();

        for (User student : students) {

            Long studentId = student.getId();

            // 2️⃣ Récupérer les cours auxquels l'étudiant est inscrit
            List<Cours> courses = coursRepository.findCoursesForStudent(studentId);

            for (Cours course : courses) {

                Long courseId = course.getId();

                // 3️⃣ Récupérer la dernière activité dans ce cours
                var acts = activityRepository.findByStudentAndCourseOrderByActivityTimeDesc(studentId, courseId);
                Instant lastActivity = acts.isEmpty() ? null : acts.get(0).getActivityTime();

                long daysInactive = (lastActivity == null)
                        ? daysThreshold + 1
                        : Duration.between(lastActivity, now).toDays();

                // 4️⃣ Vérifier s'il existe déjà une alerte ouverte
                List<InactivityAlert> existing = alertRepository
                        .findByStudent_IdAndCourse_IdAndStatus(studentId, courseId, "OPEN");

                // 5️⃣ Cas : étudiant inactif
                if (lastActivity == null || daysInactive >= daysThreshold) {

                    if (existing.isEmpty()) {

                        // ➤ Créer l'alerte
                        InactivityAlert alert = alertRepository.save(
                                InactivityAlert.builder()
                                        .student(student)
                                        .course(course)
                                        .daysInactive((int) daysInactive)
                                        .lastActivityAt(lastActivity)
                                        .status("OPEN")
                                        .build()
                        );

                        // ➤ Récupérer les professeurs du cours
                        List<User> teachers = coursRepository.findTeachersForCourse(courseId);

                        // ✅ MESSAGE PERSONNALISÉ POUR L'ÉTUDIANT
                        String titleStudent = "⚠️ Tu es inactif(ve) dans un cours";
                        String contentStudent = String.format(
                                "Tu es inactif(ve) depuis %d jours dans le cours \"%s\". " +
                                        "N'oublie pas de consulter les ressources et de participer aux activités !",
                                daysInactive, course.getTitle()
                        );

                        notificationService.createNotification(
                                studentId, "INACTIVITY_ALERT",
                                titleStudent, contentStudent, courseId, alert.getId()
                        );

                        // ✅ MESSAGE PERSONNALISÉ POUR LES ENSEIGNANTS
                        String titleTeacher = "⚠️ Alerte d'inactivité : " + student.getFirstName() + " " + student.getLastName();
                        String contentTeacher = String.format(
                                "L'étudiant %s %s est inactif depuis %d jours dans le cours \"%s\". " +
                                        "Pensez à le contacter pour l'encourager.",
                                student.getFirstName(), student.getLastName(), daysInactive, course.getTitle()
                        );

                        teachers.forEach(t ->
                                notificationService.createNotification(
                                        t.getId(), "INACTIVITY_ALERT",
                                        titleTeacher, contentTeacher, courseId, alert.getId()
                                )
                        );

                        // ➤ Envoi d'e‑mails (commenté)
                        /*teachers.forEach(t ->
                                emailService.sendEmail(
                                        t.getEmail(),
                                        "Alerte d'inactivité - " + student.getFirstName(),
                                        contentTeacher
                                )
                        );

                        emailService.sendEmail(
                                student.getEmail(),
                                "Tu es inactif dans le cours " + course.getTitle(),
                                contentStudent
                        );*/
                    }

                } else {
                    // 6️⃣ Cas : étudiant redevenu actif → fermer les alertes
                    if (!existing.isEmpty()) {

                        for (InactivityAlert alert : existing) {

                            alert.setStatus("CLOSED");
                            alert.setClosedAt(Instant.now());
                            alert.setDaysInactive((int) daysInactive);
                            alertRepository.save(alert);

                            // ✅ MESSAGE PERSONNALISÉ POUR L'ÉTUDIANT (résolution)
                            String titleStudent = "✅ Alerte résolue - Bravo !";
                            String contentStudent = String.format(
                                    "Bravo ! Tu as repris ton activité dans le cours \"%s\". Continue comme ça ! 🎉",
                                    course.getTitle()
                            );

                            notificationService.createNotification(
                                    studentId, "INACTIVITY_RESOLVED",
                                    titleStudent, contentStudent, courseId, alert.getId()
                            );

                            // ✅ MESSAGE PERSONNALISÉ POUR LES ENSEIGNANTS (résolution)
                            List<User> teachers = coursRepository.findTeachersForCourse(courseId);
                            String titleTeacher = "✅ Reprise d'activité : " + student.getFirstName() + " " + student.getLastName();
                            String contentTeacher = String.format(
                                    "L'étudiant %s %s a repris son activité dans le cours \"%s\". " +
                                            "L'alerte d'inactivité a été automatiquement fermée.",
                                    student.getFirstName(), student.getLastName(), course.getTitle()
                            );

                            teachers.forEach(t ->
                                    notificationService.createNotification(
                                            t.getId(), "INACTIVITY_RESOLVED",
                                            titleTeacher, contentTeacher, courseId, alert.getId()
                                    )
                            );

                            // ➤ Envoi d'e‑mails (commenté)
                            /*emailService.sendEmail(
                                    student.getEmail(),
                                    "Alerte résolue",
                                    contentStudent
                            );

                            teachers.forEach(t ->
                                    emailService.sendEmail(
                                            t.getEmail(),
                                            "Alerte résolue pour " + student.getFirstName(),
                                            contentTeacher
                                    )
                            );*/
                        }
                    }
                }
            }
        }
    }

    public void runInactivityCheckDefault() {
        runInactivityCheck(DEFAULT_DAYS);
    }

    public List<InactivityAlert> findOpenAlertsForTeacher(Long teacherId) {
        List<Cours> taught = coursRepository.findCoursesByTeacherId(teacherId);
        return alertRepository.findByStatus("OPEN").stream()
                .filter(a -> taught.stream().anyMatch(c -> c.getId().equals(a.getCourse().getId())))
                .toList();
    }

    public List<InactivityAlert> findAlertsForStudent(Long studentId) {
        return alertRepository.findAll().stream()
                .filter(a -> a.getStudent().getId().equals(studentId))
                .toList();
    }

    public void markAlertAsHandled(Long alertId, Long handlerUserId) {
        InactivityAlert alert = alertRepository.findById(alertId)
                .orElseThrow(() -> new RuntimeException("Alert not found"));

        alert.setStatus("CLOSED");
        alert.setClosedAt(Instant.now());
        alert.setHandledBy(userRepository.findById(handlerUserId).orElse(null));

        alertRepository.save(alert);
    }
}