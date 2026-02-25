package org.example.devopslearning.controllers;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.example.devopslearning.entities.AcademicClass;
import org.example.devopslearning.entities.Cours;
import org.example.devopslearning.entities.Filiere;
import org.example.devopslearning.entities.User;
import org.example.devopslearning.repositories.AcademicClassRepository;
import org.example.devopslearning.repositories.FiliereRepository;
import org.example.devopslearning.repositories.UserRepository;
import org.example.devopslearning.services.CoursService;
import org.example.devopslearning.services.CourseTargetingService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/courses")
public class AdminCoursController {

    private final CoursService coursService;
    private final UserRepository userRepository;
    private final CourseTargetingService courseTargetingService;
    private final FiliereRepository filiereRepository;
    private final AcademicClassRepository classeRepository;

    /**
     * Liste de tous les cours
     */
    @GetMapping
    public String listCourses(@RequestParam(required = false) String search, Model model) {
        List<Cours> courses;

        if (search != null && !search.trim().isEmpty()) {
            courses = coursService.searchCourses(search);
            model.addAttribute("search", search);
        } else {
            courses = coursService.getAllCoursesOrderedByDate();
        }

        // Liste des enseignants pour le dropdown
        List<User> teachers = userRepository.findAll().stream()
                .filter(u -> u.hasRole("ROLE_TEACHER") || u.hasRole("ROLE_ADMIN"))
                .toList();

        model.addAttribute("courses", courses);
        model.addAttribute("teachers", teachers);
        model.addAttribute("totalCourses", courses.size());

        return "admin/courses";
    }

    /**
     * Export Excel
     */
    @GetMapping("/export")
    public void exportToExcel(HttpServletResponse response) throws IOException {
        List<Cours> courses = coursService.getAllCoursesOrderedByDate();

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Cours");

        CellStyle headerStyle = workbook.createCellStyle();
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerFont.setFontHeightInPoints((short) 12);
        headerStyle.setFont(headerFont);
        headerStyle.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        Row headerRow = sheet.createRow(0);
        String[] columns = {"ID", "Code", "Titre", "Description", "Enseignant", "Email", "Date de création"};

        for (int i = 0; i < columns.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(columns[i]);
            cell.setCellStyle(headerStyle);
        }

        int rowNum = 1;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

        for (Cours cours : courses) {
            Row row = sheet.createRow(rowNum++);

            row.createCell(0).setCellValue(cours.getId() != null ? cours.getId().toString() : "");
            row.createCell(1).setCellValue(cours.getCode() != null ? cours.getCode() : "");
            row.createCell(2).setCellValue(cours.getTitle() != null ? cours.getTitle() : "");
            row.createCell(3).setCellValue(cours.getDescription() != null ? cours.getDescription() : "");

            if (cours.getCreatedBy() != null) {
                row.createCell(4).setCellValue(cours.getCreatedBy().getFirstName() + " " + cours.getCreatedBy().getLastName());
                row.createCell(5).setCellValue(cours.getCreatedBy().getEmail());
            } else {
                row.createCell(4).setCellValue("N/A");
                row.createCell(5).setCellValue("N/A");
            }

            if (cours.getCreatedAt() != null) {
                String date = cours.getCreatedAt().atZone(ZoneId.systemDefault()).format(formatter);
                row.createCell(6).setCellValue(date);
            } else {
                row.createCell(6).setCellValue("");
            }
        }

        for (int i = 0; i < columns.length; i++) {
            sheet.autoSizeColumn(i);
        }

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=cours_export.xlsx");

        workbook.write(response.getOutputStream());
        workbook.close();
    }

    /**
     * Voir les détails d'un cours
     */
    @GetMapping("/{id}")
    public String viewCourse(@PathVariable Long id, Model model) {
        Cours cours = coursService.getById(id);
        model.addAttribute("cours", cours);
        return "courses/course-details";
    }

    // ========== CIBLAGE ==========

    /**
     * Page de ciblage d'un cours spécifique
     */
    @GetMapping("/{id}/targeting")
    public String courseTargeting(@PathVariable Long id, Model model) {
        Cours cours = coursService.getById(id);

        // Récupérer toutes les filières et classes
        List<Filiere> allFilieres = courseTargetingService.getAllFilieres();
        List<AcademicClass> allClasses = courseTargetingService.getAllClasses();

        // Récupérer les IDs déjà ciblés
        List<Long> targetedFiliereIds = courseTargetingService.getTargetedFiliereIds(id);
        List<Long> targetedClasseIds = courseTargetingService.getTargetedClasseIds(id);

        model.addAttribute("cours", cours);
        model.addAttribute("allFilieres", allFilieres);
        model.addAttribute("allClasses", allClasses);
        model.addAttribute("targetedFiliereIds", targetedFiliereIds);
        model.addAttribute("targetedClasseIds", targetedClasseIds);

        return "admin/course-targeting";
    }

    /**
     * Enregistrer le ciblage d'un cours
     */
    @PostMapping("/{id}/targeting")
    public String saveCourseTargeting(@PathVariable Long id,
                                      @RequestParam(required = false) List<Long> filiereIds,
                                      @RequestParam(required = false) List<Long> classeIds,
                                      RedirectAttributes ra) {
        try {
            courseTargetingService.updateCourseTargeting(id, filiereIds, classeIds);
            ra.addFlashAttribute("success", "Ciblage enregistré avec succès");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Erreur : " + e.getMessage());
        }
        return "redirect:/admin/courses/" + id + "/targeting";
    }

    /**
     * Page de ciblage global
     */
    @GetMapping("/course-targeting")
    public String globalCourseTargeting(Model model) {
        List<Cours> courses = coursService.getAllCoursesOrderedByDate();
        model.addAttribute("courses", courses);
        return "admin/global-course-targeting";
    }

    // ========== CRUD ==========

    @GetMapping("/create")
    public String createCourseForm(Model model) {
        model.addAttribute("cours", new Cours());

        List<User> teachers = userRepository.findAll().stream()
                .filter(u -> u.hasRole("ROLE_TEACHER") || u.hasRole("ROLE_ADMIN"))
                .toList();
        model.addAttribute("teachers", teachers);

        return "courses/course-form";
    }

    @PostMapping("/create")
    public String createCourse(@ModelAttribute Cours cours,
                               @RequestParam Long createdByUserId,
                               RedirectAttributes ra) {
        try {
            coursService.createCourseByAdmin(cours, createdByUserId);
            ra.addFlashAttribute("success", "Cours créé avec succès");
            return "redirect:/admin/courses";
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Erreur : " + e.getMessage());
            return "redirect:/admin/courses/create";
        }
    }

    @GetMapping("/{id}/edit")
    public String editCourseForm(@PathVariable Long id, Model model) {
        Cours cours = coursService.getById(id);
        model.addAttribute("cours", cours);
        return "courses/course-edit";
    }

    @PostMapping("/{id}/edit")
    public String updateCourse(@PathVariable Long id,
                               @ModelAttribute Cours cours,
                               RedirectAttributes ra) {
        try {
            coursService.updateCourseByAdmin(id, cours);
            ra.addFlashAttribute("success", "Cours modifié avec succès");
            return "redirect:/admin/courses";
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Erreur : " + e.getMessage());
            return "redirect:/admin/courses/" + id + "/edit";
        }
    }

    @PostMapping("/{id}/delete")
    public String deleteCourse(@PathVariable Long id, RedirectAttributes ra) {
        try {
            coursService.deleteCourse(id);
            ra.addFlashAttribute("success", "Cours supprimé avec succès");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Erreur : " + e.getMessage());
        }
        return "redirect:/admin/courses";
    }
}