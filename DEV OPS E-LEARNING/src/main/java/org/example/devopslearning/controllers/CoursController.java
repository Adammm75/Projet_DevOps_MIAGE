package org.example.devopslearning.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.devopslearning.dto.CourseCreateRequest;
import org.example.devopslearning.entities.Cours;
import org.example.devopslearning.entities.CourseRessource;
import org.example.devopslearning.entities.User;
import org.example.devopslearning.services.*;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.security.Principal;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/teacher/courses")
@RequiredArgsConstructor
public class CoursController {

    private final CoursService coursService;
    private final UserService userService;
    private final S3StorageService s3StorageService;
    private final ResourceConsultationService consultationService;
    private final CourseCompletionService completionService; // ✅ AJOUTÉ // ✅ AJOUTÉ

    // ✅ LISTE DES COURS
    @GetMapping
    public String list(Authentication auth, Model model) {
        User teacher = userService.findByEmail(auth.getName());
        model.addAttribute("courses", coursService.getCoursesByTeacher(teacher));
        return "courses/teacher-course-list";
    }

    // ✅ FORMULAIRE CRÉATION
    @GetMapping("/new")
    public String newCourse(Model model) {
        model.addAttribute("courseForm", new CourseCreateRequest());
        return "courses/course-form";
    }

    // ✅ CRÉER UN COURS
    @PostMapping
    public String create(@ModelAttribute("courseForm") CourseCreateRequest form,
                         @RequestParam(value = "file", required = false) MultipartFile file,
                         Principal principal,
                         RedirectAttributes ra) throws IOException {
        User teacher = userService.getUserFromPrincipal(principal);
        Cours cours = coursService.createCourse(form, teacher);

        if (file != null && !file.isEmpty()) {
            String fileUrl = s3StorageService.uploadFile(file);
            coursService.addResourceToCourse(cours.getId(), file.getOriginalFilename(), fileUrl);
        }

        ra.addFlashAttribute("success", "Cours créé avec succès");
        return "redirect:/teacher/courses";
    }

    // ✅ FORMULAIRE ÉDITION
    @GetMapping("/{id}/edit")
    public String edit(@PathVariable Long id, Model model) {
        Cours c = coursService.getById(id);
        CourseCreateRequest form = new CourseCreateRequest();
        form.setCode(c.getCode());
        form.setTitle(c.getTitle());
        form.setDescription(c.getDescription());
        model.addAttribute("courseForm", form);
        model.addAttribute("courseId", id);
        return "courses/course-form";
    }

    // ✅ METTRE À JOUR
    @PostMapping("/{id}")
    public String update(@PathVariable Long id,
                         @ModelAttribute("courseForm") @Valid CourseCreateRequest form,
                         BindingResult bindingResult,
                         @RequestParam(value = "file", required = false) MultipartFile file,
                         RedirectAttributes ra) throws IOException {
        if (bindingResult.hasErrors()) return "courses/course-form";

        coursService.updateCourse(id, form);

        if (file != null && !file.isEmpty()) {
            String fileUrl = s3StorageService.uploadFile(file);
            coursService.addResourceToCourse(id, file.getOriginalFilename(), fileUrl);
        }

        ra.addFlashAttribute("success", "Cours modifié avec succès");
        return "redirect:/teacher/courses";
    }

    /**
     * ✅ MODIFIÉ : Détails d'un cours avec stats de consultation des ressources
     */
    @GetMapping("/{id}")
    public String details(@PathVariable Long id, Model model) {
        Cours cours = coursService.getById(id);
        List<CourseRessource> resources = coursService.getResources(id);

        // ✅ AJOUT : map resourceId → nombre d'étudiants ayant consulté
        Map<Long, Long> consultationCounts = consultationService.getConsultationCountPerResource(id);

        // Nombre total d'étudiants inscrits (pour calculer le taux)
        long totalStudents = coursService.countEnrolledStudents(id); // à implémenter si absent

        model.addAttribute("cours", cours);
        model.addAttribute("resources", resources);
        model.addAttribute("assignments", coursService.getAssignments(id));
        model.addAttribute("consultationCounts", consultationCounts);
        model.addAttribute("totalStudents", totalStudents);
        long completedCount = completionService.countCompleted(id);
        model.addAttribute("completedCount", completedCount);

        return "courses/teacher-course-details";
    }

    // ✅ AJOUTER RESSOURCE
    @PostMapping("/{id}/resources/add")
    public String addResource(@PathVariable Long id,
                              @RequestParam("title") String title,
                              @RequestParam("file") MultipartFile file,
                              RedirectAttributes ra) {
        try {
            String fileUrl = s3StorageService.uploadFile(file);
            coursService.addResourceToCourse(id, title, fileUrl);
            ra.addFlashAttribute("success", "Ressource ajoutée avec succès");
        } catch (IOException e) {
            ra.addFlashAttribute("error", "Erreur upload : " + e.getMessage());
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Erreur : " + e.getMessage());
        }
        return "redirect:/teacher/courses/" + id;
    }

    // ✅ MODIFIER RESSOURCE
    @PostMapping("/{courseId}/resources/{resourceId}/edit")
    public String editResource(@PathVariable Long courseId,
                               @PathVariable Long resourceId,
                               @RequestParam("title") String title,
                               RedirectAttributes ra) {
        try {
            coursService.updateResourceTitle(resourceId, title);
            ra.addFlashAttribute("success", "Ressource modifiée avec succès");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Erreur : " + e.getMessage());
        }
        return "redirect:/teacher/courses/" + courseId;
    }

    // ✅ SUPPRIMER RESSOURCE
    @PostMapping("/{courseId}/resources/{resourceId}/delete")
    public String deleteResource(@PathVariable Long courseId,
                                 @PathVariable Long resourceId,
                                 RedirectAttributes ra) {
        try {
            coursService.deleteResource(resourceId);
            ra.addFlashAttribute("success", "Ressource supprimée avec succès");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Erreur : " + e.getMessage());
        }
        return "redirect:/teacher/courses/" + courseId;
    }

    // ✅ SUPPRIMER COURS
    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes ra) {
        try {
            Cours cours = coursService.getById(id);
            coursService.deleteCourse(id);
            ra.addFlashAttribute("success", "Cours '" + cours.getTitle() + "' supprimé");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Erreur : " + e.getMessage());
        }
        return "redirect:/teacher/courses";
    }
}
