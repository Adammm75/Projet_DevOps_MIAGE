package org.example.devopslearning.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.example.devopslearning.entities.AuditLog;
import org.example.devopslearning.entities.User;
import org.example.devopslearning.repositories.AuditLogRepository;
import org.example.devopslearning.repositories.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditLogRepository auditLogRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    /**
     * Enregistre une action dans l'audit
     */
    @Transactional
    public void log(Long actorId, String action, String entityType, Long entityId, Object beforeData, Object afterData) {
        User actor = userRepository.findById(actorId)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

        AuditLog log = new AuditLog();
        log.setActor(actor);
        log.setAction(action);
        log.setEntityType(entityType);
        log.setEntityId(entityId);
        log.setBeforeData(toJson(beforeData));
        log.setAfterData(toJson(afterData));
        log.setIpAddress(getClientIp());
        log.setCreatedAt(Instant.now());

        auditLogRepository.save(log);
    }

    /**
     * Surcharge pour log simple sans before/after
     */
    @Transactional
    public void log(Long actorId, String action, String entityType, Long entityId, String details) {
        log(actorId, action, entityType, entityId, null, details);
    }

    /**
     * Récupère tous les logs
     */
    public List<AuditLog> getAllLogs() {
        return auditLogRepository.findTop50ByOrderByCreatedAtDesc();
    }

    /**
     * Récupère les logs d'un utilisateur
     */
    public List<AuditLog> getLogsByUser(Long userId) {
        return auditLogRepository.findByActorIdOrderByCreatedAtDesc(userId);
    }

    /**
     * Récupère les logs par type d'entité
     */
    public List<AuditLog> getLogsByEntityType(String entityType) {
        return auditLogRepository.findByEntityTypeOrderByCreatedAtDesc(entityType);
    }

    /**
     * Récupère les logs pour une entité spécifique
     */
    public List<AuditLog> getLogsByEntity(String entityType, Long entityId) {
        return auditLogRepository.findByEntityTypeAndEntityIdOrderByCreatedAtDesc(entityType, entityId);
    }

    /**
     * Recherche dans les logs
     */
    public List<AuditLog> searchLogs(String search) {
        return auditLogRepository.searchLogs(search);
    }

    /**
     * Convertit un objet en JSON
     */
    private String toJson(Object obj) {
        if (obj == null) return null;
        if (obj instanceof String) return (String) obj;

        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            return obj.toString();
        }
    }

    /**
     * Récupère l'adresse IP du client
     */
    private String getClientIp() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();

                String ip = request.getHeader("X-Forwarded-For");
                if (ip == null || ip.isEmpty()) {
                    ip = request.getHeader("X-Real-IP");
                }
                if (ip == null || ip.isEmpty()) {
                    ip = request.getRemoteAddr();
                }
                return ip;
            }
        } catch (Exception e) {
            // Ignore
        }
        return "unknown";
    }
}