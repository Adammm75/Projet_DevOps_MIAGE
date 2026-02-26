package org.example.devopslearning.services;

import lombok.RequiredArgsConstructor;
import org.example.devopslearning.entities.*;
import org.example.devopslearning.repositories.*;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class StudentDashboardService {

    private final CourseAccessService courseAccessService;
    private final AssignmentRepository assignmentRepository;
    private final AssignmentSubmissionRepository submissionRepository;
    private final NotesCourRepository notesCourRepository;
    private final MessageRepository messageRepository;
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    public Map<String, Object> buildDashboard(Long studentId) {

        Map<String, Object> dashboard = new HashMap<>();

        User student = userRepository.findById(studentId)
                .orElseThrow();

        List<Cours> cours = courseAccessService.getCoursAccessibles(studentId);

        List<Assignment> devoirs = cours.stream()
                .flatMap(c -> assignmentRepository.findByCourseId(c.getId()).stream())
                .toList();

        List<NotesCour> notes = notesCourRepository.findByEtudiantId(studentId);
        List<Message> messages =
                messageRepository.findTop5ByRecipientOrderBySentAtDesc(student);

        List<Notification> notifications =
                notificationRepository.findByUserIdAndIsReadFalse(studentId);

        dashboard.put("cours", cours);
        dashboard.put("devoirs", devoirs);
        dashboard.put("notes", notes);
        dashboard.put("messages", messages);
        dashboard.put("notifications", notifications);

        return dashboard;
    }
}
