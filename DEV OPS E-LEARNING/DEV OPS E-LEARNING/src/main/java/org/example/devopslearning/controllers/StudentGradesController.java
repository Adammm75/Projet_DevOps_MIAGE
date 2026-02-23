package org.example.devopslearning.controllers;

import lombok.RequiredArgsConstructor;
import org.example.devopslearning.entities.NotesCour;
import org.example.devopslearning.entities.User;
import org.example.devopslearning.services.StudentGradesService;
import org.example.devopslearning.services.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 📊 CONTRÔLEUR NOTES ÉTUDIANT - COMPLET
 */
@Controller
@RequestMapping("/student/grades")
@RequiredArgsConstructor
public class StudentGradesController {

    private final StudentGradesService gradesService;
    private final UserService userService;

    // ========================================
    // VUE GLOBALE DES NOTES
    // ========================================

    /**
     * Page principale des notes avec moyennes
     */
    @GetMapping
    public String viewGrades(Authentication auth, Model model) {
        User student = userService.findByEmail(auth.getName());

        // Récupérer toutes les notes
        List<NotesCour> notes = gradesService.getAllGrades(student.getId());

        // Calculer les statistiques
        Map<String, Object> stats = gradesService.calculateStatistics(student.getId());

        // Grouper les notes par cours
        Map<Long, List<NotesCour>> notesByCourse = gradesService.getGradesGroupedByCourse(student.getId());

        model.addAttribute("notes", notes);
        model.addAttribute("notesByCourse", notesByCourse);
        model.addAttribute("stats", stats);

        // Moyenne générale
        BigDecimal averageGrade = (BigDecimal) stats.get("averageGrade");
        model.addAttribute("averageGrade", averageGrade);
        model.addAttribute("totalNotes", notes.size());

        return "grades2/student-grades";
    }

    // ========================================
    // NOTES PAR COURS
    // ========================================

    /**
     * Détails des notes pour un cours spécifique
     */
    @GetMapping("/course/{courseId}")
    public String viewCourseGrades(@PathVariable Long courseId,
                                   Authentication auth,
                                   Model model) {
        User student = userService.findByEmail(auth.getName());

        List<NotesCour> notes = gradesService.getGradesByCourse(student.getId(), courseId);
        Map<String, Object> courseStats = gradesService.getCourseStatistics(student.getId(), courseId);

        model.addAttribute("notes", notes);
        model.addAttribute("courseStats", courseStats);
        model.addAttribute("courseId", courseId);

        return "grades/student-course-grades";
    }

    // ========================================
    // HISTORIQUE & ÉVOLUTION
    // ========================================

    /**
     * Historique complet des notes (chronologique)
     */
    @GetMapping("/history")
    public String viewHistory(Authentication auth, Model model) {
        User student = userService.findByEmail(auth.getName());

        List<NotesCour> notes = gradesService.getGradesOrderedByDate(student.getId());

        model.addAttribute("notes", notes);

        return "grades/student-grades-history";
    }

    /**
     * Évolution des moyennes (graphique - optionnel)
     */
    @GetMapping("/evolution")
    public String viewEvolution(Authentication auth, Model model) {
        User student = userService.findByEmail(auth.getName());

        Map<String, Object> evolutionData = gradesService.getGradesEvolution(student.getId());

        model.addAttribute("evolutionData", evolutionData);

        return "grades/student-grades-evolution";
    }

    // ========================================
    // DÉTAILS D'UNE NOTE
    // ========================================

    /**
     * Détails d'une note spécifique avec commentaire enseignant
     */
    @GetMapping("/detail/{gradeId}")
    public String viewGradeDetail(@PathVariable Long gradeId,
                                  Authentication auth,
                                  Model model) {
        User student = userService.findByEmail(auth.getName());
        NotesCour note = gradesService.getGradeById(gradeId);

        // Vérifier que c'est bien la note de l'étudiant
        if (!note.getEtudiant().getId().equals(student.getId())) {
            return "redirect:/student/grades";
        }

        model.addAttribute("note", note);

        return "grades/student-grade-detail";
    }

    // ========================================
    // EXPORT (bonus)
    // ========================================

    /**
     * Exporter les notes en PDF (optionnel)
     */
    @GetMapping("/export/pdf")
    public String exportPdf(Authentication auth) {
        User student = userService.findByEmail(auth.getName());

        // TODO: Implémenter génération PDF
        // String pdfUrl = gradesService.generatePdfReport(student.getId());

        return "redirect:/student/grades";
    }
}