package org.example.devopslearning.services;

import lombok.RequiredArgsConstructor;
import org.example.devopslearning.entities.Notification;
import org.example.devopslearning.repositories.NotificationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;

    /**
     * Récupère toutes les notifications d'un utilisateur
     */
    public List<Notification> getAllByUser(Long userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    /**
     * Récupère les notifications non lues d'un utilisateur
     */
    public List<Notification> getUnreadByUser(Long userId) {
        return notificationRepository.findByUserIdAndIsReadFalse(userId);
    }

    // ================== MÉTHODES POUR ADMIN ==================

    /**
     * Alertes système critiques (SYSTEM_ERROR, SECURITY_ALERT, DATABASE_FULL)
     */
    public List<Notification> getSystemAlertsByUser(Long userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .filter(n -> List.of("SYSTEM_ERROR", "SECURITY_ALERT", "DATABASE_FULL", "URGENT")
                        .contains(n.getType().toUpperCase()))
                .collect(Collectors.toList());
    }

    /**
     * Notifications d'utilisateurs (NEW_USER, NEW_STUDENT, NEW_TEACHER, USER_DISABLED)
     */
    public List<Notification> getNewUserNotificationsByUser(Long userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .filter(n -> List.of("NEW_USER", "NEW_STUDENT", "NEW_TEACHER", "USER_DISABLED", "ROLE_CHANGE")
                        .contains(n.getType().toUpperCase()))
                .collect(Collectors.toList());
    }

    /**
     * Notifications de cours (NEW_COURSE, COURSE_UPDATED, COURSE_DELETED, COURSE_INACTIVE)
     */
    public List<Notification> getCourseNotificationsByUser(Long userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .filter(n -> List.of("NEW_COURSE", "COURSE_UPDATED", "COURSE_DELETED", "COURSE_INACTIVE")
                        .contains(n.getType().toUpperCase()))
                .collect(Collectors.toList());
    }

    /**
     * Alertes d'inactivité (INACTIVITY_ALERT, STUDENT_INACTIVE, TEACHER_INACTIVE)
     */
    public List<Notification> getInactivityNotificationsByUser(Long userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .filter(n -> List.of("INACTIVITY_ALERT", "STUDENT_INACTIVE", "TEACHER_INACTIVE")
                        .contains(n.getType().toUpperCase()))
                .collect(Collectors.toList());
    }

    /**
     * Rapports et statistiques (WEEKLY_REPORT, MONTHLY_REPORT, STATS_AVAILABLE)
     */
    public List<Notification> getReportNotificationsByUser(Long userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .filter(n -> List.of("WEEKLY_REPORT", "MONTHLY_REPORT", "STATS_AVAILABLE", "REPORT")
                        .contains(n.getType().toUpperCase()))
                .collect(Collectors.toList());
    }

    /**
     * Autres notifications (tout ce qui n'est pas dans les catégories ci-dessus)
     */
    public List<Notification> getOtherNotificationsByUser(Long userId) {
        List<String> knownTypes = List.of(
                "SYSTEM_ERROR", "SECURITY_ALERT", "DATABASE_FULL", "URGENT",
                "NEW_USER", "NEW_STUDENT", "NEW_TEACHER", "USER_DISABLED", "ROLE_CHANGE",
                "NEW_COURSE", "COURSE_UPDATED", "COURSE_DELETED", "COURSE_INACTIVE",
                "INACTIVITY_ALERT", "STUDENT_INACTIVE", "TEACHER_INACTIVE",
                "WEEKLY_REPORT", "MONTHLY_REPORT", "STATS_AVAILABLE", "REPORT"
        );

        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .filter(n -> !knownTypes.contains(n.getType().toUpperCase()))
                .collect(Collectors.toList());
    }

    /**
     * Compte les notifications non lues d'un utilisateur
     */
    public long countUnreadByUser(Long userId) {
        return notificationRepository.findByUserIdAndIsReadFalse(userId).size();
    }

    /**
     * Marque une notification comme lue
     */
    @Transactional
    public void markAsRead(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification introuvable"));

        notification.setIsRead(true);
        notificationRepository.save(notification);
    }

    /**
     * Marque toutes les notifications d'un utilisateur comme lues
     */
    @Transactional
    public void markAllAsRead(Long userId) {
        List<Notification> notifications = notificationRepository.findByUserIdAndIsReadFalse(userId);
        notifications.forEach(n -> n.setIsRead(true));
        notificationRepository.saveAll(notifications);
    }

    /**
     * Supprime une notification
     */
    @Transactional
    public void deleteNotification(Long notificationId) {
        notificationRepository.deleteById(notificationId);
    }
}