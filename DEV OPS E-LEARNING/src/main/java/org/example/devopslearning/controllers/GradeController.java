package org.example.devopslearning.controllers;

import lombok.RequiredArgsConstructor;
import org.example.devopslearning.entities.Cours;
import org.example.devopslearning.entities.NotesCour;
import org.example.devopslearning.repositories.CoursRepository;
import org.example.devopslearning.services.GradeService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@Controller
@RequestMapping("/admin/courses/{courseId}/grades")
@RequiredArgsConstructor
public class GradeController {

    private final GradeService gradeService;
    private final CoursRepository coursRepository;

    /**
     * Affiche la page de gestion des notes
     */
    @GetMapping
    public String manageGrades(@PathVariable Long courseId, Model model) {
        Cours cours = coursRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Cours introuvable"));

        List<NotesCour> grades = gradeService.getGradesByCourse(courseId);
        BigDecimal average = gradeService.calculateCourseAverage(courseId);

        // ✅ AJOUTÉ : Calculer le nombre de notes saisies
        long notesCount = grades.stream()
                .filter(g -> g.getNoteFinale() != null)
                .count();

        model.addAttribute("cours", cours);
        model.addAttribute("grades", grades);
        model.addAttribute("average", average);
        model.addAttribute("notesCount", notesCount);  // ✅ NOUVEAU

        return "admin/grades/manage";
    }

    /**
     * Met à jour une note
     */
    @PostMapping("/{gradeId}/update")
    public String updateGrade(
            @PathVariable Long courseId,
            @PathVariable Long gradeId,
            @RequestParam BigDecimal noteFinale) {

        gradeService.updateGrade(gradeId, noteFinale);
        return "redirect:/admin/courses/" + courseId + "/grades";
    }

    /**
     * Verrouille toutes les notes
     */
    @PostMapping("/lock")
    public String lockGrades(@PathVariable Long courseId) {
        gradeService.lockAllGrades(courseId);
        return "redirect:/admin/courses/" + courseId + "/grades";
    }

    /**
     * Déverrouille toutes les notes
     */
    @PostMapping("/unlock")
    public String unlockGrades(@PathVariable Long courseId) {
        gradeService.unlockAllGrades(courseId);
        return "redirect:/admin/courses/" + courseId + "/grades";
    }
}