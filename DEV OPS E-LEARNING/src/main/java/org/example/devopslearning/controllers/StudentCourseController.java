package org.example.devopslearning.controllers;

import lombok.RequiredArgsConstructor;
import org.example.devopslearning.entities.*;
import org.example.devopslearning.services.CourseAccessService;
import org.example.devopslearning.services.CoursService;
import org.example.devopslearning.services.StudentAssignmentService;
import org.example.devopslearning.services.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/**
 * 📚 CONTRÔLEUR COURS ÉTUDIANT - COMPLET
 */
@Controller
@RequestMapping("/student/courses")
@RequiredArgsConstructor
public class StudentCourseController {

    private final CourseAccessService courseAccessService;
    private final UserService userService;
    private final CoursService coursService;
    private final StudentAssignmentService studentAssignmentService;

    // ========================================
    // LISTE DES COURS
    // ========================================

    /**
     * Liste tous les cours accessibles par l'étudiant
     */
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

    /**
     * Affiche les détails complets d'un cours avec onglets
     * (Aperçu, Ressources, Devoirs, QCM)
     */
    @GetMapping("/{courseId}")
    public String details(@PathVariable Long courseId,
                          @RequestParam(defaultValue = "overview") String tab,
                          Authentication auth,
                          Model model,
                          RedirectAttributes ra) {
        try {
            User student = userService.findByEmail(auth.getName());

            // Sécurité : empêche accès à un cours non autorisé
            if (!courseAccessService.canAccessCourse(student.getId(), courseId)) {
                ra.addFlashAttribute("error", "Vous n'avez pas accès à ce cours");
                return "redirect:/student/courses";
            }

            Cours course = courseAccessService.getCourseDetails(courseId);

            // Charger les données selon l'onglet actif
            model.addAttribute("course", course);
            model.addAttribute("activeTab", tab);

            // Ressources
            List<CourseRessource> resources = coursService.getResources(courseId);
            model.addAttribute("resources", resources);
            model.addAttribute("resourceCount", resources.size());

            // Devoirs
            List<Assignment> assignments = studentAssignmentService.getAssignmentsForStudentByCourse(
                    student.getId(), courseId
            );
            model.addAttribute("assignments", assignments);
            model.addAttribute("assignmentCount", assignments.size());

            // QCM (si implémenté)
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
    // RESSOURCES
    // ========================================

    /**
     * Télécharger une ressource
     */
    @GetMapping("/{courseId}/resources/{resourceId}/download")
    public String downloadResource(@PathVariable Long courseId,
                                   @PathVariable Long resourceId,
                                   Authentication auth,
                                   RedirectAttributes ra) {
        try {
            User student = userService.findByEmail(auth.getName());

            // Vérifier l'accès au cours
            if (!courseAccessService.canAccessCourse(student.getId(), courseId)) {
                ra.addFlashAttribute("error", "Accès non autorisé");
                return "redirect:/student/courses";
            }

            // Récupérer l'URL de la ressource
            CourseRessource resource = coursService.getResourceById(resourceId);

            // Vérifier que la ressource appartient au bon cours
            if (!resource.getCourse().getId().equals(courseId)) {
                ra.addFlashAttribute("error", "Ressource invalide");
                return "redirect:/student/courses/" + courseId;
            }

            // Rediriger vers l'URL S3
            return "redirect:" + resource.getUrl();

        } catch (Exception e) {
            ra.addFlashAttribute("error", "Erreur : " + e.getMessage());
            return "redirect:/student/courses/" + courseId;
        }
    }

    /**
     * Prévisualiser une ressource (pour PDF)
     */
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

            model.addAttribute("course", course);
            model.addAttribute("resource", resource);

            return "courses/resource-preview";

        } catch (Exception e) {
            ra.addFlashAttribute("error", "Erreur : " + e.getMessage());
            return "redirect:/student/courses/" + courseId;
        }
    }

    // ========================================
    // SÉANCES (si implémenté)
    // ========================================

    /**
     * Liste des séances d'un cours
     */
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

            // TODO: Implémenter récupération des séances
            // List<CourseSession> sessions = courseSessionService.getSessionsByCourse(courseId);

            model.addAttribute("course", course);
            // model.addAttribute("sessions", sessions);

            return "courses/student-course-sessions";

        } catch (Exception e) {
            ra.addFlashAttribute("error", "Erreur : " + e.getMessage());
            return "redirect:/student/courses/" + courseId;
        }
    }
}