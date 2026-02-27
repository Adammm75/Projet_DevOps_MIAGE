package org.example.devopslearning.controllers;

import lombok.RequiredArgsConstructor;
import org.example.devopslearning.entities.AbsenceJustification;
import org.example.devopslearning.entities.SessionAttendance;
import org.example.devopslearning.entities.User;
import org.example.devopslearning.services.AbsenceJustificationService;
import org.example.devopslearning.services.S3Service;
import org.example.devopslearning.services.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.UUID;

@Controller
@RequiredArgsConstructor
public class AbsenceJustificationController {

    private final AbsenceJustificationService justificationService;
    private final UserService                  userService;
    private final S3Service                    s3Service;

    // ================================================================
    // ÉTUDIANT : Page soumission justificatif
    // ================================================================
    @GetMapping("/student/justifications")
    public String studentPage(Authentication auth, Model model) {
        User student = userService.findByEmail(auth.getName());
        List<SessionAttendance> absences = justificationService.getAbsencesForStudent(student.getId());
        List<AbsenceJustification> submitted = justificationService.getForStudent(student.getId());
        model.addAttribute("absences", absences);
        model.addAttribute("submitted", submitted);
        model.addAttribute("student", student);
        return "student/student-justification";
    }

    // ================================================================
    // ÉTUDIANT : Soumettre un justificatif
    // ================================================================
    @PostMapping("/student/justifications/submit")
    public String submit(@RequestParam Long attendanceId,
                         @RequestParam String reason,
                         @RequestParam(required = false) MultipartFile file,
                         Authentication auth,
                         RedirectAttributes ra) {
        try {
            User student = userService.findByEmail(auth.getName());

            String fileUrl = null;
            if (file != null && !file.isEmpty()) {
                // Upload vers S3 avec un nom de fichier unique
                String key = "justificatifs/" + student.getId() + "/" + UUID.randomUUID() + "_" + file.getOriginalFilename();
                fileUrl = s3Service.upload(file.getInputStream(), key);
            }

            justificationService.submit(attendanceId, student, reason, fileUrl);
            ra.addFlashAttribute("success", "✅ Justificatif soumis avec succès. L'administration a été notifiée.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Erreur : " + e.getMessage());
        }
        return "redirect:/student/justifications";
    }

    // ================================================================
    // ADMIN : Liste des justificatifs
    // ================================================================
    @GetMapping("/admin/justifications")
    public String adminList(Model model) {
        model.addAttribute("justifications", justificationService.getAllForAdmin());
        model.addAttribute("pendingCount", justificationService.countPending());
        return "admin/admin-justifications";
    }

    // ================================================================
    // ADMIN : Approuver ou refuser
    // ================================================================
    @PostMapping("/admin/justifications/{id}/review")
    public String review(@PathVariable Long id,
                         @RequestParam String decision,
                         @RequestParam(required = false) String adminComment,
                         Authentication auth,
                         RedirectAttributes ra) {
        try {
            User admin = userService.findByEmail(auth.getName());
            boolean approved = "APPROVED".equals(decision);
            justificationService.review(id, admin, approved, adminComment);
            ra.addFlashAttribute("success", approved
                    ? "✅ Justificatif approuvé — l'absence a été convertie en présence."
                    : "❌ Justificatif refusé — l'étudiant a été notifié.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Erreur : " + e.getMessage());
        }
        return "redirect:/admin/justifications";
    }
}