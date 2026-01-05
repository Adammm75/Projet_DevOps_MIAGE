package org.example.devopslearning.controllers;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.example.devopslearning.entities.User;
import org.example.devopslearning.services.UserExportService;
import org.example.devopslearning.services.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.List;

@Controller
@RequestMapping("/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final UserService userService;
    private final UserExportService userExportService;  // ✅ AJOUTÉ

    @GetMapping
    public String userManagement(
            Authentication auth,
            Model model,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String status) {

        // Récupérer tous les utilisateurs
        List<User> allUsers = userService.getAllUsers();

        // ✅ FILTRER PAR RECHERCHE (nom, prénom, email)
        if (search != null && !search.isEmpty()) {
            String searchLower = search.toLowerCase();
            allUsers = allUsers.stream()
                    .filter(u ->
                            u.getFirstName().toLowerCase().contains(searchLower) ||
                                    u.getLastName().toLowerCase().contains(searchLower) ||
                                    u.getEmail().toLowerCase().contains(searchLower)
                    )
                    .toList();
        }

        // ✅ FILTRER PAR RÔLE
        if (role != null && !role.isEmpty()) {
            allUsers = allUsers.stream()
                    .filter(u -> u.getUserRoles().stream()
                            .anyMatch(ur -> ur.getRole().getName().equals(role)))
                    .toList();
        }

        // Séparer par rôle
        List<User> teachers = allUsers.stream()
                .filter(u -> u.getUserRoles().stream()
                        .anyMatch(ur -> ur.getRole().getName().equals("ROLE_TEACHER")))
                .toList();

        List<User> students = allUsers.stream()
                .filter(u -> u.getUserRoles().stream()
                        .anyMatch(ur -> ur.getRole().getName().equals("ROLE_STUDENT")))
                .toList();

        List<User> admins = allUsers.stream()
                .filter(u -> u.getUserRoles().stream()
                        .anyMatch(ur -> ur.getRole().getName().equals("ROLE_ADMIN")))
                .toList();

        model.addAttribute("allUsers", allUsers);
        model.addAttribute("teachers", teachers);
        model.addAttribute("students", students);
        model.addAttribute("admins", admins);

        // ✅ Garder les valeurs de recherche dans le formulaire
        model.addAttribute("search", search);
        model.addAttribute("selectedRole", role);
        model.addAttribute("selectedStatus", status);

        return "admin/users";
    }

    /**
     * ✅ VOIR un utilisateur
     */
    @GetMapping("/{userId}")
    public String viewUser(@PathVariable Long userId, Model model) {
        User user = userService.findById(userId);
        model.addAttribute("user", user);

        // Récupérer les rôles
        List<String> roles = user.getUserRoles().stream()
                .map(ur -> ur.getRole().getName())
                .toList();
        model.addAttribute("roles", roles);

        return "admin/user-detail";
    }

    /**
     * ✅ FORMULAIRE de modification
     */
    @GetMapping("/{userId}/edit")
    public String editUserForm(@PathVariable Long userId, Model model) {
        User user = userService.findById(userId);
        model.addAttribute("user", user);
        return "admin/user-edit";
    }

    /**
     * ✅ SAUVEGARDER les modifications
     */
    @PostMapping("/{userId}/edit")
    public String updateUser(@PathVariable Long userId,
                             @RequestParam String firstName,
                             @RequestParam String lastName,
                             @RequestParam String email,
                             RedirectAttributes redirectAttributes) {
        try {
            User user = userService.findById(userId);
            user.setFirstName(firstName);
            user.setLastName(lastName);
            user.setEmail(email);
            userService.updateUser(user);

            redirectAttributes.addFlashAttribute("success", "Utilisateur modifié avec succès");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur lors de la modification");
        }

        return "redirect:/admin/users";
    }

    /**
     * ✅ SUPPRIMER un utilisateur
     */
    @PostMapping("/{userId}/delete")
    public String deleteUser(@PathVariable Long userId, RedirectAttributes redirectAttributes) {
        try {
            userService.deleteUser(userId);
            redirectAttributes.addFlashAttribute("success", "Utilisateur supprimé avec succès");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur lors de la suppression : " + e.getMessage());
        }
        return "redirect:/admin/users";
    }

    /**
     * ✅ ACTIVER/DÉSACTIVER un utilisateur
     */
    @PostMapping("/{userId}/toggle-status")
    public String toggleUserStatus(@PathVariable Long userId, RedirectAttributes redirectAttributes) {
        try {
            // TODO: Implémenter l'activation/désactivation
            redirectAttributes.addFlashAttribute("info", "Fonctionnalité à implémenter");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur");
        }
        return "redirect:/admin/users";
    }

    /**
     * ✅ EXPORTER les utilisateurs en EXCEL (.xlsx)
     */
    @GetMapping("/export")
    public void exportUsers(HttpServletResponse response) throws IOException {
        // Récupérer tous les utilisateurs
        List<User> allUsers = userService.getAllUsers();

        // Générer le fichier Excel
        byte[] excelFile = userExportService.exportUsersToExcel(allUsers);

        // Configurer la réponse HTTP
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=utilisateurs.xlsx");
        response.setContentLength(excelFile.length);

        // Écrire le fichier dans la réponse
        response.getOutputStream().write(excelFile);
        response.getOutputStream().flush();
    }
}