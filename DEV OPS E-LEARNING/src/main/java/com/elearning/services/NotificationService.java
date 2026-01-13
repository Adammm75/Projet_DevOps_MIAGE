package com.elearning.services;

import com.elearning.notification.Notification;
import com.elearning.repositories.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public void createNotification(Long userId, String type, String title, String content, Long relatedCourseId, Long relatedAlertId) 
    {
        Notification n = Notification.builder()
                .userId(userId)
                .type(type)
                .title(title)
                .content(content)
                .relatedCourseId(relatedCourseId)
                .relatedAlertId(relatedAlertId)
                .isRead(false)
                .build();
                
        if (n != null) {
            notificationRepository.save(n);
        }
    }
}
