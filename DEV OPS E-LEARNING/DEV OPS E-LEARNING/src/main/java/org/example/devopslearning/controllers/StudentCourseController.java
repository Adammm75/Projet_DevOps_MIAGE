package org.example.devopslearning.controllers;

import lombok.RequiredArgsConstructor;
import org.example.devopslearning.entities.*;
import org.example.devopslearning.services.*;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Set;

@Controller
@RequestMapping("/student/courses")
@RequiredArgsConstructor
public class StudentCourseController {

    private final CourseAccessService courseAccessService;
    private final UserService userService;
    private final CoursService coursService;
    private final StudentAssignmentService studentAssignmentService;
    private final ResourceConsultationService consultationService;
    private final CourseCompletionService completionService;

    // ========================================
    // LISTE DES COURS
    // ========================================

    @GetMapping
    public String list(Authentication auth, Model model) {
        User student = userService.findByEmail(auth.getName());
        List<Cours> cours = courseAccessService.getCoursAccessibles(student.getId());
        model.addAttribute("courses", cours);
        model.addAttribute("totalCourses", cours.size());
        return "courses/student-course-list";
    }

    // ========================================
    // DÉTAILS D'UN COURS
    // ========================================

    @GetMapping("/{courseId}")
    public String details(@PathVariable Long courseId,
                          @RequestParam(defaultValue = "overview") String tab,
                          Authentication auth,
                          Model model,
                          RedirectAttributes ra) {
        try {
            User student = userService.findByEmail(auth.getName());

            if (!courseAccessService.canAccessCourse(student.getId(), courseId)) {
                ra.addFlashAttribute("error", "Vous n'avez pas accès à ce cours");
                return "redirect:/student/courses";
            }

            Cours course = courseAccessService.getCourseDetails(courseId);
            model.addAttribute("course", course);
            model.addAttribute("activeTab", tab);

            // Ressources
            List<CourseRessource> resources = coursService.getResources(courseId);
            model.addAttribute("resources", resources);
            model.addAttribute("resourceCount", resources.size());

            // IDs des ressources consultées + compteur de progression
            Set<Long> consultedIds = consultationService.getConsultedResourceIds(student.getId(), courseId);
            long consultedCount = consultationService.countConsulted(student.getId(), courseId);
            model.addAttribute("consultedResourceIds", consultedIds);
            model.addAttribute("consultedCount", consultedCount);

            // Statut cours terminé
            boolean isCourseCompleted = completionService.isCompleted(student.getId(), courseId);
            model.addAttribute("isCourseCompleted", isCourseCompleted);

            // Devoirs
            List<Assignment> assignments = studentAssignmentService
                    .getAssignmentsForStudentByCourse(student.getId(), courseId);
            model.addAttribute("assignments", assignments);
            model.addAttribute("assignmentCount", assignments.size());

            // QCM
            List<Qcm> qcms = courseAccessService.getQcms(courseId);
            model.addAttribute("qcms", qcms);
            model.addAttribute("qcmCount", qcms != null ? qcms.size() : 0);

            return "courses/student-course-details";

        } catch (Exception e) {
            ra.addFlashAttribute("error", "Erreur : " + e.getMessage());
            return "redirect:/student/courses";
        }
    }

    // ========================================
    // MARQUEUR COURS TERMINÉ
    // ========================================

    @PostMapping("/{courseId}/toggle-completion")
    public String toggleCompletion(@PathVariable Long courseId,
                                   Authentication auth,
                                   RedirectAttributes ra) {
        try {
            User student = userService.findByEmail(auth.getName());
            boolean isNowCompleted = completionService.toggleCompletion(student.getId(), courseId);

            if (isNowCompleted) {
                ra.addFlashAttribute("completionSuccess",
                        "Bravo ! Tu as marqué ce cours comme terminé. Tu peux y revenir quand tu veux. 🎉");
            } else {
                ra.addFlashAttribute("completionInfo",
                        "Statut réinitialisé. Continue à ton rythme !");
            }
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Erreur : " + e.getMessage());
        }
        return "redirect:/student/courses/" + courseId;
    }

    // ========================================
    // RESSOURCES — TÉLÉCHARGEMENT (avec log)
    // ========================================

    @GetMapping("/{courseId}/resources/{resourceId}/download")
    public String downloadResource(@PathVariable Long courseId,
                                   @PathVariable Long resourceId,
                                   Authentication auth,
                                   RedirectAttributes ra) {
        try {
            User student = userService.findByEmail(auth.getName());

            if (!courseAccessService.canAccessCourse(student.getId(), courseId)) {
                ra.addFlashAttribute("error", "Accès non autorisé");
                return "redirect:/student/courses";
            }

            CourseRessource resource = coursService.getResourceById(resourceId);

            if (!resource.getCourse().getId().equals(courseId)) {
                ra.addFlashAttribute("error", "Ressource invalide");
                return "redirect:/student/courses/" + courseId;
            }

            consultationService.markAsConsulted(resourceId, student);

            return "redirect:" + resource.getUrl();

        } catch (Exception e) {
            ra.addFlashAttribute("error", "Erreur : " + e.getMessage());
            return "redirect:/student/courses/" + courseId;
        }
    }

    @GetMapping("/{courseId}/resources/{resourceId}/preview")
    public String previewResource(@PathVariable Long courseId,
                                  @PathVariable Long resourceId,
                                  Authentication auth,
                                  Model model,
                                  RedirectAttributes ra) {
        try {
            User student = userService.findByEmail(auth.getName());

            if (!courseAccessService.canAccessCourse(student.getId(), courseId)) {
                ra.addFlashAttribute("error", "Accès non autorisé");
                return "redirect:/student/courses";
            }

            CourseRessource resource = coursService.getResourceById(resourceId);
            Cours course = coursService.getById(courseId);

            consultationService.markAsConsulted(resourceId, student);

            model.addAttribute("course", course);
            model.addAttribute("resource", resource);

            return "courses/resource-preview";

        } catch (Exception e) {
            ra.addFlashAttribute("error", "Erreur : " + e.getMessage());
            return "redirect:/student/courses/" + courseId;
        }
    }

    // ========================================
    // SÉANCES
    // ========================================

    @GetMapping("/{courseId}/sessions")
    public String sessions(@PathVariable Long courseId,
                           Authentication auth,
                           Model model,
                           RedirectAttributes ra) {
        try {
            User student = userService.findByEmail(auth.getName());

            if (!courseAccessService.canAccessCourse(student.getId(), courseId)) {
                ra.addFlashAttribute("error", "Accès non autorisé");
                return "redirect:/student/courses";
            }

            Cours course = coursService.getById(courseId);
            model.addAttribute("course", course);

            return "courses/student-course-sessions";

        } catch (Exception e) {
            ra.addFlashAttribute("error", "Erreur : " + e.getMessage());
            return "redirect:/student/courses/" + courseId;
        }
    }
}