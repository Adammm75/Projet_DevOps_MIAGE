package org.example.devopslearning.services;

import lombok.RequiredArgsConstructor;
import org.example.devopslearning.entities.*;
import org.example.devopslearning.repositories.NotificationRepository;
import org.example.devopslearning.repositories.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

/**
 * 🔔 SERVICE NOTIFICATIONS ÉTUDIANT - SPÉCIFIQUE
 * Gestion complète des notifications pour les étudiants
 */
@Service
@RequiredArgsConstructor
public class StudentNotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    // ========================================
    // RÉCUPÉRATION DES NOTIFICATIONS
    // ========================================

    /**
     * Récupère toutes les notifications d'un étudiant
     */
    public List<Notification> getAllNotifications(Long studentId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(studentId);
    }

    /**
     * Récupère les notifications non lues
     */
    public List<Notification> getUnreadNotifications(Long studentId) {
        return notificationRepository.findByUserIdAndIsReadFalse(studentId);
    }

    /**
     * Récupère les notifications lues
     */
    public List<Notification> getReadNotifications(Long studentId) {
        return notificationRepository.findByUserIdAndIsReadTrue(studentId);
    }

    /**
     * Récupère les notifications importantes
     */
    public List<Notification> getImportantNotifications(Long studentId) {
        // Assume qu'il y a un champ "priority" ou "type"
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(studentId).stream()
                .filter(n -> "IMPORTANT".equals(n.getType()) || "URGENT".equals(n.getType()))
                .toList();
    }

    /**
     * Récupère les notifications par type
     */
    public List<Notification> getNotificationsByType(Long studentId, String type) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(studentId).stream()
                .filter(n -> type.equals(n.getType()))
                .toList();
    }

    /**
     * Récupère les N dernières notifications
     */
    public List<Notification> getRecentNotifications(Long studentId, int limit) {
        List<Notification> all = getAllNotifications(studentId);
        return all.subList(0, Math.min(limit, all.size()));
    }

    /**
     * Récupère une notification par ID
     */
    public Notification getNotification(Long notificationId) {
        return notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification introuvable"));
    }

    // ========================================
    // COMPTEURS
    // ========================================

    /**
     * Compte les notifications non lues
     */
    public long countUnread(Long studentId) {
        return notificationRepository.countByUserIdAndIsReadFalse(studentId);
    }

    /**
     * Compte toutes les notifications
     */
    public long countAll(Long studentId) {
        return notificationRepository.countByUserId(studentId);
    }

    // ========================================
    // ACTIONS SUR LES NOTIFICATIONS
    // ========================================

    /**
     * Marquer une notification comme lue
     */
    @Transactional
    public void markAsRead(Long notificationId) {
        Notification notification = getNotification(notificationId);
        if (!notification.getIsRead()) {
            notification.setIsRead(true);
            notificationRepository.save(notification);
        }
    }

    /**
     * Marquer toutes les notifications comme lues
     */
    @Transactional
    public void markAllAsRead(Long studentId) {
        List<Notification> unread = getUnreadNotifications(studentId);
        unread.forEach(n -> n.setIsRead(true));
        notificationRepository.saveAll(unread);
    }

    /**
     * Supprimer une notification
     */
    @Transactional
    public void deleteNotification(Long notificationId) {
        notificationRepository.deleteById(notificationId);
    }

    /**
     * Supprimer toutes les notifications lues
     */
    @Transactional
    public int deleteAllRead(Long studentId) {
        List<Notification> read = getReadNotifications(studentId);
        notificationRepository.deleteAll(read);
        return read.size();
    }

    /**
     * Supprimer toutes les notifications
     */
    @Transactional
    public int deleteAllNotifications(Long studentId) {
        List<Notification> all = getAllNotifications(studentId);
        notificationRepository.deleteAll(all);
        return all.size();
    }

    // ========================================
    // CRÉATION DE NOTIFICATIONS (SYSTÈME)
    // ========================================

    /**
     * Créer une notification pour un étudiant
     */
    @Transactional
    public Notification createNotification(Long studentId, String title, String content, String type) {
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Étudiant introuvable"));

        Notification notification = new Notification();
        notification.setUser(student);
        notification.setTitle(title);
        notification.setContent(content);
        notification.setType(type);
        notification.setIsRead(false);
        notification.setCreatedAt(Instant.now());

        return notificationRepository.save(notification);
    }

    /**
     * Notifier un nouveau devoir
     */
    @Transactional
    public void notifyNewAssignment(Long studentId, String assignmentTitle, String courseName) {
        String title = "Nouveau devoir : " + assignmentTitle;
        String content = "Un nouveau devoir a été publié dans le cours " + courseName;
        createNotification(studentId, title, content, "ASSIGNMENT");
    }

    /**
     * Notifier une nouvelle note
     */
    @Transactional
    public void notifyNewGrade(Long studentId, String assignmentTitle, String grade) {
        String title = "Nouvelle note disponible";
        String content = "Votre note pour " + assignmentTitle + " est disponible : " + grade + "/20";
        createNotification(studentId, title, content, "GRADE");
    }

    /**
     * Notifier un rappel de date limite
     */
    @Transactional
    public void notifyDeadlineReminder(Long studentId, String assignmentTitle, String deadline) {
        String title = "Rappel : Date limite proche";
        String content = "Le devoir " + assignmentTitle + " doit être rendu avant le " + deadline;
        createNotification(studentId, title, content, "DEADLINE");
    }

    /**
     * Notifier un nouveau QCM disponible
     */
    @Transactional
    public void notifyNewQcm(Long studentId, String qcmTitle, String courseName) {
        String title = "Nouveau QCM : " + qcmTitle;
        String content = "Un nouveau QCM est disponible dans le cours " + courseName;
        createNotification(studentId, title, content, "QCM");
    }

    /**
     * Notifier une nouvelle ressource de cours
     */
    @Transactional
    public void notifyNewResource(Long studentId, String resourceTitle, String courseName) {
        String title = "Nouvelle ressource ajoutée";
        String content = "Une nouvelle ressource (" + resourceTitle + ") a été ajoutée au cours " + courseName;
        createNotification(studentId, title, content, "RESOURCE");
    }

    /**
     * Notifier un message reçu
     */
    @Transactional
    public void notifyNewMessage(Long studentId, String senderName, String subject) {
        String title = "Nouveau message de " + senderName;
        String content = "Sujet : " + subject;
        createNotification(studentId, title, content, "MESSAGE");
    }

    /**
     * Notification système générique
     */
    @Transactional
    public void notifySystem(Long studentId, String title, String content) {
        createNotification(studentId, title, content, "SYSTEM");
    }

    // ========================================
    // NOTIFICATIONS POUR PLUSIEURS ÉTUDIANTS
    // ========================================

    /**
     * Créer une notification pour plusieurs étudiants
     */
    @Transactional
    public void createBulkNotifications(List<Long> studentIds, String title, String content, String type) {
        for (Long studentId : studentIds) {
            createNotification(studentId, title, content, type);
        }
    }

    /**
     * Notifier tous les étudiants d'une classe
     */
    @Transactional
    public void notifyClass(List<Long> studentIds, String title, String content, String type) {
        createBulkNotifications(studentIds, title, content, type);
    }
}