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
import java.time.LocalDateTime;
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
    private final EmailService emailService;

    private final int DEFAULT_DAYS = 7;

    /**
     * Méthode principale appelée par le scheduler
     */
    public void runInactivityCheck(int daysThreshold) {

        // 1️⃣ Récupérer tous les étudiants
        List<User> students = userRepository.findByRoleName("ROLE_STUDENT");
        LocalDateTime now = LocalDateTime.now();

        for (User student : students) {

            Long studentId = student.getId();

            // 2️⃣ Récupérer les cours auxquels l’étudiant est inscrit
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

                        // ➤ Créer l’alerte
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

                        String title = "Alerte d'inactivité : " + student.getFirstName() + " " + student.getLastName();
                        String content = "L'étudiant " + student.getFirstName() + " "
                                + student.getLastName() + " est inactif depuis "
                                + daysInactive + " jours dans le cours " + course.getTitle();

                        // ➤ Notifications internes
                        teachers.forEach(t ->
                                notificationService.createNotification(
                                        t.getId(), "INACTIVITY_ALERT", title, content, courseId, alert.getId()
                                )
                        );

                        notificationService.createNotification(
                                studentId, "INACTIVITY_ALERT",
                                "Tu es inactif(ve)", content, courseId, alert.getId()
                        );

                        // ➤ Envoi d’e‑mails
                        teachers.forEach(t ->
                                emailService.sendEmail(
                                        t.getEmail(),
                                        "Alerte d'inactivité - " + student.getFirstName(),
                                        content
                                )
                        );

                        emailService.sendEmail(
                                student.getEmail(),
                                "Tu es inactif dans le cours " + course.getTitle(),
                                "Tu n'as pas été actif depuis " + daysInactive + " jours. Reviens vite !"
                        );
                    }

                } else {
                    // 6️⃣ Cas : étudiant redevenu actif → fermer les alertes
                    if (!existing.isEmpty()) {

                        for (InactivityAlert alert : existing) {

                            alert.setStatus("CLOSED");
                            alert.setClosedAt(Instant.now());
                            alert.setDaysInactive((int) daysInactive);
                            alertRepository.save(alert);

                            // ➤ Notifications internes
                            notificationService.createNotification(
                                    studentId, "INACTIVITY_RESOLVED",
                                    "Alerte résolue",
                                    "Ton activité a repris.", courseId, alert.getId()
                            );

                            List<User> teachers = coursRepository.findTeachersForCourse(courseId);
                            teachers.forEach(t ->
                                    notificationService.createNotification(
                                            t.getId(), "INACTIVITY_RESOLVED",
                                            "Alerte résolue pour " + student.getFirstName(),
                                            "L'étudiant a repris son activité.", courseId, alert.getId()
                                    )
                            );

                            // ➤ Envoi d’e‑mails
                            emailService.sendEmail(
                                    student.getEmail(),
                                    "Alerte résolue",
                                    "Bravo ! Ton activité a repris dans le cours " + course.getTitle()
                            );

                            teachers.forEach(t ->
                                    emailService.sendEmail(
                                            t.getEmail(),
                                            "Alerte résolue pour " + student.getFirstName(),
                                            "L'étudiant a repris son activité dans le cours " + course.getTitle()
                                    )
                            );
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
