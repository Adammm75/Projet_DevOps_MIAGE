package org.example.devopslearning.controllers;

import com.itextpdf.text.DocumentException;
import lombok.RequiredArgsConstructor;
import org.example.devopslearning.entities.*;
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
import java.util.Map;

@Controller
@RequestMapping("/teacher/grades")
@RequiredArgsConstructor
public class TeacherGradeController {

    private final TeacherGradeService gradeService;
    private final CoursRepository coursRepository;
    private final UserRepository userRepository;
    private final GradeExportService exportService;

    // ========================================
    // PAGE PRINCIPALE : cours regroupés par classe
    // ========================================

    @GetMapping
    public String gradesIndex(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User teacher = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

        // Cours groupés par classe (null = sans classe)
        Map<AcademicClass, List<TeacherGradeService.CourseGradeStats>> byClass =
                gradeService.getCoursesGroupedByClass(teacher);

        model.addAttribute("coursesByClass", byClass);
        model.addAttribute("currentPath", "/teacher/grades");

        return "grades2/teacher-grades-list";
    }

    // ========================================
    // DÉTAILS D'UN COURS : vue avec 3 onglets
    // (Étudiants | Devoirs | QCM)
    // ========================================

    @GetMapping("/course/{id}")
    public String courseGrades(@PathVariable Long id,
                               @RequestParam(defaultValue = "students") String tab,
                               @AuthenticationPrincipal UserDetails userDetails,
                               Model model,
                               RedirectAttributes ra) {
        try {
            User teacher = userRepository.findByEmail(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

            Cours cours = coursRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Cours introuvable"));

            if (!cours.getCreatedBy().getId().equals(teacher.getId())) {
                ra.addFlashAttribute("error", "Vous n'avez pas accès à ce cours");
                return "redirect:/teacher/grades";
            }

            // Stats globales
            TeacherGradeService.CourseGradeStats courseStats = gradeService.getCourseStats(cours);

            // Onglet Étudiants
            List<TeacherGradeService.StudentGradeDetail> studentGrades =
                    gradeService.getCourseGradeDetails(id);

            // Onglet Devoirs
            List<TeacherGradeService.AssignmentGradeDetail> assignmentDetails =
                    gradeService.getAssignmentDetails(id);

            // Onglet QCM
            List<TeacherGradeService.QcmGradeDetail> qcmDetails =
                    gradeService.getQcmDetails(id);

            // Statistiques avancées
            Map<String, Long> gradeDistribution = gradeService.getGradeDistribution(id);
            var successRate = gradeService.getSuccessRate(id);
            var studentsAtRisk = gradeService.getStudentsAtRisk(id);

            model.addAttribute("cours", cours);
            model.addAttribute("courseStats", courseStats);
            model.addAttribute("studentGrades", studentGrades);
            model.addAttribute("assignmentDetails", assignmentDetails);
            model.addAttribute("qcmDetails", qcmDetails);
            model.addAttribute("gradeDistribution", gradeDistribution);
            model.addAttribute("successRate", successRate);
            model.addAttribute("studentsAtRisk", studentsAtRisk);
            model.addAttribute("activeTab", tab);
            model.addAttribute("currentPath", "/teacher/grades");

            return "grades2/teacher-course-grades";

        } catch (Exception e) {
            ra.addFlashAttribute("error", "Erreur: " + e.getMessage());
            return "redirect:/teacher/grades";
        }
    }

    // ========================================
    // RECALCUL DES NOTES
    // ========================================

    @PostMapping("/course/{id}/calculate")
    public String calculateGrades(@PathVariable Long id,
                                  @AuthenticationPrincipal UserDetails userDetails,
                                  RedirectAttributes ra) {
        try {
            User teacher = userRepository.findByEmail(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

            Cours cours = coursRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Cours introuvable"));

            if (!cours.getCreatedBy().getId().equals(teacher.getId())) {
                ra.addFlashAttribute("error", "Accès refusé");
                return "redirect:/teacher/grades";
            }

            gradeService.calculateAllGradesForCourse(id);
            ra.addFlashAttribute("success", "Notes recalculées avec succès (devoirs + QCM) !");

        } catch (Exception e) {
            ra.addFlashAttribute("error", "Erreur lors du calcul : " + e.getMessage());
        }

        return "redirect:/teacher/grades/course/" + id;
    }

    // ========================================
    // EXPORTS
    // ========================================

    @GetMapping("/course/{id}/export/excel")
    public ResponseEntity<byte[]> exportExcel(@PathVariable Long id,
                                              @AuthenticationPrincipal UserDetails userDetails) {
        try {
            User teacher = userRepository.findByEmail(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

            Cours cours = coursRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Cours introuvable"));

            if (!cours.getCreatedBy().getId().equals(teacher.getId()))
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();

            ByteArrayOutputStream stream = exportService.exportCourseToExcel(cours);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
            headers.setContentDisposition(
                    ContentDisposition.builder("attachment")
                            .filename("notes_" + cours.getCode() + ".xlsx").build());

            return new ResponseEntity<>(stream.toByteArray(), headers, HttpStatus.OK);

        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/course/{id}/export/pdf")
    public ResponseEntity<byte[]> exportPDF(@PathVariable Long id,
                                            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            User teacher = userRepository.findByEmail(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

            Cours cours = coursRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Cours introuvable"));

            if (!cours.getCreatedBy().getId().equals(teacher.getId()))
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();

            ByteArrayOutputStream stream = exportService.exportCourseToPDF(cours);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDisposition(
                    ContentDisposition.builder("attachment")
                            .filename("notes_" + cours.getCode() + ".pdf").build());

            return new ResponseEntity<>(stream.toByteArray(), headers, HttpStatus.OK);

        } catch (DocumentException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}