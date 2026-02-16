package org.example.devopslearning.services;

import lombok.RequiredArgsConstructor;
import org.example.devopslearning.entities.Notification;
import org.example.devopslearning.entities.User;
import org.example.devopslearning.entities.Cours;
import org.example.devopslearning.entities.InactivityAlert;
import org.example.devopslearning.repositories.NotificationRepository;
import org.example.devopslearning.repositories.UserRepository;
import org.example.devopslearning.repositories.CoursRepository;
import org.example.devopslearning.repositories.InactivityAlertRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service de gestion des notifications
 */
@Service
@RequiredArgsConstructor
@Transactional
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final CoursRepository coursRepository;
    private final InactivityAlertRepository inactivityAlertRepository;

    // ================== CRÉATION DE NOTIFICATIONS ==================

    /**
     * Crée une nouvelle notification pour un utilisateur
     */
    public void createNotification(Long userId, String type, String title, String content, 
                                   Long relatedCourseId, Long relatedAlertId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable avec l'ID: " + userId));

        // Créer le builder avec les champs de base
        Notification.NotificationBuilder builder = Notification.builder()
                .user(user)
                .type(type)
                .title(title)
                .content(content)
                .isRead(false);

        // Ajouter les relations optionnelles
        if (relatedCourseId != null) {
            coursRepository.findById(relatedCourseId)
                    .ifPresent(builder::relatedCourse);
        }

        if (relatedAlertId != null) {
            inactivityAlertRepository.findById(relatedAlertId)
                    .ifPresent(builder::relatedAlert);
        }

        Notification notification = builder.build();
        notificationRepository.save(notification);
    }

    /**
     * Crée une notification simple sans relations
     */
    public void createSimpleNotification(Long userId, String type, String title, String content) {
        createNotification(userId, type, title, content, null, null);
    }

    // ================== RÉCUPÉRATION DE NOTIFICATIONS ==================

    /**
     * Récupère toutes les notifications d'un utilisateur
     */
    @Transactional(readOnly = true)
    public List<Notification> getAllByUser(Long userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    /**
     * Récupère les notifications non lues d'un utilisateur
     */
    @Transactional(readOnly = true)
    public List<Notification> getUnreadByUser(Long userId) {
        return notificationRepository.findByUserIdAndIsReadFalse(userId);
    }

    /**
     * Compte les notifications non lues d'un utilisateur
     */
    @Transactional(readOnly = true)
    public long countUnreadByUser(Long userId) {
        return notificationRepository.findByUserIdAndIsReadFalse(userId).size();
    }

    // ================== MÉTHODES POUR ADMIN - FILTRAGE PAR TYPE ==================

    /**
     * Alertes système critiques (SYSTEM_ERROR, SECURITY_ALERT, DATABASE_FULL)
     */
    @Transactional(readOnly = true)
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
    @Transactional(readOnly = true)
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
    @Transactional(readOnly = true)
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
    @Transactional(readOnly = true)
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
    @Transactional(readOnly = true)
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
    @Transactional(readOnly = true)
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

    // ================== GESTION DES NOTIFICATIONS ==================

    /**
     * Marque une notification comme lue
     */
    public void markAsRead(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification introuvable avec l'ID: " + notificationId));

        notification.setIsRead(true);
        notificationRepository.save(notification);
    }

    /**
     * Marque toutes les notifications d'un utilisateur comme lues
     */
    public void markAllAsRead(Long userId) {
        List<Notification> notifications = notificationRepository.findByUserIdAndIsReadFalse(userId);
        notifications.forEach(n -> n.setIsRead(true));
        notificationRepository.saveAll(notifications);
    }

    /**
     * Supprime une notification
     */
    public void deleteNotification(Long notificationId) {
        if (!notificationRepository.existsById(notificationId)) {
            throw new RuntimeException("Notification introuvable avec l'ID: " + notificationId);
        }
        notificationRepository.deleteById(notificationId);
    }
}