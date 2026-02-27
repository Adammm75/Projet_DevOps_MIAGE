package org.example.devopslearning.controllers;

import lombok.RequiredArgsConstructor;
import org.example.devopslearning.entities.Assignment;
import org.example.devopslearning.entities.AssignmentSubmission;
import org.example.devopslearning.entities.Cours;
import org.example.devopslearning.entities.User;
import org.example.devopslearning.services.CoursService;
import org.example.devopslearning.services.TeacherAssignmentService;
import org.example.devopslearning.services.UserService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.example.devopslearning.entities.AcademicClass;
import org.example.devopslearning.entities.AssignmentClass;
import org.example.devopslearning.entities.TeacherClass;
import org.example.devopslearning.repositories.AssignmentClassRepository;
import org.example.devopslearning.repositories.TeacherClassRepository;
import java.util.stream.Collectors;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/teacher/assignments")
@RequiredArgsConstructor
public class TeacherAssignmentController {

    private final TeacherAssignmentService teacherAssignmentService;
    private final CoursService coursService;
    private final UserService userService;
    private final AssignmentClassRepository assignmentClassRepository; // ⭐ AJOUTÉ
    private final TeacherClassRepository teacherClassRepository; // ⭐ AJOUTÉ

    // ========================================
    // 1️⃣ LISTE GLOBALE DES DEVOIRS (tous cours)
    // ========================================
    @GetMapping
    public String listAllAssignments(@AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false) Long courseId,
            Model model) {
        User teacher = userService.findByUsername(userDetails.getUsername());

        List<Assignment> assignments;

        // Si courseId est fourni, filtrer par cours
        if (courseId != null) {
            assignments = teacherAssignmentService.listByCourse(courseId);
            model.addAttribute("course", coursService.getById(courseId));
        } else {
            // Sinon, récupérer tous les devoirs du prof
            assignments = teacherAssignmentService.listByTeacher(teacher);
        }

        // Récupérer tous les cours du prof pour le filtre
        List<Cours> courses = coursService.findByTeacher(teacher);

        // Calculer les statistiques
        long pendingGradingCount = teacherAssignmentService.countPendingSubmissionsByTeacher(teacher);
        long activeCount = assignments.stream()
                .filter(a -> a.getDueDate() != null && a.getDueDate().isAfter(Instant.now()))
                .count();
        long completedCount = assignments.stream()
                .filter(a -> a.getDueDate() != null && a.getDueDate().isBefore(Instant.now()))
                .count();

        model.addAttribute("assignments", assignments);
        model.addAttribute("courses", courses);
        model.addAttribute("pendingGradingCount", pendingGradingCount);
        model.addAttribute("activeCount", activeCount);
        model.addAttribute("completedCount", completedCount);

        return "assignments/teacher-list";
    }

    // ========================================
    // 2️⃣ LISTE DES DEVOIRS D'UN COURS SPÉCIFIQUE
    // ========================================
    @GetMapping("/course/{courseId}")
    public String listCourseAssignments(@PathVariable Long courseId,
            @AuthenticationPrincipal UserDetails userDetails,
            Model model) {
        User teacher = userService.findByUsername(userDetails.getUsername());
        Cours course = coursService.getById(courseId);
        List<Assignment> assignments = teacherAssignmentService.listByCourse(courseId);

        // Récupérer tous les cours du prof pour le filtre
        List<Cours> courses = coursService.findByTeacher(teacher);

        // Calculer les statistiques
        long pendingGradingCount = assignments.stream()
                .flatMap(a -> teacherAssignmentService.listSubmissions(a.getId()).stream())
                .filter(s -> s.getGrade() == null && s.getSubmittedAt() != null)
                .count();

        long activeCount = assignments.stream()
                .filter(a -> a.getDueDate() != null && a.getDueDate().isAfter(Instant.now()))
                .count();

        long completedCount = assignments.stream()
                .filter(a -> a.getDueDate() != null && a.getDueDate().isBefore(Instant.now()))
                .count();

        model.addAttribute("course", course);
        model.addAttribute("courses", courses);
        model.addAttribute("assignments", assignments);
        model.addAttribute("pendingGradingCount", pendingGradingCount);
        model.addAttribute("activeCount", activeCount);
        model.addAttribute("completedCount", completedCount);

        return "assignments/teacher-list";
    }

    // ========================================
    // 3️⃣ FORMULAIRE DE CRÉATION
    // ========================================
    @GetMapping("/new")
    public String newAssignment(@RequestParam(required = false) Long courseId,
            @AuthenticationPrincipal UserDetails userDetails,
            Model model,
            RedirectAttributes ra) {

        // Si courseId n'est pas fourni, rediriger vers la liste avec erreur
        if (courseId == null) {
            ra.addFlashAttribute("error", "Veuillez sélectionner un cours avant de créer un devoir");
            return "redirect:/teacher/assignments";
        }

        try {
            User teacher = userService.findByUsername(userDetails.getUsername());
            Cours course = coursService.getById(courseId);

            // ⭐ RÉCUPÉRER LES CLASSES DE L'ENSEIGNANT
            List<TeacherClass> teacherClasses = teacherClassRepository.findByTeacherId(teacher.getId());
            List<AcademicClass> classes = teacherClasses.stream()
                    .map(TeacherClass::getClasse)
                    .collect(Collectors.toList());

            Assignment assignment = new Assignment();
            assignment.setCourse(course);

            model.addAttribute("assignment", assignment);
            model.addAttribute("course", course);
            model.addAttribute("classes", classes); // ⭐ AJOUTÉ

            return "assignments/create";
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Cours introuvable: " + e.getMessage());
            return "redirect:/teacher/assignments";
        }
    }

    // 4️⃣ MODIFIER LA MÉTHODE createAssignment (ligne 144-170)
    // Ajouter le paramètre classeIds et l'affectation
    @PostMapping
    public String createAssignment(@RequestParam Long courseId,
            @RequestParam String title,
            @RequestParam(required = false) String description,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dueDate,
            @RequestParam(required = false) BigDecimal maxGrade,
            @RequestParam(required = false) List<Long> classeIds, // ⭐ AJOUTÉ
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes) {
        try {
            User teacher = userService.findByUsername(userDetails.getUsername());

            Assignment assignment = teacherAssignmentService.createAssignment(
                    courseId, title, description, dueDate, maxGrade, teacher);

            // ⭐ AFFECTER AUX CLASSES
            if (classeIds != null && !classeIds.isEmpty()) {
                for (Long classeId : classeIds) {
                    AssignmentClass ac = new AssignmentClass();
                    ac.setAssignment(assignment);
                    ac.setClasse(new AcademicClass());
                    ac.getClasse().setId(classeId);
                    assignmentClassRepository.save(ac);
                }
                redirectAttributes.addFlashAttribute("success",
                        "Devoir créé et affecté à " + classeIds.size() + " classe(s) avec succès !");
            } else {
                redirectAttributes.addFlashAttribute("success", "Devoir créé avec succès !");
            }

            return "redirect:/teacher/assignments/course/" + courseId;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur : " + e.getMessage());
            return "redirect:/teacher/assignments/new?courseId=" + courseId;
        }
    }

    // ========================================
    // 5️⃣ FORMULAIRE D'ÉDITION
    // ========================================
    @GetMapping("/{assignmentId}/edit")
    public String editAssignment(@PathVariable Long assignmentId, Model model, RedirectAttributes ra) {
        try {
            Assignment assignment = teacherAssignmentService.getAssignmentById(assignmentId);
            model.addAttribute("assignment", assignment);
            model.addAttribute("course", assignment.getCourse());
            return "assignments/edit";
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Devoir introuvable: " + e.getMessage());
            return "redirect:/teacher/assignments";
        }
    }

    // ========================================
    // 6️⃣ METTRE À JOUR UN DEVOIR
    // ========================================
    @PostMapping("/{assignmentId}/update")
    public String updateAssignment(@PathVariable Long assignmentId,
            @RequestParam String title,
            @RequestParam(required = false) String description,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dueDate,
            @RequestParam BigDecimal maxGrade,
            RedirectAttributes ra) {
        try {
            Assignment assignment = teacherAssignmentService.updateAssignment(
                    assignmentId,
                    title,
                    description,
                    dueDate,
                    maxGrade);

            ra.addFlashAttribute("success", "Devoir modifié avec succès");
            return "redirect:/teacher/assignments/course/" + assignment.getCourse().getId();
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Erreur lors de la modification: " + e.getMessage());
            return "redirect:/teacher/assignments/" + assignmentId + "/edit";
        }
    }

    // ========================================
    // 7️⃣ SUPPRIMER UN DEVOIR
    // ========================================
    @PostMapping("/{assignmentId}/delete")
    public String deleteAssignment(@PathVariable Long assignmentId, RedirectAttributes ra) {
        try {
            Assignment assignment = teacherAssignmentService.getAssignmentById(assignmentId);
            Long courseId = assignment.getCourse().getId();
            String assignmentTitle = assignment.getTitle();

            teacherAssignmentService.deleteAssignment(assignmentId);

            ra.addFlashAttribute("success", "Devoir '" + assignmentTitle + "' supprimé avec succès");
            return "redirect:/teacher/assignments/course/" + courseId;
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Erreur lors de la suppression: " + e.getMessage());
            return "redirect:/teacher/assignments";
        }
    }

    // ========================================
    // 8️⃣ VOIR LES SOUMISSIONS D'UN DEVOIR
    // ========================================
    @GetMapping("/{assignmentId}/submissions")
    public String submissions(@PathVariable Long assignmentId, Model model, RedirectAttributes ra) {
        try {
            Assignment assignment = teacherAssignmentService.getAssignmentById(assignmentId);
            List<AssignmentSubmission> submissions = teacherAssignmentService.listSubmissions(assignmentId);

            // Calculer les statistiques
            long totalSubmissions = submissions.size();
            long ungradedCount = submissions.stream()
                    .filter(s -> s.getGrade() == null && s.getSubmittedAt() != null)
                    .count();
            long gradedCount = submissions.stream()
                    .filter(s -> s.getGrade() != null)
                    .count();

            double averageGrade = 0.0;
            if (gradedCount > 0) {
                averageGrade = submissions.stream()
                        .filter(s -> s.getGrade() != null)
                        .mapToDouble(s -> s.getGrade().doubleValue())
                        .average()
                        .orElse(0.0);
            }

            model.addAttribute("assignment", assignment);
            model.addAttribute("submissions", submissions);
            model.addAttribute("totalSubmissions", totalSubmissions);
            model.addAttribute("ungradedCount", ungradedCount);
            model.addAttribute("gradedCount", gradedCount);
            model.addAttribute("averageGrade", String.format("%.2f", averageGrade));

            return "assignments/submissions";
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Erreur: " + e.getMessage());
            return "redirect:/teacher/assignments";
        }
    }

    // ========================================
    // 9️⃣ FORMULAIRE DE NOTATION (page dédiée)
    // ========================================
    @GetMapping("/submissions/{submissionId}/grade")
    public String gradeForm(@PathVariable Long submissionId, Model model, RedirectAttributes ra) {
        try {
            AssignmentSubmission submission = teacherAssignmentService.getSubmissionById(submissionId);
            Assignment assignment = submission.getAssignment();

            model.addAttribute("submission", submission);
            model.addAttribute("assignment", assignment);

            return "assignments/grade";
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Soumission introuvable: " + e.getMessage());
            return "redirect:/teacher/assignments";
        }
    }

    // ========================================
    // 🔟 NOTER UNE SOUMISSION
    // ========================================
    @PostMapping("/submissions/{submissionId}/grade")
    public String grade(@PathVariable Long submissionId,
            @RequestParam BigDecimal grade,
            @RequestParam(required = false) String feedback,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes ra) {
        try {
            User teacher = userService.findByUsername(userDetails.getUsername());

            // Récupérer la soumission pour validation
            AssignmentSubmission submission = teacherAssignmentService.getSubmissionById(submissionId);
            Assignment assignment = submission.getAssignment();

            // Vérifier que la note est dans les limites
            if (grade.compareTo(BigDecimal.ZERO) < 0 || grade.compareTo(assignment.getMaxGrade()) > 0) {
                ra.addFlashAttribute("error", "La note doit être entre 0 et " + assignment.getMaxGrade());
                return "redirect:/teacher/assignments/" + assignment.getId() + "/submissions";
            }

            teacherAssignmentService.gradeSubmission(submissionId, grade, feedback, teacher);

            ra.addFlashAttribute("success", "Note enregistrée avec succès pour " +
                    submission.getStudent().getFirstName() + " " + submission.getStudent().getLastName());

            return "redirect:/teacher/assignments/" + assignment.getId() + "/submissions";
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Erreur lors de la notation: " + e.getMessage());
            return "redirect:/teacher/assignments";
        }
    }
}