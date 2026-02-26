package org.example.devopslearning.repositories;

import org.example.devopslearning.entities.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    // ========================================
    // RÉCUPÉRATION PAR UTILISATEUR
    // ========================================

    /**
     * Trouve toutes les notifications d'un utilisateur triées par date
     */
    List<Notification> findByUserIdOrderByCreatedAtDesc(Long userId);

    /**
     * Trouve les notifications non lues d'un utilisateur
     */
    List<Notification> findByUserIdAndIsReadFalse(Long userId);

    /**
     * Trouve les notifications lues d'un utilisateur
     */
    List<Notification> findByUserIdAndIsReadTrue(Long userId);

    /**
     * Trouve les notifications non lues triées par date
     */
    List<Notification> findByUserIdAndIsReadFalseOrderByCreatedAtDesc(Long userId);

    /**
     * Trouve les notifications lues triées par date
     */
    List<Notification> findByUserIdAndIsReadTrueOrderByCreatedAtDesc(Long userId);

    // ========================================
    // RÉCUPÉRATION PAR TYPE
    // ========================================

    /**
     * Trouve les notifications par type
     */
    List<Notification> findByUserIdAndTypeOrderByCreatedAtDesc(Long userId, String type);

    /**
     * Trouve les notifications par type (sans tri)
     */
    List<Notification> findByUserIdAndType(Long userId, String type);

    // ========================================
    // RÉCUPÉRATION LIMITÉE
    // ========================================

    /**
     * Récupère les 5 dernières notifications
     */
    List<Notification> findTop5ByUserIdOrderByCreatedAtDesc(Long userId);

    /**
     * Récupère les 10 dernières notifications
     */
    List<Notification> findTop10ByUserIdOrderByCreatedAtDesc(Long userId);

    /**
     * Récupère les 5 dernières notifications non lues
     */
    List<Notification> findTop5ByUserIdAndIsReadFalseOrderByCreatedAtDesc(Long userId);

    // ========================================
    // COMPTEURS
    // ========================================

    /**
     * Compte toutes les notifications d'un utilisateur
     */
    long countByUserId(Long userId);

    /**
     * Compte les notifications non lues
     */
    long countByUserIdAndIsReadFalse(Long userId);

    /**
     * Compte les notifications lues
     */
    long countByUserIdAndIsReadTrue(Long userId);

    /**
     * Compte les notifications par type
     */
    long countByUserIdAndType(Long userId, String type);

    // ========================================
    // VÉRIFICATIONS
    // ========================================

    /**
     * Vérifie si un utilisateur a des notifications non lues
     */
    boolean existsByUserIdAndIsReadFalse(Long userId);
}
