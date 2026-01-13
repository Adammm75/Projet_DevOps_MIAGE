package org.example.devopslearning.controllers;

import lombok.RequiredArgsConstructor;
import org.example.devopslearning.entities.*;
import org.example.devopslearning.repositories.*;
import org.example.devopslearning.services.BulkEnrollmentService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/courses/{courseId}/enrollment")
@RequiredArgsConstructor
public class EnrollmentController {

    private final BulkEnrollmentService bulkEnrollmentService;
    private final CoursRepository coursRepository;
    private final AcademicClassRepository academicClassRepository;
    private final UserRepository userRepository;
    private final CourseTeacherRepository courseTeacherRepository;
    private final CourseEnrollmentRepository enrollmentRepository;
    private final InscriptionsClassRepository inscriptionsClassRepository;  // ✅ AJOUTÉ

    /**
     * Affiche la page de gestion des inscriptions
     */
    @GetMapping
    public String manageEnrollment(@PathVariable Long courseId, Model model, Authentication auth) {
        Cours cours = coursRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Cours introuvable"));

        // ✅ Récupérer toutes les classes disponibles
        List<AcademicClass> allClasses = academicClassRepository.findAll();

        // ✅ Récupérer tous les enseignants
        List<User> teachers = userRepository.findAll().stream()
                .filter(u -> u.hasRole("ROLE_TEACHER") || u.hasRole("ROLE_ADMIN"))
                .collect(Collectors.toList());

        // ✅ Récupérer les enseignants déjà affectés au cours
        List<User> assignedTeachers = courseTeacherRepository.findByCourseId(courseId).stream()
                .map(CourseTeacher::getTeacher)
                .collect(Collectors.toList());

        // ✅ CORRIGÉ : Récupérer les classes déjà inscrites
        List<AcademicClass> enrolledClasses = getEnrolledClasses(courseId);

        model.addAttribute("cours", cours);
        model.addAttribute("classes", allClasses);
        model.addAttribute("teachers", teachers);
        model.addAttribute("assignedTeachers", assignedTeachers);
        model.addAttribute("enrolledClasses", enrolledClasses);

        return "admin/enrollment/manage";
    }

    /**
     * Inscrire une classe complète à un cours
     */
    @PostMapping("/enroll-class")
    public String enrollClass(@PathVariable Long courseId,
                              @RequestParam Long classId,
                              Authentication auth,
                              RedirectAttributes ra) {
        try {
            User admin = userRepository.findByEmail(auth.getName())
                    .orElseThrow(() -> new RuntimeException("Admin introuvable"));

            int count = bulkEnrollmentService.enrollClassToCourse(courseId, classId, admin.getId());

            ra.addFlashAttribute("success", count + " étudiant(s) inscrit(s) avec succès");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Erreur : " + e.getMessage());
        }
        return "redirect:/admin/courses/" + courseId + "/enrollment";
    }

    /**
     * Affecter un enseignant à un cours
     */
    @PostMapping("/assign-teacher")
    public String assignTeacher(@PathVariable Long courseId,
                                @RequestParam Long teacherId,
                                Authentication auth,
                                RedirectAttributes ra) {
        try {
            User admin = userRepository.findByEmail(auth.getName())
                    .orElseThrow(() -> new RuntimeException("Admin introuvable"));

            bulkEnrollmentService.assignTeacherToCourse(courseId, teacherId, admin.getId());

            ra.addFlashAttribute("success", "Enseignant affecté avec succès");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Erreur : " + e.getMessage());
        }
        return "redirect:/admin/courses/" + courseId + "/enrollment";
    }

    /**
     * Retirer un enseignant d'un cours
     */
    @PostMapping("/remove-teacher/{teacherId}")
    public String removeTeacher(@PathVariable Long courseId,
                                @PathVariable Long teacherId,
                                Authentication auth,
                                RedirectAttributes ra) {
        try {
            User admin = userRepository.findByEmail(auth.getName())
                    .orElseThrow(() -> new RuntimeException("Admin introuvable"));

            bulkEnrollmentService.removeTeacherFromCourse(courseId, teacherId, admin.getId());

            ra.addFlashAttribute("success", "Enseignant retiré avec succès");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Erreur : " + e.getMessage());
        }
        return "redirect:/admin/courses/" + courseId + "/enrollment";
    }

    /**
     * Désinscrire une classe complète d'un cours
     */
    @PostMapping("/unenroll-class/{classId}")
    public String unenrollClass(@PathVariable Long courseId,
                                @PathVariable Long classId,
                                Authentication auth,
                                RedirectAttributes ra) {
        try {
            User admin = userRepository.findByEmail(auth.getName())
                    .orElseThrow(() -> new RuntimeException("Admin introuvable"));

            int count = bulkEnrollmentService.unenrollClassFromCourse(courseId, classId, admin.getId());

            ra.addFlashAttribute("success", count + " étudiant(s) désinscrit(s) avec succès");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Erreur : " + e.getMessage());
        }
        return "redirect:/admin/courses/" + courseId + "/enrollment";
    }

    /**
     * ✅ CORRIGÉ : Méthode pour récupérer les classes inscrites au cours
     */
    private List<AcademicClass> getEnrolledClasses(Long courseId) {
        // 1️⃣ Récupérer tous les étudiants inscrits au cours
        List<Long> enrolledStudentIds = enrollmentRepository.findByCourseId(courseId).stream()
                .map(e -> e.getStudent().getId())
                .distinct()
                .collect(Collectors.toList());

        if (enrolledStudentIds.isEmpty()) {
            return List.of();
        }

        // 2️⃣ Récupérer toutes les inscriptions de classes actives
        List<InscriptionsClass> allInscriptions = inscriptionsClassRepository.findByStatut("ACTIF");

        // 3️⃣ Filtrer pour ne garder que les classes dont au moins un étudiant est inscrit au cours
        List<Long> enrolledClassIds = allInscriptions.stream()
                .filter(ic -> enrolledStudentIds.contains(ic.getEtudiant().getId()))
                .map(ic -> ic.getClasse().getId())
                .distinct()
                .collect(Collectors.toList());

        // 4️⃣ Récupérer les objets AcademicClass correspondants
        return enrolledClassIds.stream()
                .map(classId -> academicClassRepository.findById(classId).orElse(null))
                .filter(c -> c != null)
                .distinct()
                .collect(Collectors.toList());
    }
}