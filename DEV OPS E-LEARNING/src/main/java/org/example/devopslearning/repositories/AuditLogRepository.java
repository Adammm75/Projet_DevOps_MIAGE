package org.example.devopslearning.repositories;

import org.example.devopslearning.entities.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    /**
     * Trouve tous les logs d'un utilisateur
     */
    List<AuditLog> findByActorIdOrderByCreatedAtDesc(Long actorId);

    /**
     * Trouve tous les logs par type d'entité
     */
    List<AuditLog> findByEntityTypeOrderByCreatedAtDesc(String entityType);

    /**
     * Trouve tous les logs pour une entité spécifique
     */
    List<AuditLog> findByEntityTypeAndEntityIdOrderByCreatedAtDesc(String entityType, Long entityId);

    /**
     * Trouve les logs par action
     */
    List<AuditLog> findByActionOrderByCreatedAtDesc(String action);

    /**
     * Trouve les logs dans une période
     */
    List<AuditLog> findByCreatedAtBetweenOrderByCreatedAtDesc(Instant start, Instant end);

    /**
     * Recherche dans les logs
     */
    @Query("SELECT a FROM AuditLog a WHERE " +
            "LOWER(a.action) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(a.entityType) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "ORDER BY a.createdAt DESC")
    List<AuditLog> searchLogs(@Param("search") String search);

    /**
     * Récupère les N derniers logs
     */
    List<AuditLog> findTop50ByOrderByCreatedAtDesc();
}