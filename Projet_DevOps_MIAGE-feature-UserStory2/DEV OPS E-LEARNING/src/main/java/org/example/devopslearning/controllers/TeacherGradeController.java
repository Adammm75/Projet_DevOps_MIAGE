package org.example.devopslearning.controllers;

import com.itextpdf.text.DocumentException;
import lombok.RequiredArgsConstructor;
import org.example.devopslearning.entities.Cours;
import org.example.devopslearning.entities.User;
import org.example.devopslearning.repositories.CoursRepository;
import org.example.devopslearning.repositories.UserRepository;
import org.example.devopslearning.services.GradeExportService;
import org.example.devopslearning.services.TeacherGradeService;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

@Controller
@RequestMapping("/teacher/grades")
@RequiredArgsConstructor
public class TeacherGradeController {

    private final TeacherGradeService gradeService;
    private final CoursRepository coursRepository;
    private final UserRepository userRepository;
    private final GradeExportService exportService;

    /**
     * ✅ Page principale : Liste de tous les cours avec statistiques
     */
    @GetMapping
    public String gradesIndex(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        // Récupérer l'enseignant connecté
        User teacher = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

        // Récupérer les statistiques de tous les cours
        List<TeacherGradeService.CourseGradeStats> coursesStats =
                gradeService.getAllCoursesWithStats(teacher);

        model.addAttribute("coursesStats", coursesStats);
        model.addAttribute("currentPath", "/teacher/grades");

        return "grades2/teacher-grades-list";
    }

    /**
     * ✅ Détails des notes d'un cours spécifique
     */
    @GetMapping("/course/{id}")
    public String courseGrades(@PathVariable Long id,
                               @AuthenticationPrincipal UserDetails userDetails,
                               Model model,
                               RedirectAttributes redirectAttributes) {
        try {
            // Récupérer l'enseignant connecté
            User teacher = userRepository.findByEmail(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

            // Vérifier que le cours appartient à l'enseignant
            Cours cours = coursRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Cours introuvable"));

            if (!cours.getCreatedBy().getId().equals(teacher.getId())) {
                redirectAttributes.addFlashAttribute("error", "Vous n'avez pas accès à ce cours");
                return "redirect:/teacher/grades";
            }

            // Récupérer les statistiques du cours
            TeacherGradeService.CourseGradeStats courseStats = gradeService.getCourseStats(cours);

            // Récupérer les détails des notes des étudiants
            List<TeacherGradeService.StudentGradeDetail> studentGrades =
                    gradeService.getCourseGradeDetails(id);

            // Statistiques avancées
            var gradeDistribution = gradeService.getGradeDistribution(id);
            var successRate = gradeService.getSuccessRate(id);
            var studentsAtRisk = gradeService.getStudentsAtRisk(id);
            var topStudents = gradeService.getTopStudents(id, 5);

            model.addAttribute("cours", cours);
            model.addAttribute("courseStats", courseStats);
            model.addAttribute("studentGrades", studentGrades);
            model.addAttribute("gradeDistribution", gradeDistribution);
            model.addAttribute("successRate", successRate);
            model.addAttribute("studentsAtRisk", studentsAtRisk);
            model.addAttribute("topStudents", topStudents);
            model.addAttribute("currentPath", "/teacher/grades");

            return "grades2/teacher-course-grades";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur: " + e.getMessage());
            return "redirect:/teacher/grades";
        }
    }

    /**
     * ✅ Calculer / Recalculer toutes les notes d'un cours
     */
    @PostMapping("/course/{id}/calculate")
    public String calculateGrades(@PathVariable Long id,
                                  @AuthenticationPrincipal UserDetails userDetails,
                                  RedirectAttributes redirectAttributes) {
        try {
            // Récupérer l'enseignant connecté
            User teacher = userRepository.findByEmail(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

            // Vérifier que le cours appartient à l'enseignant
            Cours cours = coursRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Cours introuvable"));

            if (!cours.getCreatedBy().getId().equals(teacher.getId())) {
                redirectAttributes.addFlashAttribute("error", "Vous n'avez pas accès à ce cours");
                return "redirect:/teacher/grades";
            }

            // Calculer les notes
            gradeService.calculateAllGradesForCourse(id);

            redirectAttributes.addFlashAttribute("success",
                    "Les notes ont été recalculées avec succès !");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error",
                    "Erreur lors du calcul: " + e.getMessage());
        }

        return "redirect:/teacher/grades/course/" + id;
    }

    /**
     * ✅ Export PDF d'un bulletin de cours
     */
    @GetMapping("/course/{id}/export/pdf")
    public ResponseEntity<byte[]> exportPDF(@PathVariable Long id,
                                            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            User teacher = userRepository.findByEmail(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

            Cours cours = coursRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Cours introuvable"));

            // Vérifier que le cours appartient à l'enseignant
            if (!cours.getCreatedBy().getId().equals(teacher.getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            // Générer le PDF
            ByteArrayOutputStream pdfStream = exportService.exportCourseToPDF(cours);
            byte[] pdfBytes = pdfStream.toByteArray();

            // Préparer la réponse
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDisposition(
                    ContentDisposition.builder("attachment")
                            .filename("notes_" + cours.getCode() + ".pdf")
                            .build()
            );

            return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);

        } catch (DocumentException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * ✅ Export Excel d'un bulletin de cours
     */
    @GetMapping("/course/{id}/export/excel")
    public ResponseEntity<byte[]> exportExcel(@PathVariable Long id,
                                              @AuthenticationPrincipal UserDetails userDetails) {
        try {
            User teacher = userRepository.findByEmail(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

            Cours cours = coursRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Cours introuvable"));

            // Vérifier que le cours appartient à l'enseignant
            if (!cours.getCreatedBy().getId().equals(teacher.getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            // Générer l'Excel
            ByteArrayOutputStream excelStream = exportService.exportCourseToExcel(cours);
            byte[] excelBytes = excelStream.toByteArray();

            // Préparer la réponse
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
            headers.setContentDisposition(
                    ContentDisposition.builder("attachment")
                            .filename("notes_" + cours.getCode() + ".xlsx")
                            .build()
            );

            return new ResponseEntity<>(excelBytes, headers, HttpStatus.OK);

        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}