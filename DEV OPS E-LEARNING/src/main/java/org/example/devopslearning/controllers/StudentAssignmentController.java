package org.example.devopslearning.controllers;

import lombok.RequiredArgsConstructor;
import org.example.devopslearning.entities.Assignment;
import org.example.devopslearning.entities.AssignmentSubmission;
import org.example.devopslearning.entities.User;
import org.example.devopslearning.services.AssignmentService;
import org.example.devopslearning.services.StudentAssignmentService;
import org.example.devopslearning.services.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 🎓 CONTRÔLEUR DEVOIRS ÉTUDIANT - COMPLET
 */
@Controller
@RequestMapping("/student/assignments")
@RequiredArgsConstructor
public class StudentAssignmentController {

    private final AssignmentService assignmentService;
    private final StudentAssignmentService studentAssignmentService;
    private final UserService userService;

    // ========================================
    // LISTE GLOBALE DES DEVOIRS
    // ========================================

    /**
     * Liste TOUS les devoirs de l'étudiant (tous cours)
     */
    @GetMapping
    public String listAll(Authentication auth, Model model) {
        User student = userService.findByEmail(auth.getName());

        // Récupérer tous les devoirs accessibles
        List<Assignment> allAssignments = studentAssignmentService.getAssignmentsForStudent(student.getId());

        // Récupérer les soumissions de l'étudiant
        List<AssignmentSubmission> submissions = assignmentService.submissionsForStudent(student.getId());

        // Créer une map assignmentId -> submission
        Map<Long, AssignmentSubmission> submissionMap = submissions.stream()
                .collect(Collectors.toMap(
                        s -> s.getAssignment().getId(),
                        s -> s,
                        (s1, s2) -> s1.getSubmittedAt().isAfter(s2.getSubmittedAt()) ? s1 : s2));

        // Catégoriser les devoirs
        Instant now = Instant.now();

        List<Assignment> toSubmit = allAssignments.stream()
                .filter(a -> !submissionMap.containsKey(a.getId()) && a.getDueDate().isAfter(now))
                .collect(Collectors.toList());

        List<Assignment> submitted = allAssignments.stream()
                .filter(a -> submissionMap.containsKey(a.getId()) && submissionMap.get(a.getId()).getGrade() == null)
                .collect(Collectors.toList());

        List<Assignment> graded = allAssignments.stream()
                .filter(a -> submissionMap.containsKey(a.getId()) && submissionMap.get(a.getId()).getGrade() != null)
                .collect(Collectors.toList());

        List<Assignment> late = allAssignments.stream()
                .filter(a -> !submissionMap.containsKey(a.getId()) && a.getDueDate().isBefore(now))
                .collect(Collectors.toList());

        model.addAttribute("toSubmit", toSubmit);
        model.addAttribute("submitted", submitted);
        model.addAttribute("graded", graded);
        model.addAttribute("late", late);
        model.addAttribute("submissionMap", submissionMap);

        return "assignments/student-assignments-list";
    }

    /**
     * Liste des devoirs d'un cours spécifique
     */
    @GetMapping("/course/{courseId}")
    public String listByCourse(@PathVariable Long courseId,
            Authentication auth,
            Model model) {
        User student = userService.findByEmail(auth.getName());

        List<Assignment> assignments = studentAssignmentService.getAssignmentsForStudentByCourse(
                student.getId(), courseId);

        List<AssignmentSubmission> submissions = assignmentService.submissionsForStudent(student.getId());

        Map<Long, AssignmentSubmission> submissionMap = submissions.stream()
                .collect(Collectors.toMap(
                        s -> s.getAssignment().getId(),
                        s -> s,
                        (s1, s2) -> s1.getSubmittedAt().isAfter(s2.getSubmittedAt()) ? s1 : s2));

        model.addAttribute("assignments", assignments);
        model.addAttribute("submissionMap", submissionMap);
        model.addAttribute("courseId", courseId);

        return "assignments/student-list";
    }

    // ========================================
    // DÉTAILS D'UN DEVOIR
    // ========================================

    /**
     * Affiche les détails d'un devoir + formulaire de soumission
     */
    @GetMapping("/{assignmentId}")
    public String details(@PathVariable Long assignmentId,
            Authentication auth,
            Model model,
            RedirectAttributes ra) {
        try {
            User student = userService.findByEmail(auth.getName());
            Assignment assignment = studentAssignmentService.getAssignmentById(assignmentId);

            // Vérifier que l'étudiant a accès à ce devoir
            if (!studentAssignmentService.canAccessAssignment(student.getId(), assignmentId)) {
                ra.addFlashAttribute("error", "Vous n'avez pas accès à ce devoir");
                return "redirect:/student/assignments";
            }

            // Récupérer la soumission si elle existe
            AssignmentSubmission submission = studentAssignmentService.getSubmission(
                    student.getId(), assignmentId);

            // Calculer le temps restant
            Instant now = Instant.now();
            boolean isLate = assignment.getDueDate().isBefore(now);
            boolean canSubmit = !isLate || submission == null;

            model.addAttribute("assignment", assignment);
            model.addAttribute("submission", submission);
            model.addAttribute("isLate", isLate);
            model.addAttribute("canSubmit", canSubmit);

            return "assignments/student-assignment-details";

        } catch (Exception e) {
            ra.addFlashAttribute("error", "Erreur : " + e.getMessage());
            return "redirect:/student/assignments";
        }
    }

    // ========================================
    // SOUMISSION D'UN DEVOIR
    // ========================================

    /**
     * Soumettre un devoir (upload fichier)
     */
    @PostMapping("/{assignmentId}/submit")
    public String submit(@PathVariable Long assignmentId,
            @RequestParam("file") MultipartFile file,
            Authentication auth,
            RedirectAttributes ra) {
        try {
            User student = userService.findByEmail(auth.getName());

            // Vérifier que l'étudiant a accès
            if (!studentAssignmentService.canAccessAssignment(student.getId(), assignmentId)) {
                ra.addFlashAttribute("error", "Vous n'avez pas accès à ce devoir");
                return "redirect:/student/assignments";
            }

            // Vérifier qu'un fichier est fourni
            if (file.isEmpty()) {
                ra.addFlashAttribute("error", "Veuillez sélectionner un fichier");
                return "redirect:/student/assignments/" + assignmentId;
            }

            // Vérifier la taille du fichier (10 MB max)
            if (file.getSize() > 10 * 1024 * 1024) {
                ra.addFlashAttribute("error", "Le fichier est trop volumineux (max 10 MB)");
                return "redirect:/student/assignments/" + assignmentId;
            }

            // Soumettre le devoir
            assignmentService.submitAssignment(assignmentId, student, file);

            ra.addFlashAttribute("success", "Devoir soumis avec succès !");
            return "redirect:/student/assignments/" + assignmentId;

        } catch (Exception e) {
            ra.addFlashAttribute("error", "Erreur lors de la soumission : " + e.getMessage());
            return "redirect:/student/assignments/" + assignmentId;
        }
    }

    /**
     * Re-soumettre un devoir (si autorisé)
     */
    @PostMapping("/{assignmentId}/resubmit")
    public String resubmit(@PathVariable Long assignmentId,
            @RequestParam("file") MultipartFile file,
            Authentication auth,
            RedirectAttributes ra) {
        try {
            User student = userService.findByEmail(auth.getName());

            // Vérifier l'accès
            if (!studentAssignmentService.canAccessAssignment(student.getId(), assignmentId)) {
                ra.addFlashAttribute("error", "Vous n'avez pas accès à ce devoir");
                return "redirect:/student/assignments";
            }

            // Vérifier qu'un fichier est fourni
            if (file.isEmpty()) {
                ra.addFlashAttribute("error", "Veuillez sélectionner un fichier");
                return "redirect:/student/assignments/" + assignmentId;
            }

            // Re-soumettre
            studentAssignmentService.resubmitAssignment(assignmentId, student.getId(), file);

            ra.addFlashAttribute("success", "Devoir re-soumis avec succès !");
            return "redirect:/student/assignments/" + assignmentId;

        } catch (Exception e) {
            ra.addFlashAttribute("error", "Erreur : " + e.getMessage());
            return "redirect:/student/assignments/" + assignmentId;
        }
    }

    // ========================================
    // TÉLÉCHARGEMENT
    // ========================================

    /**
     * Télécharger sa propre soumission
     */
    @GetMapping("/submission/{submissionId}/download")
    public String downloadSubmission(@PathVariable Long submissionId,
            Authentication auth,
            RedirectAttributes ra) {
        try {
            User student = userService.findByEmail(auth.getName());
            AssignmentSubmission submission = studentAssignmentService.getSubmissionById(submissionId);

            // Vérifier que c'est bien la soumission de l'étudiant
            if (!submission.getStudent().getId().equals(student.getId())) {
                ra.addFlashAttribute("error", "Accès non autorisé");
                return "redirect:/student/assignments";
            }

            // Rediriger vers l'URL S3
            return "redirect:" + submission.getFileUrl();

        } catch (Exception e) {
            ra.addFlashAttribute("error", "Erreur : " + e.getMessage());
            return "redirect:/student/assignments";
        }
    }
}