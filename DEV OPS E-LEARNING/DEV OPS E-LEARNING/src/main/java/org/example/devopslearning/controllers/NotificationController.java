package org.example.devopslearning.controllers;

import lombok.RequiredArgsConstructor;
import org.example.devopslearning.dto.DashboardStatsDTO;
import org.example.devopslearning.entities.Notification;
import org.example.devopslearning.entities.User;
import org.example.devopslearning.services.DashboardStatsService;
import org.example.devopslearning.services.NotificationService;
import org.example.devopslearning.services.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private final UserService userService;
    private final DashboardStatsService dashboardStatsService;

    /**
     * Liste toutes les notifications avec stats pour ADMIN
     */
    @GetMapping
    public String list(Authentication auth, Model model) {
        User user = userService.findByEmail(auth.getName());
        Long userId = user.getId();

        // Vérifier si l'utilisateur est admin
        boolean isAdmin = user.hasRole("ROLE_ADMIN");

        if (isAdmin) {
            // ===== NOTIFICATIONS ADMIN =====
            List<Notification> systemAlerts = notificationService.getSystemAlertsByUser(userId);
            List<Notification> newUserNotifications = notificationService.getNewUserNotificationsByUser(userId);
            List<Notification> courseNotifications = notificationService.getCourseNotificationsByUser(userId);
            List<Notification> otherNotifications = notificationService.getOtherNotificationsByUser(userId);

            model.addAttribute("systemAlerts", systemAlerts);
            model.addAttribute("newUserNotifications", newUserNotifications);
            model.addAttribute("courseNotifications", courseNotifications);
            model.addAttribute("otherNotifications", otherNotifications);

            // Compteurs
            model.addAttribute("systemAlertCount", systemAlerts.size());
            model.addAttribute("newUserCount", newUserNotifications.size());
            model.addAttribute("courseCount", courseNotifications.size());

            // ===== STATISTIQUES & GRAPHIQUES =====
            DashboardStatsDTO stats = dashboardStatsService.calculateDashboardStats();
            model.addAttribute("stats", stats);

        } else {
            // ===== NOTIFICATIONS ÉTUDIANT/ENSEIGNANT =====
            List<Notification> allNotifications = notificationService.getAllByUser(userId);
            model.addAttribute("notifications", allNotifications);
        }

        // Statistiques générales
        long unreadCount = notificationService.countUnreadByUser(userId);
        model.addAttribute("unreadCount", unreadCount);
        model.addAttribute("hasUnread", unreadCount > 0);
        model.addAttribute("isAdmin", isAdmin);

        return "notifications/list";
    }

    /**
     * Marque une notification comme lue
     */
    @PostMapping("/{id}/read")
    public String markRead(@PathVariable Long id) {
        notificationService.markAsRead(id);
        return "redirect:/notifications";
    }

    /**
     * Marque toutes les notifications comme lues
     */
    @PostMapping("/read-all")
    public String markAllRead(Authentication auth) {
        User user = userService.findByEmail(auth.getName());
        notificationService.markAllAsRead(user.getId());
        return "redirect:/notifications";
    }

    /**
     * Supprime une notification
     */
    @PostMapping("/{id}/delete")
    public String deleteNotification(@PathVariable Long id) {
        notificationService.deleteNotification(id);
        return "redirect:/notifications";
    }
}