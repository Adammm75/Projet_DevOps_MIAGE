package org.example.devopslearning.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.devopslearning.dto.CourseCreateRequest;
import org.example.devopslearning.entities.Cours;
import org.example.devopslearning.entities.User;
import org.example.devopslearning.services.CoursService;
import org.example.devopslearning.services.UserService;
import org.example.devopslearning.services.S3StorageService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import org.example.devopslearning.repositories.AttendanceReportRepository;
import org.example.devopslearning.repositories.CourseSessionRepository;

import java.io.IOException;
import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/teacher/courses")
@RequiredArgsConstructor
public class CoursController {

    private final CoursService coursService;
    private final UserService userService;
    private final S3StorageService s3StorageService;
    private final CourseSessionRepository courseSessionRepository;
    private final AttendanceReportRepository attendanceReportRepository;

    // ✅ LISTE DES COURS DE L'ENSEIGNANT
    @GetMapping
    public String list(Authentication auth, Model model) {
        User teacher = userService.findByEmail(auth.getName());
        model.addAttribute("courses", coursService.getCoursesByTeacher(teacher));
        return "courses/teacher-course-list";
    }

    // ✅ FORMULAIRE DE CRÉATION
    @GetMapping("/new")
    public String newCourse(Model model) {
        model.addAttribute("courseForm", new CourseCreateRequest());
        return "courses/course-form";
    }

    // ✅ CRÉER UN NOUVEAU COURS
    @PostMapping
    public String create(@ModelAttribute("courseForm") CourseCreateRequest form,
                         @RequestParam(value = "file", required = false) MultipartFile file,
                         Principal principal,
                         RedirectAttributes ra) throws IOException {
        User teacher = userService.getUserFromPrincipal(principal);

        // 1️⃣ Création du cours
        Cours cours = coursService.createCourse(form, teacher);

        // 2️⃣ Upload fichier → S3 (optionnel)
        if (file != null && !file.isEmpty()) {
            String fileUrl = s3StorageService.uploadFile(file);
            // 3️⃣ Enregistrement en base
            coursService.addResourceToCourse(
                    cours.getId(),
                    file.getOriginalFilename(),
                    fileUrl);
        }

        ra.addFlashAttribute("success", "Cours créé avec succès");
        return "redirect:/teacher/courses";
    }

    // ✅ FORMULAIRE D'ÉDITION
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

    // ✅ METTRE À JOUR UN COURS
    @PostMapping("/{id}")
    public String update(@PathVariable Long id,
                         @ModelAttribute("courseForm") @Valid CourseCreateRequest form,
                         BindingResult bindingResult,
                         @RequestParam(value = "file", required = false) MultipartFile file,
                         RedirectAttributes ra) throws IOException {
        if (bindingResult.hasErrors()) {
            return "courses/course-form";
        }

        // 1️⃣ Mise à jour du cours
        coursService.updateCourse(id, form);

        // 2️⃣ Nouveau fichier → Upload S3
        if (file != null && !file.isEmpty()) {
            String fileUrl = s3StorageService.uploadFile(file);
            coursService.addResourceToCourse(
                    id,
                    file.getOriginalFilename(),
                    fileUrl);
        }

        ra.addFlashAttribute("success", "Cours modifié avec succès");
        return "redirect:/teacher/courses";
    }

    // ✅ DÉTAILS D'UN COURS - VERSION ENSEIGNANT
    @GetMapping("/{id}")
    public String details(@PathVariable Long id, Model model) {
        model.addAttribute("cours", coursService.getById(id));
        model.addAttribute("resources", coursService.getResources(id));
        model.addAttribute("assignments", coursService.getAssignments(id));

        // Séances + indicateur appel fait/non fait
        var sessions = courseSessionRepository.findByCourseId(id);
        Map<Long, Boolean> attendanceDoneMap = new HashMap<Long, Boolean>();
        for (var s : sessions) {
            attendanceDoneMap.put(s.getId(), attendanceReportRepository.existsBySessionId(s.getId()));
        }
        model.addAttribute("sessions", sessions);
        model.addAttribute("attendanceDoneMap", attendanceDoneMap);

        return "courses/teacher-course-details";
    }

    // ✅ AJOUTER UNE RESSOURCE AU COURS
    @PostMapping("/{id}/resources/add")
    public String addResource(@PathVariable Long id,
                              @RequestParam("title") String title,
                              @RequestParam("file") MultipartFile file,
                              RedirectAttributes ra) {
        try {
            // 1️⃣ Upload fichier → S3
            String fileUrl = s3StorageService.uploadFile(file);

            // 2️⃣ Enregistrement en base
            coursService.addResourceToCourse(id, title, fileUrl);
            ra.addFlashAttribute("success", "Ressource ajoutée avec succès");
        } catch (IOException e) {
            ra.addFlashAttribute("error", "Erreur lors de l'upload du fichier: " + e.getMessage());
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Erreur: " + e.getMessage());
        }

        return "redirect:/teacher/courses/" + id;
    }

    // ✅ MODIFIER UNE RESSOURCE (nom uniquement)
    @PostMapping("/{courseId}/resources/{resourceId}/edit")
    public String editResource(@PathVariable Long courseId,
                               @PathVariable Long resourceId,
                               @RequestParam("title") String title,
                               RedirectAttributes ra) {
        try {
            coursService.updateResourceTitle(resourceId, title);
            ra.addFlashAttribute("success", "Ressource modifiée avec succès");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Erreur: " + e.getMessage());
        }

        return "redirect:/teacher/courses/" + courseId;
    }

    // ✅ SUPPRIMER UNE RESSOURCE
    @PostMapping("/{courseId}/resources/{resourceId}/delete")
    public String deleteResource(@PathVariable Long courseId,
                                 @PathVariable Long resourceId,
                                 RedirectAttributes ra) {
        try {
            coursService.deleteResource(resourceId);
            ra.addFlashAttribute("success", "Ressource supprimée avec succès");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Erreur: " + e.getMessage());
        }

        return "redirect:/teacher/courses/" + courseId;
    }

    // ✅ SUPPRIMER UN COURS (avec confirmation)
    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes ra) {
        try {
            Cours cours = coursService.getById(id);
            coursService.deleteCourse(id);
            ra.addFlashAttribute("success", "Le cours '" + cours.getTitle() + "' a été supprimé avec succès");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Erreur lors de la suppression: " + e.getMessage());
        }
        return "redirect:/teacher/courses";
    }

}
