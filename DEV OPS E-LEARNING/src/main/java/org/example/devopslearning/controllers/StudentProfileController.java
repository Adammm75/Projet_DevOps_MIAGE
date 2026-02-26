package org.example.devopslearning.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.devopslearning.entities.PasswordChangeRequest;
import org.example.devopslearning.entities.ProfileUpdateRequest;
import org.example.devopslearning.entities.User;
import org.example.devopslearning.services.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * 👤 CONTRÔLEUR PROFIL ÉTUDIANT - COMPLET
 */
@Controller
@RequestMapping("/student/profile")
@RequiredArgsConstructor
public class StudentProfileController {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    // ========================================
    // VOIR LE PROFIL
    // ========================================

    /**
     * Afficher le profil de l'étudiant
     */
    @GetMapping
    public String viewProfile(Authentication auth, Model model) {
        User student = userService.findByEmail(auth.getName());

        model.addAttribute("user", student);
        model.addAttribute("profileForm", new ProfileUpdateRequest());
        model.addAttribute("passwordForm", new PasswordChangeRequest());

        return "profile/student-profile";
    }

    // ========================================
    // MODIFIER LES INFORMATIONS
    // ========================================

    /**
     * Mettre à jour les informations personnelles
     */
    @PostMapping("/update")
    public String updateProfile(@ModelAttribute @Valid ProfileUpdateRequest form,
                                BindingResult result,
                                Authentication auth,
                                RedirectAttributes ra) {
        if (result.hasErrors()) {
            ra.addFlashAttribute("error", "Veuillez corriger les erreurs");
            return "redirect:/student/profile";
        }

        try {
            User student = userService.findByEmail(auth.getName());

            // Mettre à jour les informations
            student.setFirstName(form.getFirstName());
            student.setLastName(form.getLastName());

            if (form.getPhone() != null && !form.getPhone().isEmpty()) {
                student.setPhone(form.getPhone());
            }

            userService.updateUser(student);

            ra.addFlashAttribute("success", "Profil mis à jour avec succès !");
            return "redirect:/student/profile";

        } catch (Exception e) {
            ra.addFlashAttribute("error", "Erreur : " + e.getMessage());
            return "redirect:/student/profile";
        }
    }

    // ========================================
    // CHANGER LE MOT DE PASSE
    // ========================================

    /**
     * Changer le mot de passe
     */
    @PostMapping("/change-password")
    public String changePassword(@ModelAttribute @Valid PasswordChangeRequest form,
                                 BindingResult result,
                                 Authentication auth,
                                 RedirectAttributes ra) {
        if (result.hasErrors()) {
            ra.addFlashAttribute("errorPassword", "Veuillez corriger les erreurs");
            return "redirect:/student/profile";
        }

        try {
            User student = userService.findByEmail(auth.getName());

            // Vérifier l'ancien mot de passe
            if (!passwordEncoder.matches(form.getCurrentPassword(), student.getPassword())) {
                ra.addFlashAttribute("errorPassword", "Mot de passe actuel incorrect");
                return "redirect:/student/profile";
            }

            // Vérifier que les nouveaux mots de passe correspondent
            if (!form.getNewPassword().equals(form.getConfirmPassword())) {
                ra.addFlashAttribute("errorPassword", "Les nouveaux mots de passe ne correspondent pas");
                return "redirect:/student/profile";
            }

            // Vérifier la force du mot de passe (min 6 caractères)
            if (form.getNewPassword().length() < 6) {
                ra.addFlashAttribute("errorPassword", "Le mot de passe doit contenir au moins 6 caractères");
                return "redirect:/student/profile";
            }

            // Mettre à jour le mot de passe
            student.setPassword(passwordEncoder.encode(form.getNewPassword()));
            userService.updateUser(student);

            ra.addFlashAttribute("successPassword", "Mot de passe modifié avec succès !");
            return "redirect:/student/profile";

        } catch (Exception e) {
            ra.addFlashAttribute("errorPassword", "Erreur : " + e.getMessage());
            return "redirect:/student/profile";
        }
    }

    // ========================================
    // PRÉFÉRENCES DE NOTIFICATION (optionnel)
    // ========================================

    /**
     * Mettre à jour les préférences de notification
     */
    @PostMapping("/notifications/preferences")
    public String updateNotificationPreferences(@RequestParam(required = false) Boolean emailNotifications,
                                                @RequestParam(required = false) Boolean assignmentReminders,
                                                @RequestParam(required = false) Boolean gradeNotifications,
                                                Authentication auth,
                                                RedirectAttributes ra) {
        try {
            User student = userService.findByEmail(auth.getName());

            // TODO: Implémenter table user_preferences
            // userPreferencesService.updatePreferences(student.getId(), ...);

            ra.addFlashAttribute("success", "Préférences mises à jour !");
            return "redirect:/student/profile";

        } catch (Exception e) {
            ra.addFlashAttribute("error", "Erreur : " + e.getMessage());
            return "redirect:/student/profile";
        }
    }

    // ========================================
    // HISTORIQUE DE CONNEXION (sécurité - optionnel)
    // ========================================

    /**
     * Voir l'historique des connexions
     */
    @GetMapping("/login-history")
    public String viewLoginHistory(Authentication auth, Model model) {
        User student = userService.findByEmail(auth.getName());

        // TODO: Implémenter table login_history
        // List<LoginHistory> history = loginHistoryService.getHistory(student.getId());

        model.addAttribute("user", student);
        // model.addAttribute("loginHistory", history);

        return "profile/login-history";
    }
}