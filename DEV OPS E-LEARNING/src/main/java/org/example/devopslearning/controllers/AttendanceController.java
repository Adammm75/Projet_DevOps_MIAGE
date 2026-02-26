package org.example.devopslearning.controllers;

import lombok.RequiredArgsConstructor;
import org.example.devopslearning.entities.*;
import org.example.devopslearning.repositories.*;
import org.example.devopslearning.entities.AcademicClass;
import org.example.devopslearning.services.*;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class AttendanceController {

    private final AttendanceService          attendanceService;
    private final UserService                userService;
    private final CoursService               coursService;
    private final MessagingService           messagingService;
    private final CourseSessionRepository    sessionRepository;
    private final AttendanceReportRepository reportRepository;
    private final CoursClassRepository       coursClassRepository;
    private final TeacherClassRepository      teacherClassRepository;
    private final UserRepository             userRepository;

    // ================================================================
    // TEACHER : Historique
    // ================================================================
    @GetMapping("/teacher/attendance")
    public String teacherList(Authentication auth, Model model) {
        User teacher = userService.findByEmail(auth.getName());
        model.addAttribute("reports", attendanceService.getReportsForTeacher(teacher.getId()));
        return "teacher/teacher-attendance-list";
    }

    // ================================================================
    // TEACHER : Formulaire nouvelle fiche d'appel
    // ================================================================
    @GetMapping("/teacher/attendance/new")
    public String newForm(Authentication auth, Model model) {
        User teacher = userService.findByEmail(auth.getName());

        // 1. Classes auxquelles l'enseignant est affecté
        List<org.example.devopslearning.entities.AcademicClass> teacherClasses =
                teacherClassRepository.findByTeacherId(teacher.getId()).stream()
                        .map(tc -> tc.getClasse())
                        .collect(Collectors.toList());

        // 2. Pour chaque classe : cours associés via cours_classes
        // Map<classeId, List<Cours>>
        Map<Long, List<Cours>> classCourses = new LinkedHashMap<>();
        for (var tc : teacherClasses) {
            List<Cours> cours = coursClassRepository.findByClasseId(tc.getId()).stream()
                    .map(cc -> cc.getCours())
                    .collect(Collectors.toList());
            if (!cours.isEmpty()) {
                classCourses.put(tc.getId(), cours);
            }
        }

        // 3. Tous les cours de l'enseignant (fallback)
        List<Cours> allCourses = coursService.getCoursesByTeacher(teacher);

        model.addAttribute("teacherClasses", teacherClasses);
        model.addAttribute("classCourses", classCourses);
        model.addAttribute("allCourses", allCourses);
        return "teacher/teacher-attendance-new";
    }

    // ================================================================
    // TEACHER : Créer séance → redirect vers feuille d'appel
    // ================================================================
    @PostMapping("/teacher/attendance/new")
    public String createSession(@RequestParam Long courseId,
                                @RequestParam String sessionTitle,
                                @RequestParam String sessionType,
                                @RequestParam String sessionDate,
                                @RequestParam(required = false) String description,
                                Authentication auth,
                                RedirectAttributes ra) {
        try {
            User teacher = userService.findByEmail(auth.getName());
            LocalDateTime ldt = LocalDateTime.parse(sessionDate,
                    DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"));
            Instant date = ldt.atZone(ZoneId.systemDefault()).toInstant();

            if (courseId == null || courseId <= 0) {
                ra.addFlashAttribute("error", "Cours invalide. Veuillez sélectionner un cours.");
                return "redirect:/teacher/attendance/new";
            }

            Cours cours = coursService.getById(courseId);
            if (cours == null) {
                ra.addFlashAttribute("error", "Cours introuvable (id=" + courseId + "). Veuillez sélectionner un cours valide.");
                return "redirect:/teacher/attendance/new";
            }

            CourseSession session = new CourseSession();
            session.setCourse(cours);
            session.setTitle("[" + sessionType + "] " + sessionTitle);
            session.setSessionDate(date);
            session.setDescription(description);
            session.setCreatedAt(Instant.now());
            CourseSession saved = sessionRepository.save(session);

            return "redirect:/teacher/attendance/" + saved.getId();
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Erreur : " + e.getMessage());
            return "redirect:/teacher/attendance/new";
        }
    }

    // ================================================================
    // TEACHER : Feuille d'appel
    // ================================================================
    @GetMapping("/teacher/attendance/{sessionId}")
    public String form(@PathVariable Long sessionId, Authentication auth, Model model) {
        User teacher = userService.findByEmail(auth.getName());
        CourseSession session = attendanceService.getSession(sessionId);
        List<AttendanceService.AttendanceRowDTO> rows = attendanceService.buildAttendanceRows(sessionId);

        boolean alreadyDone = attendanceService.isAttendanceDone(sessionId);
        attendanceService.getReport(sessionId).ifPresent(r -> model.addAttribute("report", r));

        model.addAttribute("session", session);
        model.addAttribute("rows", rows);
        model.addAttribute("alreadyDone", alreadyDone);
        model.addAttribute("teacher", teacher);
        model.addAttribute("totalRows", rows.size());
        return "teacher/teacher-attendance-form";
    }

    // ================================================================
    // TEACHER : Valider l'appel
    // ================================================================
    @PostMapping("/teacher/attendance/{sessionId}")
    public String submit(@PathVariable Long sessionId,
                         @RequestParam Map<String, String> params,
                         Authentication auth,
                         RedirectAttributes ra) {
        User teacher = userService.findByEmail(auth.getName());

        Map<Long, String> statuses = new HashMap<>();
        Map<Long, String> notes    = new HashMap<>();

        for (Map.Entry<String, String> e : params.entrySet()) {
            if (e.getKey().startsWith("status_")) {
                Long sid = Long.parseLong(e.getKey().substring(7));
                statuses.put(sid, e.getValue());
            } else if (e.getKey().startsWith("note_") && !e.getValue().isBlank()) {
                Long sid = Long.parseLong(e.getKey().substring(5));
                notes.put(sid, e.getValue());
            }
        }

        if (statuses.isEmpty()) {
            ra.addFlashAttribute("error", "Aucun étudiant enregistré.");
            return "redirect:/teacher/attendance/" + sessionId;
        }

        AttendanceReport report = attendanceService.submitAttendance(
                sessionId, teacher, statuses, notes);

        ra.addFlashAttribute("success",
                "✅ Appel validé ! Taux : " + report.getPresenceRate() +
                        "% — Cliquez « Envoyer à l'admin » pour transmettre le rapport.");
        return "redirect:/teacher/attendance/" + sessionId;
    }

    // ================================================================
    // TEACHER : Envoyer rapport aux admins
    // ================================================================
    @PostMapping("/teacher/attendance/{sessionId}/send")
    public String sendReport(@PathVariable Long sessionId,
                             Authentication auth,
                             RedirectAttributes ra) {
        User teacher = userService.findByEmail(auth.getName());
        CourseSession session = attendanceService.getSession(sessionId);
        AttendanceReport report = reportRepository.findBySessionId(sessionId)
                .orElseThrow(() -> new RuntimeException("Rapport introuvable"));

        List<User> admins = userRepository.findByRoleName("ROLE_ADMIN");
        if (admins.isEmpty()) {
            ra.addFlashAttribute("error", "Aucun administrateur trouvé.");
            return "redirect:/teacher/attendance/" + sessionId;
        }

        int sent = 0;
        for (User admin : admins) {
            try {
                messagingService.sendMessage(
                        teacher,
                        admin.getEmail(),
                        session.getCourse().getId(),
                        buildSubject(report, session),
                        buildBody(report, session, teacher)
                );
                sent++;
            } catch (Exception ignored) {}
        }

        report.setSentToAdmin(true);
        report.setSentAt(Instant.now());
        reportRepository.save(report);

        ra.addFlashAttribute("success",
                "📨 Rapport envoyé à " + sent + " administrateur(s) avec succès.");
        return "redirect:/teacher/attendance/" + sessionId;
    }

    // ================================================================
    // ADMIN : Liste globale
    // ================================================================
    @GetMapping("/admin/attendance")
    public String adminList(Model model) {
        List<AttendanceReport> all    = attendanceService.getAllReports();
        List<AttendanceReport> alerts = attendanceService.getAlertReports();
        model.addAttribute("reports", all);
        model.addAttribute("alerts", alerts);
        model.addAttribute("alertCount", alerts.size());
        return "admin/admin-attendance";
    }

    // ================================================================
    // ADMIN : Détail rapport
    // ================================================================
    @GetMapping("/admin/attendance/{reportId}")
    public String adminDetail(@PathVariable Long reportId, Model model) {
        AttendanceReport report = reportRepository.findById(reportId)
                .orElseThrow(() -> new RuntimeException("Rapport introuvable"));
        List<AttendanceService.AttendanceRowDTO> rows =
                attendanceService.buildAttendanceRows(report.getSession().getId());
        model.addAttribute("report", report);
        model.addAttribute("rows", rows);
        return "admin/admin-attendance-detail";
    }

    // ================================================================
    // Builders message admin
    // ================================================================
    private String buildSubject(AttendanceReport r, CourseSession s) {
        String emoji = switch (r.getAlertLevel()) {
            case "CRITICAL" -> "🔴";
            case "WARNING"  -> "🟡";
            default         -> "🟢";
        };
        return emoji + " Rapport d'appel — " + s.getCourse().getCode() + " · " + s.getTitle();
    }

    private String buildBody(AttendanceReport r, CourseSession s, User teacher) {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
                .withZone(ZoneId.systemDefault());
        return "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n" +
                "  RAPPORT D'APPEL OFFICIEL\n" +
                "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n\n" +
                "Cours      : " + s.getCourse().getCode() + " — " + s.getCourse().getTitle() + "\n" +
                "Séance     : " + s.getTitle() + "\n" +
                "Date       : " + fmt.format(s.getSessionDate()) + "\n" +
                "Enseignant : " + teacher.getFirstName() + " " + teacher.getLastName() + "\n\n" +
                "─────────────────────────────────\n" +
                "Présents   : " + r.getPresentCount() + " / " + r.getTotalStudents() + "\n" +
                "Absents    : " + r.getAbsentCount() + "\n" +
                "Retards    : " + r.getLateCount() + "\n" +
                "Taux       : " + r.getPresenceRate() + "%\n" +
                "Alerte     : " + r.getAlertLevel() + "\n" +
                (!"NORMAL".equals(r.getAlertLevel())
                        ? "\n⚠️ Taux de présence " +
                        ("CRITICAL".equals(r.getAlertLevel()) ? "CRITIQUE" : "INSUFFISANT") +
                        " — action recommandée.\n"
                        : "") +
                "\n━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n" +
                "Généré le : " + fmt.format(Instant.now()) + "\n";
    }
}