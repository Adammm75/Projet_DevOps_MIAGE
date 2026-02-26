package org.example.devopslearning.services;

import lombok.RequiredArgsConstructor;
import org.example.devopslearning.entities.Cours;
import org.example.devopslearning.entities.Message;
import org.example.devopslearning.entities.User;
import org.example.devopslearning.repositories.CoursRepository;
import org.example.devopslearning.repositories.MessageRepository;
import org.example.devopslearning.repositories.UserRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MessagingService {

    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final CoursRepository coursRepository;

    public List<Message> inbox(Long userId) {
        return messageRepository.findByRecipientIdOrderBySentAtDesc(userId);
    }

    public List<Message> sent(Long userId) {
        return messageRepository.findBySenderIdOrderBySentAtDesc(userId);
    }

    public Message sendMessage(User sender, String recipientEmail,
                               Long courseId, String subject, String content) {

        User recipient = userRepository.findByEmail(recipientEmail)
                .orElseThrow(() -> new IllegalArgumentException("Destinataire introuvable"));

        Message m = new Message();
        // ✅ PAS DE setId() - L'ID est auto-généré par @GeneratedValue !
        m.setSender(sender);
        m.setRecipient(recipient);
        m.setSubject(subject);
        m.setContent(content);
        m.setSentAt(Instant.now());
        m.setIsRead(false);

        // ✅ Si un courseId est fourni, associe le cours
        if (courseId != null) {
            Cours course = coursRepository.findById(courseId).orElse(null);
            m.setCourse(course);
        }

        return messageRepository.save(m);
    }

    /**
     * ✅ Marquer un message comme lu
     */
    public void markAsRead(Long messageId) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new RuntimeException("Message not found"));

        if (!message.getIsRead()) {
            message.setIsRead(true);
            messageRepository.save(message);
        }
    }

    /**
     * ✅ NOUVEAU : Compte le nombre de messages non lus pour un utilisateur
     */
    public long countUnreadMessages(Long userId) {
        return messageRepository.countByRecipientIdAndIsReadFalse(userId);
    }
}