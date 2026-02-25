package org.example.devopslearning.repositories;

import org.example.devopslearning.entities.Message;
import org.example.devopslearning.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {

    /**
     * Récupère tous les messages d'un destinataire, triés par date d'envoi (plus récent en premier)
     */
    List<Message> findByRecipientIdOrderBySentAtDesc(Long recipientId);

    /**
     * Récupère tous les messages envoyés par un utilisateur, triés par date d'envoi (plus récent en premier)
     */
    List<Message> findBySenderIdOrderBySentAtDesc(Long senderId);

    /**
     * Récupère les 5 derniers messages d'un destinataire
     */
    List<Message> findTop5ByRecipientOrderBySentAtDesc(User recipient);

    /**
     * Compte le nombre de messages non lus pour un destinataire
     * Note: utilise "isRead" (Boolean), pas "readAt"
     */
    long countByRecipientIdAndIsReadFalse(Long recipientId);


}
