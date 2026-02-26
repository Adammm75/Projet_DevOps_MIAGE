package org.example.devopslearning.controllers;

import lombok.RequiredArgsConstructor;
import org.example.devopslearning.entities.Notification;
import org.example.devopslearning.entities.User;
import org.example.devopslearning.services.NotificationService;
import org.example.devopslearning.services.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;

/**
 * 🔔 CONTRÔLEUR NOTIFICATIONS ÉTUDIANT - SPÉCIFIQUE
 */
@Controller
@RequestMapping("/student/notifications")
@RequiredArgsConstructor
public class StudentNotificationController {

    private final NotificationService notificationService;
    private final UserService userService;

    // ========================================
    // LISTE DES NOTIFICATIONS
    // ========================================

    /**
     * Liste toutes les notifications de l'étudiant
     */
    @GetMapping
    public String list(@RequestParam(defaultValue = "all") String filter,
                       Authentication auth,
                       Model model) {
        User student = userService.findByEmail(auth.getName());

        List<Notification> notifications;

        switch (filter) {
            case "unread":
                notifications = notificationService.getUnreadNotifications(student.getId());
                break;
            case "read":
                notifications = notificationService.getReadNotifications(student.getId());
                break;
            case "important":
                notifications = notificationService.getImportantNotifications(student.getId());
                break;
            default:
                notifications = notificationService.getAllNotifications(student.getId());
        }

        long unreadCount = notificationService.countUnread(student.getId());

        model.addAttribute("notifications", notifications);
        model.addAttribute("unreadCount", unreadCount);
        model.addAttribute("currentFilter", filter);
        model.addAttribute("totalNotifications", notifications.size());

        return "notifications/student-notifications";
    }

    // ========================================
    // MARQUER COMME LU
    // ========================================

    /**
     * Marquer une notification comme lue
     */
    @PostMapping("/{notificationId}/read")
    public String markAsRead(@PathVariable Long notificationId,
                             Authentication auth,
                             RedirectAttributes ra) {
        try {
            User student = userService.findByEmail(auth.getName());
            Notification notification = notificationService.getNotification(notificationId);

            // Vérifier que c'est bien la notification de l'étudiant
            if (!notification.getUser().getId().equals(student.getId())) {
                ra.addFlashAttribute("error", "Accès non autorisé");
                return "redirect:/student/notifications";
            }

            notificationService.markAsRead(notificationId);
            return "redirect:/student/notifications";

        } catch (Exception e) {
            ra.addFlashAttribute("error", "Erreur : " + e.getMessage());
            return "redirect:/student/notifications";
        }
    }

    /**
     * Marquer toutes les notifications comme lues
     */
    @PostMapping("/mark-all-read")
    public String markAllAsRead(Authentication auth, RedirectAttributes ra) {
        try {
            User student = userService.findByEmail(auth.getName());
            notificationService.markAllAsRead(student.getId());

            ra.addFlashAttribute("success", "Toutes les notifications ont été marquées comme lues");
            return "redirect:/student/notifications";

        } catch (Exception e) {
            ra.addFlashAttribute("error", "Erreur : " + e.getMessage());
            return "redirect:/student/notifications";
        }
    }

    // ========================================
    // SUPPRIMER
    // ========================================

    /**
     * Supprimer une notification
     */
    @PostMapping("/{notificationId}/delete")
    public String delete(@PathVariable Long notificationId,
                         Authentication auth,
                         RedirectAttributes ra) {
        try {
            User student = userService.findByEmail(auth.getName());
            Notification notification = notificationService.getNotification(notificationId);

            // Vérifier que c'est bien la notification de l'étudiant
            if (!notification.getUser().getId().equals(student.getId())) {
                ra.addFlashAttribute("error", "Accès non autorisé");
                return "redirect:/student/notifications";
            }

            notificationService.deleteNotification(notificationId);
            ra.addFlashAttribute("success", "Notification supprimée");
            return "redirect:/student/notifications";

        } catch (Exception e) {
            ra.addFlashAttribute("error", "Erreur : " + e.getMessage());
            return "redirect:/student/notifications";
        }
    }

    /**
     * Supprimer toutes les notifications lues
     */
    @PostMapping("/delete-all-read")
    public String deleteAllRead(Authentication auth, RedirectAttributes ra) {
        try {
            User student = userService.findByEmail(auth.getName());
            int deleted = notificationService.deleteAllRead(student.getId());

            ra.addFlashAttribute("success", deleted + " notification(s) supprimée(s)");
            return "redirect:/student/notifications";

        } catch (Exception e) {
            ra.addFlashAttribute("error", "Erreur : " + e.getMessage());
            return "redirect:/student/notifications";
        }
    }

    /**
     * Supprimer TOUTES les notifications (confirmé)
     */
    @PostMapping("/delete-all")
    public String deleteAll(Authentication auth, RedirectAttributes ra) {
        try {
            User student = userService.findByEmail(auth.getName());
            int deleted = notificationService.deleteAllNotifications(student.getId());

            ra.addFlashAttribute("success", "Toutes les notifications ont été supprimées (" + deleted + ")");
            return "redirect:/student/notifications";

        } catch (Exception e) {
            ra.addFlashAttribute("error", "Erreur : " + e.getMessage());
            return "redirect:/student/notifications";
        }
    }

    // ========================================
    // DÉTAILS D'UNE NOTIFICATION
    // ========================================

    /**
     * Voir les détails d'une notification
     */
    @GetMapping("/{notificationId}")
    public String viewDetails(@PathVariable Long notificationId,
                              Authentication auth,
                              Model model,
                              RedirectAttributes ra) {
        try {
            User student = userService.findByEmail(auth.getName());
            Notification notification = notificationService.getNotification(notificationId);

            // Vérifier que c'est la notification de l'étudiant
            if (!notification.getUser().getId().equals(student.getId())) {
                ra.addFlashAttribute("error", "Accès non autorisé");
                return "redirect:/student/notifications";
            }

            // Marquer comme lue automatiquement
            if (!notification.getIsRead()) {
                notificationService.markAsRead(notificationId);
            }

            model.addAttribute("notification", notification);

            return "notifications/student-notification-detail";

        } catch (Exception e) {
            ra.addFlashAttribute("error", "Erreur : " + e.getMessage());
            return "redirect:/student/notifications";
        }
    }

    // ========================================
    // API AJAX (pour le badge temps réel)
    // ========================================

    /**
     * Récupérer le nombre de notifications non lues (AJAX)
     */
    @GetMapping("/api/unread-count")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getUnreadCount(Authentication auth) {
        User student = userService.findByEmail(auth.getName());
        long count = notificationService.countUnread(student.getId());

        return ResponseEntity.ok(Map.of(
                "count", count,
                "hasUnread", count > 0
        ));
    }

    /**
     * Récupérer les dernières notifications (AJAX)
     */
    @GetMapping("/api/recent")
    @ResponseBody
    public ResponseEntity<List<Notification>> getRecentNotifications(Authentication auth,
                                                                     @RequestParam(defaultValue = "5") int limit) {
        User student = userService.findByEmail(auth.getName());
        List<Notification> notifications = notificationService.getRecentNotifications(student.getId(), limit);

        return ResponseEntity.ok(notifications);
    }

    /**
     * Marquer comme lue via AJAX
     */
    @PostMapping("/api/{notificationId}/read")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> markAsReadAjax(@PathVariable Long notificationId,
                                                              Authentication auth) {
        try {
            User student = userService.findByEmail(auth.getName());
            Notification notification = notificationService.getNotification(notificationId);

            if (!notification.getUser().getId().equals(student.getId())) {
                return ResponseEntity.status(403).body(Map.of("error", "Non autorisé"));
            }

            notificationService.markAsRead(notificationId);
            long newCount = notificationService.countUnread(student.getId());

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "unreadCount", newCount
            ));

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ========================================
    // FILTRES PAR TYPE
    // ========================================

    /**
     * Notifications de type "Devoir"
     */
    @GetMapping("/assignments")
    public String assignmentNotifications(Authentication auth, Model model) {
        User student = userService.findByEmail(auth.getName());

        List<Notification> notifications = notificationService.getNotificationsByType(
                student.getId(), "ASSIGNMENT"
        );

        model.addAttribute("notifications", notifications);
        model.addAttribute("currentFilter", "assignments");
        model.addAttribute("filterTitle", "Notifications de devoirs");

        return "notifications/student-notifications";
    }

    /**
     * Notifications de type "Note"
     */
    @GetMapping("/grades")
    public String gradeNotifications(Authentication auth, Model model) {
        User student = userService.findByEmail(auth.getName());

        List<Notification> notifications = notificationService.getNotificationsByType(
                student.getId(), "GRADE"
        );

        model.addAttribute("notifications", notifications);
        model.addAttribute("currentFilter", "grades");
        model.addAttribute("filterTitle", "Notifications de notes");

        return "notifications/student-notifications";
    }

    /**
     * Notifications de type "Cours"
     */
    @GetMapping("/courses")
    public String courseNotifications(Authentication auth, Model model) {
        User student = userService.findByEmail(auth.getName());

        List<Notification> notifications = notificationService.getNotificationsByType(
                student.getId(), "COURSE"
        );

        model.addAttribute("notifications", notifications);
        model.addAttribute("currentFilter", "courses");
        model.addAttribute("filterTitle", "Notifications de cours");

        return "notifications/student-notifications";
    }

    /**
     * Notifications de type "Système"
     */
    @GetMapping("/system")
    public String systemNotifications(Authentication auth, Model model) {
        User student = userService.findByEmail(auth.getName());

        List<Notification> notifications = notificationService.getNotificationsByType(
                student.getId(), "SYSTEM"
        );

        model.addAttribute("notifications", notifications);
        model.addAttribute("currentFilter", "system");
        model.addAttribute("filterTitle", "Notifications système");

        return "notifications/student-notifications";
    }
}