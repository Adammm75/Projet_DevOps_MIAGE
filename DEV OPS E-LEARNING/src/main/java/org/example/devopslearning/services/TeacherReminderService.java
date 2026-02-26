package org.example.devopslearning.services;

import lombok.RequiredArgsConstructor;
import org.example.devopslearning.entities.*;
import org.example.devopslearning.repositories.*;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 🔔 Service de rappels automatiques pour enseignants.
 *
 * 3 types de rappels :
 *   1. Devoirs avec soumissions non corrigées (peu importe la date d'échéance)
 *   2. Messages non lus
 *   3. Étudiants inactifs (aucune consultation depuis N jours)
 */
@Service
@RequiredArgsConstructor
public class TeacherReminderService {

    private final AssignmentRepository           assignmentRepository;
    private final AssignmentSubmissionRepository  submissionRepository;
    private final MessageRepository               messageRepository;
    private final CoursClassRepository            coursClassRepository;
    private final InscriptionsClassRepository     inscriptionsRepository;
    private final ResourceConsultationRepository  consultationRepository;
    private final CourseResourceRepository        resourceRepository;

    private static final int INACTIVITY_DAYS        = 7;
    private static final int CRITICAL_PENDING_COUNT  = 5;
    private static final int CRITICAL_UNREAD_COUNT   = 5;
    private static final DateTimeFormatter DATE_FMT  = DateTimeFormatter
            .ofPattern("dd/MM/yyyy HH:mm").withZone(ZoneId.of("Europe/Paris"));

    // ========================================
    // POINT D'ENTRÉE
    // ========================================

    public List<ReminderDTO> getRemindersForTeacher(User teacher) {
        List<ReminderDTO> reminders = new ArrayList<>();
        reminders.addAll(buildUncorrectedAssignmentReminders(teacher));
        reminders.addAll(buildUnreadMessageReminders(teacher));
        reminders.addAll(buildInactiveStudentReminders(teacher));

        // Trier : CRITIQUE → ATTENTION → INFO
        reminders.sort(Comparator
                .comparingInt((ReminderDTO r) -> r.getLevel().getOrder())
                .thenComparing(ReminderDTO::getDate));

        return reminders;
    }

    public long countReminders(User teacher) {
        return getRemindersForTeacher(teacher).size();
    }

    public long countCritical(User teacher) {
        return getRemindersForTeacher(teacher).stream()
                .filter(r -> r.getLevel() == ReminderLevel.CRITIQUE)
                .count();
    }

    // ========================================
    // 1. DEVOIRS NON CORRIGÉS
    // ✅ CORRIGÉ : on cherche TOUS les devoirs de l'enseignant
    //    (passés ET futurs) qui ont des soumissions sans note
    // ========================================

    private List<ReminderDTO> buildUncorrectedAssignmentReminders(User teacher) {
        // Récupère TOUS les devoirs de l'enseignant (via created_by ET via course.createdBy)
        // pour couvrir les deux cas possibles en base
        Set<Assignment> allAssignments = new LinkedHashSet<>();
        allAssignments.addAll(assignmentRepository.findByCreatedBy(teacher));
        allAssignments.addAll(assignmentRepository.findByTeacherId(teacher.getId()));

        Instant now = Instant.now();
        List<ReminderDTO> reminders = new ArrayList<>();

        for (Assignment assignment : allAssignments) {
            // Chercher les soumissions non notées (grade IS NULL)
            List<AssignmentSubmission> pending =
                    submissionRepository.findPendingGradesByAssignmentId(assignment.getId());

            if (pending.isEmpty()) continue;

            long uncorrected = pending.size();
            boolean isOverdue = assignment.getDueDate().isBefore(now);
            long daysDiff = ChronoUnit.DAYS.between(
                    isOverdue ? assignment.getDueDate() : now,
                    isOverdue ? now : assignment.getDueDate()
            );

            // Niveau d'urgence
            ReminderLevel level;
            if (isOverdue && (uncorrected > CRITICAL_PENDING_COUNT || daysDiff > 14))
                level = ReminderLevel.CRITIQUE;
            else if (isOverdue && daysDiff > 7)
                level = ReminderLevel.ATTENTION;
            else
                level = ReminderLevel.INFO;

            // Message contextuel selon si l'échéance est passée ou future
            String message;
            if (isOverdue) {
                message = uncorrected + " soumission(s) non corrigée(s) — échéance passée le "
                        + DATE_FMT.format(assignment.getDueDate())
                        + " (il y a " + daysDiff + " jour(s))";
            } else {
                message = uncorrected + " soumission(s) déjà reçue(s) — échéance le "
                        + DATE_FMT.format(assignment.getDueDate())
                        + " (dans " + daysDiff + " jour(s))";
            }

            reminders.add(new ReminderDTO(
                    ReminderType.DEVOIR_NON_CORRIGE,
                    level,
                    "Devoir à corriger : « " + assignment.getTitle() + " »",
                    message,
                    assignment.getCourse().getCode(),
                    assignment.getCourse().getTitle(),
                    assignment.getDueDate(),
                    "/teacher/assignments/course/" + assignment.getCourse().getId()
            ));
        }

        return reminders;
    }

    // ========================================
    // 2. MESSAGES NON LUS
    // ========================================

    private List<ReminderDTO> buildUnreadMessageReminders(User teacher) {
        long unread = messageRepository.countByRecipientIdAndIsReadFalse(teacher.getId());
        if (unread == 0) return List.of();

        ReminderLevel level = unread > CRITICAL_UNREAD_COUNT
                ? ReminderLevel.CRITIQUE : ReminderLevel.ATTENTION;

        return List.of(new ReminderDTO(
                ReminderType.MESSAGE_NON_LU,
                level,
                "Messages non lus",
                unread + " message(s) en attente de lecture dans votre boîte de réception.",
                null,
                null,
                Instant.now(),
                "/messages"
        ));
    }

    // ========================================
    // 3. ÉTUDIANTS INACTIFS
    // ========================================

    private List<ReminderDTO> buildInactiveStudentReminders(User teacher) {
        // Cours de l'enseignant via les deux chemins
        Set<Cours> coursesSet = new LinkedHashSet<>();
        assignmentRepository.findByCreatedBy(teacher).stream()
                .map(Assignment::getCourse).forEach(coursesSet::add);
        assignmentRepository.findByTeacherId(teacher.getId()).stream()
                .map(Assignment::getCourse).forEach(coursesSet::add);

        Instant since = Instant.now().minus(INACTIVITY_DAYS, ChronoUnit.DAYS);
        List<ReminderDTO> reminders = new ArrayList<>();

        for (Cours course : coursesSet) {
            Set<Long> allStudentIds = new LinkedHashSet<>();
            coursClassRepository.findByCoursId(course.getId()).forEach(cc ->
                    inscriptionsRepository.findByClasseId(cc.getClasse().getId())
                            .forEach(ins -> allStudentIds.add(ins.getEtudiant().getId()))
            );
            if (allStudentIds.isEmpty()) continue;
            if (resourceRepository.countByCourse_Id(course.getId()) == 0) continue;

            Set<Long> activeIds = consultationRepository
                    .findActiveStudentIdsSince(course.getId(), since);

            long inactiveCount = allStudentIds.stream()
                    .filter(id -> !activeIds.contains(id))
                    .count();

            if (inactiveCount == 0) continue;

            long total = allStudentIds.size();
            int inactiveRate = (int) Math.round((double) inactiveCount / total * 100);

            ReminderLevel level;
            if (inactiveRate >= 50)      level = ReminderLevel.CRITIQUE;
            else if (inactiveRate >= 25) level = ReminderLevel.ATTENTION;
            else                         level = ReminderLevel.INFO;

            reminders.add(new ReminderDTO(
                    ReminderType.ETUDIANT_INACTIF,
                    level,
                    "Étudiants inactifs — " + course.getCode(),
                    inactiveCount + "/" + total + " étudiant(s) sans activité depuis "
                            + INACTIVITY_DAYS + " jours (" + inactiveRate + "% de la classe).",
                    course.getCode(),
                    course.getTitle(),
                    since,
                    "/teacher/progression?courseId=" + course.getId() + "&filter=INACTIF"
            ));
        }

        return reminders;
    }

    // ========================================
    // ENUMS & DTO
    // ========================================

    public enum ReminderType {
        DEVOIR_NON_CORRIGE,
        MESSAGE_NON_LU,
        ETUDIANT_INACTIF
    }

    public enum ReminderLevel {
        CRITIQUE(0, "danger",  "bi-exclamation-octagon-fill"),
        ATTENTION(1, "warning", "bi-exclamation-triangle-fill"),
        INFO(2,     "info",    "bi-info-circle-fill");

        private final int order;
        private final String colorClass;
        private final String icon;

        ReminderLevel(int order, String colorClass, String icon) {
            this.order = order;
            this.colorClass = colorClass;
            this.icon = icon;
        }

        public int    getOrder()        { return order; }
        public String getColorClass()   { return colorClass; }
        public String getIcon()         { return icon; }
    }

    public static class ReminderDTO {
        private final ReminderType  type;
        private final ReminderLevel level;
        private final String        title;
        private final String        message;
        private final String        courseCode;
        private final String        courseTitle;
        private final Instant       date;
        private final String        actionUrl;

        public ReminderDTO(ReminderType type, ReminderLevel level,
                           String title, String message,
                           String courseCode, String courseTitle,
                           Instant date, String actionUrl) {
            this.type        = type;
            this.level       = level;
            this.title       = title;
            this.message     = message;
            this.courseCode  = courseCode;
            this.courseTitle = courseTitle;
            this.date        = date;
            this.actionUrl   = actionUrl;
        }

        public ReminderType  getType()        { return type; }
        public ReminderLevel getLevel()       { return level; }
        public String        getTitle()       { return title; }
        public String        getMessage()     { return message; }
        public String        getCourseCode()  { return courseCode; }
        public String        getCourseTitle() { return courseTitle; }
        public Instant       getDate()        { return date; }
        public String        getActionUrl()   { return actionUrl; }
    }
}