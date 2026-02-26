package org.example.devopslearning.services;

import lombok.RequiredArgsConstructor;
import org.example.devopslearning.entities.*;
import org.example.devopslearning.repositories.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 🔔 SERVICE NOTIFICATIONS - COMPLET (ADMIN + STUDENT)
 */
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    // ========================================
    // RÉCUPÉRATION DES NOTIFICATIONS (NOMS STANDARDS)
    // ========================================

    public List<Notification> getAllNotifications(Long userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public List<Notification> getUnreadNotifications(Long userId) {
        return notificationRepository.findByUserIdAndIsReadFalse(userId);
    }

    public List<Notification> getReadNotifications(Long userId) {
        return notificationRepository.findByUserIdAndIsReadTrue(userId);
    }

    public List<Notification> getImportantNotifications(Long userId) {
        return getAllNotifications(userId).stream()
                .filter(n -> "IMPORTANT".equals(n.getType()) || "URGENT".equals(n.getType()))
                .collect(Collectors.toList());
    }

    public List<Notification> getNotificationsByType(Long userId, String type) {
        return getAllNotifications(userId).stream()
                .filter(n -> type.equals(n.getType()))
                .collect(Collectors.toList());
    }

    public List<Notification> getRecentNotifications(Long userId, int limit) {
        List<Notification> all = getAllNotifications(userId);
        return all.subList(0, Math.min(limit, all.size()));
    }

    public Notification getNotification(Long notificationId) {
        return notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification introuvable"));
    }

    // ========================================
    // ALIAS POUR NotificationController (ADMIN)
    // ========================================

    /**
     * Alias pour getAllNotifications (utilisé par NotificationController)
     */
    public List<Notification> getAllByUser(Long userId) {
        return getAllNotifications(userId);
    }

    /**
     * Alias pour getUnreadNotifications
     */
    public List<Notification> getUnreadByUser(Long userId) {
        return getUnreadNotifications(userId);
    }

    // ========================================
    // MÉTHODES ADMIN - FILTRAGE PAR TYPE
    // ========================================

    /**
     * Alertes système critiques (SYSTEM_ERROR, SECURITY_ALERT, etc.)
     */
    public List<Notification> getSystemAlertsByUser(Long userId) {
        return getAllNotifications(userId).stream()
                .filter(n -> List.of("SYSTEM_ERROR", "SECURITY_ALERT", "DATABASE_FULL", "URGENT", "SYSTEM")
                        .contains(n.getType()))
                .collect(Collectors.toList());
    }

    /**
     * Notifications d'utilisateurs (NEW_USER, NEW_STUDENT, etc.)
     */
    public List<Notification> getNewUserNotificationsByUser(Long userId) {
        return getAllNotifications(userId).stream()
                .filter(n -> List.of("NEW_USER", "NEW_STUDENT", "NEW_TEACHER", "USER_DISABLED", "ROLE_CHANGE")
                        .contains(n.getType()))
                .collect(Collectors.toList());
    }

    /**
     * Notifications de cours
     */
    public List<Notification> getCourseNotificationsByUser(Long userId) {
        return getAllNotifications(userId).stream()
                .filter(n -> List.of("NEW_COURSE", "COURSE_UPDATED", "COURSE_DELETED", "COURSE_INACTIVE", "COURSE")
                        .contains(n.getType()))
                .collect(Collectors.toList());
    }

    /**
     * Alertes d'inactivité
     */
    public List<Notification> getInactivityNotificationsByUser(Long userId) {
        return getAllNotifications(userId).stream()
                .filter(n -> List.of("INACTIVITY_ALERT", "STUDENT_INACTIVE", "TEACHER_INACTIVE", "INACTIVITY_RESOLVED")
                        .contains(n.getType()))
                .collect(Collectors.toList());
    }

    /**
     * Rapports et statistiques
     */
    public List<Notification> getReportNotificationsByUser(Long userId) {
        return getAllNotifications(userId).stream()
                .filter(n -> List.of("WEEKLY_REPORT", "MONTHLY_REPORT", "STATS_AVAILABLE", "REPORT")
                        .contains(n.getType()))
                .collect(Collectors.toList());
    }

    /**
     * Autres notifications
     */
    public List<Notification> getOtherNotificationsByUser(Long userId) {
        List<String> knownTypes = List.of(
                "SYSTEM_ERROR", "SECURITY_ALERT", "DATABASE_FULL", "URGENT", "SYSTEM",
                "NEW_USER", "NEW_STUDENT", "NEW_TEACHER", "USER_DISABLED", "ROLE_CHANGE",
                "NEW_COURSE", "COURSE_UPDATED", "COURSE_DELETED", "COURSE_INACTIVE", "COURSE",
                "INACTIVITY_ALERT", "STUDENT_INACTIVE", "TEACHER_INACTIVE", "INACTIVITY_RESOLVED",
                "WEEKLY_REPORT", "MONTHLY_REPORT", "STATS_AVAILABLE", "REPORT",
                "ASSIGNMENT", "GRADE", "QCM", "RESOURCE", "MESSAGE", "DEADLINE"
        );

        return getAllNotifications(userId).stream()
                .filter(n -> !knownTypes.contains(n.getType()))
                .collect(Collectors.toList());
    }

    // ========================================
    // COMPTEURS
    // ========================================

    public long countUnread(Long userId) {
        return notificationRepository.countByUserIdAndIsReadFalse(userId);
    }

    /**
     * Alias pour countUnread (utilisé par NotificationController)
     */
    public long countUnreadByUser(Long userId) {
        return countUnread(userId);
    }

    public long countAll(Long userId) {
        return notificationRepository.countByUserId(userId);
    }

    // ========================================
    // ACTIONS SUR LES NOTIFICATIONS
    // ========================================

    @Transactional
    public void markAsRead(Long notificationId) {
        Notification notification = getNotification(notificationId);
        if (!notification.getIsRead()) {
            notification.setIsRead(true);
            notificationRepository.save(notification);
        }
    }

    @Transactional
    public void markAllAsRead(Long userId) {
        List<Notification> unread = getUnreadNotifications(userId);
        unread.forEach(n -> n.setIsRead(true));
        notificationRepository.saveAll(unread);
    }

    @Transactional
    public void deleteNotification(Long notificationId) {
        notificationRepository.deleteById(notificationId);
    }

    @Transactional
    public int deleteAllRead(Long userId) {
        List<Notification> read = getReadNotifications(userId);
        notificationRepository.deleteAll(read);
        return read.size();
    }

    @Transactional
    public int deleteAllNotifications(Long userId) {
        List<Notification> all = getAllNotifications(userId);
        notificationRepository.deleteAll(all);
        return all.size();
    }

    // ========================================
    // CRÉATION DE NOTIFICATIONS
    // ========================================

    /**
     * Méthode simple - 4 paramètres
     */
    @Transactional
    public Notification createNotification(Long userId, String title, String content, String type) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

        Notification notification = new Notification();
        notification.setUser(user);
        notification.setTitle(title);
        notification.setContent(content);
        notification.setType(type);
        notification.setIsRead(false);
        notification.setCreatedAt(Instant.now());

        return notificationRepository.save(notification);
    }

    /**
     * Méthode complète - 6 paramètres (pour InactivityAlertService)
     */
    @Transactional
    public Notification createNotification(Long userId, String type, String title, String content,
                                           Long relatedCourseId, Long relatedAlertId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

        Notification notification = new Notification();
        notification.setUser(user);
        notification.setType(type);
        notification.setTitle(title);
        notification.setContent(content);
        notification.setIsRead(false);
        notification.setCreatedAt(Instant.now());

        return notificationRepository.save(notification);
    }

    // ========================================
    // HELPERS
    // ========================================

    @Transactional
    public void notifyNewAssignment(Long studentId, String assignmentTitle, String courseName) {
        createNotification(studentId, "Nouveau devoir : " + assignmentTitle,
                "Un nouveau devoir a été publié dans le cours " + courseName, "ASSIGNMENT");
    }

    @Transactional
    public void notifyNewGrade(Long studentId, String assignmentTitle, String grade) {
        createNotification(studentId, "Nouvelle note disponible",
                "Votre note pour " + assignmentTitle + " est disponible : " + grade + "/20", "GRADE");
    }

    @Transactional
    public void notifyDeadlineReminder(Long studentId, String assignmentTitle, String deadline) {
        createNotification(studentId, "Rappel : Date limite proche",
                "Le devoir " + assignmentTitle + " doit être rendu avant le " + deadline, "DEADLINE");
    }

    @Transactional
    public void notifyNewQcm(Long studentId, String qcmTitle, String courseName) {
        createNotification(studentId, "Nouveau QCM : " + qcmTitle,
                "Un nouveau QCM est disponible dans le cours " + courseName, "QCM");
    }

    @Transactional
    public void notifyNewResource(Long studentId, String resourceTitle, String courseName) {
        createNotification(studentId, "Nouvelle ressource ajoutée",
                "Une nouvelle ressource (" + resourceTitle + ") a été ajoutée au cours " + courseName, "RESOURCE");
    }

    @Transactional
    public void notifyNewMessage(Long studentId, String senderName, String subject) {
        createNotification(studentId, "Nouveau message de " + senderName, "Sujet : " + subject, "MESSAGE");
    }

    @Transactional
    public void notifySystem(Long studentId, String title, String content) {
        createNotification(studentId, title, content, "SYSTEM");
    }

    @Transactional
    public void createBulkNotifications(List<Long> userIds, String title, String content, String type) {
        for (Long userId : userIds) {
            createNotification(userId, title, content, type);
        }
    }

    @Transactional
    public void notifyClass(List<Long> studentIds, String title, String content, String type) {
        createBulkNotifications(studentIds, title, content, type);
    }
}